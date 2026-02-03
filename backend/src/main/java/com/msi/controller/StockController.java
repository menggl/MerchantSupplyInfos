package com.msi.controller;

import com.msi.domain.Merchant;
import com.msi.domain.MerchantNewPhoneStock;
import com.msi.dto.AddNewPhoneStockRequest;
import com.msi.dto.NewPhoneStockSummaryDto;
import com.msi.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    @Autowired
    private StockService stockService;

    /**
     * 新增库存产品（新机）
     * 对应表：merchant_new_phone_stock, merchant_payment_record, merchant_stock_price_count
     */
    @PostMapping("/new-add")
    public ResponseEntity<?> addNewPhoneStock(
            @RequestAttribute("merchant") Merchant merchant,
            @RequestBody AddNewPhoneStockRequest request) {
        
        if (merchant == null) {
            return ResponseEntity.status(401).body("Merchant not logged in");
        }
        
        try {
            MerchantNewPhoneStock stock = stockService.addNewPhoneStock(merchant.getId(), request);
            return ResponseEntity.ok(stock);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error adding stock: " + e.getMessage());
        }
    }


    /**
     * 删除一条库存产品（新机）
     * 对应表：merchant_new_phone_stock，软删除，修改valid字段值为0，删除之后不可以找回
     */
    @PostMapping("/new-delete/{id}")
    public ResponseEntity<?> deleteNewPhoneStock(
            @RequestAttribute("merchant") Merchant merchant,
            @PathVariable Long id) {
        
        if (merchant == null) {
            return ResponseEntity.status(401).body("Merchant not logged in");
        }

        try {
            stockService.deleteNewPhoneStock(merchant.getId(), id);
            return ResponseEntity.ok("Deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error deleting stock: " + e.getMessage());
        }
    }


    /**
     * 对库存产品进行上架、下架状态修改，接口名应该突出是对上架、下架的状态修改（仅针对新机））
     */
    @PostMapping("/new-status/{id}")
    public ResponseEntity<?> updateNewPhoneStockStatus(
            @RequestAttribute("merchant") Merchant merchant,
            @PathVariable Long id,
            @RequestParam Integer status) {
        
        if (merchant == null) {
            return ResponseEntity.status(401).body("Merchant not logged in");
        }

        try {
            stockService.updateNewPhoneStockStatus(merchant.getId(), id, status);
            return ResponseEntity.ok("Status updated successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error updating stock status: " + e.getMessage());
        }
    }

    /**
     * 修改库存产品的数据（新机）
     * 跟新增库存产品的接口类似，但是是对已存在的库存产品进行修改，而不是新增
     * 商品的品牌、系列、型号、配置不管是ID还是名称，都不允许修改
     * 类型也不许变更（该库存产品到底是新机、二手机的类型）
     * 备注、其它备注、售卖价、上架状态（修改后都统一更改为下架状态）、实付款和实收款都可以修改
     * 入库价（多条记录）先删除，再新增
     * 出库价（多条记录）先删除，再新增
     */
    @PostMapping("/new-update/{id}")
    public ResponseEntity<?> updateNewPhoneStock(
            @RequestAttribute("merchant") Merchant merchant,
            @PathVariable Long id,
            @RequestBody AddNewPhoneStockRequest request) {
        
        if (merchant == null) {
            return ResponseEntity.status(401).body("Merchant not logged in");
        }
        
        try {
            MerchantNewPhoneStock stock = stockService.updateNewPhoneStock(merchant.getId(), id, request);
            return ResponseEntity.ok(stock);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error updating stock: " + e.getMessage());
        }
    }
    /**
     * 查询最外层的新机列表
     * 最外层的列表中的每个卡片中展示字段有品牌、系列、型号、配置名称（如果是公有的，返回字段中需要有对应的ID）
     * 新机的数量、未上架的数量、未售出的数量，也返回新机类型字段
     * 按照品牌、系列、型号、配置的名称或者自定义名称（没有ID）增序排序（先找出用户所有的新机对应的品牌、系列、型号、名称列表，然后再去查询其对应的机器数量、未上架数量、未出售数量），注意结果一定要保存到缓存，方便用户下次快速查找
     */
    @GetMapping("/new-summary-list")
    public ResponseEntity<?> listNewPhoneStockSummary(
            @RequestAttribute("merchant") Merchant merchant,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        if (merchant == null) {
            return ResponseEntity.status(401).body("Merchant not logged in");
        }

        try {
            Pageable pageable = PageRequest.of(page - 1, size);
            Page<NewPhoneStockSummaryDto> result = stockService.searchNewPhoneStockSummaries(merchant.getId(), pageable);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error querying stock summary: " + e.getMessage());
        }
    }

    /**
     * 查询库存产品（新机）列表
     * 传入品牌、系列、型号、配置的ID或者自定义名称，传入类型必须是新机类型
     * 分页，先展示未上架、再展示已上架，最后展示库存为0，同等排序下按照创建时间倒序排列
     * valid值必须为1，类型必须是新机，必须是指定品牌、系列、型号、配置（需要自己判断是不是商家自己的，是公有的还是商家自定义的）下面的产品
     */
    @GetMapping("/new-list")
    public ResponseEntity<?> listNewPhoneStock(
            @RequestAttribute("merchant") Merchant merchant,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String brandName,
            @RequestParam(required = false) Long seriesId,
            @RequestParam(required = false) String seriesName,
            @RequestParam(required = false) Long modelId,
            @RequestParam(required = false) String modelName,
            @RequestParam(required = false) Long specId,
            @RequestParam(required = false) String specName) {
        
        if (merchant == null) {
            return ResponseEntity.status(401).body("Merchant not logged in");
        }

        try {
            Pageable pageable = PageRequest.of(page - 1, size); // 1-based to 0-based
            Page<MerchantNewPhoneStock> result = stockService.searchNewPhoneStock(
                    merchant.getId(), 
                    brandId, brandName, 
                    seriesId, seriesName, 
                    modelId, modelName, 
                    specId, specName, 
                    pageable);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error querying stock: " + e.getMessage());
        }
    }

}
