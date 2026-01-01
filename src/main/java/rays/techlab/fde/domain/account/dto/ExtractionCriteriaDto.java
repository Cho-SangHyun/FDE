package rays.techlab.fde.domain.account.dto;

public record ExtractionCriteriaDto(
        Long businessUnitId,
        String baseDate,
        String startDate,
        String endDate
) {
}
