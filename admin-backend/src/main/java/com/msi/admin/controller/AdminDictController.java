package com.msi.admin.controller;

import com.msi.admin.domain.Brand;
import com.msi.admin.domain.PhoneModel;
import com.msi.admin.domain.PhoneSeries;
import com.msi.admin.domain.PhoneSpec;
import com.msi.admin.service.DictService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/api/admin", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminDictController {
  private final DictService dictService;

  public AdminDictController(DictService dictService) {
    this.dictService = dictService;
  }

  // --------------------------品牌-------------------------------

  /**
   * 添加品牌
   * 添加品牌的时候，如果该品牌之前添加过，现在只是逻辑删除，则修改逻辑删除字段即可，排序还是在所有品牌的最后面
   * @param brandName
   * @return
   */
  @PostMapping("/brands")
  public ResponseEntity<Map<String, Object>> addBrand(@RequestBody Map<String, String> request) {
    String name = request.get("name");
    Brand brand = dictService.addBrand(name);
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("brand", brand);
    return ResponseEntity.ok(response);
  }

  /**
   * 删除指定品牌，只是逻辑删除
   * @param brandId
   * @return
   */
  @DeleteMapping("/brands/{id}")
  public ResponseEntity<Map<String, Object>> deleteBrand(@PathVariable Long id) {
    dictService.deleteBrand(id);
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    return ResponseEntity.ok(response);
  }

  /**
   * 修改品牌的名称
   * @param brandId
   * @param brandName
   * @return
   */
  @PutMapping("/brands/{id}")
  public ResponseEntity<Map<String, Object>> updateBrand(@PathVariable Long id, @RequestBody Map<String, String> request) {
    String name = request.get("brandName");
    Brand brand = dictService.updateBrand(id, name);
    Map<String, Object> response = new HashMap<>();
    response.put("success", brand != null);
    response.put("brand", brand);
    return ResponseEntity.ok(response);
  }

  /**
   * 更新所有品牌的排序字段，根据传入的品牌列表排序
   * 只更新排序字段其它字段不更新
   * 优先判断数据库中有效的品牌与传入的品牌是否匹配，
   * 如果匹配则更新排序字段，否则不更新
   * @param brandList
   * @return
   */
  @PutMapping("/brands/sort")
  public ResponseEntity<Map<String, Object>> updateBrandSort(@RequestBody List<Brand> brandList) {
    boolean success = dictService.updateBrandSort(brandList);
    Map<String, Object> response = new HashMap<>();
    response.put("success", success);
    return ResponseEntity.ok(response);
  }

  /**
   * 查询所有的品牌列表
   */
  @GetMapping("/brands/all")
  public List<Brand> allBrands() {
    return dictService.listAllBrands();
  }

  // -----------------------------系列--------------------------------
  /**
   * 添加系列
   * 添加系列的时候，如果该系列之前添加过，现在只是逻辑删除，则修改逻辑删除字段即可，排序还是在所有系列的最后面
   * @param brandId
   * @param seriesName
   * @return
   */
  @PostMapping("/series")
  public ResponseEntity<Map<String, Object>> addSeries(@RequestParam Long brandId, @RequestBody Map<String, String> request) {
    String name = request.get("seriesName");
    PhoneSeries series = dictService.addSeries(brandId, name);
    Map<String, Object> response = new HashMap<>();
    response.put("success", series != null);
    response.put("series", series);
    return ResponseEntity.ok(response);
  }

  /**
   * 删除指定系列，只是逻辑删除
   * @param seriesId
   * @return
   */
  @DeleteMapping("/series/{id}")
  public ResponseEntity<Map<String, Object>> deleteSeries(@PathVariable Long id) {
    dictService.deleteSeries(id);
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    return ResponseEntity.ok(response);
  }

  /**
   * 修改系列的名称
   * @param seriesId
   * @param seriesName
   * @return
   */
  @PutMapping("/series/{id}")
  public ResponseEntity<Map<String, Object>> updateSeries(@PathVariable Long id, @RequestBody Map<String, String> request) {
    String name = request.get("seriesName");
    PhoneSeries series = dictService.updateSeries(id, name);
    Map<String, Object> response = new HashMap<>();
    response.put("success", series != null);
    if (series != null) {
      Map<String, Object> simpleSeries = new HashMap<>();
      simpleSeries.put("id", series.getId());
      simpleSeries.put("seriesName", series.getSeriesName());
      simpleSeries.put("sort", series.getSort());
      response.put("series", simpleSeries);
    } else {
      response.put("series", null);
    }
    return ResponseEntity.ok(response);
  }

  /**
   * 更新所有系列的排序字段，根据传入的品牌列表排序
   * 只更新排序字段其它字段不更新
   * 优先判断数据库中有效的系列与传入的系列是否匹配，
   * 如果匹配则更新排序字段，否则不更新
   * @param brandId
   * @param seriesList
   * @return
   */
  @PutMapping("/series/sort")
  public ResponseEntity<Map<String, Object>> updateSeriesSort(@RequestParam Long brandId, @RequestBody List<PhoneSeries> seriesList) {
    boolean success = dictService.updateSeriesSort(brandId, seriesList);
    Map<String, Object> response = new HashMap<>();
    response.put("success", success);
    return ResponseEntity.ok(response);
  }

  /**
   * 根据品牌Id查询所有的系列
   * 只返回系列的id，名称、排序
   * @param brandId
   * @return
   */
  @GetMapping("/series/by-brand/{brandId}")
  public ResponseEntity<Map<String, Object>> seriesByBrand(@PathVariable Long brandId) {
    List<PhoneSeries> seriesList = dictService.listSeriesByBrandId(brandId);
    List<Map<String, Object>> simpleSeriesList = seriesList.stream().map(series -> {
      Map<String, Object> map = new HashMap<>();
      map.put("seriesId", series.getId());
      map.put("seriesName", series.getSeriesName());
      map.put("sort", series.getSort());
      return map;
    }).collect(Collectors.toList());
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("seriesArr", simpleSeriesList);
    return ResponseEntity.ok(response);
  }

  // -----------------------------型号--------------------------------
  /**
   * 添加型号
   * 添加型号的时候，如果该型号之前添加过，现在只是逻辑删除，则修改逻辑删除字段即可，排序还是在所有型号的最后面
   * @param brandId
   * @param seriesId
   * @param modelName
   * @return
   */
  @PostMapping("/models")
  public ResponseEntity<Map<String, Object>> addModel(@RequestParam Long brandId, @RequestParam Long seriesId, @RequestBody Map<String, String> request) {
    String name = request.get("modelName");
    PhoneModel model = dictService.addModel(brandId, seriesId, name);
    Map<String, Object> response = new HashMap<>();
    response.put("success", model != null);
    response.put("model", model);
    return ResponseEntity.ok(response);
  }
  /**
   * 删除指定型号，只是逻辑删除
   * @param modelId
   * @return
   */
  @DeleteMapping("/models/{id}")
  public ResponseEntity<Map<String, Object>> deleteModel(@PathVariable Long id) {
    dictService.deleteModel(id);
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    return ResponseEntity.ok(response);
  }

  /**
   * 更新所有型号的排序字段，根据传入的型号列表排序
   * 只更新排序字段其它字段不更新
   * 优先判断数据库中有效的型号与传入的型号是否匹配，
   * 如果匹配则更新排序字段，否则不更新
   * @param seriesId
   * @param modelList
   * @return
   */
  @PutMapping("/models/sort")
  public ResponseEntity<Map<String, Object>> updateModelSort(@RequestParam Long seriesId, @RequestBody List<PhoneModel> modelList) {
    boolean success = dictService.updateModelSort(seriesId, modelList);
    Map<String, Object> response = new HashMap<>();
    response.put("success", success);
    return ResponseEntity.ok(response);
  }

  /**
   * 修改型号的名称
   * @param modelId
   * @param modelName
   * @return
   */
  @PutMapping("/models/{id}")
  public ResponseEntity<Map<String, Object>> updateModel(@PathVariable Long id, @RequestBody Map<String, String> request) {
    String name = request.get("modelName");
    PhoneModel model = dictService.updateModel(id, name);
    Map<String, Object> response = new HashMap<>();
    response.put("success", model != null);
    if (model != null) {
      Map<String, Object> simpleModel = new HashMap<>();
      simpleModel.put("id", model.getId());
      simpleModel.put("modelName", model.getModelName());
      simpleModel.put("sort", model.getSort());
      response.put("model", simpleModel);
    } else {
      response.put("model", null);
    }
    return ResponseEntity.ok(response);
  }

  /**
   * 根据系列Id查询所有的型号
   * 只返回型号的id，名称、排序
   * @param seriesId
   * @return
   */
  @GetMapping("/models/by-series/{seriesId}")
  public ResponseEntity<Map<String, Object>> modelsBySeries(@PathVariable Long seriesId) {
    List<PhoneModel> modelList = dictService.listModelsBySeriesId(seriesId);
    List<Map<String, Object>> simpleModelList = modelList.stream().map(model -> {
      Map<String, Object> map = new HashMap<>();
      map.put("modelId", model.getId());
      map.put("modelName", model.getModelName());
      map.put("sort", model.getSort());
      return map;
    }).collect(Collectors.toList());
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("modelArr", simpleModelList);
    return ResponseEntity.ok(response);
  }

  // -----------------------------规格--------------------------------
  /**
   * 添加规格
   * 添加规格的时候，如果该规格之前添加过，现在只是逻辑删除，则修改逻辑删除字段即可，排序还是在所有规格的最后面
   * @param modelId
   * @param specName
   * @return
   */
  @PostMapping("/specs")
  public ResponseEntity<Map<String, Object>> addSpec(@RequestParam Long modelId, @RequestBody Map<String, String> request) {
    String name = request.get("specName");
    PhoneSpec spec = dictService.addSpec(modelId, name);
    Map<String, Object> response = new HashMap<>();
    response.put("success", spec != null);
    response.put("spec", spec);
    return ResponseEntity.ok(response);
  }

  /**
   * 删除指定规格，只是逻辑删除
   * @param specId
   * @return
   */
  @DeleteMapping("/specs/{id}")
  public ResponseEntity<Map<String, Object>> deleteSpec(@PathVariable Long id) {
    dictService.deleteSpec(id);
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    return ResponseEntity.ok(response);
  }

  /**
   * 更新所有规格的排序字段，根据传入的规格列表排序
   * 只更新排序字段其它字段不更新
   * 优先判断数据库中有效的规格与传入的规格是否匹配，
   * 如果匹配则更新排序字段，否则不更新
   * @param modelId
   * @param specList
   * @return
   */
  @PutMapping("/specs/sort")
  public ResponseEntity<Map<String, Object>> updateSpecSort(@RequestParam Long modelId, @RequestBody List<PhoneSpec> specList) {
    boolean success = dictService.updateSpecSort(modelId, specList);
    Map<String, Object> response = new HashMap<>();
    response.put("success", success);
    return ResponseEntity.ok(response);
  }

  /**
   * 修改规格的名称
   * @param id
   * @param request
   * @return
   */
  @PutMapping("/specs/{id}")
  public ResponseEntity<Map<String, Object>> updateSpec(@PathVariable Long id, @RequestBody Map<String, String> request) {
    String name = request.get("specName");
    PhoneSpec spec = dictService.updateSpec(id, name);
    Map<String, Object> response = new HashMap<>();
    response.put("success", spec != null);
    if (spec != null) {
      Map<String, Object> simpleSpec = new HashMap<>();
      simpleSpec.put("id", spec.getId());
      simpleSpec.put("specName", spec.getSpecName());
      simpleSpec.put("sort", spec.getSort());
      response.put("spec", simpleSpec);
    } else {
      response.put("spec", null);
    }
    return ResponseEntity.ok(response);
  }

  /**
   * 根据型号Id查询所有的规格
   * 只返回规格的id，名称、排序
   * @param modelId
   * @return
   */
  @GetMapping("/specs/by-model/{modelId}")
  public ResponseEntity<Map<String, Object>> specsByModel(@PathVariable Long modelId) {
    List<PhoneSpec> specList = dictService.listSpecsByModelId(modelId);
    List<Map<String, Object>> simpleSpecList = specList.stream().map(spec -> {
      Map<String, Object> map = new HashMap<>();
      map.put("specId", spec.getId());
      map.put("specName", spec.getSpecName());
      map.put("sort", spec.getSort());
      return map;
    }).collect(Collectors.toList());
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("specArr", simpleSpecList);
    return ResponseEntity.ok(response);
  }



  @PostMapping("/dict/import/file")
  /**
   * 先清空字典数据库表中的所有数据，然后将JSON文件中的数据导入数据库
   * @param file
   * @return
   */
  public ResponseEntity<String> importDict(@RequestParam("file") MultipartFile file) {
    String result = dictService.importDict(file);
    if (result.startsWith("导入成功")) {
      return ResponseEntity.ok(result);
    } else {
      return ResponseEntity.badRequest().body(result);
    }
  }

  /**
   * 接收原始JSON字符串导入字典
   */
  @PostMapping("/dict/import/json")
  public ResponseEntity<String> importDictJson(@RequestBody String jsonContent) {
    String result = dictService.importDictJson(jsonContent);
    if (result.startsWith("导入成功")) {
      return ResponseEntity.ok(result);
    } else {
      return ResponseEntity.badRequest().body(result);
    }
  }
}
