/*
 * Created on Apr 22, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.regimenmail.javabeans;

import java.util.ArrayList;
import java.util.StringTokenizer;

import com.svb.common.dba.SVDBOperation;
import com.svb.common.utils.SVAvonUtils;
import com.svb.common.valueobjects.*;

/**
 * @author vb
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RegimenTrackingDAL {
	
	public void insertCustomerShoppingCart(String customerId, String shoppingCartString,String module)
	{

		SVDBOperation dbo = null;
	    int result;
	    String sql = "";
	    ArrayList prods = new ArrayList() ;
        StringTokenizer str = new StringTokenizer(shoppingCartString,"|") ;
        while (str.hasMoreTokens())
        {
        	String prodString = str.nextToken() ;
        	StringTokenizer str2 = new StringTokenizer(prodString,"~") ;
        	String prodid = str2.nextToken() ;
        	String quantity = str2.nextToken() ;
        	sql = "insert into customer_shopping_cart values ('"+customerId+"','"+module+"',sysdate,'"+prodid+"','"+quantity+"')";
        	System.out.println(sql) ;
        	prods.add(sql) ;
        }
	    sql = "" ;

	    try {
		    dbo = new SVDBOperation();
		    dbo.createConnection();
		    //logger.log(Level.DEBUG,sql);
		    for (int i=0; i < prods.size(); i++)
		    {
		      sql = (String) prods.get(i) ; 
		      result = dbo.executeUpdate(sql);
		    }
	     } catch (Exception ex1) {
	     	//logger.log(Level.DEBUG,ex1);
	     	ex1.printStackTrace();
	     }
	     finally {
			dbo.closeConnection();
	     }

	  }

	public void insertCustomerPDF(String customerId, String module)
	{

		SVDBOperation dbo = null;
	    int result;
	    String sql = "";
        sql = "insert into customer_pdf_files values ('"+customerId+"','"+module+"',sysdate)";
        System.out.println(sql) ;

	    try {
		    dbo = new SVDBOperation();
		    dbo.createConnection();
		    //logger.log(Level.DEBUG,sql);
		    result = dbo.executeUpdate(sql);
	     } catch (Exception ex1) {
	     	//logger.log(Level.DEBUG,ex1);
	     	ex1.printStackTrace();
	     }
	     finally {
			dbo.closeConnection();
	     }

	  }
	public void insertCustomerEmail(String customerId, String module, String from, String to, String attachment)
	{

		SVDBOperation dbo = null;
	    int result;
	    String sql = "";
	    
	    if (attachment.equals("yes"))
	    	attachment = "Y" ;
	    else
	    	attachment = "N" ;
	    
        sql = "insert into customer_emails values ('"+customerId+"','"+module+"',sysdate,'"+from+"','"+to+"','"+attachment+"')";
        System.out.println(sql) ;

	    try {
		    dbo = new SVDBOperation();
		    dbo.createConnection();
		    //logger.log(Level.DEBUG,sql);
		    result = dbo.executeUpdate(sql);
	     } catch (Exception ex1) {
	     	//logger.log(Level.DEBUG,ex1);
	     	ex1.printStackTrace();
	     }
	     finally {
			dbo.closeConnection();
	     }

	  }
}
