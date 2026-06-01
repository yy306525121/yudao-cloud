package cn.iocoder.yudao.module.infra.service.backendmodel;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.infra.controller.admin.backendmodel.vo.BackendModelPreviewReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.backendmodel.vo.BackendModelQueryRespVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.backendmodel.BackendModelDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.backendmodel.BackendModelFieldDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.db.DataSourceConfigDO;
import cn.iocoder.yudao.module.infra.dal.mysql.backendmodel.BackendModelFieldMapper;
import cn.iocoder.yudao.module.infra.dal.mysql.backendmodel.BackendModelMapper;
import cn.iocoder.yudao.module.infra.enums.backendmodel.BackendModelFieldListTypeEnum;
import cn.iocoder.yudao.module.infra.enums.backendmodel.BackendModelFieldSearchOperatorEnum;
import cn.iocoder.yudao.module.infra.enums.backendmodel.BackendModelFieldSearchTypeEnum;
import cn.iocoder.yudao.module.infra.service.db.DataSourceConfigService;
import jakarta.annotation.Resource;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.select.Select;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.*;

@Service
public class BackendModelQueryServiceImpl implements BackendModelQueryService {

    @Resource
    private BackendModelMapper backendModelMapper;
    @Resource
    private BackendModelFieldMapper backendModelFieldMapper;
    @Resource
    private DataSourceConfigService dataSourceConfigService;

    @Override
    public BackendModelQueryRespVO getBackendModelPage(Long id, Integer pageNo, Integer pageSize,
                                                       Map<String, String> queryParams) {
        BackendModelDO backendModel = validateEnabledBackendModel(id);
        List<BackendModelFieldDO> configuredFields = backendModelFieldMapper.selectListByBackendModelId(id);
        return executeQuery(backendModel.getDataSourceConfigId(), backendModel.getSqlText(), pageNo, pageSize,
                queryParams, configuredFields);
    }

    @Override
    public BackendModelQueryRespVO getBackendModelList(Long id, Map<String, String> queryParams) {
        BackendModelDO backendModel = validateEnabledBackendModel(id);
        List<BackendModelFieldDO> configuredFields = backendModelFieldMapper.selectListByBackendModelId(id);
        return executeListQuery(backendModel.getDataSourceConfigId(), backendModel.getSqlText(), queryParams, configuredFields);
    }

    private BackendModelDO validateEnabledBackendModel(Long id) {
        BackendModelDO backendModel = backendModelMapper.selectById(id);
        if (backendModel == null) {
            throw exception(BACKEND_MODEL_NOT_EXISTS);
        }
        if (!CommonStatusEnum.isEnable(backendModel.getStatus())) {
            throw exception(BACKEND_MODEL_DISABLED);
        }
        return backendModel;
    }

    @Override
    public BackendModelQueryRespVO previewBackendModel(BackendModelPreviewReqVO reqVO) {
        return executeQuery(reqVO.getDataSourceConfigId(), reqVO.getSqlText(), reqVO.getPageNo(), reqVO.getPageSize(),
                Collections.emptyMap(), Collections.emptyList());
    }

