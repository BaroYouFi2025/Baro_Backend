package baro.baro.domain.auth.service;

import baro.baro.domain.auth.exception.EmailErrorCode;
import baro.baro.domain.auth.exception.EmailException;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import java.util.Objects;
import java.util.regex.Pattern;

public final class MailOtpExtractor {

    private MailOtpExtractor() {}

    // 6자리 코드 (연속 숫자) / 공백 섞인 6자리 "2 7 1 6 2 9" 도 허용
    private static final Pattern OTP6          = Pattern.compile("(?<!\\d)(\\d{6})(?!\\d)");
    private static final Pattern OTP6_SPACED   = Pattern.compile("(\\d\\s*\\d\\s*\\d\\s*\\d\\s*\\d\\s*\\d)");
    // 한국 휴대폰 11자리(010/011/016/017/018/019) — 발신자 로컬파트에서만 추출
    private static final Pattern PHONE_11      = Pattern.compile("(?<!\\d)(01[0-9]\\d{8})(?!\\d)");

    public record Result(String phoneNumber, String code) {}

    /** 메시지에서 전화번호(From)와 6자리 코드만 반환 */
    public static Result extract(Message msg) throws EmailException {
        try {
            // 1) 전화번호: From의 로컬파트(예: 01084461297@mmsmail.uplus.co.kr → 01084461297)
            String from = firstAddress(msg.getFrom());
            String localPart = (from != null && from.contains("@")) ? from.substring(0, from.indexOf('@')) : "";
            var mPhone = PHONE_11.matcher(localPart);
            if (!mPhone.find()) {
                throw new EmailException(EmailErrorCode.PHONE_NUMBER_EXTRACTION_FAILED);
            }
            String phone = mPhone.group(1);

            // 2) 인증코드: 제목 + 본문 합쳐서 검색
            String subject = Objects.toString(msg.getSubject(), "");
            String body    = text(msg);  // 간단 텍스트 추출
            String joined  = subject + " " + body;

            var m = OTP6.matcher(joined);
            String code;
            if (m.find()) {
                code = m.group(1);
            } else {
                m = OTP6_SPACED.matcher(joined);                 // "2 7 1 6 2 9" 형태
                if (m.find()) code = m.group(1).replaceAll("\\s+", "");
                else {
                    // 최후 보정: 숫자만 모아 앞 6자리
                    String digits = joined.replaceAll("[^0-9]", "");
                    if (digits.length() >= 6) code = digits.substring(0, 6);
                    else throw new EmailException(EmailErrorCode.TOKEN_EXTRACTION_FAILED);
                }
            }
            return new Result(phone, code);

        } catch (MessagingException e) {
            throw new EmailException(EmailErrorCode.MAIL_PARSING_FAILED);
        }
    }

    // 가장 간단한 본문 추출: text/plain 우선, 없으면 text/html 태그 제거, 멀티파트는 처음 나오는 텍스트 파트 사용
    private static String text(Part part) {
        try {
            if (part.isMimeType("text/plain")) {
                Object c = part.getContent();
                return (c instanceof String s) ? s : String.valueOf(c);
            }
            if (part.isMimeType("text/html")) {
                Object c = part.getContent();
                String html = (c instanceof String s) ? s : String.valueOf(c);
                return html.replaceAll("(?is)<(script|style)[^>]*>.*?</\\1>", "")
                        .replaceAll("(?is)<br\\s*/?>", "\n")
                        .replaceAll("(?is)<[^>]+>", " ")
                        .replace("&nbsp;", " ")
                        .replaceAll("\\s+", " ")
                        .trim();
            }
            if (part.isMimeType("multipart/*")) {
                Multipart mp = (Multipart) part.getContent();
                for (int i = 0; i < mp.getCount(); i++) {
                    String t = text(mp.getBodyPart(i));
                    if (t != null && !t.isBlank()) return t;
                }
            }
            if (part.isMimeType("message/rfc822")) {
                return text((Part) part.getContent());
            }
        } catch (Exception ignore) {}
        return "";
    }

    private static String firstAddress(Address[] addrs) {
        if (addrs == null || addrs.length == 0) return null;
        Address a = addrs[0];
        return (a instanceof InternetAddress ia) ? ia.getAddress() : a.toString();
    }
}