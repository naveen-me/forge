package com.svb.struts.DAL;
import com.svb.common.dba.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import javax.naming.InitialContext;
import javax.sql.DataSource;

public class IngredientDetailsDB {
	
	String query =  "SELECT S1.SV_INGREDIENT_NAME, "+
					"S2.SV_INGREDIENT_GLOSSORY_TEXT "+
					"FROM INGREDIENTS S1, "+
					"INGREDIENTS_GLOSSORY S2 "+
					"WHERE S1.SV_INGREDIENT_ID=S2.SV_INGREDIENT_ID AND UPPER(S1.SV_INGREDIENT_NAME) LIKE '";
	
	String query1 = "SELECT SV_INGREDIENT_NAME FROM INGREDIENTS";
	
	SVDBConnection svConnection;
	
	public HashMap getIngredientDetails(String link, String search) {

		HashMap map = new HashMap();

		if(link == null && search != null){
			query = query + "%" + search.toUpperCase() + "%'";
		}else if(link != null && search == null){
			query = query + link.toUpperCase() + "%'";
		}else if(link != null && search != null){
			query = query + "A%'";
		}else if(link == null && search == null){
			query = query + "A%'";
		}
		
		svConnection = new SVDBConnection();
 
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		
		try {
			
			connection = svConnection.getConnection();
			stmt = connection.createStatement();
			rs = stmt.executeQuery(query);

			while(rs.next()){
				map.put(rs.getString(1), rs.getString(2));
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
					rs.close();
					stmt.close();
					svConnection.free(connection);
			}catch(SQLException se){
				se.printStackTrace();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return map;
	}
	
	public ArrayList getIngredients() {
		
		svConnection = new SVDBConnection();
		ArrayList list = new ArrayList();
		
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			connection = svConnection.getConnection();
			stmt = connection.createStatement();
			rs = stmt.executeQuery(query1);

			while(rs.next()){
				list.add(rs.getString(1));
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
		return list;
	}
}
