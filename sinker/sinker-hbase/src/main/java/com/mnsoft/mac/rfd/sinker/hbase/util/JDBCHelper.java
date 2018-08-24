package com.mnsoft.mac.rfd.sinker.hbase.util;


import com.google.common.collect.Lists;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.*;
import java.util.List;

/**
 * Created by Cho on 2016. 4. 12..
 * Connection 을 매번 끊고 맺을지.... 아니면 계속 연결 유지를 시키고 있을지 검토 필요...
 *
 */
public class JDBCHelper {

    protected int batchSize = 10000;

    protected BasicDataSource dataSource;

    public JDBCHelper(String driverName, String jdbcUrl) throws SQLException{
        this(driverName,jdbcUrl, null, null);
    }

    /**
     * @param driverName JDBC Driver Name
     * @param jdbcUrl JDBC 접속 URI
     * @param userName 사용자 계정
     * @param passWd 사용자 Password
     * @throws Exception
     */
    public JDBCHelper(String driverName, String jdbcUrl, String userName, String passWd) throws SQLException{
        this.dataSource = new BasicDataSource();
        this.dataSource.setDefaultAutoCommit(false);
        this.dataSource.setDriverClassName(driverName);
        this.dataSource.setUrl(jdbcUrl);
        //세션이 끊기지 않게 특정시간 Query 실행 Option
        if(userName != null) {
            this.dataSource.setUsername(userName);
        }
        if(passWd != null) {
            this.dataSource.setPassword(passWd);
        }
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }


    public boolean checkConnection() {
        try {
            Connection con = dataSource.getConnection();
            return true;
        } catch (SQLException e) {
            return false;
        } finally {
            try {
                close();
            } catch (SQLException e) {
            }
        }
    }

    /**
     * connection 연결..
     * @return
     * @throws Exception
     */
    public Connection getConnection() throws SQLException{
        return dataSource.getConnection();
    }

    /**
     * select Query
     * @param sql 검색 Query
     * @return 조회 결과
     */
    public List<String> selectList(String sql) throws SQLException{
        return selectList(sql, null);
    }

    /**
     * select Query
     * @param sql 검색 Query
     * @param params 검색 조건
     * @return 조회 결과
     */
    public List<String> selectList(String sql,Object[] params) throws SQLException{
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<String> outValues = Lists.newArrayList();
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            if(params != null){
                for(int i=0; i<params.length ; i++){
                    Object param = params[i];
                    if( param instanceof String ) {
                        pstmt.setString(i + 1, (String) param);
                    }else if( param instanceof Integer ){
                        pstmt.setInt( i + 1, (Integer)param);
                    }else if( param instanceof Float ){
                        pstmt.setFloat( i + 1, (Float)param);
                    }else if( param instanceof Double ){
                        pstmt.setDouble( i + 1, (Double)param);
                    }else if( param instanceof JDBCType){
                        pstmt.setNull( i + 1, ((JDBCType)param).getVendorTypeNumber());
                    }
                }
            }
            rs = pstmt.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            while (rs.next()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < rsmd.getColumnCount() + 1; i++) {
                    String value = rs.getString(i);
                    if(value == null || value.equals("null") ){
                        value = "";
                    }
                    sb.append(value).append("|");
                }
                outValues.add(sb.substring(0,sb.length()-1));
            }
        }finally{
            close(conn,pstmt);
        }
        return outValues;
    }


    /**
     * select Query
     * @param sql 검색 Query
     * @return 조회 결과
     */
    public List<CharSequence> selectListDataManager(String sql) throws SQLException{
        return selectListDataManager(sql, null, null);
    }


    /**
     * select Query
     * @param sql 검색 Query
     * @param params 검색 조건
     * @return 조회 결과
     */
    public List<CharSequence> selectListDataManager(String sql,String[] params, String delim) throws SQLException{
        delim = delim == null ? "," : delim;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<CharSequence> outValues = Lists.newArrayList();
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            if(params != null){
                for(int i=0; i<params.length ; i++){
                    pstmt.setString(i+1,params[i]);
                }
            }
            rs = pstmt.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            while (rs.next()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < rsmd.getColumnCount() + 1; i++) {
                    sb.append(rs.getString(i)).append(delim);
                }
                outValues.add(sb.substring(0,sb.length()-1));
            }
        }finally{
            close(conn,pstmt);
        }
        return outValues;
    }


    /**
     * 1건 Insert
     * @param sql Insert Query
     * @param params Parameter
     */
    public void insertOne(String sql, Object[] params) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try{
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);

            for(int i=0; i<params.length ; i++){
                Object param = params[i];
                if( param instanceof String ) {
                    pstmt.setString(i + 1, (String) param);
                }else if( param instanceof Integer ){
                    pstmt.setInt( i + 1, (Integer)param);
                }else if( param instanceof Long ){
                    pstmt.setLong( i + 1, (Long)param);
                }else if( param instanceof Float ){
                    pstmt.setFloat( i + 1, (Float)param);
                }else if( param instanceof Double ){
                    pstmt.setDouble( i + 1, (Double)param);
                }else if( param instanceof JDBCType){
                    pstmt.setNull( i + 1, ((JDBCType)param).getVendorTypeNumber());
                }
            }
            pstmt.execute();
            conn.commit();
        }finally {
            close(conn,pstmt);
        }
    }

    /**
     * jdbc insert dataList...
     * @param sql Insert Query
     * @param datas Insert Parameters
     */
    public void insertList(String sql,List<Object[]> datas) throws SQLException{
        Connection conn = null;
        PreparedStatement pstmt = null;
        try{
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            int cnt = 0;
            for(Object[] params : datas){
                for(int i=0; i<params.length ; i++){
                    Object param = params[i];
                    if( param instanceof String ) {
                        pstmt.setString(i + 1, (String)param);
                    }else if( param instanceof Integer ){
                        pstmt.setInt( i + 1, (Integer)param);
                    }else if( param instanceof Float ){
                        pstmt.setFloat( i + 1, (Float)param);
                    }else if( param instanceof Double ){
                        pstmt.setDouble( i + 1, (Double)param);
                    }else if( param instanceof JDBCType){
                        pstmt.setNull( i + 1, ((JDBCType)param).getVendorTypeNumber());
                    }
                }
                pstmt.addBatch();
                if(++cnt%batchSize == 0){
                    pstmt.executeBatch();
                    conn.commit();
                    pstmt.clearBatch();
                }
            }
            pstmt.executeBatch();
            conn.commit();
        }finally {
            close(conn,pstmt);
        }
    }

    public void executeSql(String sql) throws SQLException{
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            stmt.execute(sql);
            conn.commit();
        }finally{
            close(conn,stmt);
        }
    }

    /**
     * connection close
     * @param conn
     * @param stmt
     */
    private void close(Connection conn,Statement stmt){
        try{
            if(stmt != null){
                stmt.close();
                stmt = null;
            }

            if( conn != null){
                conn.close();
                conn = null;
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    /**
     * connection close
     * @param conn
     * @param pstmt
     */
     protected void close(Connection conn,PreparedStatement pstmt){
        try{
            if(pstmt != null){
                pstmt.close();
                pstmt = null;
            }

            if( conn != null){
                conn.close();
                conn = null;
            }

        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    /**
     * close dataSource
     * @throws SQLException
     */
    public void close() throws SQLException{
        if( dataSource != null ){
            dataSource.close();
        }
    }
}
