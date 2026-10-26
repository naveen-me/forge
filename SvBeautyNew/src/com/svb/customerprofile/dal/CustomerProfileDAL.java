/*
 * Created on Mar 29, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.customerprofile.dal;

/**
 * @author Neelima
 *
**/

import java.util.*;

import java.sql.ResultSet;

import com.svb.common.dba.SVDBOperation;
import com.svb.common.valueobjects.BrandVO;
import com.svb.common.valueobjects.CustProfileVO;
import com.svb.common.valueobjects.ProductVO;
import com.svb.customerprofile.formbeans.ProfileSummaryFormBean;
import com.svb.common.utils.SVAvonUtils;

public class CustomerProfileDAL {

	SVDBOperation dbo = null;
	String result[][] = null;
	String sql = null;
	private CustProfileVO pdl = new CustProfileVO();
	SVAvonUtils svUtil = new SVAvonUtils();

	public String getProfileData(ProfileSummaryFormBean profilesForm, String cusid,String repid) {
		String message = "";
		int j = 0;
		String skintype = "";
		String natural = "" ;
		String age = "";
		String gender = "" ;
		String ethnicity = "" ;
		String dummy1[] = null;
		String dummy = null;
		String skincondition[] = new String[0];
		String skinconcern[] = new String[0];
		String eyeconcern[] = new String[0];
		String allergy[] = new String[0];
		String specification[] = new String[0];

        String haircolor = "" ;
		String hairprocess[] = new String[0];
        String hairthickness = "" ;
        String hairtexture = "" ;
		String hairconditions[] = new String[0];
		String hairstyles[] = new String[0];
        String hairfinish = "" ;
        String scrname = "" ;
        
		if (profilesForm.getScrname() != null) {
			scrname = profilesForm.getScrname();
		}


		    //message = insCusRecord(profilesForm, cusid,repid); // check for condition
           if (scrname.equals("hairprofile"))
           {
			 message = deleteHairProfileRecord(cusid, "7x",repid);
           }
           else
			 message = deleteSkinProfileRecord(cusid, "5x",repid);
			
			if (profilesForm.getEthnicity() != null) {
				ethnicity = profilesForm.getEthnicity();
				message = skinbasicRecord(cusid, "58", ethnicity, dummy1,repid);
			}
			if (profilesForm.getNatural() != null && profilesForm.getNatural().length() > 0) {
				natural = profilesForm.getNatural();
				message = skinbasicRecord(cusid, "57", natural, dummy1,repid);
			}
			if (profilesForm.getGender() != null && profilesForm.getGender().length() > 0) {
				gender = profilesForm.getGender();
				message = skinbasicRecord(cusid, "59", gender, dummy1, repid);
			}
			if (profilesForm.getAge() != null && profilesForm.getAge().length() > 0) {
				age = profilesForm.getAge();
				message = skinbasicRecord(cusid, "55", age, dummy1,repid);
			}
			if (profilesForm.getAllergy() != null) {
				allergy = profilesForm.getAllergy();
				message = skinbasicRecord(cusid, "56", dummy, allergy,repid);
			}
			if (profilesForm.getSkin_type() != null && profilesForm.getSkin_type().length() >0) {
				skintype = profilesForm.getSkin_type();
				message = skinbasicRecord(cusid, "51", skintype, dummy1,repid);
			}
			if (profilesForm.getSkin_concern() != null) {
				skinconcern = profilesForm.getSkin_concern();
				message = skinbasicRecord(cusid, "53", dummy, skinconcern,repid);
			}
			if (profilesForm.getEye_concern() != null) {
				eyeconcern = profilesForm.getEye_concern();
				message = skinbasicRecord(cusid, "54", dummy, eyeconcern,repid);
			}
			if (profilesForm.getSpecification() != null) {
				specification = profilesForm.getSpecification();
				message = skinbasicRecord(cusid, "60", dummy, specification,repid);
			}

			
			if (profilesForm.getHair_Color() != null && profilesForm.getHair_Color().length() > 0) {
				haircolor = profilesForm.getHair_Color();
				message = hairBaiscRecord(cusid, "71", haircolor, dummy1,repid);
			}
/*			if (profilesForm.getHair_color() != null) {
				haircolor = profilesForm.getSkin_type();
				message = hairBaiscRecord(cusid, "72", haircolor, dummy1,repid);
			}
*/			
			if (profilesForm.getHair_Process() != null) {
				hairprocess = profilesForm.getHair_Process();
				message = hairBaiscRecord(cusid, "73", dummy, hairprocess,repid);
			}
			if (profilesForm.getHair_Thickness() != null && profilesForm.getHair_Thickness().length() > 0) {
				hairthickness = profilesForm.getHair_Thickness();
				message = hairBaiscRecord(cusid, "74", hairthickness, dummy1,repid);
			}
			if (profilesForm.getHair_Texture() != null && profilesForm.getHair_Texture().length() > 0) {
				hairtexture = profilesForm.getHair_Texture();
				message = hairBaiscRecord(cusid, "75", hairtexture, dummy1,repid);
			}
			if (profilesForm.getHair_Conditions() != null) {
				hairconditions = profilesForm.getHair_Conditions();
				message = hairBaiscRecord(cusid, "77", dummy, hairconditions,repid);
			}
			if (profilesForm.getHair_Styles() != null) {
				hairstyles = profilesForm.getHair_Styles();
				message = hairBaiscRecord(cusid, "76", dummy, hairstyles,repid);
			}
			if (profilesForm.getHair_Finish() != null && profilesForm.getHair_Finish().length() > 0) {
				hairfinish = profilesForm.getHair_Finish();
				message = hairBaiscRecord(cusid, "78", hairfinish, dummy1,repid);
			}
			
		return message;
	}
	
