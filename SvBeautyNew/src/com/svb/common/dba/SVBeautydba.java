
package com.svb.common.dba;
import java.util.ArrayList;
import java.util.Vector;
/***************************************************
 This Class contains all  database queries related to
 Stylevalet - BloomingDales Project In future
we can add the other DBA statements which we needfor
style valet project .
 ***************************************************/
public class SVBeautydba {


  String sql = "" ;

	public String getSkinConcern()
	{
	   sql = " select * from skin_concern " ;
//		 //logger.log(Level.DEBUG,sql);
		 return sql ;
	}

	public String getSkinCondition()
	{
	    sql = " select * from skin_condition " ;
	// 	  //logger.log(Level.DEBUG,sql);
		  return sql ;
	}

	public String getCustomerName(String customerid)
	{
	     sql  = " select SV_CUSTOMER_FIRST_NAME from customer_master "+
			        " where sv_customer_id = '"+customerid+"'" ;
	//     //logger.log(Level.DEBUG,sql);
		   return sql ;

	}
	public String getProductMode(String prodid)
	{
	     sql = " select * from avon_product_mode where sv_product_id = '"+prodid+"'" ;

	//	   //logger.log(Level.DEBUG,sql);
			 return sql ;

	}

	public String getProductsIngredientsLine(String id, String prodselect)
	{

	  String addsql = "" ;
	  String addsql2 = "" ;

		if (prodselect != null && prodselect.equals("prodLine"))
			addsql = "and c.sv_product_brand_id = '"+id+"' " ;
		if (prodselect != null && prodselect.equals("prodType"))
			addsql = "and c.sv_product_category_id in "+id+" " ;
		 /**
         * Added by Synoptek(29-11-2019)
         * Query modified to get Ingredient name
         * */  
		// ----------------------------------------Start--------------------------------------------
		sql = " select a.sv_product_id ,  a.sv_ingredient_id , a.sv_ingredient_conc,D.Sv_Ingredient_Name "
				+ " from avon_product_ingredients a, temp_product b, temp_product_elements c ,Sv_Ingredients_Master D"
				+ " where a.sv_product_id = b.sv_product_id and b.sv_product_id = c.sv_product_id "
				+ " and b.sv_product_status <> 'N' and a.sv_ingredient_id = D.Sv_Ingredient_Id" + " " + addsql + " "
				+ " order by b.sv_product_id, a.sv_ingredient_id";

		//------------------------------------------End---------------------------------------------
		// logger.log(Level.DEBUG,sql);
		System.out.println(sql);

		return sql;

	}
	public String getProductIngredients(String id, boolean isSingleProductAPI)
	{

	  String addsql = "" ;
	  String addsql2 = "" ;
	  
	  String productAvailabilityCheck = isSingleProductAPI ? "" : " and b.sv_product_status <> 'N' ";//Added to remove product availability check condition for single product API - Shivam - 04/09/2024

			addsql = "and b.sv_product_id in "+id ;

	    sql =  " select a.sv_product_id ,  a.sv_ingredient_id , a.sv_ingredient_conc "+   
               " from avon_product_ingredients a, temp_product b"+   
               " where a.sv_product_id = b.sv_product_id "+
               productAvailabilityCheck +
			   " "+ addsql+" " +
               " order by b.sv_product_id" ;
	    
		 System.out.println("GET INGR :: " + sql);
		 return sql ;

	}

	public String getProductIngredientsDetail(String id)
	{

	  String addsql = "" ;
	  String addsql2 = "" ;
	  
			addsql = "and b.sv_product_id in "+id ;

	    sql =  " select a.sv_product_id ,  a.sv_ingredient_id , a.sv_ingredient_conc,D.Sv_Ingredient_Name "+   
               " from avon_product_ingredients a, temp_product b,Sv_Ingredients_Master D"+   
               " where a.sv_product_id = b.sv_product_id and a.sv_ingredient_id = D.Sv_Ingredient_Id "+
               //" and b.sv_product_status <> 'N' "+ //Commented to remove product availability check condition for single product API - Shivam - 04/09/2024
			   " "+ addsql+" " +
               " order by b.sv_product_id" ;
	    
		 System.out.println("getProductIngredientsDetail() + GET INGR :: " + sql);
		 return sql ;

	}
	
	public String getHairProductsIngredientsLine(String id, String prodselect)
	{

	  String addsql = "" ;
	  String addsql2 = "" ;

		if (prodselect != null && prodselect.equals("prodLine"))
			addsql = "and c.sv_product_brand_id = '"+id+"' " ;
		if (prodselect != null && prodselect.equals("prodType"))
			addsql = "and c.sv_product_category_id in "+id+" " ;

	    sql =  " select a.sv_product_id ,  a.sv_ingredient_id , a.sv_ingredient_conc "+   
               " from hair_product_ingredients a, temp_product_hair b, temp_product_hair_elements c "+   
               " where a.sv_product_id = b.sv_product_id "+
			   " and b.sv_product_id = c.sv_product_id " +
               " and b.sv_product_status <> 'N' "+
			   " "+ addsql+" " +
               " order by b.sv_product_id, a.sv_ingredient_id" ;
	    
		 //logger.log(Level.DEBUG,sql);
	     System.out.println(sql) ; 
	     
		 return sql ;

	}
	public String getHairProductIngredients(String id)
	{

	  String addsql = "" ;
	  String addsql2 = "" ;

			addsql = "and b.sv_product_id in"+id ;

	    sql =  " select a.sv_product_id ,  a.sv_ingredient_id , a.sv_ingredient_conc "+   
               " from hair_product_ingredients a, temp_product_hair b"+   
               " where a.sv_product_id = b.sv_product_id "+
               " and b.sv_product_status <> 'N' "+
			   " "+ addsql+" " +
               " order by b.sv_product_id" ;
	    
		 //logger.log(Level.DEBUG,sql);
		 return sql ;

	}

	public String getProductsIngredients(String prodtype)
	{

	    sql =  " select a.sv_product_id ,  a.sv_ingredient_id , a.sv_ingredient_conc , c.sv_product_master_type, d.sv_product_line_prime "+
	     " from avon_product_ingredients a , temp_product b , avon_product_type c, avon_product_line d "+
			 " where a.sv_product_id = b.sv_product_id "+
		   " and b.sv_product_type_id = c.sv_product_type_id " +
		   " and b.sv_product_line_id = d.sv_product_line_id " +
		   " and b.sv_product_status <> 'N' " +
		   " and c.sv_product_master_type = '"+prodtype+"' "+
		   " order by b.sv_product_id " ;
         return sql ;

	}

	public String getIngrFunctions()
	{
	    sql = " select SV_INGREDIENT_ID,SV_INGREDIENT_CONC,SV_FUNCTION_ID  "+
			      " from ingredients_function " ;
	//	  //logger.log(Level.DEBUG,sql);
	    return sql ;
	}

	public String getFunctionPreference(String skintype,String prodtype)
	{
	    sql = " select SV_PREFERENCE_ID from function_preference_skintype where ltrim(rtrim(SV_SKIN_TYPE_ID))  = '"+skintype+"' "+
			      " and ltrim(rtrim(SV_FUNCTION_ACTION_ID)) = '"+prodtype+"'";
	//		//logger.log(Level.DEBUG,sql);
	    return sql ;
	}

	public String getFunctionPreference()
	{
	    sql = " select * from function_preference_skintype ";
	//		//logger.log(Level.DEBUG,sql);
	    return sql ;
	}


	public String getFunctionCountLine(String prodline)
	{
	  String addsql = "" ;
	  if (prodline.equals("0001"))
		  addsql = " and a.sv_product_line_id = '0001' " ;
		else if (prodline.equals("0009"))
		  addsql = " and a.sv_product_line_id in ('0009','0010','0013') " ;
		else if (prodline.equals("0002"))
		  addsql = " and a.sv_product_line_id in ('0002','0008','0012','0014','0015') " ;
		else
		  addsql = "  " ;
	    sql = " select a.sv_product_id, rpad(b.sv_ingredient_name, 50), rpad(e.sv_function_name,20), "+
            " f.sv_function_action_id "+
            " from temp_product a, ingredients b , avon_product_ingredients c, "+
            " ingredients_function d, function_master e, function_actions f , avon_product_type g "+
            " where a.sv_product_id = c.sv_product_id "+
            " and c.sv_ingredient_id = b.sv_ingredient_id "+
            " and c.sv_ingredient_id = d.sv_ingredient_id "+
            " and c.sv_ingredient_conc = d.sv_ingredient_conc "+
            " and d.sv_function_id = e.sv_function_id "+
            " and d.sv_function_id = f.sv_function_id "+
            " and f.sv_function_action_id = g.sv_product_master_type "+
            " and g.sv_product_type_id = a.SV_PRODUCT_TYPE_ID " +
						" "+addsql ;
	//    //logger.log(Level.DEBUG,sql);
			return sql ;
	}

