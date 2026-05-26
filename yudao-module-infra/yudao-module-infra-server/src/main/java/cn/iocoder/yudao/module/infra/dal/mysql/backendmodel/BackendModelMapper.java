package cn.iocoder.yudao.module.infra.dal.mysql.backendmodel;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.infra.controller.admin.backendmodel.vo.BackendModelPageReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.backendmodel.BackendModelDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BackendModelMapper extends BaseMapperX<BackendModelDO> {

    default PageResult<BackendModelDO> selectPage(BackendModelPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<BackendModelDO>()
                .likeIfPresent(BackendModelDO::getName, reqVO.getName())
                .eqIfPresent(BackendModelDO::getDataSourceConfigId, reqVO.getDataSourceConfigId())
                .eqIfPresent(BackendModelDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(BackendModelDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(BackendModelDO::getId));
    }

}