	public String getProfileDataSingleProduct(ProfileSummaryFormBean profilesForm, String cusid,String repid) {
		System.out.println("getProfileDataSingleProduct :: ");
		
		//Removed unused variables for performance improvement - Shivam - 22/07/2024
		String message = "";
		String skintype = "";
		String natural = "" ;
		String age = "";
		String gender = "" ;
		String ethnicity = "" ;
		String dummy1[] = null;
		String dummy = null;
		String skinconcern[] = new String[0];
		String eyeconcern[] = new String[0];
		String allergy[] = new String[0];
		String specification[] = new String[0];

        String scrname = "" ;
        StringBuilder sql = new StringBuilder("");
        String sqlH = "";
        
		if (profilesForm.getScrname() != null) {
			scrname = profilesForm.getScrname();
		}

		Long start = System.currentTimeMillis();
		    //message = insCusRecord(profilesForm, cusid,repid); // check for condition
           if (scrname.equals("hairprofile"))
           {
			 message = deleteHairProfileRecord(cusid, "7x",repid);
           }
           else
			 message = deleteSkinProfileRecord(cusid, "5x",repid);
		Long end = System.currentTimeMillis();
		System.out.println("deleting profile records takes "+(end - start));
		start = System.currentTimeMillis();
			if (profilesForm.getEthnicity() != null) {
				ethnicity = profilesForm.getEthnicity();
				skinbasicRecordQuery(cusid, "58", ethnicity, dummy1,repid, sql);
			}
			if (profilesForm.getNatural() != null && profilesForm.getNatural().length() > 0) {
				natural = profilesForm.getNatural();
				skinbasicRecordQuery(cusid, "57", natural, dummy1,repid, sql);
			}
			if (profilesForm.getGender() != null && profilesForm.getGender().length() > 0) {
				gender = profilesForm.getGender();
				skinbasicRecordQuery(cusid, "59", gender, dummy1, repid, sql);
			}
			if (profilesForm.getAge() != null && profilesForm.getAge().length() > 0) {
				age = profilesForm.getAge();
				skinbasicRecordQuery(cusid, "55", age, dummy1,repid, sql);
			}
			if (profilesForm.getAllergy() != null) {
				allergy = profilesForm.getAllergy();
				skinbasicRecordQuery(cusid, "56", dummy, allergy,repid, sql);
			}
			if (profilesForm.getSkin_type() != null && profilesForm.getSkin_type().length() >0) {
				skintype = profilesForm.getSkin_type();
				skinbasicRecordQuery(cusid, "51", skintype, dummy1,repid, sql);
			}
			if (profilesForm.getSkin_concern() != null) {
				skinconcern = profilesForm.getSkin_concern();
				skinbasicRecordQuery(cusid, "53", dummy, skinconcern,repid, sql);
			}
			if (profilesForm.getEye_concern() != null) {
				eyeconcern = profilesForm.getEye_concern();
				skinbasicRecordQuery(cusid, "54", dummy, eyeconcern,repid, sql);
			}
			if (profilesForm.getSpecification() != null) {
				specification = profilesForm.getSpecification();
				skinbasicRecordQuery(cusid, "60", dummy, specification,repid, sql);
			}

			
//			if (profilesForm.getHair_Color() != null && profilesForm.getHair_Color().length() > 0) {
//				haircolor = profilesForm.getHair_Color();
//				sqlH = hairBaiscRecordQuery(cusid, "71", haircolor, dummy1,repid, sqlH);
//			}
/*			if (profilesForm.getHair_color() != null) {
				haircolor = profilesForm.getSkin_type();
				sql = hairBaiscRecord(cusid, "72", haircolor, dummy1,repid);
			}
*/			
//			if (profilesForm.getHair_Process() != null) {
//				hairprocess = profilesForm.getHair_Process();
//				sqlH = hairBaiscRecordQuery(cusid, "73", dummy, hairprocess,repid, sqlH);
//			}
//			if (profilesForm.getHair_Thickness() != null && profilesForm.getHair_Thickness().length() > 0) {
//				hairthickness = profilesForm.getHair_Thickness();
//				sqlH = hairBaiscRecordQuery(cusid, "74", hairthickness, dummy1,repid, sqlH);
//			}
//			if (profilesForm.getHair_Texture() != null && profilesForm.getHair_Texture().length() > 0) {
//				hairtexture = profilesForm.getHair_Texture();
//				sqlH = hairBaiscRecordQuery(cusid, "75", hairtexture, dummy1,repid, sqlH);
//			}
//			if (profilesForm.getHair_Conditions() != null) {
//				hairconditions = profilesForm.getHair_Conditions();
//				sqlH = hairBaiscRecordQuery(cusid, "77", dummy, hairconditions,repid, sqlH);
//			}
//			if (profilesForm.getHair_Styles() != null) {
//				hairstyles = profilesForm.getHair_Styles();
//				sqlH = hairBaiscRecordQuery(cusid, "76", dummy, hairstyles,repid, sqlH);
//			}
//			if (profilesForm.getHair_Finish() != null && profilesForm.getHair_Finish().length() > 0) {
//				hairfinish = profilesForm.getHair_Finish();
//				sqlH = hairBaiscRecordQuery(cusid, "78", hairfinish, dummy1,repid, sqlH);
//			}
			
//			System.out.println("SQL PROFILE DTA :: " + sql.toString());
			System.out.println("SQL PROFILE DTA :: " + sqlH);
			sql.append(" from dual ) SELECT * FROM names");
			System.out.println("SQL PROFILE DTA :: " + sql.toString());
			end = System.currentTimeMillis();
			System.out.println("traversing through if takes "+(end-start));
			message = singleProdSkinhairBasicRecords(sql.toString());
			if(sqlH != null && sqlH.length() > 0) {
				System.out.println("IN SQLH :: ");
				message = singleProdSkinhairBasicRecords(sqlH +" from dual ) SELECT * FROM names");
			}
			
			
		return message;
	}
	
