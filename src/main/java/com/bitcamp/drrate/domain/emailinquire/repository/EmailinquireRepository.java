package com.bitcamp.drrate.domain.emailinquire.repository;

import com.bitcamp.drrate.domain.emailinquire.entity.Emailinquire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmailinquireRepository extends JpaRepository<Emailinquire, Long> {

    // 문의 내역 조회: 특정 사용자 ID로 조회
    List<Emailinquire> findByInquireId(Long inquireId);

}