	public String getFavFunctionCount(String prodids)
	{
	    sql = " select a.sv_product_id, rpad(b.sv_ingredient_name, 50), rpad(e.sv_function_name,20), "+
            " f.sv_function_action_id "+
            " from temp_product a, ingredients b , avon_product_ingredients c, "+
            " ingredients_function d, function_master e, function_actions f , avon_product_type g "+
            " where a.sv_product_id = c.sv_product_id "+
            " and c.sv_ingredient_id = b.sv_ingredient_id "+
            " and c.sv_ingredient_id = d.sv_ingredient_id "+
            " and c.sv_ingredient_conc = d.sv_ingredient_conc "+
            " and d.sv_function_id = e.sv_function_id "+
            " and d.sv_function_id = f.sv_function_id "+
            " and f.sv_function_action_id = g.sv_product_master_type "+
            " and g.sv_product_type_id = a.SV_PRODUCT_TYPE_ID "+
						" and a.sv_product_id in "+prodids ;
	//    //logger.log(Level.DEBUG,sql);
			return sql ;
	}

	public String getFunctionCount(String prodtype)
	{
	    sql = " select a.sv_product_id, rpad(b.sv_ingredient_name, 50), rpad(e.sv_function_name,20), "+
            " f.sv_function_action_id "+
            " from temp_product a, ingredients b , avon_product_ingredients c, "+
            " ingredients_function d, function_master e, function_actions f , avon_product_type g  "+
            " where a.sv_product_id = c.sv_product_id "+
            " and c.sv_ingredient_id = b.sv_ingredient_id "+
            " and c.sv_ingredient_id = d.sv_ingredient_id "+
            " and c.sv_ingredient_conc = d.sv_ingredient_conc "+
            " and d.sv_function_id = e.sv_function_id "+
            " and d.sv_function_id = f.sv_function_id "+
            " and f.sv_function_action_id = g.sv_product_master_type "+
            " and g.sv_product_type_id = a.SV_PRODUCT_TYPE_ID  "+
						" and ltrim(rtrim(a.sv_product_type_id)) = ltrim(rtrim(g.sv_product_type_id)) and "+
						" (ltrim(rtrim(g.sv_product_type_id)) = '"+prodtype+"' or ltrim(rtrim(g.SV_PRODUCT_MASTER_TYPE)) = '"+prodtype+"')" ;

	//    //logger.log(Level.DEBUG,sql);
			return sql ;
	}



	public String getBumpRules()
	{
	    sql = "select * from bump_rules " ;
		//	//logger.log(Level.DEBUG,sql);
	    return sql ;
	}

	public String getProductUsage(String query , String answer , String prodtype)
	{
	   sql = " select * from product_usage_benefits where sv_attribute_id = '"+query+"'"+
	         " and SV_ATTRIBUTE_VALUE = '"+answer+"' and SV_PRODUCT_TYPE_ID = '"+prodtype+"'" ;
	//	 //logger.log(Level.DEBUG,sql);
		 return sql ;
	}

	public String getProductsBase()
	{
	   sql = " select a.sv_product_id , a.sv_product_base_id , b.sv_product_master_type "+
			     " from  "+
			     " temp_product a , avon_product_type b where "+
					 " ltrim(rtrim(a.sv_product_type_id)) = ltrim(rtrim(b.sv_product_type_id)) " ;
	//	 //logger.log(Level.DEBUG,sql);
		 return sql ;

	}

	public String getProductsBaseForType(String prodtype)
	{
	   sql = " select a.sv_product_id ,  a.sv_product_base_id , b.sv_product_master_type "+
			     " from  "+
			     " temp_product a , avon_product_type b where "+
					 " ltrim(rtrim(a.sv_product_type_id)) = ltrim(rtrim(b.sv_product_type_id)) and "+
						" (ltrim(rtrim(b.sv_product_type_id)) = '"+prodtype+"' or ltrim(rtrim(b.SV_PRODUCT_MASTER_TYPE)) = '"+prodtype+"')" ;
	//	 //logger.log(Level.DEBUG,sql);
		 return sql ;

	}

	public String getProductDetailsLine(String prodline)
	{
	  String addsql = "" ;
		String addsql2 = "" ;

  	  if (prodline.equals("01"))
			{
		     addsql =  " and ( d.sv_product_line_prime = '01' or d.sv_product_line_prime = '03') " ;
			}
		  else if (prodline.equals("04"))
			{
		     addsql = " and d.sv_product_line_prime = '04' " ;
			}
		  else if (prodline.equals("02"))
			{
		     addsql = " and d.sv_product_line_prime = '02' " ;
				 addsql2 = " union "+
					         " select a.sv_product_id, a.SV_PRODUCT_IMAGE_FILE, a.sv_product_price, "+
			             " a.sv_product_description, a.sv_product_name, a.sv_product_type_id, "+
					         " b.sv_product_master_type, a.sv_product_base_id, a.sv_product_line_id, d.sv_product_line_prime "+
					         " from temp_product a, avon_product_type b, avon_product_line d "+
					         " where a.sv_product_type_id = b.sv_product_type_id " +
									 " and a.sv_product_line_id = d.sv_product_line_id "+
		                             " and a.sv_product_status <> 'N' "+
						       " and b.sv_product_master_type = '04' "+
									 " and d.sv_product_line_prime in ('01','03')   "  ;
			}
		  else
			{
		     addsql = "  " ;
				 addsql2 = " " ;
			}
	   sql = " select a.sv_product_id, a.SV_PRODUCT_IMAGE_FILE, a.sv_product_price, "+
			     " a.sv_product_description, a.sv_product_name, a.sv_product_type_id, "+
					 " b.sv_product_master_type, a.sv_product_base_id, a.sv_product_line_id, d.sv_product_line_prime, "+
					 " NVL(a.sv_product_other_comments,'WOMENS') "+
					 " from temp_product a, avon_product_type b, avon_product_line d "+
					 " where a.sv_product_type_id = b.sv_product_type_id " +
					 " and a.sv_product_line_id   = d.sv_product_line_id  "+
                     " and a.sv_product_status <> 'N' "
					 + " order by a.sv_product_id " ;
		 return sql ;

	}
	public String getBeautyProductIds(String id,String prodselect)
	{
		String sql1 = "" ;
		if (prodselect != null && prodselect.equals("prodLine"))
			sql1 = "and b.sv_product_brand_id = '"+id+"' " ;
		if (prodselect != null && prodselect.equals("prodType"))
			sql1 = "and b.sv_product_category_id in "+id+" " ;
		
		String sql = "" ;
		/**
         * Added by Synoptek(29-11-2019)
         * Query modified to get required column
         * */
		//---------------------------------------------------Start------------------------------------------------------------------------------------------------------
		sql="SELECT a.sv_product_id, b.sv_product_category_id, b.sv_product_base_id,b.sv_product_brand_id,a.sv_product_name,a.sv_product_price,a.sv_product_image_file,"
				+ " b.sv_product_spf_id, a.sv_product_type_id, a.sv_product_sell_copy,a.sku,a.upc, c.sv_product_category_name,d.sv_product_when_to_use_value,"
				+ "e.sv_product_multi_benefits_value,f.sv_product_lifestyle_based_recs_1_value,g.sv_product_lifestyle_based_recs_2_value, h.sv_brand_name FROM temp_product a," // Deep
				+ "avon_product_category c,temp_product_elements b left join  sv_product_when_to_use d "
				+ "on b.sv_product_when_to_use_id = d.sv_product_when_to_use_id left join  sv_product_multi_benefits e"
				+ " on b.sv_product_multi_benefits_id = e.sv_product_multi_benefits_id left join  sv_product_lifestyle_based_recs_1 f "
				+ "on b.sv_product_lifestyle_based_recs_1_id = f.sv_product_lifestyle_based_recs_1_id left join  sv_product_lifestyle_based_recs_2 g "
				+ "on b.sv_product_lifestyle_based_recs_2_id = g.sv_product_lifestyle_based_recs_2_id "
				+ "left join sv_brand_master h on b.sv_product_brand_id = h.sv_brand_id " // Deep
				+ "WHERE a.sv_product_id = b.sv_product_id"  + " " + sql1 + " "
				+ "AND a.sv_product_status = 'Y' AND b.sv_product_category_id = c.sv_product_category_id  ORDER BY a.sv_product_id"; 
		//------------------------------------------End---------------------------------------------
		System.out.println(sql) ;
		return sql ;
	}
	
