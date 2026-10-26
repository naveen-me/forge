/*
 * Created on May 17, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.common.mail.dal;

import com.svb.common.dba.SVDBOperation;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WebUnSubDAL {

	public void unSubscribeCustomer(String email){
		SVDBOperation dbo = new SVDBOperation() ;
		String sql = "" ;
		try
		{
			dbo.createConnection() ;
			sql = " update customer_master set sv_customer_email_status = 'N' where ltrim(rtrim(sv_customer_email)) = '"+email.trim()+"'";
			dbo.executeUpdate(sql) ;
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			dbo.closeConnection();
		}
	}

	public String[][] getRepCustomerDetails(String email){
		SVDBOperation dbo = new SVDBOperation() ;
		String sql = "" ;
		String [][]result = new String[0][0];
		try
		{
			dbo.createConnection() ;
			sql = " select sv_customer_first_name,sv_customer_last_name,sv_avon_rep_first_name,sv_avon_rep_last_name, "+
				  " sv_avon_rep_email from customer_master a,avon_rep_master b where ltrim(rtrim(sv_customer_email)) = '"+email.trim()+"'"+
				  " and a.sv_avon_rep_id = b.sv_avon_rep_id " ;
			result = dbo.executeQuery(sql) ;
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			dbo.closeConnection();
		}
		return result;
	}

	public void unSubscribeRep(String email){
		SVDBOperation dbo = new SVDBOperation() ;
		String sql = "" ;
		try
		{
			dbo.createConnection() ;
			sql = " update avon_rep_master set sv_avon_rep_email_status = 'N' where ltrim(rtrim(sv_customer_email)) = '"+email.trim()+"'";
			dbo.executeUpdate(sql) ;
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			dbo.closeConnection();
		}


	}
}
