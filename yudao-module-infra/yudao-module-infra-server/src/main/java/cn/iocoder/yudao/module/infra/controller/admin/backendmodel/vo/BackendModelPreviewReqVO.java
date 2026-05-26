package cn.iocoder.yudao.module.infra.controller.admin.backendmodel.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 后台模型预览 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BackendModelPreviewReqVO extends PageParam {

    @Schema(description = "数据源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "数据源不能为空")
    private Long dataSourceConfigId;

    @Schema(description = "查询 SQL", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "查询 SQL 不能为空")
    private String sqlText;

}
