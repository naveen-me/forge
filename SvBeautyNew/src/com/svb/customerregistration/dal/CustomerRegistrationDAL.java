
package com.svb.customerregistration.dal;

/**
 * @author David  murphy
 *
 */

import java.sql.ResultSet;

import com.svb.common.dba.SVDBOperation;

public class CustomerRegistrationDAL
{
	SVDBOperation db1= null;
	String result[][] = null;
	String rep_id;

	public String[] getRepId(String repemail)
	{
		SVDBOperation db1= null;
		String sql = "";
		String rs[][] = new String[0][0];
		String r = "";
		String t = "";
		String l = "";
		String[] ret = new String[3] ;

		try {
			db1 = new SVDBOperation();
			db1.createConnection();
			/*sql = " select a.SV_AVON_REP_ID, NVL(b.sv_rep_beauty_advisor_ind,' '), "+
				  " NVL(b.sv_rep_leadership_ind,' ') "+
			      " from AVON_REP_MASTER a, avon_rep_details b"
				+ " where lower(a.SV_AVON_REP_EMAIL) = lower('"+repemail+"') "
				+ " and a.sv_avon_rep_id = b.sv_avon_rep_id " ;*/

sql =
"SELECT "+
"    mas.sv_avon_rep_id, "+
"    NVL(det.sv_rep_beauty_advisor_ind,' '), "+
"    NVL(det.sv_rep_leadership_ind,' ') "+
"FROM avon_rep_master mas, "+
"    avon_rep_preferences pref, "+
"    avon_rep_details det "+
"WHERE mas.sv_avon_rep_id = det.sv_avon_rep_id "+
"    AND mas.sv_avon_rep_id = pref.sv_avon_rep_id (+) "+
"    AND "+
"    ("+
"        lower(sv_avon_rep_email) = lower('"+repemail+"') "+
"        OR lower(sv_avon_rep_pbs_email) = lower('"+repemail+"')"+
"    ) ";

			//logger.log(Level.DEBUG," sql: " + sql);
			rs = db1.executeQuery(sql);
			if(rs!=null && rs.length>0) {
				r = rs[0][0].trim();
				t = rs[0][1].trim();
				l = rs[0][2].trim();
			}
		}catch (Exception ee){
			//logger.log(Level.DEBUG,"No REP ID FOUND");}
			ee.printStackTrace();
		}
		ret[0] = r ;
		ret[1] = t ;
		ret[2] = l;
		db1.closeConnection();
		return ret;
	}

	public String[][] getCustDetails(String cusid,String repid)
	{
		SVDBOperation db2= null;
		String sql1 = "";
		String sql2="";
		String cre[][] = new String[0][0];
		String msg="no";
		boolean rs2 = true;
		try {
			db2 = new SVDBOperation();
			db2.createConnection();
			sql1 = " select sv_customer_id, sv_customer_profile_flag, sv_customer_prodrec_flag, NVL(sv_customer_gender,'F') from customer_master"
				+ " where sv_customer_id = '"
				+ cusid + "'  AND sv_avon_rep_id = '"+repid+"' ";
			//logger.log(Level.DEBUG," sql: " + sql1);
			cre = db2.executeQuery(sql1);
			msg="yes";
			//
			//logger.log(Level.DEBUG,"Custid in getcustdet loop is "+custid);
			}catch(Exception eeee){
				//logger.log(Level.DEBUG,"The Customer is not yet registered. He/She is being registered now");}
				//System.out.println("The Customer is not yet registered. He/She is being registered now");
			}
			db2.closeConnection();
		return cre;
	}

	public String[][] getcustdet(String fname, String lname, String email, String repid)
	{
		SVDBOperation db2= null;
		String sql1 = "";
		String sql2="";
		String cre[][] = new String[0][0];
		String msg="no";
		boolean rs2 = true;
		try {
			db2 = new SVDBOperation();
			db2.createConnection();
			sql1 = " select sv_customer_id, sv_customer_profile_flag, sv_customer_prodrec_flag, NVL(sv_customer_gender,'F') from customer_master"
				+ " where lower(sv_customer_first_name) = lower('"
				+ fname + "') AND lower(sv_customer_last_name) = lower('"
					+ lname + "') AND lower(sv_customer_email) =lower('"
						+ email+"') AND sv_avon_rep_id = '"+repid+"' ";
			//logger.log(Level.DEBUG," sql: " + sql1);
			cre = db2.executeQuery(sql1);
			msg="yes";
			//
			//logger.log(Level.DEBUG,"Custid in getcustdet loop is "+custid);
			}catch(Exception eeee){
				//logger.log(Level.DEBUG,"The Customer is not yet registered. He/She is being registered now");}
				System.out.println("The Customer is not yet registered. He/She is being registered now");
			}
			db2.closeConnection();
		return cre;
	}

