package com.svb.common.mail;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * <p>
 * Company:
 * </p>
 *
 * @author not attributable
 * @version 1.0
 */

import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;

import com.svb.common.ConstantDeclarations;
import com.svb.common.dba.SVDBOperation;


public class ManageMail {
	SVDBOperation dbo = null;

	String result[][] = null;

	String sql = null;

	public String saveMail(String cusid, String subj, String mailtype,
			String prodids,String messageSent, String repid, String personalMsg, String fromMsg, String themeMsg) {
		SVDBOperation dbo = null;
		String sql = "";
		String msg = "";
		String mailId = "";
		if(messageSent == null || messageSent.trim()==""){
			messageSent = "N";
		}
		result = new String[0][0];
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			sql = " select avon_mail_seq.nextval from dual ";
			ResultSet rs = dbo.executeQueryRS(sql);
			while(rs.next()){
				mailId = rs.getString(1);
			}

			fromMsg = fromMsg.replaceAll("'","''");
			themeMsg = themeMsg.replaceAll("'","''");

			//logger.log(Level.DEBUG,"mail type " + mailtype);
				sql = " insert into customer_generated_mails  values ('"
						+ mailId +"', '" + mailtype + "', '"
						+ cusid + "' , sysdate , '" + subj + "', '"+messageSent+"', '"
						+ prodids + "', '"+repid+"' , ' ', '"+personalMsg+"', '"+fromMsg+"', '"+themeMsg+"'  )";
				//logger.log(Level.DEBUG,sql);
				int i5 = dbo.executeUpdate(sql);
				if(i5!=0){
					//msg = "Yes";
					msg = mailId;
				}
				else msg = "No";
		} catch (Exception ex1) {
			msg = "No";
			//logger.log(Level.DEBUG," from insert customer Record");
			////logger.log(Level.DEBUG,ex1);
			ex1.printStackTrace();
		}
		finally {
			dbo.closeConnection() ;
		}
		return msg;
	}

	public String saveMail(String cusid, String subj, String mailtype,
			String prodids,String messageSent, String repid) {
		SVDBOperation dbo = null;
		String sql = "";
		String msg = "";
		String mailId = "";
		if(messageSent == null || messageSent.trim()==""){
			messageSent = "N";
		}
		result = new String[0][0];
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			sql = " select avon_mail_seq.nextval from dual ";
			ResultSet rs = dbo.executeQueryRS(sql);
			while(rs.next()){
				mailId = rs.getString(1);
			}
			//logger.log(Level.DEBUG,"mail type " + mailtype);
			//if (!mailtype.equals("R")) {
				sql = " insert into customer_generated_mails  values ('"
					    + mailId +"', '" + mailtype + "', '"
						+ cusid + "' , sysdate , '" + subj + "', '"+messageSent+"', '"
						+ prodids + "', '"+repid+"' , ' ', ' ', ' ', ' '  )";
				//logger.log(Level.DEBUG,sql);
				int i5 = dbo.executeUpdate(sql);
			//}

			msg = "Yes";
		} catch (Exception ex1) {
			msg = "No";
			////logger.log(Level.DEBUG," from insert customer Record");
			////logger.log(Level.DEBUG,ex1);
			ex1.printStackTrace();
		}
		finally {
			dbo.closeConnection() ;
		}
		return mailId;
	}

	public void updateMail(String mailId) {
		SVDBOperation dbo = null;
		String sql = "";
		String msg = "";
		String mailids[] = new String[0];
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			String mailflag = "";
			String scrname = " ";
			//logger.log(Level.DEBUG,mailids.length);
			sql = " update customer_generated_mails  "
							+ "  set sv_mail_flag = '" + ConstantDeclarations.EMAIL_STATUS_SEND_ID
							+ "' where sv_mail_id = '" + mailId
							+ "' ";
					//logger.log(Level.DEBUG,sql);
					int i6 = dbo.executeUpdate(sql);
					msg = "Yes";
		} catch (Exception ex1) {
			msg = "No";
			ex1.printStackTrace();
		}
		finally {
			dbo.closeConnection() ;
		}

	}

	public void updateMail(HttpServletRequest request) {
		SVDBOperation dbo = null;
		String sql = "";
		String msg = "";
		String mailids[] = new String[0];
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			String mailflag = "";
			String scrname = " ";
			if (request.getParameter("scrname") != null)
				scrname = request.getParameter("scrname");
			if (scrname.equals("mailsarray"))
				mailflag = "S";
			else
				mailflag = "D";
			if (request.getParameterValues("additem") != null)
				mailids = request.getParameterValues("additem");
			//logger.log(Level.DEBUG,mailids.length);
			for (int i = 0; i < mailids.length; i++) {
				if (!mailids[i].trim().equals("novalue")) {
					sql = " update customer_generated_mails  "
							+ "  set sv_mail_flag = '" + mailflag
							+ "' where sv_mail_id = '" + mailids[i].trim()
							+ "' ";
					//logger.log(Level.DEBUG,sql);
					int i6 = dbo.executeUpdate(sql);
					msg = "Yes";
				}
			}
		} catch (Exception ex1) {
			msg = "No";
			ex1.printStackTrace();
		}
		finally {
			dbo.closeConnection() ;
		}

	}

	public void savePersonalMail(String mailto, String subj, String mailtype,
			String prodids, String mailfrom, String mailmessage, String theme) {
		SVDBOperation dbo = null;
		String sql = "";
		String msg = "";
		result = new String[0][0];
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();

			sql = " insert into customer_generated_mails  values ("
					+ "  avon_mail_seq.nextval, '" + mailtype + "', '" + mailto
					+ "' , sysdate , '" + subj + "', 'N', '" + prodids
					+ "', ' ' , ' ', '" + mailmessage + "', '" + mailfrom
					+ "', '" + theme + "'  )";
			//logger.log(Level.DEBUG,sql);
			int i55 = dbo.executeUpdate(sql);
			msg = "Yes";
		} catch (Exception ex1) {
			msg = "No";
			////logger.log(Level.DEBUG," from insert customer Record");
			////logger.log(Level.DEBUG,ex1);
			ex1.printStackTrace();
		}
		finally {
			dbo.closeConnection() ;
		}

	}

	public String[][] getMail(String id, String repid) {
		SVDBOperation dbo = null;
		String sql = "";
		String msg = "";
		String[][] rs = new String[0][0];
		String option1 = "";
		if (id.trim().equals("yes") || id.trim().equals("no"))
			option1 = "b.sv_mail_id";
		else if (id.trim().equals("first"))
			option1 = "a.sv_customer_first_name";
		else if (id.trim().equals("last"))
			option1 = "a.sv_customer_last_name";
		else if (id.trim().equals("status"))
			option1 = "b.sv_mail_type";
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();

			sql = " select NVL(a.sv_customer_first_name,'no'), NVL(a.sv_customer_last_name,'no'), "
					+ " b.sv_mail_id, NVL(b.sv_mail_subject,'no subject'), "
					+ " NVL(b.sv_mail_type,' '), NVL(b.sv_mail_product_ids,' '), "
					+ " NVL(b.sv_mail_message,'no mesage'), "
					+ " NVL(b.sv_mail_theme,'no theme'),NVL(b.sv_mail_from,' ') "
					+ " from customer_master a, customer_generated_mails b, avon_rep_customer c "
					+ " where a.sv_customer_id = b.sv_customer_id "
					+ " and a.sv_customer_id = c.sv_customer_id "
					+ " and c.sv_avon_rep_id = '"
					+ repid
					+ "' "
					+ " and b.sv_mail_flag <> 'D' and b.sv_mail_flag <> 'S' "
					+ " order by " + option1 + " desc";
			//logger.log(Level.DEBUG,sql);
			rs = dbo.executeQuery(sql);
		} catch (Exception ex1) {
			msg = "No";
			////logger.log(Level.DEBUG," from insert customer Record");
			////logger.log(Level.DEBUG,ex1);
			ex1.printStackTrace();
		}
		finally {
			dbo.closeConnection() ;
		}
		return rs;
	}

}