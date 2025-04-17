package pawa_be.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "pawa_be")
@EnableJpaRepositories(basePackages = {"pawa_be.user_auth.internal.repository"})
@EntityScan(basePackages = {"pawa_be.user_auth.internal.model"})
public class PawaBeApplication {
    public static void main(String[] args) {
        SpringApplication.run(PawaBeApplication.class, args);
    }
}
