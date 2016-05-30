package com.tower.resmaster.framework.jdbc;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;

import com.tower.resmaster.framework.exceptions.DaoException;

/**
 * ���ݿ���ʻ���
 * BaseDaoSupport
 * shen.zhi  216-05-14
 */
public class BaseDaoSupport implements InitializingBean {
    private static final Logger _logger= Logger.getLogger(BaseDaoSupport.class);

    private JdbcTemplate jdbcTemplate;
    private LobHandler lobHandler;
    private DataSource dataSource;

	public final JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public final void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public final DataSource getDataSource() {
		return dataSource;
	}

	public final void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public final void setLobHandler(LobHandler lobHandler) {
		this.lobHandler = lobHandler;
	}

	public final LobHandler getLobHandler() {
		return lobHandler;
	}

    public void afterPropertiesSet() throws Exception {

    }
    
    
    //======================�������в������ݿⷽʽ========================
    public Connection createConnection(){
        _logger.debug("createConnection...");
    	//  ��Ҫ��nativeJdbcExtractor����JDBC������õ�Connection
    	return DataSourceUtils.getConnection(this.getDataSource());
    }
    
    public void releaseConnection(Connection connection){
        _logger.debug("releaseConnection...");
    	DataSourceUtils.releaseConnection(connection, this.getDataSource());
    }
    
    public void cleanUp(Connection connection, Statement stmt, ResultSet rs){
        _logger.debug("close rs...");
    	JdbcUtils.closeResultSet(rs);
        _logger.debug("close stmt...");
    	JdbcUtils.closeStatement(stmt);
        _logger.debug("releaseConnection...");
    	DataSourceUtils.releaseConnection(connection, this.getDataSource());
    }
    
    public void cleanUp(Connection connection,PreparedStatement ps, Statement stmt, ResultSet rs){
        _logger.debug("close rs...");
        JdbcUtils.closeResultSet(rs);
        _logger.debug("close ps...");
        JdbcUtils.closeStatement(ps);
        _logger.debug("close stmt...");
        JdbcUtils.closeStatement(stmt);
        _logger.debug("releaseConnection...");
        DataSourceUtils.releaseConnection(connection, this.getDataSource());
    }
    
    //=====================�ṩ�򵥵Ķ�дClob����=========================================
    /**
     * дClob�ֶ�
     * sql����Ҫ��'?'Ԥ��clob��λ��
     * @param sql
     * @param clobContent
     */
    public void writeClob(String sql, final String clobContent){
        writeClob(sql, clobContent, new Object[]{});
    }
    
    /**
     * дClob�ֶ�
     * sql����Ҫ��'?'Ԥ��clob��λ�ã�����������е�һ��
     * @param sql
     * @param args
     * @param clobContent
     */
    public void writeClob(String sql, final String clobContent, final Object[] args){
        this.getJdbcTemplate().execute(sql,
                new AbstractLobCreatingPreparedStatementCallback(this.getLobHandler()) {
                    protected void setValues(PreparedStatement ps, LobCreator lobCreator) {
                        try {
                            lobCreator.setClobAsString(ps, 1, clobContent);
                            
                            if (args != null) {
                                for (int i = 0; i < args.length; i++) {
                                    Object arg = args[i];
                                    if (arg instanceof SqlParameterValue) {
                                        SqlParameterValue paramValue = (SqlParameterValue) arg;
                                        StatementCreatorUtils.setParameterValue(ps, i + 2, paramValue, paramValue.getValue());
                                    }
                                    else {
                                        StatementCreatorUtils.setParameterValue(ps, i + 2, SqlTypeValue.TYPE_UNKNOWN, arg);
                                    }
                                }
                            }
                        } catch (SQLException e) {
                            throw new DaoException("SQL������Clob�ֶγ���", e);
                        }
                    }
                });
    }
    
    /**
     * ��clob�ֶ�
     * sql�з���ֵclob�ֶ�λ�ڽ�����еĵ�һ��
     * @param sql
     * @return
     */
    public String readClob(String sql) {
        return readClob(sql, new Object[]{});
    }
    
    /**
     * ��clob�ֶ�
     * sql�з���ֵclob�ֶ�λ�ڽ�����еĵ�һ��
     * @param sql
     * @param args
     * @return
     */
    public String readClob(String sql, Object[] args) {
        String clobContent = (String) getJdbcTemplate().queryForObject(sql, args,
                new RowMapper() {
                    public Object mapRow(ResultSet rs, int rowNum) {
                        String txt;
                        try {
                            txt = getLobHandler().getClobAsString(rs, 1);
                        } catch (SQLException e) {
                            throw new DaoException("��ȡClob�ֶγ���", e);
                        }
                        return txt;
                    }
                });

        return clobContent;
    }
    
  
    
    //======================��������ȡSequence�Ĵ���============================
   
    /**
     * 
     * @param seqName
     * @return
     * @deprecated
     */
    public String getSEQ(String seqName){
        _logger.warn("getSEQOrigin:" + seqName);
        String sql = "select " + seqName + ".nextval" + " seq from dual";
        return (String)this.getJdbcTemplate().queryForObject(sql, String.class);
    }

    
    /**
     * �ṩ��ҳ��ѯ�ı�ݷ���������ָ��������ݡ�
     * @param sql ��δ��ҳ�Ĳ�ѯ��䡣
     * @param params ��δ��ҳ�Ĳ������顣
     * @param startRow ��ʼ�У����ڣ���
     * @param endRow ��ֹ�У�С�ڻ���ڣ���
     * @return ָ����������ݡ�
     */
    public List queryListByRange(String sql, Object[] params, int startRow, int endRow){
    	StringBuilder sb = new StringBuilder();
		sb.append("select * from (select t_t.*, rownum r_n from (").append(sql);
		sb.append(") t_t where rownum <= " + endRow + ") where r_n >" + startRow);
		return jdbcTemplate.queryForList(sb.toString(), params);
    }
    
    /**
     * �ṩ��ҳ��ѯ�ı�ݷ���������ͳ��������
     * @param sql ��δ��ҳ�Ĳ�ѯ��䡣
     * @param params ��δ��ҳ�Ĳ������顣
     * @return ͳ��������
     */
    public int queryTotalRow(String sql, Object[] params){
    	StringBuilder sb = new StringBuilder();
		sb.append("select count(1) from(").append(sql).append(")");
		return jdbcTemplate.queryForInt(sb.toString(), params);
    }
}
