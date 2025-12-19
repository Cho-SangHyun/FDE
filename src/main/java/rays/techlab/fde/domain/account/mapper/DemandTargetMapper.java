package rays.techlab.fde.domain.account.mapper;

import org.apache.ibatis.annotations.Mapper;
import rays.techlab.fde.domain.account.dto.DemandTargetDto;

@Mapper
public interface DemandTargetMapper {

    void insertDemandTarget(DemandTargetDto demandTargetDto);
}
