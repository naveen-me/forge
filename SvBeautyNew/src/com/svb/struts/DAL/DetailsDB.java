package com.svb.struts.DAL;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import com.svb.common.dba.SVDBConnection;
import com.svb.common.dba.SVDBOperation;
import com.svb.common.valueobjects.ProductVO;
import com.svb.struts.bean.ProductDetailsBean;

public class DetailsDB {

	SVDBConnection svConnection;
	ProductDetailsBean details;
	ArrayList list;
	/**
	* Added by Synoptek(29-11-2019) 
	* Modified queries to get required column data
	*/
//---------------------------------------- start ---------------------------------------------------
	ArrayList<String> displayOrderList;
	ArrayList<String> ingredientIdList;
//---------------------------------------- end ---------------------------------------------------
	HashMap map;

	public HashMap getProductNames() {

		svConnection = new SVDBConnection();
		map = new HashMap();
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;

		String query = "SELECT SV_PRODUCT_ID, SV_PRODUCT_NAME " + "FROM temp_product";

		try {
			connection = svConnection.getConnection();
			stmt = connection.createStatement();
			rs = stmt.executeQuery(query);

			while (rs.next()) {
				String key = rs.getString(1);
				String value = rs.getString(2);
				// System.out.println(key+"-------------"+value);
				map.put(key, value);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				stmt.close();
				svConnection.free(connection);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return map;
	}

	public ProductDetailsBean getProductDetails(String productId) {

		svConnection = new SVDBConnection();
		details = new ProductDetailsBean();
		list = new ArrayList();
		ingredientIdList = new ArrayList<String>();
		displayOrderList = new ArrayList<String>();
		Hashtable<String, String> ingredientsName = new Hashtable<>();
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;

		String query = "SELECT SV_PRODUCT_NAME, SV_PRODUCT_DESCRIPTION, SV_PRODUCT_DIRECTIONS, SV_PRODUCT_IMAGE_FILE "
				+ "FROM temp_product WHERE SV_PRODUCT_ID=" + productId;
//************************************** Modified by Synoptek(29-11-2019) ********************************************* 
//---------------------------------------- start ---------------------------------------------------
		String query1 = "select a.sv_ingredient_id,a.sv_ingredient_name,NVL(b.display_order,'0') as display_order from sv_ingredients_master a, avon_product_ingredients b "
				+ "where b.sv_ingredient_id = a.sv_ingredient_id " + "and b.sv_ingredient_id is not null "
				+ "and b.sv_product_id = " + productId + " ";
//---------------------------------------- end ---------------------------------------------------
		// " and b.sv_ingredient_id in (select distinct sv_product_element_value from
		// sv_ratings) " ;

		String query2 = "select a.sv_product_description from sv_product_description a, temp_product b "
				+ "where  a.sv_product_id = b.sv_product_id " + "and    b.sv_product_id = " + productId + " ";

		String query3 = "select b.sv_product_category_name from temp_product_elements A, AVON_PRODUCT_CATEGORY B "
				+ "where a.sv_product_category_id = b.sv_product_category_id and a.sv_product_id = "+productId +" ";
		
		String query4 = "select b.sv_product_type_name from temp_product A, SV_PRODUCT_SUB_CATEGORY B "
				+ "where a.sv_product_type_id = b.sv_product_type_id and a.sv_product_id  = "+productId +" ";

		try {
			connection = svConnection.getConnection();
			stmt = connection.createStatement();
//			System.out.println("SQL ----------> "+ query);
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				details.setProductId(productId);
				details.setProductName(rs.getString("SV_PRODUCT_NAME"));
				// details.setBrandName("");
				String directions = (String) rs.getString("SV_PRODUCT_DIRECTIONS");
				directions = (String) preProcess(directions);
				details.setProductDirections(directions);
				details.setMediumImage(rs.getString("SV_PRODUCT_IMAGE_FILE"));
			}

			int ing = 0;
//			System.out.println("SQL ----------> "+ query1);
			rs = stmt.executeQuery(query1);
			while (rs.next()) {
				ing++;
				String ingredientId = rs.getString("sv_ingredient_id");
				String ingredientName = rs.getString("sv_ingredient_name");
//************************************** Added by Synoptek(29-11-2019) ********************************************* 
//---------------------------------------- start ---------------------------------------------------
				ingredientIdList.add(ingredientId);
				if (!rs.getString("display_order").equals("0")) {
					displayOrderList.add(rs.getString("display_order"));
				} else {
					displayOrderList.add("");
				}
//---------------------------------------- end ---------------------------------------------------
				list.add(ingredientName);
				
				ingredientsName.put(ingredientId,ingredientName);
				
			}
			details.setIngredients(list);
//************************************** Added by Synoptek(29-11-2019) ********************************************* 
//---------------------------------------- start ---------------------------------------------------
			details.setDisplayOrder(displayOrderList);
			details.setIngredientIdList(ingredientIdList);
//---------------------------------------- end ---------------------------------------------------			
		
			details.setIngredientsCount("" + ing);
			details.setIngredientsName(ingredientsName);

			rs = stmt.executeQuery(query2);
			while (rs.next()) {
				String longdis = rs.getString("SV_PRODUCT_DESCRIPTION");
				if (!(longdis != (null)))
					longdis = " ";
				longdis = (String) preProcess(longdis);
				details.setProductDescription(longdis);
			}
			rs = stmt.executeQuery(query3);
			while (rs.next()) {
				details.setProductMasterType(rs.getString("sv_product_category_name"));
			}
			
			rs = stmt.executeQuery(query4);
			if(rs.next() != false)
				details.setProductType(rs.getString("sv_product_type_name"));
			else
				details.setProductType("");
			 
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				stmt.close();
				svConnection.free(connection);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return details;
	}

	public HashMap getIngredientNamesWithGlossary() {

		svConnection = new SVDBConnection();
		map = new HashMap();
		List tempList = new ArrayList<>();
		Connection connection=null;
		Statement stmt=null;
		ResultSet rs=null;
		
		String query =  " SELECT S1.SV_INGREDIENT_GLOSSORY_TEXT, "+
						" S2.SV_INGREDIENT_NAME FROM INGREDIENTS_GLOSSORY S1, sv_ingredients_master S2 "+
						" WHERE S1.SV_INGREDIENT_ID = S2.SV_INGREDIENT_ID"; 
//						+"and s1.sv_ingredient_id in (select distinct s3.sv_product_element_value from sv_ratings s3)" ; // Commented By Manthan - Ingredient Glossary not showing on ingredients tab issue
		
		try{
			connection = svConnection.getConnection();
			stmt = connection.createStatement();
			System.out.println("SQL ****" + query);
			rs = stmt.executeQuery(query);
//			int count = 1;
			while (rs.next()) {
//				System.out.print("count = "+count);
//				System.out.print(", "+rs.getString("SV_INGREDIENT_NAME").toUpperCase());
//				tempList.add(rs.getString("SV_INGREDIENT_NAME").toUpperCase());
				map.put(rs.getString("SV_INGREDIENT_NAME").toUpperCase(), rs.getString("SV_INGREDIENT_GLOSSORY_TEXT"));
//				System.out.print(", map = "+map.size());
//				System.out.println(",Dif = " + (count-map.size()));
//				count++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				stmt.close();
				svConnection.free(connection);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
//		System.out.println(map.keySet().toString());
//		System.out.println(map.size());
//		System.out.println(tempList.toString());
//		System.out.println(tempList.size());
		return map;
	}
	
	public HashMap getIngredientNamesWithGlossaryNew(String prodid) {

		svConnection = new SVDBConnection();
		map = new HashMap();
		List tempList = new ArrayList<>();
		Connection connection=null;
		Statement stmt=null;
		ResultSet rs=null;
		
		String query =  new String();		
		if(!prodid.equals("")) {
			query += "SELECT SV_INGREDIENT_GLOSSORY_TEXT,SV_INGREDIENT_NAME FROM INGREDIENTS_GLOSSORY WHERE SV_INGREDIENT_ID IN "
			        +"(select sv_ingredient_id from avon_product_ingredients where sv_product_id = '"+ prodid + "')"; 
		}
		else {
		    query =  " SELECT S1.SV_INGREDIENT_GLOSSORY_TEXT, "+
					" S2.SV_INGREDIENT_NAME FROM INGREDIENTS_GLOSSORY S1, sv_ingredients_master S2 "+
					" WHERE S1.SV_INGREDIENT_ID = S2.SV_INGREDIENT_ID"; 
		}
		
		try{
			connection = svConnection.getConnection();
			stmt = connection.createStatement();
			System.out.println("SQL ****" + query);
			rs = stmt.executeQuery(query);
//			int count = 1;
			while (rs.next()) {
//				System.out.print("count = "+count);
//				System.out.print(", "+rs.getString("SV_INGREDIENT_NAME").toUpperCase());
//				tempList.add(rs.getString("SV_INGREDIENT_NAME").toUpperCase());
				map.put(rs.getString("SV_INGREDIENT_NAME").toUpperCase(), rs.getString("SV_INGREDIENT_GLOSSORY_TEXT"));
//				System.out.print(", map = "+map.size());
//				System.out.println(",Dif = " + (count-map.size()));
//				count++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				stmt.close();
				svConnection.free(connection);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
//		System.out.println(map.keySet().toString());
//		System.out.println(map.size());
//		System.out.println(tempList.toString());
//		System.out.println(tempList.size());
		return map;
	}

	public String preProcess(String newString) {
		if (newString != null && newString.length() > 0) {
			newString = newString.replaceAll("&amp;", "&");
			newString = newString.replaceAll("&amp;", "&");
			newString = newString.replaceAll("&", "&amp;");
			newString = newString.replaceAll("&amp;nbsp;", "&nbsp;");
			newString = newString.replaceAll("&amp; 153;", "&#8482;");
			newString = newString.replaceAll("&amp;#153;", "�");
			newString = newString.replaceAll("&amp;reg;", "�");
			newString = newString.replaceAll("<LI>", " ");
			newString = newString.replaceAll("</LI>", " ");
			newString = newString.replaceAll("<UL>", " ");
			newString = newString.replaceAll("</UL>", " ");
			newString = newString.replaceAll("<OL>", " ");
			newString = newString.replaceAll("</OL>", " ");
			newString = newString.replaceAll("<BR>", "<BR/>");
			newString = newString.replaceAll("&nbsp;", " ");
			newString = newString.replaceAll("&deg;", " ");
			newString = newString.replaceAll("&amp;deg;", " ");
		}
		// System.out.println("new string "+newString) ;
		return newString;
	}

	public String[][] getCustomerReviews(String prodid) {

		SVDBOperation dbo = new SVDBOperation();
		String[][] result = new String[0][0];

		String query = " select a.sv_rating, a.sv_rating_comment,to_char(sv_rating_date,'Month dd, yyyy'), b.sv_avg_customer_rating "
				+ " from customer_reviews_desc a, customer_reviews b " + " where a.sv_product_id = b.sv_product_id "
				+ " and b.sv_product_id = '" + prodid + "'";
		System.out.println(query);

		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			result = dbo.executeQuery(query);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				dbo.closeConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		result = (String[][]) setStarrating(result);
		return result;
	}

	public String[][] setStarrating(String[][] prodlist) {
		String starlogo = "";
		for (int i = 0; i < prodlist.length; i++) {
			starlogo = "";
			String prating = (String) prodlist[i][0];
			String arating = (String) prodlist[i][3];
			if (arating != null) {
				float art = Float.parseFloat(arating);
				if (art == 0.0)
					starlogo = "";
				else if (art == 1.0)
					starlogo = "rating_10_bty.gif";
				else if (art > 1.0 && art <= 1.5)
					starlogo = "rating_15_bty.gif";
				else if (art > 1.5 && art <= 2.0)
					starlogo = "rating_20_bty.gif";
				else if (art > 2.0 && art <= 2.5)
					starlogo = "rating_25_bty.gif";
				else if (art > 2.5 && art <= 3.0)
					starlogo = "rating_30_bty.gif";
				else if (art > 3.0 && art <= 3.5)
					starlogo = "rating_35_bty.gif";
				else if (art > 3.5 && art <= 4.0)
					starlogo = "rating_40_bty.gif";
				else if (art > 4.0 && art <= 4.5)
					starlogo = "rating_45_bty.gif";
				else if (art > 4.5 && art <= 5.0)
					starlogo = "rating_50_bty.gif";
				prodlist[i][3] = starlogo;
			}
			System.out.println("arating " + arating + " " + starlogo);
			starlogo = "";
			if (prating != null) {
				float art = Float.parseFloat(prating);
				if (art == 0.0)
					starlogo = "";
				else if (art == 1.0)
					starlogo = "rating_10_bty.gif";
				else if (art > 1.0 && art <= 1.5)
					starlogo = "rating_15_bty.gif";
				else if (art > 1.5 && art <= 2.0)
					starlogo = "rating_20_bty.gif";
				else if (art > 2.0 && art <= 2.5)
					starlogo = "rating_25_bty.gif";
				else if (art > 2.5 && art <= 3.0)
					starlogo = "rating_30_bty.gif";
				else if (art > 3.0 && art <= 3.5)
					starlogo = "rating_35_bty.gif";
				else if (art > 3.5 && art <= 4.0)
					starlogo = "rating_40_bty.gif";
				else if (art > 4.0 && art <= 4.5)
					starlogo = "rating_45_bty.gif";
				else if (art > 4.5 && art <= 5.0)
					starlogo = "rating_50_bty.gif";
				prodlist[i][0] = starlogo;
			}
			System.out.println("prating " + prating + " " + starlogo);
		}
		return prodlist;
	}

	public String[] getProductCategorySubCategory(String prodid) {
		svConnection = new SVDBConnection();
		String [] category= new String[2];
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;

		String query = "select b.sv_product_category_name from temp_product_elements A, AVON_PRODUCT_CATEGORY B "
				+ "where a.sv_product_category_id = b.sv_product_category_id and a.sv_product_id = "+prodid +" ";
		
		String query1 = "select b.sv_product_type_name from temp_product A, SV_PRODUCT_SUB_CATEGORY B "
				+ "where a.sv_product_type_id = b.sv_product_type_id and a.sv_product_id  = "+prodid +" ";

		try {
			connection = svConnection.getConnection();
			stmt = connection.createStatement();
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				category[0]=rs.getString("sv_product_category_name");
			}
			
			rs = stmt.executeQuery(query1);
			if(rs.next() != false)
				category[1]=rs.getString("sv_product_type_name");
			else
				category[1]="";
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				stmt.close();
				svConnection.free(connection);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return category;
	}

}
