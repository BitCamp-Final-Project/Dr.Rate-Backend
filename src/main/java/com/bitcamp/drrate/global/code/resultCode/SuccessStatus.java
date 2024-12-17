package com.bitcamp.drrate.global.code.resultCode;


import org.springframework.http.HttpStatus;

import com.bitcamp.drrate.global.code.SuccessCode;
import com.bitcamp.drrate.global.code.SuccessDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor  // 열거형 필드가 3개(httpstatus, code, message)가 있고, @AllArgsConstructor를 했기 때문에 이게 열거형을 정의할 때도 적용이 된다.
// Naming Format : {행위}_{목적어}_{성공여부}
public enum SuccessStatus implements SuccessCode {
    // Common
    _OK(HttpStatus.OK,"COMMON200", "성공입니다"),

    // user
    USER_ID_AVAILABLE(HttpStatus.OK,"USER200", "사용 가능한 id입니다."),
    USER_JOIN_SUCCESS(HttpStatus.OK,"USER201", "회원 가입 성공"),
    USER_LOGIN_SUCCESS(HttpStatus.OK,"USER202", "로그인 성공"),
    USER_MYPAGE_SUCCESS(HttpStatus.OK,"USER203", "마이페이지 조회 성공"),
    USER_LOGOUT_SUCCESS(HttpStatus.OK,"USER204", "로그아웃 성공"),
    USER_LIST_GET_SUCCESS(HttpStatus.OK,"USER205", "유저 목록 조회 성공"),



    BOARD_GET_SUCCESS(HttpStatus.OK,"BOARD200", "게시판 조회 성공"),
    BOARDLIST_GET_SUCCESS(HttpStatus.OK,"BOARD201", "전체 게시판 조회 성공"),


    // INQUIRE
    INQUIRE_LIST_GET_SUCCESS(HttpStatus.OK,"INQUIRE200", "문의 목록 조회 성공"),
    INQUIRE_MESSAGE_GET_SUCCESS(HttpStatus.OK,"INQUIRE201", "문의 메세지 조회 성공"),
    INQUIRE_ROOM_DELETE_SUCCESS(HttpStatus.OK,"INQUIRE202", "문의 종료 성공"),

    // Object Storage(S3 api)
    FILE_UPLOAD_SUCCESS(HttpStatus.OK,"S3200", "파일 업로드 성공"),
    FILE_DELETE_SUCCESS(HttpStatus.OK,"S3201", "파일 삭제 성공"),


    // Favorite
    FAVORITE_QUERY_SUCCESS(HttpStatus.OK, "FAV200", "즐겨찾기 조회 성공"),
    FAVORITE_ADD_SUCCESS(HttpStatus.OK, "FAV201", "즐겨찾기 등록 성공"),
    FAVORITE_DELETE_SUCCESS(HttpStatus.OK, "FAV202", "즐겨찾기 취소 성공")
    ;


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public SuccessDTO getReason() {
        return SuccessDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(true)
                .build();
    }

    @Override
    public SuccessDTO getHttpStatusReason() {
        return SuccessDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(true)
                .httpStatus(httpStatus)
                .build();
    }
}