	public String insertCusDetails(String cfname,String clname,String eid, String rep_id) throws Exception
	{
		SVDBOperation db3= null;
		String sql2 = "";
		String sql3 = "";
		String sql4 = "";
		String cr1[][]= new String[0][0];
		String cr2[][] = new String[0][0];
		String cr3[][] = new String[0][0];
		String custid="";
			db3 = new SVDBOperation();
			db3.createConnection();
			try{
			sql2 ="select Avon_customer_seq.nextval from dual";
			//logger.log(Level.DEBUG," sql: " + sql2);
			cr1 = db3.executeQuery(sql2);
			//logger.log(Level.DEBUG,cr1[0][0]);
			custid=cr1[0][0];
			//logger.log(Level.DEBUG,"Custid in insert loop is: "+custid);
			}catch(Exception e){e.printStackTrace();}
			try{
			sql3="Insert into customer_master (sv_customer_id, sv_customer_first_name, sv_customer_last_name, sv_customer_email) values ('"+custid+"', '" +cfname+"','"+clname+"','"+ eid+"')";
			cr2 = db3.executeQuery(sql3);
			//logger.log(Level.DEBUG,sql3);
			sql4="Insert into avon_rep_customer (sv_avon_rep_id, sv_customer_id) values ('"+ rep_id +"', '"+custid+"')";
			cr3 = db3.executeQuery(sql4);
			//logger.log(Level.DEBUG,sql4);
			}catch(Exception e){e.printStackTrace();}
		db3.closeConnection();
		return custid;
	}

	public String setcustid()
	{
		SVDBOperation db2= null;
		String sql2 = "";
		String cr1[][]= new String[0][0];
		String custid="";
		try{
		db2 = new SVDBOperation();
		db2.createConnection();
		//System.out.println(" jjjjjjjjjjjjjjjj "+db2) ;
		sql2 ="select Avon_customer_seq.nextval from dual";
		cr1 = db2.executeQuery(sql2);
		custid=cr1[0][0];
		System.out.println("customer if vb "+custid) ;
		}catch(Exception e){e.printStackTrace();}
		db2.closeConnection();
		return custid;
	}
	public String[][] getrepname(String repid) throws Exception
	{
		SVDBOperation db4= null;
		String sql6 = "";
		String cr4[][] = new String[0][0];
		String fname="";
		String lname="";
		String repname="";
		db4 = new SVDBOperation();
		db4.createConnection();
		//sql6 = "select SV_AVON_REP_FIRST_NAME, SV_AVON_REP_LAST_NAME, SV_AVON_REP_EMAIL from AVON_REP_MASTER where SV_AVON_REP_ID = '"+repid+"'";
		sql6 = "select NVL(initcap(sv_avon_rep_first_name),' '),NVL(initcap(sv_avon_rep_last_name),' '),NVL(sv_avon_rep_email,' '),NVL(sv_avon_rep_work_phone,' '),sv_avon_rep_id"+
	    " from avon_rep_master where sv_avon_rep_id = '"+ repid +"'" ;

		//logger.log(Level.DEBUG,sql6);
		cr4 = db4.executeQuery(sql6);

		db4.closeConnection();
		return cr4;
	}

	public String getCustomerName(String cusid) throws Exception
	{
		SVDBOperation db4= null;
		String sql6 = "";
		String cr4[][] = new String[0][0];
		String fname="";
		String lname="";
		String repname="";
		db4 = new SVDBOperation();
		db4.createConnection();
		sql6 = "select initcap(sv_customer_first_name) sv_customer_first_name,initcap(sv_customer_last_name) sv_customer_last_name from customer_master where sv_customer_id = '"+cusid+"'";
		ResultSet rs = db4.executeQueryRS(sql6);
		while(rs.next()){
			fname = rs.getString("sv_customer_first_name");
			lname = rs.getString("sv_customer_last_name");
		}
		db4.closeConnection();
		//return fname+" "+lname;
		return fname;
	}

