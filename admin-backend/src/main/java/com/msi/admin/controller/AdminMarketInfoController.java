package com.msi.admin.controller;

import com.msi.admin.domain.MarketInfo;
import com.msi.admin.service.MarketInfoService;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/admin/market-infos", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminMarketInfoController {
    private final MarketInfoService marketInfoService;

    public AdminMarketInfoController(MarketInfoService marketInfoService) {
        this.marketInfoService = marketInfoService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String title) {
        Page<MarketInfo> pageResult = marketInfoService.listMarketInfos(page, size, title);
        Map<String, Object> response = new HashMap<>();
        response.put("list", pageResult.getContent());
        response.put("total", pageResult.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MarketInfo> get(@PathVariable Long id) {
        MarketInfo marketInfo = marketInfoService.getMarketInfo(id);
        if (marketInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(marketInfo);
    }

    @PostMapping
    public ResponseEntity<MarketInfo> create(@RequestBody MarketInfo marketInfo) {
        return ResponseEntity.ok(marketInfoService.createMarketInfo(marketInfo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MarketInfo> update(@PathVariable Long id, @RequestBody MarketInfo marketInfo) {
        MarketInfo updated = marketInfoService.updateMarketInfo(id, marketInfo);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        marketInfoService.deleteMarketInfo(id);
        return ResponseEntity.ok().build();
    }
}
