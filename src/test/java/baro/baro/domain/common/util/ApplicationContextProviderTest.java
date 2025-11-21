package baro.baro.domain.common.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApplicationContextProviderTest {

    @AfterEach
    void tearDown() throws Exception {
        resetApplicationContext();
    }

    @Test
    void getApplicationContext_returnsContextAssignedBySpring() {
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        new ApplicationContextProvider().setApplicationContext(applicationContext);

        assertThat(ApplicationContextProvider.getApplicationContext()).isSameAs(applicationContext);
    }

    @Test
    void getBean_byName_returnsBeanFromContext() {
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        new ApplicationContextProvider().setApplicationContext(applicationContext);
        when(applicationContext.getBean("testBean")).thenReturn("bean-instance");

        Object result = ApplicationContextProvider.getBean("testBean");

        assertThat(result).isEqualTo("bean-instance");
        verify(applicationContext).getBean("testBean");
    }

    @Test
    void getBean_byType_returnsBeanFromContext() {
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        new ApplicationContextProvider().setApplicationContext(applicationContext);
        when(applicationContext.getBean(String.class)).thenReturn("bean-instance");

        String result = ApplicationContextProvider.getBean(String.class);

        assertThat(result).isEqualTo("bean-instance");
        verify(applicationContext).getBean(String.class);
    }

    private void resetApplicationContext() throws Exception {
        Field field = ApplicationContextProvider.class.getDeclaredField("context");
        field.setAccessible(true);
        field.set(null, null);
    }
}
