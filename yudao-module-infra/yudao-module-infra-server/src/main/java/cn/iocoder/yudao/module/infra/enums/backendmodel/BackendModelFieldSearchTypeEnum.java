package cn.iocoder.yudao.module.infra.enums.backendmodel;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BackendModelFieldSearchTypeEnum {

    TEXT("text"),
    NUMBER("number"),
    DATE("date"),
    DATE_RANGE("date_range"),
    DICT("dict");

    private final String type;

}
