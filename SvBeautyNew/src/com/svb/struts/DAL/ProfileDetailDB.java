package com.svb.struts.DAL;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import com.svb.common.dba.SVDBConnection;

import com.svb.struts.bean.ProfileDetailBean;

public class ProfileDetailDB {

	String query;
	SVDBConnection svConnection;
	ProfileDetailBean pdb;
	
	public ArrayList getProfileList(){
		svConnection = new SVDBConnection();
		pdb = new ProfileDetailBean();
		ArrayList  profiles= new ArrayList();
		
		query="select SV_PROFILE_ATTRIBUTE_VALUE from SV_PROFILE_ATTRIBUTE_DETAILS";
		
		
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try{
			connection = svConnection.getConnection();
			
			System.out.println("connection in ProfileDetialDB:"+connection);
			stmt = connection.createStatement();
			rs = stmt.executeQuery(query);
			
			
			while (rs.next()){
			pdb = new ProfileDetailBean();
			//System.out.println("brandid:"+rs.getString("SV_BRAND_ID"));
			pdb.setSV_PROFILE_ATTRIBUTE_VALUE(rs.getString("SV_PROFILE_ATTRIBUTE_VALUE"));
//						
			profiles.add(pdb);
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
			
		return profiles;
			
			
	}
	
	public ArrayList getProfileDisc(String profilevalue){
		svConnection = new SVDBConnection();
		pdb = new ProfileDetailBean();
		ArrayList  profiles= new ArrayList();
		
		query="select SV_PROFILE_ATTRIBUTE_DESC from SV_PROFILE_ATTRIBUTE_DETAILS WHERE SV_PROFILE_ATTRIBUTE_ID='"+profilevalue+"'";
		
		System.out.println("query in profilediscription:"+query);
		
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try{
			connection = svConnection.getConnection();
			System.out.println("connection in ProfileDetialDB:"+connection);
			stmt = connection.createStatement();
			rs = stmt.executeQuery(query);
			
			if (rs.next())
			{
				
			pdb = new ProfileDetailBean();
			pdb.setSV_PROFILE_ATTRIBUTE_DESC(rs.getString("SV_PROFILE_ATTRIBUTE_DESC"));
			System.out.println("branddiscription:"+rs.getString("SV_PROFILE_ATTRIBUTE_DESC"));
			profiles.add(pdb);
			}
			else{
				
				System.out.println("there is no profilediscription of brandid:"+profilevalue);
			}
		
		}catch(Exception e)
		{
		
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
		return profiles;
		
	}
}
