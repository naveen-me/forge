package com.svb.webservices.serviceImpl;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Service;

import com.svb.common.ConstantDeclarations;
import com.svb.common.dba.SVDBConnection;
import com.svb.common.valueobjects.CustomerProfileVO;
import com.svb.common.valueobjects.ProductVO;
import com.svb.customerprofile.dal.CustomerProfileDAL;
import com.svb.customerprofile.formbeans.ProfileSummaryFormBean;
import com.svb.ratings.javabeans.RecommendationEngine;
import com.svb.webservices.model.ErrorDetails;
import com.svb.webservices.model.ProductStatusModel;
import com.svb.webservices.model.ProductUpdateErrorDetails;
import com.svb.webservices.model.RecProductAvailibility;
import com.svb.webservices.model.RecomondationProductModel;
import com.svb.webservices.model.RecomondedProductResponseModel;
import com.svb.webservices.model.SingleProductScoreModel;
import com.svb.webservices.model.SingleProductScoreResponseModel;
import com.svb.webservices.service.ProductService;

/**
 * 
 * @date 29-Nov-2019
 * @author Synoptek
 *
 */
@Service("productService")
public class ProductServiceImpl implements ProductService {
	public static final String RATING_CONSTANT = "Not Ideal";
	private SVDBConnection dbcon;
	private Connection con;
	
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override
	public RecomondedProductResponseModel getProductRecomondationList(RecomondationProductModel productModel)
			throws IllegalAccessException, InvocationTargetException {
		
		//Added by Shivam for eye score calculations - 06/09/2023 - Start
		//---> If user selects no Concerns in eye then consider all the concerns to calculate scores of eye product 
			//(These scores will be neglected for the Skin Product by Rec Engine Algo.)
		if(Objects.isNull(productModel.getEye_concern()) || productModel.getEye_concern()[0].equals("E999")) {
			String[] eyeConcerns = new String[] {"E001","C002","C001","E004"};
			productModel.setEye_concern(eyeConcerns);
		}
		//Added by Shivam for eye score calculations - 06/09/2023 - End
		
		RecomondedProductResponseModel recomondedProductDetailDTO = new RecomondedProductResponseModel();
		String prodType = "";
		String prodLine = "";
		String customerId = "";
		if(productModel.getCusid() != null && productModel.getCusid().length() > 0) {
			customerId = productModel.getCusid();
		}
		else {
			Long time = System.currentTimeMillis();
			customerId = time.toString();
		}
		System.out.println("customerId :: " + customerId);
		RecommendationEngine rec = new RecommendationEngine();
		Hashtable<String, String> master = (Hashtable) rec.getMasterDataOne();
		CustomerProfileVO cp = new CustomerProfileVO();
		ProductRecommend productRecommend = new ProductRecommend();
		ProfileSummaryFormBean profilesForm = new ProfileSummaryFormBean();
		CustomerProfileDAL skinConProfileDAL = new CustomerProfileDAL();
		BeanUtils.copyProperties(profilesForm, productModel);
		skinConProfileDAL.getProfileData(profilesForm, customerId, "");
		recomondedProductDetailDTO = productRecommend.getRecommnedProduct(customerId, prodType, "", prodLine, cp, "",
				master, productModel, recomondedProductDetailDTO);
		return recomondedProductDetailDTO;	
	}
	
