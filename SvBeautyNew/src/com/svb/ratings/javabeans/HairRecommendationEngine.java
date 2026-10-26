package com.svb.ratings.javabeans;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;


import com.svb.common.ConstantDeclarations;
import com.svb.common.dba.SVBeautydba;
import com.svb.common.dba.SVDBOperation;
import com.svb.common.valueobjects.AttribRatingVO;
import com.svb.common.valueobjects.BrandVO;
import com.svb.common.valueobjects.CustomerProfileVO;
import com.svb.common.valueobjects.ProductVO;
import com.svb.customerprofile.dal.CustomerProfileDAL;

/*
 * // header - edit "Data/yourJavaHeader" to customize // contents - edit
 * "EventHandlers/Java file/onCreate" to customize //
 */
public class HairRecommendationEngine {
	SVBeautydba dba = new SVBeautydba();
	SVDBOperation dbo = new SVDBOperation();

	boolean externalDBO = false;
	boolean noingredients = false;


	public void initialize() throws Exception {
		dba = new SVBeautydba();
		dbo = new SVDBOperation();
		dbo.createConnection();

	}

	public void setDBO(SVDBOperation dbo){
		this.dbo 		 = dbo;
		this.externalDBO = true;
	}

	public void destroy(){
		dbo.closeConnection();
	}

