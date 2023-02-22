package com.scs.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Objects;
@RequiredArgsConstructor
@Getter
public enum PhaseEnum {
    /**
     * TCC READY阶段
     */
    READY(0,"READY准备阶段"),

    /**
     * TCC try阶段
     */
    TRYING(1,"try阶段"),

    /**
     * TCC confirm阶段
     */
    CONFIRMING(2,"confirm阶段"),

    /**
     * TCC cancel阶段
     */

    CANCELING(3,"cancel阶段");

    private final int code;

    private final String desc;

    public static PhaseEnum acquireByCode(final int code) {
        return Arrays.stream(PhaseEnum.values())
                .filter(v -> Objects.equals(v.getCode(), code))
                .findFirst().orElse(PhaseEnum.TRYING);
    }
}
