package com.msi.admin.service;

import com.msi.admin.domain.Brand;
import com.msi.admin.domain.PhoneModel;
import com.msi.admin.domain.PhoneSeries;
import com.msi.admin.domain.PhoneSpec;
import com.msi.admin.dto.BrandDto;
import com.msi.admin.dto.ModelDto;
import com.msi.admin.dto.SeriesDto;
import com.msi.admin.dto.SpecDto;
import com.msi.admin.repository.BrandRepository;
import com.msi.admin.repository.PhoneModelRepository;
import com.msi.admin.repository.PhoneSeriesRepository;
import com.msi.admin.repository.PhoneSpecRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
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

  @Transactional
  public String importDict(MultipartFile file) {
    if (file.isEmpty()) {
      return "上传文件不能为空";
    }
    try {
      ObjectMapper mapper = new ObjectMapper();
      byte[] bytes = file.getBytes();
      List<BrandDto> brands = null;
      try {
        brands = mapper.readValue(bytes, new TypeReference<List<BrandDto>>() {});
      } catch (Exception e) {
        String contentFallback = new String(bytes, java.nio.charset.StandardCharsets.ISO_8859_1);
        byte[] utf8Bytes = contentFallback.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        brands = mapper.readValue(utf8Bytes, new TypeReference<List<BrandDto>>() {});
      }
      clearAllDict();
      return processImport(brands);
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
      ObjectMapper mapper = new ObjectMapper();
      List<BrandDto> brands = mapper.readValue(json, new TypeReference<List<BrandDto>>() {});
      clearAllDict();
      return processImport(brands);
    } catch (Exception e) {
      e.printStackTrace();
      return "导入失败: " + e.getMessage();
    }
  }

  @Transactional
  public void clearAllDict() {
    specRepository.deleteAllInBatch();
    modelRepository.deleteAllInBatch();
    seriesRepository.deleteAllInBatch();
    brandRepository.deleteAllInBatch();
  }

  private String processImport(List<BrandDto> brands) {
    int brandCount = 0;
    int seriesCount = 0;
    int modelCount = 0;
    int specCount = 0;

    for (int i = 0; i < brands.size(); i++) {
      BrandDto bDto = brands.get(i);
      if (bDto.getBrand_name() == null || bDto.getBrand_name().isEmpty()) continue;
      logger.info("正在导入品牌: {}", bDto.getBrand_name());
      Brand brand = brandRepository.findByName(bDto.getBrand_name()).orElse(null);
      if (brand == null) {
        brand = new Brand();
        brand.setName(bDto.getBrand_name());
        brand.setSort(i + 1);
        brand.setDeleted(0);
        brand = brandRepository.save(brand);
      } else {
        brand.setSort(i + 1);
        brand.setDeleted(0);
        brand = brandRepository.save(brand);
      }
      brandCount++;

      if (bDto.getSeriesArr() != null) {
        for (int j = 0; j < bDto.getSeriesArr().size(); j++) {
          SeriesDto sDto = bDto.getSeriesArr().get(j);
          if (sDto.getSeries_name() == null || sDto.getSeries_name().isEmpty()) continue;
          logger.info("正在导入系列: {}", sDto.getSeries_name());
          PhoneSeries series = seriesRepository.findByBrandAndSeriesName(brand, sDto.getSeries_name()).orElse(null);
          if (series == null) {
            series = new PhoneSeries();
            series.setBrand(brand);
            series.setSeriesName(sDto.getSeries_name());
            series.setSort(j + 1);
            series.setDeleted(0);
            series = seriesRepository.save(series);
          } else {
            series.setSort(j + 1);
            series.setDeleted(0);
            series = seriesRepository.save(series);
          }
          seriesCount++;

          if (sDto.getModelArr() != null) {
            for (int k = 0; k < sDto.getModelArr().size(); k++) {
              ModelDto mDto = sDto.getModelArr().get(k);
              if (mDto.getModel_name() == null || mDto.getModel_name().isEmpty()) continue;
              logger.info("正在导入型号: {}", mDto.getModel_name());
              PhoneModel model = modelRepository.findBySeriesAndModelName(series, mDto.getModel_name()).orElse(null);
              if (model == null) {
                model = new PhoneModel();
                model.setBrand(brand);
                model.setSeries(series);
                model.setModelName(mDto.getModel_name());
                model.setSort(k + 1);
                model.setDeleted(0);
                model = modelRepository.save(model);
              } else {
                model.setSort(k + 1);
                model.setDeleted(0);
                model = modelRepository.save(model);
              }
              modelCount++;

              if (mDto.getSpecArr() != null) {
                for (int l = 0; l < mDto.getSpecArr().size(); l++) {
                  SpecDto spDto = mDto.getSpecArr().get(l);
                  if (spDto.getSpec_name() == null || spDto.getSpec_name().isEmpty()) continue;
                  logger.info("正在导入配置: {}", spDto.getSpec_name());
                  PhoneSpec spec = specRepository.findByModelAndSpecName(model, spDto.getSpec_name()).orElse(null);
                  if (spec == null) {
                    spec = new PhoneSpec();
                    spec.setBrand(brand);
                    spec.setSeries(series);
                    spec.setModel(model);
                    spec.setSpecName(spDto.getSpec_name());
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
    Optional<Brand> brand = brandRepository.findById(brandId);
    if (brand.isEmpty()) return List.of();
    return seriesRepository.findByBrand(brand.get()).stream()
        .filter(s -> s.getDeleted() == null || s.getDeleted() == 0)
        .sorted((a, b2) -> Integer.compare(a.getSort() == null ? 0 : a.getSort(), b2.getSort() == null ? 0 : b2.getSort()))
        .collect(Collectors.toList());
  }

  public List<PhoneModel> listModelsBySeriesId(Long seriesId) {
    Optional<PhoneSeries> series = seriesRepository.findById(seriesId);
    if (series.isEmpty()) return List.of();
    return modelRepository.findBySeries(series.get()).stream()
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
      Brand brand = brandMap.get(id);
      if (brand != null) {
        brand.setSort(i);
        brandRepository.save(brand);
      }
    }
    return true;
  }

  public PhoneSeries addSeries(Long brandId, String seriesName) {
    Optional<Brand> brandOpt = brandRepository.findById(brandId);
    if (brandOpt.isEmpty()) {
      return null;
    }
    Brand brand = brandOpt.get();

    Optional<PhoneSeries> existing = seriesRepository.findByBrandAndSeriesName(brand, seriesName);
    if (existing.isPresent()) {
      PhoneSeries series = existing.get();
      if (series.getDeleted() != null && series.getDeleted() == 1) {
        series.setDeleted(0);
        Integer maxSort = seriesRepository.findMaxSortByBrandId(brandId);
        series.setSort(maxSort == null ? 0 : maxSort + 1);
        return seriesRepository.save(series);
      }
      return series;
    }

    PhoneSeries series = new PhoneSeries();
    Long maxId = seriesRepository.findMaxId();
    series.setId(maxId == null ? 1L : maxId + 1);
    series.setBrand(brand);
    series.setSeriesName(seriesName);
    series.setDeleted(0);
    Integer maxSort = seriesRepository.findMaxSortByBrandId(brandId);
    series.setSort(maxSort == null ? 0 : maxSort + 1);
    return seriesRepository.save(series);
  }

  public PhoneSeries updateSeries(Long id, String seriesName) {
    Optional<PhoneSeries> optionalSeries = seriesRepository.findById(id);
    if (optionalSeries.isPresent()) {
      PhoneSeries series = optionalSeries.get();
      series.setSeriesName(seriesName);
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

  public boolean updateSeriesSort(Long brandId, List<PhoneSeries> seriesList) {
    Optional<Brand> brandOpt = brandRepository.findById(brandId);
    if (brandOpt.isEmpty()) {
      return false;
    }
    Brand brand = brandOpt.get();
    
    // Get all valid series for this brand from DB
    List<PhoneSeries> existingSeries = seriesRepository.findByBrand(brand).stream()
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
    // Create a map for faster lookup
    java.util.Map<Long, PhoneSeries> seriesMap = existingSeries.stream()
        .collect(Collectors.toMap(PhoneSeries::getId, s -> s));

    for (int i = 0; i < seriesList.size(); i++) {
      Long id = seriesList.get(i).getId();
      PhoneSeries series = seriesMap.get(id);
      if (series != null) {
        series.setSort(i);
        seriesRepository.save(series);
      }
    }
    return true;
  }

  public boolean updateModelSort(Long seriesId, List<PhoneModel> modelList) {
    Optional<PhoneSeries> seriesOpt = seriesRepository.findById(seriesId);
    if (seriesOpt.isEmpty()) {
      return false;
    }
    PhoneSeries series = seriesOpt.get();
    
    // Get all valid models for this series from DB
    List<PhoneModel> existingModels = modelRepository.findBySeries(series).stream()
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
    // Create a map for faster lookup
    java.util.Map<Long, PhoneModel> modelMap = existingModels.stream()
        .collect(Collectors.toMap(PhoneModel::getId, m -> m));

    for (int i = 0; i < modelList.size(); i++) {
      Long id = modelList.get(i).getId();
      PhoneModel model = modelMap.get(id);
      if (model != null) {
        model.setSort(i);
        modelRepository.save(model);
      }
    }
    return true;
  }

  public PhoneModel addModel(Long brandId, Long seriesId, String modelName) {
    Optional<PhoneSeries> seriesOpt = seriesRepository.findById(seriesId);
    if (seriesOpt.isEmpty()) {
      return null;
    }
    PhoneSeries series = seriesOpt.get();
    
    // Optionally verify brandId matches series.getBrand().getId()
    if (brandId != null && !brandId.equals(series.getBrand().getId())) {
      // Logic conflict: series does not belong to the provided brandId
      // You might want to handle this, or just ignore brandId and trust seriesId
      // For now, let's just proceed with the series found
    }

    Optional<PhoneModel> existing = modelRepository.findBySeriesAndModelName(series, modelName);
    if (existing.isPresent()) {
      PhoneModel model = existing.get();
      if (model.getDeleted() != null && model.getDeleted() == 1) {
        model.setDeleted(0);
        Integer maxSort = modelRepository.findMaxSortBySeriesId(seriesId);
        model.setSort(maxSort == null ? 0 : maxSort + 1);
        return modelRepository.save(model);
      }
      return model;
    }

    PhoneModel model = new PhoneModel();
    Long maxId = modelRepository.findMaxId();
    model.setId(maxId == null ? 1L : maxId + 1);
    model.setBrand(series.getBrand());
    model.setSeries(series);
    model.setModelName(modelName);
    model.setDeleted(0);
    Integer maxSort = modelRepository.findMaxSortBySeriesId(seriesId);
    model.setSort(maxSort == null ? 0 : maxSort + 1);
    return modelRepository.save(model);
  }

  public PhoneModel updateModel(Long id, String modelName) {
    Optional<PhoneModel> optionalModel = modelRepository.findById(id);
    if (optionalModel.isPresent()) {
      PhoneModel model = optionalModel.get();
      model.setModelName(modelName);
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

  public PhoneSpec addSpec(Long modelId, String specName) {
    Optional<PhoneModel> modelOpt = modelRepository.findById(modelId);
    if (modelOpt.isEmpty()) {
      return null;
    }
    PhoneModel model = modelOpt.get();

    Optional<PhoneSpec> existing = specRepository.findByModelAndSpecName(model, specName);
    if (existing.isPresent()) {
      PhoneSpec spec = existing.get();
      if (spec.getDeleted() != null && spec.getDeleted() == 1) {
        spec.setDeleted(0);
        Integer maxSort = specRepository.findMaxSortByModelId(modelId);
        spec.setSort(maxSort == null ? 0 : maxSort + 1);
        return specRepository.save(spec);
      }
      return spec;
    }

    PhoneSpec spec = new PhoneSpec();
    Long maxId = specRepository.findMaxId();
    spec.setId(maxId == null ? 1L : maxId + 1);
    spec.setBrand(model.getBrand());
    spec.setSeries(model.getSeries());
    spec.setModel(model);
    spec.setSpecName(specName);
    spec.setDeleted(0);
    Integer maxSort = specRepository.findMaxSortByModelId(modelId);
    spec.setSort(maxSort == null ? 0 : maxSort + 1);
    return specRepository.save(spec);
  }

  public PhoneSpec updateSpec(Long id, String specName) {
    Optional<PhoneSpec> optionalSpec = specRepository.findById(id);
    if (optionalSpec.isPresent()) {
      PhoneSpec spec = optionalSpec.get();
      spec.setSpecName(specName);
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

  public boolean updateSpecSort(Long modelId, List<PhoneSpec> specList) {
    Optional<PhoneModel> modelOpt = modelRepository.findById(modelId);
    if (modelOpt.isEmpty()) {
      return false;
    }
    PhoneModel model = modelOpt.get();
    
    // Get all valid specs for this model from DB
    List<PhoneSpec> existingSpecs = specRepository.findByModel(model).stream()
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
    // Create a map for faster lookup
    java.util.Map<Long, PhoneSpec> specMap = existingSpecs.stream()
        .collect(Collectors.toMap(PhoneSpec::getId, s -> s));

    for (int i = 0; i < specList.size(); i++) {
      Long id = specList.get(i).getId();
      PhoneSpec spec = specMap.get(id);
      if (spec != null) {
        spec.setSort(i);
        specRepository.save(spec);
      }
    }
    return true;
  }

  public List<PhoneSpec> listSpecsByModelId(Long modelId) {
    Optional<PhoneModel> modelOpt = modelRepository.findById(modelId);
    if (modelOpt.isEmpty()) {
      return Collections.emptyList();
    }
    return specRepository.findByModel(modelOpt.get()).stream()
        .filter(s -> s.getDeleted() == null || s.getDeleted() == 0)
        .sorted(Comparator.comparing(PhoneSpec::getSort))
        .collect(Collectors.toList());
  }
}
