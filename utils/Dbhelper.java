package utils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public class Dbhelper {
	private static String url = "com.mysql.jdbc.Driver";// 默认链接MySQL数据库
	private Connection conn = null;
	public PreparedStatement pre = null;
	Properties properties=null;
	private String username=null;
	private String password=null;
    /**
     * 
     * @param url 传递加载的数据库驱动
     * @param sql 传递操作的数据库sql语句
     */
	public Dbhelper(String url,String sql) {
		// TODO Auto-generated constructor stub		
		try {
			if(!"".equals(url)){
				Class.forName(url).newInstance();	
			}else{
				Class.forName(this.url).newInstance();
			}			
			properties=new Properties();
			try {
				properties.load(Dbhelper.class.getClassLoader().getResourceAsStream("config.properties"));
				username=properties.getProperty("username");
			    password=properties.getProperty("password");				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				try {
					throw new Exception("没有找到配置文件");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		   conn=DriverManager.getConnection(url, username, password);
		   pre=conn.prepareStatement(sql);
		} catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//初始化
	}
	/**
	     * 数据库查询用的操作 自定义接收实体对象
	     */
       public List<Book> excuteSearch(){
		List<Book> lists=new ArrayList<>();
		Book book=new Book();
		try {
		    ResultSet resultSet=pre.executeQuery();
		    while (resultSet.next()){
			book.setId(Integer.parseInt(resultSet.getString("id")));
			book.setName(resultSet.getString("name"));
			book.setDesc(resultSet.getString("description"));
			book.setImg(resultSet.getString("pic"));
			book.setPrice(resultSet.getString("price"));
			lists.add(book);
		    }
		} catch (SQLException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		return lists;
	    }
	/**
	 * 数据库插入操作,更新使用该操作方法
	 */
	public void excuteInsert(){
		try {
			pre.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return;
	}
	/**
	 * 关闭数据库连接池
	 */
	public void close(){
		try {
			pre.close();
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
