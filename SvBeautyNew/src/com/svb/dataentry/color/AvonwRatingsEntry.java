package com.svb.dataentry.color;

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

import java.util.Hashtable;
import java.util.StringTokenizer;

import com.svb.common.dba.SVBeautydba;
import com.svb.common.dba.SVDBOperation;

public class AvonwRatingsEntry {
	SVBeautydba dba = null;

	SVDBOperation dbo = null;

	String result[][] = null;

	String sql = null;

	String ratingtable = "avon_entry_ratings";

	public void initialize() throws Exception {
		dba = new SVBeautydba();
		dbo = new SVDBOperation();
		dbo.createConnection();

	}

	public void destroy() throws Exception {
		dbo.closeConnection();

	}
	public String[][] getBase() {

		try {

			sql = "select *  from avon_product_base order by upper(SV_PRODUCT_base_name)  ";
			//logger.log(Level.DEBUG,sql);
			result = dbo.executeQuery(sql);

		} catch (Exception e) {
			//logger.log(Level.DEBUG,e);
		}

		return result;
	}

	public String[][] getIngrediants() {

		try {

			sql = "select  *  from ingredients order by upper(SV_INGREDIENT_NAME)  ";
			//logger.log(Level.DEBUG,sql);
			result = dbo.executeQuery(sql);

		} catch (Exception e) {
			//logger.log(Level.DEBUG,e);
		}

		return result;
	}

	public String[][] getNewIngrediants() {

		try {

			sql = "select  *  from ingredients_new order by upper(SV_INGREDIENT_NAME)  ";
			//logger.log(Level.DEBUG,sql);
			result = dbo.executeQuery(sql);

		} catch (Exception e) {
			//logger.log(Level.DEBUG,e);
		}

		return result;
	}

	public String[][] getProducts() {

		try {

			sql = " select  a.SV_PRODUCT_ID,a.SV_PRODUCT_NAME,nvl(a.SV_PRODUCT_IMAGE_FILE,'noimage'),b.SV_PRODUCT_MASTER_TYPE  from product a "
					+ " ,avon_product_type b  where a. SV_PRODUCT_TYPE_ID = b. SV_PRODUCT_TYPE_ID "
					+ "   order by upper(a.SV_PRODUCT_NAME)  ";
			//logger.log(Level.DEBUG,sql);
			result = dbo.executeQuery(sql);

		} catch (Exception e) {
			//logger.log(Level.DEBUG,e);
		}

		return result;
	}

	public String[][] getSkinType() {

		try {

			sql = " select * from  skin_type  ";
			//logger.log(Level.DEBUG,sql);
			result = dbo.executeQuery(sql);

		} catch (Exception e) {
			;
		}

		return result;
	}

	public String[][] getSkinCondition() {

		try {

			sql = " select * from skin_condition  ";
			//logger.log(Level.DEBUG,sql);
			result = dbo.executeQuery(sql);

		} catch (Exception e) {
			//logger.log(Level.DEBUG,e);
			;
		}

		return result;
	}

	public String[][] getSkinConcern() {

		try {

			sql = " select * from  skin_concern ";
			//logger.log(Level.DEBUG,sql);
			result = dbo.executeQuery(sql);

		} catch (Exception e) {
			//logger.log(Level.DEBUG,e);
		}

		return result;
	}

	public String[][] getHandConcern() {

		try {

			sql = " select * from  hand_concerns ";
			//logger.log(Level.DEBUG,sql);
			result = dbo.executeQuery(sql);

		} catch (Exception e) {
			//logger.log(Level.DEBUG,e);
		}

		return result;
	}
	public String[][] getEnvironment() {

		try {

			sql = " select * from  environment ";
			//logger.log(Level.DEBUG,sql);
			result = dbo.executeQuery(sql);

		} catch (Exception e) {
			//logger.log(Level.DEBUG,e);
		}

		return result;
	}

	public String[][] getAge() {

		try {

			sql = " select * from  age ";
			//logger.log(Level.DEBUG,sql);
			result = dbo.executeQuery(sql);

		} catch (Exception e) {
			//logger.log(Level.DEBUG,e);
		}

		return result;
	}

