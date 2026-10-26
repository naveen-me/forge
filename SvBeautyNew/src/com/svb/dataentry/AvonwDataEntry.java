package com.svb.dataentry;

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

public class AvonwDataEntry {
	SVBeautydba dba = null;

	SVDBOperation dbo = null;

	String result[][] = null;

	String sql = null;

	public void initialize(SVBeautydba dba, SVDBOperation dbo) {
		this.dba = dba;
		this.dbo = dbo;

	}

	public void initialize() throws Exception {
		dba = new SVBeautydba();
		dbo = new SVDBOperation();
		dbo.createConnection();

	}
	public void destroy() throws Exception {
		dbo.closeConnection();

	}

	public String[][] getImages() {

		try {

			sql = " select sv_product_id,nvl(sv_product_image_file,'noprodimgqwertyokdf'),sv_product_name "
					+ " from product order by SV_PRODUCT_image_file ";
			result = dbo.executeQuery(sql);

		} catch (Exception e) {
			////logger.log(Level.DEBUG,e);
			e.printStackTrace();
		}

		return result;

	}

	public String getNextId() {
		String prodid = "0";
		try {

			sql = " select avon_product_seq.nextval from dual ";
			result = dbo.executeQuery(sql);
			for (int i = 0; i < result.length; i++)
				prodid = "SK" + result[i][0];

		} catch (Exception e) {
			////logger.log(Level.DEBUG,e);
			e.printStackTrace();
		}

		return prodid;
	}

	public String[][] getProdIds(String status) {

		try {

			sql = " select a.sv_product_id, c.sv_brand_name||' '||a.sv_product_name from temp_product a, " + 
                  " temp_product_elements b, brand_view c "+
                  " where a.sv_product_id = b.sv_product_id "+
                  " and b.sv_product_brand_id = c.sv_brand_id " +
				  " and a.sv_product_status = 'Y' "+
				  " order by c.sv_brand_name||' '||a.sv_product_name ";
			result = dbo.executeQuery(sql);

		} catch (Exception e) {
			////logger.log(Level.DEBUG,e);
			e.printStackTrace();
		}

		return result;
	}

	public String[][] getProductLine() {

		try {

			sql = "select * from  avon_product_line order by SV_PRODUCT_LINE_NAME ";
			result = dbo.executeQuery(sql);

		} catch (Exception e) {
			e.printStackTrace();;
		}

		return result;
	}

	public String[][] getProductType() {

		try {

			sql = "select * from  avon_product_type  ";
			result = dbo.executeQuery(sql);

		} catch (Exception e) {
			////logger.log(Level.DEBUG,e);
			//;
			e.printStackTrace();
		}

		return result;
	}

	public String[][] getProductCateg() {

		try {

			sql = "select * from  avon_product_category  ";
			result = dbo.executeQuery(sql);

		} catch (Exception e) {
			////logger.log(Level.DEBUG,e);
			//;
			e.printStackTrace();
		}

		return result;
	}
	public String[][] getProductUsage() {

		try {

			sql = "select * from  avon_product_usage  ";
			result = dbo.executeQuery(sql);

		} catch (Exception e) {
			////logger.log(Level.DEBUG,e);
			//;
			e.printStackTrace();
		}

		return result;
	}

	public String[][] getMasterProductType() {

		try {

			sql = "select * from avon_product_master_type  ";
			result = dbo.executeQuery(sql);

		} catch (Exception e) {
			////logger.log(Level.DEBUG,e);
			//;
			e.printStackTrace();
		}

		return result;
	}

	public String[][] getProductsByCategory(String masterProdType) {

		try {

			sql = "select sv_product_id, sv_product_name, NVL(sv_product_resource,' ') from "+
				  "temp_product a,avon_product_type b where "+
				  "b.SV_PRODUCT_TYPE_ID = a.SV_PRODUCT_TYPE_ID "+
				  "and b.SV_PRODUCT_MASTER_TYPE = '"+masterProdType+"' "+
				  "order by lower(sv_product_name)";

			result = dbo.executeQuery(sql);

		} catch (Exception e) {
			////logger.log(Level.DEBUG,e);
			//;
			e.printStackTrace();
		}

		return result;
	}

