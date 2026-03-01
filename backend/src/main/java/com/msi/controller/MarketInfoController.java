package com.msi.controller;

import com.msi.domain.MarketInfo;
import com.msi.service.MarketInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/market-infos")
public class MarketInfoController {

    @Autowired
    private MarketInfoService marketInfoService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        PageRequest pageRequest = PageRequest.of(page - 1, size, 
                Sort.by("sort").ascending().and(Sort.by("publishTime").descending()));
        
        Page<MarketInfo> result = marketInfoService.getMarketInfoList(pageRequest);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getContent());
        data.put("total", result.getTotalElements());
        data.put("totalPages", result.getTotalPages());
        data.put("pageNum", page);
        data.put("pageSize", size);
        
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/top3")
    public ResponseEntity<Map<String, Object>> getTop3() {
        List<MarketInfo> result = marketInfoService.getTop3MarketInfos();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        
        // Use a simplified map to return only necessary fields as per requirement:
        // "获取所有的行情资讯标题（前三条，根据排序字段sort获取sort最小的三条数据）"
        // Returning id and title is minimal and sufficient for navigation.
        List<Map<String, Object>> list = result.stream().map(info -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", info.getId());
            item.put("title", info.getTitle());
            return item;
        }).toList();

        response.put("data", list);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getDetail(@PathVariable Long id) {
        Optional<MarketInfo> marketInfo = marketInfoService.getMarketInfoDetail(id);
        
        Map<String, Object> response = new HashMap<>();
        if (marketInfo.isPresent()) {
            response.put("success", true);
            response.put("data", marketInfo.get());
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "资讯不存在或已下架");
            return ResponseEntity.status(404).body(response);
        }
    }
}