	public Hashtable getProdDetails(String prodtype, String prodselect, String prodline,CustomerProfileVO cp) {
		String[][]  result        = null;
		String[][]  result1        = null;
		ProductVO[] productVOList = new ProductVO[0];
		Hashtable   prodhash      = new Hashtable();
		boolean prodflag = false ;
		boolean prodidsflag = false ;
		boolean clearskinflag = false ;
		
		String cAge = cp.getAge();
		String cHairtype = cp.getSkinType();
		String cNatural = cp.getNatural() ;
		String cEthnicity = cp.getEthnicity() ;
		String cHaircolor = cp.getHaircolor();
		String cHairthickness = cp.getHairthickness();
		String cHairtexture = cp.getHairtexture();
		String cHairfinish = cp.getHairfinish();
        		
		ArrayList custHairprocess = cp.getHairprocess();
		ArrayList custHairstyles = cp.getHairstyles();
		ArrayList custHairconditions = cp.getHairconditions();
		
		boolean dryflag = false;
		boolean normalflag = false;
		boolean oilyflag = false;
		boolean under25flag = false ;
		boolean age25flag = false ;
		boolean age36flag = false ;
		boolean age46flag = false ;
		boolean age56flag = false ;
		boolean age66flag = false ;
		boolean age75flag = false ;
        boolean naturalflag = false ;
        boolean asianflag = false ;
        boolean africanamericanflag = false ;
        boolean permflag = false ;
        boolean dandurfflag = false ;
        boolean oilyscalpflag = false ;
        boolean limpflag = false ;
        boolean splitendsflag = false ;
        boolean verythinflag = false ;
        boolean fineflag = false ;
        boolean redflag = false ;
        boolean brunetteflag = false ;
        boolean blondeflag = false ;
        boolean greyflag = false ;
        boolean hairlossflag = false ;
        boolean sundamageflag = false ;
        boolean S003flag = false ;
        boolean S004flag = false ;
        boolean S005flag = false ;
        boolean S006flag = false ;
        boolean flyawayflag = false ;
        boolean colortreatedflag = false ;
        boolean colorfadedflag = false ;
        boolean dryhairflag = false ;
        boolean softnaturallookingflag = false;
        boolean otherflag = false ;
		
        if (cHairtype.equals("Y001"))
        	dryflag = true ;
        if (cHairtype.equals("Y003"))
        	normalflag = true ;
        if (cHairtype.equals("Y002"))
        	oilyflag = true ;
        if (cAge.equals("G004"))
        	under25flag = true ;
        if (cAge.equals("G001"))
        	age25flag = true ;
        if (cAge.equals("G002"))
        	age36flag = true ;
        if (cAge.equals("G003"))
        	age46flag = true ;
        if (cAge.equals("G005"))
        	age56flag = true ;
        if (cAge.equals("G006"))
        	age66flag = true ;
        if (cAge.equals("G007"))
        	age75flag = true ;
        
        if (cNatural.equals("A008"))
        	naturalflag = true ;
        
        if (cEthnicity.equals("N002"))
        	asianflag = true ;
        if (cEthnicity.equals("N001"))
        	africanamericanflag = true ;
        
        if (cHairthickness.equals("I005"))
        	verythinflag = true ;
        
        if (cHairtexture.equals("X004"))
        	fineflag = true ;
        
        if (cHaircolor.equals("O005"))
        	greyflag = true ;
        if (cHaircolor.equals("O001"))
        	blondeflag = true ;
        if (cHaircolor.equals("O002"))
        	brunetteflag = true ;
        if (cHaircolor.equals("O003"))
        	redflag = true ;
        if (cHaircolor.equals("O099"))
        	otherflag = true ;
        
		for (int m = 0; m < custHairprocess.size(); m++) {
			if (custHairprocess.get(m).equals("H002"))
				permflag = true;
			if (custHairprocess.get(m).equals("H001"))
				colortreatedflag = true;
		}
        //System.out.println("dce flag "+dceflag) ;
		for (int m = 0; m < custHairstyles.size(); m++) {
			if (custHairstyles.get(m).equals("S003"))
				S003flag = true;
			if (custHairstyles.get(m).equals("S004"))
				S004flag = true;
			if (custHairstyles.get(m).equals("S006"))
				S006flag = true;
			if (custHairstyles.get(m).equals("S005"))
				S005flag = true;
		}
		
		for (int m = 0; m < custHairconditions.size(); m++) {
			if (custHairconditions.get(m).equals("R002"))
				limpflag = true;
			if (custHairconditions.get(m).equals("R007"))
				dandurfflag = true;
			if (custHairconditions.get(m).equals("R009"))
				oilyscalpflag = true;
			if (custHairconditions.get(m).equals("R012"))
				hairlossflag = true;
			if (custHairconditions.get(m).equals("R005"))
				splitendsflag = true;
			if (custHairconditions.get(m).equals("R011"))
				sundamageflag = true;
			if (custHairconditions.get(m).equals("R006"))
				flyawayflag = true;
			if (custHairconditions.get(m).equals("R010"))
				colorfadedflag = true;
			if (custHairconditions.get(m).equals("R001"))
				dryhairflag = true;
		}
		try {
			    dbo = new SVDBOperation();
			    dbo.createConnection();
				if (prodselect !=null && prodselect.equals("prodLine"))
				{
				  result = dbo.executeQuery(dba.getHairProductIds(prodline,prodselect));
//				  result1 = dbo.executeQuery(dba.getHairProductIds("('1009')","prodType"));
				}
				else if (prodselect !=null && prodselect.equals("prodType"))
				    result = dbo.executeQuery(dba.getHairProductIds(prodtype,prodselect));
				else 
				    result = dbo.executeQuery(dba.getHairProductIds("",""));
			  } catch (Exception ex1) {
				ex1.printStackTrace() ;
			}
			finally {
				dbo.closeConnection() ;
			}
			
/*			if (prodselect !=null && prodselect.equals("prodLine"))
			{
				for (int i=0; i < result1.length ; i++)
				{
					ProductVO pr = new ProductVO();
					pr.setProdId(result1[i][0].trim());
					pr.setBase(result1[i][2]);
					pr.setProductLine(result1[i][3]);
					pr.setProdName(result1[i][4]);
					pr.setPrice(result1[i][5]);
					pr.setImage(result1[i][6]);
					pr.setSPFId(result1[i][7]) ;
					pr.setProdType(result1[i][8]) ;
					pr.setSellCopy(result1[i][9]) ;
					pr.setIngrScore("0");
					pr.setFunctionscore("0");
					pr.setBumpScore("0");
					pr.setTobeDeleted(false);
					String mcateg = "" ;
					mcateg = result1[i][1] ;
					pr.setMasterProdType(mcateg);
					prodhash.put(result1[i][0].trim(), pr);
				}
			}
*/			
			
		for (int i = 0; i < result.length; i++) {
         
		 if   (    result[i][1].equals("1001") || result[i][1].equals("1003") 
		 		|| result[i][1].equals("1005") || result[i][1].equals("1004")
				|| result[i][1].equals("1009")
			  )
		 {
  		   // System.out.println("0... "+result[i][0]+"  "+result[i][4]+" "+prodflag)  ;
			prodflag = true ;
		//	System.out.println("Natural "+cNatural+" "+naturalflag) ;
			if (naturalflag && (    result[i][3].equals("1015") 
                                 || result[i][3].equals("1028") 
                                 || result[i][3].equals("1052") 
                                 || result[i][3].equals("1055") 
		             			 || result[i][3].equals("1056") 
                                 || result[i][3].equals("1058") 
                                 || result[i][3].equals("1060") 
                                 || result[i][3].equals("1079") 
                                 || result[i][3].equals("1053") 
                                 || result[i][3].equals("1061") 
                                 || result[i][3].equals("1084") 
	                           ) 
                )
			{
		        prodflag = true ;
		        System.out.println("in natural if ") ;
			}
            else if (naturalflag)
            {
		        System.out.println("in natural else ") ;
                prodflag = false ;
            }
            if (   naturalflag &&  result[i][0].trim().equals("201449") 
                || naturalflag &&  result[i][0].trim().equals("201451")
               )
            {
            	prodflag = true ;
            }
         if (cHairfinish.equals("F999") && result[i][1].equals("1009"))
             prodflag = false ;
         if (cHairfinish.equals("F001") && result[i][1].equals("1009"))
            softnaturallookingflag = true ;
            
      	/* commenting finish code 

         if (prodflag)
         {
         	// System.out.println("vb finish "+cHairfinish+"  "+result[i][1]+"   "+result[i][0]+"  "+prodflag) ; ;
              if (cHairfinish.equals("F001") && result[i][1].equals("1009") && 
             		(  result[i][0].trim().equals("167148") 
             		 ||result[i][0].trim().equals("166536") 
             		 ||result[i][0].trim().equals("163760") 
					)
				)
                prodflag = true ;
              else if (cHairfinish.equals("F001") && result[i][1].equals("1009"))
              	prodflag = false ;
              
             if (cHairfinish.equals("F002") && result[i][1].equals("1009") && 
             		(  result[i][0].trim().equals("167141") 
             		 ||result[i][0].trim().equals("145592") 
             		 ||result[i][0].trim().equals("157416") 
             		 ||result[i][0].trim().equals("151351") 
             		 ||result[i][0].trim().equals("164649") 
             		 ||result[i][0].trim().equals("188986") 
             		 ||result[i][0].trim().equals("201743") 
             		 ||result[i][0].trim().equals("196565") 
             		 ||result[i][0].trim().equals("196552") 
             		 ||result[i][0].trim().equals("196546") 
					)
				)
                prodflag = true ;
              else if (cHairfinish.equals("F002") && result[i][1].equals("1009"))
              	prodflag = false ;
              
             if (cHairfinish.equals("F003") && result[i][1].equals("1009") && 
             		(  result[i][0].trim().equals("167143") 
             		 ||result[i][0].trim().equals("163753") 
             		 ||result[i][0].trim().equals("145254") 
             		 ||result[i][0].trim().equals("94337") 
             		 ||result[i][0].trim().equals("196544") 
             		 ||result[i][0].trim().equals("196562") 
             		 ||result[i][0].trim().equals("196545") 
					)
				)
             {
          //   	System.out.println("vb finish 3 if") ;
                prodflag = true ;
             }
              else if (cHairfinish.equals("F003") && result[i][1].equals("1009"))
              {
            // 	System.out.println("vb finish 3 else") ;
              	prodflag = false ;
              }
             if (cHairfinish.equals("F004") && result[i][1].equals("1009") && 
             		(  result[i][0].trim().equals("145591") 
             		 ||result[i][0].trim().equals("145593") 
             		 ||result[i][0].trim().equals("145255") 
					)
				)
                prodflag = true ;
              else if (cHairfinish.equals("F004") && result[i][1].equals("1009"))
              	prodflag = false ;

         }
         // end commenting
       */
//		  System.out.println("1...   "+result[i][4]+" "+prodflag)  ;
         if (prodflag)
         {
  			if (prodflag && !africanamericanflag && result[i][1].equals("1004") && result[i][8].equals("6015"))
  				prodflag = false ;
  			if (prodflag && !africanamericanflag && result[i][1].equals("1005") && result[i][8].equals("6009"))
  				prodflag = false ;
  			if (prodflag && !africanamericanflag && result[i][1].equals("1001") && result[i][8].equals("6001"))
  				prodflag = false ;
  			if (prodflag && !age46flag && result[i][1].equals("1003") && result[i][8].equals("6008"))
  				prodflag = false ;
  			if (prodflag && !age46flag && result[i][1].equals("1001") && result[i][8].equals("6006"))
  				prodflag = false ;
  			if (prodflag && !age56flag && result[i][1].equals("1005") && result[i][8].equals("6010"))
  				prodflag = false ;
  			if (prodflag && !dandurfflag && result[i][1].equals("1001") && result[i][8].equals("6002"))
  				prodflag = false ;
  			if (prodflag && !dandurfflag && result[i][1].equals("1005") && result[i][8].equals("6011"))
  				prodflag = false ;
  			if (prodflag && !fineflag && result[i][1].equals("1001") && result[i][8].equals("6003"))
  				prodflag = false ;
  			if (prodflag && !greyflag && result[i][1].equals("1001") && result[i][8].equals("6004"))
  				prodflag = false ;
  			if (prodflag && !hairlossflag && result[i][1].equals("1005") && result[i][8].equals("6012"))
  				prodflag = false ;
  			if (prodflag && !limpflag && result[i][1].equals("1005") && result[i][8].equals("6013"))
  				prodflag = false ;
  			if (prodflag && !oilyscalpflag && result[i][1].equals("1001") && result[i][8].equals("6005"))
  				prodflag = false ;
  			if (prodflag && !permflag && result[i][1].equals("1001") && result[i][8].equals("6006"))
  				prodflag = false ;
  			if (prodflag && !verythinflag && result[i][1].equals("1005") && result[i][8].equals("6014"))
  				prodflag = false ;
  			if (prodflag && !splitendsflag && result[i][1].equals("1003") && result[i][8].equals("6007"))
  				prodflag = false ;
  			if (prodflag && !sundamageflag && result[i][1].equals("1005") && result[i][8].equals("6016"))
  				prodflag = false ;
  			if (prodflag && !sundamageflag && result[i][1].equals("1001") && result[i][8].equals("6017"))
  				prodflag = false ;
  			if (prodflag && !sundamageflag && result[i][1].equals("1004") && result[i][8].equals("6018"))
  				prodflag = false ;
  			if (prodflag && !age46flag && result[i][1].equals("1003") && result[i][8].equals("6019"))
  				prodflag = false ;

  			if (prodflag && !brunetteflag && result[i][1].equals("1003") && result[i][8].equals("6020"))
  				prodflag = false ;
  			if (prodflag && !blondeflag && result[i][1].equals("1003") && result[i][8].equals("6021"))
  				prodflag = false ;
  			if (prodflag && !redflag && result[i][1].equals("1003") && result[i][8].equals("6022"))
  				prodflag = false ;
  			if (prodflag && !S003flag && result[i][1].equals("1004") && result[i][8].equals("6023"))
  				prodflag = false ;
  			if (prodflag && !S004flag && result[i][1].equals("1004") && result[i][8].equals("6024"))
  				prodflag = false ;
  			if (prodflag && !age36flag && result[i][1].equals("1005") && result[i][8].equals("6025"))
  				prodflag = false ;
  			if (prodflag && !age46flag && result[i][1].equals("1001") && result[i][8].equals("6026"))
  				prodflag = false ;
  			if (prodflag && !S006flag && result[i][1].equals("1001") && result[i][8].equals("6027"))
  				prodflag = false ;
  			if (prodflag && !S006flag && result[i][1].equals("1002") && result[i][8].equals("6028"))
  				prodflag = false ;
  			if (prodflag && !flyawayflag && result[i][1].equals("1005") && result[i][8].equals("6029"))
  				prodflag = false ;

  			if (prodflag && !colortreatedflag && result[i][1].equals("1001") && result[i][8].equals("6030"))
  				prodflag = false ;
  			if (prodflag && !colortreatedflag && result[i][1].equals("1003") && result[i][8].equals("6031"))
  				prodflag = false ;
  			if (prodflag && !colortreatedflag && result[i][1].equals("1004") && result[i][8].equals("6032"))
  				prodflag = false ;
  			if (prodflag && !colortreatedflag && result[i][1].equals("1005") && result[i][8].equals("6033"))
  				prodflag = false ;
  			
  			if (prodflag && !hairlossflag && result[i][1].equals("1001") && result[i][8].equals("6034"))
  				prodflag = false ;
  			if (prodflag && !hairlossflag && result[i][1].equals("1003") && result[i][8].equals("6035"))
  				prodflag = false ;
  			if (prodflag && !hairlossflag && result[i][1].equals("1004") && result[i][8].equals("6036"))
  				prodflag = false ;
  			if (prodflag && !blondeflag && result[i][1].equals("1001") && result[i][8].equals("6037"))
  				prodflag = false ;
  			
  			if (prodflag && !age46flag && result[i][1].equals("1009") && result[i][8].equals("6039"))
  				prodflag = false ;
  			if (prodflag && !colorfadedflag && result[i][1].equals("1003") && result[i][8].equals("6040"))
  				prodflag = false ;
  			if (prodflag && !dryhairflag && result[i][1].equals("1005") && result[i][8].equals("6041"))
  				prodflag = false ;
  			if (prodflag && !softnaturallookingflag && result[i][1].equals("1004") && result[i][8].equals("6042"))
  				prodflag = false ;
  			if (prodflag && !S005flag && result[i][1].equals("1004") && result[i][8].equals("6043"))
  				prodflag = false ;

  			if (prodflag && !blondeflag && result[i][1].equals("1004") && result[i][8].equals("6044"))
  				prodflag = false ;
  			if (prodflag && !brunetteflag && result[i][1].equals("1004") && result[i][8].equals("6045"))
  				prodflag = false ;
  			if (prodflag && !redflag && result[i][1].equals("1004") && result[i][8].equals("6046"))
  				prodflag = false ;
  			
  			if (result[i][0].equals("228247") & (brunetteflag || otherflag))
  				prodflag = false ;
  			if (result[i][0].equals("228248") & (blondeflag || redflag || greyflag))
  				prodflag = false ;
         }
		 if(prodflag)
		 {
//		    //logger.log(Level.DEBUG,"prodflag begin") ;
			ProductVO pr = new ProductVO();
			pr.setProdId(result[i][0].trim());
			pr.setBase(result[i][2]);
			pr.setProductLine(result[i][3]);
			pr.setProdName(result[i][4]);
			pr.setPrice(result[i][5]);
			pr.setImage(result[i][6]);
			pr.setSPFId(result[i][7]) ;
			pr.setProdType(result[i][8]) ;
			pr.setSellCopy(result[i][9]) ;
			pr.setIngrScore("0");
			pr.setFunctionscore("0");
			pr.setBumpScore("0");
			pr.setTobeDeleted(false);
			String mcateg = "" ;
			String pcateg = "" ;
			pcateg = result[i][1] ;
			mcateg = result[i][1] ;
			pr.setMasterProdType(mcateg);

			prodhash.put(result[i][0].trim(), pr);
		 } // if prodflag
		} //if 
		} // for
        System.out.println("vbbbbbb hair "+prodhash.size()) ;
    //    Enumeration en  = prodhash.keys() ;
      //  while (en.hasMoreElements())
        //	System.out.println("kdsfskh "+en.nextElement());
		return prodhash;
	}

