package rays.techlab.fde.domain.account.mapper;

import org.apache.ibatis.annotations.Mapper;
import rays.techlab.fde.domain.account.dto.DemandTargetDto;
import rays.techlab.fde.domain.account.dto.ExtractionCriteriaDto;

@Mapper
public interface AccountExtractionMapper {

    /**
     * 계좌정보 요구 대상자 정보 적재
     */
    void insertDemandTarget(DemandTargetDto demandTargetDto);

    /**
     * 요구 대상자들의 계좌정보 추출
     */
    void extractAccountInformation(ExtractionCriteriaDto criteriaDto);
}
