package cn.iocoder.yudao.module.infra.controller.admin.backendmodel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 后台模型字段配置更新 Request VO")
@Data
public class BackendModelFieldUpdateReqVO {

    @Schema(description = "后台模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "后台模型编号不能为空")
    private Long id;

    @Schema(description = "字段配置", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotNull(message = "字段配置不能为空")
    private List<BackendModelFieldSaveReqVO> fields;

}