	public String getSingleBeautyProductIds(String id,String prodselect, String upc)
	{
		String sql1 = "" ;
		if (prodselect != null && prodselect.equals("prodLine"))
			sql1 = "and b.sv_product_brand_id = '"+id+"' " ;
		if (prodselect != null && prodselect.equals("prodType"))
			sql1 = "and b.sv_product_category_id in "+id+" " ;
		
		String sql = "" ;
		/**
         * Added by Synoptek(29-11-2019)
         * Query modified to get required column
         * */
		//---------------------------------------------------Start------------------------------------------------------------------------------------------------------
		sql="SELECT a.sv_product_id, b.sv_product_category_id, b.sv_product_base_id,b.sv_product_brand_id,a.sv_product_name,a.sv_product_price,a.sv_product_image_file,"
				+ " b.sv_product_spf_id, a.sv_product_type_id, a.sv_product_sell_copy,a.sku,a.upc, c.sv_product_category_name,d.sv_product_when_to_use_value,"
				+ "e.sv_product_multi_benefits_value,f.sv_product_lifestyle_based_recs_1_value,g.sv_product_lifestyle_based_recs_2_value, h.sv_brand_name FROM temp_product a," // Deep
				+ "avon_product_category c,temp_product_elements b left join  sv_product_when_to_use d "
				+ "on b.sv_product_when_to_use_id = d.sv_product_when_to_use_id left join  sv_product_multi_benefits e"
				+ " on b.sv_product_multi_benefits_id = e.sv_product_multi_benefits_id left join  sv_product_lifestyle_based_recs_1 f "
				+ "on b.sv_product_lifestyle_based_recs_1_id = f.sv_product_lifestyle_based_recs_1_id left join  sv_product_lifestyle_based_recs_2 g "
				+ "on b.sv_product_lifestyle_based_recs_2_id = g.sv_product_lifestyle_based_recs_2_id "
				+ "left join sv_brand_master h on b.sv_product_brand_id = h.sv_brand_id " // Deep
				+ "WHERE a.sv_product_id = b.sv_product_id AND UPC = '"  + upc.trim() + "' " + sql1 + " "
				//+ "AND a.sv_product_status = 'Y' AND b.sv_product_category_id = c.sv_product_category_id  ORDER BY a.sv_product_id";
				+ "AND b.sv_product_category_id = c.sv_product_category_id  ORDER BY a.sv_product_id"; //Removed product availability check condition in single product API - Shivam - 04/09/2024
		//------------------------------------------End---------------------------------------------
		System.out.println(sql) ;
		return sql ;
	}
	public String getBeautyProductInfo(String prodids)
	{
		String sql = "" ;
		sql = " select a.sv_product_id,    a.sv_product_name,  a.sv_product_price, a.sv_product_image_file, "+  
              " c.sv_brand_name, b.sv_product_spf_id, b.sv_product_category_id, b.sv_product_base_id, a.sv_product_type_id, NVL(a.sv_product_sell_copy,''), a.sv_product_directions from temp_product a, temp_product_elements b, sv_brand_master c " +
              " where a.sv_product_id = b.sv_product_id "+   
              " and b.sv_product_brand_id = c.sv_brand_id "+
			  " and a.sv_product_id in "+prodids +
              " order by a.sv_product_id " ; 
		return sql ;
	}
// Get RX flag from the sv_product_rx_warning for ingredients in the product-- Start
	public String getBeautyProductInfoWarning(String prodids)
	{
		String sql = "" ;
		sql= "select * from sv_product_rx_warning  where sv_ingredient_id in(select sv_ingredient_id from avon_product_ingredients where sv_product_id="+prodids+")" ; 
		
		return sql ;
	}
// Get RX flag from the sv_product_rx_warning for ingredients in the product-- End
	public String getHairProductIds(String id,String prodselect)
	{
		String sql1 = "" ;
		if (prodselect != null && prodselect.equals("prodLine"))
			sql1 = "and b.sv_product_brand_id = '"+id+"' " ;
		if (prodselect != null && prodselect.equals("prodType"))
			sql1 = "and b.sv_product_category_id in "+id+" " ;
		
		String sql = "" ;
		sql = " select a.sv_product_id,    b.sv_product_category_id,  b.sv_product_base_id, b.sv_product_brand_id "+
		      " ,a.sv_product_name,  a.sv_product_price, a.sv_product_image_file, b.sv_product_spf_id,a.sv_product_type_id, a.sv_product_sell_copy "+
              " from temp_product_hair a, temp_product_hair_elements b "+
			  " where a.sv_product_id = b.sv_product_id "+
			  " "+sql1+" "+
              " and a.sv_product_status = 'Y' "+  
              " order by a.sv_product_id " ; 
		System.out.println(sql) ;
		return sql ;
	}
	public String getHairProductInfo(String prodids)
	{
		String sql = "" ;
		sql = " select a.sv_product_id,    a.sv_product_name,  a.sv_product_price, a.sv_product_image_file, "+  
              " c.sv_brand_name, b.sv_product_spf_id, b.sv_product_category_id, b.sv_product_base_id, a.sv_product_type_id, NVL(a.sv_product_sell_copy,''), a.sv_product_directions from temp_product_hair a, temp_product_hair_elements b, sv_brand_master c " +
              " where a.sv_product_id = b.sv_product_id "+   
              " and b.sv_product_brand_id = c.sv_brand_id "+
			  " and a.sv_product_id in "+prodids +
              " order by a.sv_product_id " ; 
		return sql ;
	}
    public String getProductRegimenCustomer(String customerid,String regimenline)
    {
	   String sql = " select a.sv_product_id, a.SV_PRODUCT_IMAGE_FILE, a.sv_product_price, "+
                    " a.sv_product_description, a.sv_product_name, a.sv_product_type_id, "+
	                " b.sv_product_master_type, a.sv_product_base_id, a.sv_product_line_id, "+
					" d.sv_product_line_prime, to_char(a.sv_product_enter_date,'yyyymmddhh24miss'), "+
					" to_char(e.sv_product_regimen_date,'yyyymmddhh24miss'), e.sv_product_regimen_flag, "+
                    " from   temp_product a, avon_product_type b, avon_product_line d, customer_product_regimen e "+
	                " where e.sv_customer_id = '"+customerid+"' "+
	                " and e.sv_product_regimen_line = '"+regimenline+"' "+
	                " and e.sv_product_id = a.sv_product_id "+
	                " and a.sv_product_type_id = b.sv_product_type_id "+
	                " and a.sv_product_line_id = d.sv_product_line_id " ;
//					" and e.sv_product_regimen_flag = 'C' " ;
	   return sql ;
    }
	public String getFavProductDetails(String prodids)
	{
	   sql = " select a.sv_product_id , a.SV_PRODUCT_IMAGE_FILE  , a.sv_product_price , "+
             " a.sv_product_description , a.sv_product_name , a.sv_product_type_id , "+
             " b.SV_PRODUCT_MASTER_TYPE , a.sv_product_base_id,a.SV_PRODUCT_LINE_ID,a.SV_PRODUCT_LINE_ID  "+
             " from temp_product a, avon_product_type b "+
             " where ltrim(rtrim(a.SV_PRODUCT_TYPE_ID)) = ltrim(rtrim(b.SV_PRODUCT_TYPE_ID)) "+
//						" and a.sv_product_line_id = '"+pline+"' "+
             " and a.sv_product_id in "+prodids ;
	//		//logger.log(Level.DEBUG,sql);
			return sql ;

	}

	public String getProductInfo(String productIds)
	{
	   sql = " select a.sv_product_id, a.sv_product_image_file, a.sv_product_price, "+
	   	     " a.sv_product_description, a.sv_product_name, "+
			 " a.sv_product_type_id, b.sv_product_master_type, "+
			 " a.sv_product_base_id, a.sv_product_line_id, d.sv_product_line_prime "+
			 " from temp_product a, avon_product_type b, avon_product_line d "+
			 " where a.sv_product_type_id = b.sv_product_type_id "+
			 " and a.sv_product_line_id = d.sv_product_line_id "+
			 " and a.sv_product_status <> 'N' "+
			 " and a.sv_product_id in ("+productIds+")";
			//logger.log(Level.DEBUG,sql);
			return sql ;
	}

	public String getSpecialtyProductsForMen() {
		sql = "select sv_attribute_id, sv_attribute_value, "+
			  "sv_product_master_type, sv_product_id "+
			  "from speciality_products_men ";

		return sql;
	}

	public String getProductDetailsForType(String prodtype)
	{
		   sql = " select a.sv_product_id, a.SV_PRODUCT_IMAGE_FILE, a.sv_product_price, "+
				     " a.sv_product_description, a.sv_product_name, a.sv_product_type_id, "+
						 " b.sv_product_master_type, a.sv_product_base_id, a.sv_product_line_id, d.sv_product_line_prime, "+
						 " NVL(a.sv_product_other_comments,'WOMENS') "+
						 " from temp_product a, avon_product_type b, avon_product_line d "+
						 " where a.sv_product_type_id = b.sv_product_type_id " +
						 " and a.sv_product_line_id   = d.sv_product_line_id  "+
	                     " and a.sv_product_status <> 'N' " +
						 " and b.sv_product_master_type = '"+prodtype+"' "+
						 " order by a.sv_product_id " ;
			 return sql ;

	}



  public String getProductDetails(String prodid)
	{
	    sql = " select a.sv_product_name,b.sv_product_type_name,c.SV_PRODUCT_BASE_name from temp_product a,avon_product_type b "+
			      " ,avon_product_base c where a.sv_product_id = '"+prodid+"' and a.sv_product_type_id = b.sv_product_type_id "+
						"  and a.sv_product_base_id = c.sv_product_base_id " ;
	//		//logger.log(Level.DEBUG,sql);
	    return sql ;
	}

	public String getCustomerPref(String customerid)
	{
	    sql = " select sv_skin_type_name from customer_skin_basics,skin_type "+
			      " where sv_customer_id = '"+customerid+"' and avon_attrib_value = sv_skin_type_id "+
						" and avon_attrib_id = '51' ";
	//    //logger.log(Level.DEBUG,sql);
			return sql ;
	}

