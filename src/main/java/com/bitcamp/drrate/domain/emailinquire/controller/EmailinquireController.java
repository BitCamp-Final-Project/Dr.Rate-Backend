package com.bitcamp.drrate.domain.emailinquire.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bitcamp.drrate.domain.emailinquire.entity.Emailinquire;
import com.bitcamp.drrate.domain.emailinquire.service.EmailinquireService;
import com.bitcamp.drrate.domain.users.dto.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/emailinquire")
@RequiredArgsConstructor
public class EmailinquireController {
    private final EmailinquireService emailinquireService;
    
    // 이메일 문의 저장
    @RequestMapping(value="/save", method=RequestMethod.POST, consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> saveEmailInquire(@RequestParam("inquireCtg") String inquireCtg,
                                                @RequestParam("inquireUser") String inquireUser,
                                                @RequestParam("inquireEmail") String inquireEmail,
                                                @RequestParam("inquireTitle") String inquireTitle,
                                                @RequestParam("inquireContent") String inquireContent,
                                                @RequestParam(value="fileUuid", required=false) MultipartFile fileUuid,
                                                @AuthenticationPrincipal CustomUserDetails userDetails) throws Exception {

        Emailinquire emailInquire = new Emailinquire();

        emailInquire.setInquireCtg(inquireCtg);
        emailInquire.setInquireUser(inquireUser);
        emailInquire.setInquireEmail(inquireEmail);
        emailInquire.setInquireTitle(inquireTitle);
        emailInquire.setInquireContent(inquireContent);
        emailInquire.setInquireId(userDetails.getId());
        
        emailinquireService.saveEmailInquire(emailInquire, fileUuid);
        System.out.println("이메일 저장 메서드 성공");
        return ResponseEntity.ok(emailInquire);
    }

    // 이메일 문의 내역 조회 (사용자 ID별)
    @RequestMapping(value="/myinquired", method=RequestMethod.GET)
    public ResponseEntity<List<Emailinquire>> getEmailInquiresByUserId(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long id = userDetails.getId(); // 사용자 users table의 pk값

        List<Emailinquire> emailInquires = emailinquireService.getEmailInquiresByUserId(id);
        return ResponseEntity.ok(emailInquires);
    }

    // 이메일 문의 삭제
    @RequestMapping(value="/delete", method=RequestMethod.GET)
    public ResponseEntity<Void> deleteEmailInquire(@PathVariable Long id) {
        emailinquireService.deleteEmailInquire(id);
        return ResponseEntity.noContent().build();
    }
}