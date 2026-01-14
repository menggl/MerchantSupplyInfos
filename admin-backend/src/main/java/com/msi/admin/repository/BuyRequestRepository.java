package com.msi.admin.repository;

import com.msi.admin.domain.BuyRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BuyRequestRepository extends JpaRepository<BuyRequest, Long>, JpaSpecificationExecutor<BuyRequest> {
}
