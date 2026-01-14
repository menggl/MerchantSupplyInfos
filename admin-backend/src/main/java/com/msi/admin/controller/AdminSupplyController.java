package com.msi.admin.controller;

import com.msi.admin.service.SupplyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/products")
public class AdminSupplyController {

    private final SupplyService supplyService;

    public AdminSupplyController(SupplyService supplyService) {
        this.supplyService = supplyService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Integer productType,
            @RequestParam(required = false) String merchantName,
            @RequestParam(required = false) String cityCode
    ) {
        return ResponseEntity.ok(supplyService.listProducts(page, size, id, productType, merchantName, cityCode));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateProductStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        supplyService.updateProductStatus(id, body.get("status"));
        return ResponseEntity.ok().build();
    }
}
