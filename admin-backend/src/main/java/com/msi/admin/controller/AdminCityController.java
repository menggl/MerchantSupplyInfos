package com.msi.admin.controller;

import com.msi.admin.domain.CityDict;
import com.msi.admin.service.CityDictService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/admin/cities", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminCityController {
    private final CityDictService cityDictService;

    public AdminCityController(CityDictService cityDictService) {
        this.cityDictService = cityDictService;
    }

    @GetMapping
    public List<CityDict> getAllCities() {
        return cityDictService.findAllCities();
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addCity(@RequestBody Map<String, String> request) {
        String cityCode = request.get("cityCode");
        String cityName = request.get("cityName");
        CityDict city = cityDictService.addCity(cityCode, cityName);
        Map<String, Object> response = new HashMap<>();
        response.put("success", city != null);
        response.put("city", city);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCity(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        String cityCode = (String) request.get("cityCode");
        String cityName = (String) request.get("cityName");
        Integer valid = request.containsKey("valid") ? (Integer) request.get("valid") : null;
        
        CityDict city = cityDictService.updateCity(id, cityCode, cityName, valid);
        Map<String, Object> response = new HashMap<>();
        response.put("success", city != null);
        response.put("city", city);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCity(@PathVariable Long id) {
        boolean success = cityDictService.deleteCity(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/sort")
    public ResponseEntity<Map<String, Object>> updateCitySort(@RequestBody List<CityDict> cityList) {
        boolean success = cityDictService.updateCitySort(cityList);
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        return ResponseEntity.ok(response);
    }
}
