package com.tj.sequence.interal.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import com.tj.sequence.SequenceException;
import com.tj.sequence.interal.SequenceDao;
import com.tj.sequence.interal.SequenceRange;

public class DefaultSequenceDao implements SequenceDao {
	private int retryTimes = 2;
	private List<DataSource> dataSources;
	private int dsCount; //data source's count
	private Map<Integer, AtomicInteger> dsFailCount;//datasource fail time count
	private int maxSkipCount = 10;
	
	@Override
	public SequenceRange nextRange(String name) throws SequenceException {
		int[] randomIntSequence = RandomSequenceUtil.randomIntSequence(dsCount);
		for (int i = 0; i < retryTimes; i++) {
			for (int j = 0; j < dsCount; j++) {
				int index = randomIntSequence[j];
				if (!toTry(index)) continue;
				
				DataSource dataSource = dataSources.get(index);
				SequenceDO seqDO;
				Long oldValue;
				try {
					seqDO = getOldValue(dataSource, name);
					oldValue = seqDO.getValue();
					
                    if (!seqDO.checkValue()) {
                    	//logger.error(name + " sequence check error");
                        continue;
                    }
                } catch (SQLException e) {
                	//logger.error("get sequenceDO error", e);
                    addFail(index);
                    continue;
                }
				
				Long newValue = oldValue + (seqDO.getSize() * seqDO.getStart());
				Long startValue = oldValue;
				try {
					if(newValue > seqDO.getMax()){
						if(seqDO.getCycle()){
							startValue = seqDO.getMin();
							newValue = seqDO.getMin() + (seqDO.getSize() * seqDO.getStart());
						}else{
							//logger.error(name + " sequence is overflow");
							continue;
						}
						
					}
					
                    if (0 == updateNewValue(dataSource, name, oldValue, newValue)) {
                        continue;
                    }
                } catch (SQLException e) {
                    //logger.error("update new value error", e);
                    continue;
                }

                return new SequenceRange(startValue, newValue, seqDO.getStep());
			}
			
			//last retry
			if (i == (retryTimes - 2)) {
				dsFailCount.clear();
            }
		}

		throw new SequenceException("all dataSource faild to get value,check db and sequence value if was overflow");
	}

	private int updateNewValue(DataSource dataSource, String name, long oldValue, long newValue)
            throws SQLException {
		Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(getUpdateSql());
            stmt.setLong(1, newValue);
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            stmt.setString(3, name);
            stmt.setLong(4, oldValue);
            return stmt.executeUpdate();
        } finally {
            closeDbResource(rs, stmt, conn);
        }
		
	}
	private SequenceDO getOldValue(DataSource dataSource, String name) throws SQLException {
		Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(getSelectSql());
            stmt.setString(1, name);
            rs = stmt.executeQuery();
            if(rs.next()){
            	SequenceDO seqDO = new SequenceDO();
            	seqDO.setName(name);
            	seqDO.setValue(rs.getLong(valueColumnName));
            	seqDO.setMin(rs.getLong(minColumnName));
            	seqDO.setMax(rs.getLong(maxColumnName));
            	seqDO.setCycle(rs.getBoolean(cycleColumnName));
            	seqDO.setSize(rs.getInt(sizeColumnName));
            	seqDO.setStart(rs.getLong(startColumnName));
            	seqDO.setStep(rs.getInt(stepColumnName));
            	return seqDO;
            }
            
            throw new SQLException(name + " sequence don't exist");
        } finally {
            closeDbResource(rs, stmt, conn);
        }
	}
	
	private void addFail(int index){
		dsFailCount.put(index, new AtomicInteger(0));
	}
	
	private boolean toTry(int index) {
		boolean result = true;
        if (dsFailCount.get(index) != null) {
            if (dsFailCount.get(index).incrementAndGet() > maxSkipCount) {
            	dsFailCount.remove(index);
            } else {
                result = false;
            }
        }
        return result;
	}
	
	private String tableName = "sequence";
	private String nameColumnName = "name";
	private String valueColumnName = "value";
	private String gmtModifiedColumnName = "gmt_modified";
	private String minColumnName = "min";
	private String maxColumnName = "max";
	private String sizeColumnName = "size";
	private String cycleColumnName = "cycle";
	private String stepColumnName = "step";
	private String startColumnName = "start";
	
	private String getSelectSql() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("select ").append(valueColumnName).append(",");
        buffer.append(minColumnName).append(",");
        buffer.append(maxColumnName).append(",");
        buffer.append(sizeColumnName).append(",");
        buffer.append(cycleColumnName).append(",");
        buffer.append(stepColumnName).append(",");
        buffer.append(startColumnName);
        buffer.append(" from ").append(tableName);
        buffer.append(" where ").append(nameColumnName).append(" = ?");
        return buffer.toString();
    }
	
	private String getUpdateSql() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("update ").append(tableName);
        buffer.append(" set ").append(valueColumnName).append(" = ?, ");
        buffer.append(gmtModifiedColumnName).append(" = ? where ");
        buffer.append(nameColumnName).append(" = ? and ");
        buffer.append(valueColumnName).append(" = ?");
        return buffer.toString();
    }
	
	private void closeDbResource(ResultSet rs, Statement stmt, Connection conn) {
        closeResultSet(rs);
        closeStatement(stmt);
        closeConnection(conn);
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

    
	public void setDataSources(List<DataSource> dataSources) {
		if (dataSources == null || dataSources.size() < 1) {
			throw new IllegalArgumentException("data source can't be null!");
		}
		this.dataSources = dataSources;
		this.dsCount = dataSources.size();
		this.dsFailCount = new ConcurrentHashMap<Integer, AtomicInteger>(dataSources.size());
	}
}
