package cn.iocoder.yudao.module.infra.controller.admin.backendmodel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - 后台模型创建/修改 Request VO")
@Data
public class BackendModelSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "模型名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "订单查询")
    @NotBlank(message = "模型名称不能为空")
    @Size(max = 100, message = "模型名称长度不能超过100个字符")
    private String name;

    @Schema(description = "数据源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "数据源不能为空")
    private Long dataSourceConfigId;

    @Schema(description = "查询 SQL", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "查询 SQL 不能为空")
    private String sqlText;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注", example = "用于菜单动态展示")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

}
