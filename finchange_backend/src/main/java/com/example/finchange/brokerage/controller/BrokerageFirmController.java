package com.example.finchange.brokerage.controller;

import com.example.finchange.brokerage.dto.BrokerageFirmRequest;
import com.example.finchange.brokerage.dto.BrokerageFirmResponse;
import com.example.finchange.brokerage.mapper.BrokerageFirmMapper;
import com.example.finchange.brokerage.model.BrokerageFirm;
import com.example.finchange.brokerage.service.BrokerageFirmService;
import com.example.finchange.common.model.dto.response.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/brokerage-firms")
@RequiredArgsConstructor
public class BrokerageFirmController {

    private final BrokerageFirmService brokerageFirmService;
    private final BrokerageFirmMapper mapper;

    @GetMapping
    @PreAuthorize("hasAuthority('user:read:all')") // reuse admin-like permission
    public SuccessResponse<List<BrokerageFirmResponse>> findAll() {
        List<BrokerageFirmResponse> list = brokerageFirmService.findAll()
                .stream().map(mapper::toResponse).collect(Collectors.toList());
        return SuccessResponse.success(list);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:read:all')")
    public SuccessResponse<BrokerageFirmResponse> getById(@PathVariable Integer id) {
        BrokerageFirm entity = brokerageFirmService.getById(id);
        return SuccessResponse.success(mapper.toResponse(entity));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('user:create')")
    @ResponseStatus(HttpStatus.CREATED)
    public SuccessResponse<BrokerageFirmResponse> create(@RequestBody @Valid BrokerageFirmRequest request) {
        BrokerageFirm toSave = mapper.toEntity(request);
        BrokerageFirm saved = brokerageFirmService.create(toSave);
        return SuccessResponse.success(mapper.toResponse(saved), "Aracı kurum oluşturuldu.");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user:update:role')")
    public SuccessResponse<BrokerageFirmResponse> update(@PathVariable Integer id,
                                                         @RequestBody @Valid BrokerageFirmRequest request) {
        BrokerageFirm existing = brokerageFirmService.getById(id);
        mapper.updateEntity(existing, request);
        BrokerageFirm updated = brokerageFirmService.update(id, existing);
        return SuccessResponse.success(mapper.toResponse(updated), "Aracı kurum güncellendi.");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:update:status')")
    public SuccessResponse<Void> delete(@PathVariable Integer id) {
        brokerageFirmService.delete(id);
        return SuccessResponse.success("Aracı kurum silindi.");
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('user:update:status')")
    public SuccessResponse<BrokerageFirmResponse> activate(@PathVariable Integer id) {
        BrokerageFirm activated = brokerageFirmService.activate(id);
        return SuccessResponse.success(mapper.toResponse(activated), "Aracı kurum aktif hale getirildi.");
    }
}
