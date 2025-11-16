package baro.baro.domain.common.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

// Spring ApplicationContext에 접근하기 위한 유틸리티 클래스
// Static 메서드에서 Bean을 가져올 수 있도록 지원
@Component
public class ApplicationContextProvider implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    // ApplicationContext를 반환합니다.
    //
    // @return ApplicationContext
    public static ApplicationContext getApplicationContext() {
        return context;
    }

    // Bean을 이름으로 가져옵니다.
    //
    // @param beanName Bean 이름
    // @return Bean 객체
    public static Object getBean(String beanName) {
        return context.getBean(beanName);
    }

    // Bean을 타입으로 가져옵니다.
    //
    // @param beanClass Bean 클래스 타입
    // @param <T> Bean 타입
    // @return Bean 객체
    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }
}