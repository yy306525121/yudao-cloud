package cn.iocoder.yudao.module.infra.service.backendmodel;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.framework.test.core.util.RandomUtils;
import cn.iocoder.yudao.module.infra.controller.admin.backendmodel.vo.BackendModelPageReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.backendmodel.vo.BackendModelSaveReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.backendmodel.BackendModelDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.db.DataSourceConfigDO;
import cn.iocoder.yudao.module.infra.dal.mysql.backendmodel.BackendModelMapper;
import cn.iocoder.yudao.module.infra.service.db.DataSourceConfigService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jakarta.annotation.Resource;
import java.util.function.Consumer;

import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.BACKEND_MODEL_NOT_EXISTS;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.DATA_SOURCE_CONFIG_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Import(BackendModelServiceImpl.class)
public class BackendModelServiceImplTest extends BaseDbUnitTest {

    @Resource
    private BackendModelServiceImpl backendModelService;
    @Resource
    private BackendModelMapper backendModelMapper;

    @MockitoBean
    private DataSourceConfigService dataSourceConfigService;
    @MockitoBean
    private BackendModelQueryService backendModelQueryService;

    @Test
    public void testCreateBackendModel_success() {
        BackendModelSaveReqVO reqVO = randomPojo(BackendModelSaveReqVO.class, o -> {
            o.setId(null);
            o.setDataSourceConfigId(1L);
            o.setSqlText("select 1");
        });
        when(dataSourceConfigService.getDataSourceConfig(eq(1L))).thenReturn(new DataSourceConfigDO());

        Long id = backendModelService.createBackendModel(reqVO);

        assertNotNull(id);
        BackendModelDO backendModel = backendModelMapper.selectById(id);
        assertPojoEquals(reqVO, backendModel, "id");
        verify(backendModelQueryService).validateSqlText(eq("select 1"));
    }

    @Test
    public void testCreateBackendModel_dataSourceNotExists() {
        BackendModelSaveReqVO reqVO = randomPojo(BackendModelSaveReqVO.class, o -> o.setDataSourceConfigId(1L));
        when(dataSourceConfigService.getDataSourceConfig(eq(1L))).thenReturn(null);

        assertServiceException(() -> backendModelService.createBackendModel(reqVO), DATA_SOURCE_CONFIG_NOT_EXISTS);
        verify(backendModelQueryService, never()).validateSqlText(anyString());
    }

    @Test
    public void testUpdateBackendModel_success() {
        BackendModelDO dbBackendModel = randomBackendModelDO();
        backendModelMapper.insert(dbBackendModel);
        BackendModelSaveReqVO reqVO = randomPojo(BackendModelSaveReqVO.class, o -> {
            o.setId(dbBackendModel.getId());
            o.setDataSourceConfigId(1L);
            o.setSqlText("select 1");
        });
        when(dataSourceConfigService.getDataSourceConfig(eq(1L))).thenReturn(new DataSourceConfigDO());

        backendModelService.updateBackendModel(reqVO);

        BackendModelDO backendModel = backendModelMapper.selectById(reqVO.getId());
        assertPojoEquals(reqVO, backendModel);
    }

    @Test
    public void testUpdateBackendModel_notExists() {
        BackendModelSaveReqVO reqVO = randomPojo(BackendModelSaveReqVO.class);

        assertServiceException(() -> backendModelService.updateBackendModel(reqVO), BACKEND_MODEL_NOT_EXISTS);
    }

    @Test
    public void testDeleteBackendModel_success() {
        BackendModelDO dbBackendModel = randomBackendModelDO();
        backendModelMapper.insert(dbBackendModel);

        backendModelService.deleteBackendModel(dbBackendModel.getId());

        assertNull(backendModelMapper.selectById(dbBackendModel.getId()));
    }

    @Test
    public void testValidateBackendModelExists_notExists() {
        assertServiceException(() -> backendModelService.validateBackendModelExists(randomLongId()), BACKEND_MODEL_NOT_EXISTS);
    }

    @Test
    public void testGetBackendModelPage() {
        BackendModelDO dbBackendModel = randomBackendModelDO(o -> {
            o.setName("订单查询");
            o.setDataSourceConfigId(1L);
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        backendModelMapper.insert(dbBackendModel);
        backendModelMapper.insert(cloneIgnoreId(dbBackendModel, o -> o.setName("用户查询")));
        backendModelMapper.insert(cloneIgnoreId(dbBackendModel, o -> o.setDataSourceConfigId(2L)));
        backendModelMapper.insert(cloneIgnoreId(dbBackendModel, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        BackendModelPageReqVO reqVO = new BackendModelPageReqVO();
        reqVO.setName("订单");
        reqVO.setDataSourceConfigId(1L);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());

        PageResult<BackendModelDO> pageResult = backendModelService.getBackendModelPage(reqVO);

        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbBackendModel, pageResult.getList().get(0));
    }

    @SafeVarargs
    private static BackendModelDO randomBackendModelDO(Consumer<BackendModelDO>... consumers) {
        Consumer<BackendModelDO> consumer = o -> {
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setSqlText("select 1");
        };
        return RandomUtils.randomPojo(BackendModelDO.class, ArrayUtils.append(consumer, consumers));
    }

}
