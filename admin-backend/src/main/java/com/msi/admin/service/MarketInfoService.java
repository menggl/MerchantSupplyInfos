package com.msi.admin.service;

import com.msi.admin.domain.MarketInfo;
import com.msi.admin.repository.MarketInfoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class MarketInfoService {
    private final MarketInfoRepository marketInfoRepository;

    public MarketInfoService(MarketInfoRepository marketInfoRepository) {
        this.marketInfoRepository = marketInfoRepository;
    }

    public Page<MarketInfo> listMarketInfos(int page, int size, String title) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "sort", "publishTime"));
        if (title != null && !title.isEmpty()) {
            return marketInfoRepository.findByIsDeletedAndTitleContaining(0, title, pageable);
        }
        return marketInfoRepository.findByIsDeleted(0, pageable);
    }

    public MarketInfo getMarketInfo(Long id) {
        return marketInfoRepository.findById(id).orElse(null);
    }

    @Transactional
    public MarketInfo createMarketInfo(MarketInfo marketInfo) {
        marketInfo.setIsDeleted(0);
        if (marketInfo.getIsOnline() == null) {
            marketInfo.setIsOnline(0); // Default offline
        }
        if (marketInfo.getIsOnline() == 1) {
            marketInfo.setPublishTime(LocalDateTime.now());
        }
        if (marketInfo.getSort() == null) {
            marketInfo.setSort(0);
        }
        return marketInfoRepository.save(marketInfo);
    }

    @Transactional
    public MarketInfo updateMarketInfo(Long id, MarketInfo marketInfoDetails) {
        Optional<MarketInfo> optionalMarketInfo = marketInfoRepository.findById(id);
        if (optionalMarketInfo.isPresent()) {
            MarketInfo existing = optionalMarketInfo.get();
            existing.setTitle(marketInfoDetails.getTitle());
            existing.setSummary(marketInfoDetails.getSummary());
            existing.setContent(marketInfoDetails.getContent());
            existing.setSort(marketInfoDetails.getSort());
            
            // Handle publish time if status changes to online
            if (marketInfoDetails.getIsOnline() != null) {
                if (existing.getIsOnline() != 1 && marketInfoDetails.getIsOnline() == 1) {
                    existing.setPublishTime(LocalDateTime.now());
                }
                existing.setIsOnline(marketInfoDetails.getIsOnline());
            }
            
            return marketInfoRepository.save(existing);
        }
        return null;
    }

    @Transactional
    public void deleteMarketInfo(Long id) {
        Optional<MarketInfo> optionalMarketInfo = marketInfoRepository.findById(id);
        if (optionalMarketInfo.isPresent()) {
            MarketInfo existing = optionalMarketInfo.get();
            existing.setIsDeleted(1);
            marketInfoRepository.save(existing);
        }
    }
}
