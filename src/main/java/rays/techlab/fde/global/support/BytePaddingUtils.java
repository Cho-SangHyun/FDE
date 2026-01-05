package rays.techlab.fde.global.support;

import java.io.UnsupportedEncodingException;

public class BytePaddingUtils {
    public static String formatByByte(String value, int targetByteLength, String encoding) {
        String defaultPaddingChar = " ";
        return formatByByte(value, targetByteLength, encoding, defaultPaddingChar);
    }

    public static String formatByByte(String value, int targetByteLength, String encoding, String paddingChar) {
        if (value == null) {
            value = "";
        }

        try {
            byte[] valueBytes = value.getBytes(encoding);
            int currentLength = valueBytes.length;

            if (currentLength == targetByteLength) {
                return value;
            }

            // 목표 길이보다 길 경우 자르기 (한글 바이트 깨짐 주의)
            if (currentLength > targetByteLength) {
                return new String(valueBytes, 0, targetByteLength, encoding);
            }

            // 부족한 만큼 공백 채우기
            StringBuilder sb = new StringBuilder();
            int paddingSize = targetByteLength - currentLength;
            for (int i = 0; i < paddingSize; i++) {
                sb.append(paddingChar);
            }

            if ("0".equals(paddingChar)) {
                sb.append(value);
            } else {
                sb.insert(0, value);
            }

            return sb.toString();

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported encoding: " + encoding, e);
        }
    }
}