	public String[][] getProductsByResource(String prodResource) {

		try {

			sql = "select sv_product_id, sv_product_name from "+
			      "temp_product a,avon_product_type b where "+
				  "b.SV_PRODUCT_TYPE_ID = a.SV_PRODUCT_TYPE_ID "+
				  "and b.SV_PRODUCT_RESOURCE = '"+prodResource+"' "+
				  "order by lower(sv_product_name)";

			result = dbo.executeQuery(sql);

		} catch (Exception e) {
			////logger.log(Level.DEBUG,e);
			//;
			e.printStackTrace();
		}

		return result;
	}

	public String[][] getUVSpec() {

		try {

			sql = "select * from  avon_uv_specification order by SV_uv_spec_id ";
			result = dbo.executeQuery(sql);

		} catch (Exception e) {
			e.printStackTrace();;
		}

		return result;
	}

	public String[][] getIngredients() {

		try {

			sql = "select * from  ingredients order by upper(sv_ingredient_name) ";
			result = dbo.executeQuery(sql);

		} catch (Exception e) {
			//logger.log(Level.DEBUG,e);
			e.printStackTrace();
		}

		return result;
	}

	public boolean saveIngredients(String prodid, String ingredients) {
		//logger.log(Level.DEBUG,"dataentry inside saveingredients");
		boolean flag = true;
		StringTokenizer stk = new StringTokenizer(ingredients, "-");

		try {

			sql = "delete avon_product_ingredients where sv_product_id = '"
					+ prodid + "'";
			//logger.log(Level.DEBUG,sql);
			dbo.executeUpdate(sql);

		} catch (Exception e) {
			flag = false;
			return flag;
		}

		while (stk.hasMoreTokens()) {
			String s1 = stk.nextToken();
			sql = " insert into avon_product_ingredients values('" + prodid
					+ "' , '" + s1 + "' , '') ";
			//logger.log(Level.DEBUG,sql);
			try {
				int j = dbo.executeUpdate(sql);
				if (j == 0) //there is some problem while data is inserted
					flag = false;
			} catch (Exception e) {
				//logger.log(Level.DEBUG,e);
				flag = false;
			}
			if (!flag)
				break;

		}

		return flag;

	}

	public String[][] getNotProdIngredients(String id) {

		try {

			sql = " select sv_ingredient_id,sv_ingredient_name from ingredients where "
					+ " sv_ingredient_id in "
					+ " ( select distinct(sv_ingredient_id)  from  ingredients "
					+ " minus select distinct(sv_ingredient_id) from  "
					+ " avon_product_ingredients where sv_product_id = 'SK10000002' ) ";

			//logger.log(Level.DEBUG,sql);
			result = dbo.executeQuery(sql);

		} catch (Exception e) {
			//logger.log(Level.DEBUG,e);
			e.printStackTrace();
		}

		return result;
	}

	public String[][] getProdIngredients(String id) {

		try {

			sql = " select a.sv_ingredient_id , initcap(b.sv_ingredient_name)  from  avon_product_ingredients a , ingredients b "
					+ " where a.SV_INGREDIENT_id is not null and  "
					+ " a.sv_ingredient_id = b.sv_ingredient_id and "
					+ " a.sv_product_id = '"
					+ id.trim()
					+ "' order by upper(b.sv_ingredient_name) ";
			//logger.log(Level.DEBUG,sql);
			result = dbo.executeQuery(sql);

		} catch (Exception e) {
			//logger.log(Level.DEBUG,e);
		}

		return result;
	}

	public String[][] getProductBase() {

		try {

			sql = "select * from  avon_product_base order by SV_PRODUCT_Base_NAME ";
			result = dbo.executeQuery(sql);

		} catch (Exception e) {
			//logger.log(Level.DEBUG,e);
		}

		return result;
	}

