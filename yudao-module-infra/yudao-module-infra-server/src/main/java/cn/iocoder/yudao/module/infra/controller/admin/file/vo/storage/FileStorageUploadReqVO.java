package cn.iocoder.yudao.module.infra.controller.admin.file.vo.storage;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "管理后台 - 存储文件上传 Request VO")
@Data
public class FileStorageUploadReqVO {

    @Schema(description = "文件配置编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "文件配置编号不能为空")
    private Long configId;

    @Schema(description = "文件目录", example = "20250602")
    private String directory;

    @Schema(description = "文件附件", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "文件附件不能为空")
    private MultipartFile file;

    @AssertTrue(message = "文件目录不正确")
    @JsonIgnore
    public boolean isDirectoryValid() {
        return StrUtil.isEmpty(directory) || (!StrUtil.contains(directory, "..")
                && !StrUtil.startWithAny(directory, "/", "\\"));
    }

}
