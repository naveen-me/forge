package com.svb.struts.DAL;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import com.svb.common.dba.SVDBConnection;
import com.svb.struts.bean.RegimenDetailsBean;

public class RegimenDetailsDetailsDB {
	
	String query;
	SVDBConnection svConnection;
	RegimenDetailsBean rdb;
	String brandId;
	
	public RegimenDetailsBean getRegimenDetails(String productId){

		svConnection = new SVDBConnection();
		query = "SELECT SV_PRODUCT_IMAGE_MEDIUM, "+
				"SV_PRODUCT_BRAND_ID, "+ 
				"SV_PRODUCT_NAME, "+
				"SV_PRODUCT_OUR_PRICE, "+
				"SV_PRODUCT_SELL_COPY, "+
				"SV_PRODUCT_DESCRIPTION "+
				"FROM SV_PRODUCTS "+
				"WHERE SV_PRODUCT_ID="+productId;
		rdb = new RegimenDetailsBean();
		
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		Statement stmt1 = null;
		ResultSet rs1 = null;
		
		System.out.println(productId);
		try{
			try {
				connection = svConnection.getConnection();
				stmt = connection.createStatement();
				rs = stmt.executeQuery(query);
				while(rs.next()){
					brandId = rs.getString("SV_PRODUCT_BRAND_ID"); 
					rdb.setProductId(""+productId);
					rdb.setDescription(rs.getString("SV_PRODUCT_DESCRIPTION"));
					rdb.setProductName(rs.getString("SV_PRODUCT_NAME"));
					rdb.setSellCopy(rs.getString("SV_PRODUCT_SELL_COPY"));
					rdb.setImageMedium(rs.getString("SV_PRODUCT_IMAGE_MEDIUM"));
					String price = rs.getString("SV_PRODUCT_OUR_PRICE");
					rdb.setPrice(Float.parseFloat(price.substring(1,price.length())));
				}
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				try{
					rs.close();
					stmt.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			try{
				stmt1 = connection.createStatement();
				rs1 = stmt1.executeQuery("SELECT SV_PRODUCT_BRAND_NAME FROM SV_PRODUCT_BRANDS WHERE SV_PRODUCT_BRAND_ID="+brandId);
				while(rs1.next()){
					rdb.setBrandName(rs1.getString("SV_PRODUCT_BRAND_NAME"));
				}
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				try{
					rs1.close();
					stmt1.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				svConnection.free(connection);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return rdb;
	}
	
}
