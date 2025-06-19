import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import java.util.Base64;
import java.util.HashMap;

public class CryptoUtils {
    
    private static final String AES_KEY = "1234567890abcdef"; // 16 bytes

    // AES
    public static String encryptAES(String value) {
        try {
            SecretKeySpec key = new SecretKeySpec(AES_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            return value; // fallback se falhar
        }
    }

    public static String decryptAES(String encrypted) {
        try {
            SecretKeySpec key = new SecretKeySpec(AES_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decoded = Base64.getDecoder().decode(encrypted);
            return new String(cipher.doFinal(decoded));
        } catch (Exception e) {
            return encrypted; // fallback se não estiver criptografado
        }
    }

    // XOR simples
    public static String encryptXOR(String input, char key) {
        StringBuilder output = new StringBuilder();
        for (char c : input.toCharArray()) {
            output.append((char) (c ^ key));
        }
        return output.toString();
    }

    public static String decryptXOR(String input, char key) {
        return encryptXOR(input, key); // simétrico
    }

    public static Map<String, Integer> encryptMapAES(Map<String, Integer> map) {
        Map<String, Integer> encrypted = new HashMap<>();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            String encryptedKey = encryptAES(entry.getKey());
            encrypted.put(encryptedKey, entry.getValue());
        }
        return encrypted;
    }

    public static Map<String, Integer> decryptMapAES(Map<String, Integer> map) {
        Map<String, Integer> decrypted = new HashMap<>();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            String decryptedKey = decryptAES(entry.getKey());
            decrypted.put(decryptedKey, entry.getValue());
        }
        return decrypted;
    }

    public static Map<String, Integer> encryptMapXOR(Map<String, Integer> map, char key) {
        Map<String, Integer> encrypted = new HashMap<>();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            String encryptedKey = encryptXOR(entry.getKey(), key);
            encrypted.put(encryptedKey, entry.getValue());
        }
        return encrypted;
    }

    public static Map<String, Integer> decryptMapXOR(Map<String, Integer> map, char key) {
        return encryptMapXOR(map, key); // simétrico
    }
}
