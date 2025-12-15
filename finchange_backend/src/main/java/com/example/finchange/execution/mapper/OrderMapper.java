package com.example.finchange.execution.mapper;

import com.example.finchange.customer.model.CustomerType;
import com.example.finchange.execution.dto.OrderResponseDto;
import com.example.finchange.execution.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "orderCode", target = "orderCode")
    @Mapping(source = "batchId", target = "batchId")
    @Mapping(source = "customerAccount.customer", target = "customerName", qualifiedByName = "toCustomerName")
    @Mapping(source = "customerAccount.customer.customerCode", target = "customerCode")
    @Mapping(source = "asset.bistCode", target = "bistCode")
    @Mapping(source = "transactionType", target = "transactionType")
    @Mapping(source = "orderType", target = "orderType")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "initialLotAmount", target = "initialLotAmount")
    @Mapping(source = "filledLotAmount", target = "filledLotAmount")
    @Mapping(source = "limitPrice", target = "limitPrice")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    OrderResponseDto toOrderResponseDto(Order order);

    @Named("toCustomerName")
    default String toCustomerName(com.example.finchange.customer.model.Customers customer) {
        if (customer == null) {
            return null;
        }
        return customer.getCustomerType() == CustomerType.GERCEK
                ? customer.getName() + " " + customer.getLastName()
                : customer.getCompanyTitle();
    }
}