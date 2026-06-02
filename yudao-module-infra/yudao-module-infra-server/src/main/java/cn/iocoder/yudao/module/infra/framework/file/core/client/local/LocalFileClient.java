package cn.iocoder.yudao.module.infra.framework.file.core.client.local;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.infra.framework.file.core.client.AbstractFileClient;
import cn.iocoder.yudao.module.infra.framework.file.core.client.FileItem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 本地文件客户端
 *
 * @author 芋道源码
 */
public class LocalFileClient extends AbstractFileClient<LocalFileClientConfig> {

    public LocalFileClient(Long id, LocalFileClientConfig config) {
        super(id, config);
    }

    @Override
    protected void doInit() {
    }

    @Override
    public String upload(byte[] content, String path, String type) {
        // 执行写入
        String filePath = getFilePath(path);
        FileUtil.writeBytes(content, filePath);
        // 拼接返回路径
        return super.formatFileUrl(config.getDomain(), path);
    }

    @Override
    public void delete(String path) {
        String filePath = getFilePath(path);
        FileUtil.del(filePath);
    }

    @Override
    public byte[] getContent(String path) {
        String filePath = getFilePath(path);
        try {
            return FileUtil.readBytes(filePath);
        } catch (IORuntimeException ex) {
            if (ex.getMessage().startsWith("File not exist:")) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public List<FileItem> list(String path) throws IOException {
        Path dir = Path.of(getFilePath(path));
        if (!Files.exists(dir)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .map(this::buildFileItem)
                    .sorted(Comparator.comparing(FileItem::getDirectory).reversed()
                            .thenComparing(FileItem::getName, String.CASE_INSENSITIVE_ORDER))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void rename(String path, String newName) throws IOException {
        Path source = Path.of(getFilePath(path));
        Files.move(source, source.resolveSibling(newName));
    }

    @Override
    public void deleteDirectory(String path) throws IOException {
        Files.delete(Path.of(getFilePath(path)));
    }

    private String getFilePath(String path) {
        return config.getBasePath() + File.separator + path;
    }

    private FileItem buildFileItem(Path file) {
        boolean directory = Files.isDirectory(file);
        String path = Path.of(config.getBasePath()).relativize(file).toString().replace(File.separator, StrUtil.SLASH);
        return new FileItem()
                .setName(file.getFileName().toString())
                .setPath(path)
                .setDirectory(directory)
                .setSize(directory ? null : FileUtil.size(file.toFile()))
                .setLastModifiedTime(getLastModifiedTime(file))
                .setUrl(directory ? null : super.formatFileUrl(config.getDomain(), path));
    }

    private LocalDateTime getLastModifiedTime(Path file) {
        try {
            return LocalDateTime.ofInstant(Files.getLastModifiedTime(file).toInstant(), ZoneId.systemDefault());
        } catch (IOException ex) {
            return null;
        }
    }

}
