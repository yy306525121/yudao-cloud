package cn.iocoder.yudao.module.infra.controller.admin.backendmodel;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.infra.controller.admin.backendmodel.vo.*;
import cn.iocoder.yudao.module.infra.dal.dataobject.backendmodel.BackendModelDO;
import cn.iocoder.yudao.module.infra.service.backendmodel.BackendModelQueryService;
import cn.iocoder.yudao.module.infra.service.backendmodel.BackendModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

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
        return success(BeanUtils.toBean(backendModel, BackendModelRespVO.class));
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
                                                                          @RequestParam("pageSize") Integer pageSize) {
        return success(backendModelQueryService.getBackendModelPage(id, pageNo, pageSize));
    }

    @GetMapping("/query-page")
    @Operation(summary = "执行后台模型分页查询")
    @PreAuthorize("@ss.hasPermission('infra:backend-model:query')")
    public CommonResult<BackendModelQueryRespVO> getBackendModelQueryPageAlias(@RequestParam("id") Long id,
                                                                               @RequestParam("pageNo") Integer pageNo,
                                                                               @RequestParam("pageSize") Integer pageSize) {
        return success(backendModelQueryService.getBackendModelPage(id, pageNo, pageSize));
    }

    @PostMapping("/preview")
    @Operation(summary = "预览后台模型查询")
    @PreAuthorize("@ss.hasPermission('infra:backend-model:query')")
    public CommonResult<BackendModelQueryRespVO> previewBackendModel(@Valid @RequestBody BackendModelPreviewReqVO reqVO) {
        return success(backendModelQueryService.previewBackendModel(reqVO));
    }

}
