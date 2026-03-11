package com.msi.repository;

import com.msi.domain.MerchantInvitationCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantInvitationCodeRepository extends JpaRepository<MerchantInvitationCode, Long> {
    long countByInvitationCodeAndIsValid(String invitationCode, Integer isValid);
}

