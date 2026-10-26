package com.svb.common.dba;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.svb.common.ConstantDeclarations;


/*******************************************************************************
 * This class is used to open and close connections and return the data in the
 * result set in the form of a two dimensional array . This is written to avoid
 * using sql related statements in the actual jsp page
 ******************************************************************************/

public class SVDBOperation {
	private SVDBConnection dbcon;
	private static  Logger logger = Logger.getLogger(SVDBOperation.class.getName());
	private SQLDBConnection dbconsql;

	/**
	 * This field holds the connection .
	 */
	private Connection con;

	/**
	 * This field will hold the statement
	 */
	private Statement stmt;

        private PreparedStatement pStmt;
        private Connection conn;
        private SVDBConnection sDbcon;

	/**
	 * This field will hold the result set
	 */
	private ResultSet rs;

	/**
	 * Insert the method's description here. Creation date: (4/3/01 2:00:26 PM)
	 */
	public void closeConnection()  {

		try
		{
		  if (rs != null)
			rs.close();
   		  if (stmt != null)
			stmt.close();
		  dbcon.free(con);
		  dbcon.destroy();
		} catch (Exception exx) {
			exx.printStackTrace();
		}
	}

	/**
	 * Insert the method's description here. Creation date: (4/3/01 1:59:03 PM)
	 */
	public void createConnection() throws Exception {

	 try{
		dbcon = new SVDBConnection();
		con = dbcon.getConnection();
		stmt = con.createStatement();
	 }catch(Exception e){
	 	e.printStackTrace();
	 	throw e;
	 }

	}
	
	public void createConnectionStatic() throws Exception {

		 try{
			dbcon = new SVDBConnection();
			con = dbcon.getConnection();
			stmt = con.createStatement();
		 }catch(Exception e){
		 	e.printStackTrace();
		 	throw e;
		 }
		}

	public void createConnection(String server) throws Exception {

		if (server.equals("oracle")) {
			dbcon = new SVDBConnection();
			con = dbcon.getConnection();
		} else {
			dbconsql = new SQLDBConnection();
			con = dbconsql.getConnection();
		}

		stmt = con.createStatement();

	}

	/**
	 * Insert the method's description here. Creation date: (4/3/01 2:01:18 PM)
	 *
	 * @return java.lang.String[][]
	 */
         public Connection createConnectionObject() throws Exception
         {
            sDbcon = new SVDBConnection();
            conn = sDbcon.getConnection();

            if (conn == null)
              throw new Exception("There is no connection available");

            conn.setAutoCommit(false);
            return conn;
        }
	public int executeUpdate(String sql) {
		int i = 1;
		long var4 = 0 ;
		try {
		//	//logger.log(Level.DEBUG,"######################################");
		//	//logger.log(Level.DEBUG," Before query "+sql);
		//	//logger.log(Level.DEBUG,"");
			Date date1 = new Date();
			stmt.execute(sql);
			Date date2 = new Date();
			var4 = date2.getTime() - date1.getTime();
			//logger.log(Level.DEBUG,"");
			//System.out.println("##################### Time Taken to execute Query : "+var4+" milliSeconds %%%%% "+sql+ "%%%%%%");
			//logger.log(Level.DEBUG,"");
		//	//logger.log(Level.DEBUG," /n After query "+sql);
		//	//logger.log(Level.DEBUG,"############################################");

		} catch (Exception e) {
			System.out.println("error query "+sql);
			//logger.log(Level.DEBUG," /n error query "+sql);
			//logger.log(Level.DEBUG,"############################################");
			e.printStackTrace();
			return 0;
		}
		return i;

	}
  public void commit() throws SQLException
  {
    conn.commit();
  }


	public ResultSet executeQueryRS(String sql){
		long var4 = 0 ;
		try {
			Date date1 = new Date();
			////logger.log(Level.DEBUG,"############################################");
			////logger.log(Level.DEBUG,"Before query "+sql);
			////logger.log(Level.DEBUG,"");
			rs = stmt.executeQuery(sql);
			Date date2 = new Date();
			var4 = date2.getTime() - date1.getTime();
			//System.out.println(" ##################### Time Taken to execute Query : "+var4+"milli seconds %%%%%% "+sql + "%%%%%%");
			//logger.log(Level.DEBUG,"");
			////logger.log(Level.DEBUG,"/n After query "+sql);
			////logger.log(Level.DEBUG,"##################################################");

		}catch(Exception e){
			System.out.println("error query "+sql);
			//logger.log(Level.DEBUG,"  error query "+sql);
			//logger.log(Level.DEBUG,"##############################################");
			e.printStackTrace();

		}
			return rs;
	}

	//Added to improve performance of loop iterations - Shivam - 01/08/2024 - Start
	
