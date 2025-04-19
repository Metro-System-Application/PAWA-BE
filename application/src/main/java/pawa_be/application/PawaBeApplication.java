package pawa_be.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
@ComponentScan(basePackages = "pawa_be")
@EnableJpaRepositories(basePackages = {
        "pawa_be.cart.internal.repository",
        "pawa_be.payment.internal.repository",
        "pawa_be.profile.internal.repository",
        "pawa_be.ticket.internal.repository"
})
@EntityScan(basePackages = {
        "pawa_be.cart.internal.model",
        "pawa_be.payment.internal.model",
        "pawa_be.profile.internal.model",
        "pawa_be.ticket.internal.model"
})
public class PawaBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(PawaBeApplication.class, args);
    }

}