	public String getCustomerSkinConcern(String customerid)
	{
	    sql = " select sv_skin_concern_name from customer_skin_basics,skin_concern "+
			      " where sv_customer_id = '"+customerid+"' and avon_attrib_value = sv_skin_concern_id "+
						" and avon_attrib_id = '53' ";
	//    //logger.log(Level.DEBUG,sql);
			return sql ;
	}
	public String getCustomerSkinCondition(String customerid)
	{
	    sql = " select sv_skin_condition_name from customer_skin_basics,skin_condition "+
			      " where sv_customer_id = '"+customerid+"' and avon_attrib_value = sv_skin_condition_id "+
						" and avon_attrib_id = '52' ";
	//    //logger.log(Level.DEBUG,sql);
			return sql ;
	}
	public String getCustomerAllergies(String customerid)
	{
	    sql = " select SV_ALLERGY_NAME from customer_skin_basics,allergies "+
			      " where sv_customer_id = '"+customerid+"' and avon_attrib_value = SV_ALLERGY_ID "+
						" and avon_attrib_id = '56' " ;
	  //  //logger.log(Level.DEBUG,sql);
			return sql ;

	}
	public String getIngredients_Allergies()
	{
	   sql = " select * from ingredients_allergy " ;
		// //logger.log(Level.DEBUG,sql);
	   return sql ;
	}


	public String getProductMasterType(String prodid)
	{
	   sql = " select  SV_PRODUCT_MASTER_TYPE from temp_product a,avon_product_type b where "+
			     " a.SV_PRODUCT_TYPE_ID = b.SV_PRODUCT_TYPE_ID " ;
	//	 //logger.log(Level.DEBUG,sql);
	   return sql ;
	}

	public String getCustomerProfile(String customid)
	{

	    sql = " select * from customer_basic_profile where sv_customer_id = '"+customid+"' ";
		//	//logger.log(Level.DEBUG,sql);
	    return sql ;
	}
	public String getCustomerProfileHair(String customid)
	{

	    sql = " select * from customer_hair_profile where sv_customer_id = '"+customid+"' "+
		      " union "+
			  " select * from customer_basic_profile where sv_customer_id = '"+customid+"' " ;
		//	//logger.log(Level.DEBUG,sql);
	    return sql ;
	}
	public String getCustomerGenProfile(String customid)
	{

	    sql = " select sv_customer_id, sv_customer_first_name, sv_customer_last_name ,NVL(sv_customer_email,' '), to_char(sv_customer_entry_date,'yyyymmddhh24miss'), NVL(sv_customer_gender,'F') from customer_master where sv_customer_id = '"+customid+"' ";
		//	//logger.log(Level.DEBUG,sql);
	    return sql ;
	}
	public String getCustomerContactProfile(String customid)
	{

	    sql = " select * from customer_contact_preference where sv_customer_id = '"+customid+"' and "+
			      " sv_contact_attrib_id > '60' ";
		//	//logger.log(Level.DEBUG,sql);
	    return sql ;
	}
	public String getCustomerProductPreferences(String customid)
	{

	    sql = " select * from customer_product_line where sv_customer_id = '"+customid+"' and "+
			      " avon_attrib_id > '90' ";
		//	//logger.log(Level.DEBUG,sql);
	    return sql ;
	}

	public String getIngredients()
	{
	   sql = "select * from sv_ingredients_master " ;
	//	 //logger.log(Level.DEBUG,sql);
	   return sql ;
	}

	public String getSkinType()
	{
	   sql = "select * from skin_type" ;
	//	 //logger.log(Level.DEBUG,sql);
		 return sql ;

	}

	public String getIngredientsFunction()
	{
	     sql = " select * from sv_INGREDIENTS_FUNCTION " ;
	//  	 //logger.log(Level.DEBUG,sql);
		   return sql ;
	}

	public String getFunctionsDetails()
	{
	    sql = "select * from sv_function_master " ;
		//	//logger.log(Level.DEBUG,sql);
	    return sql ;
	}

	public String getNonCleanGroupDetails() {
		sql = "select * from SV_NONCLEAN_GROUP_MASTER " ;
		//	//logger.log(Level.DEBUG,sql);
	    return sql ;
	}
	
	public String getGroupFunctionDetails() {
		sql = "select * from SV_FUNCTION_GROUP_MASTER ";
		//	//logger.log(Level.DEBUG,sql);
	    return sql;
	}
	
	public String getDesc(String id,String value,String prodtype,String func)
	{

				sql = " select * from function_combined_benefits where "+
				      " SV_ATTRIBUTE_ID = '"+id.trim()+"' and "+
							" SV_ATTRIBUTE_VALUE = '"+value.trim()+"' and "+
							" SV_PRODUCT_MASTER_TYPE_ID = '"+prodtype.trim()+"' and "+
							" SV_ELEMENT_ID = '"+func+"' and ltrim(rtrim(sv_element_value)) <> 'FFFF' "+
							" and ltrim(rtrim(sv_function_benefit)) <> 'NA' and ltrim(rtrim(sv_function_benefit)) <> 'N/A' " ;
				System.out.println("getDesc :: " + sql);
		//		//logger.log(Level.DEBUG,sql);
				return sql ;

	}

	public String getDesc(String id,String value,String prodtype,String func,String prodid)
	{

				sql = " select * from function_combined_benefits where "+
				      " SV_ATTRIBUTE_ID = '"+id.trim()+"' and "+
							" SV_ATTRIBUTE_VALUE = '"+value.trim()+"' and "+
							" SV_PRODUCT_MASTER_TYPE_ID = '"+prodtype.trim()+"' and "+
							" SV_ELEMENT_ID = '"+func+"' and sv_product_id = '"+prodid+"'" ;
		//		//logger.log(Level.DEBUG,sql);
				return sql ;

	}

	public String getDescHaveNot(String id,String value,String prodtype,String func,String prodid)
	{

				sql = " select * from function_negative_benefits where "+
				      " SV_ATTRIBUTE_ID = '"+id.trim()+"' and "+
							" SV_ATTRIBUTE_VALUE = '"+value.trim()+"' and "+
							" SV_PRODUCT_MASTER_TYPE_ID = '"+prodtype.trim()+"' and "+
							" SV_ELEMENT_ID = '"+func+"' and sv_product_id = '"+prodid+"'" ;
		//		//logger.log(Level.DEBUG,sql);
				return sql ;

	}



	public String getProdTypes()
	{
	    sql = " select * from avon_product_master_type " ;
		//	//logger.log(Level.DEBUG,sql);
	    return sql ;
	}

	public String getProductTypes()
	{
	   sql = " select * from avon_product_master_type " ;
	//	 //logger.log(Level.DEBUG,sql);
		 return sql ;

	}

	public String eliminateIngredients()
	{
	    	sql = " select distinct(a) from "+
			      " ( select ('02' || '-' || ltrim(rtrim(sv_product_element_value)) || '-' || ltrim(rtrim(sv_product_element_conc)) || '-' || ltrim(rtrim(SV_PRODUCT_TYPE_MASTER_ID))) a  from sv_ratings where "+
            "  sv_rating_value = '0' )  " ;
	//		 //logger.log(Level.DEBUG,sql);
			 return sql ;

	}


	public String eliminateIngredients(String skintype,ArrayList skincondition)
	{
	    String cond = "" ;
			String skinsql = "" ;

			if(skintype!=null && skintype.trim().length()!=0)
			   skinsql = " and sv_body_attribute_value = '"+skintype+"'" ;
			/**
			 * Added By Synoptek (01-05-2020)
			 * To stop eliminating products without user's skin condition selection
			 * */
		//----------------------------------Start--------------------------------------
			if (skincondition != null && skincondition.size() != 0) {
				for (int i = 0; i < skincondition.size(); i++) {
					if (i == 0)
						cond = " and ( sv_body_attribute_value = '" + skincondition.get(i) + "'";
					else
						cond = cond + "  or sv_body_attribute_value = '" + skincondition.get(i) + "' ";
					if (i == skincondition.size() - 1)
						cond = cond + ")";

				}
			}
			else {
				cond = " and sv_body_attribute_value = ''" ;
			}
		//----------------------------------End--------------------------------------
			// get the ingredients appeneded with their concentration which r present in both skin type(51)
			// and skin condition(52) attributes whose rating is equal to '0'
			sql = " select distinct(a) from "+
			      " ( select ('02' || '-' || ltrim(rtrim(sv_product_element_value)) || '-' || ltrim(rtrim(sv_product_element_conc)) || '-' || ltrim(rtrim(SV_PRODUCT_TYPE_MASTER_ID))) a  from sv_ratings where "+
            " sv_product_element_id = '02'  and sv_body_attribute_id = '51' "+
            " "+skinsql+"  and sv_rating_value = '0'  "+
            " union "+
            " select ('02' || '-' || ltrim(rtrim(sv_product_element_value)) || '-' || ltrim(rtrim(sv_product_element_conc)) || '-' || ltrim(rtrim(SV_PRODUCT_TYPE_MASTER_ID))) a from sv_ratings where "+
            " sv_product_element_id = '02' and sv_body_attribute_id = '52' "+ cond +" "+
						" and sv_rating_value = '0' ) " ;
			// log.debug(sql);
	//		//logger.log(Level.DEBUG,sql);
			return sql ;

	}

