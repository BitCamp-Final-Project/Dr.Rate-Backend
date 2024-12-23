package com.bitcamp.drrate.domain.emailinquire.service;

import com.bitcamp.drrate.domain.emailinquire.entity.Emailinquire;
import com.bitcamp.drrate.domain.emailinquire.repository.EmailinquireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailinquireService {

    private final EmailinquireRepository emailinquireRepository;

    // 문의 저장
    public Emailinquire saveEmailInquire(Emailinquire emailInquire) {
        return emailinquireRepository.save(emailInquire);
    }

    // 특정 사용자 ID로 문의 내역 조회
    public List<Emailinquire> getEmailInquiresByUserId(Long userId) {
        return emailinquireRepository.findByInquireId(userId);
    }

    // 문의 삭제
    public void deleteEmailInquire(Long id) {
        emailinquireRepository.deleteById(id);
    }
}