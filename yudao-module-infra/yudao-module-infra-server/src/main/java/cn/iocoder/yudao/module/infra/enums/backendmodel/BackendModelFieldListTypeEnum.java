package cn.iocoder.yudao.module.infra.enums.backendmodel;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BackendModelFieldListTypeEnum {

    TEXT("text"),
    NUMBER("number"),
    DATE("date"),
    DATETIME("datetime"),
    DICT("dict");

    private final String type;

}
