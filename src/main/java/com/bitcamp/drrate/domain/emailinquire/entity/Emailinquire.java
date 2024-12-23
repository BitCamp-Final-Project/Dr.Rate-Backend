package com.bitcamp.drrate.domain.emailinquire.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.bitcamp.drrate.global.entity.BaseEntity;

@Entity
@Table(name = "email_inquire") // 테이블명 지정
@Getter
@Setter
@NoArgsConstructor
public class Emailinquire extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 자동 생성
    private Long id;

    @Column(name = "inquire_ctg", nullable = false)
    private String inquireCtg; // 이메일 문의 카테고리

    @Column(name = "inquire_id", nullable = false)
    private Long inquireId; // 이메일 문의자 아이디(users 테이블의 id)

    @Column(name = "inquire_email", nullable = false)
    private String inquireEmail; // 이메일 문의자 이메일

    @Column(name = "inquire_title", nullable = false)
    private String inquireTitle; // 이메일 문의 제목

    @Column(name = "inquire_content", columnDefinition = "TEXT", nullable = false)
    private String inquireContent; // 이메일 문의 내역

    @Column(name = "file_uuid")
    private String fileUuid; // 이메일 첨부파일 UUID

    @Column(name = "answer_title")
    private String answerTitle; // 이메일 문의 답변 제목

    @Column(name = "answer_content", columnDefinition = "TEXT")
    private String answerContent; // 이메일 문의 답변 내용

}