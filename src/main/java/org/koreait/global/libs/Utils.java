package org.koreait.global.libs;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class Utils {

    private final HttpServletRequest request;
    private final MessageSource messageSource;

    public boolean isMobile() {

        // 요청 헤더 - User-Agent / 브라우저 정보
        String ua = request.getHeader("User-Agent");
        String pattern = ".*(iPhone|iPod|iPad|BlackBerry|Android|Windows CE|LG|MOT|SAMSUNG|SonyEricsson).*";


        return ua.matches(pattern);
    }

    /**
     * mobile, front 템플릿 분리 함수
     *
     * @param path
     * @return
     */
    public String tpl(String path) {
        String prefix = isMobile() ? "mobile" : "front";

        return String.format("%s/%s", prefix, path);
    }

    /**
     * 메서지 코드로 조회된 문구
     *
     * @param code
     * @return
     */
    public String getMessage(String code) {
        Locale lo = request.getLocale(); // 사용자 요청 헤더(Accept-Language)

        return messageSource.getMessage(code, null, lo);
    }

    public List<String> getMessages(String[] codes) {
//        return Arrays.stream(codes).map(this::getMessage).toList();
//        "No message found under code 'NotBlank.requestLogin.password' for locale 'ko_KR'.",
        return Arrays.stream(codes).map(c -> {
            try {
                return getMessage(c);
            } catch (Exception e) {
                return "";
            }
        }).filter(s -> !s.isBlank()).toList();
    }

    /*
    * REST 커맨드 객체 검증 실패시에 에러 코드를 가지고 메세지 추출
    * */
    public Map<String, List<String>> getErrorMessages(Errors errors) {
        ResourceBundleMessageSource ms = (ResourceBundleMessageSource) messageSource;
        ms.setUseCodeAsDefaultMessage(false);
        try {
            //        필드별 에러코드 - getFieldErrors() 커맨드 객체 검증 실패, rejectValue()
            Map<String, List<String>> messages = errors.getFieldErrors()
                    .stream()
                    .collect(Collectors.toMap(FieldError::getField, f -> getMessages(f.getCodes()), (v1/*기존*/, v2/*현재*/) -> v2));
//        글로벌 에러코드 - getGlobalErrors() reject
            List<String> gMessages = errors.getGlobalErrors()
                    .stream()
                    .flatMap(o -> getMessages(o.getCodes()).stream())
                    .toList();
//        글로벌 에러코드 필드 - global
            if(!gMessages.isEmpty()){
                messages.put("global", gMessages);
            }

            return messages;
        } finally {
            ms.setUseCodeAsDefaultMessage(true);
        }

    }
}
