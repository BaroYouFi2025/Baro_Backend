package baro.baro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BaroApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaroApplication.class, args);
    }

}
