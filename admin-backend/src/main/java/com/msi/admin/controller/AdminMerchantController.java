package com.msi.admin.controller;

import com.msi.admin.service.MerchantService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/admin/merchants", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminMerchantController {
    private final MerchantService merchantService;

    public AdminMerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @GetMapping
    public Map<String, Object> listMerchants(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String merchantName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate,
            @RequestParam(required = false) Integer isMember,
            @RequestParam(required = false) String cityCode
    ) {
        return merchantService.listMerchants(page, size, merchantName, phone, startDate, endDate, isMember, cityCode);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getMerchantDetail(@PathVariable Long id) {
        Map<String, Object> detail = merchantService.getMerchantDetail(id);
        if (detail == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(detail);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteMerchant(@PathVariable Long id) {
        boolean success = merchantService.deleteMerchant(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateMerchantStatus(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        Integer isValid = request.get("isValid");
        boolean success = merchantService.updateMerchantStatus(id, isValid);
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        return ResponseEntity.ok(response);
    }
}

