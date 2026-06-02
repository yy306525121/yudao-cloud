package cn.iocoder.yudao.module.infra.framework.file.core.client;

import java.util.List;

/**
 * 文件客户端
 *
 * @author 芋道源码
 */
public interface FileClient {

    /**
     * 获得客户端编号
     *
     * @return 客户端编号
     */
    Long getId();

    /**
     * 上传文件
     *
     * @param content 文件流
     * @param path    相对路径
     * @return 完整路径，即 HTTP 访问地址
     * @throws Exception 上传文件时，抛出 Exception 异常
     */
    String upload(byte[] content, String path, String type) throws Exception;

    /**
     * 删除文件
     *
     * @param path 相对路径
     * @throws Exception 删除文件时，抛出 Exception 异常
     */
    void delete(String path) throws Exception;

    /**
     * 获得文件的内容
     *
     * @param path 相对路径
     * @return 文件的内容
     */
    byte[] getContent(String path) throws Exception;

    /**
     * 获得目录下的文件列表
     *
     * @param path 相对目录
     * @return 文件列表
     */
    default List<FileItem> list(String path) throws Exception {
        throw new UnsupportedOperationException("不支持的操作");
    }

    /**
     * 重命名文件或目录
     *
     * @param path    相对路径
     * @param newName 新名称
     */
    default void rename(String path, String newName) throws Exception {
        throw new UnsupportedOperationException("不支持的操作");
    }

    /**
     * 删除空目录
     *
     * @param path 相对目录
     */
    default void deleteDirectory(String path) throws Exception {
        throw new UnsupportedOperationException("不支持的操作");
    }

    // ========== 文件签名，目前仅 S3 支持 ==========

    /**
     * 获得文件预签名地址，用于上传
     *
     * @param path 相对路径
     * @return 文件预签名地址
     */
    default String presignPutUrl(String path) {
        throw new UnsupportedOperationException("不支持的操作");
    }

    /**
     * 生成文件预签名地址，用于读取
     *
     * @param url 完整的文件访问地址
     * @param expirationSeconds 访问有效期，单位秒
     * @return 文件预签名地址
     */
    default String presignGetUrl(String url, Integer expirationSeconds) {
        throw new UnsupportedOperationException("不支持的操作");
    }

}
