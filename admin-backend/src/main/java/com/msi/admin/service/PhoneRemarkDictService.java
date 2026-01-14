package com.msi.admin.service;

import com.msi.admin.domain.PhoneRemarkDict;
import com.msi.admin.repository.PhoneRemarkDictRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PhoneRemarkDictService {
  private final PhoneRemarkDictRepository phoneRemarkDictRepository;

  public PhoneRemarkDictService(PhoneRemarkDictRepository phoneRemarkDictRepository) {
    this.phoneRemarkDictRepository = phoneRemarkDictRepository;
  }

  public List<PhoneRemarkDict> findByType(Integer type) {
    if (type == null) {
      return phoneRemarkDictRepository.findAllByOrderByTypeAscSortAsc();
    }
    return phoneRemarkDictRepository.findByTypeOrderBySortAsc(type);
  }

  public PhoneRemarkDict updateRemarkName(Long id, String remarkName) {
    if (id == null) return null;
    if (remarkName == null || remarkName.trim().isEmpty()) return null;
    Optional<PhoneRemarkDict> optional = phoneRemarkDictRepository.findById(id);
    if (optional.isEmpty()) return null;
    PhoneRemarkDict item = optional.get();
    item.setRemarkName(remarkName.trim());
    return phoneRemarkDictRepository.save(item);
  }

  public PhoneRemarkDict updateValid(Long id, Integer valid) {
    if (id == null) return null;
    if (valid == null) return null;
    Optional<PhoneRemarkDict> optional = phoneRemarkDictRepository.findById(id);
    if (optional.isEmpty()) return null;
    PhoneRemarkDict item = optional.get();
    item.setValid(valid);
    return phoneRemarkDictRepository.save(item);
  }

  @Transactional
  public PhoneRemarkDict addRemark(Integer type, String remarkName) {
    int safeType = type == null ? 0 : type;
    if (remarkName == null || remarkName.trim().isEmpty()) return null;

    Integer maxSort = phoneRemarkDictRepository.findMaxSortByType(safeType);
    PhoneRemarkDict item = new PhoneRemarkDict();
    item.setType(safeType);
    item.setRemarkName(remarkName.trim());
    item.setValid(1);
    item.setSort(maxSort == null ? 1 : maxSort + 1);
    try {
      return phoneRemarkDictRepository.save(item);
    } catch (DataIntegrityViolationException e) {
      return null;
    }
  }

  @Transactional
  public boolean deleteRemark(Long id) {
    if (id == null) return false;
    Optional<PhoneRemarkDict> optional = phoneRemarkDictRepository.findById(id);
    if (optional.isEmpty()) return false;
    PhoneRemarkDict item = optional.get();
    Integer type = item.getType() == null ? 0 : item.getType();
    phoneRemarkDictRepository.delete(item);

    List<PhoneRemarkDict> remaining = phoneRemarkDictRepository.findByTypeOrderBySortAsc(type);
    int sort = 1;
    for (PhoneRemarkDict r : remaining) {
      r.setSort(sort++);
    }
    phoneRemarkDictRepository.saveAll(remaining);
    return true;
  }

  public boolean updateSort(List<PhoneRemarkDict> items) {
    if (items == null || items.isEmpty()) return true;

    List<Long> idsInOrder = new ArrayList<>();
    for (PhoneRemarkDict i : items) {
      if (i != null && i.getId() != null) {
        idsInOrder.add(i.getId());
      }
    }
    if (idsInOrder.isEmpty()) return true;

    List<PhoneRemarkDict> existing = phoneRemarkDictRepository.findAllById(idsInOrder);
    Map<Long, PhoneRemarkDict> byId = new HashMap<>();
    for (PhoneRemarkDict e : existing) {
      byId.put(e.getId(), e);
    }

    Map<Integer, List<Long>> idsByType = new HashMap<>();
    for (Long id : idsInOrder) {
      PhoneRemarkDict e = byId.get(id);
      if (e == null) continue;
      Integer type = e.getType() == null ? 0 : e.getType();
      idsByType.computeIfAbsent(type, k -> new ArrayList<>()).add(id);
    }

    List<PhoneRemarkDict> toSave = new ArrayList<>();
    for (Map.Entry<Integer, List<Long>> entry : idsByType.entrySet()) {
      int sort = 1;
      for (Long id : entry.getValue()) {
        PhoneRemarkDict e = byId.get(id);
        if (e == null) continue;
        e.setSort(sort++);
        toSave.add(e);
      }
    }

    phoneRemarkDictRepository.saveAll(toSave);
    return true;
  }
}
