package cn.iocoder.yudao.module.infra.dal.dataobject.backendmodel;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.infra.enums.backendmodel.BackendModelFieldListTypeEnum;
import cn.iocoder.yudao.module.infra.enums.backendmodel.BackendModelFieldSearchOperatorEnum;
import cn.iocoder.yudao.module.infra.enums.backendmodel.BackendModelFieldSearchTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 后台模型字段 DO
 */
@TableName("infra_backend_model_field")
@KeySequence("infra_backend_model_field_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@TenantIgnore
public class BackendModelFieldDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 后台模型编号
     */
    private Long backendModelId;

    /**
     * 字段名，对应 SQL 返回列名或别名
     */
    private String fieldName;

    /**
     * 字段显示名
     */
    private String fieldLabel;

    /**
     * 字段顺序
     */
    private Integer fieldOrder;

    /**
     * 是否在列表展示
     */
    private Boolean listVisible;

    /**
     * 列表展示类型
     *
     * 枚举 {@link BackendModelFieldListTypeEnum}
     */
    private String listType;

    /**
     * 是否作为检索条件
     */
    private Boolean searchable;

    /**
     * 检索控件类型
     *
     * 枚举 {@link BackendModelFieldSearchTypeEnum}
     */
    private String searchType;

    /**
     * 检索操作符
     *
     * 枚举 {@link BackendModelFieldSearchOperatorEnum}
     */
    private String searchOperator;

    /**
     * 字典类型，仅字典下拉时使用
     */
    private String dictType;

    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
