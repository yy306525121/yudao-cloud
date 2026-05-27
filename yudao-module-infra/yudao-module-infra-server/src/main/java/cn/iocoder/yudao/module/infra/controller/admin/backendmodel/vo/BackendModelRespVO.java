package cn.iocoder.yudao.module.infra.controller.admin.backendmodel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 后台模型 Response VO")
@Data
public class BackendModelRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "模型名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "订单查询")
    private String name;

    @Schema(description = "数据源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long dataSourceConfigId;

    @Schema(description = "查询 SQL", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sqlText;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "备注", example = "用于菜单动态展示")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "字段配置")
    private List<BackendModelFieldRespVO> fields;

}
