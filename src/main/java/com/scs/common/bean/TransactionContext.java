package com.scs.common.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author 大菠萝
 * @date 2023/02/12 16:11
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionContext implements Serializable {


    private static final long serialVersionUID = 820716219294692232L;
    private String transId;
    private int phase;
    private int role;

}