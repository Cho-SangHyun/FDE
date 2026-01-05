package rays.techlab.fde.global.support;

import org.springframework.batch.item.file.transform.FieldExtractor;
import org.springframework.batch.item.file.transform.LineAggregator;

import java.util.concurrent.atomic.AtomicInteger;

/*
 * 고정 바이트 길이를 적용하는 LineAggregator
 * - 지정된 '바이트 길이'를 필드들에 적용하여 문자열로 만든다
 * - '글자수'가 아니라 '바이트 단위'로 만들어야 해서 커스텀 LineAggregator를 만들어야 했음 (한글 1글자 = 2바이트로 취급됨)
 */
public class FixedByteLengthLineAggregator<T> implements LineAggregator<T> {

    private final FieldExtractor<T> fieldExtractor;
    private final int[] fieldByteLengths;
    private String encoding = "EUC-KR";

    public FixedByteLengthLineAggregator(FieldExtractor<T> fieldExtractor, int[] fieldByteLengths) {
        this.fieldExtractor = fieldExtractor;
        this.fieldByteLengths = fieldByteLengths;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @Override
    public String aggregate(T item) {
        Object[] fields = fieldExtractor.extract(item);
        StringBuilder line = new StringBuilder();

        for (int i = 0; i < fields.length; i++) {
            String field = fields[i] == null ?  "" : fields[i].toString();

            if (i == 0) {
                line.append(BytePaddingUtils.formatByByte(field, fieldByteLengths[i], encoding, "0"));
                continue;
            }

            line.append(BytePaddingUtils.formatByByte(field, fieldByteLengths[i], encoding));
        }

        return line.toString();
    }
}
