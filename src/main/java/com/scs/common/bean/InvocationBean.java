package com.scs.common.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 大菠萝
 * @date 2023/02/12 16:23
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class InvocationBean {
    private Class targetClass;

    private String methodName;

    private Class[] parameterTypes;

    private Object[] args;
}