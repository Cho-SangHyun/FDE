package rays.techlab.fde.domain.account.mapper;

import org.apache.ibatis.annotations.Mapper;
import rays.techlab.fde.domain.account.dto.DemandTargetDto;
import rays.techlab.fde.domain.account.dto.ExtractionCriteriaDto;

@Mapper
public interface AccountExtractionMapper {

    void insertDemandTarget(DemandTargetDto demandTargetDto);

    void extractAccountInformation(ExtractionCriteriaDto criteriaDto);
}
