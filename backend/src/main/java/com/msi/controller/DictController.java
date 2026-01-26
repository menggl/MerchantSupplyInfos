package com.msi.controller;

import com.msi.dto.BrandDto;
import com.msi.dto.ModelDto;
import com.msi.dto.SeriesDto;
import com.msi.dto.SpecDto;
import com.msi.service.DictService;
import com.msi.dto.CityDto;
import com.msi.dto.PhoneRemarkDto;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class DictController {
  private final DictService dictService;

  public DictController(DictService dictService) {
    this.dictService = dictService;
  }

  /**
   * 查询字典数据表，返回品牌、系列、型号、配置所有数据的json字符串
   * 判断如果deleted字段为1，则去掉该值
   * 根据品牌、系列、型号、配置对应表中的sort字段进行排序
   * 返回json数据中包含brand_id,series_id、model_id,spec_id（为表中的主键id值）
   * 返回json数据中包含brand_name、series_name、model_name、spec_name（对应各个表中的名称字段）
   * 返回sort字段，用于前端展示排序使用
   * 返回对应的层级关系，下一级数据为上一级的json数组，下一级的key值分别为seriesArr，modelArr,specArr
   */
  @Deprecated
  @GetMapping("/dict/all")
  public List<BrandDto> allDicts() {
    return dictService.getAllDicts();
  }

  /**
   * 查询所有的品牌信息，返回给前端，包含品牌ID，品牌名称，品牌排序字段
   */
  @GetMapping("/dict/brands")
  public List<BrandDto> allBrands() {
    return dictService.getAllBrands();
  }

  /**
   * 根据品牌ID查询其下面的所有系列、型号、配置信息
   * 品牌下面的系列（phone_series）根据sort字段升序排列，deleted字段必须为0，返回id，series_name，sort三个字段
   * 品牌下面的型号（phone_model）根据sort字段升序排列，deleted字段必须为0，返回id，model_name，sort三个字段
   * 品牌下面的配置（phone_spec）根据sort字段升序排列，deleted字段必须为0，返回id，spec_name，sort三个字段
   * 该接口使用一个新创建的dto来封装返回数据，包含brandId、seriesArr、modelArr、specArr四个字段
   * seriesArr字段为品牌下面的所有系列信息，每个系列信息包含id、series_name、sort三个字段
   * modelArr字段为品牌下面的所有型号信息，每个型号信息包含id、model_name、sort三个字段
   * specArr字段为品牌下面的所有配置信息，每个配置信息包含id、spec_name、sort三个字段
   * 该接口返回的json数据中，brandId字段为请求参数中的品牌ID，seriesArr字段为品牌下面的所有系列信息，modelArr字段为品牌下面的所有型号信息，specArr字段为品牌下面的所有配置信息
   */
  @GetMapping("/dict/brand-details/{brandId}")
  public com.msi.dto.BrandDetailsDto brandDetails(@PathVariable Long brandId) {
    return dictService.getBrandDetails(brandId);
  }

  /**
   * 根据品牌ID查询所有的系列信息，返回给前端，包含品牌ID，系列ID，系列名称，系列排序字段
   */
  @Deprecated
  @GetMapping("/dict/series/{brandId}")
  public List<SeriesDto> allSeries(@PathVariable Long brandId) {
    return dictService.getAllSeries(brandId);
  }

  /**
   * 根据系列ID查询所有的型号信息，返回给前端，包含品牌ID，系列ID，型号ID，型号名称，型号排序字段
   */
  @Deprecated
  @GetMapping("/dict/models/{seriesId}")
  public List<ModelDto> allModels(@PathVariable Long seriesId) {
    return dictService.getAllModels(seriesId);
  }

  /**
   * 根据型号ID查询所有的配置信息，返回给前端，包含品牌ID，系列ID，型号ID，配置ID，配置名称，配置排序字段
   */
  @Deprecated
  @GetMapping("/dict/specs/{modelId}")
  public List<SpecDto> allSpecs(@PathVariable Long modelId) {
    return dictService.getAllSpecs(modelId);
  }

  /**
   * 获取城市列表
   */
  @GetMapping("/dict/cities")
  public List<CityDto> allCities() {
    return dictService.getAllCities();
  }

  /**
   * phone_remark_dict 查询新机备注字典选项列表
   */
  @GetMapping("/dict/phone-remark")
  public List<PhoneRemarkDto> allPhoneRemark() {
    return dictService.getAllPhoneRemark();
  }

  /**
   * phone_model_dict 查询新机其它备注选项列表
   */
  @GetMapping("/dict/phone-model-remark")
  public List<PhoneRemarkDto> allPhoneModelRemark() {
    return dictService.getAllPhoneModelRemark();
  }

  /**
   * phone_model_dict 查询二手机版本选项列表
   */
  @GetMapping("/dict/phone-model-version")
  public List<PhoneRemarkDto> allPhoneModelVersion() {
    return dictService.getAllPhoneModelVersion();
  }

  /**
   * phone_model_dict 查询二手手机成色选项列表
   */
  @GetMapping("/dict/phone-model-condition")
  public List<PhoneRemarkDto> allPhoneModelCondition() {
    return dictService.getAllPhoneModelCondition();
  }

  /**
   * phone_model_dict 查询二手手机维修和功能选项列表
   */
  @GetMapping("/dict/phone-model-maintenance")
  public List<PhoneRemarkDto> allPhoneModelMaintenance() {
    return dictService.getAllPhoneModelMaintenance();
  }
 
}
