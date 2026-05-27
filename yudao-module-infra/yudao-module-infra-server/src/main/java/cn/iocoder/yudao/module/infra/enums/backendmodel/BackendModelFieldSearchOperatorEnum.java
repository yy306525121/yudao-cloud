package cn.iocoder.yudao.module.infra.enums.backendmodel;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BackendModelFieldSearchOperatorEnum {

    EQ("eq"),
    LIKE("like"),
    GT("gt"),
    GE("ge"),
    LT("lt"),
    LE("le"),
    BETWEEN("between"),
    IN("in");

    private final String operator;

}
