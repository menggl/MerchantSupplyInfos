package com.msi.service;

import com.msi.domain.MarketInfo;
import com.msi.repository.MarketInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MarketInfoService {
    @Autowired
    private MarketInfoRepository marketInfoRepository;

    public Page<MarketInfo> getMarketInfoList(Pageable pageable) {
        return marketInfoRepository.findByIsOnlineAndIsDeleted(1, 0, pageable);
    }

    public List<MarketInfo> getTop3MarketInfos() {
        return marketInfoRepository.findTop3ByIsOnlineAndIsDeletedOrderBySortAsc(1, 0);
    }

    public Optional<MarketInfo> getMarketInfoDetail(Long id) {
        return marketInfoRepository.findByIdAndIsOnlineAndIsDeleted(id, 1, 0);
    }
}
