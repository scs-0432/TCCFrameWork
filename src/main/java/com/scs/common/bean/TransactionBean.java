package com.scs.common.bean;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author 大菠萝
 * @date 2023/02/12 16:20
 **/
@NoArgsConstructor
@Data
public class TransactionBean implements Serializable {

    private static final long serialVersionUID = 1191201537488690822L;
    /**
     * 事务id
     */
    private String transId;

    /**
     * 阶段
     */
    private Integer phase;

    /**
     * 角色
     */
    private Integer role;

    /**
     * 失败重试次数
     */
    private Integer retryTimes;

    /**
     * 版本
     */
    private Integer version;

    /**
     * 目标类
     */
    private String targetClass;

    /**
     * 目标方法
     */
    private String targetMethod;

    /**
     * confirm方法
     */
    private String confirmMethod;

    /**
     * cancel方法
     */
    private String cancelMethod;

    /**
     * createTime
     */
    private Date createTime;

    /**
     * updateTime
     */
    private Date updateTime;

    private List<ParticipantBean> participantBeanList;


    public TransactionBean(String transId){
        this.transId = transId;
        participantBeanList = new ArrayList<>();
    }

    /**
     * 添加参与者
     * @param miloParticipantBean {@linkplain MiloParticipantBean}
     */
    public void addParticipant(final ParticipantBean participantBean){
        if(participantBeanList == null){
            participantBeanList = new ArrayList<>();
        }
        participantBeanList.add(participantBean);
    }


}