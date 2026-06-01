package cn.iocoder.yudao.module.infra.controller.admin.backendmodel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 后台模型字段 Response VO")
@Data
public class BackendModelFieldRespVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "字段名", requiredMode = Schema.RequiredMode.REQUIRED, example = "billNo")
    private String fieldName;

    @Schema(description = "字段显示名", requiredMode = Schema.RequiredMode.REQUIRED, example = "账单号")
    private String fieldLabel;

    @Schema(description = "字段顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer fieldOrder;

    @Schema(description = "是否在列表展示", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean listVisible;

    @Schema(description = "列表展示类型", example = "text")
    private String listType;

    @Schema(description = "是否作为检索条件", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean searchable;

    @Schema(description = "检索控件类型", example = "text")
    private String searchType;

    @Schema(description = "检索操作符", example = "like")
    private String searchOperator;

    @Schema(description = "字典类型", example = "common_status")
    private String dictType;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

}