	public String[][] getAttribCount(String elementid, String attribid) {

		try {

			sql = " select  sv_product_element_value, count(*) from "
					+ ratingtable + " where " + " sv_product_element_id = '"
					+ elementid + "' " + " and sv_body_attribute_id = '"
					+ attribid + "' " + " group by sv_product_element_value ";

			//logger.log(Level.DEBUG,sql);
			result = dbo.executeQuery(sql);

		} catch (Exception e) {
			//logger.log(Level.DEBUG,e);
		}

		return result;
	}

	public String[][] getElementValuePresent(String elementid, String attribid,
			String typeid) {

		try {

			/*
			 * sql = " select distinct(sv_product_element_value || '-' ||
			 * ltrim(rtrim(sv_product_element_conc)))"+ " from "+ratingtable+"
			 * where "+ " sv_product_element_id = '"+elementid+"' and " + "
			 * sv_body_attribute_id = '"+attribid+"' and "+ "
			 * SV_PRODUCT_TYPE_MASTER_ID = '"+typeid+"' " ;
			 */
			sql = " select  distinct(sv_product_element_value || '-' || ltrim(rtrim(sv_product_element_conc)))"
					+ " from "
					+ ratingtable
					+ " where "
					+ " sv_product_element_id = '"
					+ elementid
					+ "' and "
					+ " sv_body_attribute_id = '"
					+ attribid
					+ "' and "
					+ " (SV_PRODUCT_TYPE_MASTER_ID = '"
					+ typeid
					+ "' or SV_PRODUCT_TYPE_MASTER_ID = '99') ";

			//logger.log(Level.DEBUG,sql);

			result = dbo.executeQuery(sql);

		} catch (Exception e) {
			//logger.log(Level.DEBUG,e);
		}

		return result;
	}

	public boolean deleteData(String elementid, String elementvalue,
			String attribid, String attribvalue, String typeid,
			String elementconc) {
		boolean flag = true;
		String deletesql = " delete " + ratingtable
				+ " where sv_product_element_id = '" + elementid + "' "
				+ " and sv_product_element_value ='" + elementvalue + "'"
				+ " and sv_body_attribute_id = '" + attribid + "' and "
				+ " sv_body_attribute_value = '" + attribvalue + "' and "
				+ " sv_product_type_master_id = '" + typeid + "' and "
				+ " sv_product_element_conc = '" + elementconc + "'";
		try {
			//logger.log(Level.DEBUG,deletesql);
			// dbo.executeUpdate(deletesql);
			dbo.executeUpdate(deletesql);

		} catch (Exception e) {
			//logger.log(Level.DEBUG,e);
			flag = false;
		}
		return flag;
	}

	public boolean deleteRecord(String token) {
		boolean flag = true;
		//logger.log(Level.DEBUG,"inside delete record");
		//logger.log(Level.DEBUG,token);
		StringTokenizer stk = new StringTokenizer(token, "-");
		String elementid = "";
		String elementvalue = "";
		String elementconc = "";
		String typeid = "";
		String attribid = "";
		String attribvalue = "";
		String rating = "";

		while (stk.hasMoreTokens()) {
			elementid = stk.nextToken();
			elementvalue = stk.nextToken();
			elementconc = stk.nextToken();
			typeid = stk.nextToken();
			attribid = stk.nextToken();
			attribvalue = stk.nextToken();
			rating = stk.nextToken();

		}
		//logger.log(Level.DEBUG,"at end");

		if (!deleteData(elementid, elementvalue, attribid, attribvalue, typeid,
				elementconc))
			flag = false;

		return flag;

	}

	public boolean saveData(String token) {
		boolean flag = true;

		StringTokenizer stk = new StringTokenizer(token, "-");
		String elementid = "";
		String elementvalue = "";
		String elementconc = "";
		String typeid = "";
		String attribid = "";
		String attribvalue = "";
		String rating = "";

		while (stk.hasMoreTokens()) {
			elementid = stk.nextToken();
			elementvalue = stk.nextToken();
			elementconc = stk.nextToken();
			typeid = stk.nextToken();
			attribid = stk.nextToken();
			attribvalue = stk.nextToken();
			rating = stk.nextToken();

		}

		if (deleteData(elementid, elementvalue, attribid, attribvalue, typeid,
				elementconc)) {

			sql = "insert into " + ratingtable + " values('" + elementid
					+ "','" + elementvalue + "','" + elementconc + "'," + " '"
					+ typeid + "', '" + attribid + "','" + attribvalue + "','"
					+ rating + "')";

			//logger.log(Level.DEBUG,sql);
			try {
				int i = dbo.executeUpdate(sql);
				if (i == 0) //there is some problem while data is inserted
					flag = false;
			} catch (Exception e) {
				//logger.log(Level.DEBUG,e);
				flag = false;
			}
		}

		return flag;

	}

