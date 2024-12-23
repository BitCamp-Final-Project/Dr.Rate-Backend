package com.bitcamp.drrate.domain.emailinquire.controller;

import com.bitcamp.drrate.domain.emailinquire.entity.Emailinquire;
import com.bitcamp.drrate.domain.emailinquire.service.EmailinquireService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emailinquire")
@RequiredArgsConstructor
public class EmailinquireController {

    private final EmailinquireService emailinquireService;

    // 이메일 문의 저장
    @PostMapping
    public ResponseEntity<Emailinquire> saveEmailInquire(@RequestBody Emailinquire emailInquire) {
        Emailinquire saved = emailinquireService.saveEmailInquire(emailInquire);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // 이메일 문의 내역 조회 (사용자 ID별)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Emailinquire>> getEmailInquiresByUserId(@PathVariable("userId") Long userId) {
        List<Emailinquire> emailInquires = emailinquireService.getEmailInquiresByUserId(userId);
        return ResponseEntity.ok(emailInquires);
    }

    // 이메일 문의 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmailInquire(@PathVariable Long id) {
        emailinquireService.deleteEmailInquire(id);
        return ResponseEntity.noContent().build();
    }
}