	public String[][] getProductMode() {

		try {

			sql = "select * from  product_mode ";
			result = dbo.executeQuery(sql);

		} catch (Exception e) {
			//logger.log(Level.DEBUG,e);
		}

		return result;

	}
	public String[][] getProductDayOrder() {

		try {

			sql = "select * from  avon_product_day_order ";
			result = dbo.executeQuery(sql);

		} catch (Exception e) {
			//logger.log(Level.DEBUG,e);
		}

		return result;

	}
	public String[][] getProductNightOrder() {

		try {

			sql = "select * from  avon_product_night_order ";
			result = dbo.executeQuery(sql);

		} catch (Exception e) {
			//logger.log(Level.DEBUG,e);
		}

		return result;

	}
	public String[][] getProductGender() {

		try {

			sql = "select * from  avon_product_gender ";
			result = dbo.executeQuery(sql);

		} catch (Exception e) {
			//logger.log(Level.DEBUG,e);
		}

		return result;

	}
	public String[][] getProductAge() {

		try {

			sql = "select * from  avon_product_age ";
			result = dbo.executeQuery(sql);

		} catch (Exception e) {
			//logger.log(Level.DEBUG,e);
		}

		return result;

	}
	public String[][] getProductRegimen() {

		try {

			sql = "select * from  avon_product_regimen ";
			result = dbo.executeQuery(sql);

		} catch (Exception e) {
			//logger.log(Level.DEBUG,e);
		}

		return result;

	}

	public boolean saveData(String prodid, String prodline, String prodtype,
			String prodimg, String prodprice, String prodsize, String prodbase,
			String comments, String desc, String[] prodmode, String prodname) {
		boolean flag = true;
		sql = "insert into temp_product fields (SV_PRODUCT_ID,SV_PRODUCT_LINE_ID,SV_PRODUCT_TYPE_ID,SV_PRODUCT_IMAGE_FILE,SV_PRODUCT_PRICE, "
				+ " SV_PRODUCT_SIZES,SV_PRODUCT_BASE_ID,SV_PRODUCT_DESCRIPTION,SV_product_name ) "
				+ " values ('"
				+ prodid
				+ "','"
				+ prodline
				+ "','"
				+ prodtype
				+ "','"
				+ prodimg
				+ "','"
				+ prodprice
				+ "','"
				+ prodsize
				+ "','"
				+ prodbase
				+ "','"
				+ ""
				+ desc
				+ "','"
				+ prodname
				+ "' )";

		//logger.log(Level.DEBUG,sql);
		try {
			int i = dbo.executeUpdate(sql);
			if (i == 0) //there is some problem while data is inserted
				flag = false;
		} catch (Exception e) {
			//logger.log(Level.DEBUG,e);
			flag = false;
		}
	//	if (flag)
	//		flag = saveMode(prodid, prodmode);

		return flag;

	}

