package pawa_be.infrastructure.jwt.key;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;

@Component
public class KeyStoreManager {
    private final KeyStore keyStore;
    private final JwtKeyProperties properties;
    private static SecretKey aesKey;

    public KeyStoreManager(JwtKeyProperties properties) throws Exception {
        this.properties = properties;
        this.keyStore = KeyStore.getInstance("PKCS12");

        try (InputStream inputStream = new ClassPathResource(properties.getKeystorePath()).getInputStream()) {
            keyStore.load(inputStream, properties.getKeystorePassword().toCharArray());
        }

        // Derive AES key from private key
        PrivateKey privateKey = getPrivateKey();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = digest.digest(privateKey.getEncoded());
        aesKey = new SecretKeySpec(keyBytes, 0, 16, "AES");
    }

    public PrivateKey getPrivateKey() throws Exception {
        Key key = keyStore.getKey(properties.getKeyAlias(), properties.getKeyPassword().toCharArray());
        if (key instanceof PrivateKey) {
            return (PrivateKey) key;
        }
        throw new Exception("Key is not a private key.");
    }

    public PublicKey getPublicKey() throws Exception {
        Certificate cert = keyStore.getCertificate(properties.getKeyAlias());
        if (cert != null) {
            return cert.getPublicKey();
        }
        throw new Exception("No certificate found for alias: " + properties.getKeyAlias());
    }

    public static SecretKey getAesKey() {
        return aesKey;
    }
}