	public String getcustprodrecflag(String fname, String lname, String email, String repid)
	{
		SVDBOperation db2= null;
		String sql1 = "";
		String cre[][] = new String[0][0];
		String prodrecflag="";
		boolean rs2 = true;
		try {
			db2 = new SVDBOperation();
			db2.createConnection();
	/*		sql1 = " select sv_customer_prodrec_flag from customer_master"
				+ " where lower(sv_customer_first_name) = lower('"
				+ fname + "') AND lower(sv_customer_last_name) = lower('"
					+ lname + "') AND lower(sv_customer_email) =lower('"
						+ email+"') AND sv_avon_rep_id = '"+repid+"' ";
   */
			sql1 = " select NVL(sv_avon_rep_status,' ') from avon_rep_master"
					+" where sv_avon_rep_id = '"+repid+"' ";

			//logger.log(Level.DEBUG," sql: " + sql1);
			cre = db2.executeQuery(sql1);
			prodrecflag=cre[0][0];
//			//logger.log(Level.DEBUG,"Custid in getcustdet loop is "+custid);
			}catch(Exception eeee){eeee.printStackTrace();
			//logger.log(Level.DEBUG,"The Customer is not registered. Can't retrieve the customer prod rec flag");}
			}
			db2.closeConnection();
		return prodrecflag;
	}

	public String getprodrecflag(String fname, String lname, String email, String repid)
	{
		SVDBOperation db2= null;
		String sql1 = "";
		String cre[][] = new String[0][0];
		String prodrecflag="";
		boolean rs2 = true;
		try {
			db2 = new SVDBOperation();
			db2.createConnection();
			sql1 = " select sv_customer_prodrec_flag from customer_master"
				+ " where lower(sv_customer_first_name) = lower('"
				+ fname + "') AND lower(sv_customer_last_name) = lower('"
					+ lname + "') AND lower(sv_customer_email) =lower('"
						+ email+"') AND sv_avon_rep_id = '"+repid+"' ";

			/*		sql1 = " select sv_avon_rep_status from avon_rep_master"
					+" where sv_avon_rep_id = '"+repid+"' ";
	*/
			//logger.log(Level.DEBUG," sql: " + sql1);
			cre = db2.executeQuery(sql1);
			prodrecflag=cre[0][0];
//			//logger.log(Level.DEBUG,"Custid in getcustdet loop is "+custid);
			}catch(Exception eeee){eeee.printStackTrace();
			//logger.log(Level.DEBUG,"The Customer is not registered. Can't retrieve the customer prod rec flag");}
			}
			db2.closeConnection();
		return prodrecflag;
	}

	public String saveCustomerProductOrders(String customerId, String repId, String productId, String prodLine, String orderFlag) throws Exception
	{
		SVDBOperation dbo = null;
		String sql = "";
		int i;
		String msg = "";

		dbo = new SVDBOperation();
		dbo.createConnection();

		try {
			sql = "insert into customer_product_orders values('"+customerId+"','"+repId+"',sysdate,'"+productId+"','"+prodLine+"','"+orderFlag+"')";
			//logger.log(Level.DEBUG,sql);
			i = dbo.executeUpdate(sql);

			if(i != 0)
				msg = "yes";
			else
				msg = "no";

		} catch(Exception e) {
			msg = "no";
			e.printStackTrace();
		}
		dbo.closeConnection();
		return msg;
	}

	public String saveCustomerColorProductOrders(String customerId, String repId, String productId, String lookId, String orderFlag) throws Exception
	{
		SVDBOperation dbo = null;
		String sql = "";
		int i;
		String msg = "";

		dbo = new SVDBOperation();
		dbo.createConnection();

		try {
			sql = "insert into customer_color_orders values('"+customerId+"','"+repId+"',sysdate,'"+productId+"','"+lookId+"','"+orderFlag+"')";
			//logger.log(Level.DEBUG,sql);
			i = dbo.executeUpdate(sql);

			if(i != 0)
				msg = "yes";
			else
				msg = "no";

		} catch(Exception e) {
			msg = "no";
			e.printStackTrace();
		}
		dbo.closeConnection();
		return msg;
	}