	/*public String[][] executeQuery(String sql) {
		String rowData[][] = null;
		ArrayList vect = new ArrayList();
		int noofrows = 0; //No of rows in a vector
		long timeTakenToExecute = 0 ;
		try {
			//Removed use of Date object to measure time - Shivam - 01/08/2024
			long startTime = System.currentTimeMillis();
			rs = stmt.executeQuery(sql);
			timeTakenToExecute = System.currentTimeMillis() - startTime;
			System.out.println("  ##################### Time Taken to execute Query "+timeTakenToExecute+" milliSeconds %%%%%% "+sql + "%%%%%%");
			startTime = System.currentTimeMillis();
			while (rs.next())
			//add each row to a vector
			{
				String[] rowvalues = new String[rs.getMetaData().getColumnCount()];
				for (int i = 0; i < rowvalues.length; i++){
					rowvalues[i] = rs.getString(i + 1);
				}
					
				vect.add(rowvalues);
			}
			noofrows = vect.size();
			rowData = new String[noofrows][];
			for (int rownum = 0; rownum < noofrows; rownum++) {
				rowData[rownum] = (String[]) vect.get(rownum);
			}
			timeTakenToExecute = System.currentTimeMillis() - startTime;
			System.out.println("  ##################### Time Taken to execute loop "+timeTakenToExecute+" milliSeconds %%%%%%%%%%%%");
		} catch (Exception e) {
			System.out.println("error query "+sql);
			e.printStackTrace();
			rowData = new String[0][];
		} finally {
			if (rowData == null) {
				rowData = new String[0][];
			}
		} //end of finally
		return rowData;
	}*/
	
	public String[][] executeQuery(String sql){
		String[][] rowData = null;
		ArrayList<String[]> vect = new ArrayList<>();
		try {
			//Removed use of Date object to measure time - Shivam - 01/08/2024
			long startTime = System.currentTimeMillis();
			stmt.setFetchSize(ConstantDeclarations.IDEAL_FETCH_SIZE);
			rs = stmt.executeQuery(sql);
			System.out.println("  ##################### Time Taken to execute Query "+(System.currentTimeMillis() - startTime)+" milliSeconds %%%%%% "+sql + "%%%%%%");
			startTime = System.currentTimeMillis();
			int columnCount = rs.getMetaData().getColumnCount();
			while (rs.next()) {
				String[] rowvalues = new String[columnCount];
				for (int i = 0; i < rowvalues.length; i++){
					rowvalues[i] = rs.getString(i + 1);
				}
				vect.add(rowvalues);
			}
			rowData = vect.toArray(new String[0][]);
			System.out.println("  ##################### Time Taken to execute loop "+(System.currentTimeMillis() - startTime)+" milliSeconds %%%%%%%%%%%%");
		} catch (Exception e) {
			System.out.println("error query "+sql);
			e.printStackTrace();
			rowData = new String[0][];
		} finally {
			if (rowData == null) {
				rowData = new String[0][];
			}
		} //end of finally
		return rowData;
	}
	//Added to improve performance of loop iterations - Shivam - 01/08/2024 - End
	
	public List<Map<Integer,Object>> executeQueryforMap(String sql) {
//		String rowData[][] = null;
		List<Map<Integer,Object>> vect = new ArrayList<Map<Integer,Object>>();
//		int noofrows = 0; //No of rows in a vector
		long var4 = 0 ;
		try {
			Date date1 = new Date();
			////logger.log(Level.DEBUG,"######################################");
			////logger.log(Level.DEBUG," Before query "+sql);
			////logger.log(Level.DEBUG,"");
			rs = stmt.executeQuery(sql);
			Date date2 = new Date();
			var4 = date2.getTime() - date1.getTime();
			//logger.log(Level.DEBUG,"");
			//logger.log(Level.DEBUG,"  ##################### Time Taken to execute Query "+var4+" milliSeconds %%%%%% "+sql + "%%%%%%");
			System.out.println("  ##################### executeQueryforMap Time Taken to execute Query "+var4+" milliSeconds %%%%%% "+sql + "%%%%%%");
			////logger.log(Level.DEBUG,"  After query "+sql);
			////logger.log(Level.DEBUG,"##############################################");
			date1 = new Date();
			int columns = rs.getMetaData()
					.getColumnCount();
			while (rs.next())
			//add each row to a vector
			{
				//String[] rowvalues = new String[rs.getMetaData()
				//		.getColumnCount()];
				Map<Integer,Object> rowData = new HashMap<Integer,Object>();
//				System.out.println("Rowvalues: "+rowvalues);
				for (int i = 0; i < columns; i++){
					//rowvalues[i] = rs.getString(i + 1);
					rowData.put(i, rs.getString(i + 1));
//					System.out.println("Rowvalues in loop: "+rowvalues[i]);
				}
					
				vect.add(rowData);
			}
//			noofrows = vect.size();
//			rowData = new String[noofrows][];
//			for (int rownum = 0; rownum < noofrows; rownum++) {
//				rowData[rownum] = (String[]) vect.get(rownum);
//			}
			date2 = new Date();
			var4 = date2.getTime() - date1.getTime();
			System.out.println("  ##################### executeQueryforMap Time Taken to execute loop "+var4+" milliSeconds %%%%%%%%%%%%");
		} catch (Exception e) {
			////logger.log(Level.DEBUG,e);
			//logger.log(Level.DEBUG,"  error query "+sql);
			System.out.println("error query "+sql);
			//logger.log(Level.DEBUG,"##############################################");
			e.printStackTrace();
			vect = new ArrayList<Map<Integer,Object>>();
//		} finally {
////			if (rowData == null) {
////				rowData = new String[0][];
////			}
//			
//
//		} //end of finally
		return vect;
		}
		return vect;
	}
}