    @Override
    public List<BackendModelQueryRespVO.Field> inferFields(Long dataSourceConfigId, String sqlText) {
        return executeQuery(dataSourceConfigId, sqlText, 1, 1, Collections.emptyMap(), Collections.emptyList()).getFields();
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

    private BackendModelQueryRespVO executeQuery(Long dataSourceConfigId, String sqlText, Integer pageNo, Integer pageSize,
                                                Map<String, String> queryParams,
                                                List<BackendModelFieldDO> configuredFields) {
        validateSqlText(sqlText);
        DataSourceConfigDO dataSourceConfig = dataSourceConfigService.getDataSourceConfig(dataSourceConfigId);
        if (dataSourceConfig == null) {
            throw exception(DATA_SOURCE_CONFIG_NOT_EXISTS);
        }

        try (Connection connection = DriverManager.getConnection(dataSourceConfig.getUrl(),
                dataSourceConfig.getUsername(), dataSourceConfig.getPassword())) {
            QueryCondition queryCondition = buildQueryCondition(sqlText, queryParams, configuredFields);
            Long total = queryTotal(connection, queryCondition);
            QueryRowsResult rowsResult = queryRows(connection, dataSourceConfig.getUrl(), queryCondition, pageNo, pageSize);
            BackendModelQueryRespVO respVO = new BackendModelQueryRespVO();
            respVO.setFields(buildResponseFields(configuredFields, rowsResult.fields()));
            respVO.setPageResult(new PageResult<>(rowsResult.rows(), total));
            return respVO;
        } catch (SQLException ex) {
            throw exception(BACKEND_MODEL_SQL_EXECUTE_FAIL, ex.getMessage());
        }
    }

    private BackendModelQueryRespVO executeListQuery(Long dataSourceConfigId, String sqlText,
                                                    Map<String, String> queryParams,
                                                    List<BackendModelFieldDO> configuredFields) {
        validateSqlText(sqlText);
        DataSourceConfigDO dataSourceConfig = dataSourceConfigService.getDataSourceConfig(dataSourceConfigId);
        if (dataSourceConfig == null) {
            throw exception(DATA_SOURCE_CONFIG_NOT_EXISTS);
        }

        try (Connection connection = DriverManager.getConnection(dataSourceConfig.getUrl(),
                dataSourceConfig.getUsername(), dataSourceConfig.getPassword())) {
            QueryCondition queryCondition = buildQueryCondition(sqlText, queryParams, configuredFields);
            QueryRowsResult rowsResult = queryRows(connection, queryCondition);
            BackendModelQueryRespVO respVO = new BackendModelQueryRespVO();
            respVO.setFields(buildResponseFields(configuredFields, rowsResult.fields()));
            respVO.setPageResult(new PageResult<>(rowsResult.rows(), (long) rowsResult.rows().size()));
            return respVO;
        } catch (SQLException ex) {
            throw exception(BACKEND_MODEL_SQL_EXECUTE_FAIL, ex.getMessage());
        }
    }

    private Long queryTotal(Connection connection, QueryCondition queryCondition) throws SQLException {
        String countSql = "SELECT COUNT(*) FROM (" + queryCondition.sql() + ") model_count";
        try (PreparedStatement statement = connection.prepareStatement(countSql)) {
            fillParameters(statement, queryCondition.params());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getLong(1) : 0L;
            }
        }
    }