	public Hashtable getHairProductInfo(String prodids) {
		String[][]  result        = null;
        Hashtable prods = new Hashtable() ;
		try {
			  dbo = new SVDBOperation();
			  dbo.createConnection();
			  result = dbo.executeQuery(dba.getHairProductInfo(prodids));
		    } catch (Exception ex1) {
			   ex1.printStackTrace() ;
		   }
		   finally {
			dbo.closeConnection() ;
		  }
			
		for (int i = 0; i < result.length; i++) {
         
		 
//		    //logger.log(Level.DEBUG,"prodflag begin") ;
			ProductVO pr = new ProductVO();
			pr.setProdId(result[i][0].trim());
			pr.setProdName(result[i][1]);
			pr.setPrice(result[i][2]);
			pr.setImage(result[i][3]);
			pr.setProductLinePrime(result[i][4]) ;
			pr.setSPFId(result[i][5]) ;
			pr.setMasterProdType(result[i][6]) ;
			pr.setBase(result[i][7]) ;
			pr.setProdType(result[i][8]) ;
			pr.setSellCopy(result[i][9]) ;
			if (result[i][9] != null && result[i][9].trim().length() == 0)
				pr.setSellCopy(" ") ;
			pr.setShortDesc(result[i][10]) ;
			prods.put(result[i][0].trim(), pr) ;
		 }

		return prods;
	}

	protected ProductVO getProductIngredients(ArrayList prodingredients,ProductVO tmpproduct) {
		String tmpprodingredients[][] = new String[prodingredients.size()][2];
		for (int j = 0; j < prodingredients.size(); j++) {
			StringTokenizer stk = new StringTokenizer((String) prodingredients
					.get(j), "-");
			while (stk.hasMoreTokens()) {

				String ingrid = stk.nextToken();
				String conc = stk.nextToken();
				if (conc == null || conc.trim().length() == 0
						|| conc.equals("null")) //if the product concentration
												// is not entered default it to
												// NA
					conc = "NA";
				tmpprodingredients[j][0] = ingrid;
				tmpprodingredients[j][1] = conc;
			}

		}
//		System.out.println("csdhfhks "+tmpproduct.getProdId()) ;
		tmpproduct.setIngredients(tmpprodingredients);
		return tmpproduct;

	}


	//this method eliminates the products that r need to be eliminated based on
	// the ingredients that got eliminated earlier

	//this methos gets the ingredients present in the products
	public Hashtable getProductsIngredients(String prodtype, String prodselect,
			String prodline,Hashtable prodhash) {
		String result[][]  = null;
		ArrayList ingrprodvect = new ArrayList();
		String prodid = "begin";
		ArrayList prodingredients = new ArrayList();
		boolean prodflag = false ;
		boolean prodidsflag = false ;
		boolean clearskinflag = false ;
        int iflag = 0 ;
//       System.out.println("dghhhhhh  prodhash size vb "+prodhash.size()) ;
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			
			if (prodselect !=null && prodselect.equals("prodLine"))
				result = dbo.executeQuery(dba.getHairProductsIngredientsLine(prodline,prodselect));
			else if (prodselect !=null && prodselect.equals("prodType"))
			    result = dbo.executeQuery(dba.getHairProductsIngredientsLine(prodtype,prodselect));
			else if (prodselect !=null && prodselect.equals("prodId"))
			    result = dbo.executeQuery(dba.getHairProductIngredients(prodid));
			else 
			    result = dbo.executeQuery(dba.getHairProductsIngredientsLine("",""));
			
		// analysis	result = dbo.executeQuery(dba.getProductsIngredients(prodselect));
		////logger.log(Level.DEBUG,"result ingr length "+result.length) ;
		//get the prodcuts along with ingredients
	//	System.out.println("vbbb result length "+result.length) ;
		for (int i = 0; i < result.length; i++) {

			
		   prodflag = true ;
           if(prodflag)
           {
			if (iflag != 0 && !prodid.equals(result[i][0].trim())) //if the oldprodid is
														// diff from the one we
														// r checking now ,
			{
	          //  System.out.println("dfgddfdg prodid 2 "+prodid) ;
	            if (prodhash.get(prodid) != null)
	            {
	        //      System.out.println("dfgddfdg prodid 3 "+((ProductVO)prodhash.get(prodid)).getProdId()) ;
				  prodhash.put(prodid, getProductIngredients(prodingredients, (ProductVO)prodhash.get(prodid)));
				  prodingredients = new ArrayList();
//	                System.out.println("proding 1"+prodid+"  "+result[i][1]) ;
				  prodingredients.add(result[i][1] + "-" + result[i][2]); //add
																		// the
																		// ingredient
																		// and
																		// concentration
																		// list
																		// to
																		// the
																		// productingredient
																		// ArrayList
	            }
			}
			else
			{
				if (prodhash.get(prodid) != null)
	            {
				 prodingredients.add(result[i][1] + "-" + result[i][2]); //add
  //               System.out.println("proding 2"+prodid+"  "+result[i][1]) ;
	            }														// the
																		// ingredient
																		// and
																		// concentration
																		// list
																		// to
																		// the
																		// productingredient
																		// ArrayList
			}
			prodid = result[i][0].trim();
           	iflag = iflag+1 ;
  //          System.out.println("dfgddfdg prodid 1 "+prodid) ;
		}
	  }
	  if (result.length > 0)
	  {
	  	 if (prodhash.get(prodid) != null)
			prodhash.put(prodid,getProductIngredients(prodingredients, (ProductVO)prodhash.get(prodid.trim())));
	  }

		if (result.length == 0)
			noingredients = true;
	} catch (Exception ex1) {
		ex1.printStackTrace() ;
	}
	finally {
		dbo.closeConnection() ;
	}
/*	Enumeration em = prodhash.keys() ;
	while (em.hasMoreElements())
	{
	//	System.out.println("vb2 11 "+em.nextElement()) ;
		String tt = (String) em.nextElement() ;
		if (tt.equals("76347"))
		{
			ProductVO tmpproduct = (ProductVO) prodhash.get(tt) ;
			String ingr[][] = tmpproduct.getIngredients();
			for (int i=0; i < ingr.length ; i++)
				System.out.println("vb prodingrs 555 "+tmpproduct.getProdId()+"  "+ingr[i][0]) ;
		} 
	}*/
		return prodhash;
	}

	public String[][] getCustomerProfileQuery(SVDBOperation dbo,SVBeautydba dba,String customid){
		return dbo.executeQuery(dba.getCustomerProfileHair(customid));
	}

	public CustomerProfileVO getCustomerProfile(String customid, CustomerProfileVO cp,String tableselect) {

		Hashtable profile = new Hashtable();
		//String result[][] = dbo.executeQuery(dba.getCustomerProfile(customid));
		String result[][] = new String[0][0];
		ArrayList hcondition = new ArrayList() ;
		ArrayList hstyles = new ArrayList() ;
		ArrayList hprocess = new ArrayList() ;
		ArrayList sallergy = new ArrayList() ;
		boolean hairtypeflag = false ;
	    try
	    {
		   dbo = new SVDBOperation() ;
		   dbo.createConnection() ;
		   result = getCustomerProfileQuery(dbo,dba,customid);
	    } catch (Exception e)
	    {
		  e.printStackTrace() ;
	    }
	    finally {
		   dbo.closeConnection() ;
	    }
		for (int i = 0; i < result.length; i++) {

			cp.setCustomerId(customid);

			if (result[i][1] != null && result[i][2].trim().equals("71"))
				cp.setHaircolor(result[i][3]);
			else if (result[i][1] != null && result[i][2].trim().equals("74"))
				cp.setHairthickness(result[i][3]);
			else if (result[i][1] != null && result[i][2].trim().equals("75"))
				cp.setHairtexture(result[i][3]);
			else if (result[i][1] != null && result[i][2].trim().equals("78"))
				cp.setHairfinish(result[i][3]);
			else if (result[i][1] != null && result[i][2].trim().equals("77"))
			{
				hcondition.add(result[i][3]);
			}
			else if (result[i][1] != null && result[i][2].trim().equals("73"))
				hprocess.add(result[i][3]);
			else if (result[i][1] != null && result[i][2].trim().equals("76"))
			{
				hstyles.add(result[i][3]);
			}
			else if (result[i][1] != null && result[i][2].trim().equals(ConstantDeclarations.SKIN_AGE_ID))
				cp.setAge(result[i][3]);
			else if (result[i][1] != null && result[i][2].trim().equals(ConstantDeclarations.SKIN_ALLERGY_ID))
				sallergy.add(result[i][3]);

			else if (result[i][1] != null && result[i][2].trim().equals("58"))
				cp.setEthnicity(result[i][3]);
			else if (result[i][1] != null && result[i][2].trim().equals("57"))
				cp.setNatural(result[i][3]);

			profile.put(result[i][2].trim() + "-" + result[i][3], "1");

	  }
		String htyp = "" ;
		if (hcondition.contains("R001"))
		{
			cp.setHairtype("Y001") ;
			htyp = "Y001" ;
		}
		else if (hcondition.contains("R008"))
		{
			cp.setHairtype("Y002") ;
			htyp = "Y002" ;
		}
		else
		{
			cp.setHairtype("Y003") ;
			htyp = "Y003" ;
		}
		profile.put("72" + "-" + htyp, "1");
		cp.setHairconditions(hcondition) ;
		cp.setHairprocess(hprocess) ;
		cp.setHairstyles(hstyles) ;
		cp.setAllergyList(sallergy) ;
		
		cp.setProfileHT(profile);
       return cp ;
	}

	public CustomerProfileVO getCustomerGeneralProfile(String customid, CustomerProfileVO cp) {
		try {
			dbo = new SVDBOperation() ;
			dbo.createConnection() ;
			String result[][] = new String[0][0];

			result = dbo.executeQuery(dba.getCustomerGenProfile(customid));

			if(result.length > 0) {
				cp.setCustomerEntryDate(result[0][4]);
				cp.setGender(result[0][5].trim());
			}
		} catch (Exception e) {
			e.printStackTrace() ;
		}
		finally {
			dbo.closeConnection() ;
		}

		return cp ;
	}


	protected String increaseScore(String value) {

		/*commented for performance reasons.
		try {

			float val = Float.parseFloat(value.trim());

			/*
			 * if(val == 7.0f) // commented on 03/25/03 val = val * 1.4f * 2f ;
			 * else if(val == 8.0f) val = val * 1.5f * 2f ; else if(val == 9.0f)
			 * val = val*1.75f * 2f ; else if(val==10f) val = val * 2.00f * 2f ;
			 */
		/*
			int v = Math.round(val);
			value = v + "";
		} catch (Exception e) {
			////logger.log(Level.DEBUG,e);
		}*/
		return value;

	}

	//ht1 is the hastable that contains prodid as key and the ingredeient
	// ArrayList related to that prodid as the value
	//ratinsgs is the array that contains the ingredient record which we got
	// from database .

	  public Hashtable getManualRatings(CustomerProfileVO cp , boolean manual)
	  {
		  Hashtable ratings  = new Hashtable();
		  String result[][]  = new String[0][0] ;
		  AttribRatingVO ar   = new AttribRatingVO();
			
		  if(manual)
		  {		     
		    try
		    {
			   dbo = new SVDBOperation() ;
			   dbo.createConnection() ;
			//   System.out.println("vb i am in dboconnectiobb,,,,,,,,,,,") ;
			   result   = dbo.executeQuery(dba.getHairManualRatings(cp.getHairtype(),cp.getHairprocess(),cp.getHairthickness(),cp.getHairtexture(),cp.getHairconditions(),cp.getHairstyles()));
			} catch (Exception e) {
				e.printStackTrace() ;
			}
			finally {
				dbo.closeConnection() ;
			}
	 	  }
				
	      for(int i = 0 ; i < result.length ; i++)
		  {
				  //key : 02-ingr-conc-mastertype
							
				  String key   = result[i][0].trim()+"-"+result[i][1].trim()+"-"+result[i][2].trim()+"-"+result[i][3].trim() ;
				  String value = result[i][6].trim() ;
				  String sub   = key+"-"+result[i][4]+"-"+result[i][5]+"-"+value ; 
	                           //key+ 51-T003-4							51-skintype ; T003 - dry  ; rating is 4 
							
			//	  System.out.println("ingr"+key+"-"+result[i][4]+"-"+result[i][5]+"-"+","+value);
				  
				  if(ratings.get(key)!=null) //if this is not the first rating value
				  {
					 if(value!=null && !value.trim().equals("NA"))
					 {
					    String tmpval = value ;
						ar = (AttribRatingVO)ratings.get(key) ;
//		vb01/20/10				value = String.valueOf(Integer.parseInt(ar.getScore())
	//							+ Integer.parseInt(value.trim()));
						value = String.valueOf(Float.parseFloat(ar.getScore())
								+ Float.parseFloat(value.trim()));
						ar.setScore(value.trim());
						ar.setCount(ar.getCount() + 1);
						ar.getIngrConcList().add(sub);
						ratings.put(key,ar) ;
					 }
				  }		
				  else if(value!=null && !value.trim().equals("NA")) //if first one
				  {
						ar       = new AttribRatingVO();
						ar.setScore(value.trim());
						ar.setCount(1);
						ar.getIngrConcList().add(sub);
						ratings.put(key,ar);
				  }
							
		 } //for 
									
		 return ratings ;
		
  }

	
	public Hashtable getIngredientsRatings(CustomerProfileVO cp, boolean manual) {
		Hashtable ratings = new Hashtable();
		String result[][] = new String[0][0];
		AttribRatingVO ar = new AttribRatingVO();
		String ingrlist = "";

		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
		//	System.out.println("I am here bn nmm") ;
		result = dbo.executeQuery(dba.getIngredientsRatings(cp.getSkinType(),
				cp.getSkinConditionList(), cp.getSkinConcernList(),
				cp.getEyeConcernList(), ingrlist));

		for (int i = 0; i < result.length; i++) {
			//key : 02-ingr-conc-mastertype

			String key = result[i][0].trim() + "-" + result[i][1].trim() + "-"
					+ result[i][2].trim() + "-" + result[i][3].trim();
			String value = result[i][6].trim();
			String sub = key + "-" + result[i][4] + "-" + result[i][5] + "-"
					+ value;
			//key+ 51-T003-4 51-skintype ; T003 - dry ; rating is 4

			//////logger.log(Level.DEBUG,"ingr"+key+"-"+result[i][4]+"-"+result[i][5]+"-"+","+value);



        //System.out.println("vbbb ingr"+key+"-"+result[i][4]+"-"+result[i][5]+"-"+","+value) ;
		if (ratings.get(key) != null) //if this is not the first rating
										  // value
			{

				if (value != null && !value.trim().equals("NA")) {
					String tmpval = value;

					if (!manual && (result[i][0].trim()).equals(ConstantDeclarations.ATTRIBUTE_INGR))//if not
																	  // manual
																	  // and if
																	  // not
																	  // base
					{
						value = increaseScore(value);

					}
					ar = (AttribRatingVO) ratings.get(key);
					value = String.valueOf(Integer.parseInt(ar.getScore())
							+ Integer.parseInt(value.trim()));
					ar.setScore(value.trim());
					ar.setCount(ar.getCount() + 1);
					ar.getIngrConcList().add(sub);

					ratings.put(key, ar);

				}
			} else if (value != null && !value.trim().equals("NA")) //if first
																	// one
			{
				ar = new AttribRatingVO();

				if (!manual && (result[i][0].trim()).equals(ConstantDeclarations.ATTRIBUTE_INGR)) {
					value = increaseScore(value);
				}
				ar.setScore(value.trim());
				ar.setCount(1);
				ar.getIngrConcList().add(sub);

				ratings.put(key, ar);

			}

		} //end of ratings

		} catch (Exception ex1) {
			ex1.printStackTrace() ;
		}
		finally {
			dbo.closeConnection() ;
		}
		return ratings;

	}
