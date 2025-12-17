package rays.techlab.fde.global.support;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/*
 * AESUtil
 *
 * AES 대칭키 암복호화 유틸리티
 */
public class AESUtil {
    private static final String SECRET_KEY = "practiceSecretKey123456789012345";
    private static final String ALGORITHM = "AES";

    // MySQL 기본 모드와 맞추기 위해 ECB/PKCS5Padding 사용
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    /**
     * 암호화 (Plain Text -> Hex String)
     */
    public static String encrypt(String plainText) throws Exception {
        if (plainText == null) return null;

        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);

        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // 바이트 배열 -> 16진수 문자열(Hex) 변환
        return bytesToHex(encryptedBytes);
    }

    /**
     * 복호화 (Hex String -> Plain Text)
     */
    public static String decrypt(String hexCipherText) throws Exception {
        if (hexCipherText == null) return null;

        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);

        // 16진수 문자열 -> 바이트 배열 변환
        byte[] originalBytes = hexToBytes(hexCipherText);

        byte[] decryptedBytes = cipher.doFinal(originalBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
