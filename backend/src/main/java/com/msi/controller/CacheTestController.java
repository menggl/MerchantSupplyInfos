package com.msi.controller;

import com.msi.config.RedisCaptchaCacheService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CacheTestController {

    @GetMapping("/api/test-cache")
    public String testCache(@RequestParam String key, @RequestParam String value) {
        RedisCaptchaCacheService service = new RedisCaptchaCacheService();
        service.set(key, value, 300);
        String retrieved = service.get(key);
        boolean exists = service.exists(key);
        service.delete(key);
        boolean existsAfterDelete = service.exists(key);
        
        return String.format("Set: OK, Get: %s, Exists: %s, ExistsAfterDelete: %s", 
            retrieved, exists, existsAfterDelete);
    }
}
