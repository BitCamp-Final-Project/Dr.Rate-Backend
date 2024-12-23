package com.bitcamp.drrate.domain.visitor.service;

import com.bitcamp.drrate.domain.visitor.entity.DailyVisitor;
import com.bitcamp.drrate.domain.visitor.repository.DailyVisitorRepository;
import com.bitcamp.drrate.global.code.resultCode.ErrorStatus;
import com.bitcamp.drrate.global.exception.exceptionhandler.VisitServiceExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class VisitorSchedulerServiceImpl implements VisitorSchedulerService {

    private final RedisTemplate<String, String> redisTemplate;
    private final DailyVisitorRepository dailyVisitorRepository;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정 실행
    @Transactional
    @Override
    public void transferDailyVisitorsToDB() {
        String today = LocalDate.now().toString();

        // Redis 키 설정
        String memberKey = "daily_visitors:member:" + today;
        String guestKey = "daily_visitors:guest:" + today;
        String newMembersKey = "daily_new_members:" + today;

        try {
            // Redis에서 방문자 수 가져오기
            Long memberCount = getRedisSetSize(memberKey);
            Long guestCount = getRedisSetSize(guestKey);
            Long totalCount = memberCount + guestCount;

            // Redis에서 신규 가입자 수 가져오기
            Integer newMembersCount = getRedisValueAsInteger(newMembersKey);



            // MySQL에 데이터 저장
            dailyVisitorRepository.save(new DailyVisitor(
                    LocalDate.now(),
                    memberCount != null ? memberCount.intValue() : 0,
                    guestCount != null ? guestCount.intValue() : 0,
                    totalCount != null ? totalCount.intValue() : 0,
                    newMembersCount != null ? newMembersCount : 0
            ));

            // Redis 데이터 삭제
            redisTemplate.delete(memberKey);
            redisTemplate.delete(guestKey);
            redisTemplate.delete(newMembersKey);

        } catch (RedisConnectionFailureException e) {
            throw new VisitServiceExceptionHandler(ErrorStatus.REDIS_LOAD_FAILED);
        }  catch (Exception e) {
            throw new VisitServiceExceptionHandler(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Redis Set의 크기 가져오기
    private Long getRedisSetSize(String key) {
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (RedisConnectionFailureException e) {
            throw new VisitServiceExceptionHandler(ErrorStatus.REDIS_LOAD_FAILED);
        }
    }

    // Redis String 값을 Integer로 가져오기
    private Integer getRedisValueAsInteger(String key) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            return (value != null) ? Integer.parseInt(value) : 0;
        } catch (NumberFormatException e) {
            return 0; // 잘못된 값은 0으로 처리
        } catch (RedisConnectionFailureException e) {
            throw new VisitServiceExceptionHandler(ErrorStatus.REDIS_LOAD_FAILED);
        }
    }
}
