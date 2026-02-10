package com.msi.controller;

import com.msi.domain.Merchant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestPermissionController {

    @GetMapping("/check-permissions")
    public ResponseEntity<Map<String, Object>> checkPermissions(@RequestAttribute("merchant") Merchant merchant) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "User has all permissions.");
        response.put("merchantId", merchant.getId());
        return ResponseEntity.ok(response);
    }
}