	public StringBuilder skinbasicRecordQuery(String cusid, String attrid, String attrval,
			String attrvals[],String repid, StringBuilder sql) {
		String sqlLocal = "";
		String msg = "";
		String attrid_2 = "" ;
		try {
			if ((attrvals == null) && (attrval != null)) {
				attrvals = new String[1];
				attrvals[0] = attrval;
			}
		//	System.out.println("Attr val ==> "+attrvals[0]) ;
//			System.out.println("New SQL ==> " + sql) ;
			for (int j = 0; j < attrvals.length; j++) {
				if (attrid.substring(0, 1).equals("5") || attrid.substring(0, 1).equals("6") || attrid.substring(0, 1).equals("9"))
				{
					attrid_2 = attrid ;
					if (attrvals[j].substring(0, 1).equals("D"))
						attrid_2 = "52" ;
					else if(attrvals[j].substring(0, 2).equals("CE") || attrvals[j].substring(0, 2).equals("RE") || attrvals[j].substring(0, 2).equals("MF")) {
						attrid_2 = "60" ;
					}
					System.out.println("SQL LENGTH +++++++> "+sql.length()) ;
					System.out.println("SQL EQUAL +++++++> "+sql.toString().equals("")) ;
					if(sql.length() > 0 && !sql.toString().equals("")) {
						
						sql.append("from dual union all select '" + cusid + "','01', '" + attrid_2 + "','" + attrvals[j] + "', sysdate ");
					}
					else {
						sql.append("insert into customer_basic_profile(SV_CUSTOMER_ID, SV_CUSTOMER_MODULE_ID, SV_CUSTOMER_PROFILE_CODE, SV_CUSTOMER_PROFILE_ATTRIBUTE, SV_CUSTOMER_PROFILE_DATE)"
								+ " with names as ("+
							      " select '" + cusid + "','01', '" + attrid_2 + "','" + attrvals[j] + "', sysdate ");
					}
					
					System.out.println("SQL +++++++> "+sql.toString()) ;
//					sql += sqlLocal+";";
				}
	//			dbo.commit() ;
			}
			msg = "Yes";
		} catch (Exception ex1) {
			msg = "No";
			////logger.log(Level.DEBUG," from inset customerSkin Record");
			////logger.log(Level.DEBUG,ex1);
			ex1.printStackTrace();

		}
		return sql;
	}
	
