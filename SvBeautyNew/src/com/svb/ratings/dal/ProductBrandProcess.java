/*
 * Created on Oct 3, 2005
 *
 */
package com.svb.ratings.dal;

import java.util.ArrayList;
import com.svb.common.dba.SVDBOperation;
import com.svb.common.valueobjects.*;

/**
 * @author VB Rao
 *
 */
public class ProductBrandProcess {

	public ProductVO getProductInfo() {
		ProductVO productVO = new ProductVO();

		SVDBOperation dbo = null;
		String sql = "";
		String[][] crec = new String[0][0];
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();

			sql = "select d.sv_product_line_prime_id, e.sv_product_master_type_id, a.sv_product_id "+
                  "from temp_product a, avon_product_line b, avon_product_type c, "+
                  "avon_product_line_prime d, avon_product_master_type e "+
                  "where a.sv_product_line_id =  b.sv_product_line_id  "+
                  "and b.sv_product_line_prime_id = d.sv_product_line_prime_id "+
                  "and a.sv_product_type_id = c.sv_product_type_id "+
                  "and c.sv_product_master_type = e.sv_product_master_type_id "+
                  "and a.sv_product_status = 'Y' "+
                  "order by b.sv_product_line_prime_id, c.sv_product_master_type, a.sv_product_id "+
                  "" ;

			//logger.log(Level.DEBUG,sql);
			crec = dbo.executeQuery(sql);
//			productVO = setProductVo(crec);
			
			boolean cleanseflag = false ;
			boolean toneflag  = false ;
			boolean exfoliantflag = false ;
			boolean moisturizeflag = false ;
			boolean nmoisturizeflag = false ;
			boolean treatflag = false ;
			boolean etreatflag = false ;
            boolean brandflag = false ;
            String cleanse = "" ;
            String tone = "" ;
            String exfoliant = "" ;
            String moisturize = "" ;
            String nmoisturize = "" ;
            String treat = "" ;
            String etreat = "" ;
            
            String oldbrandid = "" ;
            String brandid = "" ;
            int iflag = 0 ;
			for (int i=0 ; i < crec.length ; i++)
			{
				System.out.println("Crec  "+crec[i][0]+" "+crec[i][1]+"  "+crec[i][2]) ;	
				if (iflag != 0 && !brandid.equals(crec[i][0])) 
				{
					int bflag = 0 ;
					if (cleanseflag)
					{
						bflag = bflag+1 ;
						cleanse = "Y" ;
					}
					if (moisturizeflag)
					{
						moisturize = "Y" ;
						bflag = bflag+1 ;
					}
					if (treatflag)
					{
						treat = "Y" ;
						bflag = bflag+1 ;
					}
					if (etreatflag)
					{
						etreat = "Y" ;
						bflag = bflag+1 ;
					}
					if (toneflag)
					{
						tone = "Y" ;
						bflag = bflag+1 ;
					}
					if (exfoliantflag)
					{
						exfoliant = "Y" ;
						bflag = bflag+1 ;
					}
					
					if (bflag > 2)
					{
						brandflag = true ;
						
						String sql2 = "insert into sv_brand_check values "+
						              "('"+brandid+"','"+cleanse+"', '"+tone+"', "+
									  " '"+exfoliant+"', '"+moisturize+"', '"+nmoisturize+"', "+
					                  " '"+treat+"', '"+etreat+"', 'Y' ) " ;
						int i1 = dbo.executeUpdate(sql2) ;
						cleanseflag = false ;
						toneflag  = false ;
						exfoliantflag = false ;
						moisturizeflag = false ;
						nmoisturizeflag = false ;
						treatflag = false ;
						etreatflag = false ;
			            brandflag = false ;
			            cleanse = "" ;
			            tone = "" ;
			            exfoliant = "" ;
			            moisturize = "" ;
			            nmoisturize = "" ;
			            treat = "" ;
			            etreat = "" ;
						
					}
					if (crec[i][1].equals("01"))
						cleanseflag = true ;
					else if (crec[i][1].equals("02"))
						moisturizeflag = true ;
					else if (crec[i][1].equals("03"))
						toneflag = true ;
					else if (crec[i][1].equals("04"))
						treatflag = true ;
					else if (crec[i][1].equals("06"))
						etreatflag = true ;
					else if (crec[i][1].equals("11"))
						exfoliantflag = true ;

					}
					else
					{
						if (crec[i][1].equals("01"))
							cleanseflag = true ;
						else if (crec[i][1].equals("02"))
							moisturizeflag = true ;
						else if (crec[i][1].equals("03"))
							toneflag = true ;
						else if (crec[i][1].equals("04"))
							treatflag = true ;
						else if (crec[i][1].equals("06"))
							etreatflag = true ;
						else if (crec[i][1].equals("11"))
							exfoliantflag = true ;
					}
					brandid = crec[i][0];
		           	iflag = iflag+1 ;

			}


		} catch (Exception ex) {
			//logger.log(Level.DEBUG,ex);
			ex.printStackTrace();
		}
		finally {
			dbo.closeConnection() ;
		}

		return productVO;
	}

	public String[][] getBrands(String prodstring) {
		ProductVO productVO = new ProductVO();

		SVDBOperation dbo = null;
		String sql = "";
		String[][] crec = new String[0][0];
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();

			sql = " select distinct c.sv_brand_id,c.sv_brand_name from temp_product_elements a, sv_brand_master c "+ 
                  " where a.sv_product_brand_id = c.sv_brand_id "+ 
                  " and a.sv_product_id in "+prodstring+" " +
                  " order by c.sv_brand_name" ;

			//logger.log(Level.DEBUG,sql);
			crec = dbo.executeQuery(sql);
//			productVO = setProductVo(crec);
			

		} catch (Exception ex) {
			//logger.log(Level.DEBUG,ex);
			ex.printStackTrace();
		}
		finally {
			dbo.closeConnection() ;
		}

		return crec;
	}



	protected ProductVO setProductVo(String[][] result) {

		ProductVO prodVO = new ProductVO();
		prodVO.setProdId(result[0][0].trim());
		prodVO.setImage(result[0][1]);
		prodVO.setPrice(result[0][2]);
		prodVO.setShortDesc(result[0][3]);
		prodVO.setProdName(result[0][4]);
		prodVO.setProdType(result[0][5]);
		prodVO.setMasterProdType(result[0][6]);
		prodVO.setBase(result[0][7]);
		prodVO.setProductLine(result[0][8]);
		prodVO.setIngrScore("0");
		prodVO.setFunctionscore("0");
		prodVO.setBumpScore("0");
		prodVO.setTobeDeleted(false);
		prodVO.setProductLinePrime(result[0][9]);
		prodVO.setProductLaunchDate(result[0][10]);

		return prodVO;
	}

	public ArrayList toArrayList(ProductVO[] products) {

		ArrayList prodList = new ArrayList();

		if(products.length > 0) {
			for(int i=0; i<products.length; i++) {
				prodList.add(i,products[i]);
			}
		}

		return prodList;
	}

  public static void main(String[] args)
  {
  	 ProductBrandProcess pbr = new ProductBrandProcess() ;
  	 pbr.getProductInfo() ;
  }

}
