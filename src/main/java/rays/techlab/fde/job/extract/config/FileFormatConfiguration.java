package rays.techlab.fde.job.extract.config;

import org.springframework.batch.item.file.transform.Range;

/**
 * 파일 포맷 설정 클래스
 *
 * 고정 길이 파일의 필드 범위와 길이를 관리합니다.
 */
public class FileFormatConfiguration {

    /**
     * 요구 대상자 파일 포맷
     */
    public static class DemandFileFormat {
        // 필드 시작 위치
        public static final int SEQUENCE_NUMBER_START = 1;
        public static final int INHABITANT_NUMBER_START = 9;
        public static final int TARGET_NAME_START = 23;
        public static final int BASE_DATE_START = 53;

        // 필드 끝 위치
        public static final int SEQUENCE_NUMBER_END = 8;
        public static final int INHABITANT_NUMBER_END = 22;
        public static final int TARGET_NAME_END = 52;
        public static final int BASE_DATE_END = 61;

        // 필드명
        public static final String[] FIELD_NAMES = {
                "sequenceNumber",
                "inhabitantNumber",
                "targetName",
                "baseDate"
        };

        /**
         * 요구 대상자 파일의 Range 배열을 반환합니다.
         */
        public static Range[] getRanges() {
            return new Range[] {
                    new Range(SEQUENCE_NUMBER_START, SEQUENCE_NUMBER_END),
                    new Range(INHABITANT_NUMBER_START, INHABITANT_NUMBER_END),
                    new Range(TARGET_NAME_START, TARGET_NAME_END),
                    new Range(BASE_DATE_START, BASE_DATE_END)
            };
        }
    }

    /**
     * 결과 파일 포맷
     */
    public static class ResultFileFormat {
        // 필드 길이 (바이트 단위)
        public static final int SEQUENCE_NUMBER_LENGTH = 8;
        public static final int CUSTOMER_NAME_LENGTH = 30;
        public static final int INHABITANT_NUMBER_LENGTH = 14;
        public static final int ACCOUNT_NUMBER_LENGTH = 30;
        public static final int PRODUCT_TYPE_LENGTH = 2;
        public static final int PRODUCT_NAME_LENGTH = 20;
        public static final int BALANCE_LENGTH = 20;

        /**
         * 결과 파일의 필드 길이 배열을 반환합니다.
         */
        public static int[] getFieldLengths() {
            return new int[] {
                    SEQUENCE_NUMBER_LENGTH,
                    CUSTOMER_NAME_LENGTH,
                    INHABITANT_NUMBER_LENGTH,
                    ACCOUNT_NUMBER_LENGTH,
                    PRODUCT_TYPE_LENGTH,
                    PRODUCT_NAME_LENGTH,
                    BALANCE_LENGTH
            };
        }
    }

    /**
     * 공통 인코딩 설정
     */
    public static final String DEFAULT_ENCODING = "EUC-KR";
}