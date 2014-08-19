package com.tj;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DBcasTest {
	public static void main(String[] args)throws Exception {
		String config = DBcasTest.class.getPackage().getName().replace('.', '/') + "/app-test.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(config);
        context.start();
        System.out.println("spring inited");
        
        final DataSource dataSource = (DataSource)context.getBean("dataSource0");
        
        final Map<Long,AtomicLong> count = new ConcurrentHashMap<Long,AtomicLong>();
        
        final AtomicLong updateFail = new AtomicLong(0);
        final AtomicLong exceptions = new AtomicLong(0);
        final AtomicLong num = new AtomicLong(0);
        Runnable job = new Runnable(){

			@Override
			public void run() {
				for(;;){
					if(num.get() > 100000) break;
						
		        	String name = "seq0";
		        	Connection conn = null;
					try {
						conn = dataSource.getConnection();
						Long oldValue = this.getOldValue(conn, name);
						
						//simulate another version
//						conn.close();
//						conn = dataSource.getConnection();
						
						if(0 == this.updateNewValue(conn, name, oldValue, oldValue+1L)){
							updateFail.getAndIncrement();
							continue;
						}
						
						num.getAndIncrement();
						
						if(count.get(oldValue) == null){
							count.put(oldValue, new AtomicLong(1));
						}else{
							System.out.println(Thread.currentThread().getName() + ":confilict key " + oldValue + ":count: " +count.get(oldValue).getAndIncrement());
						}
			          
					} catch (Exception e) {
						exceptions.getAndIncrement();
						//e.printStackTrace();
						
					}finally{
						closeConnection(conn);
					}
		            
		        }
				
				//System.out.println(Thread.currentThread().getName() + ": key count =" + count.size() );
			}
			
			private int updateNewValue(Connection conn, String name, long oldValue, long newValue)
		            throws SQLException {
		        PreparedStatement stmt = null;
		        ResultSet rs = null;
		        try {
		            stmt = conn.prepareStatement(getUpdateSql());
		            stmt.setLong(1, newValue);
		            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
		            stmt.setString(3, name);
		            stmt.setLong(4, oldValue);
		            return stmt.executeUpdate();
		        } finally {
		            closeDbResource(rs, stmt);
		        }
				
			}
			
			private Long getOldValue(Connection conn, String name) throws SQLException {
		        PreparedStatement stmt = null;
		        ResultSet rs = null;
		        try {
		            stmt = conn.prepareStatement(getSelectSql());
		            stmt.setString(1, name);
		            rs = stmt.executeQuery();
		            if(rs.next()){
		            	
		            	return rs.getLong("max");
		            }
		            
		            throw new SQLException(name + " sequence don't exist");
		        } finally {
		            closeDbResource(rs, stmt);
		        }
			}
			
			private String getSelectSql() {
		        StringBuilder buffer = new StringBuilder();
		        buffer.append("select ").append("max");
		        buffer.append(" from ").append("sequence");
		        buffer.append(" where ").append("name").append(" = ?");
		        return buffer.toString();
		    }
			
			private String getUpdateSql() {
		        StringBuilder buffer = new StringBuilder();
		        buffer.append("update ").append("sequence");
		        buffer.append(" set ").append("max").append(" = ?, ");
		        buffer.append("gmt_modified").append(" = ? where ");
		        buffer.append("name").append(" = ? and ");
		        buffer.append("max").append(" = ?");
		        return buffer.toString();
		    }
			
			private void closeDbResource(ResultSet rs, Statement stmt) {
		        closeResultSet(rs);
		        closeStatement(stmt);
		    }

			private void closeResultSet(ResultSet rs) {
		        if (rs != null) {
		            try {
		                rs.close();
		            } catch (SQLException e) {
		            } catch (Throwable e) {
		            }
		        }
		    }

		    private void closeStatement(Statement stmt) {
		        if (stmt != null) {
		            try {
		                stmt.close();
		            } catch (SQLException e) {
		            } catch (Throwable e) {
		            }
		        }
		    }

		    private void closeConnection(Connection conn) {
		        if (conn != null) {
		            try {
		                conn.close();
		            } catch (SQLException e) {
		            } catch (Throwable e) {
		            }
		        }
		    }
        };
        
        List<Thread> ts = new ArrayList<Thread>();
        Long start = System.currentTimeMillis();
        for(int j = 0;j < 100;j++){
        	Thread thread = new Thread(job,"thread"+j);
        	ts.add(thread);
        	thread.start();
        }
        
        for(int k=0 ;k < ts.size();k++){
        	ts.get(k).join();
        }
        
        System.out.println((System.currentTimeMillis() - start)/1000 + ": seconde");
        System.out.println(count.size() + ": count");
        System.out.println(updateFail.get() + ": fail");
        System.out.println(exceptions.get() + ": exception");
        System.out.println((exceptions.get()+updateFail.get()+count.size()) + ": all");
        System.out.println(num.get() + ": nums");
	}

}
