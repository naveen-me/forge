/*
 * Created on Apr 21, 2008
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.common.javabean;

import com.svb.common.dba.SVDBOperation;

/**
 * @author Rao
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CustomerTrackBean {
	
	SVDBOperation dbo = null;
	String result[][] = null;
	String sql = null;

	public void insertCustomerTrack(String module,String customerId, String scrName,String buttonName,String attribId,String trackIP)
	{
		SVDBOperation dbo = null;
		String sql = "";
		String msg = "";
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
					sql = "insert into customer_track values "+
					      " ('" + module + "','"+customerId+"', '" + scrName + "','" + buttonName + "','" + attribId + "', sysdate,'') ";
				int i1 = dbo.executeUpdate(sql);
		} catch (Exception ex1) {
			ex1.printStackTrace();
		}
		finally {
			dbo.closeConnection() ;
		}
	   	
	}
	public static void main(String[] args) {
	}
}
