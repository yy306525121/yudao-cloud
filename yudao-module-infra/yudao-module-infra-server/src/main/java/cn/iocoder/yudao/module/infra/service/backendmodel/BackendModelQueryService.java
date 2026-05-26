package cn.iocoder.yudao.module.infra.service.backendmodel;

import cn.iocoder.yudao.module.infra.controller.admin.backendmodel.vo.BackendModelPreviewReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.backendmodel.vo.BackendModelQueryRespVO;

public interface BackendModelQueryService {

    BackendModelQueryRespVO getBackendModelPage(Long id, Integer pageNo, Integer pageSize);

    BackendModelQueryRespVO previewBackendModel(BackendModelPreviewReqVO reqVO);

    void validateSqlText(String sqlText);

    String buildPageSql(String url, String sqlText, Integer pageNo, Integer pageSize);

}