public ProductVO[] ratings(CustomerProfileVO cp,ProductVO[] product,Hashtable ingrratings) {

		//		 	//logger.log(Level.DEBUG,"product array size is"+product.length);

		for (int count = 0; count < product.length; count++) {

			ProductVO producttmp = product[count];
			String prodid = producttmp.getProdId();
			String prod = producttmp.getProdId();
			String ingr[][] = producttmp.getIngredients();
			String prodmastertype = producttmp.getMasterProdType();
			String prodtype = producttmp.getProdType();
			String pmtype = "" ;
			boolean tobedeleted = producttmp.isTobeDeleted();
			
			int ingrcount = 0;
			int ingrscore = 0;
			int mancount = 0;
			int manscore = 0;
			float percentage = 0.0f;
			Hashtable ht = new Hashtable();

			if (!tobedeleted) //if this product is not to be deleted
			{
                pmtype = prodmastertype ;

				prodmastertype = "99";

				String prodstring = "10-" + prod + "-NA-" + prodmastertype;


				//below is commented earlier this was functioning
				/**
				 * if(ingrratings.get(prodstring) !=null ) {
				 * ////logger.log(Level.DEBUG,"prod string"+prodstring); String tmp =
				 * ((AttribRating)(ingrratings.get(prodstring))).score; if(
				 * !tmp.trim().equals(":n")) { manscore = manscore +
				 * Integer.parseInt(((AttribRating)(ingrratings.get(prodstring))).score) ;
				 * mancount = mancount +
				 * ((AttribRating)ingrratings.get(prodstring)).count ;
				 * if(mancount==0) mancount = 1 ;
				 * ht.put(prodstring,((AttribRating)ingrratings.get(prodstring)).ingrconc); } }
				 */
				if (ingrratings.get(prodstring) != null) {
					AttribRatingVO att = (AttribRatingVO) ingrratings.get(prodstring);
					////logger.log(Level.DEBUG,"inside");

					ArrayList vect = att.getIngrConcList();
					for (int i = 0; i < vect.size(); i++) {
						String tmp = (String) vect.get(i);
						StringTokenizer stk = new StringTokenizer(tmp, "-");
						//02"+"-"+ingr[j][0]+"-3-99" values : ArrayList that holds
						// 51-T003-4 , 52-0009-5
						if (stk.nextToken().equals("10"))
							; //10
						{
							stk.nextToken(); //prodid
							stk.nextToken(); //prodconc
							stk.nextToken(); //mastertype
							String skin = stk.nextToken() + "-"
									+ stk.nextToken();

							String tmskin = skin;
							
							Hashtable profile = cp.getProfileHT();
							//		vb 01/21/10 below											//logger.log(Level.DEBUG,"prodstring "+prodstring);
							if (profile.get(tmskin) != null) {
								float sc1 = Float.parseFloat(stk.nextToken());
								int sc = (int)sc1 ;
								//completed
								if (sc !=5)
								{
								  manscore = manscore + sc ;  
								  mancount++;
								}
								ht.put(prodstring, ((AttribRatingVO) ingrratings.get(prodstring)).getIngrConcList());
						}
						} //end of if stk

					}//end of for
//					ht.put(prodstring, ((AttribRatingVO) ingrratings.get(prodstring)).getIngrConcList());
				}


/*				if (ingr != null)
					for (int j = 0; j < ingr.length; j++) {
						//02-ingredientid-ingredientconc-productmastertype
						if (ingr[j][1] == null
								|| ingr[j][1].trim().length() == 0)
							ingr[j][1] = "NA";

						String ingrstring = "02" + "-" + ingr[j][0] + "-"
								+ ingr[j][1] + "-" + prodmastertype;
						
						if (ingrratings.get(ingrstring) == null)
							ingrstring = "02" + "-" + ingr[j][0] + "-"
									+ ingr[j][1] + "-99";
						////logger.log(Level.DEBUG,ingrstring);
//                        System.out.println("ingr string "+ingrstring) ;
						if (ingrratings.get(ingrstring) != null) {
							AttribRatingVO att = (AttribRatingVO) ingrratings.get(ingrstring);
							////logger.log(Level.DEBUG,"inside");

							ArrayList vect = att.getIngrConcList();
//							System.out.println(" cccc vect size "+vect.size()) ;
							for (int i = 0; i < vect.size(); i++) {
								String tmp = (String) vect.get(i);
								StringTokenizer stk = new StringTokenizer(tmp, "-");
								//02"+"-"+ingr[j][0]+"-3-99" values : ArrayList
								// that holds 51-T003-4 , 52-0009-5
								if (stk.nextToken().equals(ConstantDeclarations.ATTRIBUTE_INGR))
									; //02
								{
									int addval = 1 ;
									stk.nextToken(); //ingrid
									stk.nextToken(); //ingrconc
									stk.nextToken(); //mastertype
									String skin = stk.nextToken() + "-"
											+ stk.nextToken();
									//													//logger.log(Level.DEBUG,"skin"+skin+"
									// "+prodid+" "+handbodyflag+" "+faceflag);

									String tmskin = skin;
//									System.out.println("cccc tmskin "+tmskin) ;
									boolean concernflag = false;
									if ( skin.trim().substring(0, 2).equals(ConstantDeclarations.SKIN_CONCERN_ID))
									{
										String tskin = skin.trim().substring(3, 7);
										if (faceflag
												&&  ((tskin.trim().equals("C022")
														|| tskin.trim().equals("C003")
														|| tskin.trim().equals("C020")
														|| tskin.trim().equals("C004")
														|| tskin.trim().equals("C023")
														|| tskin.trim().equals("C012")
														|| tskin.trim().equals("C019")
														|| tskin.trim().equals("C007")
													 )
													))
											concernflag = true;
										if (concernflag)
											tmskin = skin;
										else
											tmskin = "99999";
									} 
									concernflag = false;
									if ( skin.trim().substring(0, 2).equals("54"))
									{
										String tskin = skin.trim().substring(3, 7);
										if (eyeflag
												&& ( (tskin.trim().equals("C001")
														|| tskin.trim().equals("C002")
														|| tskin.trim().equals("E001")
														|| tskin.trim().equals("E004")
													 )
													))
											concernflag = true;
										if (concernflag)
											tmskin = skin;
										else
											tmskin = "99999";
									} 
									else
										tmskin = skin;
									
									// Not to apply eye scores to cleansers // 09-22-07 VB
									if (pmtype.equals("0001"))
									{
										if (tmskin.trim().equals("54-C001")
												|| tmskin.trim().equals("54-C002")
												|| tmskin.trim().equals("54-E001")
												|| tmskin.trim().equals("54-E004")
										   )
										   tmskin = "9999" ;
									}
									
									if (pmtype.equals("0005"))
									{
										if (tmskin.trim().equals("54-C001")
												|| tmskin.trim().equals("54-C002")
												|| tmskin.trim().equals("54-E001")
												|| tmskin.trim().equals("54-E004")
										   )
										   tmskin = "9999" ;
									}

									if (pmtype.equals("0004"))
									{
										if ((tmskin.trim().equals("53-C022")
														|| tmskin.trim().equals("53-C003")
														|| tmskin.trim().equals("53-C020")
														|| tmskin.trim().equals("53-C004")
														|| tmskin.trim().equals("53-C023")
														|| tmskin.trim().equals("53-C012")
														|| tmskin.trim().equals("53-C019")
														|| tmskin.trim().equals("53-C007")
														|| tmskin.trim().equals("52-D001")
														|| tmskin.trim().equals("52-D003")
														|| tmskin.trim().equals("52-D006")
														|| tmskin.trim().equals("52-D008")
														|| tmskin.trim().equals("52-D002")
														|| tmskin.trim().equals("51-Y001")
														|| tmskin.trim().equals("51-Y003")
														|| tmskin.trim().equals("51-T003")
														|| tmskin.trim().equals("51-Y002")
													 )
													)
							//				System.out.println(" I am in ingr eyeconcerns filter "+tmskin) ;
										   tmskin = "9999" ;
									}
									//													//logger.log(Level.DEBUG,"finally "+prodid+"
									// "+prodtype+" "+tmskin) ;
									Hashtable profile = cp.getProfileHT();
//									System.out.println("vbbbbbbbbbbbbbbbbbb "+tmskin+"  "+prodmastertype) ;
									if (profile.get(tmskin) != null) {
										
									//	System.out.println("vfsdksh "+prodmastertype+"  "+tmskin) ;
										
										if ( tmskin.trim().substring(0, 2).equals(ConstantDeclarations.SKIN_TYPE_ID))
											addval = 2 ;
										if ( tmskin.trim().substring(0, 2).equals(ConstantDeclarations.SKIN_CONDITION_ID))
											addval = 2 ;
										
										int sc = Integer.parseInt(increaseScore(stk.nextToken()));
										if (sc !=5)
										{
							//				if (product[count].getProdId().equals("146228"))
						//						   System.out.println("VB Ingr string "+pmtype+"   "+skin+"  "+ingrstring) ;
										  ingrscore = ingrscore + ( sc * addval); // VB 09/10/07
										  ingrcount++;
										  if (addval == 2)
											ingrcount++ ;
										}
									//	if (product[count].getProdId().equals("146228"))
									//	   System.out.println("vbscore "+pmtype+"  "+skin+"  "+ingrscore+" count "+ingrcount);
										ht.put(ingrstring, ((AttribRatingVO) ingrratings.get(ingrstring)).getIngrConcList());
									}
								} //end of if stk

							}//end of for
//							ht.put(ingrstring, ((AttribRatingVO) ingrratings.get(ingrstring)).getIngrConcList());
						}//end if ingr
                  
					} //end of for
				*/ 
				

			}//end of is to be deleted
			
			product[count] = producttmp;
			int prodpercentage = 0;


			if (mancount != 0)
				prodpercentage = 100 * manscore / (mancount * 10);

//			System.out.println("prodid"+product[count].getProdId()+"ingrpercentage"+ingrpercentage+"  "+ingrcount+"  "+"prod percentage "+prodpercentage+" "+mancount);
			//uncmmnet this , if u want to see the percentAGES
			//							if(ingrpercentage > 100) // commented on 02/26/03
			//						     ingrpercentage = 100 ;
			
            product[count].setManualScore(String.valueOf(prodpercentage)) ;
			product[count].setIngrScore("0");
			product[count].setBaseScore("0");
			product[count].setIndividualingrratings(ht);
			product[count].setScore(calculatescore(product[count]));
			////logger.log(Level.DEBUG,"end of products");

		}//end of for for final products

		return product ;
	}

	protected String calculatescore(ProductVO prod) {

//vbh		float ingrpercentage = Float.parseFloat(prod.getIngrScore());
		float ingrpercentage = 0.0f;
//vbh		float prodpercentage = Float.parseFloat(prod.getBaseScore());
		float basepercentage = 0.0f;
		float manualpercentage = 0.0f;
		float bmpscore = 0.0f;
		try
		{
		   bmpscore = Float.parseFloat(prod.getBumpScore());
		}
		catch(Exception bm)
		{
			bmpscore = 0.0f;
		}
		float score = 0.0f;

		if (prod.getManualScore() != null
					&& prod.getManualScore().trim().length() > 0) 
		{
				score = Float.parseFloat(prod.getManualScore())+Float.parseFloat(prod.getBumpScore());
		}
        else 
		{
				  basepercentage = (basepercentage * 20f) / 100f; // original 10
				  ingrpercentage = (ingrpercentage * 80f) / 100f; // original 90
				  score = ingrpercentage + basepercentage + bmpscore;
		}
	//	System.out.println("fianlly prodid"+prod.getProdId()+"score "+score+" manual score "+manualpercentage+" bump score "+prod.getBumpScore()+"    "+"prod percentage "+basepercentage+" ");
		
		return score + "";
	}

    // this method eliminates the ingredients that r not taken into ratings ,
	// ie , whose ratings values r less than 0
	protected Hashtable eliminateIngredients(String skintype, ArrayList skincondition) {

	Hashtable eliminatedingredients = new Hashtable();
	try
	{
		dbo = new SVDBOperation() ;
		dbo.createConnection() ;
		String result[][] = dbo.executeQuery(dba.eliminateIngredients(skintype,skincondition));
		for (int i = 0; i < result.length; i++) {

			////logger.log(Level.DEBUG,"eliminated ingredients"+result[i][0]);
			eliminatedingredients.put(result[i][0], "Y");
		}
	} catch (Exception e)
	{
		e.printStackTrace() ;
	}
	finally {
		dbo.closeConnection() ;
	}
		return eliminatedingredients;
	}

	public ProductVO[] getProductArray(ProductVO[] productVOList,Hashtable prodhash) {
		int count = 0;
		Enumeration en = prodhash.keys();
		productVOList = new ProductVO[prodhash.size()];
		while (en.hasMoreElements()) {
			ProductVO pr = (ProductVO) prodhash.get(en.nextElement());
			productVOList[count++] = pr;

		}
		return productVOList;
	}





	public static void main(String args[]) {
		HairRecommendationEngine gp = new HairRecommendationEngine();
		String[] customers = new String[3];
		customers[0] = "C10062"; //leann
		customers[1] = "C10085"; // theresa
		customers[2] = "C10130"; // vb2
	//	gp.getProduct("C10085", "01", "", "0002");

	}
	public String getProductLine(CustomerProfileVO cp) {
		String prodLine = "" ;
		ArrayList prodUsage = new ArrayList() ;
		prodUsage = (ArrayList) cp.getProductUsage() ;
		boolean anewflag = false ;
		boolean avonflag = false ;
		boolean becomeflag = false ;

		if (prodUsage != null)
		{
			if (prodUsage.size() > 2)
				prodLine = ConstantDeclarations.PRODUCT_LINE_PRIME_ANEW ;
			else if (prodUsage.size() == 1)
			{
				    if ( ((String)prodUsage.get(0)).equals(ConstantDeclarations.PRODUCT_USAGE_NATING) )
				    	prodLine = ConstantDeclarations.PRODUCT_LINE_PRIME_ANEW ;
				    if ( ((String)prodUsage.get(0)).equals(ConstantDeclarations.PRODUCT_USAGE_CUTEDT) )
				    	prodLine = ConstantDeclarations.PRODUCT_LINE_PRIME_ANEW ;
				    if ( ((String)prodUsage.get(0)).equals(ConstantDeclarations.PRODUCT_USAGE_AGEBNF) )
				    	prodLine = ConstantDeclarations.PRODUCT_LINE_PRIME_ANEW ;
				    if ( ((String)prodUsage.get(0)).equals(ConstantDeclarations.PRODUCT_USAGE_PRDSKT) )
				    	prodLine = ConstantDeclarations.PRODUCT_LINE_PRIME_AVON ;
				    if ( ((String)prodUsage.get(0)).equals(ConstantDeclarations.PRODUCT_USAGE_BTFPKG) )
				    	prodLine = ConstantDeclarations.PRODUCT_LINE_PRIME_BCMG ;
				    if ( ((String)prodUsage.get(0)).equals(ConstantDeclarations.PRODUCT_USAGE_ATHDTR) )
				    	prodLine = ConstantDeclarations.PRODUCT_LINE_PRIME_ANEW ;
			}
			else if (prodUsage.size() == 2 )
			{
				    String pusage1 = (String)prodUsage.get(0) ;
				    String pusage2 = (String)prodUsage.get(1) ;
				    if (pusage1.equals(ConstantDeclarations.PRODUCT_USAGE_NATING) && pusage2.equals(ConstantDeclarations.PRODUCT_USAGE_BTFPKG) )
				    //	prodLine = ConstantDeclarations.PRODUCT_LINE_PRIME_BCMG ;
			    	    prodLine = ConstantDeclarations.PRODUCT_LINE_PRIME_ANEW ;
				    else
				    	prodLine = ConstantDeclarations.PRODUCT_LINE_PRIME_ANEW ;
			}
			else if (prodUsage.size() == 0)
		    	prodLine = ConstantDeclarations.PRODUCT_LINE_PRIME_ANEW ;
		 }
		 else
			prodLine = ConstantDeclarations.PRODUCT_LINE_PRIME_ANEW ;
		return prodLine;
	}

	public ArrayList getProdDetailsRegimen(String customerid, String regimenline)
	{
		String[][]  result        = null;
		ProductVO[] productVOList = new ProductVO[0];
		ArrayList   prodlist      = new ArrayList();
		String[][] result2 = null ;
		String sql = "" ;

		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			sql = " select sv_product_id from customer_product_regimen where sv_customer_id = '"+customerid+"' "+
			      " and sv_product_regimen_line = '"+regimenline+"' " ;
			result2 = dbo.executeQuery(sql) ;

			result = dbo.executeQuery(dba.getProductRegimenCustomer(customerid,regimenline));

		  for (int j=0; j < result2.length ; j++)
		  {
		    for (int i = 0; i < result.length; i++)
		    {
              if (result2[j][0].equals(result[i][0]))
              {
			    ProductVO pr = new ProductVO();
			    pr.setProdId(result[i][0].trim());
			    pr.setImage(result[i][1]);
			    pr.setPrice(result[i][2]);
			    pr.setShortDesc(result[i][3]);
			    pr.setProdName(result[i][4]);
			    pr.setProdType(result[i][5]);
			    pr.setMasterProdType(result[i][6]);
			    pr.setBase(result[i][7]);
			    pr.setProductLine(result[i][8]);
			    pr.setIngrScore("0");
			    pr.setFunctionscore("0");
			    pr.setBumpScore("0");
			    pr.setTobeDeleted(false);
			    pr.setProductLinePrime(result[i][9]);
			    pr.setProductLaunchDate(result[i][10]);
			    pr.setProductRegimenDate(result[i][11]);
			    pr.setProductRegimenFlag(result[i][12]) ;
			    prodlist.add(pr);
			    break ;
              }
		    }
		  }
		} catch (Exception ex1) {
			ex1.printStackTrace() ;
		}
		finally {
			dbo.closeConnection() ;
		}

		return prodlist;
	}
    public Hashtable getMasterDataOne()
    {
		String[][]  result        = null;
		String sql = "" ;
        Hashtable master = new Hashtable() ;
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			sql = " select * from svb_view1 " ;
			result = dbo.executeQuery(sql) ;


		  for (int j=0; j < result.length ; j++)
		  {
		  	master.put(result[j][0],result[j][1]) ;
		  }
		} catch (Exception e)
		{
			System.out.println(e) ;
		}
		finally {
			dbo.closeConnection() ;
		}
		return master ;
    }
    public Hashtable getMasterDataTwo()
    {
		String[][]  result        = null;
		String sql = "" ;
        Hashtable master = new Hashtable() ;
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			sql = " select * from svb_view2 " ;
			result = dbo.executeQuery(sql) ;


		  for (int j=0; j < result.length ; j++)
		  {
		  	master.put(result[j][0],result[j][1]) ;
		  }
		} catch (Exception e)
		{
			System.out.println(e) ;
		}
		finally {
			dbo.closeConnection() ;
		}
		return master ;
    }
    public Hashtable getBrandDetails()
    {
		String[][]  result        = null;
		String sql = "" ;
		Hashtable brands = new Hashtable() ;
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			sql = " select * from sv_brand_description " ;
			result = dbo.executeQuery(sql) ;


		  for (int j=0; j < result.length ; j++)
		  {
		  	String brandid = "" ;
		  	String branddesc = "" ;
		  	String brandlogo = "" ;
		  	brandid = result[j][0].trim() ;
		  	branddesc = result[j][1].trim() ;
		  	brandlogo = result[j][2].trim() ;
		  	BrandVO brand = new BrandVO() ;
		  	brand.setBrandId(brandid) ;
		  	brand.setBrandDescription(branddesc) ;
		  	brand.setBrandLogoFile(brandlogo) ;
//			System.out.println("Brand "+brandid+" "+branddesc+"  "+brandlogo) ;
		  	brands.put(result[j][0],brand) ;
		  }
		} catch (Exception e)
		{
			System.out.println(e) ;
		}
		finally {
			dbo.closeConnection() ;
		}
		//System.out.println("Brand size..."+brands.size()) ;
		return brands ;
    }
    public ArrayList getDynamicText(Hashtable prodhash, CustomerProfileVO cp, Hashtable master1)
    {
        //System.out.println("I am in getdynamictext  ") ;        
	   String[][]  result        = null;
	   ArrayList dynamic = new ArrayList() ;
	   String sql = "" ;
	   String tableid = "" ;
	   String ingrgroup = "" ;
	   String ingrid = "" ;
	   String ingrname = "" ;
	   String attribvalues = "" ;
	   String grouptext = "" ;
	   String attribdesc = "" ;
	   String warnings = "" ;
	   String commontext = "" ;
	   String prodname = "" ;
	   boolean dryskinflag = false ;
	   boolean sensitiveflag = false ;
	   boolean hyperflag = false ;
	   boolean rosaceaflag = false ;
	   boolean dynamicflag = false ;
	   boolean ethnicityflag = false ;
       ArrayList prodAHA = new ArrayList();
  	   ArrayList prodBHA = new ArrayList();
  	   ArrayList prodHQ = new ArrayList();
	   ArrayList prodVIT = new ArrayList();
  	   ArrayList prodAZ = new ArrayList();
  	   ArrayList prodRET = new ArrayList();
  	   ArrayList prodAFR = new ArrayList();
       ArrayList AHA = new ArrayList();
  	   ArrayList BHA = new ArrayList();
  	   ArrayList HQ = new ArrayList();
	   ArrayList VIT = new ArrayList();
  	   ArrayList AZ = new ArrayList();
  	   ArrayList RET = new ArrayList();
  	   ArrayList AFR = new ArrayList();
  	   
       String[] txtAHA = new String[3];
  	   String[] txtBHA = new String[3];
  	   String[] txtHQ = new String[3];
	   String[] txtVIT = new String[3];
  	   String[] txtAZ = new String[3];
  	   String[] txtRET = new String[3];
  	   String[] txtAFR = new String[3];
 

		    String cHairtype = cp.getSkinType();
		    String cEthnicity = cp.getEthnicity();
		
		    ArrayList custHairstyles = cp.getSkinConcernList();
		    ArrayList custHairconditions = cp.getSkinConditionList();
            ArrayList concerns = new ArrayList() ;			
         
		    if (cHairtype.equals("Y001"))
		    {
		    	dryskinflag = true ;
                concerns.add(cHairtype) ;
		    }
		    //System.out.println("cust skin condition list "+custHairconditions.size());
		    for (int m = 0; m < custHairconditions.size(); m++) {
		    	
				if (custHairconditions.get(m).equals("D001"))
				{
				   concerns.add(custHairconditions.get(m)) ;
					sensitiveflag = true;
				}
				if (custHairconditions.get(m).equals("D003"))
				{
			       concerns.add(custHairconditions.get(m)) ;
					rosaceaflag = true;
				}
				if (custHairconditions.get(m).equals("D006"))
				{
				   concerns.add(custHairconditions.get(m)) ;
					hyperflag = true;
				}
		    }
		    System.out.println("concerns 1 size "+concerns.size());
		    if (cEthnicity.equals("N001"))
			{
				     ethnicityflag = true;
				     dynamicflag = true ;
			}
		    
		    if (dryskinflag || ethnicityflag ||
		    		sensitiveflag || rosaceaflag || hyperflag)
		    {
			  dynamicflag = true ;
	  	      
		      
			   try {
		            dbo = new SVDBOperation();
		            dbo.createConnection();
		            sql = " select * from sv_ingredients_warnings " ;
		            result = dbo.executeQuery(sql) ;
	                System.out.println("I am before for  "+result.length) ;        
			       } catch (Exception e)
			       {
					System.out.println(e) ;
			       }
			       finally {
					dbo.closeConnection() ;
				   }
			       
	          for (int j=0; j < result.length ; j++)
	          {
	  	        tableid = result[j][0].trim() ;
	  	        ingrgroup = result[j][1].trim() ;
	  	        ingrid = result[j][2].trim() ;
	  	        grouptext = result[j][3].trim() ;
	  	        attribvalues = result[j][4].trim() ;
	  	        warnings = result[j][5].trim() ;
	  	        commontext = result[j][6].trim() ;
         
	  	        if (ingrgroup.equals("AHA"))
	  	        {
	  	 	       AHA.add(ingrid) ;
	  	 	       txtAHA = new String[3] ;
	  	 	       txtAHA[0] = grouptext ;
	  	 	       txtAHA[1] = warnings ;
	  	 	       txtAHA[2] = commontext ;
	  	        }
	  	        if (ingrgroup.equals("BHA"))
	  	        {
		  	 	       BHA.add(ingrid) ;
		  	 	       txtBHA = new String[3] ;
		  	 	       txtBHA[0] = grouptext ;
		  	 	       txtBHA[1] = warnings ;
		  	 	       txtBHA[2] = commontext ;
	  	        }
	  	        if (ingrgroup.equals("HQ"))
	  	        {
		  	 	       HQ.add(ingrid) ;
		  	 	       txtHQ = new String[3] ;
		  	 	       txtHQ[0] = grouptext ;
		  	 	       txtHQ[1] = warnings ;
		  	 	       txtHQ[2] = commontext ;
	  	        }
	  	        if (ingrgroup.equals("VIT"))
	  	        {
		  	 	       VIT.add(ingrid) ;
		  	 	       txtVIT = new String[3] ;
		  	 	       txtVIT[0] = grouptext ;
		  	 	       txtVIT[1] = warnings ;
		  	 	       txtVIT[2] = commontext ;
	  	        }
	  	        if (ingrgroup.equals("AZ"))
	  	        {
		  	 	       AZ.add(ingrid) ;
		  	 	       txtAZ = new String[3] ;
		  	 	       txtAZ[0] = grouptext ;
		  	 	       txtAZ[1] = warnings ;
		  	 	       txtAZ[2] = commontext ;
	  	        }
	  	        if (ingrgroup.equals("RET"))
	  	        {
		  	 	       RET.add(ingrid) ;
		  	 	       txtRET = new String[3] ;
		  	 	       txtRET[0] = grouptext ;
		  	 	       txtRET[1] = warnings ;
		  	 	       txtRET[2] = commontext ;
	  	        }
	  	        if (ingrgroup.equals("AFR"))
	  	        {
		  	 	       AFR.add(ingrid) ;
		  	 	       txtAFR = new String[3] ;
		  	 	       txtAFR[0] = grouptext ;
		  	 	       txtAFR[1] = warnings ;
		  	 	       txtAFR[2] = commontext ;
	  	        }
	          }
	       }
		    
         		    
		   if (dynamicflag)
		   {
             System.out.println("I am in dynamic flag  ") ;        
	         Enumeration en = prodhash.keys() ;
	         
	         Hashtable master  = getMasterDataTwo() ;
	         
		     while (en.hasMoreElements())
		     {
	         //    System.out.println("I am in while  ") ;        
		     	String prodid = (String) en.nextElement() ;
		     	ProductVO prod = new ProductVO() ;
		     	try
				{
		      //       System.out.println("I am in try  ") ;
		     	  prod = (ProductVO) prodhash.get(prodid) ;
		      //       System.out.println("I am in try 2 "+prod) ;        
		     	  prodname = (String) prod.getProdName() ;
		      //       System.out.println("I am in try 3 "+prodname) ;        
		          String[][] ingrlist = (String[][]) prod.getIngredients() ;
		      //       System.out.println("I am in prod  "+prodid+ " " +prodname+" "+ingrlist.length) ;
		          
		          if (ingrlist != null)
		          for (int n=0; n < ingrlist.length ; n++)
		          {
		        	String ingr = ingrlist[n][0] ;
		        	//System.out.println(prodid+"  "+ingr) ;
		        	if (AHA.contains(ingr))
		        	{
		                //System.out.println("I am in AHA  ") ;        
		                if (ethnicityflag)
		                	prodAFR.add(ingr) ;
		                else
		        		    prodAHA.add(ingr) ;
		        	}
		        	if (BHA.contains(ingr))
		        	{
		                //System.out.println("I am in BHA  ") ;        
		                if (ethnicityflag)
		                	prodAFR.add(ingr) ;
		                else
		        		    prodBHA.add(ingr) ;
		        	}
		        	if (HQ.contains(ingr))
		        	{
		                //System.out.println("I am in HQ  ") ;        
		                if (ethnicityflag)
		                	prodAFR.add(ingr) ;
		                else
		        		    prodHQ.add(ingr) ;
		        	}
		        	if (VIT.contains(ingr))
		        	{
		                //System.out.println("I am in VIT  ") ;        
		                if (ethnicityflag)
		                	prodAFR.add(ingr) ;
		                else
		        		    prodVIT.add(ingr) ;
		        	}
		        	if (AZ.contains(ingr))
		        	{
		                //System.out.println("I am in AZ  ") ;        
		                if (ethnicityflag)
		                	prodAFR.add(ingr) ;
		                else
		        		    prodAZ.add(ingr) ;
		        	}
		        	if (RET.contains(ingr))
		        	{
		                //System.out.println("I am in RET  ") ;
		                if (ethnicityflag)
		                	prodAFR.add(ingr) ;
		                else
		        		    prodRET.add(ingr) ;
		        	}
		        	if (AFR.contains(ingr))
		        	{
		                //System.out.println("I am in AFR  ") ;        
		        		prodAFR.add(ingr) ;
		        	}
		          } // if ingr list
				}catch (Exception exp) // try
				{
					//exp.printStackTrace() ;
				}
		     } // while enum
             boolean repeat = false ;
             //System.out.println("HQ "+prodHQ.size()) ;
             //System.out.println("AHA "+prodAHA.size()) ;
             //System.out.println("BHA "+prodBHA.size()) ;
             //System.out.println("VIT "+prodVIT.size()) ;
             //System.out.println("AZ "+prodAZ.size()) ;
             //System.out.println("RET "+prodRET.size()) ;
		     if (!repeat && prodAFR != null && prodAFR.size() > 0)
		     {
                //System.out.println("I am in AFR 2  ") ;        
  			  /* if (ethnicityflag)
 			   {
 			   	dynamic.add(txtAFR[1]) ;
 			   	dynamic.add(txtAFR[2]) ;
 			   }*/
             	String wtext = getText(prodAFR, master, txtAFR, concerns,false,true,master1) ;
				dynamic.add(wtext) ;
				dynamic.add(txtAFR[2]) ;
		     	repeat = true ;
		     }
		     if (!repeat && prodHQ != null && prodHQ.size() > 0 && hyperflag)
		     {
                //System.out.println("I am in HQ 2  ") ;        
		     	String wtext = getText(prodHQ, master, txtHQ, concerns,hyperflag,false,master1) ;
				dynamic.add(wtext) ;
				dynamic.add(txtHQ[2]) ;
				repeat = true ;
		     }
		     if (!repeat && prodAHA != null && prodAHA.size() > 0)
		     {
                //System.out.println("I am in AHA 2  ") ;        
		     	String wtext = getText(prodAHA, master, txtAHA, concerns,false,false,master1) ;
				dynamic.add(wtext) ;
				dynamic.add(txtAHA[2]) ;
		     	repeat = true ;
		     }
		     if (!repeat && prodBHA != null && prodBHA.size() > 0)
		     {
                //System.out.println("I am in BHA 2  ") ;        
		     	String wtext = getText(prodBHA, master, txtBHA, concerns,false,false,master1) ;
				dynamic.add(wtext) ;
				dynamic.add(txtBHA[2]) ;
		     	repeat = true ;
		     }
		     if (!repeat && prodVIT != null && prodVIT.size() > 0)
		     {
                //System.out.println("I am in VIT 2  ") ;        
		     	String wtext = getText(prodVIT, master, txtVIT, concerns,false,false,master1) ;
				dynamic.add(wtext) ;
				dynamic.add(txtVIT[2]) ;
		     	repeat = true ;
		     }
		     if (!repeat && prodAZ != null && prodAZ.size() > 0)
		     {
                //System.out.println("I am in AZ 2  ") ;        
		     	String wtext = getText(prodAZ, master, txtAZ, concerns,false,false,master1) ;
				dynamic.add(wtext) ;
				dynamic.add(txtAZ[2]) ;
		     	repeat = true ;
		     }
		     if (!repeat && prodRET != null && prodRET.size() > 0)
		     {
                //System.out.println("I am in RET 2  ") ;        
		     	String wtext = getText(prodRET, master, txtRET, concerns,false,false,master1) ;
				dynamic.add(wtext) ;
				dynamic.add(txtRET[2]) ;
		     	repeat = true ;
		     }
		   } // if dynamic
		
       
	   //System.out.println("dynamic "+dynamic.size()) ;
	   return dynamic ;
    }
    public String getText(ArrayList prod,Hashtable master, String[]txt, ArrayList concerns, boolean hyperflag, boolean ethnicityflag, Hashtable master1)
    {
        //System.out.println("I am in gettext  ") ;        
    	String textM = "" ;
    	String text1 = "" ;
    	String text2 = "" ;
        text1 = txt[0] ;
        for (int i=0; i < prod.size(); i++)
        {
        	String ingrname = (String) master.get((String)prod.get(i)) ;
            if (i == 0)
                text1 = text1+" <strong>"+ingrname.toLowerCase()+"</strong>" ;
            else if (i == prod.size()-1)
            {
                   text1 = text1+" and "+ingrname.toLowerCase() ;
            }
            else
                text1 = text1+", "+ingrname.toLowerCase() ;

        }
        text2 = "" ;
        //System.out.println("text2 concerns "+concerns.size()) ;
        
        if (!ethnicityflag)
        for (int i=0; i < concerns.size(); i++)
        {
        	String attribname = (String) master1.get((String)concerns.get(i)) ;
        	if (attribname.equals("tight or dry"))
        		attribname = "tight or dry skin" ;
        	if (attribname.equals("sensitive skin"))
        		attribname = "sensitivity" ;
        	attribname = "<strong>"+attribname+"</strong>" ;
            if (i == 0)
                text2 = text2+attribname ;
            else if (i == concerns.size()-1)
            {    
            	if (concerns.size() == 2)
            	    text2 = text2+" with "+attribname ;
                 else
                   text2 = text2+" and "+attribname ;
            }
            else
                text2 = text2+", "+attribname ;

        }

        
        if (!hyperflag)
           textM = text1+" "+txt[1]+" "+text2+"." ;
        else 
            textM = text1+" "+txt[1] ;
        //System.out.println("text 1 "+text1) ;        
        //System.out.println("txt[1] "+txt[1]) ;        
        //System.out.println("text 2 "+text2) ;        
        System.out.println("text M "+textM) ;        
    	return textM ;
    }
    public ProductVO ratingsIndividual(CustomerProfileVO cp,ProductVO product,Hashtable ingrratings) {

			ProductVO producttmp = product;
			String prodid = producttmp.getProdId();
			String prod = producttmp.getProdId();
			String ingr[][] = producttmp.getIngredients();
			String prodmastertype = producttmp.getMasterProdType();
			String prodtype = producttmp.getProdType();
			String pmtype = "" ;
            Hashtable pManRatings = new Hashtable() ;			
			int ingrcount = 0;
			int ingrscore = 0;
			int mancount = 0;
			int manscore = 0;
			float percentage = 0.0f;
			Hashtable ht = new Hashtable();

            pmtype = prodmastertype ;

			prodmastertype = "99";

			String prodstring = "10-" + prod.trim() + "-NA-" + prodmastertype;

		//	  System.out.println("ingr ratings size 333 "+ingrratings.size()+"  "+prodstring) ;
				if (ingrratings.get(prodstring) != null) {
					AttribRatingVO att = (AttribRatingVO) ingrratings.get(prodstring);
					////logger.log(Level.DEBUG,"inside");

					ArrayList vect = att.getIngrConcList();
					  System.out.println("vect size "+vect.size()) ;
					for (int i = 0; i < vect.size(); i++) {
						String tmp = (String) vect.get(i);
						StringTokenizer stk = new StringTokenizer(tmp, "-");
						//02"+"-"+ingr[j][0]+"-3-99" values : ArrayList that holds
						// 51-T003-4 , 52-0009-5
						if (stk.nextToken().equals("10"))
							; //10
						{
							stk.nextToken(); //prodid
							stk.nextToken(); //prodconc
							stk.nextToken(); //mastertype
							String skin = stk.nextToken() + "-"
									+ stk.nextToken();

							String tmskin = skin;
							
							Hashtable profile = cp.getProfileHT();
							//													//logger.log(Level.DEBUG,"prodstring "+prodstring);
							System.out.println("cc2 "+prodid+"  "+tmskin) ;
							if (profile.get(tmskin) != null) {
							//	int sc = Integer.parseInt(stk.nextToken());
								float sc1 = Float.parseFloat(stk.nextToken());
								int sc = (int)sc1 ;
								System.out.println("cc3 "+prodid+"  "+tmskin+"  "+sc) ;
								if (sc !=5)
								{
								  manscore = manscore + sc ;  
								  mancount++;
								}
//								if (sc > 6)
								   pManRatings.put(tmskin.substring(3,tmskin.length()),""+sc+"::1") ;
								ht.put(prodstring, ((AttribRatingVO) ingrratings.get(prodstring)).getIngrConcList());
						}
						} //end of if stk

					}//end of for
//					ht.put(prodstring, ((AttribRatingVO) ingrratings.get(prodstring)).getIngrConcList());
				}
				System.out.println("man ratings size  "+pManRatings.size()) ;
                producttmp.setProfileBaseRatings(pManRatings) ;

			
			product = producttmp;

//			System.out.println("prodid"+product.getProdId()+"ingrpercentage"+ingrpercentage+"  "+ingrcount+"  "+"prod percentage "+prodpercentage+" "+mancount);
			//uncmmnet this , if u want to see the percentAGES
			//							if(ingrpercentage > 100) // commented on 02/26/03
			//						     ingrpercentage = 100 ;
			


		return product ;
	}
    public ArrayList getCustomerReviewStars(ArrayList prodlist)
    {
    	Hashtable creviews = new Hashtable() ;
		String[][]  result        = null;
		String sql = "" ;
		Hashtable brands = new Hashtable() ;
	  	String starlogo = "" ;
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			sql = " select sv_product_id, sv_avg_customer_rating from customer_reviews where sv_avg_customer_rating <> '0'" ;
			System.out.println(sql);
			result = dbo.executeQuery(sql) ;


		  for (int j=0; j < result.length ; j++)
		  {
		  	String prodid = "" ;
		  	String avgrating = "" ;
		  	prodid = result[j][0].trim() ;
		  	avgrating = result[j][1].trim() ;
		  	creviews.put(prodid,avgrating) ;
		  }
		} catch (Exception e)
		{
			System.out.println(e) ;
		}
		finally {
			dbo.closeConnection() ;
		}
		ArrayList shampoo = new ArrayList() ;
		ArrayList conditioner = new ArrayList();
		ArrayList htreat  = new ArrayList() ;
		ArrayList styling = new ArrayList() ;
		ArrayList finish = new ArrayList() ;
		Hashtable fbrand = new Hashtable() ;
		
		if (prodlist.size() > 0)
		{
          shampoo = (ArrayList) prodlist.get(0) ;
          conditioner =    (ArrayList) prodlist.get(1) ;           
          htreat =    (ArrayList) prodlist.get(2) ;           
          styling =    (ArrayList) prodlist.get(3) ; 
          finish    =    (ArrayList) prodlist.get(4) ;  
          fbrand =    (Hashtable) prodlist.get(5) ;
          
			System.out.println("vbbbb 11");
			shampoo = (ArrayList) setStarrating(creviews,shampoo) ;
			conditioner = (ArrayList) setStarrating(creviews,conditioner) ;
			htreat = (ArrayList) setStarrating(creviews,htreat) ;
			styling = (ArrayList) setStarrating(creviews,styling) ;
		}
		prodlist = new ArrayList() ;
		prodlist.add(shampoo) ;
		prodlist.add(conditioner) ;
		prodlist.add(htreat) ;
		prodlist.add(styling) ;
		prodlist.add(finish) ;
		prodlist.add(fbrand) ;
		
		return prodlist ;
    }
    public ArrayList setStarrating(Hashtable creviews, ArrayList prodlist)
    {
    	System.out.println("vbbbb 2332 "+prodlist.size());
		for (int i=0; i < prodlist.size(); i++)
		{
	    	String starlogo = "" ;
			ProductVO prod = (ProductVO) prodlist.get(i) ;
			String pid = prod.getProdId() ;
			String arating = (String) creviews.get(pid);
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
				prod.setShadeImage(starlogo) ;
			}
		//	System.out.println("recommendation "+prod.getProdId()+" "+starlogo) ;
		}
    	return prodlist ;
    }
    public Hashtable getCustomerReviewStars2(Hashtable prodlist)
    {
    	Hashtable creviews = new Hashtable() ;
		String[][]  result        = null;
		String sql = "" ;
	  	String starlogo = "" ;
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			sql = " select sv_product_id, sv_avg_customer_rating from customer_reviews where sv_avg_customer_rating <> '0'" ;
			System.out.println(sql);
			result = dbo.executeQuery(sql) ;


		  for (int j=0; j < result.length ; j++)
		  {
		  	String prodid = "" ;
		  	String avgrating = "" ;
		  	prodid = result[j][0].trim() ;
		  	avgrating = result[j][1].trim() ;
		  	creviews.put(prodid,avgrating) ;
		  }
		} catch (Exception e)
		{
			System.out.println(e) ;
		}
		finally {
			dbo.closeConnection() ;
		}
        ArrayList prods = new ArrayList() ;
        Enumeration eth = prodlist.keys() ;
        while (eth.hasMoreElements())
        {
        	String prodid = (String) eth.nextElement() ;
        	ProductVO pr = (ProductVO) prodlist.get(prodid) ;
        	prods.add(pr) ;
        }
		prods = (ArrayList) setStarrating(creviews,prods) ;
		for (int i=0; i < prods.size(); i++)
		{
			ProductVO pr = (ProductVO) prods.get(i) ;
			String prid = (String) pr.getProdId() ;
			prodlist.put(prid,pr);
		}
		return prodlist ;
    }
    
}//end of class