	/*public String getProfileStatus(String repId, String custId) {
		AvonwDBOperation db = new AvonwDBOperation();
		String sql = "";
		String rs[][] = new String[0][0];
		String status = "";

		try {
			db.createConnection();

			sql = "select NVL(sv_contact_purpose_id,' ') "+
				  "from customer_contact_history "+
				  "where sv_avon_rep_id = '"+repId+"' "+
				  "and sv_customer_id = '"+custId+"' "+
				  "and sv_contact_type_id = 'CT08' "+
				  "order by sv_contact_date desc";

			//logger.log(Level.DEBUG," sql: " + sql1);
			rs = db.executeQuery(sql);

			if(rs!=null && rs.length > 0)
				status = rs[0][0].trim();

		} catch(Exception ex) {
			status = "";
			//logger.log(Level.DEBUG,ex);
			ex.printStackTrace();
		}
		finally {
			db.closeConnection();
	    }

		return status;
	}*/

	public String[][] getCustomerDetailsForEmail(String custId){

	  	SVDBOperation dbo = null;
	    String [][] rs = new String[0][0];
	    String sql = "";

	    sql = "select NVL(initcap(sv_customer_first_name),' '),NVL(initcap(sv_customer_last_name),' '),NVL(sv_customer_email,' ')"+
		    " from customer_master where SV_CUSTOMER_ID = '"+ custId +"'" ;

	    try {
		    dbo = new SVDBOperation();
		    dbo.createConnection();
		    //logger.log(Level.DEBUG,sql);
		    rs = dbo.executeQuery(sql);

	     } catch (Exception ex1) {
	     	//logger.log(Level.DEBUG,ex1);
	     	ex1.printStackTrace();
	     }
	     finally {
			dbo.closeConnection();
		}
	    return rs;
	  }

	public String insertintotrack(String email,String repid,String status) {
		SVDBOperation db = null;
		String sql = "";
		String msg = "no";
		int i;

		try {
			db = new SVDBOperation();
			db.createConnection();

			sql = "insert into abp_customer_track values ('"+email+"',sysdate,'"+status+"','"+repid+"')";

			//logger.log(Level.DEBUG," sql: " + sql1);
			i = db.executeUpdate(sql);
			if(i!=0)
				msg = "yes";
			else
				msg = "no";

		} catch(Exception ex) {
			//logger.log(Level.DEBUG,"Error inserting the record into track table");
			msg = "no";
			ex.printStackTrace();
		}
		finally {
			db.closeConnection();
		}
		return msg;
	}

	public String updatetrack(String email) {
		SVDBOperation db = null;
		String sql = "";
		String msg = "no";
		int i;

		try {
			db = new SVDBOperation();
			db.createConnection();

			sql = "update abp_customer_track "+
				  "set sv_customer_track_date = sysdate, "+
				  "sv_customer_track_status = 'N' "+
				  "where sv_customer_email = '"+email+"'";

			//logger.log(Level.DEBUG," sql: " + sql1);
			i = db.executeUpdate(sql);
			if(i!=0)
				msg = "yes";
			else
				msg = "no";

		} catch(Exception ex) {
			//logger.log(Level.DEBUG,"Error inserting the record into track table");
			msg = "no";
			ex.printStackTrace();
		}
		finally {
			db.closeConnection();
		}
		return msg;
	}

	public String getTrackInfo(String str) {
		SVDBOperation db = null;
		String sql = "";
		String count = "";
		String[][] rs = new String[0][0];

		try {
			db = new SVDBOperation();
			db.createConnection();

			sql = "select count(*) from abp_customer_track where sv_customer_email = '"+str+"'";

			//logger.log(Level.DEBUG," sql: " + sql1);
			rs = db.executeQuery(sql);
			if(rs!=null && rs.length>0)
				count = rs[0][0].trim();

		} catch(Exception ex) {
			//logger.log(Level.DEBUG,"Error inserting the record into track table");
			count = "0";
			ex.printStackTrace();
		}
		finally {
			db.closeConnection();
		}
		return count;
	}

	public String deletetrack(String email) {
		SVDBOperation db = null;
		String sql = "";
		String msg = "no";
		int i;

		try {
			db = new SVDBOperation();
			db.createConnection();

			sql = "delete from abp_customer_track "+
				  "where sv_customer_email = '"+email+"'";

			//logger.log(Level.DEBUG," sql: " + sql1);
			i = db.executeUpdate(sql);
			if(i!=0)
				msg = "yes";
			else
				msg = "no";

		} catch(Exception ex) {
			//logger.log(Level.DEBUG,"Error inserting the record into track table");
			msg = "no";
			ex.printStackTrace();
		}
		finally {
			db.closeConnection();
		}
		return msg;
	}

}
