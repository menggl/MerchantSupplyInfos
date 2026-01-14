package com.msi.service;

import com.msi.domain.Product;
import com.msi.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import com.msi.repository.CityDictRepository;
import com.msi.domain.CityDict;
import com.msi.dto.CityDto;

@Service
public class SupplyService {
  private final ProductRepository productRepository;
  private final CityDictRepository cityRepository;

  public SupplyService(ProductRepository productRepository, CityDictRepository cityRepository) {
    this.productRepository = productRepository;
    this.cityRepository = cityRepository;
  }

  public List<CityDto> getCities() {
    List<CityDict> list = cityRepository.findAllByValidOrderBySortAsc(1);
    List<CityDto> cities = list.stream()
        .filter(c -> c.getCityName() != null && !"全国".equals(c.getCityName()))
        .map(c -> new CityDto(c.getCityCode(), c.getCityName()))
        .toList();
    List<CityDto> result = new ArrayList<>();
    result.add(new CityDto("000000", "全国"));
    result.addAll(cities);
    return result;
  }

  public List<Product> search(String cityCode, String brand, String model, String keyword, int page, int size) {
    Specification<Product> spec = Specification.where(null);
    if (cityCode != null && !cityCode.isEmpty() && !"000000".equals(cityCode) && !"全国".equals(cityCode)) {
      spec = spec.and((root, q, cb) -> cb.equal(root.get("cityCode"), cityCode));
    }
    if (brand != null && !brand.isEmpty()) {
      spec = spec.and((root, q, cb) -> cb.like(root.get("brand").get("name"), "%" + brand + "%"));
    }
    if (model != null && !model.isEmpty()) {
      spec = spec.and((root, q, cb) -> cb.like(root.get("model").get("modelName"), "%" + model + "%"));
    }
    if (keyword != null && !keyword.isEmpty()) {
      spec = spec.and((root, q, cb) -> cb.or(
          cb.like(root.get("model").get("modelName"), "%" + keyword + "%"),
          cb.like(root.get("spec").get("specName"), "%" + keyword + "%"),
          cb.like(root.get("description"), "%" + keyword + "%")
      ));
    }
    Page<Product> p = productRepository.findAll(spec, PageRequest.of(page, size));
    return p.getContent();
  }
}
