package com.bitcamp.drrate.domain.visitor.service;

import com.bitcamp.drrate.domain.users.dto.CustomUserDetails;
import com.bitcamp.drrate.domain.users.service.UsersService;
import com.bitcamp.drrate.domain.visitor.entity.DailyVisitor;
import com.bitcamp.drrate.domain.visitor.repository.DailyVisitorRepository;
import com.bitcamp.drrate.global.code.resultCode.ErrorStatus;
import com.bitcamp.drrate.global.exception.exceptionhandler.VisitServiceExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VisitorServiceImpl implements VisitorService {

    private final RedisTemplate<String, String> redisTemplate;
    private final UsersService usersService;
    private final DailyVisitorRepository dailyVisitorRepository;

    @Override
    @Transactional
    public void trackVisit(CustomUserDetails userDetails, String guestId) {
        String today = LocalDate.now().toString();
        String redisKey;

        try {
            if (userDetails != null) { // 회원 방문자 처리
                Long userId = usersService.getUserId(userDetails); // userId 검증 수행
                redisKey = "daily_visitors:member:" + today;

                String redisKey2 = "daily_visitors:guest:" + today;
                Boolean isGuestLoggedIn = false;
                if(guestId != null) {
                    isGuestLoggedIn = redisTemplate.opsForSet().isMember(redisKey2, guestId);
                }

                if (Boolean.FALSE.equals(isGuestLoggedIn)) {
                    Boolean isMemberVisited = redisTemplate.opsForSet().isMember(redisKey, String.valueOf(userId));
                    if (Boolean.FALSE.equals(isMemberVisited)) {
                        redisTemplate.opsForSet().add(redisKey, String.valueOf(userId));
                    }
                }
                redisTemplate.expire(redisKey, Duration.ofDays(1));

            } else if (guestId != null && !guestId.isEmpty()) { // 비회원 방문자 처리
                redisKey = "daily_visitors:guest:" + today;

                Boolean isGuestLoggedIn = redisTemplate.opsForSet().isMember(redisKey, guestId);
                if (Boolean.FALSE.equals(isGuestLoggedIn)) {
                    redisTemplate.opsForSet().add(redisKey, guestId);
                }
                redisTemplate.expire(redisKey, Duration.ofDays(1));
            } else {
                throw new VisitServiceExceptionHandler(ErrorStatus.VISIT_RECORD_FAILED);
            }
        } catch (RedisConnectionFailureException e) {
            e.printStackTrace();
            throw new VisitServiceExceptionHandler(ErrorStatus.REDIS_LOAD_FAILED);
        } catch (DataAccessException e) {
            e.printStackTrace();
            throw new VisitServiceExceptionHandler(ErrorStatus.REDIS_SAVE_FAILED);
        } catch (Exception e) {
            e.printStackTrace();
            throw new VisitServiceExceptionHandler(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    public Long getTodayMembersCount() {
        try {
            String today = LocalDate.now().toString();
            String redisKey = "daily_visitors:member:" + today;

            Long size = redisTemplate.opsForSet().size(redisKey);
            return size != null ? size: 0L;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new VisitServiceExceptionHandler(ErrorStatus.REDIS_LOAD_FAILED);
        } catch (RedisConnectionFailureException e) {
            e.printStackTrace();
            throw new VisitServiceExceptionHandler(ErrorStatus.REDIS_LOAD_FAILED);
        } catch (Exception e) {
            e.printStackTrace();
            throw new VisitServiceExceptionHandler(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Long getTodayGuestCount() {
        try {
            String today = LocalDate.now().toString();
            String redisKey = "daily_visitors:guest:" + today;

            Long size = redisTemplate.opsForSet().size(redisKey);
            return size != null ? size: 0L;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new VisitServiceExceptionHandler(ErrorStatus.REDIS_LOAD_FAILED);
        } catch (RedisConnectionFailureException e) {
            e.printStackTrace();
            throw new VisitServiceExceptionHandler(ErrorStatus.REDIS_LOAD_FAILED);
        } catch (Exception e) {
            e.printStackTrace();
            throw new VisitServiceExceptionHandler(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    public Long getTodayNewMembersCount() {
        try {
            String today = LocalDate.now().toString();
            String redisKey = "daily_new_members:" + today;

            Long size = redisTemplate.opsForSet().size(redisKey);
            return size != null ? size : 0L;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new VisitServiceExceptionHandler(ErrorStatus.REDIS_LOAD_FAILED);
        } catch (RedisConnectionFailureException e) {
            e.printStackTrace();
            throw new VisitServiceExceptionHandler(ErrorStatus.REDIS_LOAD_FAILED);
        } catch (Exception e) {
            e.printStackTrace();
            throw new VisitServiceExceptionHandler(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 지난 4일 데이터 불러옴
    @Override
    public List<DailyVisitor> getLast4DaysVisitorCounts() {
        try {
            LocalDate yesterday = LocalDate.now();
            return dailyVisitorRepository.findLast4DaysVisitors(yesterday);
        } catch (DataAccessException e) {
            e.printStackTrace();
            throw new VisitServiceExceptionHandler(ErrorStatus.MYSQL_LOAD_FAILED);
        } catch (Exception e) {
            e.printStackTrace();
            throw new VisitServiceExceptionHandler(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 일주일 간 데이터 불러옴


    // 일주일 간 회원 방문
    @Override
    public int getLast7DaysMemberVisitorCount() {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(7);
        return dailyVisitorRepository.findTotalMemberVisitorsBetweenDates(sevenDaysAgo, today);
    }

    // 일주일 간 비회원 방문
    @Override
    public int getLast7DaysGuestVisitorCount() {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(7);
        return dailyVisitorRepository.findTotalGuestVisitorsBetweenDates(sevenDaysAgo, today);
    }

    // 일주일 간 총 방문자 수
    @Override
    public int getLast7DaysTotalVisitorCount() {
        try {
            LocalDate today = LocalDate.now();
            LocalDate sevenDaysAgo = today.minusDays(7);
            Integer total = dailyVisitorRepository.findTotalVisitorsBetweenDates(sevenDaysAgo, today);
            return total != null ? total : 0;
        } catch (DataAccessException e) {
            e.printStackTrace();
            throw new VisitServiceExceptionHandler(ErrorStatus.MYSQL_LOAD_FAILED);
        } catch (Exception e) {
            e.printStackTrace();
            throw new VisitServiceExceptionHandler(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // 일주일 간 회원 가입
    @Override
    public int getLast7DaysNewMembersCount() {
        try {
            LocalDate today = LocalDate.now();
            LocalDate sevenDaysAgo = today.minusDays(7);
            Integer total = dailyVisitorRepository.findTotalNewMembersBetweenDates(sevenDaysAgo, today);
            return total != null ? total : 0;
        } catch (Exception e) {
            e.printStackTrace();
            throw new VisitServiceExceptionHandler(ErrorStatus.MYSQL_LOAD_FAILED);
        }
    }

    // 한달 간 데이터 불러옴


    // 한달 간 회원 방문 수
    @Override
    public int getThisMonthTotalMemberCount() {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        return dailyVisitorRepository.findTotalMemberVisitorsBetweenDates(startOfMonth, today);
    }

    // 한달 간 비회원 방문 수
    @Override
    public int getThisMonthGuestVisitorCount() {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        return dailyVisitorRepository.findTotalGuestVisitorsBetweenDates(startOfMonth, today);
    }

    // 한달 간 총 방문 수
    @Override
    public int getThisMonthVisitorTotalCount() {
        try {
            LocalDate today = LocalDate.now();
            LocalDate startOfMonth = today.withDayOfMonth(1);
            Integer total = dailyVisitorRepository.findTotalVisitorsBetweenDates(startOfMonth, today);
            return total != null ? total : 0;
        } catch (DataAccessException e) {
            e.printStackTrace();
            throw new VisitServiceExceptionHandler(ErrorStatus.MYSQL_LOAD_FAILED);
        } catch (Exception e) {
            e.printStackTrace();
            throw new VisitServiceExceptionHandler(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 한달 간 회원 가입 수
    @Override
    public int getThisMonthNewMembersCount() {
        try {
            LocalDate today = LocalDate.now();
            LocalDate startOfMonth = today.withDayOfMonth(1);
            Integer total = dailyVisitorRepository.findTotalNewMembersBetweenDates(startOfMonth, today);
            return total != null ? total : 0;
        } catch (Exception e) {
            e.printStackTrace();
            throw new VisitServiceExceptionHandler(ErrorStatus.MYSQL_LOAD_FAILED);
        }
    }

}
