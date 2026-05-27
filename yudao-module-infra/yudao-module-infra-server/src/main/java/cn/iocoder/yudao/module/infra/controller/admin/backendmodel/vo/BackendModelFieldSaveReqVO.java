package cn.iocoder.yudao.module.infra.controller.admin.backendmodel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 后台模型字段保存 Request VO")
@Data
public class BackendModelFieldSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "字段名", requiredMode = Schema.RequiredMode.REQUIRED, example = "billNo")
    @NotBlank(message = "字段名不能为空")
    private String fieldName;

    @Schema(description = "字段显示名", requiredMode = Schema.RequiredMode.REQUIRED, example = "账单号")
    @NotBlank(message = "字段显示名不能为空")
    private String fieldLabel;

    @Schema(description = "字段顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "字段顺序不能为空")
    private Integer fieldOrder;

    @Schema(description = "是否在列表展示", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否在列表展示不能为空")
    private Boolean listVisible;

    @Schema(description = "是否作为检索条件", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否作为检索条件不能为空")
    private Boolean searchable;

    @Schema(description = "检索控件类型", example = "text")
    private String searchType;

    @Schema(description = "检索操作符", example = "like")
    private String searchOperator;

    @Schema(description = "字典类型", example = "common_status")
    private String dictType;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

}
