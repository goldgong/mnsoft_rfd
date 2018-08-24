package com.mnsoft.mac.rfd.sinker.hbase.util;


import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;

/**
 * Created by linuxlv on 2016. 7. 5..
 */
public class PhoenixJDBCHelper extends JDBCHelper{
    private Logger logger = LoggerFactory.getLogger(getClass());
    private static String delim="^";

    public PhoenixJDBCHelper(String driverName, String jdbcUrl) throws SQLException{
        this(driverName,jdbcUrl, null, null);
    }

    /**
     * @param driverName JDBC Driver Name
     * @param jdbcUrl JDBC 접속 URI
     * @param userName 사용자 계정
     * @param passWd 사용자 Password
     * @throws Exception
     */
    public PhoenixJDBCHelper(String driverName, String jdbcUrl, String userName, String passWd) throws SQLException{

        super(driverName,jdbcUrl, userName, passWd);
        dataSource.setConnectionProperties("phoenix.query.dateFormatTimeZon=GMT+09:00;hbase.client.scanner.timeout.period=1200000;phoenix.query.timeoutMs=1200000;phoenix.query.keepAliveMs=1200000;zookeeper.session.timeout=180000;");
    }


    /**
     * select Query
     * @param sql 검색 Query
     * @return 조회 결과
     */
    public List<CharSequence> selectListDataManager(String sql) throws SQLException{
        return selectListDataManager(sql, null);
    }

    /**
     * select Query
     * @param sql 검색 Query
     * @param params 검색 조건
     * @return 조회 결과
     */
    public List<CharSequence> selectListDataManager(String sql,String[] params) throws SQLException{
        return selectListDataManager(sql, params, true);
    }

    /**
     * select Query
     * @param sql 검색 Query
     * @param params 검색 조건
     * @return 조회 결과
     */
    public List<CharSequence> selectListDataManager(String sql,String[] params, boolean useFieldInfo) throws SQLException{
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<CharSequence> rtnValues = Lists.newArrayList();
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
            StringBuilder sb = new StringBuilder( );
            StringBuilder fieldSb = new StringBuilder();
            boolean setFieldInfo = false;
            ResultSetMetaData rsmd = rs.getMetaData();
                while (rs.next()) {
                sb.setLength(0);
                for (int i = 1; i < rsmd.getColumnCount() + 1; i++) {
                    if(useFieldInfo && !setFieldInfo) {
                        fieldSb.append(rsmd.getColumnName(i)).append(delim);
                    }
                    boolean isValConvert = false;
                    if(isValConvert) {
                        if(rsmd.getColumnType(i) == Types.BIGINT ||rsmd.getColumnType(i) == Types.INTEGER ||rsmd.getColumnType(i) == Types.SMALLINT ||rsmd.getColumnType(i) == Types.TINYINT )  {
                            sb.append(rs.getInt(i)).append(delim);
                        }
                        else if( rsmd.getColumnType(i) == Types.DOUBLE || rsmd.getColumnType(i) == Types.DECIMAL || rsmd.getColumnType(i) == Types.NUMERIC) {
                            sb.append(rs.getDouble(i)).append(delim);
                        }else if( rsmd.getColumnType(i) == Types.FLOAT) {
                            sb.append(rs.getFloat(i)).append(delim);
                        } else if(rsmd.getColumnType(i) == Types.VARBINARY) {
                            sb.append(rs.getBytes(i)).append(delim);
                        } else {
                            sb.append(rs.getString(i)).append(delim);
                        }
                    } else {
                        sb.append(rs.getString(i)).append(delim);
                    }

                }
                outValues.add(sb.substring(0, sb.length() -1));
                setFieldInfo = true;
            }
            if(useFieldInfo && setFieldInfo) {
                rtnValues.add(fieldSb.substring(0,fieldSb.length()-1));
                rtnValues.addAll(outValues);
            } else {
                rtnValues.addAll(outValues);
            }

        }finally{
            close(conn, pstmt);
        }
        return rtnValues;
    }



    /**
     * 1건 Insert
     * @param sql Insert Query
     * @param params Parameter
     */
    public void insertOne(String sql, String[] params) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try{
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);

            for(int i=0; i<params.length ; i++){
                pstmt.setString(i+1,params[i]);
            }
            pstmt.execute();
            conn.commit();
        }finally {
            close(conn,pstmt);
        }
    }




    public void executeSql( String sql) throws SQLException{
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            stmt.execute(sql);
            conn.commit();
        }finally{
            if(conn != null){
                conn.close();
            }
        }
    }

    /**
     * connection close
     * @param conn
     * @param pstmt
     */
    private void close(Connection conn, Statement pstmt){
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
     * connection close
     * @param conn
     */
    private void close( Connection conn){
        try{
            if(conn != null){
                conn.close();
                conn = null;
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    /**
     * connection close
     * @param pstmt
     */
    private void close( Statement pstmt){
        try{
            if(pstmt != null){
                pstmt.close();
                pstmt = null;
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
    }

}