	public String eliminateIngredientsBasedOnAttribute(String skintype,ArrayList skincondition, ArrayList specification)
	{
	    	String cond = "" ;
			String skinsql = "" ;
			String specific = "";
			
			ArrayList dupSpecification 	= new ArrayList();
			
			for(int i = 0; i < specification.size(); i++) {
				if(!specification.get(i).toString().equalsIgnoreCase("CE03") && !specification.get(i).toString().equalsIgnoreCase("CE04") && !specification.get(i).toString().equalsIgnoreCase("CE05")) {
					dupSpecification.add(specification.get(i));
				}
			}
			System.out.println("SPECIFICATION :: " + specification);
			System.out.println("DUP SPECIFICATION :: " + dupSpecification);
			
			if(skintype!=null && skintype.trim().length()!=0)
			   skinsql = " and sv_body_attribute_value = '"+skintype+"'" ;
			/**
			 * Added By Synoptek (01-05-2020)
			 * To stop eliminating products without user's skin condition selection
			 * */
		//----------------------------------Start--------------------------------------
			if (skincondition != null && skincondition.size() != 0) {
				for (int i = 0; i < skincondition.size(); i++) {
					if (i == 0)
						cond = " and ( sv_body_attribute_value = '" + skincondition.get(i) + "'";
					else
						cond = cond + "  or sv_body_attribute_value = '" + skincondition.get(i) + "' ";
					if (i == skincondition.size() - 1)
						cond = cond + ")";

				}
			}
			else {
				cond = " and sv_body_attribute_value = ''" ;
			}
			
			if (dupSpecification != null && dupSpecification.size() != 0) {
				for (int i = 0; i < dupSpecification.size(); i++) {
					if (i == 0)
						specific = " and ( sv_body_attribute_value = '" + dupSpecification.get(i) + "'";
					else
						specific = specific + "  or sv_body_attribute_value = '" + dupSpecification.get(i) + "' ";
					if (i == dupSpecification.size() - 1)
						specific = specific + ")";

				}
			}
			else {
				specific = " and sv_body_attribute_value = ''" ;
			}
			
//			if (specification != null && specification.size() != 0) {
//				for (int i = 0; i < specification.size(); i++) {
//					if (i == 0)
//						specific = " and ( sv_body_attribute_value = '" + specification.get(i) + "'";
//					else
//						specific = specific + "  or sv_body_attribute_value = '" + specification.get(i) + "' ";
//					if (i == specification.size() - 1)
//						specific = specific + ")";
//
//				}
//			}
//			else {
//				specific = " and sv_body_attribute_value = ''" ;
//			}
				
		//----------------------------------End--------------------------------------
			// get the ingredients appeneded with their concentration which r present in both skin type(51)
			// and skin condition(52) and specification(60) attributes whose rating is equal to '0'
			sql = " select distinct(a) from "+
			      " ( select ('02' || '-' || ltrim(rtrim(sv_product_element_value)) || '-' || ltrim(rtrim(sv_product_element_conc)) || '-' || ltrim(rtrim(SV_PRODUCT_TYPE_MASTER_ID))) a  from sv_ratings where "+
            " sv_product_element_id = '02'  and sv_body_attribute_id = '51' "+
            " "+skinsql+"  and sv_rating_value = '0'  "+
            " union "+
            " select ('02' || '-' || ltrim(rtrim(sv_product_element_value)) || '-' || ltrim(rtrim(sv_product_element_conc)) || '-' || ltrim(rtrim(SV_PRODUCT_TYPE_MASTER_ID))) a from sv_ratings where "+
            " sv_product_element_id = '02' and sv_body_attribute_id = '52' "+ cond +" "+
						" and sv_rating_value = '0'  " + 
            " union " +
            " select ('02' || '-' || ltrim(rtrim(sv_product_element_value)) || '-' || ltrim(rtrim(sv_product_element_conc)) || '-' || ltrim(rtrim(SV_PRODUCT_TYPE_MASTER_ID))) a from sv_ratings where "+
            " sv_product_element_id = '02' and sv_body_attribute_id = '60' "+ specific +" "+
						" and sv_rating_value = '0' ) " 			
			;
			System.out.println("SQL :: " + sql);
			// log.debug(sql);
	//		//logger.log(Level.DEBUG,sql);
			return sql ;

	}
	
	public  String eliminateAllIngredientsBasedOnAttribute(String skintype,ArrayList skincondition, ArrayList specification)
	{
	    	String cond = "" ;
			String skinsql = "" ;
			String specific = "";
			
			ArrayList dupSpecification 	= new ArrayList();
			
			System.out.println("SPECIFICATION :: " + specification);
			for(int i = 0; i < specification.size(); i++) {
				System.out.println("IN FOR LOOP");
				if(!specification.get(i).toString().equalsIgnoreCase("CE03") && !specification.get(i).toString().equalsIgnoreCase("CE04") && !specification.get(i).toString().equalsIgnoreCase("CE05")) {
					System.out.println("IN IF");
					dupSpecification.add(specification.get(i));
				}
			}
			System.out.println("SPECIFICATION :: " + specification);
			System.out.println("DUP SPECIFICATION :: " + dupSpecification);
			
			if(skintype!=null && skintype.trim().length()!=0)
			   skinsql = " and sv_body_attribute_value = '"+skintype+"'" ;
			/**
			 * Added By Synoptek (01-05-2020)
			 * To stop eliminating products without user's skin condition selection
			 * */
		//----------------------------------Start--------------------------------------
			if (skincondition != null && skincondition.size() != 0) {
				for (int i = 0; i < skincondition.size(); i++) {
					if (i == 0)
						cond = " and ( sv_body_attribute_value = '" + skincondition.get(i) + "'";
					else
						cond = cond + "  or sv_body_attribute_value = '" + skincondition.get(i) + "' ";
					if (i == skincondition.size() - 1)
						cond = cond + ")";

				}
			}
			else {
				cond = " and sv_body_attribute_value = ''" ;
			}
			
			if (dupSpecification != null && dupSpecification.size() != 0) {
				for (int i = 0; i < dupSpecification.size(); i++) {
					if (i == 0)
						specific = " and ( sv_body_attribute_value = '" + dupSpecification.get(i) + "'";
					else
						specific = specific + "  or sv_body_attribute_value = '" + dupSpecification.get(i) + "' ";
					if (i == dupSpecification.size() - 1)
						specific = specific + ")";

				}
			}
			else {
				specific = " and sv_body_attribute_value = ''" ;
			}
			
//			if (specification != null && specification.size() != 0) {
//				for (int i = 0; i < specification.size(); i++) {
//					if (i == 0)
//						specific = " and ( sv_body_attribute_value = '" + specification.get(i) + "'";
//					else
//						specific = specific + "  or sv_body_attribute_value = '" + specification.get(i) + "' ";
//					if (i == specification.size() - 1)
//						specific = specific + ")";
//
//				}
//			}
//			else {
//				specific = " and sv_body_attribute_value = ''" ;
//			}
				
		//----------------------------------End--------------------------------------
			// get the ingredients appeneded with their concentration which r present in both skin type(51)
			// and skin condition(52) and specification(60) attributes whose rating is equal to '0'
			sql ="select ('02' || '-' || ltrim(rtrim(sv_product_element_value)) || '-' || ltrim(rtrim(sv_product_element_conc)) || '-' || ltrim(rtrim(SV_PRODUCT_TYPE_MASTER_ID))) a,sv_ratings.SV_BODY_ATTRIBUTE_VALUE   from sv_ratings where "+
            " sv_product_element_id = '02'  and sv_body_attribute_id = '51' "+
            " "+skinsql+"  and sv_rating_value = '0'  "+
            " union "+
            " select ('02' || '-' || ltrim(rtrim(sv_product_element_value)) || '-' || ltrim(rtrim(sv_product_element_conc)) || '-' || ltrim(rtrim(SV_PRODUCT_TYPE_MASTER_ID))) a,sv_ratings.SV_BODY_ATTRIBUTE_VALUE  from sv_ratings where "+
            " sv_product_element_id = '02' and sv_body_attribute_id = '52' "+ cond +" "+
						" and sv_rating_value = '0'  " + 
            " union " +
            " select ('02' || '-' || ltrim(rtrim(sv_product_element_value)) || '-' || ltrim(rtrim(sv_product_element_conc)) || '-' || ltrim(rtrim(SV_PRODUCT_TYPE_MASTER_ID))) a,sv_ratings.SV_BODY_ATTRIBUTE_VALUE  from sv_ratings where "+
            " sv_product_element_id = '02' and sv_body_attribute_id = '60' "+ specific +" "+
						" and sv_rating_value = '0'" 			
			;
			System.out.println("SQL :: " + sql);
			// log.debug(sql);
	//		//logger.log(Level.DEBUG,sql);
			return sql ;

	}

