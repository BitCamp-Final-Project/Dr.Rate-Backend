package com.bitcamp.drrate.domain.calendar.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "calendar")
@Data
@NoArgsConstructor
public class Calendar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 기본 키 

    @Column(name = "cal_user_id", nullable = false)
    private Long cal_user_id; // 사용자 ID

    @Column(name = "bank_name", nullable = false)
    private String bank_name; // 은행명
   
    @Column(name = "installment_name", nullable = false)
    private String installment_name; // 적금명

    @Column(name = "amount", nullable = false)
    private Long amount; // 금액

    @Column(name = "start_date", nullable = false)
    private LocalDate start_date; // 시작날짜

    @Column(name = "end_date", nullable = false)
    private LocalDate end_date; // 만기일
    
    @Column(name = "group_id", nullable = false)
    private String groupId; // 이벤트 그룹

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at; // 생성 날짜

    @Column(name = "updated_at")
    private LocalDateTime updated_at; // 수정 날짜

    @PrePersist
    public void prePersist() {
        this.created_at = LocalDateTime.now();  // 생성 시 현재 시간
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updated_at = LocalDateTime.now(); // 수정 시 현재 시간
    }

    @Builder
    public Calendar(Long cal_user_id, String bank_name, String installment_name, Long amount, LocalDate start_date, LocalDate end_date, String groupId) {
        this.cal_user_id = cal_user_id;
        this.bank_name = bank_name;
        this.installment_name = installment_name;
        this.amount = amount;
        this.start_date = start_date;
        this.end_date = end_date;
        this.groupId = groupId;
    }
}
