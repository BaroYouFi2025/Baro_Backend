package baro.baro.domain.auth.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.search.FlagTerm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 이메일 수신을 통한 전화번호 인증 처리 서비스
 *
 * SMS 인증 토큰이 이메일로 전달되는 경우를 처리하기 위해
 * IMAP 프로토콜을 사용하여 주기적으로 메일박스를 확인하고
 * 인증 토큰과 전화번호를 추출하여 인증을 수행합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailListener {

    private final PhoneVerificationService phoneVerificationService;
    private volatile boolean isListening = false;
    private volatile long listeningStartTime = 0;

    @Value("${mail.imap.host}")
    private String host;

    @Value("${mail.imap.username}")
    private String username;

    @Value("${mail.imap.password}")
    private String password;

    /**
     * 이메일 리스너 시작
     */
    public void startListening() {
        this.isListening = true;
        this.listeningStartTime = System.currentTimeMillis();
        log.info("이메일 리스너 시작됨");
    }

    /**
     * 이메일 리스너 중지
     */
    public void stopListening() {
        this.isListening = false;
        log.info("이메일 리스너 중지됨");
    }

    /**
     * 메일박스를 주기적으로 확인하여 SMS 인증 토큰을 처리합니다.
     * <p>
     * 15초마다 실행되며, 리스너가 활성화된 경우에만 읽지 않은 메일에서
     * 인증 토큰과 전화번호를 추출하여 전화번호 인증을 수행합니다.
     */
    @Scheduled(fixedDelay = 15000)
    @Async
    public void checkMailbox() {
        if (!isListening) {
            return;
        }

        // 10분(600초) 경과시 자동 중지
        if (System.currentTimeMillis() - listeningStartTime > 600000) {
            stopListening();
            return;
        }
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");
        props.setProperty("mail.imaps.connectionpoolsize", "10");
        props.setProperty("mail.imaps.connectionpooltimeout", "30000");

        Store store = null;
        Folder inbox = null;

        try {
            Session session = Session.getInstance(props, null);
            store = session.getStore();

            // 연결 재시도 로직
            int retryCount = 0;
            while (retryCount < 3) {
                try {
                    store.connect(host, username, password);
                    break;
                } catch (Exception e) {
                    retryCount++;
                    if (retryCount >= 3) {
                        throw new RuntimeException("IMAP 연결 실패 (3회 재시도)", e);
                    }
                    Thread.sleep(1000); // 1초 대기
                }
            }

            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            // 검색 조건 : 읽지 않은 메일만 검색
            FlagTerm unreadFlag = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
            Message[] messages = inbox.search(unreadFlag);

            if (messages.length == 0) {
                inbox.close(false);
                store.close();
                return;
            }

            List<Message> processedMessages = new ArrayList<>();

            for (Message message : messages) {
                try {
                    String from = ((InternetAddress) message.getFrom()[0]).getAddress();
                    String body = getTextFromMessage(message);

                    String token = extractToken(body);
                    String phoneNumber = extractPhoneNumber(from);

                    if (token != null && phoneNumber != null) {
                        phoneVerificationService.verifyToken(token, phoneNumber);
                        processedMessages.add(message);
                    }
                } catch (Exception e) {
                    log.warn("메시지 처리 중 오류: {}", e.getMessage());
                }
            }

            // 배치로 읽음 처리
            for (Message message : processedMessages) {
                message.setFlag(Flags.Flag.SEEN, true);
            }

        } catch (Exception e) {
            log.error("메일확인이 실패했습니다.", e);
        } finally {
            // 안전한 연결 종료
            try {
                if (inbox != null && inbox.isOpen()) {
                    inbox.close(false);
                }
                if (store != null && store.isConnected()) {
                    store.close();
                }
            } catch (Exception e) {
                log.warn("메일 연결 종료 중 오류: {}", e.getMessage());
            }
        }
    }

    /**
     * 메시지에서 텍스트 내용을 추출합니다.
     *
     * @param message 추출할 메시지
     * @return 메시지의 텍스트 내용
     * @throws Exception 메시지 읽기 중 발생하는 예외
     */
    private String getTextFromMessage(Message message) throws Exception {
        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        } else if (message.isMimeType("text/html")) {
            return message.getContent().toString();
        } else if (message.getContent() instanceof Multipart) {
            return message.getContent().toString();
        }
        return "";
    }

    /**
     * 메시지 본문에서 6자리 숫자 인증 토큰을 추출합니다.
     *
     * @param body 메시지 본문
     * @return 추출된 6자리 토큰, 없으면 null
     */
    private String extractToken(String body) {
        return body.replaceAll("[^0-9]", "").length() >= 6
                ? body.replaceAll("[^0-9]", "").substring(0, 6)
                : null;
    }

    /**
     * 발신자 이메일 주소에서 전화번호를 추출합니다.
     *
     * @param from 발신자 이메일 주소 (예: "01012345678@sktmail.net")
     * @return 추출된 전화번호 (예: "01012345678"), 없으면 null
     */
    private String extractPhoneNumber(String from) {
        if (from.contains("@")) {
            return from.split("@")[0];
        }
        return null;
    }
}