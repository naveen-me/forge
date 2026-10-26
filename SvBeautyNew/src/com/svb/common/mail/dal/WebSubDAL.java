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
public class WebSubDAL {

	public void subscribeRep(String repId){
		SVDBOperation dbo = new SVDBOperation() ;
		String sql = "" ;
		try
		{
			dbo.createConnection() ;
			sql = " update avon_rep_master set sv_avon_rep_email_status = 'Y' where sv_avon_rep_id = '"+repId+"'";
			dbo.executeUpdate(sql) ;
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			dbo.closeConnection();
		}


	}
}
