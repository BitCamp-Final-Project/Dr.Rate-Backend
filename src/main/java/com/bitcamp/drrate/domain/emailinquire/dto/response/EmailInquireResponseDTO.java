package com.bitcamp.drrate.domain.emailinquire.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmailInquireResponseDTO {
    private Long id; // 문의 ID
    private String inquireCtg; // 문의 카테고리
    private Long inquireId; // 사용자 ID
    private String inquireEmail; // 사용자 이메일
    private String inquireTitle; // 문의 제목
    private String inquireContent; // 문의 내용
    private String fileUuid; // 첨부파일 UUID (선택적)
    private String answerTitle; // 답변 제목 (선택적)
    private String answerContent; // 답변 내용 (선택적)
    private LocalDateTime createdAt; // 문의 생성 날짜
    private LocalDateTime updatedAt; // 문의 업데이트 날짜
}