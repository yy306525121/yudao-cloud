package cn.iocoder.yudao.module.infra.service.file;

import cn.iocoder.yudao.module.infra.framework.file.core.client.FileItem;

import java.util.List;

/**
 * 文件存储浏览 Service 接口
 *
 * @author 芋道源码
 */
public interface FileStorageService {

    /**
     * 获得目录下的文件列表
     *
     * @param configId 文件配置编号
     * @param path 目录
     * @param keyword 文件名关键字
     * @return 文件列表
     */
    List<FileItem> listFiles(Long configId, String path, String keyword) throws Exception;

    /**
     * 上传文件到指定存储配置
     *
     * @param configId 文件配置编号
     * @param content 文件内容
     * @param name 文件名
     * @param directory 目录
     * @param type 文件类型
     * @return 文件 URL
     */
    String uploadFile(Long configId, byte[] content, String name, String directory, String type) throws Exception;

    /**
     * 获得文件内容
     *
     * @param configId 文件配置编号
     * @param path 文件路径
     * @return 文件内容
     */
    byte[] getFileContent(Long configId, String path) throws Exception;

    /**
     * 重命名文件或目录
     *
     * @param configId 文件配置编号
     * @param path 文件路径
     * @param newName 新名称
     */
    void renameFile(Long configId, String path, String newName) throws Exception;

    /**
     * 删除文件或空目录
     *
     * @param configId 文件配置编号
     * @param path 文件路径
     */
    void deleteFile(Long configId, String path) throws Exception;

}