    private QueryRowsResult queryRows(Connection connection, String url, QueryCondition queryCondition,
                                     Integer pageNo, Integer pageSize) throws SQLException {
        String pageSql = buildPageSql(url, queryCondition.sql(), pageNo, pageSize);
        try (PreparedStatement statement = connection.prepareStatement(pageSql)) {
            fillParameters(statement, queryCondition.params());
            try (ResultSet resultSet = statement.executeQuery()) {
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
    }

    private QueryRowsResult queryRows(Connection connection, QueryCondition queryCondition) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(queryCondition.sql())) {
            fillParameters(statement, queryCondition.params());
            try (ResultSet resultSet = statement.executeQuery()) {
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
    }

    private QueryCondition buildQueryCondition(String sqlText, Map<String, String> queryParams,
                                               List<BackendModelFieldDO> configuredFields) {
        String sql = "SELECT * FROM (" + normalizeSql(sqlText) + ") model_query";
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        for (BackendModelFieldDO field : configuredFields) {
            if (!CommonStatusEnum.isEnable(field.getStatus()) || !Boolean.TRUE.equals(field.getSearchable())) {
                continue;
            }
            appendCondition(field, queryParams == null ? Collections.emptyMap() : queryParams, conditions, params);
        }
        if (conditions.isEmpty()) {
            return new QueryCondition(sql, params);
        }
        return new QueryCondition(sql + " WHERE " + String.join(" AND ", conditions), params);
    }

    private void appendCondition(BackendModelFieldDO field, Map<String, String> queryParams,
                                 List<String> conditions, List<Object> params) {
        String fieldName = field.getFieldName();
        if (!Pattern.matches("[A-Za-z_][A-Za-z0-9_]*", fieldName)) {
            throw exception(BACKEND_MODEL_SQL_INVALID);
        }
        String operator = StrUtil.blankToDefault(field.getSearchOperator(), BackendModelFieldSearchOperatorEnum.LIKE.getOperator());
        String column = "model_query." + fieldName;
        if (BackendModelFieldSearchOperatorEnum.BETWEEN.getOperator().equals(operator)) {
            String begin = queryParams.get(fieldName + "Begin");
            String end = queryParams.get(fieldName + "End");
            if (StrUtil.isBlank(begin) || StrUtil.isBlank(end)) {
                return;
            }
            conditions.add(column + " BETWEEN ? AND ?");
            params.add(begin);
            params.add(end);
            return;
        }
        String value = queryParams.get(fieldName);
        if (StrUtil.isBlank(value)) {
            return;
        }
        switch (operator) {
            case "eq" -> {
                conditions.add(column + " = ?");
                params.add(value);
            }
            case "gt" -> {
                conditions.add(column + " > ?");
                params.add(value);
            }
            case "ge" -> {
                conditions.add(column + " >= ?");
                params.add(value);
            }
            case "lt" -> {
                conditions.add(column + " < ?");
                params.add(value);
            }
            case "le" -> {
                conditions.add(column + " <= ?");
                params.add(value);
            }
            case "in" -> {
                List<String> values = StrUtil.splitTrim(value, ",");
                if (values.isEmpty()) {
                    return;
                }
                conditions.add(column + " IN (" + String.join(",", Collections.nCopies(values.size(), "?")) + ")");
                params.addAll(values);
            }
            default -> {
                conditions.add(column + " LIKE ?");
                params.add("%" + value + "%");
            }
        }
    }

    private void fillParameters(PreparedStatement statement, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            statement.setObject(i + 1, params.get(i));
        }
    }

    private List<BackendModelQueryRespVO.Field> buildResponseFields(List<BackendModelFieldDO> configuredFields,
                                                                    List<BackendModelQueryRespVO.Field> inferredFields) {
        List<BackendModelFieldDO> enabledFields = configuredFields.stream()
                .filter(field -> CommonStatusEnum.isEnable(field.getStatus()))
                .toList();
        if (enabledFields.isEmpty()) {
            return inferredFields;
        }
        return enabledFields.stream().map(this::buildField).toList();
    }

    private BackendModelQueryRespVO.Field buildField(BackendModelFieldDO field) {
        BackendModelQueryRespVO.Field respField = new BackendModelQueryRespVO.Field();
        respField.setName(field.getFieldName());
        respField.setLabel(field.getFieldLabel());
        respField.setListVisible(field.getListVisible());
        respField.setListType(StrUtil.blankToDefault(field.getListType(), inferListType(field.getSearchType())));
        respField.setSearchable(field.getSearchable());
        respField.setSearchType(field.getSearchType());
        respField.setSearchOperator(field.getSearchOperator());
        respField.setDictType(field.getDictType());
        respField.setStatus(field.getStatus());
        return respField;
    }

    private List<BackendModelQueryRespVO.Field> buildFields(ResultSetMetaData metaData) throws SQLException {
        List<BackendModelQueryRespVO.Field> fields = new ArrayList<>(metaData.getColumnCount());
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String name = StrUtil.blankToDefault(metaData.getColumnLabel(i), metaData.getColumnName(i));
            BackendModelQueryRespVO.Field field = new BackendModelQueryRespVO.Field();
            field.setName(name);
            field.setLabel(name);
            field.setListVisible(true);
            field.setListType(BackendModelFieldListTypeEnum.TEXT.getType());
            field.setSearchable(false);
            field.setSearchType(BackendModelFieldSearchTypeEnum.TEXT.getType());
            field.setSearchOperator(BackendModelFieldSearchOperatorEnum.LIKE.getOperator());
            field.setStatus(CommonStatusEnum.ENABLE.getStatus());
            fields.add(field);
        }
        return fields;
    }

    private String inferListType(String searchType) {
        if (BackendModelFieldSearchTypeEnum.DATE.getType().equals(searchType)) {
            return BackendModelFieldListTypeEnum.DATE.getType();
        }
        if (BackendModelFieldSearchTypeEnum.DATE_RANGE.getType().equals(searchType)) {
            return BackendModelFieldListTypeEnum.DATETIME.getType();
        }
        return BackendModelFieldListTypeEnum.TEXT.getType();
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

    private record QueryCondition(String sql, List<Object> params) {
    }

}
