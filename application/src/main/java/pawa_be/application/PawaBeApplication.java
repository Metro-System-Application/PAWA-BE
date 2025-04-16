package pawa_be.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "pawa_be")
public class PawaBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(PawaBeApplication.class, args);
    }

}
