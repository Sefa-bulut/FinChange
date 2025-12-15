package com.example.finchange.portfolio.service.impl;

import com.example.finchange.brokerage.service.BrokerageFirmService;
import com.example.finchange.customer.model.CustomerAccount;
import com.example.finchange.customer.repository.CustomerAccountRepository;
import com.example.finchange.execution.model.Order;
import com.example.finchange.execution.model.OrderExecution;
import com.example.finchange.execution.model.enums.TransactionType;
import com.example.finchange.portfolio.model.AccountTransaction;
import com.example.finchange.execution.repository.OrderExecutionRepository;
import com.example.finchange.portfolio.model.CustomerAsset;
import com.example.finchange.portfolio.model.Asset;
import com.example.finchange.portfolio.repository.AccountTransactionRepository;
import com.example.finchange.portfolio.repository.CustomerAssetRepository;
import com.example.finchange.portfolio.repository.AssetRepository;
import com.example.finchange.portfolio.service.PortfolioService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {

    private final CustomerAccountRepository accountRepository;
    private final CustomerAssetRepository customerAssetRepository;
    private final AccountTransactionRepository transactionRepository;
    private final BrokerageFirmService brokerageFirmService;
    private final AssetRepository assetRepository;
    private final OrderExecutionRepository executionRepository;

    @Override
    @Transactional
    public void deposit(Integer accountId, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Yatırılacak tutar pozitif olmalıdır.");
        }

        CustomerAccount account = findAccountById(accountId);
        if (!account.isActive()) {
            throw new IllegalStateException("Pasif hesaba para yatırma işlemi yapılamaz. Hesap ID: " + accountId);
        }
        account.setBalance(account.getBalance().add(amount));

        createTransaction(account, "DEPOSIT", amount, (short) 1, null, null, description);
        accountRepository.save(account);
        log.info("Hesap ID {}: {} tutarında para yatırıldı.", accountId, amount);
    }

    @Override
    @Transactional
    public void applySellExecutionHold(OrderExecution execution) {
        if (execution == null || execution.getOrder() == null) return;
        if (execution.getOrder().getTransactionType() != TransactionType.SELL) return;

        CustomerAccount account = findAccountById(execution.getOrder().getCustomerAccountId());
        Integer customerId = account.getCustomer().getId();
        CustomerAsset asset = findAssetByCustomerIdAndAssetId(customerId, execution.getOrder().getAssetId());

        int executedLots = execution.getExecutedLotAmount();
        if (asset.getBlockedLot() < executedLots) {
            log.warn("SELL execution sonrası blokaj seviyesi beklenenden düşük. CustomerId={} AssetId={} BlockedLot={} ExecutedLots={}",
                    customerId, asset.getAssetId(), asset.getBlockedLot(), executedLots);
        }
        log.info("SELL execution T+2 beklemede. CustomerId={} AssetId={} ExecutedLots={} BlockedLot={}",
                customerId, asset.getAssetId(), executedLots, asset.getBlockedLot());
    }

    @Override
    @Transactional
    public void withdraw(Integer accountId, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Çekilecek tutar pozitif olmalıdır.");
        }
        CustomerAccount account = findAccountById(accountId);
        if (!account.isActive()) {
            throw new IllegalStateException("Pasif hesaptan para çekilemez. Hesap ID: " + accountId);
        }
        BigDecimal availableBalance = account.getBalance().subtract(account.getBlockedBalance());
        if (availableBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("Yetersiz kullanılabilir bakiye.");
        }
        account.setBalance(account.getBalance().subtract(amount));

        createTransaction(account, "WITHDRAWAL", amount, (short) -1, null, null, description);
        accountRepository.save(account);
        log.info("Hesap ID {}: {} tutarında para çekildi.", accountId, amount);
    }

    @Override
    @Transactional
    public void blockBalanceForBuyOrder(Order order) {
        CustomerAccount account = findAccountById(order.getCustomerAccountId());
        BigDecimal commissionRate = brokerageFirmService.getActiveCommissionRate();
        
        BigDecimal orderValue = order.getLimitPrice().multiply(new BigDecimal(order.getInitialLotAmount()));
        BigDecimal commission = orderValue.multiply(commissionRate).setScale(4, RoundingMode.HALF_UP);
        BigDecimal totalBlockAmount = orderValue.add(commission);

        BigDecimal availableBalance = account.getBalance().subtract(account.getBlockedBalance());
        if (availableBalance.compareTo(totalBlockAmount) < 0) {
            throw new IllegalStateException("Emir için yetersiz bakiye. Gerekli: " + totalBlockAmount + ", Mevcut: " + availableBalance);
        }

        account.setBlockedBalance(account.getBlockedBalance().add(totalBlockAmount));
        
        String descBlocked = String.format("%d lot %s @ %.2f TL alım emri için blokaj",
                order.getInitialLotAmount(), getBistCodeForOrder(order), order.getLimitPrice());
        createTransaction(account, "ORDER_BLOCKED", totalBlockAmount, (short) -1, order, null, descBlocked);
        accountRepository.save(account);
        log.info("Hesap ID {}: Emir ID {} için {} tutarında bakiye bloke edildi.", account.getId(), order.getId(), totalBlockAmount);
    }

    @Override
    @Transactional
    public void blockAssetForSellOrder(Order order) {
        CustomerAccount acc = findAccountById(order.getCustomerAccountId());
        Integer customerId = acc.getCustomer().getId();
        Integer assetId = order.getAssetId();
        int lotsToBlock = order.getInitialLotAmount();

        CustomerAsset asset = findAssetByCustomerIdAndAssetId(customerId, assetId);
        int availableLots = asset.getTotalLot() - asset.getBlockedLot();
        if (availableLots < lotsToBlock) {
            throw new IllegalStateException("Emir için yetersiz varlık. Gerekli: " + lotsToBlock + ", Mevcut: " + availableLots);
        }

        asset.setBlockedLot(asset.getBlockedLot() + lotsToBlock);
        customerAssetRepository.saveAndFlush(asset);
        log.info("Müşteri ID {}: Emir ID {} için {} adet {} lotu bloke edildi.", customerId, order.getId(), lotsToBlock, getBistCodeForOrder(order));
    }

    @Override
    @Transactional
    public void releaseBlockForCancelledOrder(Order order) {
        log.info("Emir ID {} için blokaj iadesi işlemi başlatıldı. Durum: {}", order.getId(), order.getStatus());
        
        if (TransactionType.BUY.equals(order.getTransactionType())) {
            CustomerAccount account = findAccountById(order.getCustomerAccountId());
            
            BigDecimal totalBlockedForOrder = transactionRepository.sumAmountByTypeAndOrder("ORDER_BLOCKED", order);
            BigDecimal totalSettledForOrder = transactionRepository.sumAmountByTypeAndOrder("TRADE_SETTLEMENT_DEBIT", order);
            BigDecimal amountToRelease = totalBlockedForOrder.subtract(totalSettledForOrder);

            if (amountToRelease.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("İade edilecek bakiye blokajı bulunmuyor (hesaplanan <= 0), Emir ID: {}", order.getId());
                return;
            }

            if (account.getBlockedBalance().compareTo(amountToRelease) < 0) {
                log.error("KRİTİK HATA: İade edilecek tutar ({}) mevcut blokajdan ({}) büyük! Hesap ID: {}. Mevcut blokaj iade ediliyor.",
                        amountToRelease, account.getBlockedBalance(), account.getId());
                amountToRelease = account.getBlockedBalance();
            }

            if (amountToRelease.compareTo(BigDecimal.ZERO) > 0) {
                account.setBlockedBalance(account.getBlockedBalance().subtract(amountToRelease));
                createTransaction(account, "ORDER_UNBLOCKED", amountToRelease, (short) 1, order, null,
                        "Emir iptali/güncellemesi için blokaj iadesi");
                accountRepository.save(account);
                log.info("Hesap ID {}: {} tutarında bakiye blokajı iade edildi.", account.getId(), amountToRelease);
            }

        } else { // SELL
            CustomerAccount acc = findAccountById(order.getCustomerAccountId());
            Integer customerId = acc.getCustomer().getId();
            CustomerAsset asset = findAssetByCustomerIdAndAssetId(customerId, order.getAssetId());
            
            int lotsToRelease = order.getInitialLotAmount() - order.getFilledLotAmount();
            if (lotsToRelease <= 0) {
                log.warn("İade edilecek lot blokajı bulunmuyor (kalan lot <= 0), Emir ID: {}", order.getId());
                return;
            }

            if (asset.getBlockedLot() < lotsToRelease) {
                 log.error("KRİTİK HATA: İade edilecek lot ({}) mevcut blokajdan ({}) büyük! Müşteri Varlık ID: {}. Mevcut blokaj iade ediliyor.", 
                    lotsToRelease, asset.getBlockedLot(), asset.getId());
                 lotsToRelease = asset.getBlockedLot();
            }

            if (lotsToRelease > 0) {
                asset.setBlockedLot(asset.getBlockedLot() - lotsToRelease);
                customerAssetRepository.save(asset);
                log.info("Müşteri ID {}: {} adet lot blokajı iade edildi.", customerId, lotsToRelease);
            }
        }
    }

    @Override
    @Transactional
    public void settleBuyTransaction(OrderExecution execution) {
        log.info("Alım emri takası netleştiriliyor: Execution ID {}", execution.getId());
        CustomerAccount account = findAccountById(execution.getOrder().getCustomerAccountId());
        Integer customerId = account.getCustomer().getId();
        CustomerAsset asset = findOrCreateAsset(customerId, execution.getOrder().getAssetId());

        BigDecimal totalDebit = execution.getExecutedPrice().multiply(new BigDecimal(execution.getExecutedLotAmount()))
                .add(execution.getCommissionAmount());

        BigDecimal accBalBefore = account.getBalance();
        BigDecimal accBlkBefore = account.getBlockedBalance();
        int assetTotalBefore = asset.getTotalLot();
        int assetBlockedBefore = asset.getBlockedLot();

        if (account.getBlockedBalance().compareTo(totalDebit) < 0) {
            log.warn("Takas sırasında blokeli bakiye yetersiz. Bloke: {}, Gerekli: {}. Fark düzeltiliyor.", account.getBlockedBalance(), totalDebit);
            account.setBalance(account.getBalance().subtract(totalDebit));
            account.setBlockedBalance(BigDecimal.ZERO);
        } else {
            account.setBalance(account.getBalance().subtract(totalDebit));
            account.setBlockedBalance(account.getBlockedBalance().subtract(totalDebit));
        }

        String descBuySettle = String.format("%d lot %s @ %.2f TL alım takası (Komisyon: %.2f)",
                execution.getExecutedLotAmount(), getBistCodeForOrder(execution.getOrder()),
                execution.getExecutedPrice(), execution.getCommissionAmount());
        createTransaction(account, "TRADE_SETTLEMENT_DEBIT", totalDebit, (short) -1, execution.getOrder(), execution, descBuySettle);

        int lotsToUnblock = execution.getExecutedLotAmount();
        if (asset.getBlockedLot() < lotsToUnblock) {
            log.error("KRİTİK TAKAS HATASI: Takası yapılacak lot ({}) mevcut blokajdan ({}) büyük! CustomerAsset ID: {}",
                    lotsToUnblock, asset.getBlockedLot(), asset.getId());
            asset.setBlockedLot(0);
        } else {
            asset.setBlockedLot(asset.getBlockedLot() - lotsToUnblock);
        }

        accountRepository.saveAndFlush(account);
        customerAssetRepository.saveAndFlush(asset);
        log.info(
                "Alım takası tamamlandı: custId={} accId={} assetId={} lots(unblock)={} | AccBalance {} -> {} | AccBlocked {} -> {} | TotalLot sabit {} | BlockedLot {} -> {}",
                customerId,
                account.getId(),
                asset.getAssetId(),
                lotsToUnblock,
                accBalBefore, account.getBalance(),
                accBlkBefore, account.getBlockedBalance(),
                assetTotalBefore,
                assetBlockedBefore, asset.getBlockedLot()
        );

        if (execution.getOrder().getStatus() == com.example.finchange.execution.model.enums.OrderStatus.FILLED) {
            BigDecimal remainingBlock = transactionRepository
                    .sumAmountByTypeAndOrder("ORDER_BLOCKED", execution.getOrder())
                    .subtract(transactionRepository.sumAmountByTypeAndOrder("TRADE_SETTLEMENT_DEBIT", execution.getOrder()));

            if (remainingBlock.compareTo(BigDecimal.ZERO) > 0) {
                if (account.getBlockedBalance().compareTo(remainingBlock) < 0) {
                    log.error("KRİTİK HATA: Kalan blokaj ({}) iade edilemiyor çünkü mevcut blokaj ({}) yetersiz! Hesap ID: {}",
                            remainingBlock, account.getBlockedBalance(), account.getId());
                    account.setBlockedBalance(BigDecimal.ZERO);
                } else {
                    account.setBlockedBalance(account.getBlockedBalance().subtract(remainingBlock));
                }

                createTransaction(account, "ORDER_UNBLOCKED", remainingBlock, (short) 1, execution.getOrder(), null,
                        getBistCodeForOrder(execution.getOrder()) + " emir tamamlama sonrası artık blokaj iadesi");
                accountRepository.save(account);
                log.info("Hesap ID {}: Emir ID {} tamamlandığı için {} tutarında artık blokaj iade edildi.",
                        account.getId(), execution.getOrder().getId(), remainingBlock);
            }
        }
    }

    @Override
    @Transactional
    public void settleSellTransaction(OrderExecution execution) {
        log.info("Satım emri takası netleştiriliyor: Execution ID {}", execution.getId());
        CustomerAccount account = findAccountById(execution.getOrder().getCustomerAccountId());
        Integer customerId = account.getCustomer().getId();
        CustomerAsset asset = findAssetByCustomerIdAndAssetId(customerId, execution.getOrder().getAssetId());

        BigDecimal netIncome = execution.getExecutedPrice().multiply(new BigDecimal(execution.getExecutedLotAmount()))
                .subtract(execution.getCommissionAmount());

        int soldLots = execution.getExecutedLotAmount();
        int assetTotalBefore = asset.getTotalLot();
        int assetBlockedBefore = asset.getBlockedLot();
        if (asset.getBlockedLot() < soldLots || asset.getTotalLot() < soldLots) {
            log.error("SELL settlement tutarsız: custId={} assetId={} blockedLot={} totalLot={} soldLots={}",
                    customerId, asset.getAssetId(), asset.getBlockedLot(), asset.getTotalLot(), soldLots);
            throw new IllegalStateException("SELL settlement: lot tutarsızlığı");
        }
        asset.setBlockedLot(asset.getBlockedLot() - soldLots);
        asset.setTotalLot(asset.getTotalLot() - soldLots);

        BigDecimal accBalBefore = account.getBalance();
        account.setBalance(account.getBalance().add(netIncome));

        String descSellSettle = String.format("%d lot %s @ %.2f TL satım takası (Net Gelir: %.2f)",
                execution.getExecutedLotAmount(), getBistCodeForOrder(execution.getOrder()),
                execution.getExecutedPrice(), netIncome);
        createTransaction(account, "TRADE_SETTLEMENT_CREDIT", netIncome, (short) 1, execution.getOrder(), execution, descSellSettle);

        accountRepository.saveAndFlush(account);
        if (asset.getTotalLot() == 0) {
            customerAssetRepository.delete(asset);
            log.info("Müşteri ID {} için {} varlığı tamamen satıldığı için portföyden kaldırıldı.",
                    customerId, getBistCodeForOrder(execution.getOrder()));
        } else {
            customerAssetRepository.saveAndFlush(asset);
        }
        log.info(
                "Satım takası tamamlandı: custId={} accId={} assetId={} soldLots={} | AccBalance {} -> {} | TotalLot {} -> {} | BlockedLot {} -> {} | NetIncome={}",
                customerId,
                account.getId(),
                asset.getAssetId(),
                soldLots,
                accBalBefore, account.getBalance(),
                assetTotalBefore, asset.getTotalLot(),
                assetBlockedBefore, asset.getBlockedLot(),
                netIncome
        );
    }

    @Override
    @Transactional
    public void blockAssetForBuyExecution(OrderExecution execution) {
        if (execution == null || execution.getOrder() == null) return;
        if (execution.getOrder().getTransactionType() != TransactionType.BUY) return;

        CustomerAccount account = findAccountById(execution.getOrder().getCustomerAccountId());
        Integer customerId = account.getCustomer().getId();
        CustomerAsset asset = findOrCreateAsset(customerId, execution.getOrder().getAssetId());

        int executedLots = execution.getExecutedLotAmount();

        int oldLots = asset.getTotalLot();
        BigDecimal oldTotalValue = asset.getAverageCost().multiply(new BigDecimal(oldLots));
        BigDecimal purchaseValue = execution.getExecutedPrice().multiply(new BigDecimal(executedLots));

        int newTotalLots = oldLots + executedLots;
        if (newTotalLots > 0) {
            BigDecimal newAverageCost = oldTotalValue.add(purchaseValue)
                    .divide(new BigDecimal(newTotalLots), 4, RoundingMode.HALF_UP);
            asset.setAverageCost(newAverageCost);
        }

        asset.setTotalLot(newTotalLots);
        asset.setBlockedLot(asset.getBlockedLot() + executedLots);

        customerAssetRepository.saveAndFlush(asset);
        log.info("BUY execution için T+2 varlık blokajı uygulandı. CustomerId={}, AssetId={}, EklenenLot={}, YeniTotalLot={}, YeniBlockedLot={}",
                customerId, asset.getAssetId(), executedLots, asset.getTotalLot(), asset.getBlockedLot());
    }

    @Override
    @Transactional
    public void increaseHoldingsImmediately(OrderExecution execution) {
        if (execution == null || execution.getOrder() == null) return;
        if (execution.getOrder().getTransactionType() != TransactionType.BUY) return;

        log.info("Override AÇIK: BUY için anında netleştirme uygulanıyor. Execution ID {}", execution.getId());
        blockAssetForBuyExecution(execution);
        settleBuyTransaction(execution);
    }

    @Override
    @Transactional
    public void releaseAllBlocksForOverride() {
        log.warn("[OVERRIDE] Takas kontrolleri KAPATILDI: Tüm blokajlar serbest bırakılıyor ve bekleyen takaslar netleştiriliyor.");

        List<OrderExecution> executionsToSettle = executionRepository.findByIsSettled(false);
        if (!executionsToSettle.isEmpty()) {
            log.info("[OVERRIDE] Netleştirilmeyi bekleyen {} adet işlem bulundu. Anında netleştirme başlıyor...", executionsToSettle.size());
            for (OrderExecution execution : executionsToSettle) {
                try {
                    if (execution.getOrder().getTransactionType() == TransactionType.BUY) {
                        settleBuyTransaction(execution);
                    } else {
                        settleSellTransaction(execution);
                    }
                    execution.setSettled(true);
                    executionRepository.save(execution);
                } catch (Exception e) {
                    log.error("[OVERRIDE] Execution ID {} anında netleştirilirken hata oluştu. Atlanıyor. Hata: {}", execution.getId(), e.getMessage());
                }
            }
        } else {
            log.info("[OVERRIDE] Netleştirilmeyi bekleyen işlem bulunamadı.");
        }

        List<CustomerAccount> accounts = accountRepository.findAll();
        for (CustomerAccount acc : accounts) {
            BigDecimal blocked = acc.getBlockedBalance();
            if (blocked != null && blocked.compareTo(BigDecimal.ZERO) > 0) {
                acc.setBlockedBalance(BigDecimal.ZERO);
                createTransaction(acc, "ORDER_UNBLOCKED", blocked, (short) 1, null, null, "Override ile bakiye blokaj serbest");
                accountRepository.save(acc);
                log.info("[OVERRIDE] Account {} için {} tutarında bakiye blokajı serbest bırakıldı.", acc.getId(), blocked);
            }
        }

        List<CustomerAsset> assets = customerAssetRepository.findAll();
        for (CustomerAsset ca : assets) {
            int blockedLots = ca.getBlockedLot();
            if (blockedLots > 0) {
                int beforeBlocked = ca.getBlockedLot();
                int beforeTotal = ca.getTotalLot();
                ca.setBlockedLot(0);
                customerAssetRepository.saveAndFlush(ca);
                log.info("[OVERRIDE] CustomerId={} AssetId={} blokaj serbest: Blocked {} -> 0 | Total {} -> {}", 
                        ca.getCustomerId(), ca.getAssetId(), beforeBlocked, beforeTotal, ca.getTotalLot());
            }
        }
    }

    private CustomerAccount findAccountById(Integer accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Müşteri hesabı bulunamadı: " + accountId));
    }

    private CustomerAsset findAssetByCustomerIdAndAssetId(Integer customerId, Integer assetId) {
        return customerAssetRepository.findByCustomerIdAndAssetId(customerId, assetId)
                .orElseThrow(() -> new EntityNotFoundException("Müşteri varlığı bulunamadı: Müşteri ID " + customerId + ", Varlık ID " + assetId));
    }

    private CustomerAsset findOrCreateAsset(Integer customerId, Integer assetId) {
        return customerAssetRepository.findByCustomerIdAndAssetId(customerId, assetId)
                .orElseGet(() -> {
                    CustomerAsset newAsset = new CustomerAsset();
                    newAsset.setCustomerId(customerId);
                    newAsset.setAssetId(assetId);
                    newAsset.setTotalLot(0);
                    newAsset.setBlockedLot(0);
                    newAsset.setAverageCost(BigDecimal.ZERO);
                    return newAsset;
                });
    }

    private void createTransaction(CustomerAccount account, String type, BigDecimal amount, short direction, Order order, OrderExecution execution, String description) {
        AccountTransaction transaction = AccountTransaction.builder()
                .account(account)
                .transactionType(type)
                .amount(amount)
                .direction(direction)
                .balanceAfterTransaction(account.getBalance())
                .relatedOrder(order)
                .relatedExecution(execution)
                .description(description)
                .build();
        transactionRepository.save(transaction);
    }

    private String getBistCodeForOrder(Order order) {
        try {
            Integer assetId = order.getAssetId();
            if (assetId == null) {
                return "UNKNOWN_ASSET";
            }
            Asset asset = assetRepository.findById(assetId)
                    .orElse(null);
            return asset != null ? asset.getBistCode() : String.valueOf(assetId);
        } catch (Exception e) {
            log.warn("BIST kodu alınırken hata oluştu. Order ID: {}", order != null ? order.getId() : null, e);
            return "UNKNOWN_ASSET";
        }
    }
}