/*
 * Created on Apr 11, 2008
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.common.javabean;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.StringTokenizer;

/**
 * @author Rao
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class VisitorProfileNumbers {

 public Connection ConnectToServerDB()throws SQLException, 
    FileNotFoundException, 
    IOException{
 Statement stmt = null;
 Connection con=null;
 try
 {

       
   String drivername = "oracle.jdbc.driver.OracleDriver";
   // String providerurl ="jdbc:oracle:thin:@127.0.0.1:1521:VBDB2"; 172.20.9.160:1521:svbeauty
   String providerurl ="jdbc:oracle:thin:@172.20.9.160:1521:svbeauty"; //prod 
   //stage   String providerurl ="jdbc:oracle:thin:@172.20.9.20:1521:svtest"; //stage
   Class.forName(drivername);
   con = DriverManager.getConnection(providerurl,"svb1","svb1");
   System.out.println("Connected to Remote DataBase");

 }
 catch(Exception e)
 {
   String message = e.getMessage();
   System.out.println("message in connection 18"+message);
 }
 return con;
 }
 public void doAnalysis()
 {
 	 Statement stmt = null;
 	 Connection con=null;
 	 ResultSet rs = null ;
 	 String sql = "" ;
 	 try
	 {
 	    //con = (Connection)ConnectToServerDB() ;
 	    //stmt = con.createStatement() ;
 	 	

 	    BufferedReader in = new BufferedReader(new FileReader("G:\\avon_2006\\beauty.com\\reports\\customer_data.txt"));
 	    String line = null;
        
 	    int pcnt = 0 ;
 	    int vcnt = 0 ;
 	    String vcdate = "" ;
// 	    long vcid = 300163580 ; // 1st time from 02/16
// 	    long vcid = 300211934 ; // 1st time from 05/17
 	    long vcid = 300247157 ; 
	    long cid = 0 ;
 	    boolean cflag = false ;
 	    String cdate = "" ;
 	    
 	    line = in.readLine() ;
    	if (line != null)
    	{
	    //while ((line = in.readLine()) != null) {
	    	 StringTokenizer str = new StringTokenizer(line,"\t") ;
	    	 String cnum = str.nextToken() ;
	    	 cdate = str.nextToken() ;
	    	 cid = Long.parseLong(cnum) ;
    	}
    	 // long i=300163580; i < 300215990;
	    for (long i=300247157; i < 300323124; i++)
 	    { 
	    	
 			vcnt = vcnt+1 ;
	    	if (i == cid)
	    	{
	    		//System.out.println(vcdate+"\t"+cdate) ;
	    		if (cdate.trim().equals(vcdate.trim()))
 	    	    {
	    		//	System.out.println("5555 "+vcdate+"\t"+vcnt+"\t"+pcnt) ;
	    		    pcnt = pcnt+1 ;
	    		    cflag = true ;
//	    		    System.out.println(vcdate+"\t"+i+"\t"+cid) ;
 	    		    vcdate = cdate ;
 	    	   }
 	    	   else
 	    	   {
 	    			System.out.println(vcdate+"\t"+vcnt+"\t"+pcnt) ;
 	    			pcnt = 1 ;
 	    			vcnt = 1 ;
 	    		    vcdate = cdate ;
	    		    cflag = true ;
 	    	   }
	    	}
 			if (cflag)
 			{
 			 line = in.readLine() ;
	    	 if (line != null)
	    	 {
 	    //while ((line = in.readLine()) != null) {
 	    	 StringTokenizer str = new StringTokenizer(line,"\t") ;
 	    	 String cnum = str.nextToken() ;
 	    	 cdate = str.nextToken() ;
 	    	 cid = Long.parseLong(cnum) ;
 	    	 cflag = false ;
 	    	//System.out.println(line) ;
 	    /* use line */
	    	 }
 		    }
 	   }
	   System.out.println(vcdate+"\t"+vcnt+"\t"+pcnt) ;
// 	   close reader


	 }
 	 catch(Exception e)
 	 {
 	   String message = e.getMessage();
 	   System.out.println("message in connection 19"+message);
 	 }
 	 finally
	 {
 	 	try
		{
 	 	  if (rs!=null)
 	 	    rs.close() ;
 	 	  if (stmt!=null)
 	 	    stmt.close() ;
 	 	  if (con !=null)
 	 	  	con.close() ;
		}
 	 	catch (Exception e)
		{
 	 		System.out.println("rrrr "+e) ;
		}
	 }
 }
 public static void main(String[] args) {
	VisitorProfileNumbers vpn = new VisitorProfileNumbers() ;
	vpn.doAnalysis() ;
 }
}
