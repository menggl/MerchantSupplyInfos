package com.msi.admin.controller;

import com.msi.admin.domain.PhoneRemarkDict;
import com.msi.admin.service.PhoneRemarkDictService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/admin/phone-remarks", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminPhoneRemarkController {
  private final PhoneRemarkDictService phoneRemarkDictService;

  public AdminPhoneRemarkController(PhoneRemarkDictService phoneRemarkDictService) {
    this.phoneRemarkDictService = phoneRemarkDictService;
  }

  @GetMapping
  public List<PhoneRemarkDict> list(@RequestParam(required = false) Integer type) {
    return phoneRemarkDictService.findByType(type);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> request) {
    String remarkName = (String) request.get("remarkName");
    Integer valid = request.containsKey("valid") ? (Integer) request.get("valid") : null;
    PhoneRemarkDict item = null;
    if (remarkName != null) {
      item = phoneRemarkDictService.updateRemarkName(id, remarkName);
    }
    if (valid != null) {
      item = phoneRemarkDictService.updateValid(id, valid);
    }
    Map<String, Object> response = new HashMap<>();
    response.put("success", item != null);
    response.put("item", item);
    return ResponseEntity.ok(response);
  }

  @PutMapping("/sort")
  public ResponseEntity<Map<String, Object>> updateSort(@RequestBody List<PhoneRemarkDict> items) {
    boolean success = phoneRemarkDictService.updateSort(items);
    Map<String, Object> response = new HashMap<>();
    response.put("success", success);
    return ResponseEntity.ok(response);
  }

  @PostMapping
  public ResponseEntity<Map<String, Object>> add(@RequestParam Integer type, @RequestBody Map<String, String> request) {
    String remarkName = request.get("remarkName");
    PhoneRemarkDict item = phoneRemarkDictService.addRemark(type, remarkName);
    Map<String, Object> response = new HashMap<>();
    response.put("success", item != null);
    response.put("item", item);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
    boolean success = phoneRemarkDictService.deleteRemark(id);
    Map<String, Object> response = new HashMap<>();
    response.put("success", success);
    return ResponseEntity.ok(response);
  }
}
