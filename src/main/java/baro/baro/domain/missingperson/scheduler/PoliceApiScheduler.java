package baro.baro.domain.missingperson.scheduler;

import baro.baro.domain.missingperson.service.PoliceApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// 경찰청 실종자 API 주기적 동기화 스케줄러
@Slf4j
@Component
@RequiredArgsConstructor
public class PoliceApiScheduler {
    
    private final PoliceApiService policeApiService;
    
    // 매일 새벽 3시에 경찰청 실종자 데이터 동기화
    // Cron: 초 분 시 일 월 요일
    @Scheduled(cron = "0 0 3 * * *")
    public void syncMissingPersonsDaily() {
        log.info("경찰청 실종자 데이터 일일 동기화 스케줄 시작");
        try {
            policeApiService.syncMissingPersonsFromPoliceApi();
            log.info("경찰청 실종자 데이터 일일 동기화 스케줄 완료");
        } catch (Exception e) {
            log.error("경찰청 실종자 데이터 일일 동기화 중 오류 발생", e);
        }
    }
    
    // 매주 일요일 오전 2시에 경찰청 실종자 데이터 동기화
    // Cron: 초 분 시 일 월 요일 (0=일요일)
    @Scheduled(cron = "0 0 2 * * 0")
    public void syncMissingPersonsWeekly() {
        log.info("경찰청 실종자 데이터 주간 동기화 스케줄 시작");
        try {
            policeApiService.syncMissingPersonsFromPoliceApi();
            log.info("경찰청 실종자 데이터 주간 동기화 스케줄 완료");
        } catch (Exception e) {
            log.error("경찰청 실종자 데이터 주간 동기화 중 오류 발생", e);
        }
    }
}
