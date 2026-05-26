package cn.iocoder.yudao.module.infra.service.backendmodel;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.infra.dal.mysql.backendmodel.BackendModelMapper;
import cn.iocoder.yudao.module.infra.service.db.DataSourceConfigService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jakarta.annotation.Resource;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.BACKEND_MODEL_DB_NOT_SUPPORT;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.BACKEND_MODEL_SQL_INVALID;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.BACKEND_MODEL_SQL_ONLY_SELECT;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Import(BackendModelQueryServiceImpl.class)
public class BackendModelQueryServiceImplTest extends BaseDbUnitTest {

    @Resource
    private BackendModelQueryServiceImpl backendModelQueryService;

    @MockitoBean
    private BackendModelMapper backendModelMapper;
    @MockitoBean
    private DataSourceConfigService dataSourceConfigService;

    @Test
    public void testValidateSqlText_success() {
        backendModelQueryService.validateSqlText("select id, name from system_user;");
    }

    @Test
    public void testValidateSqlText_onlySelect() {
        assertServiceException(() -> backendModelQueryService.validateSqlText("update system_user set name = 'a'"),
                BACKEND_MODEL_SQL_ONLY_SELECT);
    }

    @Test
    public void testValidateSqlText_multiStatements() {
        assertServiceException(() -> backendModelQueryService.validateSqlText("select 1; select 2"),
                BACKEND_MODEL_SQL_INVALID);
    }

    @Test
    public void testValidateSqlText_blank() {
        assertServiceException(() -> backendModelQueryService.validateSqlText(" "), BACKEND_MODEL_SQL_INVALID);
    }

    @Test
    public void testBuildPageSql_mysql() {
        String sql = backendModelQueryService.buildPageSql("jdbc:mysql://127.0.0.1:3306/test",
                "select id from system_user", 2, 10);
        assertEquals("SELECT * FROM (select id from system_user) model_page LIMIT 10 OFFSET 10", sql);
    }

    @Test
    public void testBuildPageSql_postgresql() {
        String sql = backendModelQueryService.buildPageSql("jdbc:postgresql://127.0.0.1:5432/test",
                "select id from system_user", 3, 20);
        assertEquals("SELECT * FROM (select id from system_user) model_page LIMIT 20 OFFSET 40", sql);
    }

    @Test
    public void testBuildPageSql_oracle() {
        String sql = backendModelQueryService.buildPageSql("jdbc:oracle:thin:@127.0.0.1:1521:xe",
                "select id from system_user", 1, 10);
        assertEquals("SELECT * FROM (select id from system_user) model_page OFFSET 0 ROWS FETCH NEXT 10 ROWS ONLY", sql);
    }

    @Test
    public void testBuildPageSql_sqlServer() {
        String sql = backendModelQueryService.buildPageSql("jdbc:sqlserver://127.0.0.1:1433;databaseName=test",
                "select id from system_user", 2, 10);
        assertEquals("SELECT * FROM (select id from system_user) model_page ORDER BY (SELECT NULL) OFFSET 10 ROWS FETCH NEXT 10 ROWS ONLY", sql);
    }

    @Test
    public void testBuildPageSql_notSupport() {
        assertServiceException(() -> backendModelQueryService.buildPageSql("jdbc:h2:mem:test",
                "select id from system_user", 1, 10), BACKEND_MODEL_DB_NOT_SUPPORT);
    }

}
