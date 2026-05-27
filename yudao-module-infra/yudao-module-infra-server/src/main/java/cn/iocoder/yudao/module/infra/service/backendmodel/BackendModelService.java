package cn.iocoder.yudao.module.infra.service.backendmodel;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.infra.controller.admin.backendmodel.vo.BackendModelFieldSaveReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.backendmodel.vo.BackendModelPageReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.backendmodel.vo.BackendModelSaveReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.backendmodel.BackendModelDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.backendmodel.BackendModelFieldDO;
import jakarta.validation.Valid;

import java.util.List;

public interface BackendModelService {

    Long createBackendModel(@Valid BackendModelSaveReqVO createReqVO);

    void updateBackendModel(@Valid BackendModelSaveReqVO updateReqVO);

    void updateBackendModelFieldList(Long id, List<BackendModelFieldSaveReqVO> fields);

    void deleteBackendModel(Long id);

    void deleteBackendModelList(List<Long> ids);

    BackendModelDO getBackendModel(Long id);

    List<BackendModelFieldDO> getBackendModelFieldList(Long backendModelId);

    PageResult<BackendModelDO> getBackendModelPage(BackendModelPageReqVO pageReqVO);

    BackendModelDO validateBackendModelExists(Long id);

}
