package baro.baro.domain.member.listener;

import baro.baro.domain.member.dto.event.MemberLocationChangedEvent;
import baro.baro.domain.member.dto.event.MemberLocationEvent;
import baro.baro.domain.member.dto.response.MemberLocationResponse;
import baro.baro.domain.member.repository.RelationshipRepository;
import baro.baro.domain.member.service.MemberLocationEmitterRegistry;
import baro.baro.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

// 구성원 위치 변경 이벤트 리스너
// 위치 변경 시 관련 사용자들에게 SSE 이벤트를 브로드캐스트합니다.
@Slf4j
@Component
@RequiredArgsConstructor
public class MemberLocationEventListener {

    private final MemberLocationEmitterRegistry emitterRegistry;
    private final RelationshipRepository relationshipRepository;
    private final MemberService memberService;

    // 위치 변경 이벤트를 처리합니다.
    // 비동기로 실행되어 메인 요청 처리를 블로킹하지 않습니다.
    //
    // @param event 위치 변경 이벤트
    @Async
    @EventListener
    public void handleLocationChanged(MemberLocationChangedEvent event) {
        Long changedUserId = event.getUserId();
        log.debug("위치 변경 이벤트 수신 - userId: {}", changedUserId);

        // 변경된 사용자와 관계를 맺은 모든 사용자 조회
        List<Long> relatedUserIds = relationshipRepository.findUserIdsByMemberId(changedUserId);

        if (relatedUserIds.isEmpty()) {
            log.debug("관련 사용자 없음 - userId: {}", changedUserId);
            return;
        }

        // 각 관련 사용자에게 업데이트된 위치 정보 브로드캐스트
        for (Long userId : relatedUserIds) {
            // 연결이 없으면 스킵
            if (!emitterRegistry.hasConnection(userId)) {
                continue;
            }

            try {
                // 해당 사용자 관점의 구성원 위치 목록 조회
                List<MemberLocationResponse> locations = memberService.getMemberLocationsForUser(userId);

                // UPDATE 이벤트 생성 및 브로드캐스트
                MemberLocationEvent locationEvent = MemberLocationEvent.update(locations);
                emitterRegistry.broadcast(userId, locationEvent);

                log.debug("위치 업데이트 브로드캐스트 - userId: {}, 구성원 수: {}", userId, locations.size());
            } catch (Exception e) {
                log.error("위치 브로드캐스트 실패 - userId: {}", userId, e);
            }
        }
    }
}
