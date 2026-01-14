package com.msi.controller;

import com.msi.domain.Product;
import com.msi.service.SupplyService;
import com.msi.service.ProductService;
import com.msi.service.MerchantService;
import com.msi.dto.CityDto;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class SupplyController {
  private final SupplyService supplyService;
  private final MerchantService merchantService;
  private final ProductService productService;

  public SupplyController(SupplyService supplyService, MerchantService merchantService, ProductService productService) {
    this.supplyService = supplyService;
    this.merchantService = merchantService;
    this.productService = productService;
  }

  @GetMapping("/cities")
  public List<CityDto> cities() {
    return supplyService.getCities();
  }

  @GetMapping("/supplies")
  public List<Product> supplies(
      @RequestParam(required = false) String cityCode,
      @RequestParam(required = false) String brand,
      @RequestParam(required = false) String model,
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    return supplyService.search(cityCode, brand, model, keyword, page, size);
  }

  /**
   * 下架二手商品
   * @param productId
   * @return
   */
  @PostMapping("/merchants/products/{productId}/withdraw")
  public ResponseEntity<Void> withdrawProduct(@PathVariable Long productId) {
      try {
          productService.withdrawProduct(productId);
          return ResponseEntity.ok().build();
      } catch (IllegalArgumentException e) {
          org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SupplyController.class);
          logger.error("下架商品失败: productId={}, {}", productId, e.getMessage(), e);
          return ResponseEntity.badRequest().build();
      }
  }

}
