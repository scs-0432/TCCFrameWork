package com.scs.common.threadLocal;

import com.scs.common.bean.TransactionBean;
import com.scs.common.bean.TransactionContext;

/**
 * @author 大菠萝
 * @date 2023/02/12 16:18
 **/
public class TransactionThreadLocal {
    private final ThreadLocal<TransactionContext> CONTEXT_THREAD_LOCAL = new ThreadLocal<>();
    private final ThreadLocal<TransactionBean> BEAN_THREAD_LOCAL = new ThreadLocal<>();
    private static final TransactionThreadLocal TRANSACTION_CONTEXT_THREAD_LOCAL = new TransactionThreadLocal();

    private TransactionThreadLocal() {

    }
    public static TransactionThreadLocal getInstance() {
        return TRANSACTION_CONTEXT_THREAD_LOCAL;
    }

    public void setContext(final TransactionContext context) {
        CONTEXT_THREAD_LOCAL.set(context);

    }
    public  void setBean(final TransactionBean bean) {
        BEAN_THREAD_LOCAL.set(bean);

    }

    public TransactionContext getContext() {
        return CONTEXT_THREAD_LOCAL.get();
    }

    public TransactionBean getBean() {
        return BEAN_THREAD_LOCAL.get();
    }

    public void removeContext() {
        CONTEXT_THREAD_LOCAL.remove();

    }

    public void removeBean() {
        BEAN_THREAD_LOCAL.remove();

    }
}