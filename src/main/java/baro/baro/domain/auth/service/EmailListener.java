package baro.baro.domain.auth.service;

import baro.baro.domain.auth.exception.EmailErrorCode;
import baro.baro.domain.auth.exception.EmailException;
import jakarta.mail.*;
import jakarta.mail.search.FlagTerm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 이메일 수신을 통한 전화번호 인증 처리 서비스
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
    @Scheduled(fixedDelay = 15000, initialDelay = 3000)
    public void checkMailbox() {
        if (!isListening) {
            return;
        }

        // 2분(120초) 경과시 자동 중지
        long elapsedTime = System.currentTimeMillis() - listeningStartTime;

        if (elapsedTime > 120000) {
            log.info("2분 타임아웃으로 인한 이메일 리스너 자동 중지");
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

            while (true) {
                try {
                    store.connect(host, username, password);
                    break;
                } catch (AuthenticationFailedException e) {
                    log.error("이메일 인증 실패: {}", e.getMessage());
                    throw new EmailException(EmailErrorCode.CONNECTION_FAILED);
                }
                catch (Exception e) {
                    retryCount++;
                    if (retryCount >= 3) {
                        log.error("IMAP 연결 실패 ({}회 재시도)", retryCount, e);
                        throw new EmailException(EmailErrorCode.CONNECTION_RETRY_EXCEEDED);
                    }
                    try {
                        Thread.sleep(1000); // 1초 대기
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new EmailException(EmailErrorCode.CONNECTION_FAILED);
                    }
                }
            }

            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            // 검색 조건 : 읽지 않은 메일만 검색
            FlagTerm unreadFlag = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
            Message[] messages = inbox.search(unreadFlag);
            log.info("읽지 않은 메시지 {} 개 발견", messages.length);

            if (messages.length == 0) {
                inbox.close(false);
                store.close();
                return;
            }

            List<Message> processedMessages = new ArrayList<>();

            for (int i = 0; i < messages.length; i++) {
                Message message = messages[i];
                try {
                    MailOtpExtractor.Result result = MailOtpExtractor.extract(message);

                    // 토큰과 전화번호 추출 및 인증 수행
                    String token = result.code();
                    String phoneNumber = result.phoneNumber();

                    phoneVerificationService.authenticateWithToken(token, phoneNumber);
                    processedMessages.add(message);
                    log.info("메시지 {} 토큰 인증 성공 - 전화번호: {}, 토큰: {}", i + 1, phoneNumber, token);

                } catch (EmailException e) {
                    log.error("메시지 {} 이메일 처리 중 EmailException 발생: {}", i + 1, e.getMessage());
                } catch (Exception e) {
                    log.error("메시지 {} 처리 중 예상치 못한 오류: {}", i + 1, e.getMessage(), e);
                }
            }

            // 배치로 읽음 처리
            for (Message message : processedMessages) {
                message.setFlag(Flags.Flag.SEEN, true);
            }
            log.info("메시지 처리 완료 - 총 {} 개 처리됨", processedMessages.size());

        } catch (EmailException e) {
            log.error("이메일 처리 중 EmailException 오류: {} (ErrorCode: {})", e.getMessage(), e.getEmailErrorCode());
        } catch (MessagingException e) {
            log.error("메일 서버 연결 또는 메시지 처리 중 MessagingException 오류: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("메일확인 중 예상치 못한 오류가 발생했습니다.", e);
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


}