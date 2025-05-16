package pawa_be.infrastructure.common.validation;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import pawa_be.infrastructure.jwt.key.KeyStoreManager;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.util.Base64;

@Converter
public class StripeIdEncryptor implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES";

    @Override
    public String convertToDatabaseColumn(String attribute) {
        try {
            if (attribute == null) return null;
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKey key = KeyStoreManager.getAesKey();
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return Base64.getEncoder().encodeToString(cipher.doFinal(attribute.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null) return null;
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKey key = KeyStoreManager.getAesKey();
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(Base64.getDecoder().decode(dbData)));
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
