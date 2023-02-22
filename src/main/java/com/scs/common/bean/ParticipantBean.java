package com.scs.common.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author 大菠萝
 * @date 2023/02/12 16:22
 **/
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ParticipantBean implements Serializable {
    private static final long serialVersionUID = -1928925820940645217L;
    private String transId;
    private InvocationBean confirmInvocation;
    private InvocationBean cancelInvocation;



}
