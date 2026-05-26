package cn.iocoder.yudao.module.infra.service.backendmodel;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.infra.controller.admin.backendmodel.vo.BackendModelPreviewReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.backendmodel.vo.BackendModelQueryRespVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.backendmodel.BackendModelDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.db.DataSourceConfigDO;
import cn.iocoder.yudao.module.infra.dal.mysql.backendmodel.BackendModelMapper;
import cn.iocoder.yudao.module.infra.service.db.DataSourceConfigService;
import jakarta.annotation.Resource;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.select.Select;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.*;

@Service
public class BackendModelQueryServiceImpl implements BackendModelQueryService {

    @Resource
    private BackendModelMapper backendModelMapper;
    @Resource
    private DataSourceConfigService dataSourceConfigService;

    @Override
    public BackendModelQueryRespVO getBackendModelPage(Long id, Integer pageNo, Integer pageSize) {
        BackendModelDO backendModel = backendModelMapper.selectById(id);
        if (backendModel == null) {
            throw exception(BACKEND_MODEL_NOT_EXISTS);
        }
        if (!CommonStatusEnum.isEnable(backendModel.getStatus())) {
            throw exception(BACKEND_MODEL_DISABLED);
        }
        return executeQuery(backendModel.getDataSourceConfigId(), backendModel.getSqlText(), pageNo, pageSize);
    }

    @Override
    public BackendModelQueryRespVO previewBackendModel(BackendModelPreviewReqVO reqVO) {
        return executeQuery(reqVO.getDataSourceConfigId(), reqVO.getSqlText(), reqVO.getPageNo(), reqVO.getPageSize());
    }

    @Override
    public void validateSqlText(String sqlText) {
        String normalizedSql = normalizeSql(sqlText);
        try {
            Statements statements = CCJSqlParserUtil.parseStatements(normalizedSql);
            if (statements.size() != 1) {
                throw exception(BACKEND_MODEL_SQL_INVALID);
            }
            Statement statement = statements.get(0);
            if (!(statement instanceof Select)) {
                throw exception(BACKEND_MODEL_SQL_ONLY_SELECT);
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw exception(BACKEND_MODEL_SQL_INVALID);
        }
    }

    @Override
    public String buildPageSql(String url, String sqlText, Integer pageNo, Integer pageSize) {
        String sql = normalizeSql(sqlText);
        long offset = (long) (pageNo - 1) * pageSize;
        String wrappedSql = "SELECT * FROM (" + sql + ") model_page";
        DatabaseDialect dialect = parseDialect(url);
        return switch (dialect) {
            case MYSQL, POSTGRESQL -> wrappedSql + " LIMIT " + pageSize + " OFFSET " + offset;
            case ORACLE -> wrappedSql + " OFFSET " + offset + " ROWS FETCH NEXT " + pageSize + " ROWS ONLY";
            case SQL_SERVER -> wrappedSql + " ORDER BY (SELECT NULL) OFFSET " + offset + " ROWS FETCH NEXT "
                    + pageSize + " ROWS ONLY";
        };
    }

    private BackendModelQueryRespVO executeQuery(Long dataSourceConfigId, String sqlText, Integer pageNo, Integer pageSize) {
        validateSqlText(sqlText);
        DataSourceConfigDO dataSourceConfig = dataSourceConfigService.getDataSourceConfig(dataSourceConfigId);
        if (dataSourceConfig == null) {
            throw exception(DATA_SOURCE_CONFIG_NOT_EXISTS);
        }

        try (Connection connection = DriverManager.getConnection(dataSourceConfig.getUrl(),
                dataSourceConfig.getUsername(), dataSourceConfig.getPassword())) {
            Long total = queryTotal(connection, sqlText);
            QueryRowsResult rowsResult = queryRows(connection, dataSourceConfig.getUrl(), sqlText, pageNo, pageSize);
            BackendModelQueryRespVO respVO = new BackendModelQueryRespVO();
            respVO.setFields(rowsResult.fields());
            respVO.setPageResult(new PageResult<>(rowsResult.rows(), total));
            return respVO;
        } catch (SQLException ex) {
            throw exception(BACKEND_MODEL_SQL_EXECUTE_FAIL, ex.getMessage());
        }
    }

    private Long queryTotal(Connection connection, String sqlText) throws SQLException {
        String countSql = "SELECT COUNT(*) FROM (" + normalizeSql(sqlText) + ") model_count";
        try (PreparedStatement statement = connection.prepareStatement(countSql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getLong(1) : 0L;
        }
    }

    private QueryRowsResult queryRows(Connection connection, String url, String sqlText,
                                     Integer pageNo, Integer pageSize) throws SQLException {
        String pageSql = buildPageSql(url, sqlText, pageNo, pageSize);
        try (PreparedStatement statement = connection.prepareStatement(pageSql);
             ResultSet resultSet = statement.executeQuery()) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            List<BackendModelQueryRespVO.Field> fields = buildFields(metaData);
            List<Map<String, Object>> rows = new ArrayList<>();
            while (resultSet.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 0; i < fields.size(); i++) {
                    BackendModelQueryRespVO.Field field = fields.get(i);
                    row.put(field.getName(), resultSet.getObject(i + 1));
                }
                rows.add(row);
            }
            return new QueryRowsResult(fields, rows);
        }
    }

    private List<BackendModelQueryRespVO.Field> buildFields(ResultSetMetaData metaData) throws SQLException {
        List<BackendModelQueryRespVO.Field> fields = new ArrayList<>(metaData.getColumnCount());
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String name = StrUtil.blankToDefault(metaData.getColumnLabel(i), metaData.getColumnName(i));
            fields.add(new BackendModelQueryRespVO.Field(name, name));
        }
        return fields;
    }

    private String normalizeSql(String sqlText) {
        String sql = StrUtil.trim(sqlText);
        if (StrUtil.isBlank(sql)) {
            throw exception(BACKEND_MODEL_SQL_INVALID);
        }
        while (sql.endsWith(";")) {
            sql = StrUtil.trim(sql.substring(0, sql.length() - 1));
        }
        return sql;
    }

    private DatabaseDialect parseDialect(String url) {
        String lowerUrl = StrUtil.nullToEmpty(url).toLowerCase(Locale.ROOT);
        if (lowerUrl.startsWith("jdbc:mysql:") || lowerUrl.startsWith("jdbc:mariadb:")) {
            return DatabaseDialect.MYSQL;
        }
        if (lowerUrl.startsWith("jdbc:postgresql:") || lowerUrl.startsWith("jdbc:kingbase:")
                || lowerUrl.startsWith("jdbc:opengauss:")) {
            return DatabaseDialect.POSTGRESQL;
        }
        if (lowerUrl.startsWith("jdbc:oracle:")) {
            return DatabaseDialect.ORACLE;
        }
        if (lowerUrl.startsWith("jdbc:sqlserver:")) {
            return DatabaseDialect.SQL_SERVER;
        }
        throw exception(BACKEND_MODEL_DB_NOT_SUPPORT);
    }

    private enum DatabaseDialect {
        MYSQL,
        POSTGRESQL,
        ORACLE,
        SQL_SERVER
    }

    private record QueryRowsResult(List<BackendModelQueryRespVO.Field> fields, List<Map<String, Object>> rows) {
    }

}
