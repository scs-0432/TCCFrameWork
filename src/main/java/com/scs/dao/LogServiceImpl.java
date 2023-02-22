package com.scs.dao;

import com.google.gson.Gson;
import com.scs.common.bean.ParticipantBean;
import com.scs.common.bean.TransactionBean;
import com.scs.common.config.DbConfig;
import com.scs.common.config.FrameConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author 大菠萝
 * @date 2023/02/12 19:38
 **/
@Service
@Slf4j
public class LogServiceImpl implements LogService {
    /**
     * 数据源
     */
    private DataSource dataSource;

    /**
     * 表名
     */
    private String tableName;

    private final Gson gson = new Gson();



    @Override
    public void init(FrameConfig config) {

        HikariDataSource hikariDataSource = new HikariDataSource();
        DbConfig dbConfig = config.getDbConfig();
        hikariDataSource.setJdbcUrl(dbConfig.getUrl());
        hikariDataSource.setDriverClassName(dbConfig.getDriverClassName());
        hikariDataSource.setUsername(dbConfig.getUsername());
        hikariDataSource.setPassword(dbConfig.getPassword());
        hikariDataSource.setMinimumIdle(dbConfig.getMinimumIdle());
        hikariDataSource.setMaximumPoolSize(dbConfig.getMaximumPoolSize());
        hikariDataSource.setIdleTimeout(dbConfig.getIdleTimeout());
        hikariDataSource.setConnectionTimeout(dbConfig.getConnectionTimeout());
        hikariDataSource.setConnectionTestQuery(dbConfig.getConnectionTestQuery());
        hikariDataSource.setMaxLifetime(dbConfig.getMaxLifetime());
        dataSource = hikariDataSource;
        tableName = "tcc_transaction"+config.getModelName()+"_log";
        //创建表结构
        createTable(tableName);
    }

    @Override
    public int save(TransactionBean bean) {
        String sql = "insert into " + tableName + "(trans_id,phase,role,retried_times,version,target_class,target_method,"
                + "confirm_method,cancel_method,create_time,update_time,invocation)"
                + " values(?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            return executeUpdate(sql, bean.getTransId(), bean.getPhase(),bean.getRole(),bean.getRetryTimes(),bean.getVersion(),
                    bean.getTargetClass(), bean.getTargetMethod(),bean.getConfirmMethod(),bean.getCancelMethod(),
                    bean.getCreateTime(),bean.getUpdateTime());
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int updatePhase(String transId, Integer phase) {
        String sql = "update " + tableName + " set phase = ? , update_time = ?  where trans_id = ?  ";
        return executeUpdate(sql, phase,new Date(),transId);
    }

    @Override
    public int updateParticipantList(TransactionBean bean) {
        try {
            String sql = "update " + tableName + " set invocation = ? , update_time = ? where trans_id = ? ";
            return executeUpdate(sql,  new Date(),bean.getTransId());
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int remove(String transId) {
        String sql = "delete from " + tableName + " where trans_id = ? and phase != 0 ";
        return executeUpdate(sql,transId);
    }

    @Override
    public TransactionBean query(String transId) {
        String selectSql = "select * from " + tableName + " where trans_id = ? ";
        List<Map<String, Object>> list = executeQuery(selectSql, transId);
        if (!CollectionUtils.isEmpty(list)) {
            return list.stream()
                    .filter(Objects::nonNull)
                    .map(this::buildByResultMap)
                    .findFirst().orElse(null);
        }
        return null;

    }

    @Override
    public List<TransactionBean> queryAllByDelay(Date delayTime) {
        String selectSql = "select * from " + tableName + " where update_time < ? ";
        List<Map<String, Object>> list = executeQuery(selectSql, delayTime);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(list)) {
            return list.stream().filter(Objects::nonNull)
                    .map(this::buildByResultMap)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();



    }

    @Override
    public int updateTransactionRetryTimes(String transId, Integer version, Integer retryTimes) {
        try {
            int currentVersion = version + 1;
            String sql = "update " + tableName + " set retried_times = ? , version = ? ,update_time = ? where trans_id = ? and version = ? ";
            return executeUpdate(sql, retryTimes,currentVersion, new Date(),transId,version);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    private void createTable(String tableName) {
        String buildTableSQL = buildMysql(tableName);
        executeUpdate(buildTableSQL);

    }
    private int executeUpdate(final String sql, final Object... params) {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            ps = connection.prepareStatement(sql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1,params[i]);
                }
            }
            int influence = ps.executeUpdate();
            connection.commit();
            return influence;
        } catch (SQLException e) {
            log.error("create  log table error: {}",e);
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return 0;
        } finally {
            close(connection, ps, null);
        }

    }
    private void close(final Connection con, final PreparedStatement ps, final ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        try {
            if (ps != null) {
                ps.close();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        try {
            if (con != null) {
                con.close();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

    }
    private String buildMysql(final String tableName) {
        return "CREATE TABLE IF NOT EXISTS `" +
                tableName +
                "` (" +
                "  `trans_id` varchar(64) NOT NULL," +
                "  `phase` tinyint NOT NULL," +
                "  `role` tinyint NOT NULL," +
                "  `retried_times` tinyint NOT NULL," +
                "  `version` tinyint NOT NULL," +
                "  `target_class` varchar(256) ," +
                "  `target_method` varchar(128) ," +
                "  `confirm_method` varchar(128) ," +
                "  `cancel_method` varchar(128) ," +
                "  `invocation` longblob," +
                "  `create_time` datetime NOT NULL," +
                "  `update_time` datetime NOT NULL," +
                "  PRIMARY KEY (`trans_id`))";
    }

    private List<Map<String, Object>> executeQuery(final String sql, final Object... params) {
        
        
        
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Map<String, Object>> list = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(sql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
            }
            rs = ps.executeQuery();
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            list = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> rowData = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    rowData.put(md.getColumnName(i), rs.getObject(i));
                }
                list.add(rowData);
            }
        } catch (SQLException e) {
            log.error("query  log table error: {}",e);
        } finally {
            close(connection, ps, rs);
        }
        return list;
    }

    private TransactionBean buildByResultMap(final Map<String, Object> map) {
        TransactionBean transaction = new TransactionBean();
        transaction.setTransId((String)map.get("trans_id"));
        transaction.setPhase((Integer)map.get("phase"));
        transaction.setRole((Integer)map.get("role"));
        transaction.setRetryTimes((Integer)map.get("retried_times"));
        transaction.setVersion((Integer)map.get("version"));
        transaction.setCreateTime((Date)map.get("create_time"));
        transaction.setUpdateTime((Date)map.get("update_time"));
        byte[] bytes = (byte[]) map.get("invocation");
        try {
          final  List< ParticipantBean> participantBeans = gson.fromJson(gson.toJson(bytes), CopyOnWriteArrayList.class);
            transaction.setParticipantBeanList(participantBeans);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return transaction;
    }
}

