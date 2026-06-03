package cn.iocoder.yudao.module.infra.controller.admin.file.vo.storage;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 存储文件重命名 Request VO")
@Data
public class FileStorageRenameReqVO {

    @Schema(description = "文件配置编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "文件配置编号不能为空")
    private Long configId;

    @Schema(description = "文件路径", requiredMode = Schema.RequiredMode.REQUIRED, example = "20250602/yudao.jpg")
    @NotEmpty(message = "文件路径不能为空")
    private String path;

    @Schema(description = "新文件名", requiredMode = Schema.RequiredMode.REQUIRED, example = "ruoyi.jpg")
    @NotEmpty(message = "新文件名不能为空")
    private String newName;

    @AssertTrue(message = "文件路径不正确")
    @JsonIgnore
    public boolean isPathValid() {
        return !StrUtil.contains(path, "..") && !StrUtil.startWithAny(path, "/", "\\");
    }

    @AssertTrue(message = "新文件名不正确")
    @JsonIgnore
    public boolean isNewNameValid() {
        return !StrUtil.containsAny(newName, "/", "\\") && !StrUtil.contains(newName, "..");
    }

}