	public String getEliminatedIngredients(String skintype,Vector skincondition,String prodmastertype)
	{
	    String cond = "" ;
			String skinsql = "" ;

			if(skintype!=null && skintype.trim().length()!=0)
			   skinsql = " and sv_body_attribute_value = '"+skintype+"'" ;

			for(int i = 0 ; i < skincondition.size() ; i++)
			{
			   if(i==0)
					 cond = " and ( sv_body_attribute_value = '"+skincondition.elementAt(i)+"'" ;
				 else
					 cond = cond + "  or sv_body_attribute_value = '"+skincondition.elementAt(i)+"' " ;
			   if(i == skincondition.size()-1 )
					 cond = cond + ")" ;

			}
			// get the ingredients appeneded with their concentration which r present in both skin type(51)
			// and skin condition(52) attributes whose rating is equal to '0'
			sql = " select distinct(a1) from "+
			      " ( select (ltrim(rtrim(sv_product_element_value)) || '-' || ltrim(rtrim(SV_PRODUCT_TYPE_MASTER_ID))) a1  from sv_ratings   where "+
            " sv_product_element_id = '02'  and sv_body_attribute_id = '51' "+
            " "+skinsql+"  and sv_rating_value = '0'  "+
						" and (SV_PRODUCT_TYPE_MASTER_ID =  '"+prodmastertype+"' or SV_PRODUCT_TYPE_MASTER_ID =  '99' )  "+
            " union "+
            " select (ltrim(rtrim(sv_product_element_value)) || '-' || ltrim(rtrim(SV_PRODUCT_TYPE_MASTER_ID))) a1 from sv_ratings  where "+
            " sv_product_element_id = '02' and sv_body_attribute_id = '52' "+ cond +" "+
						" and sv_rating_value = '0' "+
						" and (SV_PRODUCT_TYPE_MASTER_ID =  '"+prodmastertype+"' or SV_PRODUCT_TYPE_MASTER_ID =  '99') order by a1 ) " ;

	//		//logger.log(Level.DEBUG,sql);
			return sql ;

	}


	/* public String getIngredientsRatings()
	{
	    sql =   "   select *  from ratings where "+
              "   sv_rating_value > '0'  and sv_product_element_id  <> '10' and SV_RATING_VALUE <> ':n'  "+
							"   and sv_rating_value <> 'NA' " ;
		//	//logger.log(Level.DEBUG,sql);
			return sql ;

	}
	*/
	// query modified on 03/14/05 in order to eleminate base 0 products
	public String getIngredientsRatings()
	{

	    //sql =   "   select *  from ratings  "+
        sql = "select sv_product_element_id,sv_product_element_value,sv_product_element_conc,sv_product_type_master_id," +
		     " sv_body_attribute_id,sv_body_attribute_value,ltrim(rtrim(sv_rating_value)) as sv_rating_value from sv_ratings "+
			"   where sv_product_element_id <> '10' "+
							"   and (sv_product_element_id = '01'  or  "         +
							"        (sv_product_element_id = '02' and sv_rating_value > '0') )  "     +
							"   and SV_RATING_VALUE <> ':n'  "       +
							"   and sv_rating_value <> 'NA' " ;
		return sql ;

	}

	public String getIngredientsRatingsForProduct(String prodid,String prodMasterType,String baseId)
	{

	    //sql =   "   select *  from ratings  "+
       /* sql = "select sv_product_element_id,sv_product_element_value,sv_product_element_conc,sv_product_type_master_id," +
		     " sv_body_attribute_id,sv_body_attribute_value,ltrim(rtrim(sv_rating_value)) as sv_rating_value from ratings "+
			"   where sv_product_element_id <> '10' "+
							"   and (sv_product_element_id = '01'  or  "         +
							"        (sv_product_element_id = '02' and sv_rating_value > '0') )  "     +
							"   and SV_RATING_VALUE <> ':n'  "       +
							"   and sv_rating_value <> 'NA' "+
							" and sv_product_element_value in (select sv_ingredient_id from avon_product_ingredients where sv_product_id = '"+prodid+"')";
		*/
        sql = " select sv_product_element_id,sv_product_element_value,sv_product_element_conc,sv_product_type_master_id,"+
			  "	sv_body_attribute_id,sv_body_attribute_value,ltrim(rtrim(sv_rating_value)) as sv_rating_value from sv_ratings a "+
			  " where sv_product_type_master_id in ('"+prodMasterType+"','99') "+
			  "	and ( sv_rating_value > '0' and SV_RATING_VALUE <> ':n' and sv_rating_value <> 'NA' ) "+
			  "	and ((sv_product_element_id = '02' and sv_product_element_value in "+
		      " (select sv_ingredient_id from avon_product_ingredients where  sv_product_id in "+prodid+")) or "+
			  " (sv_product_element_id = '01' and sv_product_element_value =  '"+baseId+"'))";


        return sql ;

	}



	public String getManual()
	{
	    sql =   "   select *  from sv_ratings where "+
              "   sv_rating_value > '0'  and sv_product_element_id  = '10' and SV_RATING_VALUE <> ':n' "+
							"  and sv_rating_value <> 'NA' " ;

	//		//logger.log(Level.DEBUG,sql);
			return sql ;
	}


