package cn.iocoder.yudao.module.infra.service.file;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileConfigDO;
import cn.iocoder.yudao.module.infra.framework.file.core.client.FileClient;
import cn.iocoder.yudao.module.infra.framework.file.core.client.FileItem;
import cn.iocoder.yudao.module.infra.framework.file.core.enums.FileStorageEnum;
import cn.iocoder.yudao.module.infra.framework.file.core.utils.FileTypeUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.*;

/**
 * 文件存储浏览 Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Set<Integer> BROWSABLE_STORAGES = Set.of(
            FileStorageEnum.LOCAL.getStorage(),
            FileStorageEnum.FTP.getStorage(),
            FileStorageEnum.SFTP.getStorage());

    @Resource
    private FileConfigService fileConfigService;

    @Override
    public List<FileItem> listFiles(Long configId, String path, String keyword) throws Exception {
        validatePath(path, true);
        FileClient client = getBrowsableFileClient(configId);
        return client.list(path).stream()
                .filter(item -> StrUtil.isEmpty(keyword) || StrUtil.containsIgnoreCase(item.getName(), keyword))
                .collect(Collectors.toList());
    }

    @Override
    public String uploadFile(Long configId, byte[] content, String name, String directory, String type) throws Exception {
        validatePath(directory, true);
        if (StrUtil.isEmpty(name) || StrUtil.containsAny(name, "/", "\\") || StrUtil.contains(name, "..")) {
            throw exception(FILE_PATH_INVALID);
        }
        if (StrUtil.isEmpty(type)) {
            type = FileTypeUtils.getMineType(content, name);
        }
        FileClient client = getBrowsableFileClient(configId);
        return client.upload(content, buildPath(directory, name), type);
    }

    @Override
    public byte[] getFileContent(Long configId, String path) throws Exception {
        validatePath(path, false);
        FileClient client = getBrowsableFileClient(configId);
        return client.getContent(path);
    }

    @Override
    public void renameFile(Long configId, String path, String newName) throws Exception {
        validatePath(path, false);
        if (StrUtil.isEmpty(newName) || StrUtil.containsAny(newName, "/", "\\") || StrUtil.contains(newName, "..")) {
            throw exception(FILE_PATH_INVALID);
        }
        FileClient client = getBrowsableFileClient(configId);
        client.rename(path, newName);
    }

    @Override
    public void deleteFile(Long configId, String path) throws Exception {
        validatePath(path, false);
        FileClient client = getBrowsableFileClient(configId);
        FileItem item = getFileItem(client, path);
        if (Boolean.TRUE.equals(item.getDirectory())) {
            if (!client.list(path).isEmpty()) {
                throw exception(FILE_DIRECTORY_NOT_EMPTY);
            }
            client.deleteDirectory(path);
            return;
        }
        client.delete(path);
    }

    private FileClient getBrowsableFileClient(Long configId) {
        FileConfigDO fileConfig = fileConfigService.getFileConfig(configId);
        if (fileConfig == null) {
            throw exception(FILE_CONFIG_NOT_EXISTS);
        }
        if (!BROWSABLE_STORAGES.contains(fileConfig.getStorage())) {
            throw exception(FILE_STORAGE_NOT_SUPPORTED);
        }
        FileClient client = fileConfigService.getFileClient(configId);
        Assert.notNull(client, "客户端({}) 不能为空", configId);
        return client;
    }

    private FileItem getFileItem(FileClient client, String path) throws Exception {
        String parentPath = StrUtil.contains(path, StrUtil.SLASH) ? StrUtil.subBefore(path, StrUtil.SLASH, true) : "";
        String name = FileUtil.getName(path);
        return client.list(parentPath).stream()
                .filter(item -> StrUtil.equals(item.getName(), name))
                .findFirst()
                .orElseThrow(() -> exception(FILE_NOT_EXISTS));
    }

    private void validatePath(String path, boolean allowEmpty) {
        if (StrUtil.isEmpty(path)) {
            if (allowEmpty) {
                return;
            }
            throw exception(FILE_PATH_INVALID);
        }
        if (StrUtil.contains(path, "..") || StrUtil.startWithAny(path, "/", "\\")) {
            throw exception(FILE_PATH_INVALID);
        }
    }

    private String buildPath(String directory, String name) {
        return StrUtil.isEmpty(directory) ? name : directory + StrUtil.SLASH + name;
    }

}