	public boolean activateAllProducts() {
		String sql = "update temp_product set SV_PRODUCT_STATUS=?";
		int updateCounts = 0;
		boolean productsUpdated = false;
				PreparedStatement preparedStatement = null;
				try{
					dbcon = new SVDBConnection();
					con = dbcon.getConnection();	
					con.setAutoCommit(false);
				    preparedStatement = con.prepareStatement(sql);
				    preparedStatement.setString(1, "Y");
					updateCounts = preparedStatement.executeUpdate();
					productsUpdated = true;
					System.out.println("Product Activated Successfully " + updateCounts);
					con.commit();
					con.close();
				} catch (SQLException ex) {
					System.out.println("Product Not Activated Successfully " + updateCounts);
					ex.printStackTrace();
					productsUpdated = false;
					try {
						con.rollback();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
		return productsUpdated;
	}

	@Override
	public List<ErrorDetails> updateProductAvailabilityList(RecProductAvailibility productStatusModel) throws SQLException {	
		List<ProductStatusModel> productStatus = productStatusModel.getProductAvailability();
		List<ErrorDetails> errorList = new ArrayList<>();
		String sql = "update temp_product set SV_PRODUCT_STATUS=? where UPC=?";
		int[] updateCounts = null;
		boolean productUpdated = true;
				PreparedStatement preparedStatement = null;
				boolean productsActivated = activateAllProducts();
				if(productsActivated) {
					try{
						dbcon = new SVDBConnection();
						con = dbcon.getConnection();	
						con.setAutoCommit(false);
					    preparedStatement = con.prepareStatement(sql);
					    for(ProductStatusModel status : productStatus) {
					        preparedStatement.setString(1, status.getStatus());
					        preparedStatement.setString(2, status.getUpc());
					        preparedStatement.addBatch();
					    }
						updateCounts = preparedStatement.executeBatch();				
						con.commit();
						con.close();
					} catch (SQLException ex) {
						ex.printStackTrace();
						try {
							con.rollback();
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
//					for (int i = 0; i < updateCounts.length; i++) {
//						if (updateCounts[i] == 0) {
//							productUpdated = false;
//							ErrorDetails error = new ErrorDetails();
//							error.setStatus(500);
//					        error.setResponseMessage("Error in updating product status. UPC "+productStatus.get(i).getUpc()+" not exist");
//					        errorList.add(error);
//						}
//					}
//					if (productUpdated) {
//						ErrorDetails error = new ErrorDetails();
//						error.setStatus(201);
//				        error.setResponseMessage("Product status updated successfully");
//				        errorList.add(error);
//						return errorList;
//					} else {
//						return errorList;
//					}
					String errorMsg = "";
					int errCount = 0;
					for (int i = 0; i < updateCounts.length; i++) {
						if (updateCounts[i] == 0) {
							productUpdated = false;
							if(errCount > 0) {
								errorMsg = errorMsg + ", ";
							}
							errorMsg = errorMsg + productStatus.get(i).getUpc();
							errCount ++;
						}
					}
					if(errCount > 0) {
						ProductUpdateErrorDetails error = new ProductUpdateErrorDetails();
						error.setStatus(500);
						error.setUpcCodeList(errorMsg);
				        error.setResponseMessage("Error in updating product status. UPC not found.");
				        errorList.add(error);
					}
					
					String successrMsg = "";
					int successCount = 0;
					for (int i = 0; i < updateCounts.length; i++) {
						if(updateCounts[i] != 0) {
							if(successCount > 0) {
								successrMsg = successrMsg + ", ";
							}
							successrMsg = successrMsg + productStatus.get(i).getUpc();
							successCount ++;
						}
					}
					
					if(successCount > 0) {
						ProductUpdateErrorDetails error = new ProductUpdateErrorDetails();
						error.setStatus(201);
						error.setUpcCodeList(successrMsg);
				        error.setResponseMessage("Product status updated successfully.");
				        errorList.add(error);
					}
					
					return errorList;
				}
				else {
					ProductUpdateErrorDetails error = new ProductUpdateErrorDetails();
					error.setStatus(400);
			        error.setResponseMessage("Something Went Wrong!");
			        errorList.add(error);
			        return errorList;
				}
				
//				if (productUpdated) {
//					ErrorDetails error = new ErrorDetails();
//					error.setStatus(201);
//			        error.setResponseMessage("Product status updated successfully");
//			        errorList.add(error);
//					return errorList;
//				} else {
//					return errorList;
//				}
			}

	@Override
	public SingleProductScoreResponseModel singleProductScore(SingleProductScoreModel productModel) throws Exception {
		
		//Added by Shivam for eye score calculations - 06/09/2023 - Start
		//---> If user selects no Concerns in eye then consider all the concerns to calculate scores of eye product 
			//(These scores will be neglected for the Skin Product by Rec Engine Algo.)
		if(Objects.isNull(productModel.getEye_concern()) || productModel.getEye_concern()[0].equals("E999")) {
			String[] eyeConcerns = new String[] {"E001","C002","C001","E004"};
			productModel.setEye_concern(eyeConcerns);
		}
		//Added by Shivam for eye score calculations - 06/09/2023 - End
		
		System.out.println("IN PRODUCT SERVICE IMPL :: " + productModel);
		SingleProductScoreResponseModel productModelDTO = new SingleProductScoreResponseModel();
		String prodType = "";
		String prodLine = "";
		String customerId = "";
		if(productModel.getCusid() != null && productModel.getCusid().length() > 0) {
			customerId = productModel.getCusid();
		}
		else {
			Long time = System.currentTimeMillis();
			customerId = time.toString();
		}
		System.out.println("customerId :: " + customerId);
		RecommendationEngine rec = new RecommendationEngine();
		long start = 0L;
		long end = 0L;
		
		start = System.currentTimeMillis();
		Hashtable<String, String> master = (Hashtable) rec.getMasterDataOne();
		end = System.currentTimeMillis();
		System.out.println("TOTAL SECONDS TAKEN BY MASTER :: " + (end-start)/1000 + " Sec + " + (end-start) + " ms" );
		
		ProfileSummaryFormBean profilesForm = new ProfileSummaryFormBean();
		CustomerProfileDAL skinConProfileDAL = new CustomerProfileDAL();
		System.out.println("IN PRODUCT SERVICE IMPL :: 4 " + productModelDTO.getProductId());
		BeanUtils.copyProperties(profilesForm, productModel);
		System.out.println("IN PRODUCT SERVICE IMPL :: 5");
		
		start = System.currentTimeMillis();
//		skinConProfileDAL.getProfileData(profilesForm, customerId, "");
		skinConProfileDAL.getProfileDataSingleProduct(profilesForm, customerId, "");
		end = System.currentTimeMillis();
		System.out.println("TOTAL SECONDS TAKEN BY profile data :: " + (end-start)/1000 + " Sec + " + (end-start) + " ms" );
		
//		start = System.currentTimeMillis();
//		skinConProfileDAL.getProfileDataSingleProduct(profilesForm, customerId, "");
//		end = System.currentTimeMillis();
//		System.out.println("TOTAL SECONDS TAKEN BY profile data :: " + (end-start)/1000 + " Sec + " + (end-start) + " ms" );
		
		System.out.println("IN PRODUCT SERVICE IMPL :: 3");
		CustomerProfileVO cp = new CustomerProfileVO();
		ProductRecommend productRecommend = new ProductRecommend();
		
		start = System.currentTimeMillis();
		cp = (CustomerProfileVO) productRecommend.getCustomerProfile(customerId, cp, "skin");
		end = System.currentTimeMillis();
		System.out.println("TOTAL SECONDS TAKEN BY CP :: " + (end-start)/1000 + " Sec + " + (end-start) + " ms" );
		
		Hashtable all_products = new Hashtable();
		Hashtable productsHashtable = new Hashtable();
		Hashtable eliminated_products = new Hashtable();
		
		start = System.currentTimeMillis();
		all_products = productRecommend.getProdDetails("", "", "",cp,productModel.getUpc());
		end = System.currentTimeMillis();
		System.out.println("TOTAL SECONDS TAKEN BY ALL_PRODUCTS :: " + (end-start)/1000 + " Sec + " + (end-start) + " ms" );
		
		productsHashtable = (Hashtable) all_products.get("eligible products");
		eliminated_products = (Hashtable) all_products.get("eliminated products");
		ProductVO productVOList[] = new ProductVO[0];
		ProductVO productVO = new ProductVO();
		System.out.println("all_products :: " + all_products.size());
		System.out.println("productsHashtable :: " + productsHashtable.size());
		System.out.println("eliminated_products :: " + eliminated_products.size());
		if(productsHashtable == null || productsHashtable.size() == 0) {
			System.out.println("PRODUCT ELEMINATED!");
			if(eliminated_products != null && eliminated_products.size() > 0) {
//				productVOList = productRecommend.getProductArray(productVOList, eliminated_products);
				productVOList = productRecommend.getProductVO(cp,productVOList, eliminated_products,master);
				productVO = productVOList[0];
				productModelDTO.setProductId(productVO.getProdId());
				productModelDTO.setUpc(productVO.getUpc());
				productModelDTO.setSku(productVO.getSku());
//				productModelDTO.setProductName(productVO.getProdName());
//				productModelDTO.setBrandId(productVO.getBase());
				productModelDTO.setProductScore("0.0");
				productModelDTO.setScoreMessage(ConstantDeclarations.NOT_GOOD_SCORE);
				productModelDTO.setResponseCode(ConstantDeclarations.ELIMINATION_RULE_CODE);
				productModelDTO.setResponseMessage(productVO.getEliminatedIngrName());
//				productModelDTO.setProductType(productVO.getProdType());
//				productModelDTO.setProductMasterType(productVO.getMasterProdType());
				return productModelDTO;
			}
			else {
				System.out.println("PRODUCT NOT FOUND!");
				productModelDTO.setUpc(productModel.getUpc());
				productModelDTO.setProductScore("0.0");
				productModelDTO.setScoreMessage(ConstantDeclarations.NOT_GOOD_SCORE);
				productModelDTO.setResponseCode(ConstantDeclarations.PROD_NOT_FOUND_CODE);
				productModelDTO.setResponseMessage(ConstantDeclarations.PROD_NOT_FOUND);
				return productModelDTO;
			}
		}
		else {
//			ProductVO prod = (ProductVO) productsHashtable.keySet().toArray()[0];
//			System.out.println("PROD PROS :: " + prod);
			productModelDTO.setProductId(productsHashtable.keySet().toArray()[0].toString());
					
			System.out.println("IN PRODUCT SERVICE IMPL :: BEFORE getScoreForSingleProduct");
			start = System.currentTimeMillis();
			productModelDTO = productRecommend.getScoreForSingleProduct(customerId, prodType, "", prodLine, cp, "",
					master, productModel, productModelDTO);
			end = System.currentTimeMillis();
			
			if(productModelDTO != null) {
				Float score=0.0f;
				if(productModelDTO.getProductScore()!= null && (Float.parseFloat(productModelDTO.getProductScore()) >0.0f)) {
			        score = Float.parseFloat(productModelDTO.getProductScore()) + 10.0f;
				}
				if(score >= 71) {
					productModelDTO.setScoreMessage(ConstantDeclarations.GREAT_SCORE);
					if(score < 100) {
						Float newScore = score;
						productModelDTO.setProductScore(newScore.toString());
					}
					else {
						productModelDTO.setProductScore("100.0");
					}
					
				}
				else if(score >= 61 && score <= 70) {
					productModelDTO.setScoreMessage(ConstantDeclarations.VERY_GOOD_SCORE);
					Float newScore = score;
					productModelDTO.setProductScore(newScore.toString());
				}
				else if(score >= 51 && score <= 60) {
					productModelDTO.setScoreMessage(ConstantDeclarations.GOOD_SCORE);
					Float newScore = score;
					productModelDTO.setProductScore(newScore.toString());
				}
				else if(score >=31 && score <= 50) {
					productModelDTO.setScoreMessage(ConstantDeclarations.JUST_OK_SCORE);
					Float newScore = score;
					productModelDTO.setProductScore(newScore.toString());
				}
				else if(score <= 30) {
					productModelDTO.setScoreMessage(ConstantDeclarations.NOT_GOOD_SCORE);
					if(score > 0) {
						Float newScore = score;
						productModelDTO.setProductScore(newScore.toString());
					}
					else {
						productModelDTO.setProductScore("0.0");
						productModelDTO.setWhyRecommended(null);
					}
				}
			}
			//Added for rule (Deduct 5 points from product with score 98 or more & having caution ingredient/s - Shivam - 26/10/2023 - Start
			if (Objects.nonNull(productModelDTO)
					&& Objects.nonNull(productModelDTO.getProductScore())
					&& Float.parseFloat(productModelDTO.getProductScore()) > 97
					&& Objects.nonNull(productModelDTO.getProductScoreAnalysis().getNotForYourSkin())
					&& !productModelDTO.getProductScoreAnalysis().getNotForYourSkin().isEmpty()) {
				productModelDTO
						.setProductScore(Float.toString(Float.parseFloat(productModelDTO.getProductScore()) - 5));
			}
			//Added for rule (Deduct 5 points from product with score 98 or more & having caution ingredient/s - Shivam - 26/10/2023 - End
			System.out.println("TOTAL SECONDS TAKEN BY productModelDTO :: " + (end-start)/1000 + " Sec + " + (end-start) + " ms" );
			
			
			return productModelDTO;
		}
	}
}