  public String getIngredientsRatings(String skintype,ArrayList skincondition,ArrayList skinconcern,ArrayList eyeconcern,String ingrlist)
	{
	    String skinsql = "" ;
	    String cond    = "" ;
		String concern = "" ;
		String econcern = "" ;
		String agesql  = "" ;
		String envsql  = "" ;
		String ingrsql = "" ;

	    skinsql = " and sv_body_attribute_value = '"+skintype+"'" ;
		cond    = " and sv_body_attribute_value = '' " ;
		concern = " and sv_body_attribute_value = '' " ;

		for(int i = 0 ; i < skincondition.size() ; i++)
		{
			if(i==0)
					 cond = " and ( sv_body_attribute_value = '"+skincondition.get(i)+"'" ;
			else
					 cond = cond + "  or sv_body_attribute_value = '"+skincondition.get(i)+"' " ;
			if(i == skincondition.size()-1 )
					 cond = cond + ")" ;

		}
		for(int i = 0 ; i < skinconcern.size() ; i++)
		{
		   if(i==0)
				 concern = " and ( sv_body_attribute_value = '"+skinconcern.get(i)+"'" ;
		   else
				 concern = concern + "  or sv_body_attribute_value = '"+skinconcern.get(i)+"' " ;
		   if(i == skinconcern.size()-1 )
				 concern = concern + ")" ;

	    }
		for(int i = 0 ; i < eyeconcern.size() ; i++)
		{
		   if(i==0)
				 econcern = " and ( sv_body_attribute_value = '"+eyeconcern.get(i)+"'" ;
		   else
				 econcern = econcern + "  or sv_body_attribute_value = '"+eyeconcern.get(i)+"' " ;
		   if(i == eyeconcern.size()-1 )
				 econcern = econcern + ")" ;

	    }
		/**
		 * Added By Synoptek (14-01-2020)
		 * Get ingredients who do not have rating NA or 00
		 * */
		//----------------------------------------------------------------Start--------------------------------------------------------------
				sql = "   select *  from sv_ratings where "+
          //  "   sv_product_element_id = '02' and
					  "   sv_body_attribute_id = '51' " + skinsql +" "+
                      "   and (sv_product_element_id = '01'  or  "         +
					  "   (sv_product_element_id = '02' and sv_rating_value > '0'  ) ) "+
					  "   and SV_RATING_VALUE <> ':n'  and sv_rating_value <> '00'    and sv_rating_value <> 'NA' "+
				//	  "   "+ingrsql+""+
					  "   union "+
                      "   select *  from sv_ratings where "+
                      "       sv_body_attribute_id = '52' "+ cond +" "+
					  "   and sv_rating_value > '0'  and sv_rating_value <> '00'  and SV_RATING_VALUE <> ':n'  and sv_rating_value <> 'NA' "+
				//	  "   "+ingrsql+""+
					  "   union "+
					  "   select *  from sv_ratings where "+
                      "   sv_body_attribute_id = '53'  "+ concern +" "+
					  "   and sv_rating_value > '0'  and sv_rating_value <> '00'   and SV_RATING_VALUE <> ':n'  and sv_rating_value <> 'NA' " +
				      "   union "+
				      "   select *  from sv_ratings where "+
                      "   sv_body_attribute_id = '54'  "+ econcern +" "+
				      "   and sv_rating_value > '0'  and sv_rating_value <> '00'   and SV_RATING_VALUE <> ':n'  and sv_rating_value <> 'NA' " ;
		  //----------------------------------------------------------------End--------------------------------------------------------------
				//log.debug("SQL -------------------"+sql);
			return sql ;

	}
  public String getIngredientsRatingsAboveZero(String skintype,ArrayList skincondition,ArrayList skinconcern,ArrayList eyeconcern,String ingrlist, ArrayList specification)
	{
	    String skinsql = "" ;
	    String cond    = "" ;
		String concern = "" ;
		String econcern = "" ;
		String agesql  = "" ;
		String envsql  = "" ;
		String ingrsql = "" ;
		String specfic = "" ;
		String ingreIds = "";
		String ingreIdsOr = "";
		String ingreIdsAnd = "";


	    skinsql = " and sv_body_attribute_value = '"+skintype+"'" ;
		cond    = " and sv_body_attribute_value = '' " ;
		concern = " and sv_body_attribute_value = '' " ;
		specfic = " and sv_body_attribute_value = '' " ;
		
		if(ingrlist != null && ingrlist != "") {
			ingreIds = " SV_PRODUCT_ELEMENT_VALUE in (" + ingrlist + ")";
			ingreIdsOr = "OR   SV_PRODUCT_ELEMENT_VALUE in (" + ingrlist + ")";
			ingreIdsAnd = "AND   SV_PRODUCT_ELEMENT_VALUE in (" + ingrlist + ")";
		}

		for(int i = 0 ; i < skincondition.size() ; i++)
		{
			if(i==0)
					 cond = " and ( sv_body_attribute_value = '"+skincondition.get(i)+"'" ;
			else
					 cond = cond + "  or sv_body_attribute_value = '"+skincondition.get(i)+"' " ;
			if(i == skincondition.size()-1 )
					 cond = cond + ")" ;

		}
		for(int i = 0 ; i < skinconcern.size() ; i++)
		{
		   if(i==0)
				 concern = " and ( sv_body_attribute_value = '"+skinconcern.get(i)+"'" ;
		   else
				 concern = concern + "  or sv_body_attribute_value = '"+skinconcern.get(i)+"' " ;
		   if(i == skinconcern.size()-1 )
				 concern = concern + ")" ;

	    }
		for(int i = 0 ; i < eyeconcern.size() ; i++)
		{
		   if(i==0)
				 econcern = " and ( sv_body_attribute_value = '"+eyeconcern.get(i)+"'" ;
		   else
				 econcern = econcern + "  or sv_body_attribute_value = '"+eyeconcern.get(i)+"' " ;
		   if(i == eyeconcern.size()-1 )
				 econcern = econcern + ")" ;

	    }
		for(int i = 0 ; i < specification.size() ; i++)
		{
		   if(i==0)
			   specfic = " and ( sv_body_attribute_value = '"+specification.get(i)+"'" ;
		   else
			   specfic = specfic + "  or sv_body_attribute_value = '"+specification.get(i)+"' " ;
		   if(i == specification.size()-1 )
			   specfic = specfic + ")" ;

	    }
		/**
		 * Added By Synoptek (14-01-2020)
		 * Get ingredients who do not have rating NA or 00
		 * */
		//----------------------------------------------------------------Start--------------------------------------------------------------
				if(ingrlist != null && ingrlist.length() > 0) {
					sql = "   select *  from sv_ratings where "+
					        //  "   sv_product_element_id = '02' and
										  "   sv_body_attribute_id = '51' " + skinsql +" "+
					                    "   and (sv_product_element_id = '01'  or  "         +
										  "   (" + ingreIds + " and sv_rating_value > '0'  ) ) "+
										  "   and SV_RATING_VALUE <> ':n'  and sv_rating_value <> '00' and sv_rating_value <> 'NA' "+
									//	  "   "+ingrsql+""+
										  "   union "+
					                    "   select *  from sv_ratings where "+
					                    "       sv_body_attribute_id = '52' "+ cond +" "+
					                    "   and (sv_product_element_id = '01'  or  "         +
										  "   (" + ingreIds + " and sv_rating_value > '0'  ) ) "+
										  "   and sv_rating_value > '0'  and sv_rating_value <> '00'  and SV_RATING_VALUE <> ':n' and sv_rating_value <> 'NA' "+
									//	  "   "+ingrsql+""+
										  "   union "+
										  "   select *  from sv_ratings where "+
					                    "   sv_body_attribute_id = '53'  "+ concern +" "+
					                    "   and (sv_product_element_id = '01'  or  "         +
										  "   (" + ingreIds + " and sv_rating_value > '0'  ) ) "+
										  "   and sv_rating_value > '0'  and sv_rating_value <> '00'   and SV_RATING_VALUE <> ':n' and sv_rating_value <> 'NA' " +
									      "   union "+
									      "   select *  from sv_ratings where "+
					                    "   sv_body_attribute_id = '54'  "+ econcern +" "+
					                    "   and (sv_product_element_id = '01'  or  "         +
										  "   (" + ingreIds + " and sv_rating_value > '0'  ) ) "+
									      "   and sv_rating_value > '0'  and sv_rating_value <> '00'   and SV_RATING_VALUE <> ':n' and sv_rating_value <> 'NA' " +
									      "   union "+
									      "   select *  from sv_ratings where "+
					                    "   sv_body_attribute_id = '60'  "+ specfic +" "+
					                    "   and (sv_product_element_id = '01'  or  "         +
										  "   (" + ingreIds + " and sv_rating_value > '0'  ) ) "+
									      "   and sv_rating_value > '0'  and sv_rating_value <> '00'   and SV_RATING_VALUE <> ':n' and sv_rating_value <> 'NA' ";
				}
				else {
					sql = "   select *  from sv_ratings where "+
					        //  "   sv_product_element_id = '02' and
										  "   sv_body_attribute_id = '51' " + skinsql +" "+
					                    "   and (sv_product_element_id = '01'  or  "         +
										  "   (sv_product_element_id = '02' and sv_rating_value > '0'  ) ) "+
										  "   and SV_RATING_VALUE <> ':n'  and sv_rating_value <> '00' and sv_rating_value <> 'NA' "+
									//	  "   "+ingrsql+""+
										  "   union "+
					                    "   select *  from sv_ratings where "+
					                    "       sv_body_attribute_id = '52' "+ cond +" "+
										  "   and sv_rating_value > '0'  and sv_rating_value <> '00'  and SV_RATING_VALUE <> ':n' and sv_rating_value <> 'NA' "+
									//	  "   "+ingrsql+""+
										  "   union "+
										  "   select *  from sv_ratings where "+
					                    "   sv_body_attribute_id = '53'  "+ concern +" "+
										  "   and sv_rating_value > '0'  and sv_rating_value <> '00'   and SV_RATING_VALUE <> ':n' and sv_rating_value <> 'NA' " +
									      "   union "+
									      "   select *  from sv_ratings where "+
					                    "   sv_body_attribute_id = '54'  "+ econcern +" "+
									      "   and sv_rating_value > '0'  and sv_rating_value <> '00'   and SV_RATING_VALUE <> ':n' and sv_rating_value <> 'NA' " +
									      "   union "+
									      "   select *  from sv_ratings where "+
					                    "   sv_body_attribute_id = '60'  "+ specfic +" "+
									      "   and sv_rating_value > '0'  and sv_rating_value <> '00'   and SV_RATING_VALUE <> ':n' and sv_rating_value <> 'NA' ";
				}
		  //----------------------------------------------------------------End--------------------------------------------------------------
				System.out.println("SQL -------------------"+sql);
			return sql ;

	}

public String getProductsBase(String prodid)
	{
	   sql = " select ('01' || '-' || a.sv_product_base_id || '-' || 'NA' || '-' || b.sv_product_master_type) "+
			     " from  "+
			     " temp_product a , avon_product_type b where "+
					 " a.sv_product_type_id = b.sv_product_type_id "+
					 " and a.sv_product_id = '"+prodid+"'";
	//	 //logger.log(Level.DEBUG,sql);
		 return sql ;

	}

	public String getManualRatings()
	{
	   sql = " select * from sv_ratings where sv_product_element_id = '10' "+
            " order by sv_product_element_value " ;
   //  //logger.log(Level.DEBUG,sql);
	   return sql ;
	}

	public String getCustomerRegimen(String customerid)
	{
	    sql = " select  REGI_QUERY_ID,REGI_ANSWER_ID  from customer_regimen "+
			       " where SV_CUSTOMER_ID = '"+customerid+"'" ;
	//    //logger.log(Level.DEBUG,sql);
			return sql ;
	}

