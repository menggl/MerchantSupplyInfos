package com.msi.repository;

import com.msi.domain.CityDict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CityDictRepository extends JpaRepository<CityDict, Long> {
  Optional<CityDict> findByCityCode(String cityCode);
  boolean existsByCityCode(String cityCode);
  List<CityDict> findAllByValid(Integer valid);
  List<CityDict> findAllByValidOrderBySortAsc(Integer valid);
}
