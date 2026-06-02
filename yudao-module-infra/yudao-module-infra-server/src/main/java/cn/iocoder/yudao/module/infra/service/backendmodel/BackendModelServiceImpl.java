package cn.iocoder.yudao.module.infra.service.backendmodel;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.infra.controller.admin.backendmodel.vo.BackendModelPageReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.backendmodel.vo.BackendModelSaveReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.backendmodel.vo.BackendModelFieldSaveReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.backendmodel.vo.BackendModelQueryRespVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.backendmodel.BackendModelDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.backendmodel.BackendModelFieldDO;
import cn.iocoder.yudao.module.infra.dal.mysql.backendmodel.BackendModelFieldMapper;
import cn.iocoder.yudao.module.infra.dal.mysql.backendmodel.BackendModelMapper;
import cn.iocoder.yudao.module.infra.enums.backendmodel.BackendModelFieldListTypeEnum;
import cn.iocoder.yudao.module.infra.enums.backendmodel.BackendModelFieldSearchOperatorEnum;
import cn.iocoder.yudao.module.infra.enums.backendmodel.BackendModelFieldSearchTypeEnum;
import cn.iocoder.yudao.module.infra.service.db.DataSourceConfigService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.enums.CommonStatusEnum.ENABLE;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.BACKEND_MODEL_NOT_EXISTS;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.DATA_SOURCE_CONFIG_NOT_EXISTS;

@Service
@Validated
public class BackendModelServiceImpl implements BackendModelService {

