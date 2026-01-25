package com.msi.admin.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.msi.admin.domain.Brand;
import com.msi.admin.domain.PhoneModel;
import com.msi.admin.domain.PhoneSeries;
import com.msi.admin.domain.PhoneSpec;
import com.msi.admin.repository.BrandRepository;
import com.msi.admin.repository.PhoneModelRepository;
import com.msi.admin.repository.PhoneSeriesRepository;
import com.msi.admin.repository.PhoneSpecRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DictService {
  private static final Logger logger = LoggerFactory.getLogger(DictService.class);

  private final BrandRepository brandRepository;
  private final PhoneSeriesRepository seriesRepository;
  private final PhoneModelRepository modelRepository;
  private final PhoneSpecRepository specRepository;

  public DictService(BrandRepository brandRepository, PhoneSeriesRepository seriesRepository, PhoneModelRepository modelRepository, PhoneSpecRepository specRepository) {
    this.brandRepository = brandRepository;
    this.seriesRepository = seriesRepository;
    this.modelRepository = modelRepository;
    this.specRepository = specRepository;
  }

  public boolean updateSpecSort(Long modelId, List<PhoneSpec> specList) {
    // Validate model
    Optional<PhoneModel> modelOpt = modelRepository.findById(modelId);
    if (!modelOpt.isPresent()) {
      return false;
    }
    PhoneModel model = modelOpt.get();

    // Get all valid specs for this model from DB
    List<PhoneSpec> existingSpecs = specRepository.findByModelId(model.getId()).stream()
        .filter(s -> s.getDeleted() == null || s.getDeleted() == 0)
        .collect(Collectors.toList());

    // Extract IDs from DB
    java.util.Set<Long> existingIds = existingSpecs.stream()
        .map(PhoneSpec::getId)
        .collect(Collectors.toSet());

    // Extract IDs from input
    java.util.Set<Long> inputIds = specList.stream()
        .map(PhoneSpec::getId)
        .collect(Collectors.toSet());

    // Check if they match exactly
    if (!existingIds.equals(inputIds)) {
      return false;
    }

    // Update sort order
    java.util.Map<Long, PhoneSpec> specMap = existingSpecs.stream()
        .collect(Collectors.toMap(PhoneSpec::getId, s -> s));

    for (int i = 0; i < specList.size(); i++) {
      Long id = specList.get(i).getId();
      if (specMap.containsKey(id)) {
        PhoneSpec spec = specMap.get(id);
        spec.setSort(i + 1);
        specRepository.save(spec);
      }
    }
    return true;
  }

  @Transactional
  public String importDict(MultipartFile file) {
    if (file.isEmpty()) {
      return "上传文件不能为空";
    }
    try {
      byte[] bytes = file.getBytes();
      String jsonContent;
      try {
        jsonContent = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
      } catch (Exception e) {
        jsonContent = new String(bytes, java.nio.charset.StandardCharsets.ISO_8859_1);
      }

      JSONArray brandsArray = parseBrands(jsonContent);
      clearAllDict();
      return processImport(brandsArray);
    } catch (IOException e) {
      e.printStackTrace();
      return "文件读取失败: " + e.getMessage();
    } catch (Exception e) {
      e.printStackTrace();
      return "导入失败: " + e.getMessage();
    }
  }

  @Transactional
  public String importDictJson(String json) {
    if (json == null || json.isEmpty()) {
      return "JSON内容不能为空";
    }
    try {
      JSONArray brandsArray = parseBrands(json);
      clearAllDict();
      return processImport(brandsArray);
    } catch (Exception e) {
      e.printStackTrace();
      return "导入失败: " + e.getMessage();
    }
  }

  private JSONArray parseBrands(String jsonContent) {
    Object parsed = JSON.parse(jsonContent);
    if (parsed instanceof JSONObject) {
      JSONObject root = (JSONObject) parsed;
      if (root.containsKey("brands")) {
        return root.getJSONArray("brands");
      }
      // If it's an object but doesn't have "brands", maybe try to cast it as a list?
      // But based on user description, it's either array or object with brands.
      // If the root object itself is not brands array, we might return empty or throw error.
      // Let's assume if it's not containing brands, maybe the object itself is what we want (unlikely given previous code).
      // But let's check if it can be cast to array if it was parsed as object (unlikely in fastjson unless structure is weird).
      return new JSONArray(); 
    } else if (parsed instanceof JSONArray) {
      return (JSONArray) parsed;
    }
    return new JSONArray();
  }

  @Transactional
  public void clearAllDict() {
    specRepository.deleteAllInBatch();
    modelRepository.deleteAllInBatch();
    seriesRepository.deleteAllInBatch();
    brandRepository.deleteAllInBatch();
  }

  private String processImport(JSONArray brands) {
    Long currentBrandId = brandRepository.findMaxId();
    currentBrandId = (currentBrandId == null) ? 0L : currentBrandId;
    
    Long currentSeriesId = seriesRepository.findMaxId();
    currentSeriesId = (currentSeriesId == null) ? 0L : currentSeriesId;
    
    Long currentModelId = modelRepository.findMaxId();
    currentModelId = (currentModelId == null) ? 0L : currentModelId;
    
    Long currentSpecId = specRepository.findMaxId();
    currentSpecId = (currentSpecId == null) ? 0L : currentSpecId;

    int brandCount = 0;
    int seriesCount = 0;
    int modelCount = 0;
    int specCount = 0;

    if (brands == null) {
      return "导入数据为空";
    }

    for (int i = 0; i < brands.size(); i++) {
      JSONObject bObj = brands.getJSONObject(i);
      String brandName = bObj.getString("title");
      if (brandName == null || brandName.isEmpty()) continue;
      
      logger.info("正在导入品牌: {}", brandName);
      Brand brand = brandRepository.findByName(brandName).orElse(null);
      if (brand == null) {
        currentBrandId++;
        brand = new Brand();
        brand.setId(currentBrandId);
        brand.setName(brandName);
        brand.setSort(i + 1);
        brand.setDeleted(0);
        brand = brandRepository.save(brand);
      } else {
        brand.setSort(i + 1);
        brand.setDeleted(0);
        brand = brandRepository.save(brand);
      }
      brandCount++;

      JSONArray seriesArr = bObj.getJSONArray("series");
      if (seriesArr != null) {
        for (int j = 0; j < seriesArr.size(); j++) {
          JSONObject sObj = seriesArr.getJSONObject(j);
          String seriesName = sObj.getString("title");
          if (seriesName == null || seriesName.isEmpty()) continue;
          
          logger.info("正在导入系列: {}", seriesName);
          PhoneSeries series = seriesRepository.findByBrandIdAndSeriesName(brand.getId(), seriesName).orElse(null);
          if (series == null) {
            currentSeriesId++;
            series = new PhoneSeries();
            series.setId(currentSeriesId);
            series.setBrandId(brand.getId());
            series.setSeriesName(seriesName);
            series.setSort(j + 1);
            series.setDeleted(0);
            series = seriesRepository.save(series);
          } else {
            series.setSort(j + 1);
            series.setDeleted(0);
            series = seriesRepository.save(series);
          }
          seriesCount++;

          JSONArray modelArr = sObj.getJSONArray("models");
          if (modelArr != null) {
            for (int k = 0; k < modelArr.size(); k++) {
              JSONObject mObj = modelArr.getJSONObject(k);
              String modelName = mObj.getString("title");
              if (modelName == null || modelName.isEmpty()) continue;
              
              logger.info("正在导入型号: {}", modelName);
              PhoneModel model = modelRepository.findBySeriesIdAndModelName(series.getId(), modelName).orElse(null);
              if (model == null) {
                currentModelId++;
                model = new PhoneModel();
                model.setId(currentModelId);
                model.setBrandId(brand.getId());
                model.setSeriesId(series.getId());
                model.setModelName(modelName);
                model.setSort(k + 1);
                model.setDeleted(0);
                model = modelRepository.save(model);
              } else {
                model.setSort(k + 1);
                model.setDeleted(0);
                model = modelRepository.save(model);
              }
              modelCount++;

              JSONArray specArr = mObj.getJSONArray("variants");
              if (specArr != null) {
                for (int l = 0; l < specArr.size(); l++) {
                  JSONObject spObj = specArr.getJSONObject(l);
                  String specName = spObj.getString("title");
                  if (specName == null || specName.isEmpty()) continue;
                  
                  logger.info("正在导入配置: {}", specName);
                  PhoneSpec spec = specRepository.findByModelIdAndSpecName(model.getId(), specName).orElse(null);
                  if (spec == null) {
                    currentSpecId++;
                    spec = new PhoneSpec();
                    spec.setId(currentSpecId);
                    spec.setBrandId(brand.getId());
                    spec.setSeriesId(series.getId());
                    spec.setModelId(model.getId());
                    spec.setSpecName(specName);
                    spec.setSort(l + 1);
                    spec.setDeleted(0);
                    spec = specRepository.save(spec);
                  } else {
                    spec.setSort(l + 1);
                    spec.setDeleted(0);
                    spec = specRepository.save(spec);
                  }
                  specCount++;
                }
              }
            }
          }
        }
      }
    }
    return String.format("导入成功: 品牌%d个, 系列%d个, 型号%d个, 配置%d个", brandCount, seriesCount, modelCount, specCount);
  }
  public List<Brand> listAllBrands() {
    return brandRepository.findAllValidOrderBySort();
  }

  public List<PhoneSeries> listSeriesByBrandId(Long brandId) {
    if (brandId == null) return List.of();
    return seriesRepository.findByBrandId(brandId).stream()
        .filter(s -> s.getDeleted() == null || s.getDeleted() == 0)
        .sorted((a, b2) -> Integer.compare(a.getSort() == null ? 0 : a.getSort(), b2.getSort() == null ? 0 : b2.getSort()))
        .collect(Collectors.toList());
  }

  public List<PhoneModel> listModelsBySeriesId(Long seriesId) {
    if (seriesId == null) return List.of();
    return modelRepository.findBySeriesId(seriesId).stream()
        .filter(m -> m.getDeleted() == null || m.getDeleted() == 0)
        .sorted((a,bm) -> Integer.compare(a.getSort() == null ? 0 : a.getSort(), bm.getSort() == null ? 0 : bm.getSort()))
        .collect(Collectors.toList());
  }

  public Brand addBrand(String name) {
    Optional<Brand> existing = brandRepository.findByName(name);
    if (existing.isPresent()) {
      Brand brand = existing.get();
      if (brand.getDeleted() != null && brand.getDeleted() == 1) {
        brand.setDeleted(0);
        Integer maxSort = brandRepository.findMaxSort();
        brand.setSort(maxSort == null ? 0 : maxSort + 1);
        return brandRepository.save(brand);
      }
      return brand;
    }
    Brand brand = new Brand();
    Long maxId = brandRepository.findMaxId();
    brand.setId(maxId == null ? 1L : maxId + 1);
    brand.setName(name);
    brand.setDeleted(0);
    Integer maxSort = brandRepository.findMaxSort();
    brand.setSort(maxSort == null ? 0 : maxSort + 1);
    return brandRepository.save(brand);
  }

  public Brand updateBrand(Long id, String name) {
    Optional<Brand> optionalBrand = brandRepository.findById(id);
    if (optionalBrand.isPresent()) {
      Brand brand = optionalBrand.get();
      brand.setName(name);
      return brandRepository.save(brand);
    }
    return null;
  }

  public void deleteBrand(Long id) {
    Optional<Brand> optionalBrand = brandRepository.findById(id);
    if (optionalBrand.isPresent()) {
      Brand brand = optionalBrand.get();
      brand.setDeleted(1);
      brandRepository.save(brand);
    }
  }

  public boolean updateBrandSort(List<Brand> brandList) {
    // Get all valid brands from DB
    List<Brand> existingBrands = brandRepository.findAll().stream()
        .filter(b -> b.getDeleted() == null || b.getDeleted() == 0)
        .collect(Collectors.toList());

    // Extract IDs from DB
    java.util.Set<Long> existingIds = existingBrands.stream()
        .map(Brand::getId)
        .collect(Collectors.toSet());

    // Extract IDs from input
    java.util.Set<Long> inputIds = brandList.stream()
        .map(Brand::getId)
        .collect(Collectors.toSet());

    // Check if they match exactly
    if (!existingIds.equals(inputIds)) {
      return false;
    }

    // Update sort order
    java.util.Map<Long, Brand> brandMap = existingBrands.stream()
        .collect(Collectors.toMap(Brand::getId, b -> b));

    for (int i = 0; i < brandList.size(); i++) {
      Long id = brandList.get(i).getId();
      if (brandMap.containsKey(id)) {
        Brand brand = brandMap.get(id);
        brand.setSort(i + 1);
        brandRepository.save(brand);
      }
    }
    return true;
  }

  public boolean updateSeriesSort(Long brandId, List<PhoneSeries> seriesList) {
    // Validate brand
    Optional<Brand> brandOpt = brandRepository.findById(brandId);
    if (!brandOpt.isPresent()) {
      return false;
    }
    Brand brand = brandOpt.get();

    // Get all valid series for this brand from DB
    List<PhoneSeries> existingSeries = seriesRepository.findByBrandId(brand.getId()).stream()
        .filter(s -> s.getDeleted() == null || s.getDeleted() == 0)
        .collect(Collectors.toList());

    // Extract IDs from DB
    java.util.Set<Long> existingIds = existingSeries.stream()
        .map(PhoneSeries::getId)
        .collect(Collectors.toSet());

    // Extract IDs from input
    java.util.Set<Long> inputIds = seriesList.stream()
        .map(PhoneSeries::getId)
        .collect(Collectors.toSet());

    // Check if they match exactly
    if (!existingIds.equals(inputIds)) {
      return false;
    }

    // Update sort order
    java.util.Map<Long, PhoneSeries> seriesMap = existingSeries.stream()
        .collect(Collectors.toMap(PhoneSeries::getId, s -> s));

    for (int i = 0; i < seriesList.size(); i++) {
      Long id = seriesList.get(i).getId();
      if (seriesMap.containsKey(id)) {
        PhoneSeries series = seriesMap.get(id);
        series.setSort(i + 1);
        seriesRepository.save(series);
      }
    }
    return true;
  }

  public boolean updateModelSort(Long seriesId, List<PhoneModel> modelList) {
    // Validate series
    Optional<PhoneSeries> seriesOpt = seriesRepository.findById(seriesId);
    if (!seriesOpt.isPresent()) {
      return false;
    }
    PhoneSeries series = seriesOpt.get();

    // Get all valid models for this series from DB
    List<PhoneModel> existingModels = modelRepository.findBySeriesId(series.getId()).stream()
        .filter(m -> m.getDeleted() == null || m.getDeleted() == 0)
        .collect(Collectors.toList());

    // Extract IDs from DB
    java.util.Set<Long> existingIds = existingModels.stream()
        .map(PhoneModel::getId)
        .collect(Collectors.toSet());

    // Extract IDs from input
    java.util.Set<Long> inputIds = modelList.stream()
        .map(PhoneModel::getId)
        .collect(Collectors.toSet());

    // Check if they match exactly
    if (!existingIds.equals(inputIds)) {
      return false;
    }

    // Update sort order
    java.util.Map<Long, PhoneModel> modelMap = existingModels.stream()
        .collect(Collectors.toMap(PhoneModel::getId, m -> m));

    for (int i = 0; i < modelList.size(); i++) {
      Long id = modelList.get(i).getId();
      if (modelMap.containsKey(id)) {
        PhoneModel model = modelMap.get(id);
        model.setSort(i + 1);
        modelRepository.save(model);
      }
    }
    return true;
  }

  public PhoneSeries addSeries(Long brandId, String name) {
    Optional<Brand> brand = brandRepository.findById(brandId);
    if (brand.isPresent()) {
      PhoneSeries series = seriesRepository.findByBrandIdAndSeriesName(brandId, name).orElse(null);
      if (series != null) {
        if (series.getDeleted() != null && series.getDeleted() == 1) {
          series.setDeleted(0);
          Integer maxSort = seriesRepository.findMaxSortByBrandId(brandId);
          series.setSort(maxSort == null ? 0 : maxSort + 1);
          return seriesRepository.save(series);
        }
        return series;
      }
      series = new PhoneSeries();
      Long maxId = seriesRepository.findMaxId();
      series.setId(maxId == null ? 1L : maxId + 1);
      series.setBrandId(brandId);
      series.setSeriesName(name);
      series.setDeleted(0);
      Integer maxSort = seriesRepository.findMaxSortByBrandId(brandId);
      series.setSort(maxSort == null ? 0 : maxSort + 1);
      return seriesRepository.save(series);
    }
    return null;
  }

  public PhoneSeries updateSeries(Long id, String name) {
    Optional<PhoneSeries> optionalSeries = seriesRepository.findById(id);
    if (optionalSeries.isPresent()) {
      PhoneSeries series = optionalSeries.get();
      series.setSeriesName(name);
      return seriesRepository.save(series);
    }
    return null;
  }

  public void deleteSeries(Long id) {
    Optional<PhoneSeries> optionalSeries = seriesRepository.findById(id);
    if (optionalSeries.isPresent()) {
      PhoneSeries series = optionalSeries.get();
      series.setDeleted(1);
      seriesRepository.save(series);
    }
  }

  public PhoneModel addModel(Long seriesId, String name) {
    Optional<PhoneSeries> series = seriesRepository.findById(seriesId);
    if (series.isPresent()) {
      PhoneModel model = modelRepository.findBySeriesIdAndModelName(seriesId, name).orElse(null);
      if (model != null) {
        if (model.getDeleted() != null && model.getDeleted() == 1) {
          model.setDeleted(0);
          Integer maxSort = modelRepository.findMaxSortBySeriesId(seriesId);
          model.setSort(maxSort == null ? 0 : maxSort + 1);
          return modelRepository.save(model);
        }
        return model;
      }
      model = new PhoneModel();
      Long maxId = modelRepository.findMaxId();
      model.setId(maxId == null ? 1L : maxId + 1);
      model.setBrandId(series.get().getBrandId());
      model.setSeriesId(seriesId);
      model.setModelName(name);
      model.setDeleted(0);
      Integer maxSort = modelRepository.findMaxSortBySeriesId(seriesId);
      model.setSort(maxSort == null ? 0 : maxSort + 1);
      return modelRepository.save(model);
    }
    return null;
  }

  public PhoneModel updateModel(Long id, String name) {
    Optional<PhoneModel> optionalModel = modelRepository.findById(id);
    if (optionalModel.isPresent()) {
      PhoneModel model = optionalModel.get();
      model.setModelName(name);
      return modelRepository.save(model);
    }
    return null;
  }

  public void deleteModel(Long id) {
    Optional<PhoneModel> optionalModel = modelRepository.findById(id);
    if (optionalModel.isPresent()) {
      PhoneModel model = optionalModel.get();
      model.setDeleted(1);
      modelRepository.save(model);
    }
  }

  public PhoneSpec addSpec(Long modelId, String name) {
    Optional<PhoneModel> model = modelRepository.findById(modelId);
    if (model.isPresent()) {
      PhoneSpec spec = specRepository.findByModelIdAndSpecName(modelId, name).orElse(null);
      if (spec != null) {
        if (spec.getDeleted() != null && spec.getDeleted() == 1) {
          spec.setDeleted(0);
          Integer maxSort = specRepository.findMaxSortByModelId(modelId);
          spec.setSort(maxSort == null ? 0 : maxSort + 1);
          return specRepository.save(spec);
        }
        return spec;
      }
      spec = new PhoneSpec();
      Long maxId = specRepository.findMaxId();
      spec.setId(maxId == null ? 1L : maxId + 1);
      spec.setBrandId(model.get().getBrandId());
      spec.setSeriesId(model.get().getSeriesId());
      spec.setModelId(modelId);
      spec.setSpecName(name);
      spec.setDeleted(0);
      Integer maxSort = specRepository.findMaxSortByModelId(modelId);
      spec.setSort(maxSort == null ? 0 : maxSort + 1);
      return specRepository.save(spec);
    }
    return null;
  }

  public PhoneSpec updateSpec(Long id, String name) {
    Optional<PhoneSpec> optionalSpec = specRepository.findById(id);
    if (optionalSpec.isPresent()) {
      PhoneSpec spec = optionalSpec.get();
      spec.setSpecName(name);
      return specRepository.save(spec);
    }
    return null;
  }

  public void deleteSpec(Long id) {
    Optional<PhoneSpec> optionalSpec = specRepository.findById(id);
    if (optionalSpec.isPresent()) {
      PhoneSpec spec = optionalSpec.get();
      spec.setDeleted(1);
      specRepository.save(spec);
    }
  }

  public List<PhoneSpec> listSpecsByModelId(Long modelId) {
    if (modelId == null) return List.of();
    return specRepository.findByModelId(modelId).stream()
        .filter(s -> s.getDeleted() == null || s.getDeleted() == 0)
        .sorted((a, b2) -> Integer.compare(a.getSort() == null ? 0 : a.getSort(), b2.getSort() == null ? 0 : b2.getSort()))
        .collect(Collectors.toList());
  }
}
