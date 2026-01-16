package com.msi.service;

import com.msi.domain.Brand;
import com.msi.domain.CityDict;
import com.msi.domain.PhoneModel;
import com.msi.domain.PhoneSeries;
import com.msi.domain.PhoneSpec;
import com.msi.dto.*;
import com.msi.repository.BrandRepository;
import com.msi.repository.CityDictRepository;
import com.msi.repository.PhoneModelRepository;
import com.msi.repository.PhoneRemarkDictRepository;
import com.msi.repository.PhoneSeriesRepository;
import com.msi.repository.PhoneSpecRepository;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class DictService {
	private final BrandRepository brandRepository;
	private final PhoneSeriesRepository seriesRepository;
	private final PhoneModelRepository modelRepository;
	private final PhoneSpecRepository specRepository;
	private final CityDictRepository cityRepository;
	private final PhoneRemarkDictRepository phoneRemarkDictRepository;
	private final Cache<Long, String> brandNameCache;
	private final Cache<Long, String> seriesNameCache;
	private final Cache<Long, String> modelNameCache;
	private final Cache<Long, String> specNameCache;

	public DictService(BrandRepository brandRepository, PhoneSeriesRepository seriesRepository,
			PhoneModelRepository modelRepository, PhoneSpecRepository specRepository, CityDictRepository cityRepository,
			PhoneRemarkDictRepository phoneRemarkDictRepository) {
		this.brandRepository = brandRepository;
		this.seriesRepository = seriesRepository;
		this.modelRepository = modelRepository;
		this.specRepository = specRepository;
		this.cityRepository = cityRepository;
		this.phoneRemarkDictRepository = phoneRemarkDictRepository;
		this.brandNameCache = CacheBuilder.newBuilder()
				.expireAfterWrite(24, TimeUnit.HOURS)
				.maximumSize(1000)
				.build();
		this.seriesNameCache = CacheBuilder.newBuilder()
				.expireAfterWrite(24, TimeUnit.HOURS)
				.maximumSize(2000)
				.build();
		this.modelNameCache = CacheBuilder.newBuilder()
				.expireAfterWrite(24, TimeUnit.HOURS)
				.maximumSize(5000)
				.build();
		this.specNameCache = CacheBuilder.newBuilder()
				.expireAfterWrite(24, TimeUnit.HOURS)
				.maximumSize(5000)
				.build();
	}

	public List<BrandDto> getAllDicts() {
		// 1. Fetch all data sorted
		List<Brand> brands = brandRepository.findAll(Sort.by("sort").ascending());
		List<PhoneSeries> allSeries = seriesRepository.findAll(Sort.by("sort").ascending());
		List<PhoneModel> allModels = modelRepository.findAll(Sort.by("sort").ascending());
		List<PhoneSpec> allSpecs = specRepository.findAll(Sort.by("sort").ascending());

		// 2. Group by parent ID
		Map<Long, List<PhoneSeries>> seriesMap = allSeries.stream()
				.filter(s -> s.getDeleted() == null || s.getDeleted() == 0)
				.collect(Collectors.groupingBy(s -> s.getBrand().getId()));

		Map<Long, List<PhoneModel>> modelMap = allModels.stream()
				.filter(m -> m.getDeleted() == null || m.getDeleted() == 0)
				.collect(Collectors.groupingBy(m -> m.getSeries().getId()));

		Map<Long, List<PhoneSpec>> specMap = allSpecs.stream()
				.filter(s -> s.getDeleted() == null || s.getDeleted() == 0)
				.collect(Collectors.groupingBy(s -> s.getModel().getId()));

		// 3. Assemble DTOs
		return brands.stream()
				.map(brand -> {
					List<SeriesDto> seriesDtos = seriesMap.getOrDefault(brand.getId(), new ArrayList<>()).stream()
							.map(series -> {
								List<ModelDto> modelDtos = modelMap.getOrDefault(series.getId(), new ArrayList<>())
										.stream()
										.map(model -> {
											List<SpecDto> specDtos = specMap
													.getOrDefault(model.getId(), new ArrayList<>()).stream()
													.map(spec -> new SpecDto(spec.getId(), spec.getSpecName()))
													.collect(Collectors.toList());
											return new ModelDto(model.getId(), model.getModelName(),
													specDtos);
										})
										.collect(Collectors.toList());
								return new SeriesDto(series.getId(), series.getSeriesName(),
										modelDtos);
							})
							.collect(Collectors.toList());
					return new BrandDto(brand.getId(), brand.getName(), seriesDtos);
				})
				.collect(Collectors.toList());
	}

	public List<BrandDto> getAllBrands() {
		
		return brandRepository.findAll(Sort.by("sort").ascending()).stream()
				.filter(b -> b.getDeleted() == null || b.getDeleted() == 0)
				.map(brand -> new BrandDto(brand.getId(), brand.getName(), null))
				.collect(Collectors.toList());
	}

	public List<SeriesDto> getAllSeries(Long brandId) {
		Optional<Brand> brand = brandRepository.findById(brandId);
		if (brand.isEmpty()) {
			return new ArrayList<>();
		}
		return seriesRepository.findByBrandOrderBySortAsc(brand.get()).stream()
				.filter(s -> s.getDeleted() == null || s.getDeleted() == 0)
				.map(series -> new SeriesDto(brandId, series.getId(), series.getSeriesName(), null))
				.collect(Collectors.toList());
	}

	public List<ModelDto> getAllModels(Long seriesId) {
		Optional<PhoneSeries> series = seriesRepository.findById(seriesId);
		if (series.isEmpty()) {
			return new ArrayList<>();
		}
		Long brandId = series.get().getBrand().getId();
		return modelRepository.findBySeriesOrderBySortAsc(series.get()).stream()
				.filter(m -> m.getDeleted() == null || m.getDeleted() == 0)
				.map(model -> new ModelDto(brandId, seriesId, model.getId(), model.getModelName(),
						null))
				.collect(Collectors.toList());
	}

	public List<SpecDto> getAllSpecs(Long modelId) {
		Optional<PhoneModel> model = modelRepository.findById(modelId);
		if (model.isEmpty()) {
			return new ArrayList<>();
		}
		Long seriesId = model.get().getSeries().getId();
		Long brandId = model.get().getSeries().getBrand().getId();
		return specRepository.findByModelOrderBySortAsc(model.get()).stream()
				.filter(s -> s.getDeleted() == null || s.getDeleted() == 0)
				.map(spec -> new SpecDto(brandId, seriesId, modelId, spec.getId(), spec.getSpecName()))
				.collect(Collectors.toList());
	}

	// 品牌CRUD方法
	public Brand addBrand(String name) {
		Brand brand = new Brand();
		brand.setName(name);
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
		brandRepository.deleteById(id);
	}

	public void updateBrandSort(Long id, Integer sort) {
		Optional<Brand> optionalBrand = brandRepository.findById(id);
		if (optionalBrand.isPresent()) {
			Brand brand = optionalBrand.get();
			brand.setSort(sort);
			brandRepository.save(brand);
		}
	}

	// 系列CRUD方法
	public PhoneSeries addSeries(String brandName, String seriesName) {
		Brand brand = brandRepository.findByName(brandName).orElse(null);
		if (brand == null)
			return null;

		PhoneSeries series = new PhoneSeries();
		series.setBrand(brand);
		series.setSeriesName(seriesName);
		series.setDeleted(0);
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

	public void updateSeriesSort(Long id, Integer sort) {
		Optional<PhoneSeries> optionalSeries = seriesRepository.findById(id);
		if (optionalSeries.isPresent()) {
			PhoneSeries series = optionalSeries.get();
			series.setSort(sort);
			seriesRepository.save(series);
		}
	}

	// 型号CRUD方法
	public PhoneModel addModel(String brandName, String seriesName, String modelName) {
		Brand brand = brandRepository.findByName(brandName).orElse(null);
		if (brand == null)
			return null;

		List<PhoneSeries> ss = seriesRepository.findByBrandOrderBySortAsc(brand).stream()
				.filter(s -> seriesName.equals(s.getSeriesName()))
				.collect(Collectors.toList());
		if (ss.isEmpty())
			return null;

		PhoneModel model = new PhoneModel();
		model.setBrand(brand);
		model.setSeries(ss.get(0));
		model.setModelName(modelName);
		model.setDeleted(0);
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

	public void updateModelSort(Long id, Integer sort) {
		Optional<PhoneModel> optionalModel = modelRepository.findById(id);
		if (optionalModel.isPresent()) {
			PhoneModel model = optionalModel.get();
			model.setSort(sort);
			modelRepository.save(model);
		}
	}

	// 配置CRUD方法
	public PhoneSpec addSpec(String modelName, String specName) {
		List<PhoneModel> ms = modelRepository.findAll().stream()
				.filter(m -> modelName.equals(m.getModelName()))
				.collect(Collectors.toList());
		if (ms.isEmpty())
			return null;

		PhoneModel model = ms.get(0);
		PhoneSpec spec = new PhoneSpec();
		spec.setBrand(model.getBrand());
		spec.setSeries(model.getSeries());
		spec.setModel(model);
		spec.setSpecName(specName);
		spec.setDeleted(0);
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

	public void updateSpecSort(Long id, Integer sort) {
		Optional<PhoneSpec> optionalSpec = specRepository.findById(id);
		if (optionalSpec.isPresent()) {
			PhoneSpec spec = optionalSpec.get();
			spec.setSort(sort);
			specRepository.save(spec);
		}
	}

	// 根据ID获取实体的方法
	public Brand getBrandById(Long id) {
		return brandRepository.findById(id).orElse(null);
	}

	public PhoneSeries getSeriesById(Long id) {
		return seriesRepository.findById(id).orElse(null);
	}

	public PhoneModel getModelById(Long id) {
		return modelRepository.findById(id).orElse(null);
	}

	public PhoneSpec getSpecById(Long id) {
		return specRepository.findById(id).orElse(null);
	}

	// 根据ID获取名称的方法
	public String getBrandNameById(Long id) {
		if (id == null) {
			return null;
		}
		String cached = brandNameCache.getIfPresent(id);
		if (cached != null && !cached.isEmpty()) {
			return cached;
		}
		Brand brand = brandRepository.findById(id).orElse(null);
		String name = brand != null ? brand.getName() : null;
		if (name != null && !name.isEmpty()) {
			brandNameCache.put(id, name);
		}
		return name;
	}

	public String getSeriesNameById(Long id) {
		if (id == null) {
			return null;
		}
		String cached = seriesNameCache.getIfPresent(id);
		if (cached != null && !cached.isEmpty()) {
			return cached;
		}
		PhoneSeries series = seriesRepository.findById(id).orElse(null);
		String name = series != null ? series.getSeriesName() : null;
		if (name != null && !name.isEmpty()) {
			seriesNameCache.put(id, name);
		}
		return name;
	}

	public String getModelNameById(Long id) {
		if (id == null) {
			return null;
		}
		String cached = modelNameCache.getIfPresent(id);
		if (cached != null && !cached.isEmpty()) {
			return cached;
		}
		PhoneModel model = modelRepository.findById(id).orElse(null);
		String name = model != null ? model.getModelName() : null;
		if (name != null && !name.isEmpty()) {
			modelNameCache.put(id, name);
		}
		return name;
	}

	public String getSpecNameById(Long id) {
		if (id == null) {
			return null;
		}
		String cached = specNameCache.getIfPresent(id);
		if (cached != null && !cached.isEmpty()) {
			return cached;
		}
		PhoneSpec spec = specRepository.findById(id).orElse(null);
		String name = spec != null ? spec.getSpecName() : null;
		if (name != null && !name.isEmpty()) {
			specNameCache.put(id, name);
		}
		return name;
	}

	public List<CityDto> getAllCities() {
		List<CityDict> list = cityRepository.findAllByValidOrderBySortAsc(1);
		List<CityDto> cities = list.stream()
				.filter(c -> c.getCityName() != null && !"全国".equals(c.getCityName()))
				.map(c -> new CityDto(c.getCityCode(), c.getCityName(), c.getIsOnline()))
				.collect(Collectors.toList());
		List<CityDto> result = new ArrayList<>();
		result.add(new CityDto("000000", "全国", 1));
		result.addAll(cities);
		return result;
	}

	public List<PhoneRemarkDto> getAllPhoneRemark() {
		return phoneRemarkDictRepository.findByTypeAndValidOrderBySortAsc(0, 1).stream()
				.map(r -> new PhoneRemarkDto(r.getId(), r.getRemarkName()))
				.collect(Collectors.toList());
	}

	public List<PhoneRemarkDto> getAllPhoneModelRemark() {
		return phoneRemarkDictRepository.findByTypeAndValidOrderBySortAsc(1, 1).stream()
				.map(r -> new PhoneRemarkDto(r.getId(), r.getRemarkName()))
				.collect(Collectors.toList());
	}

	public List<PhoneRemarkDto> getAllPhoneModelVersion() {
		return phoneRemarkDictRepository.findByTypeAndValidOrderBySortAsc(2, 1).stream()
				.map(r -> new PhoneRemarkDto(r.getId(), r.getRemarkName()))
				.collect(Collectors.toList());
	}

	public List<PhoneRemarkDto> getAllPhoneModelCondition() {
		return phoneRemarkDictRepository.findByTypeAndValidOrderBySortAsc(3, 1).stream()
				.map(r -> new PhoneRemarkDto(r.getId(), r.getRemarkName()))
				.collect(Collectors.toList());
	}

	public List<PhoneRemarkDto> getAllPhoneModelMaintenance() {
		return phoneRemarkDictRepository.findByTypeAndValidOrderBySortAsc(4, 1).stream()
				.map(r -> new PhoneRemarkDto(r.getId(), r.getRemarkName()))
				.collect(Collectors.toList());
	}
}
