package com.scs.common;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RoleEnum {

    /**
     * TCC事务发起者（分布式事务事务的发起的调用）
     */
    INITIATOR(1, "发起者"),

    /**
     * TCC事务消费者（分布式事务跨进程rpc的调用）
     */
    CONSUMER(2, "消费者"),

    /**
     * TCC事务提供者（分布式事务rpc调用的实现方）
     */
    PROVIDER(3, "提供者"),

    /**
     * TCC事务提供者(本地嵌套事务) TODO 嵌套事务
     */
    NESTER(4,"嵌套者");

    private final int code;

    private final String desc;
}
