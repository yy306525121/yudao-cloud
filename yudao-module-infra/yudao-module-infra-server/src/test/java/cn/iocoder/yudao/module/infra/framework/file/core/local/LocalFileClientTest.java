package cn.iocoder.yudao.module.infra.framework.file.core.local;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.module.infra.framework.file.core.client.local.LocalFileClient;
import cn.iocoder.yudao.module.infra.framework.file.core.client.local.LocalFileClientConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;

public class LocalFileClientTest {

    @TempDir
    public Path tempDir;

    @Test
    public void testListRenameAndDeleteDirectory() throws Exception {
        // 创建客户端
        LocalFileClientConfig config = new LocalFileClientConfig();
        config.setDomain("http://127.0.0.1:48080");
        config.setBasePath(tempDir.toString());
        LocalFileClient client = new LocalFileClient(10L, config);
        client.init();

        // 上传文件
        byte[] content = ResourceUtil.readBytes("file/erweima.jpg");
        String url = client.upload(content, "demo/erweima.jpg", "image/jpeg");
        assertEquals("http://127.0.0.1:48080/admin-api/infra/file/10/get/demo/erweima.jpg", url);

        // 列表
        var list = client.list("demo");
        assertEquals(1, list.size());
        assertEquals("erweima.jpg", list.get(0).getName());
        assertFalse(list.get(0).getDirectory());
        assertArrayEquals(content, client.getContent("demo/erweima.jpg"));

        // 重命名
        client.rename("demo/erweima.jpg", "renamed.jpg");
        assertFalse(Files.exists(tempDir.resolve("demo/erweima.jpg")));
        assertTrue(Files.exists(tempDir.resolve("demo/renamed.jpg")));

        // 删除文件和空目录
        client.delete("demo/renamed.jpg");
        client.deleteDirectory("demo");
        assertFalse(Files.exists(tempDir.resolve("demo")));
    }

    @Test
    @Disabled
    public void test() {
        // 创建客户端
        LocalFileClientConfig config = new LocalFileClientConfig();
        config.setDomain("http://127.0.0.1:48080");
        config.setBasePath("/Users/yunai/file_test");
        LocalFileClient client = new LocalFileClient(0L, config);
        client.init();
        // 上传文件
        String path = IdUtil.fastSimpleUUID() + ".jpg";
        byte[] content = ResourceUtil.readBytes("file/erweima.jpg");
        String fullPath = client.upload(content, path, "image/jpeg");
        System.out.println("访问地址：" + fullPath);
        client.delete(path);
    }

    @Test
    @Disabled
    public void testGetContent_notFound() {
        // 创建客户端
        LocalFileClientConfig config = new LocalFileClientConfig();
        config.setDomain("http://127.0.0.1:48080");
        config.setBasePath("/Users/yunai/file_test");
        LocalFileClient client = new LocalFileClient(0L, config);
        client.init();
        // 上传文件
        byte[] content = client.getContent(randomString());
        System.out.println();
    }

}
