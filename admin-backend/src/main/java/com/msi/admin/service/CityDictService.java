package com.msi.admin.service;

import com.msi.admin.domain.CityDict;
import com.msi.admin.repository.CityDictRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CityDictService {
    private final CityDictRepository cityDictRepository;

    public CityDictService(CityDictRepository cityDictRepository) {
        this.cityDictRepository = cityDictRepository;
    }

    public List<CityDict> findAll() {
        return cityDictRepository.findAll();
    }

    public List<CityDict> findValidCities() {
        return cityDictRepository.findByValidOrderBySortAsc(1);
    }

    public List<CityDict> findAllCities() {
        return cityDictRepository.findAllByOrderBySortAsc();
    }

    @Transactional
    public CityDict addCity(String cityCode, String cityName) {
        Optional<CityDict> existing = cityDictRepository.findByCityCode(cityCode);
        if (existing.isPresent()) {
            CityDict city = existing.get();
            city.setCityName(cityName); // Update name if code exists
            city.setValid(1); // Re-enable if it was invalid
            if (city.getSort() == null || city.getSort() <= 0) {
                Integer maxSort = cityDictRepository.findMaxSort();
                city.setSort((maxSort == null ? 0 : maxSort) + 1);
            }
            return cityDictRepository.save(city);
        }
        CityDict city = new CityDict();
        city.setCityCode(cityCode);
        city.setCityName(cityName);
        city.setValid(1);
        Integer maxSort = cityDictRepository.findMaxSort();
        city.setSort((maxSort == null ? 0 : maxSort) + 1);
        return cityDictRepository.save(city);
    }

    @Transactional
    public CityDict updateCity(Long id, String cityCode, String cityName, Integer valid) {
        Optional<CityDict> optionalCity = cityDictRepository.findById(id);
        if (optionalCity.isPresent()) {
            CityDict city = optionalCity.get();
            if (cityCode != null) city.setCityCode(cityCode);
            if (cityName != null) city.setCityName(cityName);
            if (valid != null) city.setValid(valid);
            return cityDictRepository.save(city);
        }
        return null;
    }

    @Transactional
    public boolean deleteCity(Long id) {
        if (!cityDictRepository.existsById(id)) {
            return false;
        }
        cityDictRepository.deleteById(id);
        return true;
    }

    @Transactional
    public boolean updateCitySort(List<CityDict> cityList) {
        for (CityDict item : cityList) {
            Optional<CityDict> optionalCity = cityDictRepository.findById(item.getId());
            if (optionalCity.isPresent()) {
                CityDict city = optionalCity.get();
                city.setSort(item.getSort());
                cityDictRepository.save(city);
            }
        }
        return true;
    }
}
