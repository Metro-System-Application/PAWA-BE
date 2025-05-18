package pawa_be.infrastructure.jwt.key;

import com.google.gson.Gson;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.*;
import java.security.cert.Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

class JWTPublicKeyDTO {
    public String algorithm;
    public String format;
    public String encoded;
}

@Component
public class KeyStoreManager {
    private final KeyStore keyStore;
    private final JwtKeyProperties properties;
    private static SecretKey aesKey;
    private static PublicKey opwaPublicKey;

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

        fetchOpwaPublicKey();
    }

    private void fetchOpwaPublicKey() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8081/api/auth/jwtCert"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Gson gson = new Gson();
                JWTPublicKeyDTO json = gson.fromJson(response.body(), JWTPublicKeyDTO.class);

                byte[] decodedBytes = Base64.getDecoder().decode(json.encoded);
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                opwaPublicKey = keyFactory.generatePublic(keySpec);

                System.out.println("Public key loaded successfully!");
            } else {
                throw new RuntimeException("Failed to fetch key. HTTP status: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching opwa public key", e);
        }
    }

    public static PublicKey getOpwaPublicKey() {
        return opwaPublicKey;
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

