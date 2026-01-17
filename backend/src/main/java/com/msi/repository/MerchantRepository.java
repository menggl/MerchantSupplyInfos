package com.msi.repository;

import com.msi.domain.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {
    boolean existsByWechatId(String wechatId);
    boolean existsByMerchantPhone(String merchantPhone);
    java.util.Optional<Merchant> findByWechatId(String wechatId);
    java.util.Optional<Merchant> findByWechatIdAndIsValid(String wechatId, Integer isValid);
    java.util.Optional<Merchant> findByToken(String token);
    java.util.Optional<Merchant> findByPublicId(String publicId);
}
