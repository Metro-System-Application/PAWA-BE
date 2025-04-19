package pawa_be.infrastructure.jwt.key;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
public class JwtKeyProperties {
    @Value("${jwt.keystore.path}")
    private String keystorePath;
    @Value("${jwt.keystore.password}")
    private String keystorePassword;
    @Value("${jwt.key.password}")
    private String keyPassword;
    @Value("${jwt.key.alias}")
    private String keyAlias;
}
