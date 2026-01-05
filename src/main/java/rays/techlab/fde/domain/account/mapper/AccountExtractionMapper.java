package rays.techlab.fde.domain.account.mapper;

import org.apache.ibatis.annotations.Mapper;
import rays.techlab.fde.domain.account.dto.DemandTargetDto;
import rays.techlab.fde.domain.account.dto.ExtractedAccountDto;
import rays.techlab.fde.domain.account.dto.ExtractionCriteriaDto;
import rays.techlab.fde.job.extract.dto.AccountInformationResultItem;

import java.util.List;
import java.util.Map;

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

    /**
     * 추출된 계좌정보 조회
     */
    List<ExtractedAccountDto> selectExtractedAccountInformation(Map<String, Object> params);
}
