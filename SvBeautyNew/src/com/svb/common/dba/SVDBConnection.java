
package com.svb.common.dba;


import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

//import com.se.db.DBConnection;

import oracle.jdbc.driver.OracleDriver;

public class SVDBConnection {

   /*	public SVDBConnection() {
	}

	public void destroy() {
	}

	public void free(Connection connection) throws Exception {
		if (connection != null)
			connection.close();
	}

	static String JDBCDriver = "oracle.jdbc.driver.OracleDriver";

    static String JDBCurl = "jdbc:oracle:thin:@localhost:1521:orcl";

    static String userName = "svb1";

    static String passwd = "svb1";


    /*
     * getConnection is used to get connection from database @returns Connection
     */
    /*public Connection getConnection() throws SQLException, ClassNotFoundException {
    	Connection conn = null;
    	try {
    		Context initCtx = new InitialContext();
    		DataSource ds = (DataSource) initCtx.lookup("java:/SvBeautyDS");

    		conn = ds.getConnection();
    	} catch (Exception e) {
    		System.out.println("getDBConnection Failed to get database connection.");
    		//e.printStackTrace();
    	}
    	return conn;

    }

  public Connection getConnectionAdi()
  {
    Connection conn = null;
    try {
      Context ctx = new InitialContext();
      if (ctx == null)
        throw new Exception("Sorry - No Context");

      DataSource ds = (DataSource) ctx.lookup("java:/SvBeautyDS");

      if (ds != null) {
        conn = ds.getConnection();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return conn;
  }

	public Connection getConnectionStatic() throws Exception {
		Connection connection = null;
		try {
			Properties properties = new Properties();
			properties.load(new DataInputStream(new BufferedInputStream(
					new FileInputStream("g:\\eclipse\\workspace\\svtest\\webapp\\svbdataprop.txt"))));
			String s = properties.getProperty("username");
			String s1 = properties.getProperty("password");
			String s2 = properties.getProperty("url");
			String s3 = properties.getProperty("logfile");
			String s4 = properties.getProperty("driver");
			int i = Integer.parseInt(properties.getProperty("mincon"));
			int j = Integer.parseInt(properties.getProperty("maxcon"));
			float f = Float.parseFloat(properties.getProperty("days"));
			DriverManager.registerDriver(new OracleDriver());
			connection = DriverManager.getConnection(s2, s, s1);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return connection;
	}*/

//-------------------****************-------------------------------
//	private String dbURL="jdbc:oracle:thin:@172.20.9.80:1521:SVSTG"; 
//	private String dbURL="jdbc:oracle:thin:@127.0.0.1:1521:VBDB1"; 
//	private String dbURL="jdbc:oracle:thin:@192.168.1.210:1521:ORCL"; 
//	private String dbUserName="svb1"; 
//	private String dbPassword="svb1"; 
	
	/** Added By Synoptek (15-1-2020)
	 * Local Database Credential 
	 */
	/*
	 --------------------------------Start-------------------------------------
	 * private String dbURL = "jdbc:oracle:thin:@//172.30.252.159:1521/orclpdb";
	 * private String dbUserName = "SVB18"; private String dbPassword = "SVB18";
	 --------------------------------End-------------------------------------
	 */

	/** Added By Synoptek (15-05-2020)
	 * To implement Singleton class functionality 
	 */
	//------------------------------------------Start--------------------------------
	public Connection getConnection(){ 
	Connection conn = null; 
	try{ 
		Context initContext = new InitialContext();
		Context envContext  = (Context)initContext.lookup("java:/comp/env");
		DataSource ds = (DataSource)envContext.lookup("jdbc/myoraclestage");
		conn = ds.getConnection();
	} 
	catch(SQLException | NamingException e){ 
	System.out.println("Could not connect to DB"); 
	} 
	return conn; 
	} 
	//------------------------------------------End-----------------------------------
	public void putConnection(Connection conn){ 
	if(conn!=null) 
	{ 
	try { 
	conn.close(); 
	} 
	catch (SQLException e) 
	{ 
	System.out.println("Could not close the connection"); 

	} 
	} 
	} 
	public void destroy() {
	}
	public void free(Connection connection) throws Exception {
		if (connection != null)
			connection.close();
	}
	public static void main(String args[]){ 
		try { 
		System.out.println(new SVDBConnection().getConnection()); 
		} catch (Exception e) { 
		System.out.println("Exception"+e); 
		} 
		}
}