	public boolean saveData1(String prodid, String prodcateg, String prodbase,
			String prodspf, String proddayorder, String prodnightorder, String prodgender,
			String prodage, String prodregimen,String produsage) {
		boolean flag = true;
		
		sql = "delete from temp_product_elements where sv_product_id = '"+prodid+"' " ;
		sql = "insert into temp_product_elements fields (SV_PRODUCT_ID,SV_PRODUCT_category_ID,SV_PRODUCT_base_ID,SV_PRODUCT_spf_id,SV_PRODUCT_order_day_id, "
				+ " SV_PRODUCT_order_night_id,SV_PRODUCT_gender_ID,SV_PRODUCT_age_id,SV_product_regimen_id, sv_product_usage_id ) "
				+ " values ('"
				+ prodid
				+ "','"
				+ prodcateg
				+ "','"
				+ prodbase
				+ "','"
				+ prodspf
				+ "','"
				+ proddayorder
				+ "','"
				+ prodnightorder
				+ "','"
				+ prodgender
				+ "','"
				+ ""
				+ prodage
				+ "','"
				+ prodregimen
				+ "','"
				+ produsage
				+ "' )";

		//logger.log(Level.DEBUG,sql);
		try {
			int i = dbo.executeUpdate(sql);
			if (i == 0) //there is some problem while data is inserted
				flag = false;
		} catch (Exception e) {
			//logger.log(Level.DEBUG,e);
			flag = false;
		}
	//	if (flag)
	//		flag = saveMode(prodid, prodmode);

		return flag;

	}
	public boolean deleteData1(String prodid) {
		boolean flag = true;
		
		sql = "delete from temp_product_elements where sv_product_id = '"+prodid+"' " ;
		try {
			int i = dbo.executeUpdate(sql);
			if (i == 0) //there is some problem while data is inserted
				flag = false;
		} catch (Exception e) {
			//logger.log(Level.DEBUG,e);
			flag = false;
		}
	//	if (flag)
	//		flag = saveMode(prodid, prodmode);
		return flag;

	}
	public boolean modifyData(String prodid, String prodline, String prodtype,
			String prodimg, String prodprice, String prodsize, String prodbase,
			String comments, String desc, String[] prodmode, String prodname) {
		if (desc != null && !desc.equals("null") && desc.indexOf('\'') != -1) //single
																			  // quote
																			  // in
																			  // description
		{
			desc = replaceSingleQuote(desc);

		}
		boolean flag = true;
		sql = " update temp_product set sv_product_line_id = '" + prodline + "'"
				+ " , sv_product_type_id = '" + prodtype+ "' ,  "
				+ " sv_product_price = '" + prodprice+ "' ,  "
				+ " sv_product_base_id = '" + prodbase + "'  where "
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
	//	if (flag)
		//	flag = saveMode(prodid, prodmode);

		return flag;

	}

	public boolean modifyStatus(String prodid) {
		boolean flag = true;
		sql = " update temp_product set sv_product_status = 'R' " +
			  " where  sv_product_id = '" + prodid + "'" ;

		//logger.log(Level.DEBUG,sql);
		try {
			int i = dbo.executeUpdate(sql);
			if (i == 0) //there is some problem while data is inserted
				flag = false;
		} catch (Exception e) {
			//logger.log(Level.DEBUG,e);
			flag = false;
		}
	//	if (flag)
		//	flag = saveMode(prodid, prodmode);

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

	public boolean saveUV(String prodid, String uvspec) {
		boolean flag = true;
		try {

			sql = " delete avon_product_uv where sv_product_id = '" + prodid
					+ "'";
			dbo.executeUpdate(sql);
			sql = " insert into avon_product_uv values('" + prodid + "','"
					+ uvspec + "') ";
			dbo.executeUpdate(sql);

		}//try
		catch (Exception e) {
			//logger.log(Level.DEBUG,e);
			flag = false;
		}

		return flag;
	}//savemode

	public String[][] getData(String prodid) {
		sql = "select * from temp_product where sv_product_id = '" + prodid.trim()+"'" ;
		result = dbo.executeQuery(sql);
		return result;
	}
	
	public String[][] getData1(String prodid) {
		sql = "select * from temp_product_elements where sv_product_id = '" + prodid.trim()
				+ "'";
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

	public String getProductUVs(String prodid) {

		String resultu[][] = new String[0][0];
		String uvspec = "";
		sql = "select  SV_uv_spec_ID   from "
				+ " avon_product_uv where sv_product_id = '" + prodid.trim()
				+ "'";

		//logger.log(Level.DEBUG,sql);
		resultu = dbo.executeQuery(sql);
		//logger.log(Level.DEBUG,"vuspec.length " + resultu.length);
		if (resultu.length > 0)
			uvspec = resultu[0][0];
		return uvspec;

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

	String replaceSingleQuote(String str) {
		char charArray[] = new char[str.length()];
		str.getChars(0, str.length(), charArray, 0);
		for (int i = 0; i < charArray.length; i++) {
			if (charArray[i] == '\'')
				charArray[i] = '`';
		}
		return new String(charArray);
	}

}