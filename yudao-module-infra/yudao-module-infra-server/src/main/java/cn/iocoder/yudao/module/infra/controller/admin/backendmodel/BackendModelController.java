package cn.iocoder.yudao.module.infra.controller.admin.backendmodel;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.infra.controller.admin.backendmodel.vo.*;
import cn.iocoder.yudao.module.infra.dal.dataobject.backendmodel.BackendModelDO;
import cn.iocoder.yudao.module.infra.enums.backendmodel.BackendModelFieldListTypeEnum;
import cn.iocoder.yudao.module.infra.service.backendmodel.BackendModelQueryService;
import cn.iocoder.yudao.module.infra.service.backendmodel.BackendModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;

@Tag(name = "管理后台 - 后台模型")
@RestController
@RequestMapping("/infra/backend-model")
@Validated
public class BackendModelController {

    @Resource
    private BackendModelService backendModelService;
    @Resource
    private BackendModelQueryService backendModelQueryService;

    @PostMapping("/create")
    @Operation(summary = "创建后台模型")
    @PreAuthorize("@ss.hasPermission('infra:backend-model:create')")
    public CommonResult<Long> createBackendModel(@Valid @RequestBody BackendModelSaveReqVO createReqVO) {
        return success(backendModelService.createBackendModel(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新后台模型")
    @PreAuthorize("@ss.hasPermission('infra:backend-model:update')")
    public CommonResult<Boolean> updateBackendModel(@Valid @RequestBody BackendModelSaveReqVO updateReqVO) {
        backendModelService.updateBackendModel(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-fields")
    @Operation(summary = "更新后台模型字段配置")
    @PreAuthorize("@ss.hasPermission('infra:backend-model:update')")
    public CommonResult<Boolean> updateBackendModelFields(@Valid @RequestBody BackendModelFieldUpdateReqVO updateReqVO) {
        backendModelService.updateBackendModelFieldList(updateReqVO.getId(), updateReqVO.getFields());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除后台模型")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('infra:backend-model:delete')")
    public CommonResult<Boolean> deleteBackendModel(@RequestParam("id") Long id) {
        backendModelService.deleteBackendModel(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除后台模型")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('infra:backend-model:delete')")
    public CommonResult<Boolean> deleteBackendModelList(@RequestParam("ids") List<Long> ids) {
        backendModelService.deleteBackendModelList(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得后台模型")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('infra:backend-model:query')")
    public CommonResult<BackendModelRespVO> getBackendModel(@RequestParam("id") Long id) {
        BackendModelDO backendModel = backendModelService.getBackendModel(id);
        BackendModelRespVO respVO = BeanUtils.toBean(backendModel, BackendModelRespVO.class);
        respVO.setFields(BeanUtils.toBean(backendModelService.getBackendModelFieldList(id), BackendModelFieldRespVO.class));
        return success(respVO);
    }

    @GetMapping(value = "/page", params = "!id")
    @Operation(summary = "获得后台模型分页")
    @PreAuthorize("@ss.hasPermission('infra:backend-model:query')")
    public CommonResult<PageResult<BackendModelRespVO>> getBackendModelPage(@Valid BackendModelPageReqVO pageReqVO) {
        PageResult<BackendModelDO> pageResult = backendModelService.getBackendModelPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, BackendModelRespVO.class));
    }

    @GetMapping(value = "/page", params = "id")
    @Operation(summary = "执行后台模型分页查询")
    @PreAuthorize("@ss.hasPermission('infra:backend-model:query')")
    public CommonResult<BackendModelQueryRespVO> getBackendModelQueryPage(@RequestParam("id") Long id,
                                                                          @RequestParam("pageNo") Integer pageNo,
                                                                          @RequestParam("pageSize") Integer pageSize,
                                                                          @RequestParam Map<String, String> params) {
        return success(backendModelQueryService.getBackendModelPage(id, pageNo, pageSize, buildQueryParams(params)));
    }

    @GetMapping("/query-page")
    @Operation(summary = "执行后台模型分页查询")
    @PreAuthorize("@ss.hasPermission('infra:backend-model:query')")
    public CommonResult<BackendModelQueryRespVO> getBackendModelQueryPageAlias(@RequestParam("id") Long id,
                                                                               @RequestParam("pageNo") Integer pageNo,
                                                                               @RequestParam("pageSize") Integer pageSize,
                                                                               @RequestParam Map<String, String> params) {
        return success(backendModelQueryService.getBackendModelPage(id, pageNo, pageSize, buildQueryParams(params)));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出后台模型查询结果")
    @PreAuthorize("@ss.hasPermission('infra:backend-model:query')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportBackendModel(@RequestParam("id") Long id,
                                   @RequestParam Map<String, String> params,
                                   HttpServletResponse response) throws IOException {
        BackendModelQueryRespVO queryRespVO = backendModelQueryService.getBackendModelList(id, buildQueryParams(params));
        List<BackendModelQueryRespVO.Field> fields = queryRespVO.getFields().stream()
                .filter(field -> field.getListVisible() == null || field.getListVisible())
                .toList();
        List<List<String>> head = fields.stream().map(field -> List.of(field.getLabel())).toList();
        List<List<Object>> data = new ArrayList<>();
        for (Map<String, Object> row : queryRespVO.getPageResult().getList()) {
            List<Object> rowData = new ArrayList<>();
            for (BackendModelQueryRespVO.Field field : fields) {
                rowData.add(formatExportValue(field, row.get(field.getName())));
            }
            data.add(rowData);
        }
        ExcelUtils.write(response, "后台模型数据.xls", "数据", head, data);
    }

    @PostMapping("/preview")
    @Operation(summary = "预览后台模型查询")
    @PreAuthorize("@ss.hasPermission('infra:backend-model:query')")
    public CommonResult<BackendModelQueryRespVO> previewBackendModel(@Valid @RequestBody BackendModelPreviewReqVO reqVO) {
        return success(backendModelQueryService.previewBackendModel(reqVO));
    }

    private Map<String, String> buildQueryParams(Map<String, String> params) {
        Map<String, String> queryParams = new HashMap<>(params);
        queryParams.remove("id");
        queryParams.remove("pageNo");
        queryParams.remove("pageSize");
        return queryParams;
    }

    private Object formatExportValue(BackendModelQueryRespVO.Field field, Object value) {
        if (value == null) {
            return null;
        }
        if (BackendModelFieldListTypeEnum.DICT.getType().equals(field.getListType())) {
            return formatDictExportValue(field, value);
        }
        if (!BackendModelFieldListTypeEnum.DATE.getType().equals(field.getListType())
                && !BackendModelFieldListTypeEnum.DATETIME.getType().equals(field.getListType())) {
            return value;
        }
        LocalDateTime dateTime = parseDateTime(value);
        if (dateTime == null) {
            return value;
        }
        if (BackendModelFieldListTypeEnum.DATE.getType().equals(field.getListType())) {
            return dateTime.toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private Object formatDictExportValue(BackendModelQueryRespVO.Field field, Object value) {
        if (StrUtil.isBlank(field.getDictType())) {
            return value;
        }
        String text = value.toString();
        if (StrUtil.isBlank(text)) {
            return value;
        }
        List<String> values = text.contains(",") ? StrUtil.splitTrim(text, ",") : List.of(text);
        List<String> labels = new ArrayList<>(values.size());
        for (String item : values) {
            String label = DictFrameworkUtils.parseDictDataLabel(field.getDictType(), item);
            labels.add(StrUtil.blankToDefault(label, item));
        }
        return String.join(",", labels);
    }

    private LocalDateTime parseDateTime(Object value) {
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof LocalDate localDate) {
            return localDate.atStartOfDay();
        }
        if (value instanceof java.sql.Date date) {
            return date.toLocalDate().atStartOfDay();
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        if (value instanceof Date date) {
            return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        }
        if (value instanceof Number number) {
            return parseNumberDateTime(number.longValue());
        }
        String text = value.toString();
        if (StrUtil.isBlank(text)) {
            return null;
        }
        if (text.matches("\\d+")) {
            return parseNumberDateTime(Long.parseLong(text));
        }
        try {
            return LocalDateTime.parse(text.replace(" ", "T"));
        } catch (Exception ignored) {
            try {
                return LocalDate.parse(text).atStartOfDay();
            } catch (Exception ignoredAgain) {
                return null;
            }
        }
    }

    private LocalDateTime parseNumberDateTime(Long value) {
        String text = value.toString();
        if (text.matches("\\d{8}")) {
            try {
                return LocalDate.parse(text, DateTimeFormatter.ofPattern("yyyyMMdd")).atStartOfDay();
            } catch (Exception ignored) {
                return null;
            }
        }
        long millis = text.length() == 10 ? value * 1000 : value;
        try {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
        } catch (Exception ignored) {
            return null;
        }
    }

}
