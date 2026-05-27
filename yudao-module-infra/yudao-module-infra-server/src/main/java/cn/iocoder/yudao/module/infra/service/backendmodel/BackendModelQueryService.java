package cn.iocoder.yudao.module.infra.service.backendmodel;

import cn.iocoder.yudao.module.infra.controller.admin.backendmodel.vo.BackendModelPreviewReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.backendmodel.vo.BackendModelQueryRespVO;

import java.util.List;
import java.util.Map;

public interface BackendModelQueryService {

    BackendModelQueryRespVO getBackendModelPage(Long id, Integer pageNo, Integer pageSize, Map<String, String> queryParams);

    BackendModelQueryRespVO getBackendModelList(Long id, Map<String, String> queryParams);

    BackendModelQueryRespVO previewBackendModel(BackendModelPreviewReqVO reqVO);

    List<BackendModelQueryRespVO.Field> inferFields(Long dataSourceConfigId, String sqlText);

    void validateSqlText(String sqlText);

    String buildPageSql(String url, String sqlText, Integer pageNo, Integer pageSize);

}
