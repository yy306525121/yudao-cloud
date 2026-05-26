package cn.iocoder.yudao.module.infra.service.backendmodel;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.infra.controller.admin.backendmodel.vo.BackendModelPageReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.backendmodel.vo.BackendModelSaveReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.backendmodel.BackendModelDO;
import cn.iocoder.yudao.module.infra.dal.mysql.backendmodel.BackendModelMapper;
import cn.iocoder.yudao.module.infra.service.db.DataSourceConfigService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.BACKEND_MODEL_NOT_EXISTS;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.DATA_SOURCE_CONFIG_NOT_EXISTS;

@Service
@Validated
public class BackendModelServiceImpl implements BackendModelService {

    @Resource
    private BackendModelMapper backendModelMapper;
    @Resource
    private DataSourceConfigService dataSourceConfigService;
    @Resource
    private BackendModelQueryService backendModelQueryService;

    @Override
    public Long createBackendModel(BackendModelSaveReqVO createReqVO) {
        validateDataSourceConfigExists(createReqVO.getDataSourceConfigId());
        backendModelQueryService.validateSqlText(createReqVO.getSqlText());

        BackendModelDO backendModel = BeanUtils.toBean(createReqVO, BackendModelDO.class);
        backendModelMapper.insert(backendModel);
        return backendModel.getId();
    }

    @Override
    public void updateBackendModel(BackendModelSaveReqVO updateReqVO) {
        validateBackendModelExists(updateReqVO.getId());
        validateDataSourceConfigExists(updateReqVO.getDataSourceConfigId());
        backendModelQueryService.validateSqlText(updateReqVO.getSqlText());

        BackendModelDO updateObj = BeanUtils.toBean(updateReqVO, BackendModelDO.class);
        backendModelMapper.updateById(updateObj);
    }

    @Override
    public void deleteBackendModel(Long id) {
        validateBackendModelExists(id);
        backendModelMapper.deleteById(id);
    }

    @Override
    public void deleteBackendModelList(List<Long> ids) {
        backendModelMapper.deleteByIds(ids);
    }

    @Override
    public BackendModelDO getBackendModel(Long id) {
        return backendModelMapper.selectById(id);
    }

    @Override
    public PageResult<BackendModelDO> getBackendModelPage(BackendModelPageReqVO pageReqVO) {
        return backendModelMapper.selectPage(pageReqVO);
    }

    @Override
    public BackendModelDO validateBackendModelExists(Long id) {
        BackendModelDO backendModel = backendModelMapper.selectById(id);
        if (backendModel == null) {
            throw exception(BACKEND_MODEL_NOT_EXISTS);
        }
        return backendModel;
    }

    private void validateDataSourceConfigExists(Long id) {
        if (dataSourceConfigService.getDataSourceConfig(id) == null) {
            throw exception(DATA_SOURCE_CONFIG_NOT_EXISTS);
        }
    }

}
