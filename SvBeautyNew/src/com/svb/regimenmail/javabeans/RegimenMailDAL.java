/*
 * Created on Apr 22, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.regimenmail.javabeans;

import java.util.ArrayList;
import com.svb.common.dba.SVDBOperation;
import com.svb.common.utils.SVAvonUtils;
import com.svb.common.valueobjects.*;

/**
 * @author vb
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RegimenMailDAL {
	public String[][] getMailDetails(String cusid,String repid,String mailid, String mailtype )
	{
		String rs[][] = new String[0][0] ;
		SVDBOperation dbo = new SVDBOperation() ;
		String sql = "" ;
		try
		{
			dbo.createConnection() ;
			sql = "select sv_mail_id, to_char(sv_mail_created_date), sv_mail_message, sv_mail_from, sv_mail_theme "
				 +" from customer_generated_mails "
				 +" where sv_customer_id = '"+cusid+"' and sv_avon_rep_id = '"+repid+"' "
				 +" and sv_mail_type = '"+mailtype+"' "
				 +" order by sv_mail_created_date desc " ;
			rs = dbo.executeQuery(sql) ;

		}catch (Exception e)
		{
			//logger.log(Level.DEBUG,e) ;
			e.printStackTrace();
		}
		finally
		{
			dbo.closeConnection() ;
		}
		return rs ;
	}
	public String getMailDate(String cusid,String repid,String mailid, String mailtype )
	{
		String rs[][] = new String[0][0] ;
		SVDBOperation dbo = new SVDBOperation() ;
		String sql = "" ;
		String maildate = "" ;
		try
		{
			dbo.createConnection() ;
			sql = "select max(to_char(sv_mail_created_date,'Mon-dd-yyyy')) "
				 +" from customer_generated_mails "
				 +" where sv_customer_id = '"+cusid+"' and sv_avon_rep_id = '"+repid+"' "
				 +" and sv_mail_type = '"+mailtype+"' "
				 +" and sv_mail_created_date > sysdate -30 " ;
			rs = dbo.executeQuery(sql) ;
			if (rs != null && rs.length >0 )
				maildate = rs[0][0] ;
		}catch (Exception e)
		{
			//logger.log(Level.DEBUG,e) ;
			e.printStackTrace();
		}
		finally
		{
			dbo.closeConnection() ;
		}
		return maildate ;
	}

	public ArrayList getCustDetForEmail(String custId){

	  	ArrayList customersdet = new ArrayList();

	  	SVDBOperation dbo = null;

	    String [][] rs = new String[0][0];

	    StringBuffer custlist = new StringBuffer() ;
	    String sql="";

	    sql="select SV_CUSTOMER_ID,NVL(initcap(sv_customer_first_name),' '),NVL(initcap(sv_customer_last_name),' '),sv_customer_email,NVL(sv_customer_gender,'F')"+
		    " from customer_master where SV_CUSTOMER_ID = '"+ custId +"'" ;

	    try {
		    dbo = new SVDBOperation();
		    dbo.createConnection();
		    //logger.log(Level.DEBUG,sql);
		    rs = dbo.executeQuery(sql);
		    customersdet = setCustomerVo(rs);

	     } catch (Exception ex1) {
	     	//logger.log(Level.DEBUG,ex1);
	     	ex1.printStackTrace();
	     }
	     finally {
			dbo.closeConnection();
		}
	    return customersdet;
	  }

	protected ArrayList setCustomerVo(String[][] rs) {
		ArrayList list = new ArrayList();
		CustomerVO customerVO = new CustomerVO();

		customerVO.setCustId(rs[0][0].trim());
		customerVO.setFirstName(rs[0][1].trim());
		customerVO.setLastName(rs[0][2].trim());
		customerVO.setEmail(rs[0][3]);
		customerVO.setGender(rs[0][4]);

		list.add(customerVO);

		return list;

	  }

	public CustomerVO getCustDetails(String custId){

	  	SVDBOperation dbo = null;
	    String [][] rs = new String[0][0];
	    String sql = "";
	    CustomerVO customerVO = new CustomerVO();

	    sql = "select SV_CUSTOMER_ID,NVL(initcap(sv_customer_first_name),' '),NVL(initcap(sv_customer_last_name),' '),sv_customer_email"+
		    " from customer_master where SV_CUSTOMER_ID = '"+ custId +"'" ;

	    try {
		    dbo = new SVDBOperation();
		    dbo.createConnection();
		    //logger.log(Level.DEBUG,sql);
		    rs = dbo.executeQuery(sql);

		    customerVO.setCustId(rs[0][0].trim());
			customerVO.setFirstName(rs[0][1].trim());
			customerVO.setLastName(rs[0][2].trim());
			customerVO.setEmail(rs[0][3]);

	     } catch (Exception ex1) {
	     	//logger.log(Level.DEBUG,ex1);
	     	ex1.printStackTrace();
	     }
	     finally {
			dbo.closeConnection();
		}
	    return customerVO;
	  }

	public String getCustEthnicity(String custId){

	  	String ethnicity = "N003";
	  	SVDBOperation dbo = null;
	  	String [][] rs = new String[0][0];
	    String sql="";

	    sql="select SV_CUSTOMER_ETHNICITY "+
		    " from customer_master where SV_CUSTOMER_ID = '"+ custId +"'" ;

	    try {
		    dbo = new SVDBOperation();
		    dbo.createConnection();
		    //logger.log(Level.DEBUG,sql);
		    rs = dbo.executeQuery(sql);

		    if(rs!=null && rs.length >0)
		    	if(rs[0][0] != null)
		    		ethnicity = rs[0][0];
		    	else
		    		ethnicity = "N003";

	     } catch (Exception ex1) {
	     	ethnicity = "N003";
	     	//logger.log(Level.DEBUG,ex1);
	     }
	     finally {
			dbo.closeConnection();
		}
	    return ethnicity;
	  }

	public RepVO getRepDetails(String repId){

		RepVO repVO = new RepVO();
	  	SVDBOperation dbo = null;
	    String [][] rs = new String[0][0];
	    String sql = "";

	    sql = " select a.sv_avon_rep_id,NVL(initcap(a.sv_avon_rep_first_name),' '),NVL(initcap(a.sv_avon_rep_last_name),' '), "+
		      " NVL(a.sv_avon_rep_email,' '),NVL(a.sv_avon_rep_work_phone,' '), NVL(b.sv_rep_beauty_advisor_ind,' ') "+
		      " from avon_rep_master a, avon_rep_details b "+
			  " where a.SV_AVON_REP_ID = '"+ repId +"' and a.sv_avon_rep_id = b.sv_avon_rep_id " ;

	    try {
		    dbo = new SVDBOperation();
		    dbo.createConnection();
		    //logger.log(Level.DEBUG,sql);
		    rs = dbo.executeQuery(sql);

		    if(rs!=null && rs.length >0) {
		    	repVO.setRepId(rs[0][0].trim());
		    	repVO.setFirstName(rs[0][1].trim());
		    	repVO.setLastName(rs[0][2].trim());
		    	repVO.setEmail(rs[0][3].trim());
		    	repVO.setWorkPhone(rs[0][4].trim());
		    	repVO.setRepType(rs[0][5].trim());
		    } else {
		    	repVO.setRepId("");
		    	repVO.setFirstName("");
		    	repVO.setLastName("");
		    	repVO.setEmail("");
		    	repVO.setWorkPhone("");
		    	repVO.setRepType("");
		    }

	     } catch (Exception ex1) {
	     	//logger.log(Level.DEBUG,ex1);
	     	ex1.printStackTrace();
	     }
	     finally {
			dbo.closeConnection();
		}
	    return repVO;
	  }

	public RepPreferencesVO getRepPreferences(String repId){

		RepPreferencesVO repVO = new RepPreferencesVO();
	  	SVDBOperation dbo = null;
	    String [][] rs = new String[0][0];
	    String sql = "";
	    SVAvonUtils avonUtil = new SVAvonUtils();

	    sql = "select sv_avon_rep_id, NVL(sv_rep_prefer_name,' '), NVL(sv_rep_prefer_email,' '),"
				+ " NVL(sv_rep_prefer_signature,' '), NVL(sv_rep_prefer_phone_number,' '),"
				+ " NVL(sv_rep_customer_salutation,' '), NVL(sv_avon_rep_pbs_email,' ') "
				+ " from avon_rep_preferences where SV_AVON_REP_ID = '"+ repId +"'" ;

	    try {
		    dbo = new SVDBOperation();
		    dbo.createConnection();
		    //logger.log(Level.DEBUG,sql);
		    rs = dbo.executeQuery(sql);
		    String preferName = "";

		    if(rs!=null && rs.length >0) {
		    	repVO.setRepId(rs[0][0].trim());
		    	preferName = rs[0][1].trim();
		    	if(!preferName.equals(""))
		    		preferName = avonUtil.formatName(preferName);

		    	repVO.setRepSignature(preferName);
		    	repVO.setRepName(preferName);
		    	repVO.setRepEmail(rs[0][2].trim());
		    	repVO.setRepClosing(rs[0][3].trim());
		    	repVO.setRepPhoneNum(rs[0][4].trim());
		    	repVO.setRepSalutation(rs[0][5].trim());
		    	repVO.setRepPBSEmail(rs[0][6].trim());
		    } else {
		    	repVO.setRepId("");
		    	repVO.setRepSignature("");
		    	repVO.setRepName("");
		    	repVO.setRepEmail("");
		    	repVO.setRepClosing("");
		    	repVO.setRepPhoneNum("");
		    	repVO.setRepSalutation("");
		    	repVO.setRepPBSEmail("");
		    }

	     } catch (Exception ex1) {
	     	//logger.log(Level.DEBUG,ex1);
	     	ex1.printStackTrace();
	     }
	     finally {
			dbo.closeConnection();
		}
	    return repVO;
	  }

	public String insertRepPreferences(String repId, String name, String email, String signature, String phonenum, String salutation){

		SVDBOperation dbo = null;
	    int result;
	    String sql = "";
	    String msg = "";

	    name = name.replaceAll("'","''");
	    email = email.replaceAll("'","''");
	    signature = signature.replaceAll("'","''");

	    sql = "insert into avon_rep_preferences values ('"+repId+"','"+name+"',"
				+"'"+email+"','"+signature+"','"+phonenum+"',sysdate,'','"+salutation+"','')";

	    try {
		    dbo = new SVDBOperation();
		    dbo.createConnection();
		    //logger.log(Level.DEBUG,sql);
		    result = dbo.executeUpdate(sql);
		    if(result != 0)
		    	msg = "yes";
		    else
		    	msg = "no";

	     } catch (Exception ex1) {
	     	msg = "no";
	     	//logger.log(Level.DEBUG,ex1);
	     	ex1.printStackTrace();
	     }
	     finally {
			dbo.closeConnection();
	     }

	     return msg;
	  }

	public String updateRepPreferences(String repId, String name, String email, String signature, String phonenum, String salutation){

		SVDBOperation dbo = null;
	    int result;
	    String sql = "";
	    String msg = "";

	    name = name.replaceAll("'","''");
	    email = email.replaceAll("'","''");
	    signature = signature.replaceAll("'","''");

	    sql = "update avon_rep_preferences set sv_rep_prefer_name = '"+name+"'"
				+ ", sv_rep_prefer_email = '"+email+"'"
				+ ", sv_rep_prefer_signature = '"+signature+"'"
				+ ", sv_rep_prefer_phone_number = '"+phonenum+"'"
				+ ", sv_rep_prefer_date = sysdate"
				+ ", sv_rep_customer_salutation = '"+salutation+"'"
				+ " where sv_avon_rep_id = '"+repId+"'";

	    try {
		    dbo = new SVDBOperation();
		    dbo.createConnection();
		    //logger.log(Level.DEBUG,sql);
		    result = dbo.executeUpdate(sql);
		    if(result != 0)
		    	msg = "yes";
		    else
		    	msg = "no";

	     } catch (Exception ex1) {
	     	msg = "no";
	     	//logger.log(Level.DEBUG,ex1);
	     	ex1.printStackTrace();
	     }
	     finally {
			dbo.closeConnection();
	     }

	     return msg;
	  }

	public String getCustEmailStatus(String custId){

	  	String emailStatus = "";
	  	SVDBOperation dbo = null;
	  	String [][] rs = new String[0][0];
	    String sql="";

	    sql = "select sv_customer_email_status "+
		    " from customer_master where sv_customer_id = '"+custId+"'" ;

	    try {
		    dbo = new SVDBOperation();
		    dbo.createConnection();
		    //logger.log(Level.DEBUG,sql);
		    rs = dbo.executeQuery(sql);

		    if(rs!=null && rs.length >0)
		    	if(rs[0][0] != null)
		    		emailStatus = rs[0][0];

	     } catch (Exception ex1) {
	     	emailStatus = "";
	     	//logger.log(Level.DEBUG,ex1);
	     }
	     finally {
			dbo.closeConnection();
		}
	    return emailStatus;
	  }

	public String getRepPBSEmail(String repId){

		RepPreferencesVO repVO = new RepPreferencesVO();
	  	SVDBOperation dbo = null;
	    String [][] rs = new String[0][0];
	    String sql = "";
	    String pbsemail = "";

	    sql = "select sv_avon_rep_id, NVL(sv_avon_rep_pbs_email,' ') "
				+ " from avon_rep_preferences where SV_AVON_REP_ID = '"+ repId +"'" ;

	    try {
		    dbo = new SVDBOperation();
		    dbo.createConnection();
		    //logger.log(Level.DEBUG,sql);
		    rs = dbo.executeQuery(sql);

		    if(rs!=null && rs.length >0) {
		    	pbsemail = rs[0][1].trim();
		    }

	     } catch (Exception ex1) {
	     	pbsemail = "";
	     	//logger.log(Level.DEBUG,ex1);
	     	ex1.printStackTrace();
	     }
	     finally {
			dbo.closeConnection();
		}
	    return pbsemail;
	  }


}
