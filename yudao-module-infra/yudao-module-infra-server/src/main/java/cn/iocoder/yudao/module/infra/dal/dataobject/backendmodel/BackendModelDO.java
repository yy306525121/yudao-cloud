package cn.iocoder.yudao.module.infra.dal.dataobject.backendmodel;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.infra.dal.dataobject.db.DataSourceConfigDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 后台模型 DO
 */
@TableName("infra_backend_model")
@KeySequence("infra_backend_model_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@TenantIgnore
public class BackendModelDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 模型名称
     */
    private String name;
    /**
     * 数据源编号
     *
     * 关联 {@link DataSourceConfigDO#getId()}
     */
    private Long dataSourceConfigId;
    /**
     * 只读查询 SQL
     */
    private String sqlText;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
