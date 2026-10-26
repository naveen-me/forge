/*
 * Created on Sep 6, 2007
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.common.javabean;

/**
 * @author Rao
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
 import com.svb.common.dba.* ;

 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.* ;
 import java.io.* ;

 public class BaseRatings {

  public Connection ConnectToServerDB()throws SQLException, FileNotFoundException, IOException 
  {
    	
       Statement stmt = null;
       Connection con=null;
       try
       {
          String drivername = "oracle.jdbc.driver.OracleDriver";
          // String providerurl ="jdbc:oracle:thin:@127.0.0.1:1521:VBDB2"; 172.20.9.160:1521:svbeauty
          //prod   String providerurl ="jdbc:oracle:thin:@172.20.9.160:1521:svbeauty"; //prod 
          String providerurl ="jdbc:oracle:thin:@172.20.9.20:1521:svtest"; //stage
          Class.forName(drivername);
          con = DriverManager.getConnection(providerurl,"svb1","svb1");
          System.out.println("conn obj------------->"+con);
          System.out.println("Connected to Remote DataBase");
          stmt=con.createStatement();
       }
       catch(Exception e)
       {
         String message = e.getMessage();
         System.out.println("message in connection 18"+message);
       }
       return con;
  }
  public ArrayList getProfAttribs()
  {
  	ArrayList pattribs = new ArrayList() ;
  	pattribs.add("T001") ;
  	pattribs.add("T002") ;
  	pattribs.add("T003") ;
  	pattribs.add("T004") ;
  	pattribs.add("D001") ;
  	pattribs.add("D003") ;
  	pattribs.add("D006") ;
  	pattribs.add("D008") ;
  	pattribs.add("D002") ;
  	pattribs.add("C999") ;
  	pattribs.add("C022") ;
  	pattribs.add("C003") ;
  	pattribs.add("C020") ;
  	pattribs.add("C004") ;
  	pattribs.add("C023") ;
  	pattribs.add("C012") ;
  	pattribs.add("C019") ;
  	pattribs.add("C007") ;
  	pattribs.add("E001") ;
  	pattribs.add("C002") ;
  	pattribs.add("C001") ;
  	pattribs.add("E004") ;
  	return pattribs ;
  }
  public ArrayList getProfBases()
  {
  	ArrayList pbases = new ArrayList() ;
  	pbases.add("0001") ;
  	pbases.add("0002") ;
 	pbases.add("0003") ;
 	pbases.add("0004") ;
 	pbases.add("0005") ;
 	pbases.add("0006") ;
 	pbases.add("0007") ;
 	pbases.add("0008") ;
 	pbases.add("0010") ;
 	pbases.add("0011") ;
 	pbases.add("0012") ;
 	pbases.add("0013") ;
 	pbases.add("0014") ;
 	pbases.add("0016") ;
 	pbases.add("0019") ;
 	pbases.add("0020") ;
 	pbases.add("0022") ;
 	pbases.add("0024") ;
  	return pbases ;
  }
  public void doBaseProcess(String pmtype)
     {
	    SVDBOperation dbo = new SVDBOperation() ;
	    String[][] rs = new String[0][0] ;
	    Hashtable bases = new Hashtable() ;
	    Hashtable attribs = new Hashtable() ;
	    Hashtable svratings = new Hashtable() ;
	    ArrayList pattribs = new ArrayList() ;
	    ArrayList pbases = new ArrayList() ;
	    
	    try
	    {
		  bases   = (Hashtable) getProductBase() ;
		  attribs = (Hashtable) getProfileAttributes() ;
		  pattribs = (ArrayList) getProfAttribs() ;
		  pbases = (ArrayList) getProfBases() ;
		  
		  dbo.createConnectionStatic() ;
		  String sql = " select sv_product_type_master_id, sv_product_element_value,  sv_body_attribute_value, sv_rating_value "+
                       " from sv_ratings "+ 
                       " where sv_product_element_id = '01' "+
					   " and sv_product_type_master_id  = '"+pmtype+"'" +
                       " and sv_body_attribute_value in "+ 
                       "  (select sv_profile_attribute_id from sv_profile_attribute_details) "+
                       " order by sv_product_type_master_id, sv_product_element_value, sv_body_attribute_id, sv_body_attribute_value " ;
		  rs = dbo.executeQuery(sql) ;
	      //	System.out.println(rs.length) ;
		  String prodelement = "" ;
		  String bodyattribute = "" ;
		  String rating = "" ;
		
		  for (int i=0; i < rs.length ; i++)
		  {
			   prodelement      = rs[i][1].trim() ;
			   bodyattribute    = rs[i][2].trim() ;
			   rating           = rs[i][3].trim() ;
			   svratings.put(prodelement+bodyattribute,rating) ;
          }
		  for (int i=0; i < pbases.size(); i++)
		  {
		  	String pbase = (String) pbases.get(i) ;
		  	System.out.print(bases.get(pbase)) ;
		  	for (int j=0; j < pattribs.size(); j++)
		  	{
		  		String pattrib = (String) pattribs.get(j) ;
		  		if (svratings.get(pbase+pattrib) != null)
		  		{
		  			System.out.print("\t"+svratings.get(pbase+pattrib) ) ;
		  		}
		  		else
		  		{
		  			System.out.print("\t"+"NV" ) ;
		  		}
		  	}
		  	System.out.println("") ;
		  }
//        System.out.println("vb mm "+rownum+"\t"+ingrid+"\t"+pbsid+"\t"+ingrname+"\t"+funcname+"\t"+rowref+"\t"+rating_na+"\t"+dry+"\t"+normal+"\t"+combo+"\t"+oily) ;
            
	}catch (Exception e)
	{
		System.out.println(e) ;
	}
	finally
	{
		dbo.closeConnection() ;
	}
  }
  public Hashtable getProductBase()
  {
 	Hashtable base = new Hashtable() ;
	SVDBOperation dbo = new SVDBOperation() ;
	String[][] rs = new String[0][0] ;
	try
	{
		dbo.createConnectionStatic() ;
		String sql = "select * from avon_product_base  order by sv_product_base_id " ;
		rs = dbo.executeQuery(sql) ;
	//	System.out.println(rs.length) ;
		String baseid = "" ;
		String basename = "" ;
		for (int i=0; i < rs.length ; i++)
		{
			   baseid   = rs[i][0].trim() ;
			   basename = rs[i][1].trim() ;
			   base.put(baseid,basename) ;
	    }	
	}catch (Exception e)
	{
		System.out.println(e) ;
	}
	finally
	{
		dbo.closeConnection() ;
	}
	return base ;
  }
  public Hashtable getProfileAttributes()
  {
 	Hashtable prof = new Hashtable() ;
	SVDBOperation dbo = new SVDBOperation() ;
	String[][] rs = new String[0][0] ;
	try
	{
		dbo.createConnectionStatic() ;
		String sql = " select sv_profile_attribute_id, sv_profile_attribute_value "+
                     " from sv_profile_attribute_details "+ 
                     " where sv_profile_module_id = '01' and sv_profile_code < '55' "+ 
                     " order by sv_profile_code "  ;
		rs = dbo.executeQuery(sql) ;
	//	System.out.println(rs.length) ;
		String profid = "" ;
		String profname = "" ;
		for (int i=0; i < rs.length ; i++)
		{
			   profid   = rs[i][0].trim() ;
			   profname = rs[i][1].trim() ;
			   prof.put(profid,profname) ;
	    }	
	}catch (Exception e)
	{
		System.out.println(e) ;
	}
	finally
	{
		dbo.closeConnection() ;
	}
	return prof ;
  }
  public static void main(String[] args) {
	
	BaseRatings igl = new BaseRatings() ;
    // igl.doIngrProcess() ;
    try
	{
	  PrintStream out1 = new PrintStream(new FileOutputStream("g:\\avon_2006\\beauty.com\\masterdata\\NoBaseScores_020208.txt"));
      System.setOut(out1);
	}catch (Exception e)
	{
		System.out.println(e) ;
	}
   // igl.doBaseProcess("0001") ;
//    igl.doBaseProcess("0003") ;
//    igl.doBaseProcess("0004") ;
    igl.doBaseProcess("0005") ;
  }
 }
