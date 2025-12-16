package rays.techlab.fde.global.support;

import org.springframework.batch.item.file.transform.AbstractLineTokenizer;
import org.springframework.batch.item.file.transform.Range;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * FixedByteLengthTokenizer
 *
 * 고정 바이트 길이로 파싱하는 토크나이저
 * - 문자열을 지정된 '바이트 범위'에 따라 토큰화
 * - '글자수'가 아니라 '바이트 단위'로 잘라야 해서 커스텀 토크나이저를 만들어야 했음 (한글 1글자 = 2바이트로 취급됨)
 */
public class FixedByteLengthTokenizer extends AbstractLineTokenizer {

    private Range[] ranges;
    private String encoding = "EUC-KR";

    public void setRanges(Range[] ranges) {
        this.ranges = ranges;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @Override
    protected List<String> doTokenize(String line) {
        List<String> tokens = new ArrayList<>();
        byte[] lineBytes;

        try {
            // String -> Byte[]로 변환 (EUC-KR 사용)
            lineBytes = line.getBytes(encoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported encoding: " + encoding, e);
        }

        for (Range range : ranges) {
            // Range의 범위값은 1부터 시작하므로 0 - based 인덱스로 변환
            int start = range.getMin() - 1;
            int end = range.getMax();

            // Byte[] 배열 범위 체크
            if (start >= lineBytes.length) {
                tokens.add("");
                continue;
            }
            if (end > lineBytes.length) {
                end = lineBytes.length;
            }

            // Byte[] 배열에서 Range 범위에 맞게 Slicing
            byte[] tokenBytes = Arrays.copyOfRange(lineBytes, start, end);

            // Byte[] -> String (다시 복원)
            try {
                // trim()을 해서 공백 제거
                tokens.add(new String(tokenBytes, encoding).trim());
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        return tokens;
    }
}