    @Resource
    private BackendModelMapper backendModelMapper;
    @Resource
    private BackendModelFieldMapper backendModelFieldMapper;
    @Resource
    private DataSourceConfigService dataSourceConfigService;
    @Resource
    private BackendModelQueryService backendModelQueryService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBackendModel(BackendModelSaveReqVO createReqVO) {
        validateDataSourceConfigExists(createReqVO.getDataSourceConfigId());
        backendModelQueryService.validateSqlText(createReqVO.getSqlText());
        List<BackendModelQueryRespVO.Field> inferredFields = backendModelQueryService
                .inferFields(createReqVO.getDataSourceConfigId(), createReqVO.getSqlText());

        BackendModelDO backendModel = BeanUtils.toBean(createReqVO, BackendModelDO.class);
        backendModelMapper.insert(backendModel);
        syncFields(backendModel.getId(), inferredFields, createReqVO.getFields());
        return backendModel.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBackendModel(BackendModelSaveReqVO updateReqVO) {
        validateBackendModelExists(updateReqVO.getId());
        validateDataSourceConfigExists(updateReqVO.getDataSourceConfigId());
        backendModelQueryService.validateSqlText(updateReqVO.getSqlText());
        List<BackendModelQueryRespVO.Field> inferredFields = backendModelQueryService
                .inferFields(updateReqVO.getDataSourceConfigId(), updateReqVO.getSqlText());

        BackendModelDO updateObj = BeanUtils.toBean(updateReqVO, BackendModelDO.class);
        backendModelMapper.updateById(updateObj);
        syncFields(updateReqVO.getId(), inferredFields, updateReqVO.getFields());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBackendModelFieldList(Long id, List<BackendModelFieldSaveReqVO> fields) {
        BackendModelDO backendModel = validateBackendModelExists(id);
        List<BackendModelQueryRespVO.Field> inferredFields = backendModelQueryService
                .inferFields(backendModel.getDataSourceConfigId(), backendModel.getSqlText());
        syncFields(id, inferredFields, fields);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBackendModel(Long id) {
        validateBackendModelExists(id);
        backendModelMapper.deleteById(id);
        backendModelFieldMapper.deleteByBackendModelId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBackendModelList(List<Long> ids) {
        backendModelMapper.deleteByIds(ids);
        backendModelFieldMapper.deleteByBackendModelIds(ids);
    }

    @Override
    public BackendModelDO getBackendModel(Long id) {
        return backendModelMapper.selectById(id);
    }

    @Override
    public List<BackendModelFieldDO> getBackendModelFieldList(Long backendModelId) {
        List<BackendModelFieldDO> fields = backendModelFieldMapper.selectListByBackendModelId(backendModelId);
        fields.forEach(field -> field.setListType(StrUtil.blankToDefault(field.getListType(),
                inferListType(field.getSearchType()))));
        return fields;
    }

    @Override
    public PageResult<BackendModelDO> getBackendModelPage(BackendModelPageReqVO pageReqVO) {
        return backendModelMapper.selectPage(pageReqVO);
    }

    @Override
    public BackendModelDO validateBackendModelExists(Long id) {
        BackendModelDO backendModel = backendModelMapper.selectById(id);
        if (backendModel == null) {
            throw exception(BACKEND_MODEL_NOT_EXISTS);
        }
        return backendModel;
    }

    private void validateDataSourceConfigExists(Long id) {
        if (dataSourceConfigService.getDataSourceConfig(id) == null) {
            throw exception(DATA_SOURCE_CONFIG_NOT_EXISTS);
        }
    }

    private void syncFields(Long backendModelId, List<BackendModelQueryRespVO.Field> inferredFields,
                            List<BackendModelFieldSaveReqVO> submittedFields) {
        List<BackendModelFieldDO> existingFields = backendModelFieldMapper.selectListByBackendModelId(backendModelId);
        Map<String, BackendModelFieldDO> existingFieldMap = existingFields.stream()
                .collect(Collectors.toMap(BackendModelFieldDO::getFieldName, Function.identity(), (a, b) -> a));
        Map<String, BackendModelFieldSaveReqVO> submittedFieldMap = submittedFields == null ? Map.of()
                : submittedFields.stream().filter(field -> field.getFieldName() != null)
                        .collect(Collectors.toMap(BackendModelFieldSaveReqVO::getFieldName, Function.identity(), (a, b) -> a));

        List<String> inferredFieldNames = new ArrayList<>();
        for (int i = 0; i < inferredFields.size(); i++) {
            BackendModelQueryRespVO.Field inferredField = inferredFields.get(i);
            String fieldName = inferredField.getName();
            inferredFieldNames.add(fieldName);
            BackendModelFieldDO existingField = existingFieldMap.get(fieldName);
            BackendModelFieldSaveReqVO submittedField = submittedFieldMap.get(fieldName);
            BackendModelFieldDO field = existingField == null ? new BackendModelFieldDO() : existingField;
            field.setBackendModelId(backendModelId);
            field.setFieldName(fieldName);
            field.setFieldLabel(submittedField != null ? submittedField.getFieldLabel()
                    : Objects.requireNonNullElseGet(field.getFieldLabel(), () -> inferredField.getLabel()));
            field.setFieldOrder(submittedField != null ? submittedField.getFieldOrder()
                    : Objects.requireNonNullElse(field.getFieldOrder(), i + 1));
            field.setListVisible(submittedField != null ? submittedField.getListVisible()
                    : Objects.requireNonNullElse(field.getListVisible(), true));
            field.setListType(submittedField != null ? StrUtil.blankToDefault(submittedField.getListType(),
                    inferListType(submittedField.getSearchType())) : Objects.requireNonNullElseGet(field.getListType(),
                    () -> inferListType(field.getSearchType())));
            field.setSearchable(submittedField != null ? submittedField.getSearchable()
                    : Objects.requireNonNullElse(field.getSearchable(), false));
            field.setSearchType(submittedField != null ? submittedField.getSearchType()
                    : Objects.requireNonNullElse(field.getSearchType(), BackendModelFieldSearchTypeEnum.TEXT.getType()));
            field.setSearchOperator(submittedField != null ? submittedField.getSearchOperator()
                    : Objects.requireNonNullElse(field.getSearchOperator(), BackendModelFieldSearchOperatorEnum.LIKE.getOperator()));
            field.setDictType(submittedField != null ? submittedField.getDictType() : field.getDictType());
            field.setStatus(submittedField != null ? submittedField.getStatus()
                    : Objects.requireNonNullElse(field.getStatus(), ENABLE.getStatus()));
            if (field.getId() == null) {
                backendModelFieldMapper.insert(field);
            } else {
                backendModelFieldMapper.updateById(field);
            }
        }

        existingFields.stream()
                .filter(field -> !inferredFieldNames.contains(field.getFieldName()))
                .forEach(field -> backendModelFieldMapper.deleteById(field.getId()));
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

}
