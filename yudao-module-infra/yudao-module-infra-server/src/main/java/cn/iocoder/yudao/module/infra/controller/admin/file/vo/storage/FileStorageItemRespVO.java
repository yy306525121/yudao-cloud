package cn.iocoder.yudao.module.infra.controller.admin.file.vo.storage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 存储文件 Response VO")
@Data
public class FileStorageItemRespVO {

    @Schema(description = "文件名", requiredMode = Schema.RequiredMode.REQUIRED, example = "yudao.jpg")
    private String name;

    @Schema(description = "文件路径", requiredMode = Schema.RequiredMode.REQUIRED, example = "20250602/yudao.jpg")
    private String path;

    @Schema(description = "是否为目录", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean directory;

    @Schema(description = "文件大小", example = "1024")
    private Long size;

    @Schema(description = "最后修改时间")
    private LocalDateTime lastModifiedTime;

    @Schema(description = "文件 URL", example = "https://www.iocoder.cn/yudao.jpg")
    private String url;

}
