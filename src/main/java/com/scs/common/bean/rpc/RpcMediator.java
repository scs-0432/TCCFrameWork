package com.scs.common.bean.rpc;

import com.google.gson.Gson;
import com.scs.common.bean.TransactionContext;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;


public class RpcMediator {
    private static final RpcMediator rpcMediator = new RpcMediator();
    private final String TRANSACTION_CONTEXT = "transaction_context";
    private final Gson gson = new Gson();

    public static RpcMediator getRpcMediator() {
        return rpcMediator;
    }


    public void transfer(RequestTemplate requestTemplate, TransactionContext context) {
        if (Objects.nonNull(context)) {
            requestTemplate.header(TRANSACTION_CONTEXT, gson.toJson(context));
        }
    }

    public TransactionContext getTransactionContext(RequestAttributes requestAttributes) {
        TransactionContext transactionContext = null;
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        String header = request.getHeader(TRANSACTION_CONTEXT);
        if (Objects.nonNull(header)) {
            transactionContext = gson.fromJson(header, TransactionContext.class);
        }
        return transactionContext;
    }

}