	public String getOtherSellingTools()
	{
	    sql = " select  * from other_selling_suggessions " ;
			return sql ;
	}

 
  public String getRatings(String skintype,Vector skincondition,Vector skinconcern,String age,Vector env,String prodid)
	{
	    String skinsql = "" ;
	    String cond    = "" ;
			String concern = "" ;
			String agesql  = "" ;
			String envsql  = "" ;


		//	if(ingrlist!=null && ingrlist.trim().length()!=0 )
//			  ingrsql = " and (sv_product_element_value || '-' || sv_product_element_conc || '-' ||  SV_PRODUCT_TYPE_MASTER_ID) in  ("+ingrlist+" )" ;

			//if(skintype!=null && skintype.trim().length()!=0)
			  skinsql = " and a.sv_body_attribute_value = '"+skintype+"'" ;
				cond    = " and a.sv_body_attribute_value = '' " ;
				concern = " and a.sv_body_attribute_value = '' " ;
				envsql  = " and a.sv_body_attribute_value = '' " ;

			for(int i = 0 ; i < skincondition.size() ; i++)
			{
			   if(i==0)
					 cond = " and ( a.sv_body_attribute_value = '"+skincondition.elementAt(i)+"'" ;
				 else
					 cond = cond + "  or a.sv_body_attribute_value = '"+skincondition.elementAt(i)+"' " ;
			   if(i == skincondition.size()-1 )
					 cond = cond + ")" ;

			}
			for(int i = 0 ; i < skinconcern.size() ; i++)
			{
			   if(i==0)
					 concern = " and ( a.sv_body_attribute_value = '"+skinconcern.elementAt(i)+"'" ;
				 else
					 concern = concern + "  or a.sv_body_attribute_value = '"+skinconcern.elementAt(i)+"' " ;
			   if(i == skinconcern.size()-1 )
					 concern = concern + ")" ;

			}
			for(int i = 0 ; i < env.size() ; i++)
			{
			   if(i==0)
					 envsql = " and ( sv_body_attribute_value = '"+env.elementAt(i)+"'" ;
				 else
					 envsql = envsql + "  or sv_body_attribute_value = '"+env.elementAt(i)+"' " ;
			   if(i == env.size()-1 )
					 envsql = envsql + ")" ;

			}


			//if(age!=null && age.trim().length()!=0)
			  agesql = " and a.sv_body_attribute_value = '"+age+"'" ;

			//if(env!=null && env.trim().length()!=0)


			// get the ingredients and bases  which r present in both skin type(51)
			// and skin condition(52) attributes whose ratings r greater than '0'

			  String ingrsql = " and ( "+
				                "     ((c.sv_product_type_id = d.sv_product_type_id ) and (a.SV_PRODUCT_ELEMENT_VALUE  || '-' ||  a.SV_PRODUCT_ELEMENT_CONC || '-' || "+
				                " a.SV_PRODUCT_TYPE_MASTER_ID) = (b.SV_INGREDIENT_ID || '-' || b.SV_INGREDIENT_CONC "+
												" || '-' ||  c.sv_product_master_type)) or "+
												" ((a.SV_PRODUCT_ELEMENT_VALUE  || '-' ||  a.SV_PRODUCT_ELEMENT_CONC || '-' || "+
				                " a.SV_PRODUCT_TYPE_MASTER_ID) =  (b.SV_INGREDIENT_ID || '-' || b.SV_INGREDIENT_CONC "+
												" || '-' || '99'))) " ;

				/*String ingrsql = " and  (a.SV_PRODUCT_ELEMENT_VALUE  || '-' ||  a.SV_PRODUCT_ELEMENT_CONC || '-' || "+
				                " a.SV_PRODUCT_TYPE_MASTER_ID) =  (b.SV_INGREDIENT_ID || '-' || b.SV_INGREDIENT_CONC "+
												" || '-' || 'NA')" ;
					*/
				String fields = "  distinct (SV_PRODUCT_ELEMENT_ID || '-' || SV_PRODUCT_ELEMENT_VALUE || '-' || SV_PRODUCT_ELEMENT_CONC "+
                        " || '-' || SV_PRODUCT_TYPE_MASTER_ID  || '-' || SV_RATING_VALUE || '-' || sv_body_attribute_id || '-' || sv_body_attribute_value ) " ;

			  String condi   = " order by  (SV_PRODUCT_ELEMENT_ID || '-' || SV_PRODUCT_ELEMENT_VALUE || '-' || SV_PRODUCT_ELEMENT_CONC "+
                         " || '-' || SV_PRODUCT_TYPE_MASTER_ID  || '-' || SV_RATING_VALUE || '-' || sv_body_attribute_id || '-' || sv_body_attribute_value ) desc " ;


				sql    = "   select distinct(x) from ( "+
				         "   select "+fields+" x from sv_ratings a,avon_product_ingredients b,avon_product_type c,temp_product d where "+
                 "   a.sv_body_attribute_id = '51' "+ skinsql +" "+
                 "   and a.sv_rating_value > '0' and SV_RATING_VALUE <> ':n'  and sv_rating_value <> 'NA' "+
							   "   and b.sv_product_id =  '"+prodid+"'"+
								 "   and b.sv_product_id = d.sv_product_id "+
				         " "+ingrsql+""+
								// " "+condi+""+
					       "   union "+
                 "   select "+fields+"  x from sv_ratings a,avon_product_ingredients b,avon_product_type c,temp_product d  where "+
                 "   a.sv_body_attribute_id = '52' "+ cond +" "+
						     "   and a.sv_rating_value > '0' and SV_RATING_VALUE <> ':n'  and sv_rating_value <> 'NA' "+
							   "   and b.sv_product_id =  '"+prodid+"'"+
								 "   and b.sv_product_id = d.sv_product_id "+
				         "   "+ingrsql+""+
								// " "+condi+""+
						     "   union "+
						     "   select "+fields+"  x from sv_ratings a,avon_product_ingredients b,avon_product_type c,temp_product d  where "+
                 "   a.sv_body_attribute_id = '53' "+ concern +" "+
						     "   and a.sv_rating_value > '0' and SV_RATING_VALUE <> ':n'  and sv_rating_value <> 'NA' "+
								 "   and b.sv_product_id =  '"+prodid+"'"+
                 "   and b.sv_product_id = d.sv_product_id  "+
				       	 "   "+ingrsql+""+
					  		// " "+condi+""+
								"   union "+
						     "   select "+fields+" x from sv_ratings a,avon_product_ingredients b,avon_product_type c,temp_product d  where "+
                 "   a.sv_body_attribute_id = '54' "+ envsql +" "+
						     "   and a.sv_rating_value > '0' and SV_RATING_VALUE <> ':n'  and sv_rating_value <> 'NA' "+
							   "   and b.sv_product_id =  '"+prodid+"'"+
								 "   and b.sv_product_id = d.sv_product_id  "+
				         "   "+ingrsql+""+
					  		// " "+condi+""+
							   "   union "+
						     "   select "+fields+" x from sv_ratings a,avon_product_ingredients b,avon_product_type c,temp_product d  where "+
                 "   a.sv_body_attribute_id = '55' "+ agesql +" "+
						     "   and a.sv_rating_value > '0' and SV_RATING_VALUE <> ':n'  and sv_rating_value <> 'NA' "+
							   "   and b.sv_product_id =  '"+prodid+"'"+
								 "   and b.sv_product_id = d.sv_product_id  "+
				         "   "+ingrsql+""+
								 //" "+condi +""+
								 "  ) order by x  " ;

	//		//logger.log(Level.DEBUG,sql);
			return sql ;

	}

  public String getHairManualRatings(String hairtype,ArrayList hairprocess,String hairthickness,String hairtexture,ArrayList hairconditions,ArrayList hairstyles)
  {
    String hairtyp = "" ;
    String hairthick = "" ;
    String hairtext = "" ;
    String haircond    = "" ;
	String hairproc = "" ;
	String hairstyl = "" ;

    hairtyp = " and sv_body_attribute_value = '"+hairtype+"'" ;
    hairthick = " and sv_body_attribute_value = '"+hairthickness+"'" ;
    hairtext = " and sv_body_attribute_value = '"+hairtexture+"'" ;

	for(int i = 0 ; i < hairconditions.size() ; i++)
	{
		if(i==0)
				 haircond = " and ( sv_body_attribute_value = '"+hairconditions.get(i)+"'" ;
		else
				 haircond = haircond + "  or sv_body_attribute_value = '"+hairconditions.get(i)+"' " ;
		if(i == hairconditions.size()-1 )
				 haircond = haircond + ")" ;

	}
	for(int i = 0 ; i < hairprocess.size() ; i++)
	{
	   if(i==0)
			 hairproc = " and ( sv_body_attribute_value = '"+hairprocess.get(i)+"'" ;
	   else
			 hairproc = hairproc + "  or sv_body_attribute_value = '"+hairprocess.get(i)+"' " ;
	   if(i == hairprocess.size()-1 )
			 hairproc = hairproc + ")" ;

    }
	for(int i = 0 ; i < hairstyles.size() ; i++)
	{
	   if(i==0)
			 hairstyl = " and ( sv_body_attribute_value = '"+hairstyles.get(i)+"'" ;
	   else
	   	     hairstyl = hairstyl + "  or sv_body_attribute_value = '"+hairstyles.get(i)+"' " ;
	   if(i == hairstyles.size()-1 )
	   	     hairstyl = hairstyl + ")" ;

    }
			sql = "   select *  from sv_hair_ratings where "+
				  "   sv_body_attribute_id = '72' " + hairtyp +" "+
                  "   and sv_product_element_id = '10'   "         +
				  "   union "+
      			  "   select *  from sv_hair_ratings where "+
				  "   sv_body_attribute_id = '74' " + hairthick +" "+
			      "   and sv_product_element_id = '10'   "         +
				  "   union "+
      			  "   select *  from sv_hair_ratings where "+
				  "   sv_body_attribute_id = '75' " + hairtext +" "+
			      "   and sv_product_element_id = '10'   "         +
				  "   union "+
                  "   select *  from sv_hair_ratings where "+
                  "   sv_body_attribute_id = '73' "+ hairproc +" "+
				  "   union "+
				  "   select *  from sv_hair_ratings where "+
                  "   sv_body_attribute_id = '76'  "+ hairstyl +" "+
			      "   union "+
			      "   select *  from sv_hair_ratings where "+
                  "   sv_body_attribute_id = '77'  "+ haircond +" "+
			      "  " ;

	    System.out.println(sql) ;
		return sql ;

 }

  
  //Exception rules - Shivam - 24/02/2025 - Start
  public String ExceptionRuleProducts() {
	  return "select erm.SELECTED_SKIN_CONCERNS, erp.SV_PRODUCT_ID from EXCEPTION_RULE_PRODUCTS erp left join EXCEPTION_RULE_MASTER erm on erp.RULE_ID = erm.RULE_ID";
  }
  //Exception rules - Shivam - 24/02/2025 - End

}