	public String hairBaiscRecordQuery(String cusid, String attrid, String attrval,
			String attrvals[],String repid, String sql) {
		SVDBOperation dbo = null;
		String sqlLocal = "";
		String msg = "";
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			if ((attrvals == null) && (attrval != null)) {
				attrvals = new String[1];
				attrvals[0] = attrval;
			}
			for (int j = 0; j < attrvals.length; j++) {
				if (attrid.substring(0, 1).equals("7"))
				{
//					sqlLocal = "insert into customer_hair_profile values "+
//					      " ('" + cusid + "','02', '" + attrid + "','" + attrvals[j] + "', sysdate) ";
//					sql += sqlLocal+";";
//					System.out.println("SQL HAIR +++++++> "+sql) ;
					
					if(sql == null || sql.length() == 0 || sql == "") {
						sql = "insert into customer_hair_profile values "+
							      " ('" + cusid + "','02', '" + attrid + "','" + attrvals[j] + "', sysdate) ";
					}
					else {
						sql += sql + ", ('" + cusid + "','02', '" + attrid + "','" + attrvals[j] + "', sysdate) ";
					}
					if(sql == null || sql.length() == 0 || sql == "") {
						sql = "insert into customer_hair_profile values(SV_CUSTOMER_ID, SV_CUSTOMER_MODULE_ID, SV_CUSTOMER_PROFILE_CODE, SV_CUSTOMER_PROFILE_ATTRIBUTE, SV_CUSTOMER_PROFILE_DATE)"
								+ " with names as ("+
							      " select '" + cusid + "','02', '" + attrid + "','" + attrvals[j] + "', sysdate ";
					}
					else {
						sql += "from dual union all select '" + cusid + "','02', '" + attrid + "','" + attrvals[j] + "', sysdate ";
					}
				}
//				int i1 = dbo.executeUpdate(sql);
	//			dbo.commit() ;
			}
			msg = "Yes";
		} catch (Exception ex1) {
			msg = "No";
			////logger.log(Level.DEBUG," from inset customerSkin Record");
			////logger.log(Level.DEBUG,ex1);
			ex1.printStackTrace();

		}
//		finally {
//			dbo.closeConnection() ;
//		}
		return sql;
	}

	public String updateMasterRecord(String cusid, String repid) {
		SVDBOperation dbo = null;
		String sql = "";
		String msg = "";
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();

			sql = " update customer_master  "
				+ " set sv_customer_profile_flag = 'Y', sv_customer_entry_date = sysdate "
				+ " where sv_customer_id = '" + cusid + "' "
				+ " and sv_avon_rep_id = '"+repid+"' " ;
			int i5 = dbo.executeUpdate(sql);
			msg = "Yes";
		} catch (Exception ex1) {
			msg = "No";
			////logger.log(Level.DEBUG," from update customer Record");
			////logger.log(Level.DEBUG,ex1);
			ex1.printStackTrace();
		}
		finally {
			dbo.closeConnection() ;
		}
		return msg;
	}

	public String updateEntryDate(String cusid, String repid) {
		SVDBOperation dbo = null;
		String sql = "";
		String msg = "";
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();

			sql = " update customer_master  "
				+ " set sv_customer_entry_date = sysdate "
				+ " where sv_customer_id = '" + cusid + "' "
				+ " and sv_avon_rep_id = '"+repid+"' " ;
			int i5 = dbo.executeUpdate(sql);
			msg = "Yes";
		} catch (Exception ex1) {
			msg = "No";
			////logger.log(Level.DEBUG," from update customer Record");
			////logger.log(Level.DEBUG,ex1);
			ex1.printStackTrace();
		}
		finally {
			dbo.closeConnection() ;
		}
		return msg;
	}

	public String skinbasicRecord(String cusid, String attrid, String attrval,
			String attrvals[],String repid) {
		SVDBOperation dbo = null;
		String sql = "";
		String msg = "";
		String attrid_2 = "" ;
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			if ((attrvals == null) && (attrval != null)) {
				attrvals = new String[1];
				attrvals[0] = attrval;
			}
		//	System.out.println("Attr val ==> "+attrvals[0]) ;
			for (int j = 0; j < attrvals.length; j++) {
				if (attrid.substring(0, 1).equals("5") || attrid.substring(0, 1).equals("6") || attrid.substring(0, 1).equals("9"))
				{
					attrid_2 = attrid ;
					if (attrvals[j].substring(0, 1).equals("D"))
						attrid_2 = "52" ;
					else if(attrvals[j].substring(0, 2).equals("CE") || attrvals[j].substring(0, 2).equals("RE") || attrvals[j].substring(0, 2).equals("MF")) {
						attrid_2 = "60" ;
					}
					sql = "insert into customer_basic_profile values "+
					      " ('" + cusid + "','01', '" + attrid_2 + "','" + attrvals[j] + "', sysdate) ";
					System.out.println("SQL +++++++> "+sql) ;
				}
				int i1 = dbo.executeUpdate(sql);
	//			dbo.commit() ;
			}
			msg = "Yes";
		} catch (Exception ex1) {
			msg = "No";
			////logger.log(Level.DEBUG," from inset customerSkin Record");
			////logger.log(Level.DEBUG,ex1);
			ex1.printStackTrace();

		}
		finally {
			dbo.closeConnection() ;
		}
		return msg;
	}

	public String hairBaiscRecord(String cusid, String attrid, String attrval,
			String attrvals[],String repid) {
		SVDBOperation dbo = null;
		String sql = "";
		String msg = "";
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			if ((attrvals == null) && (attrval != null)) {
				attrvals = new String[1];
				attrvals[0] = attrval;
			}
			for (int j = 0; j < attrvals.length; j++) {
				if (attrid.substring(0, 1).equals("7"))
				{
					sql = "insert into customer_hair_profile values "+
					      " ('" + cusid + "','02', '" + attrid + "','" + attrvals[j] + "', sysdate) ";
				}
				int i1 = dbo.executeUpdate(sql);
	//			dbo.commit() ;
			}
			msg = "Yes";
		} catch (Exception ex1) {
			msg = "No";
			////logger.log(Level.DEBUG," from inset customerSkin Record");
			////logger.log(Level.DEBUG,ex1);
			ex1.printStackTrace();

		}
		finally {
			dbo.closeConnection() ;
		}
		return msg;
	}
	
	public String singleProdSkinhairBasicRecords(String sql) {
		SVDBOperation dbo = null;
		String msg = "";
//		String attrid_2 = "" ;
		try {
			System.out.println("USER PROFILE DATA :: " + sql);
			dbo = new SVDBOperation();
			dbo.createConnection();
//			if ((attrvals == null) && (attrval != null)) {
//				attrvals = new String[1];
//				attrvals[0] = attrval;
//			}
		//	System.out.println("Attr val ==> "+attrvals[0]) ;
//			for (int j = 0; j < attrvals.length; j++) {
//				if (attrid.substring(0, 1).equals("5") || attrid.substring(0, 1).equals("6") || attrid.substring(0, 1).equals("9"))
//				{
//					attrid_2 = attrid ;
//					if (attrvals[j].substring(0, 1).equals("D"))
//						attrid_2 = "52" ;
//					else if(attrvals[j].substring(0, 2).equals("CE") || attrvals[j].substring(0, 2).equals("RE") || attrvals[j].substring(0, 2).equals("MF")) {
//						attrid_2 = "60" ;
//					}
//					sql = "insert into customer_basic_profile values "+
//					      " ('" + cusid + "','01', '" + attrid_2 + "','" + attrvals[j] + "', sysdate) ";
//					System.out.println("SQL +++++++> "+sql) ;
//				}
//				int i1 = dbo.executeUpdate(sql);
//	//			dbo.commit() ;
//			}
			int i1 = dbo.executeUpdate(sql);
			msg = "Yes";
		} catch (Exception ex1) {
			msg = "No";
			////logger.log(Level.DEBUG," from inset customerSkin Record");
			////logger.log(Level.DEBUG,ex1);
			ex1.printStackTrace();

		}
		finally {
			dbo.closeConnection() ;
		}
		return msg;
	}

	public String deleteSkinProfileRecord(String cusid, String attrid,String repid) {
		SVDBOperation dbo = null;
		String sql = "";
		String msg = "";
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			if (attrid.substring(0, 1).equals("5") || attrid.substring(0, 1).equals("6") )
				sql =  " delete from customer_basic_profile "
				       + " where sv_customer_id = '"+cusid+"'  "
					   + " and (sv_customer_profile_code like '5%' or sv_customer_profile_code like '6%') " ;
			int i0 = dbo.executeUpdate(sql);
//			dbo.commit() ;
			msg = "Yes";
		} catch (Exception ex1) {
			msg = "No";
			////logger.log(Level.DEBUG," from deleteSkinRecord");
			////logger.log(Level.DEBUG,ex1);
			ex1.printStackTrace();
		}
		finally {
			dbo.closeConnection() ;
		}
		return msg;
	}

	public String deleteHairProfileRecord(String cusid, String attrid,String repid) {
		SVDBOperation dbo = null;
		String sql = "";
		String sql2 = "";
		String msg = "";
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			
			sql =  " delete from customer_hair_profile "
			       + " where sv_customer_id = '"+cusid+"'  "
				   + " and sv_customer_profile_code like '7%'  " ;
			System.out.println("vb delete "+sql) ;
			int i0 = dbo.executeUpdate(sql);
			
			sql2 =  " delete from customer_basic_profile "
			       + " where sv_customer_id = '"+cusid+"'  "
				   + " and sv_customer_profile_code in ('55','56','57','58','59') " ;
			
		    int i1 = dbo.executeUpdate(sql2);
		    
//			dbo.commit() ;
			msg = "Yes";
		} catch (Exception ex1) {
			msg = "No";
			////logger.log(Level.DEBUG," from deleteSkinRecord");
			////logger.log(Level.DEBUG,ex1);
			ex1.printStackTrace();
		}
		finally {
			dbo.closeConnection() ;
		}
		return msg;
	}
	
	public ArrayList getCustomerSkinRecord(String cusid,String repid) {
		SVDBOperation dbo = null;
		String sql = "";
		ArrayList vt = new ArrayList();
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			String[][] rs = new String[0][0];
			sql = " select sv_customer_profile_code, sv_customer_profile_attribute from customer_basic_profile "
					+ " where sv_customer_id = '"+cusid+"' " +
					 " and sv_customer_module_id = '01' " ;
			rs = dbo.executeQuery(sql);
			if (rs != null) {
				for (int i = 0; i < rs.length; i++) {
					vt.add(rs[i][0] + rs[i][1]);
				}
			}
		} catch (Exception ex1) {
			////logger.log(Level.DEBUG,ex1);
			ex1.printStackTrace();
		}
		finally {
			dbo.closeConnection() ;
		}
		return vt;
	}

	public ArrayList getCustomerHairRecord(String cusid,String repid) {
		SVDBOperation dbo = null;
		String sql = "";
		ArrayList vt = new ArrayList();
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			String[][] rs = new String[0][0];
			System.out.println("11111111 aaaa") ;
			sql = " select sv_customer_profile_code, sv_customer_profile_attribute "+
			      " from customer_hair_profile " +
				  " where sv_customer_id = '"+cusid+"' " +
				  " and sv_customer_module_id = '02' " ;
			System.out.println("11111111 bbbb "+sql) ;
			rs = dbo.executeQuery(sql);
			if (rs != null) {
				for (int i = 0; i < rs.length; i++) {
					vt.add(rs[i][0] + rs[i][1]);
				}
			}

			String[][] rs2 = new String[0][0];
			String sql1 = " select sv_customer_profile_code, sv_customer_profile_attribute from customer_basic_profile "
					+ " where sv_customer_id = '"+cusid+"' " +
					 " and sv_customer_module_id = '01' " ;
			System.out.println("11111111 cccc "+sql1) ;
			rs2 = dbo.executeQuery(sql1);
			if (rs2 != null) {
				for (int i = 0; i < rs2.length; i++) {
					vt.add(rs2[i][0] + rs2[i][1]);
				}
			}
			System.out.println("11111111 vvvv "+vt) ;

		} catch (Exception ex1) {
			////logger.log(Level.DEBUG,ex1);
			ex1.printStackTrace();
		}
		finally {
			dbo.closeConnection() ;
		}
		return vt;
	}


	public CustProfileVO getCustomerRecord(String scrname, String cusid,
			String option, String repid) {
		SVDBOperation dbo = null;
		String noption = "";
		String sql = "";
		String contactid = "";

		String[][] crec = new String[0][0];
		//logger.log(Level.DEBUG,"option inside ave is " + option);
		if (option.equals("regular")) {
			if (scrname.equals("cdatabase"))
				option = "upper(a.sv_customer_last_name)"; // sv_customer_id
			else if (scrname.equals("contacthistory"))
				option = "sv_contact_date desc";
			else if (scrname.equals("productfav"))
				option = "a.sv_purchase_date desc";
			else if (scrname.equals("replenish"))
				option = "a.sv_replm_date";
		} else if (option.equals("first")) {
			if (scrname.equals("cdatabase"))
				option = "upper(a.sv_customer_first_name)";
			else if (scrname.equals("contacthistory"))
				option = "sv_contact_type_id ";
			else if (scrname.equals("productfav"))
				option = "c.sv_product_line_id";
		} else if (option.equals("last")) {
			//logger.log(Level.DEBUG,"i canme clstr");
			if (scrname.equals("cdatabase"))
				option = "upper(a.sv_customer_last_name)";
			else if (scrname.equals("contacthistory"))
				option = "sv_contact_purpose_id ";
			else if (scrname.equals("productfav"))
				option = "c.sv_product_type_id";
		} else if (option.equals("status")) {
			option = "sv_customer_status ";
		} else if (option.substring(0, 6).equals("search")) {
			//logger.log(Level.DEBUG,"i canme search");
			if (option.substring(6, 8).equals("01")) {
				noption = "and upper(ltrim(rtrim(a.sv_customer_first_name))) like  upper('%"
						+ option.substring(8, option.length()).trim() + "%') ";
			}
			if (option.substring(6, 8).equals("02")) {
				noption = "and upper(ltrim(rtrim(a.sv_customer_last_name))) like  upper('%"
						+ option.substring(8, option.length()).trim() + "%') ";
			}
			if (option.substring(6, 8).equals("03")) {
				noption = "and ( upper(ltrim(rtrim(a.sv_customer_last_name))) like  upper('%"
						+ option.substring(8, option.length()).trim()
						+ "%') )  "
						+ " or (upper(ltrim(rtrim(a.sv_customer_first_name))) like  upper('%"
						+ option.substring(8, option.length()).trim()
						+ "%') ) ";
			}
			option = "upper(a.sv_customer_last_name)";
		}
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
				sql = " select NVL(initcap(a.sv_customer_first_name),' '), NVL(a.sv_customer_middle_name,' '), "
						+ // 0,1
						" NVL(initcap(a.sv_customer_last_name),' '), "
						+ //2
						" NVL(a.sv_customer_email,' '), NVL(a.sv_customer_home_phone,' '), "
						+ //3,4
						" NVL(a.sv_customer_work_phone, ' ') , NVL(a.sv_customer_fax,' '), "
						+ // 5, 6
						" NVL(a.sv_customer_address1,' '), NVL(a.sv_customer_address2, ' '), "
						+ //7, 8
						" NVL(a.sv_customer_city, ' '), NVL(a.sv_customer_state,' '), "
						+ //9, 10
						" NVL(a.sv_customer_zip, ' '), "
						+ // 11
						" NVL(a.sv_customer_gender, 'F'),NVL(a.sv_customer_status, ' '), "
						+ //12,13
						" NVL(a.sv_customer_mobile_phone, ' '), NVL(a.sv_customer_ethnicity, ' '), "
						+ //14,15
						" NVL(to_char(a.sv_customer_birthdate, 'mm/dd'), ' '), "
						+ " NVL(to_char(a.sv_customer_anndate, 'mm/dd'), ' '), "
						+ //16, 17
						" NVL(a.sv_customer_comments, ' ') "
						+ //18
						" from customer_master a"
						+ " where a.sv_customer_id = '" + cusid + "' "
						+ " and a.sv_avon_rep_id = '"+repid+"' " ;
			//logger.log(Level.DEBUG,sql);

			crec = dbo.executeQuery(sql);
			pdl = new CustProfileVO();
			pdl.setFirstname(crec[0][0]);
			pdl.setMiddlename(crec[0][1]);
			pdl.setLastname(crec[0][2]);
			pdl.setEmail(crec[0][3]);
			pdl.setHomephone(crec[0][4]);
			pdl.setWorkphone(crec[0][5]);
			pdl.setFaxphone(crec[0][6]);
			pdl.setAddress1(crec[0][7]);
			pdl.setAddress2(crec[0][8]);
			pdl.setCity(crec[0][9]);
			pdl.setState(crec[0][10]);
			pdl.setZip(crec[0][11]);
			pdl.setGender(crec[0][12]);
			pdl.setStatus(crec[0][13]);
			pdl.setMobilephone(crec[0][14]);
			pdl.setEthnicity(crec[0][15]);
			pdl.setBday(crec[0][16]);
			pdl.setAnvday(crec[0][17]);
			pdl.setComments(crec[0][18]);

		} catch (Exception ex1) {
			//logger.log(Level.DEBUG," from  customer Record");
			////logger.log(Level.DEBUG,ex1);
			ex1.printStackTrace();
		}
		finally {
			dbo.closeConnection() ;
		}
		return pdl;
	}

	public String updateCustRecord(String cusid, ProfileSummaryFormBean profilesForm,String repid) {
		SVDBOperation dbo = null;
		String sql = "";
		String msg = "";
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			String fname = "";
			String mname = "";
			String lname = "";
			String hphone = "";
			String hphone1 = "";
			String hphone2 = "";
			String hphone3 = "";
			String wphone = "";
			String wphone1 = "";
			String wphone2 = "";
			String wphone3 = "";
			String fphone = "";
			String fphone1 = "";
			String fphone2 = "";
			String fphone3 = "";
			String mphone = "";
			String mphone1 = "";
			String mphone2 = "";
			String mphone3 = "";
			String email = "";
			String addr1 = "";
			String addr2 = "";
			String city = "";
			String state = "";
			String zip = "";
			String gender = "";
			String status = "";
			String bdate = "";
			String bday_mm = "";
			String bday_dd = "";
			String bday_yy = "";
			String adate = "";
			String anday_mm = "";
			String anday_dd = "";
			String anday_yy = "";
			String comments = "";
			String ethnicity = "";
			String selfprofile = "" ;
			String entryflag  = "" ;

			int year = new java.util.Date().getYear()+1900;

			if (profilesForm.getSelfprofileflag() != null)
				selfprofile = profilesForm.getSelfprofileflag().trim();
			if (selfprofile != null && selfprofile.equals("yes"))
				entryflag = "S" ;
			else
				entryflag = "R" ;

			if (profilesForm.getFname() != null) {
				fname = profilesForm.getFname().trim();
				fname = svUtil.insertSingleQuote(fname);
				char[] chars = fname.toCharArray();
				chars[0] = Character.toUpperCase(chars[0]);
				fname = new String(chars);

			}

			if (profilesForm.getMname() != null)
				mname = profilesForm.getMname().trim();

			if (profilesForm.getLname() != null) {
				lname = profilesForm.getLname().trim();
				lname = svUtil.insertSingleQuote(lname);
				char[] chars = lname.toCharArray();
				chars[0] = Character.toUpperCase(chars[0]);
				lname = new String(chars);
			}

			if (profilesForm.getHphone1() != null)
				hphone1 = profilesForm.getHphone1().trim();
			if (profilesForm.getHphone2() != null)
				hphone2 = profilesForm.getHphone2().trim();
			if (profilesForm.getHphone3() != null)
				hphone3 = profilesForm.getHphone3().trim();
			hphone = hphone1 + hphone2 +hphone3;

			if (profilesForm.getWphone1() != null)
				wphone1 = profilesForm.getWphone1().trim();
			if (profilesForm.getWphone2() != null)
				wphone2 = profilesForm.getWphone2().trim();
			if (profilesForm.getWphone3() != null)
				wphone3 = profilesForm.getWphone3().trim();
			wphone = wphone1 + wphone2 +wphone3;

			if (profilesForm.getFphone1() != null)
				fphone1 = profilesForm.getFphone1().trim();
			if (profilesForm.getFphone2() != null)
				fphone2 = profilesForm.getFphone2().trim();
			if (profilesForm.getFphone3() != null)
				fphone3 = profilesForm.getFphone3().trim();
			fphone = fphone1 + fphone2 +fphone3;

			if (profilesForm.getMphone1() != null)
				mphone1 = profilesForm.getMphone1().trim();
			if (profilesForm.getMphone2() != null)
				mphone2 = profilesForm.getMphone2().trim();
			if (profilesForm.getMphone3() != null)
				mphone3 = profilesForm.getMphone3().trim();
			mphone = mphone1 + mphone2 +mphone3;

			if (profilesForm.getEmail() != null)
				email = profilesForm.getEmail().trim();
			if (profilesForm.getAddr1() != null)
				addr1 = profilesForm.getAddr1().trim();
			if (profilesForm.getAddr2() != null)
				addr2 = profilesForm.getAddr2().trim();
			if (profilesForm.getCity() != null)
				city = profilesForm.getCity().trim();
			if (profilesForm.getState() != null)
				state = profilesForm.getState().trim();
			if (profilesForm.getZip() != null)
				zip = profilesForm.getZip().trim();
			if (profilesForm.getGender() != null)
				gender = profilesForm.getGender().trim();
			if (profilesForm.getStatus() != null)
				status = profilesForm.getStatus().trim();

			if (profilesForm.getBday_mm() != null) {
				bday_mm = profilesForm.getBday_mm().trim();
				if(bday_mm.length() == 1)
					bday_mm = "0"+bday_mm;
			}

			if (profilesForm.getBday_dd() != null) {
				bday_dd = profilesForm.getBday_dd().trim();
				if(bday_dd.length() == 1)
					bday_dd = "0"+bday_dd;
			}

			/*if (profilesForm.getBday_yy() != null)
				bday_yy = profilesForm.getBday_yy().trim();*/
			bday_yy = ""+year;
			bdate = bday_mm + bday_dd + bday_yy;
			if(bdate.length()<8)
				bdate = "";

			if (profilesForm.getAnday_mm() != null) {
				anday_mm = profilesForm.getAnday_mm().trim();
				if(anday_mm.length() == 1)
					anday_mm = "0"+anday_mm;
			}

			if (profilesForm.getAnday_dd() != null) {
				anday_dd = profilesForm.getAnday_dd().trim();
				if(anday_dd.length() == 1)
					anday_dd = "0"+anday_dd;
			}

			/*if (profilesForm.getAnday_yy() != null)
				anday_yy = profilesForm.getAnday_yy().trim();*/
			anday_yy = ""+year;
			adate = anday_mm + anday_dd + anday_yy;
			if(adate.length()<8)
				adate = "";

			if (profilesForm.getEthnicity() != null)
				ethnicity = profilesForm.getEthnicity().trim();
			if (profilesForm.getComments() != null) {
				comments = profilesForm.getComments().trim();
				comments = comments.replaceAll("'","''");
			}


			sql = " update customer_master  "
					+ " set sv_customer_first_name = '" + fname + "' , "
					+ " sv_customer_last_name = '" + lname + "' ,"
					+ " sv_customer_middle_name = '" + mname + "' ,"
					+ " sv_customer_gender = '" + gender + "' ,"
					+ " sv_customer_ethnicity = '" + ethnicity + "' ,"
					+ " sv_customer_home_phone = '" + hphone + "' ,"
					+ " sv_customer_work_phone = '" + wphone + "' ,"
					+ " sv_customer_mobile_phone = '" + mphone + "' ,"
					+ " sv_customer_email  = '" + email + "' ,"
					+ " sv_customer_fax  = '" + fphone + "' ,"
					+ " sv_customer_address1 = '" + addr1 + "' ,"
					+ " sv_customer_address2 = '" + addr2 + "' ,"
					+ " sv_customer_city = '" + city + "' ,"
					+ " sv_customer_state = '" + state + "' ,"
					+ " sv_customer_zip = '" + zip + "' ,"
					+ " sv_customer_birthdate = to_date('" + bdate
					+ "','mm/dd/yyyy') ," + " sv_customer_anndate = to_date('"
					+ adate + "','mm/dd/yyyy') ,"
					+ " sv_customer_comments = '" + comments + " ' ,"
					+ " sv_customer_status = '" + status + "' "
					+ " where sv_customer_id = '" + cusid + "' "
					+ " and sv_avon_rep_id = '"+repid+"' " ;
			int i5 = dbo.executeUpdate(sql);
			if(i5!=0)
				msg = "Yes";
			else
				msg = "No";
		} catch (Exception ex1) {
			msg = "No";
			//logger.log(Level.DEBUG," from update customer Record");
			////logger.log(Level.DEBUG,ex1);
			ex1.printStackTrace();
		}
		finally {
			dbo.closeConnection() ;
		}
		return msg;
	}

	public String insCusRecord(ProfileSummaryFormBean profilesForm, String cusid, String repid) {
		SVDBOperation dbo = null;
		String sql = "";
		String msg = "";
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			String fname = "";
			String mname = "";
			String lname = "";
			String hphone = "";
			String hphone1 = "";
			String hphone2 = "";
			String hphone3 = "";
			String wphone = "";
			String wphone1 = "";
			String wphone2 = "";
			String wphone3 = "";
			String mphone = "";
			String mphone1 = "";
			String mphone2 = "";
			String mphone3 = "";
			String fphone = "";
			String fphone1 = "";
			String fphone2 = "";
			String fphone3 = "";
			String email = "";
			String addr1 = "";
			String addr2 = "";
			String city = "";
			String state = "";
			String zip = "";
			String gender = "";
			String status = "1";
			String bdate = "";
			String bday_mm = "";
			String bday_dd = "";
			String bday_yy = "";
			String adate = "";
			String anday_mm = "";
			String anday_dd = "";
			String anday_yy = "";
			String comments = "";
			String ethnicity = "";
			String selfprofile = "" ;
			String entryflag  = "R" ;
			String inviteType = "N" ;

			int year = new java.util.Date().getYear()+1900;

			//jayaram
			String prodrecflag = "N";


			if (profilesForm.getSelfprofileflag() != null)
				selfprofile = profilesForm.getSelfprofileflag().trim();
			if (selfprofile != null && selfprofile.equals("yes"))
				entryflag = "S" ;
			else
				entryflag = "R" ;

/*			if (profilesForm.getFname() != null) {
				fname = profilesForm.getFname().trim();
				fname = svUtil.insertSingleQuote(fname);
				char[] chars = fname.toCharArray();
				chars[0] = Character.toUpperCase(chars[0]);
				fname = new String(chars);
			}

			if (profilesForm.getMname() != null)
				mname = profilesForm.getMname().trim();

			if (profilesForm.getLname() != null) {
				lname = profilesForm.getLname().trim();
				lname = svUtil.insertSingleQuote(lname);
				char[] chars = lname.toCharArray();
				chars[0] = Character.toUpperCase(chars[0]);
				lname = new String(chars);
			}

			if (profilesForm.getHphone1() != null)
				hphone1 = profilesForm.getHphone1().trim();
			if (profilesForm.getHphone2() != null)
				hphone2 = profilesForm.getHphone2().trim();
			if (profilesForm.getHphone3() != null)
				hphone3 = profilesForm.getHphone3().trim();
			hphone = hphone1 + hphone2 +hphone3;

			if (profilesForm.getWphone1() != null)
				wphone1 = profilesForm.getWphone1().trim();
			if (profilesForm.getWphone2() != null)
				wphone2 = profilesForm.getWphone2().trim();
			if (profilesForm.getWphone3() != null)
				wphone3 = profilesForm.getWphone3().trim();
			wphone = wphone1 + wphone2 +wphone3;

			if (profilesForm.getFphone1() != null)
				fphone1 = profilesForm.getFphone1().trim();
			if (profilesForm.getFphone2() != null)
				fphone2 = profilesForm.getFphone2().trim();
			if (profilesForm.getFphone3() != null)
				fphone3 = profilesForm.getFphone3().trim();
			fphone = fphone1 + fphone2 +fphone3;

			if (profilesForm.getMphone1() != null)
				mphone1 = profilesForm.getMphone1().trim();
			if (profilesForm.getMphone2() != null)
				mphone2 = profilesForm.getMphone2().trim();
			if (profilesForm.getMphone3() != null)
				mphone3 = profilesForm.getMphone3().trim();
			mphone = mphone1 + mphone2 +mphone3;

			if (profilesForm.getEmail() != null) {
				email = profilesForm.getEmail().trim();
				email = email.replaceAll("'","''");
			}
			if (profilesForm.getAddr1() != null) {
				addr1 = profilesForm.getAddr1().trim();
				addr1 = addr1.replaceAll("'","''");
			}
			if (profilesForm.getAddr2() != null) {
				addr2 = profilesForm.getAddr2().trim();
				addr2 = addr2.replaceAll("'","''");
			}
			if (profilesForm.getCity() != null)
				city = profilesForm.getCity().trim();
			if (profilesForm.getState() != null)
				state = profilesForm.getState().trim();
			if (profilesForm.getZip() != null)
				zip = profilesForm.getZip().trim();
			if (profilesForm.getGender() != null)
				gender = profilesForm.getGender().trim();
			if (profilesForm.getStatus() != null)
				status = profilesForm.getStatus().trim();
			if (profilesForm.getRepid() != null)
				repid = profilesForm.getRepid().trim();

			if (profilesForm.getBday_mm() != null) {
				bday_mm = profilesForm.getBday_mm().trim();
				if(bday_mm.length() == 1)
					bday_mm = "0"+bday_mm;
			}

			if (profilesForm.getBday_dd() != null) {
				bday_dd = profilesForm.getBday_dd().trim();
				if(bday_dd.length() == 1)
					bday_dd = "0"+bday_dd;
			}

			/*if (profilesForm.getBday_yy() != null)
				bday_yy = profilesForm.getBday_yy().trim();*/
			bday_yy = ""+year;
			bdate = bday_mm + bday_dd + bday_yy;
			if(bdate.length()<8)
				bdate = "";

			if (profilesForm.getAnday_mm() != null) {
				anday_mm = profilesForm.getAnday_mm().trim();
				if(anday_mm.length() == 1)
					anday_mm = "0"+anday_mm;
			}

			if (profilesForm.getAnday_dd() != null) {
				anday_dd = profilesForm.getAnday_dd().trim();
				if(anday_dd.length() == 1)
					anday_dd = "0"+anday_dd;
			}
			/*if (profilesForm.getAnday_yy() != null)
				anday_yy = profilesForm.getAnday_yy().trim();*/
			anday_yy = ""+year;
			adate = anday_mm + anday_dd + anday_yy;
			if(adate.length()<8)
				adate = "";

			if (profilesForm.getComments() != null) {
				comments = profilesForm.getComments().trim();
				comments = comments.replaceAll("'","''");
			}

			if (profilesForm.getEthnicity() != null)
				ethnicity = profilesForm.getEthnicity().trim();

			//Jayaram
			if (profilesForm.getProdrecflag() != null && (profilesForm.getProdrecflag().trim().equalsIgnoreCase("G")) )
				prodrecflag = profilesForm.getProdrecflag().trim();

			if(profilesForm.getInviteType()!=null && (profilesForm.getInviteType().trim().equalsIgnoreCase("I")) )
				inviteType = "I";

			sql = " insert into customer_master  values "
			        + " ( '"+cusid+"', '" + fname + "' ,  '" + lname + "'  ,   "
					+ " '"+mname + "', '" + gender + "' , '" + email + "' ,   "
					+ " '"+hphone + "','" + wphone + "' , '" + fphone + "' ,   "
					+ " '"+addr1  + "','" + addr2 + "' ,  '" + city+ "',       "
					+ " '"+state  + "','" + zip   + "',   '" + status+"',       "
					+ " '"+ethnicity+"', to_date('"+ bdate +"','mm/dd/yyyy'),  "
					+ " to_date('"+adate+ "','mm/dd/yyyy') ,'" + comments + "',"
					+ " sysdate,'"+entryflag+"', 'N', '"+prodrecflag+"', '"+inviteType+"', sysdate, "
					+ " '"+repid+"',   '" + mphone + "', 'Y' )";

//			//logger.log(Level.DEBUG,sql) ;
			int i5 = dbo.executeUpdate(sql);
			if(i5!=0)
				msg = "Yes";
			else
				msg = "No";
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

	public String getCustomerId(String repid) {
		SVDBOperation dbo = null;
		String sql = "";
		String cusid = "";
		String[][] crec = new String[0][0];
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			sql = " select  avon_customer_seq.nextval from dual "  ;
			crec = dbo.executeQuery(sql);
		    cusid = crec[0][0] ;
		} catch (Exception ex1) {
			////logger.log(Level.DEBUG,ex1);
			ex1.printStackTrace();
		}
		finally {
			dbo.closeConnection() ;
		}
		return cusid;
	}


	public String getSystemDate() throws Exception{
		dbo = new SVDBOperation();
		dbo.createConnection();
		String sql = "select to_char(sysdate,'mm/dd/yyyy') as dt from dual";
		//String sql = "select sysdate as dt from dual";
		ResultSet rs = dbo.executeQueryRS(sql);
		String date = "";
		while(rs.next())
			date = rs.getString(1);
		dbo.closeConnection();
		return date;
	}

    public Hashtable getProfileDescriptions(String mode)
    {
		String[][]  result        = null;
		String sql = "" ;
		Hashtable profdesc = new Hashtable() ;
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			sql = " select sv_profile_attribute_id, sv_profile_attribute_desc from sv_profile_attribute_details " ;
			result = dbo.executeQuery(sql) ;

			System.out.println("result length  "+result.length) ;
		  for (int j=0; j < result.length ; j++)
		  {
		  	String attribid = "" ;
		  	String attribdesc = "" ;
		  	String brandlogo = "" ;
		  	attribid = result[j][0].trim() ;
		  	attribdesc = result[j][1].trim() ;
//		  	brandlogo = result[j][2].trim() ;
//			System.out.println("Brand "+attribid+" "+branddesc+"  "+brandlogo) ;
		  	profdesc.put(attribid,attribdesc) ;
		  }
		} catch (Exception e)
		{
			System.out.println(e) ;
		}
		finally {
			dbo.closeConnection() ;
		}
		System.out.println("Attrib size... tttttt "+profdesc.size()) ;
		return profdesc ;
    }


}