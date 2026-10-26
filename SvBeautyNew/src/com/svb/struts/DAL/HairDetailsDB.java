package com.svb.struts.DAL;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import com.svb.common.dba.SVDBConnection;
import com.svb.common.dba.SVDBOperation;
import com.svb.struts.bean.ProductDetailsBean;

public class HairDetailsDB {
	
	SVDBConnection svConnection;
	ProductDetailsBean details;
	ArrayList list;
	HashMap map;
	
	
	public HashMap getProductNames(){

		svConnection = new SVDBConnection();
		map = new HashMap();
		Connection connection=null;
		Statement stmt=null;
		ResultSet rs=null;
		
		String query = "SELECT SV_PRODUCT_ID, SV_PRODUCT_NAME "+
				"FROM TEMP_PRODUCT_HAIR";
		
		try{
			connection = svConnection.getConnection();
			stmt = connection.createStatement();
			rs = stmt.executeQuery(query);
			
			while(rs.next()){
				String key = rs.getString(1);
				String value = rs.getString(2);
		//		System.out.println(key+"-------------"+value);
				map.put(key, value);
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				rs.close();
				stmt.close();
				svConnection.free(connection);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return map;
	}
	
	public ProductDetailsBean getProductDetails(String productId){
	
		svConnection = new SVDBConnection();
		details = new ProductDetailsBean();
		list = new ArrayList();
		Connection connection=null;
		Statement stmt=null;
		ResultSet rs=null;
		
		String query =  "SELECT SV_PRODUCT_NAME, SV_PRODUCT_DESCRIPTION, SV_PRODUCT_DIRECTIONS, SV_PRODUCT_IMAGE_FILE "+
						"FROM TEMP_PRODUCT_HAIR WHERE SV_PRODUCT_ID="+productId;
		
		String query1 = "select a.sv_ingredient_name from sv_ingredients_master a, hair_product_ingredients b "+
                        "where b.sv_ingredient_id = a.sv_ingredient_id "+
                        "and b.sv_ingredient_id is not null "+
                        "and b.sv_product_id = "+productId+" " ;
                      //  "and b.sv_ingredient_id not in (select sv_ingredient_id from hair_product_ingredients_na) " ;
		
		String query2 = " select a.productid, a.productdescription from sv_hc_product_desc a"+
                        " where ltrim(rtrim(a.productid)) ="+productId+" ";
		System.out.println("query2 "+query2) ;
		
		try{
			connection = svConnection.getConnection();
			stmt = connection.createStatement();
			rs = stmt.executeQuery(query);
			while(rs.next()){
				details.setProductId(productId);
				details.setProductName(rs.getString("SV_PRODUCT_NAME"));
				//details.setBrandName("");
				String directions = (String) rs.getString("SV_PRODUCT_DIRECTIONS") ;
				directions = (String)preProcess(directions) ;
				details.setProductDirections(directions);
				details.setMediumImage(rs.getString("SV_PRODUCT_IMAGE_FILE"));
			}
			
			int ing = 0 ;
			rs = stmt.executeQuery(query1);
			while(rs.next()){
				ing++;
				list.add(rs.getString("sv_ingredient_name"));
			}
			details.setIngredients(list);
			details.setIngredientsCount(""+ing);
			
			rs = stmt.executeQuery(query2);
			while(rs.next()){
				String longdis=rs.getString("productdescription");
	            if(!(longdis!=(null)))
	                longdis=" "; 
	            longdis = (String)preProcess(longdis) ;
				details.setProductDescription(longdis);			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				rs.close();
				stmt.close();
				svConnection.free(connection);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		return details;
	}
	
	public HashMap getIngredientNamesWithGlossary(){
		
		svConnection = new SVDBConnection();
		map = new HashMap();
		Connection connection=null;
		Statement stmt=null;
		ResultSet rs=null;
		
		String query =  " SELECT S1.SV_INGREDIENT_GLOSSORY_TEXT, "+
						" S2.SV_INGREDIENT_NAME FROM HAIR_INGREDIENTS_GLOSSORY S1, sv_ingredients_master S2 "+
						" WHERE S1.SV_INGREDIENT_ID = S2.SV_INGREDIENT_ID" +
						" and s1.sv_ingredient_id not in (select  s3.sv_ingredient_id from hair_product_ingredients_na s3)" ;
		
		try{
			connection = svConnection.getConnection();
			stmt = connection.createStatement();
			rs = stmt.executeQuery(query);
			while(rs.next()){
				map.put(rs.getString("SV_INGREDIENT_NAME").toUpperCase(), rs.getString("SV_INGREDIENT_GLOSSORY_TEXT"));
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				rs.close();
				stmt.close();
				svConnection.free(connection);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return map;
	}
	public String preProcess(String newString){
		if (newString != null && newString.length() > 0)
		{
		 newString = newString.replaceAll("&amp;", "&"); 
    	 newString = newString.replaceAll("&","&amp;");
    	 newString = newString.replaceAll("&amp;nbsp;", "&nbsp;");
//    	 newString = newString.replaceAll("&amp; 153;", "");
    	 newString = newString.replaceAll("&amp; 153;", "&#8482;");
    	 newString = newString.replaceAll("&amp;#153;", "™");
    	 newString = newString.replaceAll("&amp;reg;", "®");
    	 newString = newString.replaceAll("<LI>", " ");
    	 newString = newString.replaceAll("</LI>", " ");
    	 newString = newString.replaceAll("<UL>", " ");
    	 newString = newString.replaceAll("</UL>", " ");
    	 newString = newString.replaceAll("<OL>", " ");
    	 newString = newString.replaceAll("</OL>", " ");
    	 newString = newString.replaceAll("<BR>", "<BR/>");
    	 newString = newString.replaceAll("&nbsp;"," ");
    	 newString = newString.replaceAll("&deg;"," ");
    	 newString = newString.replaceAll("&amp;deg;"," ");
    	 //newString = newString.replaceAll("?"," ");
		}
		//System.out.println("new string "+newString) ;
    	return newString;
    }
	public String[][] getCustomerReviews(String prodid){
		
		SVDBOperation dbo = new SVDBOperation();
		String[][] result = new String[0][0];
		
		String query =  " select a.sv_rating, a.sv_rating_comment,to_char(sv_rating_date,'Month dd, yyyy'), b.sv_avg_customer_rating "+ 
                        " from customer_reviews_desc a, customer_reviews b "+
                        " where a.sv_product_id = b.sv_product_id "+
                        " and b.sv_product_id = '"+prodid+"'" ;
		System.out.println(query);

		try{
			dbo = new SVDBOperation();
			dbo.createConnection();
			result = dbo.executeQuery(query); 
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				dbo.closeConnection() ;
				}catch(Exception e){
				e.printStackTrace();
			}
		}
		result = (String[][])setStarrating(result) ;
		return result;
	}
    public String[][] setStarrating(String[][] prodlist)
    {
    	String starlogo = "" ;
		for (int i=0; i < prodlist.length; i++)
		{
			starlogo = "" ;
			String prating = (String) prodlist[i][0];
			String arating = (String) prodlist[i][3];
			if (arating != null)
			{
				float art = Float.parseFloat(arating);
				if (art == 0.0)
					starlogo = "";
				else if (art == 1.0)
					starlogo = "rating_10_bty.gif" ;
				else if (art > 1.0 && art <= 1.5  )
					starlogo = "rating_15_bty.gif" ;
				else if (art > 1.5 && art <= 2.0  )
					starlogo = "rating_20_bty.gif" ;
				else if (art > 2.0 && art <= 2.5  )
					starlogo = "rating_25_bty.gif" ;
				else if (art > 2.5 && art <= 3.0  )
					starlogo = "rating_30_bty.gif" ;
				else if (art > 3.0 && art <= 3.5  )
					starlogo = "rating_35_bty.gif" ;
				else if (art > 3.5 && art <= 4.0  )
					starlogo = "rating_40_bty.gif" ;
				else if (art > 4.0 && art <= 4.5  )
					starlogo = "rating_45_bty.gif" ;
				else if (art > 4.5 && art <= 5.0  )
					starlogo = "rating_50_bty.gif" ;
				prodlist[i][3] = starlogo ;
			}
			System.out.println("arating "+arating+" "+starlogo) ;
			starlogo = "" ;
			if (prating != null)
			{
				float art = Float.parseFloat(prating);
				if (art == 0.0)
					starlogo = "";
				else if (art == 1.0)
					starlogo = "rating_10_bty.gif" ;
				else if (art > 1.0 && art <= 1.5  )
					starlogo = "rating_15_bty.gif" ;
				else if (art > 1.5 && art <= 2.0  )
					starlogo = "rating_20_bty.gif" ;
				else if (art > 2.0 && art <= 2.5  )
					starlogo = "rating_25_bty.gif" ;
				else if (art > 2.5 && art <= 3.0  )
					starlogo = "rating_30_bty.gif" ;
				else if (art > 3.0 && art <= 3.5  )
					starlogo = "rating_35_bty.gif" ;
				else if (art > 3.5 && art <= 4.0  )
					starlogo = "rating_40_bty.gif" ;
				else if (art > 4.0 && art <= 4.5  )
					starlogo = "rating_45_bty.gif" ;
				else if (art > 4.5 && art <= 5.0  )
					starlogo = "rating_50_bty.gif" ;
				prodlist[i][0] = starlogo ;
			}
			System.out.println("prating "+prating+" "+starlogo) ;
		}
    	return prodlist ;
    }
	
}


