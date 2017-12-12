package utils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public class Dbhelper {
 public static final String name = "com.mysql.jdbc.Driver";
    public Connection conn = null;
    public PreparedStatement pst = null;

    /**
     * 初始化就会链接数据库
     */
    public DBHelper() throws SQLException {

        try {
            Class.forName(name);//指定连接类型
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Properties pro = new Properties();
        try {
            pro.load(DBHelper.class.getResourceAsStream("/jdbc.properties"));
        } catch (IOException e) {
            System.out.println("未找到配置文件！！！");
        }
        /**
         * rewriteBatchedStatements=true，mysql默认关闭了batch处理，通过此参数进行打开，这个参数可以重写向数据库提交的SQL语句
         *useServerPrepStmts=false，如果不开启(useServerPrepStmts=false)，使用com.mysql.jdbc.PreparedStatement进行本地SQL拼装，最后送到db上就是已经替换了?后的最终SQL.
	 *更换url链接中的内容
         */
        String url = pro.getProperty("jdbcUrl").replace("/tbdap_sd?", "/tbdap_sd?useServerPrepStmts=false&rewriteBatchedStatements=true&");
        String user = pro.getProperty("user");
        String password = pro.getProperty("password");
        conn = DriverManager.getConnection(url, user, password);//获取连接
    }

    public void preparedStatement(String sql) throws SQLException {

        pst = conn.prepareStatement(sql);//准备执行语句

    }

    public void close() {
        try {
            pst.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开启数据库事务
     */
    public void beginTransaction() throws SQLException {
        if (conn != null) {
            if (conn.getAutoCommit()) {
                conn.setAutoCommit(false);
            }
        }
    }

    /**
     * 提交数据库事务
     */
    public void commitTransaction() throws SQLException {
        if (conn != null) {
            if (!conn.getAutoCommit()) {
                conn.commit();
            }
        }
    }

    /**
     * 回滚事务
     */
    public void rollbackTransaction() {
        if (conn != null) {
            try {
                if (!conn.getAutoCommit()) {
                    conn.rollback();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
