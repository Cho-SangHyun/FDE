package rays.techlab.fde.domain.account.dto;

public record DemandTargetDto(
        Long businessUnitId,
        Long sequenceNumber,
        String encryptedInhabitantNumber,
        String demandTargetName,
        String baseDate
) {
}
