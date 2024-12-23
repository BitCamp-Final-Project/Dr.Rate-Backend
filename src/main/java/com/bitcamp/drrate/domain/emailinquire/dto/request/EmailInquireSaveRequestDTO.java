package com.bitcamp.drrate.domain.emailinquire.dto.request;

import lombok.Data;

@Data
public class EmailInquireSaveRequestDTO {
    private String inquireCtg; // 문의 카테고리
    private Long inquireId; // 사용자 ID
    private String inquireEmail; // 사용자 이메일
    private String inquireTitle; // 문의 제목
    private String inquireContent; // 문의 내용
    private String fileUuid; // 첨부파일 UUID (선택적)
}