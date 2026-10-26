package com.svb.struts.DAL;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.svb.common.dba.SVDBConnection;
import com.svb.struts.bean.BrandDetailsBean;

public class BrandDetailsDB {
	String query;
	SVDBConnection svConnection;
	BrandDetailsBean bdb;
	String[] brandId = new String[100];
	
	public String[] getBrandID(){
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		svConnection = new SVDBConnection();
		
		query= "select SV_BRAND_ID from SV_BRAND_NAMES ";
		System.out.println("query is:"+query);
		
		try{
		connection = svConnection.getConnection();
		System.out.println("connection:"+connection);
		stmt = connection.createStatement();
		rs = stmt.executeQuery(query);
	
		int i=0;
		while(rs.next()){
			
			System.out.println("brandid:"+rs.getString(1));
			
			brandId[i]= rs.getString(1);
			//System.out.println("brandid from rs:"+brandId[i]);
			i++;
			
			
		
		
		}
		
		}
		catch(SQLException e){}
		catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				rs.close();
				stmt.close();
				connection.close();
				
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return brandId;
		
	}
	
	
	
	public BrandDetailsBean getBrandList(String brandId){

		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		svConnection = new SVDBConnection();
		query = "SELECT SV_BRAND_ID, "+
				"SV_BRAND_NAME, "+ 
				"SV_BRAND_SUB_ID, "+
				"SV_BRAND_SUB_NAME, "+
				"SV_BRAND_VENDOR_ID, "+
				"SV_BRAND_VENDOR_NAME "+
				"FROM SV_BRAND_NAMES "+
				"WHERE SV_BRAND_ID="+brandId;
		bdb = new BrandDetailsBean();
		
		System.out.println(brandId);
	
			try {
				connection = svConnection.getConnection();
				stmt = connection.createStatement();
				rs = stmt.executeQuery(query);
				while(rs.next()){
//					System.out.println("brandid:"+rs.getString("SV_BRAND_ID"));
					bdb.setSV_BRAND_NAME(rs.getString("SV_BRAND_ID"));
//					System.out.println("brandname:"+rs.getString("SV_BRAND_NAME"));			
					bdb.setSV_BRAND_NAME(rs.getString("SV_BRAND_NAME"));
					bdb.setSV_BRAND_SUB_ID(rs.getString("SV_BRAND_SUB_ID"));
					bdb.setSV_BRAND_SUB_NAME(rs.getString("SV_BRAND_SUB_NAME"));
					bdb.setSV_BRAND_VENDOR_ID(rs.getString("SV_BRAND_VENDOR_ID"));
					bdb.setSV_BRAND_VENDOR_NAME(rs.getString("SV_BRAND_VENDOR_NAME"));
					
				}
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				try{
					rs.close();
					stmt.close();
					connection.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
		return bdb;
	}
	
	public ArrayList getBrandList(){
		svConnection = new SVDBConnection();
		bdb = new BrandDetailsBean();
		ArrayList  brands= new ArrayList();
		
		query="select a.SV_BRAND_ID,a.SV_BRAND_NAME,a.SV_BRAND_SUB_ID, a.SV_BRAND_SUB_NAME,a.SV_BRAND_VENDOR_ID,a.SV_BRAND_VENDOR_NAME ,b.SV_BRAND_DESCRIPTION,b.SV_BRAND_LOGO_FILE from SV_BRAND_NAMES a,SV_BRAND_DESCRIPTION b where a.SV_BRAND_ID=b.SV_BRAND_ID";
		
		
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try{
			connection = svConnection.getConnection();
			
			System.out.println("connection in BrandDetialDB:"+connection);
			stmt = connection.createStatement();
			rs = stmt.executeQuery(query);
			
			
			while (rs.next()){
				bdb = new BrandDetailsBean();
			//System.out.println("brandid:"+rs.getString("SV_BRAND_ID"));
			bdb.setSV_BRAND_ID(rs.getString("SV_BRAND_ID"));
//			System.out.println("brandname:"+rs.getString("SV_BRAND_NAME"));			
			bdb.setSV_BRAND_NAME(rs.getString("SV_BRAND_NAME"));
			
			bdb.setSV_BRAND_SUB_ID(rs.getString("SV_BRAND_SUB_ID"));
			bdb.setSV_BRAND_SUB_NAME(rs.getString("SV_BRAND_SUB_NAME"));
			bdb.setSV_BRAND_VENDOR_ID(rs.getString("SV_BRAND_VENDOR_ID"));
			bdb.setSV_BRAND_VENDOR_NAME(rs.getString("SV_BRAND_VENDOR_NAME"));
			bdb.setSV_BRAND_DESCRIPTION(rs.getString("SV_BRAND_DESCRIPTION"));
			System.out.println("branddiscription:"+rs.getString("SV_BRAND_DESCRIPTION"));
			
			bdb.setSV_BRAND_LOGO_FILE(rs.getString("SV_BRAND_LOGO_FILE"));
			
			brands.add(bdb);
			}
			
			
		}catch(Exception e){
				e.printStackTrace();
			}
		finally{
			try{
				rs.close();
				stmt.close();
				svConnection.free(connection);
			}catch(Exception e){
				e.printStackTrace();
			}
		}			
		return brands;
			
			
	}
	
	public ArrayList getBrandDisc(String brandid){
		svConnection = new SVDBConnection();
		bdb = new BrandDetailsBean();
		ArrayList  brands= new ArrayList();
		
		query="select * from SV_BRAND_DESCRIPTION WHERE SV_BRAND_ID="+brandid;
		
		System.out.println("query in branddiscription:"+query);
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try{
			connection = svConnection.getConnection();
			
			System.out.println("connection in BrandDetialDB:"+connection);
			stmt = connection.createStatement();
			rs = stmt.executeQuery(query);
			
			
			if (rs.next()){
				bdb = new BrandDetailsBean();
			
			String longdis=rs.getString("SV_BRAND_DESCRIPTION");
			if(!(longdis!=(null)))
				longdis=" ";
            longdis = (String)preProcess(longdis) ;
			bdb.setSV_BRAND_DESCRIPTION(longdis);
			bdb.setSV_BRAND_LOGO_FILE(rs.getString("SV_BRAND_LOGO_FILE"));
			
			
			brands.add(bdb);
			}
			else{
				
				System.out.println("there is no branddiscription of brandid:"+brandid);
			}
			
			
		}catch(Exception e){
				e.printStackTrace();
			}
		finally{
			try{
				rs.close();
				stmt.close();
				svConnection.free(connection);
			}catch(Exception e){
				e.printStackTrace();
			}
		}			
		return brands;
			
		
		
	}
	public String preProcess(String newString){
		if (newString != null && newString.length() > 0)
		{
    	 newString = newString.replaceAll("&","&amp;");
    	 newString = newString.replaceAll("&amp;nbsp;", "&nbsp;");
    	 newString = newString.replaceAll("&amp; 153;", "™");
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
    	 newString = newString.replaceAll("¿","");
		}
		//System.out.println("new string "+newString) ;
    	return newString;
    }
	
}
