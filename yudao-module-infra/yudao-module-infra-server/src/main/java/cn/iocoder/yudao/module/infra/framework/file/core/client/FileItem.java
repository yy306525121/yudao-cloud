package cn.iocoder.yudao.module.infra.framework.file.core.client;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 文件存储中的文件或目录项
 *
 * @author 芋道源码
 */
@Data
@Accessors(chain = true)
public class FileItem {

    /**
     * 文件名
     */
    private String name;
    /**
     * 文件路径，相对于存储配置的 basePath
     */
    private String path;
    /**
     * 是否为目录
     */
    private Boolean directory;
    /**
     * 文件大小。目录为空
     */
    private Long size;
    /**
     * 最后修改时间
     */
    private LocalDateTime lastModifiedTime;
    /**
     * 文件 URL。目录为空
     */
    private String url;

}
