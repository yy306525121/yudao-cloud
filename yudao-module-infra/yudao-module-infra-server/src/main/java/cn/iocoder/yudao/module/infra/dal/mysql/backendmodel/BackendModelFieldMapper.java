package cn.iocoder.yudao.module.infra.dal.mysql.backendmodel;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.infra.dal.dataobject.backendmodel.BackendModelFieldDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface BackendModelFieldMapper extends BaseMapperX<BackendModelFieldDO> {

    default List<BackendModelFieldDO> selectListByBackendModelId(Long backendModelId) {
        return selectList(new LambdaQueryWrapperX<BackendModelFieldDO>()
                .eq(BackendModelFieldDO::getBackendModelId, backendModelId)
                .orderByAsc(BackendModelFieldDO::getFieldOrder)
                .orderByAsc(BackendModelFieldDO::getId));
    }

    default void deleteByBackendModelId(Long backendModelId) {
        delete(BackendModelFieldDO::getBackendModelId, backendModelId);
    }

    default void deleteByBackendModelIds(Collection<Long> backendModelIds) {
        delete(new LambdaQueryWrapperX<BackendModelFieldDO>()
                .in(BackendModelFieldDO::getBackendModelId, backendModelIds));
    }

}
