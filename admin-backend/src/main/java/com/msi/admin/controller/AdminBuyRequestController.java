package com.msi.admin.controller;

import com.msi.admin.service.BuyRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/buy-requests")
public class AdminBuyRequestController {

    private final BuyRequestService buyRequestService;

    public AdminBuyRequestController(BuyRequestService buyRequestService) {
        this.buyRequestService = buyRequestService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listBuyRequests(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String merchantName,
            @RequestParam(required = false) String cityCode
    ) {
        return ResponseEntity.ok(buyRequestService.listBuyRequests(page, size, id, merchantName, cityCode));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        buyRequestService.updateBuyRequestStatus(id, body.get("status"));
        return ResponseEntity.ok().build();
    }
}
