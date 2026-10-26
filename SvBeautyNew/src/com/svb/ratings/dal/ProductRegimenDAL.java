/*
 * Created on Oct 3, 2005
 *
 */
package com.svb.ratings.dal;

import java.util.ArrayList;
import com.svb.common.dba.SVDBOperation;
import com.svb.common.valueobjects.*;

/**
 * @author Neelima
 *
 */
public class ProductRegimenDAL {

	public ProductVO getProductInfo(String prodid) {
		ProductVO productVO = new ProductVO();

		SVDBOperation dbo = null;
		String sql = "";
		String[][] crec = new String[0][0];
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();

			sql = "select a.sv_product_id, a.SV_PRODUCT_IMAGE_FILE, a.sv_product_price, "+
				  "a.sv_product_description, a.sv_product_name, a.sv_product_type_id, "+
				  "b.sv_product_master_type, a.sv_product_base_id, a.sv_product_line_id, "+
				  "d.sv_product_line_prime, to_char(a.sv_product_enter_date,'yyyymmddhh24miss') "+
				  "from product a, avon_product_type b, avon_product_line d "+
				  "where sv_product_id = '"+prodid+"' "+
				  "and a.sv_product_type_id = b.sv_product_type_id "+
				  "and a.sv_product_line_id = d.sv_product_line_id ";

			//logger.log(Level.DEBUG,sql);
			crec = dbo.executeQuery(sql);
			productVO = setProductVo(crec);

		} catch (Exception ex) {
			//logger.log(Level.DEBUG,ex);
			ex.printStackTrace();
		}
		finally {
			dbo.closeConnection() ;
		}

		return productVO;
	}

	public String deleteModifiedRegimen(String customerId, String repId) {
		SVDBOperation dbo = null;
		String sql = "";
		String msg = "";
		int result;
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();

			sql = "delete from customer_product_regimen "+
				  "where sv_customer_id = '"+customerId+"' and "+
				  "sv_avon_rep_id = '"+repId+"' and "+
				  "sv_product_regimen_line = 'BA'";

			//logger.log(Level.DEBUG,sql);
			result = dbo.executeUpdate(sql);
			if(result!=0)
				msg = "yes";
			else
				msg = "no";

		} catch (Exception ex) {
			msg = "no";
			//logger.log(Level.DEBUG,ex);
			ex.printStackTrace();
		}
		finally {
			dbo.closeConnection() ;
		}

		return msg;
	}

	public String saveModifiedRegimen(String customerId, String repId,ProductVO products[]) {
		ProductVO productVO = new ProductVO();

		SVDBOperation dbo = null;
		String sql = "";
		String msg = "";
		int result;
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();

			for(int i=0; i<products.length; i++) {
				String productId = "";
				productId = products[i].getProdId();

				sql = "insert into customer_product_regimen "+
					  "values ('"+customerId+"','"+repId+"', sysdate, '"+productId+"','BA', 'C')";

				//logger.log(Level.DEBUG,sql);
				if(productId != null && !productId.equals(""))
					result = dbo.executeUpdate(sql);
			}

		} catch (Exception ex) {
			msg = "no";
			//logger.log(Level.DEBUG,ex);
			ex.printStackTrace();
		}
		finally {
			dbo.closeConnection() ;
		}

		return msg;
	}

	public String checkIfModifiedRegimenExists(String customerId, String repId) {

		SVDBOperation dbo = null;
		String sql = "";
		String[][] crec = new String[0][0];
		String modifyRegimenFlag = "no";
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();

			sql = "select count(*) from customer_product_regimen "+
				  "where sv_customer_id = '"+customerId+"' and "+
				  "sv_avon_rep_id = '"+repId+"' and sv_product_regimen_line = 'BA'";


			//logger.log(Level.DEBUG,sql);
			crec = dbo.executeQuery(sql);
			if(crec != null && crec.length > 0) {
				if(crec[0][0].trim().equals("0"))
					modifyRegimenFlag = "no";
				else
					modifyRegimenFlag = "yes";
			}

		} catch (Exception ex) {
			//logger.log(Level.DEBUG,ex);
			modifyRegimenFlag = "no";
			ex.printStackTrace();
		}
		finally {
			dbo.closeConnection() ;
		}

		return modifyRegimenFlag;
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

	public ArrayList assortMoisturizerProducts(ArrayList productList,String masterProdType,String masterProdTypeName) {

		//if it is night moisturizer, remove day moisturizer products from the list
		if(masterProdTypeName.equals("Night Moisturizer")) {
			for(int i=0; i<productList.size(); i++) {
				if(!((ProductVO)productList.get(i)).getProdType().equals("0002")) {
					productList.remove(i);
					i--;
				}
			}
		}
		// if it is day moisturizer, remove night moisturizer products from the list
		else {
			for(int i=0; i<productList.size(); i++) {
				if(((ProductVO)productList.get(i)).getProdType().equals("0002")) {
					productList.remove(i);
					i--;
				}
			}
		}

		return productList;
	}

	public ArrayList removeProductFromList(ArrayList cusProdList,ArrayList productList) {

		for(int i=0; i<cusProdList.size(); i++) {
			if(((ProductVO)cusProdList.get(i)).getMasterProdType().equals("04")) {
				for(int j=0; j<productList.size(); j++) {
					if(((ProductVO)cusProdList.get(i)).getProdId()
							.equals(((ProductVO)productList.get(j)).getProdId())) {
						productList.remove(j);
						break;
					}
				}
			}
		}

		return productList;
	}


}
