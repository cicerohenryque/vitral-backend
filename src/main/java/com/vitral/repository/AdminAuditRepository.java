package com.vitral.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.vitral.entity.AdminAudit;

public interface AdminAuditRepository extends JpaRepository<AdminAudit, Long>, JpaSpecificationExecutor<AdminAudit> {
    @Override
    @EntityGraph(attributePaths = "admin")
    Page<AdminAudit> findAll(Specification<AdminAudit> spec, Pageable pageable);
}
