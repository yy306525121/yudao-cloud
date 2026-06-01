package cn.iocoder.yudao.module.infra.controller.admin.backendmodel.vo;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - 后台模型查询 Response VO")
@Data
public class BackendModelQueryRespVO {

    @Schema(description = "字段列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Field> fields;

    @Schema(description = "分页数据", requiredMode = Schema.RequiredMode.REQUIRED)
    private PageResult<Map<String, Object>> pageResult;

    @Schema(description = "字段")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Field {

        @Schema(description = "字段名", requiredMode = Schema.RequiredMode.REQUIRED, example = "userName")
        private String name;

        @Schema(description = "字段标签", requiredMode = Schema.RequiredMode.REQUIRED, example = "userName")
        private String label;

        @Schema(description = "是否在列表展示")
        private Boolean listVisible;

        @Schema(description = "列表展示类型", example = "text")
        private String listType;

        @Schema(description = "是否作为检索条件")
        private Boolean searchable;

        @Schema(description = "检索控件类型", example = "text")
        private String searchType;

        @Schema(description = "检索操作符", example = "like")
        private String searchOperator;

        @Schema(description = "字典类型", example = "common_status")
        private String dictType;

        @Schema(description = "状态", example = "0")
        private Integer status;

    }

}
