package cn.iocoder.yudao.module.infra.service.backendmodel;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.infra.controller.admin.backendmodel.vo.BackendModelPageReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.backendmodel.vo.BackendModelSaveReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.backendmodel.BackendModelDO;
import jakarta.validation.Valid;

import java.util.List;

public interface BackendModelService {

    Long createBackendModel(@Valid BackendModelSaveReqVO createReqVO);

    void updateBackendModel(@Valid BackendModelSaveReqVO updateReqVO);

    void deleteBackendModel(Long id);

    void deleteBackendModelList(List<Long> ids);

    BackendModelDO getBackendModel(Long id);

    PageResult<BackendModelDO> getBackendModelPage(BackendModelPageReqVO pageReqVO);

    BackendModelDO validateBackendModelExists(Long id);

}