	public String[][] getRatings(String elementid, String attribid) {
		sql = " select ltrim(rtrim(sv_product_element_id)) || '-' || ltrim(rtrim(sv_product_element_value))"
				+ " || '-' || ltrim(rtrim(sv_product_element_conc)) ||  '-'  || ltrim(rtrim(sv_product_type_master_id)) || '-' || ltrim(rtrim(sv_body_attribute_id)) || "
				+ " '-' || sv_body_attribute_value , sv_rating_value "
				+ " from "
				+ ratingtable
				+ " where sv_product_element_id = '"
				+ elementid + "' and sv_body_attribute_id = '" + attribid + "'";
		try {
			//logger.log(Level.DEBUG,sql);
			result = dbo.executeQuery(sql);
		} catch (Exception e) {
			//logger.log(Level.DEBUG,e);
		}
		return result;
	}

	public boolean modifyData(String prodid, String prodline, String prodtype,
			String prodimg, String prodprice, String prodsize, String prodbase,
			String comments, String desc, String[] prodmode) {
		boolean flag = true;
		sql = " update temp_product set sv_product_line_id = '" + prodline
				+ "'" + " , sv_product_type_id = '" + prodtype
				+ "' , sv_product_image_file = '" + prodimg + "' , "
				+ " sv_product_price = '" + prodprice
				+ "' , sv_product_sizes = '" + prodsize + "' , "
				+ " sv_product_base_id = '" + prodbase
				+ "' , sv_product_description = '" + desc + "' where "
				+ " sv_product_id = '" + prodid + "'";

		//logger.log(Level.DEBUG,sql);
		try {
			int i = dbo.executeUpdate(sql);
			if (i == 0) //there is some problem while data is inserted
				flag = false;
		} catch (Exception e) {
			//logger.log(Level.DEBUG,e);
			flag = false;
		}
//		if (flag)
	//		flag = saveMode(prodid, prodmode);

		return flag;

	}

	public boolean saveMode(String prodid, String[] prodmode) {
		boolean flag = true;
		try {

			sql = " delete avon_product_mode where sv_product_id = '" + prodid
					+ "'";
			dbo.executeUpdate(sql);
			for (int i = 0; i < prodmode.length; i++) {
				//logger.log(Level.DEBUG,"stringtokenizer before");
				StringTokenizer stk = new StringTokenizer(prodmode[i], "-");
				String prodmode1 = "";
				String modevalue = "";
				while (stk.hasMoreTokens()) {
					//logger.log(Level.DEBUG,"before first token");
					prodmode1 = stk.nextToken();
					//logger.log(Level.DEBUG,"before second token");
					modevalue = stk.nextToken();
				}
				if (!modevalue.equals("0")) //if there is any value for mode
				{
					sql = " insert into avon_product_mode values('" + prodid
							+ "','" + prodmode1 + "','" + modevalue + "') ";
					dbo.executeUpdate(sql);
				}

			}//for

		}//try
		catch (Exception e) {
			//logger.log(Level.DEBUG,e);
			flag = false;
		}

		return flag;
	}//savemode

	public String[][] getData(String prodid) {
		sql = "select * from temp_product where sv_product_id = '"
				+ prodid.trim() + "'";
		result = dbo.executeQuery(sql);
		return result;
	}

	public Hashtable getProductModes(String prodid) {
		Hashtable ht = new Hashtable();
		sql = "select  SV_PRODUCT_MODE_ID,SV_PRODUCT_MODE_RATE    from "
				+ " avon_product_mode where sv_product_id = '" + prodid.trim()
				+ "'";

		//logger.log(Level.DEBUG,sql);
		result = dbo.executeQuery(sql);
		for (int i = 0; i < result.length; i++)
			ht.put(result[i][0], result[i][1]);

		return ht;

	}

	public static void main(String args[]) {
		try {
			AvonwDataEntry de = new AvonwDataEntry();
			SVBeautydba dba = new SVBeautydba();
			SVDBOperation dbo = new SVDBOperation();
			dbo.createConnection();
			de.initialize(dba, dbo);
			de.getNextId();

		} catch (Exception e) {
			//logger.log(Level.DEBUG,e);
		}

	}

}