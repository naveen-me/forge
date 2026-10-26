package com.svb.webservices.serviceImpl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.StringTokenizer;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.svb.analysis.javabeans.PopupRatings;
import com.svb.common.ConstantDeclarations;
import com.svb.common.dba.SVBeautydba;
import com.svb.common.dba.SVDBOperation;
import com.svb.common.valueobjects.CustomerProfileVO;
import com.svb.common.valueobjects.ProductVO;
import com.svb.ratings.javabeans.BumpRating;
import com.svb.ratings.javabeans.EleminateProducts;
import com.svb.ratings.javabeans.ProductBumpRating;
import com.svb.ratings.javabeans.ProductRecommendationEngine;
import com.svb.ratings.javabeans.RecommendationEngine;
import com.svb.ratings.javabeans.RecommendationEngineDebug;
import com.svb.struts.bean.ProductDetailsBean;
import com.svb.struts.service.DetailsService;
import com.svb.webservices.model.HeroIngredientModel;
import com.svb.webservices.model.IngredientModel;
import com.svb.webservices.model.MoreGreatFunctionGenericTextsDTO;
import com.svb.webservices.model.ProductModel;
import com.svb.webservices.model.ProductNotRecommededModel;
import com.svb.webservices.model.ProductScoreAnalysisModel;
import com.svb.webservices.model.RecomondationProductModel;
import com.svb.webservices.model.RecomondedProductResponseModel;
import com.svb.webservices.model.SeeIngredientModel;
import com.svb.webservices.model.SingleProductScoreModel;
import com.svb.webservices.model.SingleProductScoreResponseModel;
import com.svb.webservices.model.WhyRecomondationModel;
import com.svb.webservices.model.topthreeproducts.Top3ProductsList;
import com.svb.webservices.serviceHelper.CleanScoreSection;
import com.svb.webservices.serviceHelper.HeroIngredientsSection;
import com.svb.webservices.serviceHelper.MoreGreatIngredientsSection;
import com.svb.webservices.serviceHelper.NotForYourSkinSection;
import com.svb.webservices.serviceHelper.NotHaveTheseBadIngredientsSection;

public class ProductRecommend extends RecommendationEngine {
	public static final String RATING_CONSTANT = "Not Ideal";
	
	//Added for Elimination reason - Shivam - 26-08-2023 - Start
	//Note: This map is only used for Single product Api
	private List<String> allergicIngredients;
	
	public List<String> getAllergicIngredients() {
		return allergicIngredients;
	}

	public void setAllergicIngredients(List<String> allergicIngredients) {
		this.allergicIngredients = allergicIngredients;
	}
	//Added for Elimination reason - Shivam - 26-08-2023 - End

	private static Log log = LogFactory.getLog(ProductRecommend.class);

	public RecomondedProductResponseModel getRecommnedProduct(String customid, String prodtype, String prodids,
			String prodline, CustomerProfileVO cp, String prodselect, Hashtable<String, String> master,
			RecomondationProductModel productModel, RecomondedProductResponseModel recomondedProductDetailDTO) {
		ArrayList prodList = new ArrayList();
		System.out.println("SKIN CONCERN Size :: " + cp.getSkinConcernList().size());
		String pid = "";
		String prid = "";
		String pmtype = "";
		ProductVO product = new ProductVO();
		Hashtable productsHashtable = new Hashtable();
		Hashtable ingrratings = new Hashtable();
		Hashtable bumpscores = new Hashtable();
		Hashtable scoreWiseRatings = new Hashtable();
		ArrayList<String> productIds = new ArrayList<String>();
		DetailsService service = new DetailsService();
		PopupRatings popRate = new PopupRatings();
		List<ProductModel> cleanserList = new ArrayList<>();
		List<ProductModel> moisturizerList = new ArrayList<>();
		List<ProductModel> dailyTreatmentList = new ArrayList<>();
		List<ProductModel> targetedOneTreatmentList = new ArrayList<>();
		List<ProductModel> tagetedTwoTreatmentList = new ArrayList<>();
		List<ProductModel> weeklyTreatmentList = new ArrayList<>();
		List<ProductModel> atHomeFacialWeeklyTreatmentList = new ArrayList<>();

		List<ProductModel> eyeCareList = new ArrayList<>();
		ArrayList cleanse = new ArrayList();
		ArrayList moisturize = new ArrayList();
		ArrayList treat = new ArrayList();
		ArrayList etreat = new ArrayList();
		ArrayList profiles = new ArrayList();		
		int cleancnt=0;
		int moistcnt=0;
		int treatcnt=0;
		int eyecnt=0;
		int suncnt=0;
		int primercnt=0;	
		ArrayList suncare = new ArrayList();	
		ArrayList primer = new ArrayList();
		ArrayList skinCondition = new ArrayList();	
		ArrayList skinConcern = new ArrayList();
		ProductRecommendationEngine productRec = new ProductRecommendationEngine();
		RecommendationEngineDebug recEngDebug = new RecommendationEngineDebug();
		try {
			cp = (CustomerProfileVO) getCustomerProfile(customid, cp, "skin");
			ingrratings = getIngredientsRatingsAboveZero(cp, false);
			String[] skinSelection = productModel.getSkin_concern();
			prodList = getProductData(cp, ingrratings, productRec, prodline, prodselect, prodtype,skinSelection,"");
			//prodList = productRec.getCustomerReviewStars(prodList);
			System.out.println("SKIN CONCERN Size :: " + cp.getSkinConcernList().size());
			cleanse = (ArrayList) prodList.get(0);
			moisturize = (ArrayList) prodList.get(1);
			treat = (ArrayList) prodList.get(2);
			etreat = (ArrayList) prodList.get(3);
			suncare = (ArrayList) prodList.get(4);
			//primer = (ArrayList) prodList.get(10);
			
			productIds = getProductIds((ArrayList)cleanse.get(0), (ArrayList)moisturize.get(0), (ArrayList)treat.get(0), (ArrayList)etreat.get(0), (ArrayList)suncare.get(0));
//			HashMap ingredientsWithGlossary = service.getIngredientNamesWithGlossary();
			HashMap ingredientsWithGlossary = service.getIngredientNamesWithGlossaryNew("");
			bumpscores = (Hashtable) recEngDebug.getBumpScores();
			profiles = getProfile(cp);
//			Hashtable funcnames = popRate.getFunctionDetailsHT();
//			Hashtable<String,String[][]> funcnames = popRate.getFunctionDetailsHT();
			Hashtable mastertwo = productRec.getMasterDataTwo();
//			List<String> bodyAttributeList = new ArrayList<>();
//			bodyAttributeList.add(cp.getSkinType());
//			bodyAttributeList.addAll(cp.getSkinConditionList());
//			bodyAttributeList.addAll(cp.getSkinConcernList());
//			Hashtable<String,String> functext = popRate.getFunctionAttributeText(bodyAttributeList);
//			TreeMap<String, List<String>> nonCleanGroupIngredientIds = new TreeMap<String, List<String>>();
//			Hashtable<String,List<IngredientNameModel>> nonCleanGroup = popRate.getNonCleanGroupIngredients(nonCleanGroupIngredientIds);	
//			TreeMap<String,DoesContainModel> nonCleanGroupDetail = popRate.getNonCleanGroupAllDetails(nonCleanGroup);

			Top3ProductsList top3ProductsList = new Top3ProductsList();
			boolean[] topCleanserProductsFlagsForEveryCategory = new boolean[4];
			boolean[] topMoisturizerProductsFlagsForEveryCategory = new boolean[4];
			boolean[] topDailyTreatmentProductsFlagsForEveryCategory = new boolean[6];
			boolean[] topTargetedOneTreatmentProductsFlagsForEveryCategory = new boolean[6];
			boolean[] topTargetedTwoTreatmentProductsFlagsForEveryCategory = new boolean[6];
			boolean[] topWeeklyTreatmentProductsFlagsForEveryCategory = new boolean[3];
			boolean[] topAtHomeFacialWeeklyTreatmentProductsFlagsForEveryCategory = new boolean[3];
			boolean[] topEyeCareProductsFlagsForEveryCategory = new boolean[2];
			if (productIds != null && productIds.size() > 0) {
				for (int i = 0; i < productIds.size(); i++) {
					pid = (String) productIds.get(i);
					prid = pid;
					pid = "('" + pid + "')";
					productsHashtable = (Hashtable) getBeautyProductInfo(pid); // gets the product
//					productsHashtable = recEngDebug.getProductsIngredients("", "prodId", "", productsHashtable, pid);
					product = (ProductVO) productsHashtable.get(prid);
					String[][] productIngredient = product.getIngredients();
					product = (ProductVO) recEngDebug.ratingsIndividual(cp, product, ingrratings);
					ProductVO pro = new ProductVO();
//					List<WhyRecomondationModel> whyRecomondationModelList = new ArrayList<>();
//					ProductScoreAnalysisModel productScoreAnalysisModel = new ProductScoreAnalysisModel();
					boolean flag = false;
					for (int j = 0; j < profiles.size(); j++) {
						String profileAtt = (String) profiles.get(j);
						String ptype = product.getProdType();
						pmtype = product.getMasterProdType();
						Hashtable pBaseRatings = product.getProfileBaseRatings();
						Hashtable pIngrRatings = product.getProfileIngrRatings();
						int pbasescore = 0;
						int pbasecount = 0;
						int pbasepercen = 0;
						int pingrscore = 0;
						int pingrcount = 0;
						int pingrpercen = 0;
						String bumpkey = profileAtt + ptype;
						String bscore = (String) bumpscores.get(bumpkey);
						if (pBaseRatings.get(profileAtt) != null) {
							String pbasescorestring = (String) pBaseRatings.get(profileAtt);
							StringTokenizer stp = new StringTokenizer(pbasescorestring, "::");
							String stps = stp.nextToken();
							String stpc = stp.nextToken();
							pbasescore = Integer.parseInt(stps);
							pbasecount = Integer.parseInt(stpc);
						}
						if (pIngrRatings.get(profileAtt) != null) {
							String pingrscorestring = (String) pIngrRatings.get(profileAtt);
							StringTokenizer stp = new StringTokenizer(pingrscorestring, "::");
							String stps = stp.nextToken();
							String stpc = stp.nextToken();
							pingrscore = Integer.parseInt(stps);
							pingrcount = Integer.parseInt(stpc);
						}
						float ingrpercentage = 0.0f;
						float basepercentage = 0.0f;
						if (pingrcount != 0)
							pingrpercen = 100 * pingrscore / (pingrcount * 10);
						if (pbasecount != 0)
							pbasepercen = 100 * pbasescore / (pbasecount * 10);
						float pingrpercentage = Float.parseFloat(String.valueOf(pingrpercen));
						float pbasepercentage = Float.parseFloat(String.valueOf(pbasepercen));
						float score = 0.0f;
						float bumpscore = 0.0f;
						if (bscore != null && bscore.length() != 0)
							bumpscore = Float.parseFloat(String.valueOf(bscore));
						if (pmtype.equals("0004")) {
							ingrpercentage = (pingrpercentage * 100f) / 100f;
							score = ingrpercentage;
						} else {
							if (pbasepercentage == 0.0f) {
								ingrpercentage = (pingrpercentage * 100f) / 100f;
								score = ingrpercentage;
							} else {
								basepercentage = (pbasepercentage * 20f) / 100f;
								ingrpercentage = (pingrpercentage * 80f) / 100f;
								score = ingrpercentage + basepercentage;
							}
						}
						score = score + bumpscore;
						scoreWiseRatings.put(profileAtt, "" + score);
					
						// START : ADDED By Synoptek::HARESH: Changes loop for Regimen wise 
																	
						if(!flag) {
							//Updating below conditions to Display top 3 products in each category - Shivam - 15/02/2024 - Start
							if(ConstantDeclarations.PRODUCT_MASTER_TYPE_CLEANSE.equals(pmtype)) {
								pro = (ProductVO) ((ArrayList)cleanse.get(0)).get(cleancnt);
								flag=true;
								cleancnt++;
							} else if(ConstantDeclarations.PRODUCT_MASTER_TYPE_MOIST.equals(pmtype)) {
								pro = (ProductVO) ((ArrayList)moisturize.get(0)).get(moistcnt);
								flag=true;
								moistcnt++;
							} else if(ConstantDeclarations.PRODUCT_MASTER_TYPE_TREAT.equals(pmtype)) {
								pro = (ProductVO) ((ArrayList)treat.get(0)).get(treatcnt);
								flag=true;
								treatcnt++;
							} else if(ConstantDeclarations.PRODUCT_MASTER_TYPE_ETREAT.equals(pmtype)) {
								pro = (ProductVO) ((ArrayList)etreat.get(0)).get(eyecnt);
								flag=true;
								eyecnt++;
							} else if(ConstantDeclarations.PRODUCT_MASTER_TYPE_UV.equals(pmtype)) {
								pro = (ProductVO) ((ArrayList)suncare.get(0)).get(suncnt);
								flag=true;
								suncnt++;
							} else if(primercnt < 1 && ConstantDeclarations.PRODUCT_MASTER_TYPE_PRIMER.equals(pmtype)) {
								pro = (ProductVO)((ArrayList)primer.get(0)).get(primercnt);
								flag=true;
								primercnt++;
							}
							//Updating below conditions to Display top 3 products in each category - Shivam - 15/02/2024 - End
						} 
						// END : ADDED By Synoptek::HARESH : Changes loop for Regimen wise 
						
						/** Get result after second time recommendation check */
//						WhyRecomondationModel whyRecomondationModel = new WhyRecomondationModel();
//						if (scoreWiseRatings != null && !scoreWiseRatings.isEmpty()
//								&& !getproductRating(Float.valueOf(scoreWiseRatings.get(profileAtt).toString()))
//										.equals(RATING_CONSTANT)
//								&& master != null && !master.isEmpty()) {
//							System.out.println("profileAtt :: " + profileAtt);
//							whyRecomondationModel.setSkinConcern(master.get(profileAtt).toString());
//							System.out.println("master.get(profileAtt).toString() :: " + master.get(profileAtt).toString());
//							whyRecomondationModel.setProductRating(
//									getproductRating(Float.valueOf(scoreWiseRatings.get(profileAtt).toString())));
//							System.out.println("funcnames :: " + funcnames);
//							System.out.println("mastertwo :: " + mastertwo);
//							System.out.println("popRate :: " + popRate);
//							System.out.println("prid :: " + prid);
//							System.out.println("profileAtt :: " + profileAtt);
//							whyRecomondationModel.setActiveIngredient(
//									getSeeIngredientModel(funcnames, mastertwo, popRate, prid, profileAtt));
//
//							whyRecomondationModelList.add(whyRecomondationModel);
//						}
					}
					
//					long start1,end1;
					
//					start1 = System.currentTimeMillis();
//					HashMap ingredientsWithGlossary = service.getIngredientNamesWithGlossaryNew(pro.getProdId());
//					end1 = System.currentTimeMillis();
//					System.out.println("TOTAL SECONDS TAKEN BY ingredientsWithGlossary :: " + (end1-start1)/1000 + " Sec + " + (end1-start1) + " ms" );
					
//					List<String> ingredientGroupFunction = new ArrayList<>();
//					Hashtable<String,Boolean> groupFunctionFlagList = new Hashtable<>();
//					Hashtable<String, List<String>> groupFunctionAttributes = new Hashtable<>();
//					Hashtable<String, NotHaveTheseBadIngredientsListModel> groupFunctionAllDetails = popRate.getAllGroupFunctionDetailsWithFlag(ingredientGroupFunction,groupFunctionFlagList,product.getMasterProdType(),bodyAttributeList,groupFunctionAttributes);
//					
//
//					ProductDetailsBean ingredientDetail = service.getProductDetails(pro.getProdId());
//					Hashtable<String,String> ingredientsName = ingredientDetail.getIngredientsName();
//					Hashtable<String,List<String>> ingredient = pro.getIndividualingrratings();
//					Hashtable<String,String> funcingr = popRate.getIngredientFunction(pro.getProdId());
////					Hashtable<String,String[][]> funcnames = popRate.getFunctionDetailsHT();
//					Hashtable<String,List<String>> remainIngredients = new Hashtable<>();
//
//					start1 = System.currentTimeMillis();
//					HeroIngredientsSection heroIngredientsSection = new HeroIngredientsSection();
//					productScoreAnalysisModel.setHeroIngredients(heroIngredientsSection.getHeroIngredients(ingredient,ingredientsWithGlossary,master,productModel,ingredientsName,funcingr,funcnames));	
//					end1 = System.currentTimeMillis();
//					System.out.println("TOTAL SECONDS TAKEN BY HeroIngredients :: " + (end1-start1)/1000 + " Sec + " + (end1-start1) + " ms" );
//					
//					start1 = System.currentTimeMillis();
//					NotHaveTheseBadIngredientsSection notHaveTheseBadIngredientsSection = new NotHaveTheseBadIngredientsSection();
//					productScoreAnalysisModel.setNotHaveTheseBadIngredients(notHaveTheseBadIngredientsSection.getNotHaveTheseBadIngredients(ingredientGroupFunction,groupFunctionAllDetails,groupFunctionAttributes,groupFunctionFlagList,ingredient,remainIngredients,master,cp.getSkinType()));
//					end1 = System.currentTimeMillis();
//					System.out.println("TOTAL SECONDS TAKEN BY NotHaveTheseBadIngredients :: " + (end1-start1)/1000 + " Sec + " + (end1-start1) + " ms" );
//					
//					start1 = System.currentTimeMillis();
//					if(!remainIngredients.isEmpty() && remainIngredients != null) {
//						NotForYourSkinSection notForYourSkinSection = new NotForYourSkinSection();
//						List<String> ingredientIdList = new ArrayList<>(remainIngredients.keySet());
//						Hashtable<String,String> cautionHashtable = popRate.getCautionText(bodyAttributeList,ingredientIdList);
//						productScoreAnalysisModel.setNotForYourSkin(notForYourSkinSection.getNotForYourSkin(remainIngredients,ingredientsName,cautionHashtable));
//					}
//					else {
//						productScoreAnalysisModel.setNotForYourSkin(null);
//					}
//					end1 = System.currentTimeMillis();
//					System.out.println("TOTAL SECONDS TAKEN BY NotForYourSkin :: " + (end1-start1)/1000 + " Sec + " + (end1-start1) + " ms" );
//					
//					
//					start1 = System.currentTimeMillis();
//					MoreGreatIngredientsSection moreGreatIngredientsSection = new MoreGreatIngredientsSection();
//					productScoreAnalysisModel.setMoreGreatIngredients(moreGreatIngredientsSection.getMoreGreatIngredients(ingredientsName,ingredient,funcingr,funcnames,functext));
//					end1 = System.currentTimeMillis();
//					System.out.println("TOTAL SECONDS TAKEN BY MoreGreatIngredients :: " + (end1-start1)/1000 + " Sec + " + (end1-start1) + " ms" );
//
//					start1 = System.currentTimeMillis();
//					CleanScoreSection cleanScoreSection = new CleanScoreSection();
//					productScoreAnalysisModel.setCleanScore(cleanScoreSection.getCleanScore(ingredientDetail,popRate,nonCleanGroupIngredientIds,nonCleanGroup,ingredientsName));
//					end1 = System.currentTimeMillis();
//					System.out.println("TOTAL SECONDS TAKEN BY CleanScore :: " + (end1-start1)/1000 + " Sec + " + (end1-start1) + " ms" );

					
					ProductModel prodModel = new ProductModel();
//					ProductDetailsBean ingredientDetail = service.getProductDetails(pro.getProdId());
					String catgorySubCategory[] = service.getProductCategorySubCategory(pro.getProdId());
					prodModel.setProductId(pro.getProdId());
					prodModel.setSku(pro.getSku());
					prodModel.setUpc(pro.getUpc());
					prodModel.setProductName(pro.getProdName());
					prodModel.setBrandId(pro.getProductLine());
					prodModel.setProductType(catgorySubCategory[1]);
					// Get brand name for product recommendation api - Deep - Start
					prodModel.setBrandName(pro.getProductLineName());
					// Get brand name for product recommendation api - Deep - End
					
					
					// Add/Deduct Scores as per Business Rule Changes - Shivam - 04/10/2024 - Start
					ProductRecommend.addDeductScoresAsPerBusinessRules(pro);
					Float score  = Float.parseFloat(pro.getScore())+ 10.0f;
					// Add/Deduct Scores as per Business Rule Changes - Shivam - 04/10/2024 - End
					
					if (score < 100) {
						prodModel.setProductScore(score.toString());
					}
					else {
						prodModel.setProductScore("100.0");
					}
					
//					prodModel.setProductScore(pro.getScore());
					prodModel.setProductMasterType(catgorySubCategory[0]);
					prodModel.setWhenToUse(pro.getWhenToUse());
					prodModel.setMultiBenefits(pro.getMultiBenefits());
					prodModel.setLifeStyleBasedRecs1(pro.getLifeStyleBasedRecs1());
					prodModel.setLifeStyleBasedRecs2(pro.getLifeStyleBasedRecs2());
					// Get RX flag from the sv_product_rx_warning for ingredients in the product - Start
					prodModel.setFlagRXAcne(pro.getFlagAcne());
					prodModel.setFlagRXRetinol(pro.getFlagRetinolt());
					prodModel.setFlagTropicalSteriods(pro.getFlagSteriods());
					// Get RX flag from the sv_product_rx_warning for ingredients in the product - End
					
					//Added to display RX Warning Text - Shivam - 20/12/2023 - Start
					if(Objects.nonNull(pro.getRxWarningText())
							&& pro.getRxWarningText().length() > 0) {
						prodModel.setRxWarningText(pro.getRxWarningText());
					}
					//Added to display RX Warning Text - Shivam - 20/12/2023 - End
					
//					prodModel.setWhyRecommended(whyRecomondationModelList);
//					prodModel.setProductScoreAnalysis(productScoreAnalysisModel);
//					ArrayList<IngredientModel> ingredientList = new ArrayList<>();
//					if (pro.getIngredients() != null && pro.getIngredients().length > 0 && ingredientDetail != null
//							&& !ingredientDetail.getIngredientIdList().isEmpty()) {
//						for (int k = 0; k < pro.getIngredients().length; k++) {
//							IngredientModel ingredientModel = new IngredientModel();
//							ingredientModel.setIngredientId(ingredientDetail.getIngredientIdList().get(k).toString());
//							ingredientModel.setIngredientName(ingredientDetail.getIngredients().get(k).toString().trim());
//							ingredientModel.setDisplayOrder(ingredientDetail.getDisplayOrder().get(k).toString());
//							if (ingredientsWithGlossary.get(ingredientDetail.getIngredients().get(k).toString().trim().toUpperCase()) != null) {
//								ingredientModel.setIngredientDefination((String)ingredientsWithGlossary.get(ingredientDetail.getIngredients().get(k).toString().trim().toUpperCase()));
//							} else {
//								ingredientModel.setIngredientDefination("");
//							}
//							ingredientList.add(ingredientModel);
//						}
//						prodModel.setIngredients(ingredientList);
//					}
					prodModel.setWarningText(getWarningTextNew(pro,cp,master,mastertwo));
					
					//Updating Below Conditions for displaying top 3 products in each category - Shivam - 21/02/2024 - Start
					
					if (pmtype.equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_CLEANSE)) {
						if (pro.getProdType().equals(ConstantDeclarations.PRECLEANSE)) {
							if(!topCleanserProductsFlagsForEveryCategory[0]) {
								cleanserList.add(prodModel);
								topCleanserProductsFlagsForEveryCategory[0] = true;
							}
							top3ProductsList.getCleanserProductsList().getPreCleanseList().add(prodModel);
						} else if (pro.getProdType().equals(ConstantDeclarations.FWASHCLEANSE)) {
							if(!topCleanserProductsFlagsForEveryCategory[1]) {
								cleanserList.add(prodModel);
								topCleanserProductsFlagsForEveryCategory[1] = true;
							}
							top3ProductsList.getCleanserProductsList().getFaceWashAndCleansersList().add(prodModel);
						} else if (pro.getProdType().equals(ConstantDeclarations.TONMIST)) {
							if(!topCleanserProductsFlagsForEveryCategory[2]) {
								cleanserList.add(prodModel);
								topCleanserProductsFlagsForEveryCategory[2] = true;
							}
							top3ProductsList.getCleanserProductsList().getTonersAndMistsList().add(prodModel);
						} else if (pro.getProdType().equals(ConstantDeclarations.FACIAL_WIPES)) {
							if(!topCleanserProductsFlagsForEveryCategory[3]) {
								cleanserList.add(prodModel);
								topCleanserProductsFlagsForEveryCategory[3] = true;
							}
							top3ProductsList.getCleanserProductsList().getFacialWipesList().add(prodModel);
						}
					} else if (pmtype.equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_MOIST)
							|| pmtype.equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_UV)
							|| pmtype.equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_PRIMER)) {
						if (Objects.nonNull(pro.getWhenToUse())
								&& pro.getWhenToUse().equals(ConstantDeclarations.WHEN_TO_USE_DLYMOI)
								&& Objects.nonNull(pro.getMultiBenefits())
								&& pro.getMultiBenefits().equals(ConstantDeclarations.MULBENIFIT_NOSPF)) {
							if (!topMoisturizerProductsFlagsForEveryCategory[0]) {
								topMoisturizerProductsFlagsForEveryCategory[0] = true;
								moisturizerList.add(prodModel);
							}
							top3ProductsList.getMoisturizerProductsList().getMoisturizerProductsWithoutSPFList()
									.add(prodModel);
						} else if (Objects.nonNull(pro.getWhenToUse())
								&& pro.getWhenToUse().equals(ConstantDeclarations.WHEN_TO_USE_DLYMOISPF)
								&& Objects.nonNull(pro.getMultiBenefits())
								&& pro.getMultiBenefits().equals(ConstantDeclarations.MULBENIFIT_HASSPF)) {
							if (!topMoisturizerProductsFlagsForEveryCategory[1]) {
								topMoisturizerProductsFlagsForEveryCategory[1] = true;
								moisturizerList.add(prodModel);
							}
							top3ProductsList.getMoisturizerProductsList().getMoisturizerProductsWithSPFList()
									.add(prodModel);
						} else if (Objects.nonNull(pro.getWhenToUse())
								&& pro.getWhenToUse().equals(ConstantDeclarations.WHEN_TO_USE_NGHTLYMOI)) {
							if (!topMoisturizerProductsFlagsForEveryCategory[2]) {
								topMoisturizerProductsFlagsForEveryCategory[2] = true;
								moisturizerList.add(prodModel);
							}
							top3ProductsList.getMoisturizerProductsList().getNightlyMoisturizerProductsList()
									.add(prodModel);
						} else if (pro.getProdType().equals(ConstantDeclarations.FCSUNSCREEN)) {
							if (!topMoisturizerProductsFlagsForEveryCategory[3]) {
								topMoisturizerProductsFlagsForEveryCategory[3] = true;
								moisturizerList.add(prodModel);
							}
							top3ProductsList.getMoisturizerProductsList().getFacialSunscreensProductsList()
									.add(prodModel);
						}
					} else if (pmtype.equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_TREAT)) {
						if (pro.getComments().equalsIgnoreCase(ConstantDeclarations.DAILY)) {
							//......topDailyTreatmentProductsFlagsForEveryCategory
							if(pro.getProdType().equals(ConstantDeclarations.REDNESS)) {
								if(!topDailyTreatmentProductsFlagsForEveryCategory[0]) {
									dailyTreatmentList.add(prodModel);
									topDailyTreatmentProductsFlagsForEveryCategory[0] = true;
								}
								top3ProductsList.getDailyTreatmentProductsList().getRednessProductsList().add(prodModel);
							} else if(pro.getProdType().equals(ConstantDeclarations.ACNE)) {
								if(!topDailyTreatmentProductsFlagsForEveryCategory[1]) {
									dailyTreatmentList.add(prodModel);
									topDailyTreatmentProductsFlagsForEveryCategory[1] = true;
								}
								top3ProductsList.getDailyTreatmentProductsList().getAcneProductsList().add(prodModel);
							} else if(pro.getProdType().equals(ConstantDeclarations.AGESPOTSSUNDAMAGE)) {
								if(!topDailyTreatmentProductsFlagsForEveryCategory[2]) {
									dailyTreatmentList.add(prodModel);
									topDailyTreatmentProductsFlagsForEveryCategory[2] = true;
								}
								top3ProductsList.getDailyTreatmentProductsList().getAgeSpotsAndSunDamageProductsList().add(prodModel);
							} else if(pro.getProdType().equals(ConstantDeclarations.SERUM)) {
								if(!topDailyTreatmentProductsFlagsForEveryCategory[3]) {
									dailyTreatmentList.add(prodModel);
									topDailyTreatmentProductsFlagsForEveryCategory[3] = true;
								}
								top3ProductsList.getDailyTreatmentProductsList().getSerumProductsList().add(prodModel);
							} else if(pro.getProdType().equals(ConstantDeclarations.EXFOLIATORS)) {
								if(!topDailyTreatmentProductsFlagsForEveryCategory[4]) {
									dailyTreatmentList.add(prodModel);
									topDailyTreatmentProductsFlagsForEveryCategory[4] = true;
								}
								top3ProductsList.getDailyTreatmentProductsList().getExfoliatorProductsList().add(prodModel);
							} else if(pro.getProdType().equals(ConstantDeclarations.SERUMSHYDRATOR)) {
								if(!topDailyTreatmentProductsFlagsForEveryCategory[5]) {
									dailyTreatmentList.add(prodModel);
									topDailyTreatmentProductsFlagsForEveryCategory[5] = true;
								}
								top3ProductsList.getDailyTreatmentProductsList().getSerumHydratorProductsList().add(prodModel);
							}
						}
						else if (pro.getComments().equalsIgnoreCase(ConstantDeclarations.TARGETEDONE)) {
							if(pro.getProdType().equals(ConstantDeclarations.DECNECK)) {
								if(!topTargetedOneTreatmentProductsFlagsForEveryCategory[0]) {
									targetedOneTreatmentList.add(prodModel);
									topTargetedOneTreatmentProductsFlagsForEveryCategory[0] = true;
								}
								top3ProductsList.getTargetedOneTreatmentProductsList().getDecolleteAndNeckProductsList().add(prodModel);
							}else if(pro.getProdType().equals(ConstantDeclarations.ACNESPOT)) {
								if(!topTargetedOneTreatmentProductsFlagsForEveryCategory[1]) {
									targetedOneTreatmentList.add(prodModel);
									topTargetedOneTreatmentProductsFlagsForEveryCategory[1] = true;
								}
								top3ProductsList.getTargetedOneTreatmentProductsList().getAcneSpotTreatmentProductsList().add(prodModel);
							}else if(pro.getProdType().equals(ConstantDeclarations.AGESPOTSSUNDAMAGE)) {
								if(!topTargetedOneTreatmentProductsFlagsForEveryCategory[2]) {
									targetedOneTreatmentList.add(prodModel);
									topTargetedOneTreatmentProductsFlagsForEveryCategory[2] = true;
								}
								top3ProductsList.getTargetedOneTreatmentProductsList().getAgeSpotsSunDamageProductsList().add(prodModel);
							}else if(pro.getProdType().equals(ConstantDeclarations.REDNESS)) {
								if(!topTargetedOneTreatmentProductsFlagsForEveryCategory[3]) {
									targetedOneTreatmentList.add(prodModel);
									topTargetedOneTreatmentProductsFlagsForEveryCategory[3] = true;
								}
								top3ProductsList.getTargetedOneTreatmentProductsList().getRednessProductsList().add(prodModel);
							}else if(pro.getProdType().equals(ConstantDeclarations.SERUM_RETINOL)) {
								if(!topTargetedOneTreatmentProductsFlagsForEveryCategory[4]) {
									targetedOneTreatmentList.add(prodModel);
									topTargetedOneTreatmentProductsFlagsForEveryCategory[4] = true;
								}
								top3ProductsList.getTargetedOneTreatmentProductsList().getSerumsRetinolProductsList().add(prodModel);
							}else if(pro.getProdType().equals(ConstantDeclarations.SERUMSVITAMINC)) {
								if(!topTargetedOneTreatmentProductsFlagsForEveryCategory[5]) {
									targetedOneTreatmentList.add(prodModel);
									topTargetedOneTreatmentProductsFlagsForEveryCategory[5] = true;
								}
								top3ProductsList.getTargetedOneTreatmentProductsList().getSerumsVitaminCProductsList().add(prodModel);
							}
						}
						else if (pro.getComments().equalsIgnoreCase(ConstantDeclarations.TARGETEDTWO)) {
							if (pro.getProdType().equals(ConstantDeclarations.DECNECK)) {
								if (!topTargetedTwoTreatmentProductsFlagsForEveryCategory[0]) {
									tagetedTwoTreatmentList.add(prodModel);
									topTargetedTwoTreatmentProductsFlagsForEveryCategory[0] = true;
								}
								top3ProductsList.getTargetedTwoTreatmentProductsList().getDecolleteAndNeckProductsList()
										.add(prodModel);
							} else if (pro.getProdType().equals(ConstantDeclarations.ACNESPOT)) {
								if (!topTargetedTwoTreatmentProductsFlagsForEveryCategory[1]) {
									tagetedTwoTreatmentList.add(prodModel);
									topTargetedTwoTreatmentProductsFlagsForEveryCategory[1] = true;
								}
								top3ProductsList.getTargetedTwoTreatmentProductsList()
										.getAcneSpotTreatmentProductsList().add(prodModel);
							} else if (pro.getProdType().equals(ConstantDeclarations.AGESPOTSSUNDAMAGE)) {
								if (!topTargetedTwoTreatmentProductsFlagsForEveryCategory[2]) {
									tagetedTwoTreatmentList.add(prodModel);
									topTargetedTwoTreatmentProductsFlagsForEveryCategory[2] = true;
								}
								top3ProductsList.getTargetedTwoTreatmentProductsList()
										.getAgeSpotsSunDamageProductsList().add(prodModel);
							} else if (pro.getProdType().equals(ConstantDeclarations.REDNESS)) {
								if (!topTargetedTwoTreatmentProductsFlagsForEveryCategory[3]) {
									tagetedTwoTreatmentList.add(prodModel);
									topTargetedTwoTreatmentProductsFlagsForEveryCategory[3] = true;
								}
								top3ProductsList.getTargetedTwoTreatmentProductsList().getRednessProductsList()
										.add(prodModel);
							}else if(pro.getProdType().equals(ConstantDeclarations.SERUM_RETINOL)) {
								if(!topTargetedTwoTreatmentProductsFlagsForEveryCategory[4]) {
									tagetedTwoTreatmentList.add(prodModel);
									topTargetedTwoTreatmentProductsFlagsForEveryCategory[4] = true;
								}
								top3ProductsList.getTargetedTwoTreatmentProductsList().getSerumsRetinolProductsList().add(prodModel);
							}else if(pro.getProdType().equals(ConstantDeclarations.SERUMSVITAMINC)) {
								if(!topTargetedTwoTreatmentProductsFlagsForEveryCategory[5]) {
									tagetedTwoTreatmentList.add(prodModel);
									topTargetedTwoTreatmentProductsFlagsForEveryCategory[5] = true;
								}
								top3ProductsList.getTargetedTwoTreatmentProductsList().getSerumsVitaminCProductsList().add(prodModel);
							}
						}
						else if (pro.getComments().equalsIgnoreCase(ConstantDeclarations.WEEKLY)) {
							if (pro.getProdType().equals(ConstantDeclarations.EXFOLIATORS)) {
								if (!topWeeklyTreatmentProductsFlagsForEveryCategory[0]) {
									weeklyTreatmentList.add(prodModel);
									topWeeklyTreatmentProductsFlagsForEveryCategory[0] = true;
								}
								top3ProductsList.getWeeklyTreatmentProductsList()
										.getWeeklyTreatmentExfoliatorProductsList().add(prodModel);
							} else if (pro.getProdType().equals(ConstantDeclarations.MASKS)) {
								if (!topWeeklyTreatmentProductsFlagsForEveryCategory[1]) {
									weeklyTreatmentList.add(prodModel);
									topWeeklyTreatmentProductsFlagsForEveryCategory[1] = true;
								}
								top3ProductsList.getWeeklyTreatmentProductsList().getWeeklyTreatmentMasksProductsList()
										.add(prodModel);
							} else if (pro.getProdType().equals(ConstantDeclarations.PEELS)) {
								if (!topWeeklyTreatmentProductsFlagsForEveryCategory[2]) {
									weeklyTreatmentList.add(prodModel);
									topWeeklyTreatmentProductsFlagsForEveryCategory[2] = true;
								}
								top3ProductsList.getWeeklyTreatmentProductsList().getWeeklyTreatmentPeelsProductsList()
										.add(prodModel);
							}
						}
						else if (pro.getComments().equalsIgnoreCase(ConstantDeclarations.ATHOMEWEEKLY)) {
							if (pro.getProdType().equals(ConstantDeclarations.EXFOLIATORS)) {
								if (!topAtHomeFacialWeeklyTreatmentProductsFlagsForEveryCategory[0]) {
									atHomeFacialWeeklyTreatmentList.add(prodModel);
									topAtHomeFacialWeeklyTreatmentProductsFlagsForEveryCategory[0] = true;
								}
								top3ProductsList.getAtHomeFacialWeeklyTreatmentProductsList()
										.getAtHomeWeeklyTreatmentExfoliatorProductsList().add(prodModel);
							} else if (pro.getProdType().equals(ConstantDeclarations.MASKS)) {
								if (!topAtHomeFacialWeeklyTreatmentProductsFlagsForEveryCategory[1]) {
									atHomeFacialWeeklyTreatmentList.add(prodModel);
									topAtHomeFacialWeeklyTreatmentProductsFlagsForEveryCategory[1] = true;
								}
								top3ProductsList.getAtHomeFacialWeeklyTreatmentProductsList()
										.getAtHomeWeeklyTreatmentMasksProductsList().add(prodModel);
								;
							} else if (pro.getProdType().equals(ConstantDeclarations.PEELS)) {
								if (!topAtHomeFacialWeeklyTreatmentProductsFlagsForEveryCategory[2]) {
									atHomeFacialWeeklyTreatmentList.add(prodModel);
									topAtHomeFacialWeeklyTreatmentProductsFlagsForEveryCategory[2] = true;
								}
								top3ProductsList.getAtHomeFacialWeeklyTreatmentProductsList()
										.getAtHomeWeeklyTreatmentPeelsProductsList().add(prodModel);
							}
						}
					} else {
						if (pro.getProdType().equals(ConstantDeclarations.EYETREATSERUM)) {
							if (!topEyeCareProductsFlagsForEveryCategory[0]) {
								eyeCareList.add(prodModel);
								topEyeCareProductsFlagsForEveryCategory[0] = true;
							}
							top3ProductsList.getEyeProductsList().getEyeTreatmentAndSerumProductsList().add(prodModel);
						} else if (pro.getProdType().equals(ConstantDeclarations.EYEMASK)) {
							if (!topEyeCareProductsFlagsForEveryCategory[1]) {
								eyeCareList.add(prodModel);
								topEyeCareProductsFlagsForEveryCategory[1] = true;
							}
							top3ProductsList.getEyeProductsList().getEyeMasksProductsList().add(prodModel);
						}
					}
					//Updating Above Conditions for displaying top 3 products in each category - Shivam - 21/02/2024 - End
				}
			}
			
			recomondedProductDetailDTO.setCleanserList(cleanserList);
			recomondedProductDetailDTO.setMoisturizerList(moisturizerList);
			recomondedProductDetailDTO.setDailyTreatmentList(dailyTreatmentList);
			recomondedProductDetailDTO.setTargetedOneTreatmentList(targetedOneTreatmentList);
			recomondedProductDetailDTO.setTargetedTwoTreatmentList(tagetedTwoTreatmentList);
			recomondedProductDetailDTO.setWeeklyTreatmentList(weeklyTreatmentList);
			recomondedProductDetailDTO.setAtHomeFacialWeeklyTreatmentList(atHomeFacialWeeklyTreatmentList);
			recomondedProductDetailDTO.setEyeProductList(eyeCareList);
			recomondedProductDetailDTO.setTop3ProductsList(top3ProductsList);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return recomondedProductDetailDTO;
	}

	private String getWarningText(ProductVO pro, CustomerProfileVO cp, Hashtable<String, String> master) {
		String warningText = "";
		ArrayList warnText = new ArrayList<>();
		Hashtable prods = new Hashtable() ;
		prods.put(pro.getProdId().trim(), pro) ;
		warnText = getDynamicText(prods,cp,master);
		if (warnText.size() > 0) {
			for (int i = 0; i < warnText.size(); i=i+2) {
				if(i==0)
					warningText = ((String) warnText.get(i));
				else
					warningText = warningText.concat("<br><br>").concat(((String) warnText.get(i)));	
			}
		}
		return warningText;
	}
	
	private String getWarningTextNew(ProductVO pro, CustomerProfileVO cp, Hashtable<String, String> master,Hashtable mastertwo) {
		String warningText = "";
		ArrayList warnText = new ArrayList<>();
		Hashtable prods = new Hashtable() ;
		prods.put(pro.getProdId().trim(), pro) ;
		warnText = getDynamicTextNew(prods,cp,master,mastertwo);
		if (warnText.size() > 0) {
			for (int i = 0; i < warnText.size(); i=i+2) {
				if(i==0)
					warningText = ((String) warnText.get(i));
				else
					warningText = warningText.concat("<br><br>").concat(((String) warnText.get(i)));	
			}
		}
		return warningText;
	}


	private ArrayList<String> getProfile(CustomerProfileVO cp) {
		String skintype = cp.getSkinType();
		String age = cp.getAge();
		ArrayList skincondition = cp.getSkinConditionList();
		ArrayList skinconcern = cp.getSkinConcernList();
		ArrayList eyeconcern = cp.getEyeConcernList();
		ArrayList specification = cp.getSpecification();

		ArrayList profiles = new ArrayList();
		profiles.add(skintype);
		for (int i = 0; i < skincondition.size(); i++) {
			String scond = (String) skincondition.get(i);
			profiles.add(scond);
		}
		for (int i = 0; i < skinconcern.size(); i++) {
			String sconc = (String) skinconcern.get(i);
			profiles.add(sconc);
		}
		for (int i = 0; i < eyeconcern.size(); i++) {
			String econc = (String) eyeconcern.get(i);
			if (econc.equals("E999"))
				break;
			profiles.add(econc);
		}
		for (int i = 0; i < specification.size(); i++) {
			String speci = (String) specification.get(i);
			profiles.add(speci);
		}
		return profiles;
	}

	private ArrayList<String> getProductIds(ArrayList cleanse, ArrayList moisturize, ArrayList treat,
			ArrayList etreat, ArrayList suncare) {
		ArrayList<String> productIds = new ArrayList<String>();
		if(cleanse != null && cleanse.size() > 0) {
			for (int i = 0; i < cleanse.size(); i++) {
				ProductVO prod = (ProductVO) cleanse.get(i);
				String prodId = (String) prod.getProdId();
				productIds.add(prodId);
			}
		}
		
		if(moisturize != null && moisturize.size() > 0) {
			for (int i = 0; i < moisturize.size(); i++) {
				ProductVO prod = (ProductVO) moisturize.get(i);
				String prodId = (String) prod.getProdId();
				productIds.add(prodId);
			}
		}
		
		if(treat != null && treat.size() > 0) {
			for (int i = 0; i < treat.size(); i++) {
				ProductVO prod = (ProductVO) treat.get(i);
				String prodId = (String) prod.getProdId();
				productIds.add(prodId);
			}
		}
		
		if(etreat != null && etreat.size() > 0) {
			for (int i = 0; i < etreat.size(); i++) {
				ProductVO prod = (ProductVO) etreat.get(i);
				String prodId = (String) prod.getProdId();
				productIds.add(prodId);
			}
		}
		
		if(suncare != null && suncare.size() > 0) {
			for (int i = 0; i < suncare.size(); i++) {
				ProductVO prod = (ProductVO) suncare.get(i);
				String prodId = (String) prod.getProdId();
				productIds.add(prodId);
			}
		}
		
		return productIds;
	}

	private ArrayList getProductData(CustomerProfileVO cp, Hashtable ingrratings,
			ProductRecommendationEngine productRec, String prodline, String prodselect, String prodtype, String[] skinSelection, String upc) {
		//Get RX flag from the sv_product_rx_warning for ingredients in the product - Start
		boolean rxacneflag = false;
		boolean rxretinoalflag = false;
		boolean rxsteriodsflag = false;
		boolean rxcheckskinSpecification = false;
		boolean skinSpecificationAC = false;
		boolean skinSpecificationST = false;
		boolean skinSpecificationRE = false;
		String[][] resultrx = null;
		//Get RX flag from the sv_product_rx_warning for ingredients in the product - End	
		Set<String> rxWarningIngredients = new HashSet<>(); //Added to display RX Warning Text - Shivam - 19/12/2023
		//Added to display RX Warning Text for Pregnant/Nursing concern - Shivam - 20/03/2024 - Start
		boolean rxPregnantFlag = false;
		boolean skinSpecificationPregnantNursing = false;
		//Added to display RX Warning Text for Pregnant/Nursing concern - Shivam - 20/03/2024 - End
		Hashtable eliminatedIngredients = new Hashtable();
		Hashtable productsHashtable = new Hashtable();
		Hashtable all_products = new Hashtable();
		Hashtable eliminated_products = new Hashtable();
		ProductVO productVOList[] = new ProductVO[0];
		String debugline = "";
		ArrayList prodList = new ArrayList();
		Hashtable eliminatedAllIngredients = new Hashtable<>();
		eliminatedIngredients = eliminateAllIngredientsBasedOnAttribute(cp.getSkinType(), cp.getSkinConditionList(),cp.getSpecification(),eliminatedAllIngredients);
		all_products = getProdDetails(prodtype, prodselect, prodline,cp,upc); //gets
		productsHashtable = (Hashtable) all_products.get("eligible products");
		eliminated_products=(Hashtable) all_products.get("elimiated products");
		productsHashtable = getProductsIngredients(prodtype, prodselect, prodline, productsHashtable);
		productVOList = getProductArray(productVOList, productsHashtable);
		ArrayList vbprods1 = new ArrayList();
		
		EleminateProducts ep = new EleminateProducts();
		productVOList = ep.getProducts(cp, productVOList, eliminatedIngredients);
		ArrayList vbprods2 = new ArrayList();
		
		Long start = System.currentTimeMillis();
		productVOList = (ProductVO[]) ratings(cp, productVOList, ingrratings);
		Long end = System.currentTimeMillis();
		System.out.println("TIME FOR RATING :: " + (end-start));
		BumpRating br = new BumpRating();
		productVOList = br.getProducts(cp, productVOList);
		ProductBumpRating pbr = new ProductBumpRating();
		productVOList = pbr.getProducts(cp, productVOList);
		ArrayList vbprods = new ArrayList();
		for (int z = 0; z < productVOList.length; z++) {
			if (!productVOList[z].isTobeDeleted()) {
				vbprods.add(productVOList[z]);
			}
		}
		
		ProductVO[] vbproduct = new ProductVO[vbprods.size()];
		for (int vz = 0; vz < vbprods.size(); vz++)
			vbproduct[vz] = (ProductVO) vbprods.get(vz);
		ArrayList vbnproduct = new ArrayList();
		for (int i = 0; i < vbproduct.length; i++) {
			vbnproduct.add(vbproduct[i]);
		}
		
		vbnproduct = getSortedProducts(vbnproduct);
		
		// Code Starts For Top Picks
//		ArrayList cleansers = new ArrayList();
//		ArrayList cleansersFinalList = new ArrayList();
//		ArrayList exfoliantsNPeels = new ArrayList();
//		ArrayList exfoliantsNPeelsFinalList = new ArrayList();
//		ArrayList treatmentsFinalList = new ArrayList();
//		ArrayList treatmentsCond1 = new ArrayList();
//		ArrayList treatmentsCond2 = new ArrayList();
//		ArrayList treatmentsCond3 = new ArrayList();
//		ArrayList treatmentsGroup2 = new ArrayList();
//		ArrayList targetedTreatmentsFinalList = new ArrayList();
//		ArrayList targetedTreatmentsCond1 = new ArrayList();
//		ArrayList targetedTreatmentsCond2 = new ArrayList();
//		ArrayList targetedTreatmentsCond3 = new ArrayList();
//		ArrayList targetedTreatmentsCond4 = new ArrayList();
//		ArrayList targetedTreatmentsGroup2 = new ArrayList();
//		ArrayList moisturizersFinalList = new ArrayList();
//		ArrayList moisturizers = new ArrayList();
//		ArrayList eyeTreatment = new ArrayList();
//		ArrayList eyeTreatmentSerum = new ArrayList();
//		ArrayList eyeTreatmentMask = new ArrayList();
//		
//		for(int d=0;d < vbnproduct.size();d++) {
//			System.out.println("PRODTS :: " + ((ProductVO) vbnproduct.get(d)).getProdName() + " - " + ((ProductVO) vbnproduct.get(d)).getProdType() + " - " + ((ProductVO) vbnproduct.get(d)).getWhenToUse() + " - " + ((ProductVO) vbnproduct.get(d)).getScore() + " - " + ((ProductVO) vbnproduct.get(d)).getMcategoryName());
//			if(((ProductVO) vbnproduct.get(d)).getProdType().equalsIgnoreCase("1002")  
//					&& ((ProductVO) vbnproduct.get(d)).getMcategoryName().equalsIgnoreCase("Cleansers") 
//					&& ((ProductVO) vbnproduct.get(d)).getWhenToUse().equalsIgnoreCase("Daily Cleanser")
//					&& (long)Double.parseDouble(((ProductVO) vbnproduct.get(d)).getScore()) >= 50) {
//				cleansers.add((ProductVO) vbnproduct.get(d));
//			}
//			
//			if((((ProductVO) vbnproduct.get(d)).getProdType().equalsIgnoreCase("1020") 
//					&& ((ProductVO) vbnproduct.get(d)).getMcategoryName().equalsIgnoreCase("Treatments") 
//					&& (((ProductVO) vbnproduct.get(d)).getWhenToUse().equalsIgnoreCase("Daily Treatment") 
//						|| ((ProductVO) vbnproduct.get(d)).getWhenToUse().equalsIgnoreCase("Weekly Treatment") 
//						|| ((ProductVO) vbnproduct.get(d)).getWhenToUse().equalsIgnoreCase("Targeted Treatment"))
//					&& (long)Double.parseDouble(((ProductVO) vbnproduct.get(d)).getScore()) >= 50)
//					|| (((ProductVO) vbnproduct.get(d)).getProdType().equalsIgnoreCase("1017") 
//							&& ((ProductVO) vbnproduct.get(d)).getMcategoryName().equalsIgnoreCase("Treatments") 
//							&& (((ProductVO) vbnproduct.get(d)).getWhenToUse().equalsIgnoreCase("Daily Treatment") 
//								|| ((ProductVO) vbnproduct.get(d)).getWhenToUse().equalsIgnoreCase("Weekly Treatment") 
//								|| ((ProductVO) vbnproduct.get(d)).getWhenToUse().equalsIgnoreCase("Targeted Treatment"))
//							&& (long)Double.parseDouble(((ProductVO) vbnproduct.get(d)).getScore()) >= 50)) {
//				exfoliantsNPeels.add((ProductVO) vbnproduct.get(d));
//			}
//			
//			for(int e = 0; e < cp.getSkinConditionList().size(); e++) {
//				if(((String) cp.getSkinConditionList().get(e)).equalsIgnoreCase("D002")) {
//					if(((ProductVO) vbnproduct.get(d)).getProdType().equalsIgnoreCase("1021")  
//							&& ((ProductVO) vbnproduct.get(d)).getMcategoryName().equalsIgnoreCase("Treatments") 
//							&& ((ProductVO) vbnproduct.get(d)).getWhenToUse().equalsIgnoreCase("Daily Treatment")
//							&& (long)Double.parseDouble(((ProductVO) vbnproduct.get(d)).getScore()) >= 50) {
//						treatmentsCond1.add((ProductVO) vbnproduct.get(d));
//					}
//					
//				}
//				
//				if(((String) cp.getSkinConditionList().get(e)).equalsIgnoreCase("D002") || ((String) cp.getSkinConditionList().get(e)).equalsIgnoreCase("D008")) {
//					if(((ProductVO) vbnproduct.get(d)).getProdType().equalsIgnoreCase("1015")  
//							&& ((ProductVO) vbnproduct.get(d)).getMcategoryName().equalsIgnoreCase("Treatments") 
//							&& ((ProductVO) vbnproduct.get(d)).getWhenToUse().equalsIgnoreCase("Targeted Treatment")
//							&& (long)Double.parseDouble(((ProductVO) vbnproduct.get(d)).getScore()) >= 50) {
//						targetedTreatmentsCond1.add((ProductVO) vbnproduct.get(d));
//					}
//					
//				}
//				
//				if(((String) cp.getSkinConditionList().get(e)).equalsIgnoreCase("D006")){
//					
//					if(((ProductVO) vbnproduct.get(d)).getProdType().equalsIgnoreCase("1019")  
//							&& ((ProductVO) vbnproduct.get(d)).getMcategoryName().equalsIgnoreCase("Treatments") 
//							&& ((ProductVO) vbnproduct.get(d)).getWhenToUse().equalsIgnoreCase("Daily Treatment")
//							&& (long)Double.parseDouble(((ProductVO) vbnproduct.get(d)).getScore()) >= 50) {
//						treatmentsCond2.add((ProductVO) vbnproduct.get(d));
//					}
//					
//					if(((ProductVO) vbnproduct.get(d)).getProdType().equalsIgnoreCase("1019")  
//							&& ((ProductVO) vbnproduct.get(d)).getMcategoryName().equalsIgnoreCase("Treatments") 
//							&& ((ProductVO) vbnproduct.get(d)).getWhenToUse().equalsIgnoreCase("Targeted Treatment")
//							&& (long)Double.parseDouble(((ProductVO) vbnproduct.get(d)).getScore()) >= 50) {
//						targetedTreatmentsCond2.add((ProductVO) vbnproduct.get(d));
//					}
//					
//				}
//			}
//			
//			for(int e = 0; e < cp.getSkinConcernList().size(); e++) {
//				
//				if(((String) cp.getSkinConcernList().get(e)).equalsIgnoreCase("C007")){
//					
//					if(((ProductVO) vbnproduct.get(d)).getProdType().equalsIgnoreCase("1019")  
//							&& ((ProductVO) vbnproduct.get(d)).getMcategoryName().equalsIgnoreCase("Treatments") 
//							&& ((ProductVO) vbnproduct.get(d)).getWhenToUse().equalsIgnoreCase("Daily Treatment")
//							&& (long)Double.parseDouble(((ProductVO) vbnproduct.get(d)).getScore()) >= 50) {
//						treatmentsCond2.add((ProductVO) vbnproduct.get(d));
//					}
//					
//					if(((ProductVO) vbnproduct.get(d)).getProdType().equalsIgnoreCase("1019")  
//							&& ((ProductVO) vbnproduct.get(d)).getMcategoryName().equalsIgnoreCase("Treatments") 
//							&& ((ProductVO) vbnproduct.get(d)).getWhenToUse().equalsIgnoreCase("Targeted Treatment")
//							&& (long)Double.parseDouble(((ProductVO) vbnproduct.get(d)).getScore()) >= 50) {
//						targetedTreatmentsCond2.add((ProductVO) vbnproduct.get(d));
//					}
//					
//				}
//				else if(((String) cp.getSkinConcernList().get(e)).equalsIgnoreCase("C019")) {
//					
//					if(((ProductVO) vbnproduct.get(d)).getProdType().equalsIgnoreCase("1037")  
//							&& ((ProductVO) vbnproduct.get(d)).getMcategoryName().equalsIgnoreCase("Treatments") 
//							&& ((ProductVO) vbnproduct.get(d)).getWhenToUse().equalsIgnoreCase("Daily Treatment")
//							&& (long)Double.parseDouble(((ProductVO) vbnproduct.get(d)).getScore()) >= 50) {
//						treatmentsCond3.add((ProductVO) vbnproduct.get(d));
//					}
//					
//					if(((ProductVO) vbnproduct.get(d)).getProdType().equalsIgnoreCase("1037")  
//							&& ((ProductVO) vbnproduct.get(d)).getMcategoryName().equalsIgnoreCase("Treatments") 
//							&& ((ProductVO) vbnproduct.get(d)).getWhenToUse().equalsIgnoreCase("Targeted Treatment")
//							&& (long)Double.parseDouble(((ProductVO) vbnproduct.get(d)).getScore()) >= 50) {
//						targetedTreatmentsCond3.add((ProductVO) vbnproduct.get(d));
//					}
//					
//				}
//				else if(((String) cp.getSkinConcernList().get(e)).equalsIgnoreCase("C030")) {
//					
//					if(((ProductVO) vbnproduct.get(d)).getProdType().equalsIgnoreCase("1024")  
//							&& ((ProductVO) vbnproduct.get(d)).getMcategoryName().equalsIgnoreCase("Treatments") 
//							&& ((ProductVO) vbnproduct.get(d)).getWhenToUse().equalsIgnoreCase("Targeted Treatment")
//							&& (long)Double.parseDouble(((ProductVO) vbnproduct.get(d)).getScore()) >= 50) {
//						targetedTreatmentsCond4.add((ProductVO) vbnproduct.get(d));
//					}
//					
//				}
//			}
//			
//			if((((ProductVO) vbnproduct.get(d)).getProdType().equalsIgnoreCase("1018")  
//					&& ((ProductVO) vbnproduct.get(d)).getMcategoryName().equalsIgnoreCase("Treatments") 
//					&& ((ProductVO) vbnproduct.get(d)).getWhenToUse().equalsIgnoreCase("Daily Treatment")
//					&& (long)Double.parseDouble(((ProductVO) vbnproduct.get(d)).getScore()) >= 50)
//					|| (((ProductVO) vbnproduct.get(d)).getProdType().equalsIgnoreCase("1040")  
//							&& ((ProductVO) vbnproduct.get(d)).getMcategoryName().equalsIgnoreCase("Treatments") 
//							&& ((ProductVO) vbnproduct.get(d)).getWhenToUse().equalsIgnoreCase("Daily Treatment")
//							&& (long)Double.parseDouble(((ProductVO) vbnproduct.get(d)).getScore()) >= 50)
//					|| (((ProductVO) vbnproduct.get(d)).getProdType().equalsIgnoreCase("1039")  
//							&& ((ProductVO) vbnproduct.get(d)).getMcategoryName().equalsIgnoreCase("Treatments") 
//							&& ((ProductVO) vbnproduct.get(d)).getWhenToUse().equalsIgnoreCase("Daily Treatment")
//							&& (long)Double.parseDouble(((ProductVO) vbnproduct.get(d)).getScore()) >= 50)
//					|| (((ProductVO) vbnproduct.get(d)).getProdType().equalsIgnoreCase("1038")  
//							&& ((ProductVO) vbnproduct.get(d)).getMcategoryName().equalsIgnoreCase("Treatments") 
//							&& ((ProductVO) vbnproduct.get(d)).getWhenToUse().equalsIgnoreCase("Daily Treatment")
//							&& (long)Double.parseDouble(((ProductVO) vbnproduct.get(d)).getScore()) >= 50)) {
//				treatmentsGroup2.add((ProductVO) vbnproduct.get(d));
//			}
//			
//			if((((ProductVO) vbnproduct.get(d)).getProdType().equalsIgnoreCase("1018")  
//					&& ((ProductVO) vbnproduct.get(d)).getMcategoryName().equalsIgnoreCase("Treatments") 
//					&& ((ProductVO) vbnproduct.get(d)).getWhenToUse().equalsIgnoreCase("Nightly Treatment")
//					&& (long)Double.parseDouble(((ProductVO) vbnproduct.get(d)).getScore()) >= 50)
//					|| (((ProductVO) vbnproduct.get(d)).getProdType().equalsIgnoreCase("1016")  
//							&& ((ProductVO) vbnproduct.get(d)).getMcategoryName().equalsIgnoreCase("Treatments") 
//							&& ((ProductVO) vbnproduct.get(d)).getWhenToUse().equalsIgnoreCase("Weekly Treatment")
//							&& (long)Double.parseDouble(((ProductVO) vbnproduct.get(d)).getScore()) >= 50)
//					|| (((ProductVO) vbnproduct.get(d)).getProdType().equalsIgnoreCase("1022")  
//							&& ((ProductVO) vbnproduct.get(d)).getMcategoryName().equalsIgnoreCase("Treatments") 
//							&& ((ProductVO) vbnproduct.get(d)).getWhenToUse().equalsIgnoreCase("Targeted Treatment")
//							&& (long)Double.parseDouble(((ProductVO) vbnproduct.get(d)).getScore()) >= 50)) {
//				targetedTreatmentsGroup2.add((ProductVO) vbnproduct.get(d));
//			}
//			
//			if((((ProductVO) vbnproduct.get(d)).getProdType().equalsIgnoreCase("1006")  
//					&& ((ProductVO) vbnproduct.get(d)).getMcategoryName().equalsIgnoreCase("Moisturizers") 
//					&& ((ProductVO) vbnproduct.get(d)).getWhenToUse().equalsIgnoreCase("Daily Moisturizer")
//					&& (long)Double.parseDouble(((ProductVO) vbnproduct.get(d)).getScore()) >= 50)
//					|| (((ProductVO) vbnproduct.get(d)).getProdType().equalsIgnoreCase("1006")  
//							&& ((ProductVO) vbnproduct.get(d)).getMcategoryName().equalsIgnoreCase("Moisturizers") 
//							&& ((ProductVO) vbnproduct.get(d)).getWhenToUse().equalsIgnoreCase("Daily Moisturizer with SPF")
//							&& (long)Double.parseDouble(((ProductVO) vbnproduct.get(d)).getScore()) >= 50)
//					|| (((ProductVO) vbnproduct.get(d)).getProdType().equalsIgnoreCase("1006")  
//							&& ((ProductVO) vbnproduct.get(d)).getMcategoryName().equalsIgnoreCase("Moisturizers") 
//							&& ((ProductVO) vbnproduct.get(d)).getWhenToUse().equalsIgnoreCase("Daily Moisturizer Does not have SPF")
//							&& (long)Double.parseDouble(((ProductVO) vbnproduct.get(d)).getScore()) >= 50)) {
//				moisturizers.add((ProductVO) vbnproduct.get(d));
//			}
//			
//			if(cp.getSkinType().equalsIgnoreCase("T004") || cp.getSkinType().equalsIgnoreCase("T003") || cp.getSkinType().equalsIgnoreCase("T002")
//					|| cp.getAge().equalsIgnoreCase("G008") || cp.getAge().equalsIgnoreCase("G004") || cp.getAge().equalsIgnoreCase("G001")) {
//				if(((ProductVO) vbnproduct.get(d)).getProdType().equalsIgnoreCase("1007")  
//						&& ((ProductVO) vbnproduct.get(d)).getMcategoryName().equalsIgnoreCase("Moisturizers") 
//						&& ((ProductVO) vbnproduct.get(d)).getWhenToUse().equalsIgnoreCase("Nightly Moisturizers")
//						&& (long)Double.parseDouble(((ProductVO) vbnproduct.get(d)).getScore()) >= 50) {
//					moisturizers.add((ProductVO) vbnproduct.get(d));
//				}
//			}
//			
//			if(((ProductVO) vbnproduct.get(d)).getProdType().equalsIgnoreCase("1012")  
//					&& ((ProductVO) vbnproduct.get(d)).getMcategoryName().equalsIgnoreCase("Eye Care") 
//					&& ((ProductVO) vbnproduct.get(d)).getWhenToUse().equalsIgnoreCase("Daily Eye Treatment")
//					&& (long)Double.parseDouble(((ProductVO) vbnproduct.get(d)).getScore()) >= 50) {
//				eyeTreatmentSerum.add((ProductVO) vbnproduct.get(d));
//			}
//			
//			if(((ProductVO) vbnproduct.get(d)).getProdType().equalsIgnoreCase("1013")  
//					&& ((ProductVO) vbnproduct.get(d)).getMcategoryName().equalsIgnoreCase("Eye Care") 
//					&& ((ProductVO) vbnproduct.get(d)).getWhenToUse().equalsIgnoreCase("Weekly Eye Treatment")
//					&& (long)Double.parseDouble(((ProductVO) vbnproduct.get(d)).getScore()) >= 50) {
//				eyeTreatmentMask.add((ProductVO) vbnproduct.get(d));
//			}
//			
//		}
//		
//		Collections.sort(eyeTreatmentSerum, ProductVO.RatingComparator);
//		Collections.sort(eyeTreatmentMask, ProductVO.RatingComparator);
//		
//		if(eyeTreatmentSerum != null && eyeTreatmentSerum.size() >= 3 && eyeTreatmentMask != null && eyeTreatmentMask.size() > 0) {
//			int higherScore = 0;
//			for(int f = 0; f < 3; f++) {
//				
//				if((long)Double.parseDouble(((ProductVO) eyeTreatmentSerum.get(f)).getScore()) 
//						> (long)Double.parseDouble(((ProductVO) eyeTreatmentMask.get(0)).getScore())) {
//					higherScore ++;
//				}
//				
//			}
//			 if(higherScore == 3) {
//				 for(int f = 0; f < 3; f++) {
//					 eyeTreatment.add(eyeTreatmentSerum.get(f));
//				 }
//			 }
//			 else if(higherScore <= 2) {
//				 for(int f = 0; f < 2; f++) {
//					 eyeTreatment.add(eyeTreatmentSerum.get(f));
//				 }
//				 eyeTreatment.add(eyeTreatmentMask.get(0));
//			 }
//		}
//		else {
//			for(int f = 0; f < eyeTreatmentSerum.size(); f++) {
//				 eyeTreatment.add(eyeTreatmentSerum.get(f));
//			 }
//			
//			if(eyeTreatmentMask.size() >= (3-eyeTreatment.size())) {
//				for(int f = 0; f < 3 - eyeTreatment.size(); f++) {
//					 eyeTreatment.add(eyeTreatmentMask.get(f));
//				 }
//			}
//			else {
//				for(int f = 0; f < eyeTreatmentMask.size(); f++) {
//					 eyeTreatment.add(eyeTreatmentMask.get(f));
//				 }
//			}
//		}
//		
//		Collections.sort(cleansers, ProductVO.RatingComparator);
//		if(cleansers.size() >= 3) {
//			for(int d=0;d < 3;d++) {
//				cleansersFinalList.add(cleansers.get(d));
//			}
//		}
//		else {
//			for(int d=0;d < cleansers.size(); d++) {
//				cleansersFinalList.add(cleansers.get(d));
//			}
//		}
//		
//		Collections.sort(exfoliantsNPeels, ProductVO.RatingComparator);
//		if(exfoliantsNPeels.size() >= 3) {
//			for(int d=0;d < 3;d++) {
//				exfoliantsNPeelsFinalList.add(exfoliantsNPeels.get(d));
//			}
//		}
//		else {
//			for(int d=0;d < exfoliantsNPeels.size(); d++) {
//				exfoliantsNPeelsFinalList.add(exfoliantsNPeels.get(d));
//			}
//		}
//		
//		if(treatmentsCond1 != null && treatmentsCond1.size() > 0) {
//			Collections.sort(treatmentsCond1, ProductVO.RatingComparator);
//			for(int d=0;d < treatmentsCond1.size();d++) {
//				System.out.println("--treatmentsCond1 :: " + ((ProductVO) treatmentsCond1.get(d)).getProdName() + " - " + ((ProductVO) treatmentsCond1.get(d)).getProdType() + " - " + ((ProductVO) treatmentsCond1.get(d)).getWhenToUse() + " - " + ((ProductVO) treatmentsCond1.get(d)).getScore() + " - " + ((ProductVO) treatmentsCond1.get(d)).getMcategoryName());
//			}
//			treatmentsGroup2.add(treatmentsCond1.get(0));
//		}
//		if(treatmentsCond2 != null && treatmentsCond2.size() > 0) {
//			Collections.sort(treatmentsCond2, ProductVO.RatingComparator);
//			for(int d=0;d < treatmentsCond2.size();d++) {
//				System.out.println("--treatmentsCond2 :: " + ((ProductVO) treatmentsCond2.get(d)).getProdName() + " - " + ((ProductVO) treatmentsCond2.get(d)).getProdType() + " - " + ((ProductVO) treatmentsCond2.get(d)).getWhenToUse() + " - " + ((ProductVO) treatmentsCond2.get(d)).getScore() + " - " + ((ProductVO) treatmentsCond2.get(d)).getMcategoryName());
//			}
//			treatmentsGroup2.add(treatmentsCond2.get(0));
//		}
//		if(treatmentsCond3 != null && treatmentsCond3.size() > 0) {
//			Collections.sort(treatmentsCond3, ProductVO.RatingComparator);
//			for(int d=0;d < treatmentsCond3.size();d++) {
//				System.out.println("--treatmentsCond3 :: " + ((ProductVO) treatmentsCond3.get(d)).getProdName() + " - " + ((ProductVO) treatmentsCond3.get(d)).getProdType() + " - " + ((ProductVO) treatmentsCond3.get(d)).getWhenToUse() + " - " + ((ProductVO) treatmentsCond3.get(d)).getScore() + " - " + ((ProductVO) treatmentsCond3.get(d)).getMcategoryName());
//			}
//			treatmentsGroup2.add(treatmentsCond3.get(0));
//		}
//		
//		Collections.sort(treatmentsGroup2, ProductVO.RatingComparator);
//		
//		if(treatmentsGroup2.size() >= 3) {
//			for(int d=0;d < 3;d++) {
//				treatmentsFinalList.add(treatmentsGroup2.get(d));
//			}
//		}
//		else {
//			for(int d=0;d < treatmentsGroup2.size(); d++) {
//				treatmentsFinalList.add(treatmentsGroup2.get(d));
//			}
//		}
//		
//		System.out.println("targetedTreatmentsCond1 :: SIZE :: " + targetedTreatmentsCond1.size());
//		System.out.println("targetedTreatmentsCond2 :: SIZE :: " + targetedTreatmentsCond2.size());
//		System.out.println("targetedTreatmentsCond3 :: SIZE :: " + targetedTreatmentsCond3.size());
//		
//		if(targetedTreatmentsCond1 != null && targetedTreatmentsCond1.size() > 0) {
//			Collections.sort(targetedTreatmentsCond1, ProductVO.RatingComparator);
//			targetedTreatmentsGroup2.add(targetedTreatmentsCond1.get(0));
//		}
//		if(targetedTreatmentsCond2 != null && targetedTreatmentsCond2.size() > 0) {
//			Collections.sort(targetedTreatmentsCond2, ProductVO.RatingComparator);
//			for(int d=0;d < targetedTreatmentsCond2.size();d++) {
//				System.out.println("--targetedTreatmentsCond2 :: " + ((ProductVO) targetedTreatmentsCond2.get(d)).getProdName() + " - " + ((ProductVO) targetedTreatmentsCond2.get(d)).getProdType() + " - " + ((ProductVO) targetedTreatmentsCond2.get(d)).getWhenToUse() + " - " + ((ProductVO) targetedTreatmentsCond2.get(d)).getScore() + " - " + ((ProductVO) targetedTreatmentsCond2.get(d)).getMcategoryName());
//			}
//			targetedTreatmentsGroup2.add(targetedTreatmentsCond2.get(0));
//		}
//		if(targetedTreatmentsCond3 != null && targetedTreatmentsCond3.size() > 0) {
//			Collections.sort(targetedTreatmentsCond3, ProductVO.RatingComparator);
//			targetedTreatmentsGroup2.add(targetedTreatmentsCond3.get(0));
//		}
//		if(targetedTreatmentsCond4 != null && targetedTreatmentsCond4.size() > 0) {
//			Collections.sort(targetedTreatmentsCond4, ProductVO.RatingComparator);
//			targetedTreatmentsGroup2.add(targetedTreatmentsCond4.get(0));
//		}
//		
//		Collections.sort(targetedTreatmentsGroup2, ProductVO.RatingComparator);
//		
//		if(targetedTreatmentsGroup2.size() >= 3) {
//			for(int d=0;d < 3;d++) {
//				targetedTreatmentsFinalList.add(targetedTreatmentsGroup2.get(d));
//			}
//		}
//		else {
//			for(int d=0;d < targetedTreatmentsGroup2.size(); d++) {
//				targetedTreatmentsFinalList.add(targetedTreatmentsGroup2.get(d));
//			}
//		}
//		
//		Collections.sort(moisturizers, ProductVO.RatingComparator);
//		if(moisturizers.size() >= 3) {
//			for(int d=0;d < 3;d++) {
//				moisturizersFinalList.add(moisturizers.get(d));
//			}
//		}
//		else {
//			for(int d=0;d < moisturizers.size(); d++) {
//				moisturizersFinalList.add(moisturizers.get(d));
//			}
//		}
//		
//		// PRINTING LIST		
//		
//		for(int d=0;d < cleansersFinalList.size();d++) {
//			System.out.println("--cleansersFinalList :: " + ((ProductVO) cleansersFinalList.get(d)).getProdName() + " - " + ((ProductVO) cleansersFinalList.get(d)).getProdType() + " - " + ((ProductVO) cleansersFinalList.get(d)).getWhenToUse() + " - " + ((ProductVO) cleansersFinalList.get(d)).getScore() + " - " + ((ProductVO) cleansersFinalList.get(d)).getMcategoryName());
//		}
//		
//		System.out.println("");
//		
//		for(int d=0;d < exfoliantsNPeelsFinalList.size();d++) {
//			System.out.println("--exfoliantsNPeelsFinalList :: " + ((ProductVO) exfoliantsNPeelsFinalList.get(d)).getProdName() + " - " + ((ProductVO) exfoliantsNPeelsFinalList.get(d)).getProdType() + " - " + ((ProductVO) exfoliantsNPeelsFinalList.get(d)).getWhenToUse() + " - " + ((ProductVO) exfoliantsNPeelsFinalList.get(d)).getScore() + " - " + ((ProductVO) exfoliantsNPeelsFinalList.get(d)).getMcategoryName());
//		}
//		
//		System.out.println("");
//		
//		for(int d=0;d < treatmentsFinalList.size();d++) {
//			System.out.println("--treatmentsFinalList :: " + ((ProductVO) treatmentsFinalList.get(d)).getProdName() + " - " + ((ProductVO) treatmentsFinalList.get(d)).getProdType() + " - " + ((ProductVO) treatmentsFinalList.get(d)).getWhenToUse() + " - " + ((ProductVO) treatmentsFinalList.get(d)).getScore() + " - " + ((ProductVO) treatmentsFinalList.get(d)).getMcategoryName());
//		}
//		
//		System.out.println("");
//		
//		for(int d=0;d < targetedTreatmentsFinalList.size();d++) {
//			System.out.println("--targetedTreatmentsFinalList :: " + ((ProductVO) targetedTreatmentsFinalList.get(d)).getProdName() + " - " + ((ProductVO) targetedTreatmentsFinalList.get(d)).getProdType() + " - " + ((ProductVO) targetedTreatmentsFinalList.get(d)).getWhenToUse() + " - " + ((ProductVO) targetedTreatmentsFinalList.get(d)).getScore() + " - " + ((ProductVO) targetedTreatmentsFinalList.get(d)).getMcategoryName());
//		}
//		
//		System.out.println("");
//		
//		for(int d=0;d < moisturizersFinalList.size();d++) {
//			System.out.println("--moisturizersFinalList :: " + ((ProductVO) moisturizersFinalList.get(d)).getProdName() + " - " + ((ProductVO) moisturizersFinalList.get(d)).getProdType() + " - " + ((ProductVO) moisturizersFinalList.get(d)).getWhenToUse() + " - " + ((ProductVO) moisturizersFinalList.get(d)).getScore() + " - " + ((ProductVO) moisturizersFinalList.get(d)).getMcategoryName());
//		}
//		
//		System.out.println("");
//		
//		for(int d=0;d < eyeTreatment.size();d++) {
//			System.out.println("--eyeTreatment :: " + ((ProductVO) eyeTreatment.get(d)).getProdName() + " - " + ((ProductVO) eyeTreatment.get(d)).getProdType() + " - " + ((ProductVO) eyeTreatment.get(d)).getWhenToUse() + " - " + ((ProductVO) eyeTreatment.get(d)).getScore() + " - " + ((ProductVO) eyeTreatment.get(d)).getMcategoryName());
//		}
//		
//		System.out.println("ALL PRODUCTS2 " + vbnproduct.size());
		
		// Code Ends For Top Picks
		

		if (!debugline.equals("debug")) {
			productVOList = new ProductVO[0];
			long beautyProductsStartTime = System.currentTimeMillis();
			vbnproduct = (ArrayList) productRec.getBeautyProductAfterDisplayRule(vbnproduct, prodtype, cp, prodline,skinSelection);
			System.out.println("Beauty Products Time : " + (beautyProductsStartTime - System.currentTimeMillis()));
			prodList = vbnproduct;
		}//Get RX flag from the sv_product_rx_warning for ingredients in the product 
		// Get RX flag from the sv_product_rx_warning for ingredients in the product - Start
		System.out.println("ALL PRODUCTS3 " + prodList.size());
		if(prodList!=null && prodList.size()>0) {
			if(cp !=null && cp.getSpecification()!=null) {
				System.out.println("SPECIFICATION! :: " + cp.getSpecification());
				for(int d=0;d < cp.getSpecification().size();d++) {
					
					if(cp.getSpecification().get(d).equals(ConstantDeclarations.SKIN_CONDITION_ACNE)) {
						skinSpecificationAC=true;
						rxcheckskinSpecification=true;
					}
					if(cp.getSpecification().get(d).equals(ConstantDeclarations.SKIN_CONDITION_RETINOL)) {
						skinSpecificationRE=true;
						rxcheckskinSpecification=true;
					}
					if(cp.getSpecification().get(d).equals(ConstantDeclarations.SKIN_CONDITION_STERIODS)) {
						skinSpecificationST=true;
						rxcheckskinSpecification=true;
					}
					//Added to display RX Warning Text for Pregnant/Nursing concern - Shivam - 20/03/2024 - Start
					if(cp.getSpecification().get(d).equals(ConstantDeclarations.SKIN_SPECIFICATION_PREGNANT_NURSING)) {
						skinSpecificationPregnantNursing=true;
						rxcheckskinSpecification=true;
					}
					//Added to display RX Warning Text for Pregnant/Nursing concern - Shivam - 20/03/2024 - End
				}
			}
			
			if(rxcheckskinSpecification) {
				
				SVBeautydba dba = new SVBeautydba();
				SVDBOperation dbo = new SVDBOperation();
				try{
					dbo.createConnection();
				
					for (int i = 0; i < prodList.size(); i++) {
						
						if( prodList.get(i) !=null && prodList.get(i) instanceof ArrayList) {
							
							ArrayList innerlist = (ArrayList)prodList.get(i);
							if(innerlist.size()>0) {
								ArrayList objlist = (ArrayList)innerlist.get(0);
				
								for (int j = 0; j < objlist.size(); j++) {
									
									if(objlist.get(j) !=null && objlist.get(j) instanceof ProductVO ) {
								
										rxretinoalflag=false;
										rxsteriodsflag=false;
										rxacneflag=false;
										rxPregnantFlag = false; //Added to display RX Warning Text for Pregnant/Nursing concern - Shivam - 20/03/2024
										rxWarningIngredients = new HashSet<>(); //Added to display RX Warning Text - Shivam - 19/12/2023
									
										if ( ((ProductVO)objlist.get(j)).getProdId() != null ) {
												resultrx = dbo.executeQuery(dba.getBeautyProductInfoWarning( ((ProductVO)objlist.get(j)).getProdId()));
												if(resultrx != null ) {
													for(int r =0;r<resultrx.length;r++) {
												
														if(Objects.nonNull(resultrx[r][2])
																&& ("Y").equalsIgnoreCase(resultrx[r][2])
																&& skinSpecificationRE) {
															rxretinoalflag=true;
															//Added to display RX Warning Text - Shivam - 19/12/2023 - Start
															if(Objects.nonNull(resultrx[r][1]))
																rxWarningIngredients.add(resultrx[r][1]);
															//Added to display RX Warning Text - Shivam - 19/12/2023 - End
														}
														if(Objects.nonNull(resultrx[r][3])
																&& ("Y").equalsIgnoreCase(resultrx[r][3])
																&& skinSpecificationST) {
															rxsteriodsflag=true;
															//Added to display RX Warning Text - Shivam - 19/12/2023 - Start
															if(Objects.nonNull(resultrx[r][1]))
																rxWarningIngredients.add(resultrx[r][1]);
															//Added to display RX Warning Text - Shivam - 19/12/2023 - End
														}
														if(Objects.nonNull(resultrx[r][4])
																&& ("Y").equalsIgnoreCase(resultrx[r][4])
																&& skinSpecificationAC) {
															rxacneflag=true;
															//Added to display RX Warning Text - Shivam - 19/12/2023 - Start
															if(Objects.nonNull(resultrx[r][1]))
																rxWarningIngredients.add(resultrx[r][1]);
															//Added to display RX Warning Text - Shivam - 19/12/2023 - End
														}
														//Added to display RX Warning Text for Pregnant/Nursing concern - Shivam - 20/03/2024 - Start
														if(Objects.nonNull(resultrx[r][5])
																&& resultrx[r][5].equalsIgnoreCase("Y")
																&& skinSpecificationPregnantNursing) {
															rxPregnantFlag=true;
															if(Objects.nonNull(resultrx[r][1]))
																rxWarningIngredients.add(resultrx[r][1]);
														}
														//Added to display RX Warning Text for Pregnant/Nursing concern - Shivam - 20/03/2024 - End
													}
												}
										}
										/*if (rxretinoalflag) {
											((ProductVO)objlist.get(j)).setFlagRetinolt("true");
										} else {
											((ProductVO)objlist.get(j)).setFlagRetinolt("false");
										}
										if (rxacneflag) {
											((ProductVO)objlist.get(j)).setFlagAcne("true");
										} else {
											((ProductVO)objlist.get(j)).setFlagAcne("false");
										}
										if (rxsteriodsflag) {
											((ProductVO)objlist.get(j)).setFlagSteriods("true");
										} else {
											((ProductVO)objlist.get(j)).setFlagSteriods("false");
										}*/
										((ProductVO)objlist.get(j)).setFlagRetinolt("false");
										((ProductVO)objlist.get(j)).setFlagAcne("false");
										((ProductVO)objlist.get(j)).setFlagSteriods("false");
										
										//Added to display RX Warning Text - Shivam - 19/12/2023 - Start
										if((rxretinoalflag || rxacneflag || rxsteriodsflag || rxPregnantFlag)
												&& Objects.nonNull(rxWarningIngredients)
												&& !rxWarningIngredients.isEmpty()) {
											
											StringJoiner warningIngredients = new StringJoiner(", ");
											for(String warningIngredient : rxWarningIngredients) {
												warningIngredients.add(warningIngredient);
											}
											
											String rxWarningText = "This product contains <strong>"+ warningIngredients.toString()
													+ "</strong> that when paired with your prescription medication, may cause irritation or other interactions. "
													+ "This product will work great for you once you have completed your prescription medication. "
													+ "It is always a good idea to consult with your dermatologist before starting a new regimen.";
											((ProductVO)objlist.get(j)).setRxWarningText(rxWarningText);
										}
										//Added to display RX Warning Text - Shivam - 19/12/2023 - End
							     }
						       }
							}
						 }
							
		    	}
			}catch(Exception ex) {
				ex.printStackTrace();
			}finally {
				dbo.closeConnection();
			}
		  }
		  // Get RX flag from the sv_product_rx_warning for ingredients in the product - End
		}
		System.out.println("ALL PRODUCTS " + prodList.size());
//		System.out.println("ALL PRODUCTS " + productsHashtable);
		return prodList;
	}
	public List<SeeIngredientModel> getSeeIngredientModel(Hashtable funcnames, Hashtable master, PopupRatings popRate,
			String prid, String attribvalue) {
		List<SeeIngredientModel> seeIngredientModel = new ArrayList<>();
		ArrayList attribfunctions = new ArrayList();
		Hashtable functext = new Hashtable();
		Hashtable funcingr = new Hashtable();
		try {
			attribfunctions = popRate.getAttributeFunctions(attribvalue);
			String funcstring = (String) getString(attribfunctions);
			functext = (Hashtable) popRate.getFunctionText(attribvalue);
			funcingr =  (Hashtable) popRate.getFunctionIngredients(attribvalue, prid, funcstring);
			Enumeration en = funcingr.keys();
			while (en.hasMoreElements()) {
				SeeIngredientModel ingredientModel = new SeeIngredientModel();
				String funckey = (String) en.nextElement();
				ArrayList ingredients = (ArrayList) funcingr.get(funckey);
				String[] ingredientId = new String[ingredients.size()];
				String[] ingredientName = new String[ingredients.size()];
				for (int p = 0; p < ingredients.size(); p++) {
					ingredientId[p] = ingredients.get(p).toString();
					ingredientName[p] = master.get(ingredients.get(p)).toString();
				}
				ingredientModel.setIngredientId(ingredientId);
				ingredientModel.setIngredientName(ingredientName);
				String[][] funcdata = (String[][]) funcnames.get(funckey);
				String funcname = "";
				if (ingredients.size() > 0) {
					if (ingredients.size() > 1)
						funcname = "<strong>" + " " + funcdata[0][1] + "s</strong>,";
					else
						funcname = "<strong>" + " " + funcdata[0][0] + " " + funcdata[0][1] + "</strong>,";
					String functiontext = (String) functext.get(funckey + attribvalue);
					ingredientModel.setIngredientDescription(funcname + functiontext);
					seeIngredientModel.add(ingredientModel);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
		return seeIngredientModel;
	}

	/**
	 * This method calculates and returns why product is not recommended.
	 */
	private List getProductNotRecommeded(ProductVO productVO,Hashtable eliminatedAllIngredients,ProductDetailsBean ingredientDetail,PopupRatings popRate) {
		List<HeroIngredientModel> productNotRecommeded = new ArrayList();
		List zeroScoreIngredient = productVO.getEliminatedIngrList();
		Hashtable ingrfunc = popRate.getZeroScoreIngredientFunction(productVO.getProdId());
		Hashtable funcnames = popRate.getFunctionDetailsHT();
		Hashtable eliminationText = popRate.getIngredientEliminationText();
		
		for(int i=0; i<zeroScoreIngredient.size();i++) {
			
			StringTokenizer stk = new StringTokenizer((String) zeroScoreIngredient.get(i),"-") ;
			stk.nextToken();
			String ingrid =  String.valueOf(stk.nextToken());
//			System.out.println(ingrid);
//			System.out.println(ingredientDetail.getIngredientIdList().get(ingredientDetail.getIngredientIdList().indexOf(ingrid)).toString());
//			System.out.println(ingredientDetail.getIngredients().get(ingredientDetail.getIngredientIdList().indexOf(ingrid)).toString());
			HeroIngredientModel productNotRecommededModel = new HeroIngredientModel();
			productNotRecommededModel.setIngredientId(ingrid);
			productNotRecommededModel.setIngredientName(ingredientDetail.getIngredients().get(ingredientDetail.getIngredientIdList().indexOf(ingrid)).toString());
			String[][] funcdata = (String[][]) funcnames.get(ingrfunc.get(ingrid));
			String funcname = "<strong>" + " " + funcdata[0][0] + " " + funcdata[0][1] + "</strong>, ";
			List atrributeList = (List) eliminatedAllIngredients.get(zeroScoreIngredient.get(i));
			String elimination = "";
			if(atrributeList.size() < 2) {
				if(!(eliminationText.get(ingrid+atrributeList.get(0)) == null)) {
					elimination += eliminationText.get(ingrid+atrributeList.get(0));
				}
			}
			else {
				if(!(eliminationText.get(ingrid+"GEN1") == null)) {
					elimination += eliminationText.get(ingrid+"GEN1");
				}
			}
			
			System.out.println(atrributeList.toString());
			productNotRecommededModel.setIngredientDescription(funcname+elimination);
			productNotRecommeded.add(productNotRecommededModel);
		}
		
		
		return productNotRecommeded;
	}
	
	public String getString(ArrayList values)
    {
    	String ret = "(" ;
    	for (int i=0 ; i < values.size(); i++)
        {
        	if (i == values.size()-1)
        	  ret = ret+"'"+(String)values.get(i)+"'"+")" ;
        	else
          	  ret = ret+"'"+(String)values.get(i)+"'"+"," ;
        }
    	if(values.size()==0) {
    		  ret = ret+"' '"+")" ;
    	}
    	return ret ;
    }
	
	public String getproductRating(Float rating) {
		if (rating > 64.0f)
			return "Excellent";
		else if (rating > 55.0f && rating < 65.0f)
			return "Very Good";
		else if (rating > 49.0f && rating < 55.0f)
			return "Good";
		else if (rating > 39.0f && rating < 50.0f)
			return "Fair";
		else
			return RATING_CONSTANT;

	}

	String getAnalysisRating(float value) {
		String comment = "Not Ideal";
		float val = value;
		if (val > 64.0f)
			comment = "Excellent";
		else if (val > 55.0f && val < 65.0f)
			comment = "Very Good";
		else if (val > 49.0f && val < 55.0f)
			comment = "Good";
		else if (val > 39.0f && val < 50.0f)
			comment = "Fair";
		else if (val < 40.0f)
			comment = "Not Ideal";
		return comment;
	}

	private ArrayList getSortedProducts(ArrayList vect) {
		ProductVO[] pr = new ProductVO[vect.size()];
		for (int i = 0; i < vect.size(); i++) {
			((ProductVO) vect.get(i)).setScore(calculatescore((ProductVO) vect.get(i)));
			pr[i] = (ProductVO) vect.get(i);
		}
		try {
			sort(pr, 0, pr.length - 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ArrayList vt = new ArrayList();
		for (int i = 0; i < pr.length; i++) {
			ProductVO prod = pr[i];
			if (prod.isTobeDeleted() == false) {
				vt.add(pr[i]);
			}
		}
		return vt;
	}

	private void sort(ProductVO a[], int lo0, int hi0) throws Exception {
		int lo = lo0;
		int hi = hi0;
		if (lo >= hi) {
			return;
		} else if (lo == hi - 1) {
			if (Float.parseFloat(a[lo].getScore()) > Float.parseFloat(a[hi].getScore())) {
				ProductVO T = a[lo];
				a[lo] = a[hi];
				a[hi] = T;
			}
			return;
		}
		ProductVO pivot = a[(lo + hi) / 2];
		a[(lo + hi) / 2] = a[hi];
		a[hi] = pivot;

		while (lo < hi) {
			while (Float.parseFloat(a[lo].getScore()) <= Float.parseFloat(pivot.getScore()) && lo < hi) {
				lo++;
			}
			while (Float.parseFloat(pivot.getScore()) <= Float.parseFloat(a[hi].getScore()) && lo < hi) {
				hi--;
			}

			if (lo < hi) {
				ProductVO T = a[lo];
				a[lo] = a[hi];
				a[hi] = T;
			}
		}
		a[hi0] = a[hi];
		a[hi] = pivot;
		sort(a, lo0, lo - 1);
		sort(a, hi + 1, hi0);
	}
	
	public ProductModel getProductScore(String customid, String prodtype, String prodids,
			String prodline, CustomerProfileVO cp, String prodselect, Hashtable<String, String> master,
			SingleProductScoreModel productModel, ProductModel productModelDTO) {
		ArrayList prodList = new ArrayList();
		String upc = productModel.getUpc();
		String pid = "";
		String prid = "";
		String pmtype = "";
		ProductVO product = new ProductVO();
		Hashtable productsHashtable = new Hashtable();
		Hashtable ingrratings = new Hashtable();
		Hashtable bumpscores = new Hashtable();
		Hashtable scoreWiseRatings = new Hashtable();
		ArrayList<String> productIds = new ArrayList<String>();
		DetailsService service = new DetailsService();
		PopupRatings popRate = new PopupRatings();
		List<ProductModel> cleanserList = new ArrayList<>();
		List<ProductModel> moisturizerList = new ArrayList<>();
		List<ProductModel> dailyTreatmentList = new ArrayList<>();
		List<ProductModel> targetedOneTreatmentList = new ArrayList<>();
		List<ProductModel> tagetedTwoTreatmentList = new ArrayList<>();
		List<ProductModel> weeklyTreatmentList = new ArrayList<>();
		List<ProductModel> atHomeFacialWeeklyTreatmentList = new ArrayList<>();

		List<ProductModel> eyeCareList = new ArrayList<>();
		ArrayList cleanse = new ArrayList();
		ArrayList moisturize = new ArrayList();
		ArrayList treat = new ArrayList();
		ArrayList etreat = new ArrayList();
		ArrayList profiles = new ArrayList();		
		int cleancnt=0;
		int moistcnt=0;
		int treatcnt=0;
		int eyecnt=0;
		int suncnt=0;
		int primercnt=0;	
		ArrayList suncare = new ArrayList();	
		ArrayList primer = new ArrayList();
		ArrayList skinCondition = new ArrayList();	
		ArrayList skinConcern = new ArrayList();
		ProductRecommendationEngine productRec = new ProductRecommendationEngine();
		RecommendationEngineDebug recEngDebug = new RecommendationEngineDebug();
		try {
			cp = (CustomerProfileVO) getCustomerProfile(customid, cp, "skin");
			ingrratings = getIngredientsRatingsAboveZero(cp, false);
			String[] skinSelection = productModel.getSkin_concern();
			Hashtable eliminatedAllIngredients = new Hashtable();
			prodList = getSingleProductData(cp, ingrratings, productRec, prodline, prodselect, prodtype,skinSelection,upc,eliminatedAllIngredients);
			//prodList = productRec.getCustomerReviewStars(prodList);
			System.out.println("SKIN CONCERN Size :: " + cp.getSkinConcernList().size());
			cleanse = (ArrayList) prodList.get(0);
			System.out.println("CLENSERS :: " + cleanse);
			moisturize = (ArrayList) prodList.get(1);
			System.out.println("MOIST :: " + moisturize);
			treat = (ArrayList) prodList.get(2);
			System.out.println("TREAT :: " + treat);
			etreat = (ArrayList) prodList.get(3);
			System.out.println("EYE TREAT :: " + etreat);
			suncare = (ArrayList) prodList.get(4);
			System.out.println("SUNCARE :: " + suncare);
			//primer = (ArrayList) prodList.get(10);
			
			productIds = getProductIds((ArrayList)cleanse.get(0), (ArrayList)moisturize.get(0), (ArrayList)treat.get(0), (ArrayList)etreat.get(0), (ArrayList)suncare.get(0));
			HashMap ingredientsWithGlossary = service.getIngredientNamesWithGlossary();
			bumpscores = (Hashtable) recEngDebug.getBumpScores();
			profiles = getProfile(cp);
			Hashtable funcnames = popRate.getFunctionDetailsHT();
			Hashtable mastertwo = productRec.getMasterDataTwo();

			if (productIds != null && productIds.size() > 0) {
				for (int i = 0; i < productIds.size(); i++) {
					pid = (String) productIds.get(i);
					prid = pid;
					pid = "('" + pid + "')";
					productsHashtable = (Hashtable) getBeautyProductInfo(pid); // gets the product
					productsHashtable = recEngDebug.getProductsIngredients("", "prodId", "", productsHashtable, pid, false);
					product = (ProductVO) productsHashtable.get(prid);
					product = (ProductVO) recEngDebug.ratingsIndividual(cp, product, ingrratings);
					ProductVO pro = new ProductVO();
					List<WhyRecomondationModel> whyRecomondationModelList = new ArrayList<>();
					boolean flag = false;
					for (int j = 0; j < profiles.size(); j++) {
						String profileAtt = (String) profiles.get(j);
						String ptype = product.getProdType();
						pmtype = product.getMasterProdType();
						Hashtable pBaseRatings = product.getProfileBaseRatings();
						Hashtable pIngrRatings = product.getProfileIngrRatings();
						int pbasescore = 0;
						int pbasecount = 0;
						int pbasepercen = 0;
						int pingrscore = 0;
						int pingrcount = 0;
						int pingrpercen = 0;
						String bumpkey = profileAtt + ptype;
						String bscore = (String) bumpscores.get(bumpkey);
						if (pBaseRatings.get(profileAtt) != null) {
							String pbasescorestring = (String) pBaseRatings.get(profileAtt);
							StringTokenizer stp = new StringTokenizer(pbasescorestring, "::");
							String stps = stp.nextToken();
							String stpc = stp.nextToken();
							pbasescore = Integer.parseInt(stps);
							pbasecount = Integer.parseInt(stpc);
						}
						if (pIngrRatings.get(profileAtt) != null) {
							String pingrscorestring = (String) pIngrRatings.get(profileAtt);
							StringTokenizer stp = new StringTokenizer(pingrscorestring, "::");
							String stps = stp.nextToken();
							String stpc = stp.nextToken();
							pingrscore = Integer.parseInt(stps);
							pingrcount = Integer.parseInt(stpc);
						}
						float ingrpercentage = 0.0f;
						float basepercentage = 0.0f;
						if (pingrcount != 0)
							pingrpercen = 100 * pingrscore / (pingrcount * 10);
						if (pbasecount != 0)
							pbasepercen = 100 * pbasescore / (pbasecount * 10);
						float pingrpercentage = Float.parseFloat(String.valueOf(pingrpercen));
						float pbasepercentage = Float.parseFloat(String.valueOf(pbasepercen));
						float score = 0.0f;
						float bumpscore = 0.0f;
						if (bscore != null && bscore.length() != 0)
							bumpscore = Float.parseFloat(String.valueOf(bscore));
						if (pmtype.equals("0004")) {
							ingrpercentage = (pingrpercentage * 100f) / 100f;
							score = ingrpercentage;
						} else {
							if (pbasepercentage == 0.0f) {
								ingrpercentage = (pingrpercentage * 100f) / 100f;
								score = ingrpercentage;
							} else {
								basepercentage = (pbasepercentage * 20f) / 100f;
								ingrpercentage = (pingrpercentage * 80f) / 100f;
								score = ingrpercentage + basepercentage;
							}
						}
						score = score + bumpscore;
						scoreWiseRatings.put(profileAtt, "" + score);
					
						// START : ADDED By Synoptek::HARESH: Changes loop for Regimen wise 
																	
//						if(!flag) {
//							if(cleancnt <= 5 && ConstantDeclarations.PRODUCT_MASTER_TYPE_CLEANSE.equals(pmtype)) {
//								pro = (ProductVO) ((ArrayList)cleanse.get(0)).get(cleancnt);
//								flag=true;
//								cleancnt++;
//							} else if(moistcnt <= 3 && ConstantDeclarations.PRODUCT_MASTER_TYPE_MOIST.equals(pmtype)) {
//								pro = (ProductVO) ((ArrayList)moisturize.get(0)).get(moistcnt);
//								flag=true;
//								moistcnt++;
//							} else if(treatcnt <= 9 && ConstantDeclarations.PRODUCT_MASTER_TYPE_TREAT.equals(pmtype)) {
//								pro = (ProductVO) ((ArrayList)treat.get(0)).get(treatcnt);
//								flag=true;
//								treatcnt++;
//							} else if(eyecnt <= 1 && ConstantDeclarations.PRODUCT_MASTER_TYPE_ETREAT.equals(pmtype)) {
//								pro = (ProductVO) ((ArrayList)etreat.get(0)).get(eyecnt);
//								flag=true;
//								eyecnt++;
//							} else if(suncnt < 1 && ConstantDeclarations.PRODUCT_MASTER_TYPE_UV.equals(pmtype)) {
//								pro = (ProductVO) ((ArrayList)suncare.get(0)).get(suncnt);
//								flag=true;
//								suncnt++;
//							} else if(primercnt < 1 && ConstantDeclarations.PRODUCT_MASTER_TYPE_PRIMER.equals(pmtype)) {
//								pro = (ProductVO)((ArrayList)primer.get(0)).get(primercnt);
//								flag=true;
//								primercnt++;
//							}
//						} 
						// END : ADDED By Synoptek::HARESH : Changes loop for Regimen wise 
						
						/** Get result after second time recommendation check */
						WhyRecomondationModel whyRecomondationModel = new WhyRecomondationModel();
						if (scoreWiseRatings != null && !scoreWiseRatings.isEmpty()
								&& !getproductRating(Float.valueOf(scoreWiseRatings.get(profileAtt).toString()))
										.equals(RATING_CONSTANT)
								&& master != null && !master.isEmpty()) {
							System.out.println("profileAtt :: " + profileAtt);
							whyRecomondationModel.setSkinConcern(master.get(profileAtt).toString());
							System.out.println("master.get(profileAtt).toString() :: " + master.get(profileAtt).toString());
							whyRecomondationModel.setProductRating(
									getproductRating(Float.valueOf(scoreWiseRatings.get(profileAtt).toString())));
							whyRecomondationModel.setActiveIngredient(
									getSeeIngredientModel(funcnames, mastertwo, popRate, prid, profileAtt));

							whyRecomondationModelList.add(whyRecomondationModel);
						}
					}
//					ProductModel prodModel = new ProductModel();
					ProductDetailsBean ingredientDetail = service.getProductDetails(pro.getProdId());
					String catgorySubCategory[] = service.getProductCategorySubCategory(pro.getProdId());
					productModelDTO.setProductId(pro.getProdId());
					productModelDTO.setSku(pro.getSku());
					productModelDTO.setUpc(pro.getUpc());
					productModelDTO.setProductName(pro.getProdName());
					productModelDTO.setBrandId(pro.getProductLine());
					productModelDTO.setProductType(catgorySubCategory[1]);
					productModelDTO.setProductScore(pro.getScore());
					productModelDTO.setProductMasterType(catgorySubCategory[0]);
					productModelDTO.setWhenToUse(pro.getWhenToUse());
					productModelDTO.setMultiBenefits(pro.getMultiBenefits());
					productModelDTO.setLifeStyleBasedRecs1(pro.getLifeStyleBasedRecs1());
					productModelDTO.setLifeStyleBasedRecs2(pro.getLifeStyleBasedRecs2());
					// Get RX flag from the sv_product_rx_warning for ingredients in the product - Start
					productModelDTO.setFlagRXAcne(pro.getFlagAcne());
					productModelDTO.setFlagRXRetinol(pro.getFlagRetinolt());
					productModelDTO.setFlagTropicalSteriods(pro.getFlagSteriods());
					// Get RX flag from the sv_product_rx_warning for ingredients in the product - End
					
					//Added to display RX Warning Text - Shivam - 20/12/2023 - Start
					if(Objects.nonNull(pro.getRxWarningText())
							&& pro.getRxWarningText().length() > 0) {
						productModelDTO.setRxWarningText(pro.getRxWarningText());
					}
					//Added to display RX Warning Text - Shivam - 20/12/2023 - End
					
					productModelDTO.setWhyRecommended(whyRecomondationModelList);
					ArrayList<IngredientModel> ingredientList = new ArrayList<>();
					if (pro.getIngredients() != null && pro.getIngredients().length > 0 && ingredientDetail != null
							&& !ingredientDetail.getIngredientIdList().isEmpty()) {
						for (int k = 0; k < pro.getIngredients().length; k++) {
							IngredientModel ingredientModel = new IngredientModel();
							ingredientModel.setIngredientId(ingredientDetail.getIngredientIdList().get(k).toString());
							ingredientModel.setIngredientName(ingredientDetail.getIngredients().get(k).toString().trim());
							ingredientModel.setDisplayOrder(ingredientDetail.getDisplayOrder().get(k).toString());
							if (ingredientsWithGlossary.get(ingredientDetail.getIngredients().get(k).toString().trim().toUpperCase()) != null) {
								ingredientModel.setIngredientDefination((String)ingredientsWithGlossary.get(ingredientDetail.getIngredients().get(k).toString().trim().toUpperCase()));
							} else {
								ingredientModel.setIngredientDefination("");
							}
							ingredientList.add(ingredientModel);
						}
						productModelDTO.setIngredients(ingredientList);
					}
					productModelDTO.setWarningText(getWarningText(pro,cp,master));
//					if (pmtype.equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_CLEANSE)) {
//						cleanserList.add(prodModel);
//					} else if (pmtype.equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_MOIST) || pmtype.equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_UV) || pmtype.equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_PRIMER)) {
//						moisturizerList.add(prodModel);
//					} else if (pmtype.equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_TREAT)) {
//						if (pro.getComments().equalsIgnoreCase(ConstantDeclarations.DAILY))
//							dailyTreatmentList.add(prodModel);
//						else if (pro.getComments().equalsIgnoreCase(ConstantDeclarations.TARGETEDONE))
//							targetedOneTreatmentList.add(prodModel);
//						else if (pro.getComments().equalsIgnoreCase(ConstantDeclarations.TARGETEDTWO))
//							tagetedTwoTreatmentList.add(prodModel);
//						else if (pro.getComments().equalsIgnoreCase(ConstantDeclarations.WEEKLY))
//							weeklyTreatmentList.add(prodModel);
//						else if (pro.getComments().equalsIgnoreCase(ConstantDeclarations.ATHOMEWEEKLY))
//							atHomeFacialWeeklyTreatmentList.add(prodModel);
//					} else {
//						eyeCareList.add(prodModel);
//					}
				}
			}
			
//			recomondedProductDetailDTO.setCleanserList(cleanserList);
//			recomondedProductDetailDTO.setMoisturizerList(moisturizerList);
//			recomondedProductDetailDTO.setDailyTreatmentList(dailyTreatmentList);
//			recomondedProductDetailDTO.setTargetedOneTreatmentList(targetedOneTreatmentList);
//			recomondedProductDetailDTO.setTargetedTwoTreatmentList(tagetedTwoTreatmentList);
//			recomondedProductDetailDTO.setWeeklyTreatmentList(weeklyTreatmentList);
//			recomondedProductDetailDTO.setAtHomeFacialWeeklyTreatmentList(atHomeFacialWeeklyTreatmentList);
//			recomondedProductDetailDTO.setEyeProductList(eyeCareList);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return productModelDTO;
	}
	
	private ArrayList getSingleProductData(CustomerProfileVO cp, Hashtable ingrratings,
			ProductRecommendationEngine productRec, String prodline, String prodselect, String prodtype, String[] skinSelection, String upc,Hashtable eliminatedAllIngredients) {
		//Get RX flag from the sv_product_rx_warning for ingredients in the product - Start
		boolean rxacneflag = false;
		boolean rxretinoalflag = false;
		boolean rxsteriodsflag = false;
		boolean rxcheckskinSpecification = false;
		boolean skinSpecificationAC = false;
		boolean skinSpecificationST = false;
		boolean skinSpecificationRE = false;
		String[][] resultrx = null;
		//Get RX flag from the sv_product_rx_warning for ingredients in the product - End
		Set<String> rxWarningIngredients = new HashSet<>(); //Added to display RX Warning Text - Shivam - 20/12/2023
		// Added to display RX Warning Text for Pregnant/Nursing concern - Shivam - 20/03/2024 - Start
		boolean rxPregnantFlag = false;
		boolean skinSpecificationPregnantNursing = false;
		// Added to display RX Warning Text for Pregnant/Nursing concern - Shivam - 20/03/2024 - End
		Hashtable eliminatedIngredients = new Hashtable();
		Hashtable productsHashtable = new Hashtable();
		Hashtable bumpscores = new Hashtable();
		Hashtable all_products = new Hashtable();
		Hashtable eliminated_products = new Hashtable();
		System.out.println("Hello 1");
		ProductVO productVOList[] = new ProductVO[0];
		String debugline = "";
		ArrayList prodList = new ArrayList();
//		Hashtable eliminatedAllIngredients = new Hashtable<>();
		eliminatedIngredients = eliminateAllIngredientsBasedOnAttribute(cp.getSkinType(), cp.getSkinConditionList(),cp.getSpecification(),eliminatedAllIngredients);
		all_products = getProdDetails(prodtype, prodselect, prodline,cp,upc); //gets
		productsHashtable = (Hashtable) all_products.get("eligible products");
		System.out.println("ELIGIBLE PRODUCTS ::" + productsHashtable);
		eliminated_products=(Hashtable) all_products.get("elimiated products");
		productsHashtable = getProductsIngredients(prodtype, prodselect, prodline, productsHashtable);
		productVOList = getProductArray(productVOList, productsHashtable);
		ArrayList vbprods1 = new ArrayList();
		
		EleminateProducts ep = new EleminateProducts();
		//Added for Elimination reason - Shivam - 26-08-2023 - Start
		ArrayList<String> allergy = new ArrayList<String>();
		ep.setAllergicIngredients(allergy);
		//Added for Elimination reason - Shivam - 26-08-2023 - End
		productVOList = ep.getProducts(cp, productVOList, eliminatedIngredients);
		setAllergicIngredients(ep.getAllergicIngredients());//Added for Elimination reason - Shivam - 26-08-2023
		ArrayList vbprods2 = new ArrayList();
		System.out.println("ALL PRODUCTS1 " + productVOList.length);
		
		productVOList = (ProductVO[]) ratings(cp, productVOList, ingrratings);
		if(productVOList.length > 0 && productVOList[0].isTobeDeleted()) {
			prodList = new ArrayList();
			prodList.add(productVOList[0]);
			return prodList;
		}
		
		System.out.println("ALL PRODUCTS12 " + productVOList.length);
		BumpRating br = new BumpRating();
		productVOList = br.getProducts(cp, productVOList);
		System.out.println("ALL PRODUCTS13 " + productVOList.length);
		ProductBumpRating pbr = new ProductBumpRating();
		productVOList = pbr.getProducts(cp, productVOList);
		System.out.println("ALL PRODUCTS14 " + productVOList.length);
		ArrayList vbprods = new ArrayList();
		for (int z = 0; z < productVOList.length; z++) {
			if (!productVOList[z].isTobeDeleted()) {
				vbprods.add(productVOList[z]);
			}
		}
		System.out.println("ALL PRODUCTS15 " + productVOList.length);
		System.out.println("ALL PRODUCTS16 " + vbprods.size());
		ProductVO[] vbproduct = new ProductVO[vbprods.size()];
		for (int vz = 0; vz < vbprods.size(); vz++)
			vbproduct[vz] = (ProductVO) vbprods.get(vz);
		ArrayList vbnproduct = new ArrayList();
		for (int i = 0; i < vbproduct.length; i++) {
			vbnproduct.add(vbproduct[i]);
		}
		
		vbnproduct = getSortedProducts(vbnproduct);		

		System.out.println("Hello from outside of if");
		if (!debugline.equals("debug") && upc == null && upc.length() >= 0) {
			System.out.println("Hello from if");
			productVOList = new ProductVO[0];
			vbnproduct = (ArrayList) productRec.getBeautyProductAfterDisplayRule(vbnproduct, prodtype, cp, prodline,skinSelection);
			prodList = vbnproduct;
		}//Get RX flag from the sv_product_rx_warning for ingredients in the product 
		// Get RX flag from the sv_product_rx_warning for ingredients in the product - Start
		else {
			prodList = vbnproduct;
		}
		System.out.println("ALL PRODUCTS31 " + prodList.size());
		if(prodList!=null && prodList.size()>0) {
			if(cp !=null && cp.getSpecification()!=null) {
				System.out.println("SPECIFICATION! :: " + cp.getSpecification());
				for(int d=0;d < cp.getSpecification().size();d++) {
					
					if(cp.getSpecification().get(d).equals(ConstantDeclarations.SKIN_CONDITION_ACNE)) {
						skinSpecificationAC=true;
						rxcheckskinSpecification=true;
					}
					if(cp.getSpecification().get(d).equals(ConstantDeclarations.SKIN_CONDITION_RETINOL)) {
						skinSpecificationRE=true;
						rxcheckskinSpecification=true;
					}
					if(cp.getSpecification().get(d).equals(ConstantDeclarations.SKIN_CONDITION_STERIODS)) {
						skinSpecificationST=true;
						rxcheckskinSpecification=true;
					}
					//Added to display RX Warning Text for Pregnant/Nursing concern - Shivam - 20/03/2024 - Start
					if(cp.getSpecification().get(d).equals(ConstantDeclarations.SKIN_SPECIFICATION_PREGNANT_NURSING)) {
						skinSpecificationPregnantNursing=true;
						rxcheckskinSpecification=true;
					}
					//Added to display RX Warning Text for Pregnant/Nursing concern - Shivam - 20/03/2024 - End
				}
			}
			
			if(rxcheckskinSpecification) {
				
				SVBeautydba dba = new SVBeautydba();
				SVDBOperation dbo = new SVDBOperation();
				try{
					dbo.createConnection();
				
					for (int i = 0; i < prodList.size(); i++) {
						
						if( prodList.get(i) !=null && prodList.get(i) instanceof ArrayList) {
							
							ArrayList innerlist = (ArrayList)prodList.get(i);
							if(innerlist.size()>0) {
								ArrayList objlist = (ArrayList)innerlist.get(0);
				
								for (int j = 0; j < objlist.size(); j++) {
									
									if(objlist.get(j) !=null && objlist.get(j) instanceof ProductVO ) {
								
										rxretinoalflag=false;
										rxsteriodsflag=false;
										rxacneflag=false;
										rxPregnantFlag = false; //Added to display RX Warning Text for Pregnant/Nursing concern - Shivam - 20/03/2024
										rxWarningIngredients = new HashSet<>(); //Added to display RX Warning Text - Shivam - 20/12/2023
									
										if ( ((ProductVO)objlist.get(j)).getProdId() != null ) {
												resultrx = dbo.executeQuery(dba.getBeautyProductInfoWarning( ((ProductVO)objlist.get(j)).getProdId()));
												if(resultrx != null ) {
													for(int r =0;r<resultrx.length;r++) {
												
														if(Objects.nonNull(resultrx[r][2])
																&& ("Y").equalsIgnoreCase(resultrx[r][2])
																&& skinSpecificationRE) {
															rxretinoalflag=true;
															//Added to display RX Warning Text - Shivam - 20/12/2023 - Start
															if(Objects.nonNull(resultrx[r][1]))
																rxWarningIngredients.add(resultrx[r][1]);
															//Added to display RX Warning Text - Shivam - 20/12/2023 - End
														}
														if(Objects.nonNull(resultrx[r][3])
																&& ("Y").equalsIgnoreCase(resultrx[r][3])
																&& skinSpecificationST) {
															rxsteriodsflag=true;
															//Added to display RX Warning Text - Shivam - 20/12/2023 - Start
															if(Objects.nonNull(resultrx[r][1]))
																rxWarningIngredients.add(resultrx[r][1]);
															//Added to display RX Warning Text - Shivam - 20/12/2023 - End
														}
														if(Objects.nonNull(resultrx[r][4])
																&& ("Y").equalsIgnoreCase(resultrx[r][4])
																&& skinSpecificationAC) {
															rxacneflag=true;
															//Added to display RX Warning Text - Shivam - 20/12/2023 - Start
															if(Objects.nonNull(resultrx[r][1]))
																rxWarningIngredients.add(resultrx[r][1]);
															//Added to display RX Warning Text - Shivam - 20/12/2023 - End
														}
														//Added to display RX Warning Text for Pregnant/Nursing concern - Shivam - 20/03/2024 - Start
														if(Objects.nonNull(resultrx[r][5])
																&& resultrx[r][5].equalsIgnoreCase("Y")
																&& skinSpecificationPregnantNursing) {
															rxPregnantFlag=true;
															if(Objects.nonNull(resultrx[r][1]))
																rxWarningIngredients.add(resultrx[r][1]);
														}
														//Added to display RX Warning Text for Pregnant/Nursing concern - Shivam - 20/03/2024 - End
													}
												}
										}
										if (rxretinoalflag)
											((ProductVO)objlist.get(j)).setFlagRetinolt("true");
										else
											((ProductVO)objlist.get(j)).setFlagRetinolt("false");
										if (rxacneflag)
											((ProductVO)objlist.get(j)).setFlagAcne("true");
										else
											((ProductVO)objlist.get(j)).setFlagAcne("false");
										if (rxsteriodsflag)
											((ProductVO)objlist.get(j)).setFlagSteriods("true");
										else
											((ProductVO)objlist.get(j)).setFlagSteriods("false");
										
										//Added to display RX Warning Text - Shivam - 20/12/2023 - Start
										if((rxretinoalflag || rxacneflag || rxsteriodsflag || rxPregnantFlag)
												&& Objects.nonNull(rxWarningIngredients)
												&& !rxWarningIngredients.isEmpty()) {
											
											StringJoiner warningIngredients = new StringJoiner(", ");
											
											for(String warningIngredient : rxWarningIngredients) {
												warningIngredients.add(warningIngredient);
											}
											String rxWarningText = "This product contains <strong>"+ warningIngredients.toString()
													+ "</strong> that when paired with your prescription medication, may cause irritation or other interactions. "
													+ "This product will work great for you once you have completed your prescription medication. "
													+ "It is always a good idea to consult with your dermatologist before starting a new regimen.";
											((ProductVO)objlist.get(j)).setRxWarningText(rxWarningText);
										}
										//Added to display RX Warning Text - Shivam - 20/12/2023 - End
							     }
						       }
							}
						 }
							
		    	}
			}catch(Exception ex) {
				ex.printStackTrace();
			}finally {
				dbo.closeConnection();
			}
		  }
		  // Get RX flag from the sv_product_rx_warning for ingredients in the product - End
		}
		System.out.println("ALL PRODUCTS " + prodList.size());
//		System.out.println("ALL PRODUCTS " + productsHashtable);
		for(int i = 0; i< prodList.size(); i++) {
			System.out.println("PROD ALL " + prodList.get(i));
		}
		return prodList;
	}
	
	public SingleProductScoreResponseModel getScoreForSingleProduct(String customid, String prodtype, String prodids,
			String prodline, CustomerProfileVO cp, String prodselect, Hashtable<String, String> master,
			SingleProductScoreModel productModel, SingleProductScoreResponseModel productModelDTO) throws IllegalAccessException, InvocationTargetException {
		ArrayList prodList = new ArrayList();
		RecomondationProductModel recomondedproductModel = new RecomondationProductModel();
		BeanUtils.copyProperties(recomondedproductModel, productModel);
		System.out.println(recomondedproductModel.toString());
		System.out.println("SKIN CONCERN Size :: " + cp.getSkinConcernList().size());
		String pid = "";
		String prid = "";
		String pmtype = "";
		ProductVO product = new ProductVO();
		Hashtable productsHashtable = new Hashtable();
		Hashtable ingrratings = new Hashtable();
		Hashtable bumpscores = new Hashtable();
		Hashtable scoreWiseRatings = new Hashtable();
		ArrayList<String> productIds = new ArrayList<String>();
		DetailsService service = new DetailsService();
		PopupRatings popRate = new PopupRatings();
//		List<ProductModel> cleanserList = new ArrayList<>();
//		List<ProductModel> moisturizerList = new ArrayList<>();
//		List<ProductModel> dailyTreatmentList = new ArrayList<>();
//		List<ProductModel> targetedOneTreatmentList = new ArrayList<>();
//		List<ProductModel> tagetedTwoTreatmentList = new ArrayList<>();
//		List<ProductModel> weeklyTreatmentList = new ArrayList<>();
//		List<ProductModel> atHomeFacialWeeklyTreatmentList = new ArrayList<>();

//		List<ProductModel> eyeCareList = new ArrayList<>();
//		ArrayList cleanse = new ArrayList();
//		ArrayList moisturize = new ArrayList();
//		ArrayList treat = new ArrayList();
//		ArrayList etreat = new ArrayList();
		ArrayList profiles = new ArrayList();		
//		int cleancnt=0;
//		int moistcnt=0;
//		int treatcnt=0;
//		int eyecnt=0;
//		int suncnt=0;
//		int primercnt=0;	
//		ArrayList suncare = new ArrayList();	
//		ArrayList primer = new ArrayList();
		ArrayList skinCondition = new ArrayList();	
		ArrayList skinConcern = new ArrayList();
		ProductRecommendationEngine productRec = new ProductRecommendationEngine();
		RecommendationEngineDebug recEngDebug = new RecommendationEngineDebug();
		SingleProductScoreResponseModel prodModel = new SingleProductScoreResponseModel();
		try {
			long start = 0L;
			long end = 0L;
			
			start = System.currentTimeMillis();
			cp = (CustomerProfileVO) getCustomerProfile(customid, cp, "skin");
			end = System.currentTimeMillis();
			System.out.println("TOTAL SECONDS TAKEN BY CP :: " + (end-start)/1000 + " Sec + " + (end-start) + " ms" );
			
			start = System.currentTimeMillis();
			ingrratings = getIngredientsRatingsAboveZero(cp, false,productModelDTO.getProductId());
			end = System.currentTimeMillis();
			System.out.println("TOTAL SECONDS TAKEN BY ingrratings :: " + (end-start)/1000 + " Sec + " + (end-start) + " ms" );
			
			String[] skinSelection = productModel.getSkin_concern();
			List skinConcernList = Arrays.asList(productModel.getSkin_concern());
			Hashtable eliminatedAllIngredients = new Hashtable<>();
			start = System.currentTimeMillis();
			prodList = getSingleProductData(cp, ingrratings, productRec, prodline, prodselect, prodtype,skinSelection,productModel.getUpc(),eliminatedAllIngredients);
			end = System.currentTimeMillis();
			System.out.println("TOTAL SECONDS TAKEN BY prodList :: " + (end-start)/1000 + " Sec + " + (end-start) + " ms" );
			
			System.out.println(eliminatedAllIngredients.toString());
			
			ProductVO productVO = new ProductVO();
			System.out.println("SIZE prodList :: " + prodList.size());
			if(prodList != null) {
				productVO = (ProductVO) prodList.get(0);
			}
			System.out.println(productVO.toString());
			if(productVO != null && productVO.isTobeDeleted()) {
				SingleProductScoreResponseModel SingleProdModel = new SingleProductScoreResponseModel();
				ProductScoreAnalysisModel productScoreAnalysisModel = new ProductScoreAnalysisModel();
				ProductDetailsBean ingredientDetail = service.getProductDetails(productVO.getProdId());
				SingleProdModel.setProductId(productVO.getProdId());
				SingleProdModel.setUpc(productVO.getUpc());
				SingleProdModel.setSku(productVO.getSku());
//				SingleProdModel.setProductName(productVO.getProdName());
//				SingleProdModel.setBrandId(productVO.getBase());
				SingleProdModel.setResponseMessage(productVO.getReason());
				if(productVO.getReason() != null && productVO.getReason().length() > 0) {
					if(productVO.getReason().equalsIgnoreCase(ConstantDeclarations.ZERO_INGR_SCO_ALLERGY)) {
						SingleProdModel.setResponseCode(ConstantDeclarations.ZERO_INGR_SCO_ALLERGY_CODE);
					}
					else if(productVO.getReason().equalsIgnoreCase(ConstantDeclarations.ZERO_INGR_SCO)) {
						SingleProdModel.setResponseCode(ConstantDeclarations.ZERO_INGR_SCO_CODE);
						
						
					}
					else if(productVO.getReason().equalsIgnoreCase(ConstantDeclarations.ALLERGY)) {
						SingleProdModel.setResponseCode(ConstantDeclarations.ALLERGY_CODE);
					}
					else if(productVO.getReason().equalsIgnoreCase(ConstantDeclarations.ZERO_FORMU_SCO)) {
						SingleProdModel.setResponseCode(ConstantDeclarations.ZERO_FORMU_SCO_CODE);
					}
					else if(productVO.getReason().equalsIgnoreCase(ConstantDeclarations.FORM_NOT_FOUND)) {
						SingleProdModel.setResponseCode(ConstantDeclarations.FORM_NOT_FOUND_CODE);
					}
					else if(productVO.getReason().equalsIgnoreCase(ConstantDeclarations.CAT_BRND_COMBINATION)) {
						SingleProdModel.setResponseCode(ConstantDeclarations.CAT_BRND_COMBINATION_CODE);
					}
					else if(productVO.getReason().equalsIgnoreCase(ConstantDeclarations.COMBNTION_NOT_FOUND)) {
						SingleProdModel.setResponseCode(ConstantDeclarations.COMBNTION_NOT_FOUND_CODE);
					}
					else if(productVO.getReason().equalsIgnoreCase(ConstantDeclarations.ELIMINATION_RULE)) {
						SingleProdModel.setResponseCode(ConstantDeclarations.ELIMINATION_RULE_CODE);
					}
					else if(productVO.getReason().equalsIgnoreCase(ConstantDeclarations.INGR_NOT_FOUND)) {
						SingleProdModel.setResponseCode(ConstantDeclarations.INGR_NOT_FOUND_CODE);
					}
				}
				if(productVO.getReason().equalsIgnoreCase(ConstantDeclarations.ZERO_INGR_SCO)) {
					
					start = System.currentTimeMillis();
					productScoreAnalysisModel.setProductNotRecommeded(getProductNotRecommeded(productVO,eliminatedAllIngredients,ingredientDetail,popRate));
					end = System.currentTimeMillis();
					System.out.println("TOTAL SECONDS TAKEN BY ProductNotRecommeded :: " + (end-start)/1000 + " Sec + " + (end-start) + " ms" );
					
				}
				//Added to show Elimination Reason Allergy - Shivam - 25-08-2023 - Start
				else if((productVO.getReason().equalsIgnoreCase(ConstantDeclarations.ALLERGY) || productVO.getReason().equalsIgnoreCase(ConstantDeclarations.ZERO_INGR_SCO_ALLERGY)) && (!getAllergicIngredients().isEmpty())) { 
						
					productScoreAnalysisModel.setProductNotRecommeded(getProductNotRecommededForAllergy(master));
				}
				//Added to show Elimination Reason Allergy - Shivam - 25-08-2023 - End
				
				SingleProdModel.setProductScoreAnalysis(productScoreAnalysisModel);
//				SingleProdModel.setProductType(productVO.getProdType());
				SingleProdModel.setProductScore("0.0");
//				SingleProdModel.setProductMasterType(productVO.getMasterProdType());
//				SingleProdModel
				return SingleProdModel;
			}
			//prodList = productRec.getCustomerReviewStars(prodList);
//			System.out.println("SKIN CONCERN Size :: " + cp.getSkinConcernList().size());
//			cleanse = (ArrayList) prodList.get(0);
//			System.out.println("CLENSERS :: " + cleanse);
//			moisturize = (ArrayList) prodList.get(1);
//			System.out.println("MOIST :: " + moisturize);
//			treat = (ArrayList) prodList.get(2);
//			System.out.println("TREAT :: " + treat);
//			etreat = (ArrayList) prodList.get(3);
//			System.out.println("EYE TREAT :: " + etreat);
//			suncare = (ArrayList) prodList.get(4);
//			System.out.println("SUNCARE :: " + suncare);
			//primer = (ArrayList) prodList.get(10);
			start = System.currentTimeMillis();
			productIds = getProductIds((ArrayList)prodList, null, null, null, null);
			end = System.currentTimeMillis();
			System.out.println("TOTAL SECONDS TAKEN BY productIds :: " + (end-start)/1000 + " Sec + " + (end-start) + " ms" );
//			start = System.currentTimeMillis();
//			HashMap ingredientsWithGlossary = service.getIngredientNamesWithGlossary();
//			end = System.currentTimeMillis();
//			System.out.println("TOTAL SECONDS TAKEN BY ingredientsWithGlossary :: " + (end-start)/1000 + " Sec + " + (end-start) + " ms" );
			start = System.currentTimeMillis();
			bumpscores = (Hashtable) recEngDebug.getBumpScores();
			end = System.currentTimeMillis();
			System.out.println("TOTAL SECONDS TAKEN BY bumpscores :: " + (end-start)/1000 + " Sec + " + (end-start) + " ms" );
			profiles = getProfile(cp);
//			Hashtable funcnames = popRate.getFunctionDetailsHT();
			start = System.currentTimeMillis();
//			Hashtable mastertwo = productRec.getMasterDataTwo();
			end = System.currentTimeMillis();
			System.out.println("TOTAL SECONDS TAKEN BY mastertwo :: " + (end-start)/1000 + " Sec + " + (end-start) + " ms" );
			
			start = System.currentTimeMillis();
			if (productIds != null && productIds.size() > 0) {
				for (int i = 0; i < productIds.size(); i++) {
					pid = (String) productIds.get(i);
					prid = pid;
					pid = "('" + pid + "')";
					productsHashtable = (Hashtable) getBeautyProductInfo(pid); // gets the product
					productsHashtable = recEngDebug.getProductsIngredients("", "prodId", "", productsHashtable, pid, true); //Single changes
					product = (ProductVO) productsHashtable.get(prid);
					product = (ProductVO) recEngDebug.ratingsIndividual(cp, product, ingrratings);
					ProductVO pro = new ProductVO();
//					List<WhyRecomondationModel> whyRecomondationModelList = new ArrayList<>();
					ProductScoreAnalysisModel productScoreAnalysisModel = new ProductScoreAnalysisModel();
					
				
					boolean flag = false;
					for (int j = 0; j < profiles.size(); j++) {
						String profileAtt = (String) profiles.get(j);
						String ptype = product.getProdType();
						pmtype = product.getMasterProdType();
						Hashtable pBaseRatings = product.getProfileBaseRatings();
						Hashtable pIngrRatings = product.getProfileIngrRatings();
						int pbasescore = 0;
						int pbasecount = 0;
						int pbasepercen = 0;
						int pingrscore = 0;
						int pingrcount = 0;
						int pingrpercen = 0;
						String bumpkey = profileAtt + ptype;
						String bscore = (String) bumpscores.get(bumpkey);
						if (pBaseRatings.get(profileAtt) != null) {
							String pbasescorestring = (String) pBaseRatings.get(profileAtt);
							StringTokenizer stp = new StringTokenizer(pbasescorestring, "::");
							String stps = stp.nextToken();
							String stpc = stp.nextToken();
							pbasescore = Integer.parseInt(stps);
							pbasecount = Integer.parseInt(stpc);
						}
						if (pIngrRatings.get(profileAtt) != null) {
							String pingrscorestring = (String) pIngrRatings.get(profileAtt);
							StringTokenizer stp = new StringTokenizer(pingrscorestring, "::");
							String stps = stp.nextToken();
							String stpc = stp.nextToken();
							pingrscore = Integer.parseInt(stps);
							pingrcount = Integer.parseInt(stpc);
						}
						float ingrpercentage = 0.0f;
						float basepercentage = 0.0f;
						if (pingrcount != 0)
							pingrpercen = 100 * pingrscore / (pingrcount * 10);
						if (pbasecount != 0)
							pbasepercen = 100 * pbasescore / (pbasecount * 10);
						float pingrpercentage = Float.parseFloat(String.valueOf(pingrpercen));
						float pbasepercentage = Float.parseFloat(String.valueOf(pbasepercen));
						float score = 0.0f;
						float bumpscore = 0.0f;
						if (bscore != null && bscore.length() != 0)
							bumpscore = Float.parseFloat(String.valueOf(bscore));
						if (pmtype.equals("0004")) {
							ingrpercentage = (pingrpercentage * 100f) / 100f;
							score = ingrpercentage;
						} else {
							if (pbasepercentage == 0.0f) {
								ingrpercentage = (pingrpercentage * 100f) / 100f;
								score = ingrpercentage;
							} else {
								basepercentage = (pbasepercentage * 20f) / 100f;
								ingrpercentage = (pingrpercentage * 80f) / 100f;
								score = ingrpercentage + basepercentage;
							}
						}
						score = score + bumpscore;
						scoreWiseRatings.put(profileAtt, "" + score);
						pro = (ProductVO) prodList.get(0);
					
						// START : ADDED By Synoptek::HARESH: Changes loop for Regimen wise 
																	
//						if(!flag) {
//							if(cleancnt <= 5 && ConstantDeclarations.PRODUCT_MASTER_TYPE_CLEANSE.equals(pmtype)) {
//								pro = (ProductVO) ((ArrayList)cleanse.get(0)).get(cleancnt);
//								flag=true;
//								cleancnt++;
//							} else if(moistcnt <= 3 && ConstantDeclarations.PRODUCT_MASTER_TYPE_MOIST.equals(pmtype)) {
//								pro = (ProductVO) ((ArrayList)moisturize.get(0)).get(moistcnt);
//								flag=true;
//								moistcnt++;
//							} else if(treatcnt <= 9 && ConstantDeclarations.PRODUCT_MASTER_TYPE_TREAT.equals(pmtype)) {
//								pro = (ProductVO) ((ArrayList)treat.get(0)).get(treatcnt);
//								flag=true;
//								treatcnt++;
//							} else if(eyecnt <= 1 && ConstantDeclarations.PRODUCT_MASTER_TYPE_ETREAT.equals(pmtype)) {
//								pro = (ProductVO) ((ArrayList)etreat.get(0)).get(eyecnt);
//								flag=true;
//								eyecnt++;
//							} else if(suncnt < 1 && ConstantDeclarations.PRODUCT_MASTER_TYPE_UV.equals(pmtype)) {
//								pro = (ProductVO) ((ArrayList)suncare.get(0)).get(suncnt);
//								flag=true;
//								suncnt++;
//							} else if(primercnt < 1 && ConstantDeclarations.PRODUCT_MASTER_TYPE_PRIMER.equals(pmtype)) {
//								pro = (ProductVO)((ArrayList)primer.get(0)).get(primercnt);
//								flag=true;
//								primercnt++;
//							}
//						} 
						// END : ADDED By Synoptek::HARESH : Changes loop for Regimen wise 
						
						/** Get result after second time recommendation check */
//						WhyRecomondationModel whyRecomondationModel = new WhyRecomondationModel();
//						if (scoreWiseRatings != null && !scoreWiseRatings.isEmpty()
//								&& !getproductRating(Float.valueOf(scoreWiseRatings.get(profileAtt).toString()))
//										.equals(RATING_CONSTANT)
//								&& master != null && !master.isEmpty()) {
//							System.out.println("profileAtt :: " + profileAtt);
//							whyRecomondationModel.setSkinConcern(master.get(profileAtt).toString());
//							System.out.println("master.get(profileAtt).toString() :: " + master.get(profileAtt).toString());
//							whyRecomondationModel.setSkinConcern(master.get(profileAtt).toString());
//							whyRecomondationModel.setProductRating(
//									getproductRating(Float.valueOf(scoreWiseRatings.get(profileAtt).toString())));
//							whyRecomondationModel.setActiveIngredient(
//									getSeeIngredientModel(funcnames, mastertwo, popRate, prid, profileAtt));
//
//							whyRecomondationModelList.add(whyRecomondationModel);
//						}
					}
			
					long start1,end1;
					
					start1 = System.currentTimeMillis();
					HashMap ingredientsWithGlossary = service.getIngredientNamesWithGlossaryNew(pro.getProdId());
					end1 = System.currentTimeMillis();
					System.out.println("TOTAL SECONDS TAKEN BY ingredientsWithGlossary :: " + (end1-start1)/1000 + " Sec + " + (end1-start1) + " ms" );
					
					List<String> bodyAttributeList = new ArrayList<>();
					bodyAttributeList.add(cp.getSkinType());
					bodyAttributeList.addAll(cp.getSkinConditionList());
					bodyAttributeList.addAll(cp.getSkinConcernList());
					Hashtable<String,String> functext = popRate.getFunctionAttributeText(bodyAttributeList);
					ProductDetailsBean ingredientDetail = service.getProductDetails(pro.getProdId());
					Hashtable<String,String> ingredientsName = ingredientDetail.getIngredientsName();
					Hashtable<String,List<String>> ingredient = pro.getIndividualingrratings();
					Hashtable<String,String> funcingr = popRate.getIngredientFunction(pro.getProdId());
					Hashtable<String,String[][]> funcnames = popRate.getFunctionDetailsHT();
					Hashtable<String, String> ingredientGrpFunc = popRate.getIngredientGrpFunction(ingredientDetail);
					Hashtable<String,String[][]> grpfuncnames = popRate.getGroupFunctionDetails();
					
					productScoreAnalysisModel.setHeaderText(getHeaderText(recomondedproductModel,master));
					start1 = System.currentTimeMillis();
					HeroIngredientsSection heroIngredientsSection = new HeroIngredientsSection();
					productScoreAnalysisModel.setHeroIngredients(heroIngredientsSection.getHeroIngredients(ingredient,ingredientsWithGlossary,master,recomondedproductModel,ingredientsName,funcingr,funcnames));	
					end1 = System.currentTimeMillis();
					System.out.println("TOTAL SECONDS TAKEN BY HeroIngredients :: " + (end1-start1)/1000 + " Sec + " + (end1-start1) + " ms" );
					
					
					List<String> ingredientsIdList = new ArrayList<String>();
					start1 = System.currentTimeMillis();
					NotForYourSkinSection notForYourSkinSection = new NotForYourSkinSection();
					List<String> ingredientIdList = new ArrayList<>(ingredient.keySet());
					Hashtable<String,String> cautionHashtable = popRate.getCautionText(bodyAttributeList,ingredientIdList);
					//Added by Shivam For caution section - 08/09/2023 - Start
					HashMap<String, String> genericGroupFunctionTexts = (HashMap<String, String>) popRate.getGenericGroupFunctionTexts();
					//Added by Shivam For caution section - 08/09/2023 - End
					productScoreAnalysisModel.setNotForYourSkin(notForYourSkinSection.getNotForYourSkin(ingredient,ingredientsName,cautionHashtable,ingredientsIdList,funcingr,funcnames,ingredientGrpFunc,grpfuncnames, genericGroupFunctionTexts)); //Added genericGroupFunctionTexts by Shivam - 08/09/2023
					end1 = System.currentTimeMillis();
					System.out.println("TOTAL SECONDS TAKEN BY NotForYourSkin :: " + (end1-start1)/1000 + " Sec + " + (end1-start1) + " ms" );
					
					
					start1 = System.currentTimeMillis();
					MoreGreatIngredientsSection moreGreatIngredientsSection = new MoreGreatIngredientsSection();
					//Added by Shivam For More Great Ingredients change (New Function Definitions) - 21/09/2023 - Start
					//productType
					String productType = pmtype.equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_ETREAT)
							? ConstantDeclarations.EYE_PRODUCT 
							: ConstantDeclarations.SKIN_PRODUCT ;
					String userSkinType = pmtype.equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_ETREAT)
							? ConstantDeclarations.ANY_SKIN_TYPE 
							: cp.getSkinType() ;
					log.info("Product Master Type::::::::" + pmtype );
					ArrayList<String> concernOrConditionList = new ArrayList<>();
					if(pmtype.equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_ETREAT)) {
						concernOrConditionList.addAll(cp.getEyeConcernList());
						log.info("Eye Product::::::::::::::" + concernOrConditionList);
					}else {
						concernOrConditionList.addAll(cp.getSkinConditionList());
						concernOrConditionList.addAll(cp.getSkinConcernList());
						log.info("Skin Product:::::::::::::" + concernOrConditionList);
					}
					//Added (MoreGreatFunctionGenericTextsDTO as a data transfer object) parameters in getMoreGreatIngredients() method - Shivam
					//Add new parameters in MoreGreatFunctionGenericTextsDTO for method getMoreGreatIngredients()
					MoreGreatFunctionGenericTextsDTO dto = new MoreGreatFunctionGenericTextsDTO();
					dto.setProductType(productType);
					dto.setUserSkinType(userSkinType);
					dto.setConcernOrConditionList(concernOrConditionList);
					dto.setPopRate(popRate);
					productScoreAnalysisModel.setMoreGreatIngredients(moreGreatIngredientsSection.getMoreGreatIngredients(ingredientsName,ingredient,funcingr,funcnames,functext, dto));
					//Added by Shivam For More Great Ingredients change (New Function Definitions) - 21/09/2023 - End
					end1 = System.currentTimeMillis();
					System.out.println("TOTAL SECONDS TAKEN BY MoreGreatIngredients :: " + (end1-start1)/1000 + " Sec + " + (end1-start1) + " ms" );

					start1 = System.currentTimeMillis();
					NotHaveTheseBadIngredientsSection notHaveTheseBadIngredientsSection = new NotHaveTheseBadIngredientsSection();
					productScoreAnalysisModel.setNotHaveTheseBadIngredients(notHaveTheseBadIngredientsSection.getNotHaveTheseBadIngredients(popRate,ingredientsIdList,master,bodyAttributeList,product,cp.getAllergyList()));
					end1 = System.currentTimeMillis();
					System.out.println("TOTAL SECONDS TAKEN BY NotHaveTheseBadIngredients :: " + (end1-start1)/1000 + " Sec + " + (end1-start1) + " ms" );
					
					
					start1 = System.currentTimeMillis();
					CleanScoreSection cleanScoreSection = new CleanScoreSection();
					productScoreAnalysisModel.setCleanScore(cleanScoreSection.getCleanScore(ingredientDetail,popRate,ingredientsName));
					end1 = System.currentTimeMillis();
					System.out.println("TOTAL SECONDS TAKEN BY CleanScore :: " + (end1-start1)/1000 + " Sec + " + (end1-start1) + " ms" );

					
					prodModel = new SingleProductScoreResponseModel();
					String catgorySubCategory[] = service.getProductCategorySubCategory(pro.getProdId());
					prodModel.setProductId(pro.getProdId());
					prodModel.setSku(pro.getSku());
					prodModel.setUpc(pro.getUpc());
//					prodModel.setProductName(pro.getProdName());
//					prodModel.setBrandId(pro.getProductLine());
//					prodModel.setProductType(catgorySubCategory[1]);
					
					// Add/Deduct Scores as per Business Rule Changes - Shivam - 04/10/2024 - Start
					ProductRecommend.addDeductScoresAsPerBusinessRules(pro);
					// Add/Deduct Scores as per Business Rule Changes - Shivam - 04/10/2024 - End
					
					prodModel.setProductScore(pro.getScore());
//					prodModel.setProductMasterType(catgorySubCategory[0]);
//					prodModel.setWhenToUse(pro.getWhenToUse());
//					prodModel.setMultiBenefits(pro.getMultiBenefits());
//					prodModel.setLifeStyleBasedRecs1(pro.getLifeStyleBasedRecs1());
//					prodModel.setLifeStyleBasedRecs2(pro.getLifeStyleBasedRecs2());
					// Get RX flag from the sv_product_rx_warning for ingredients in the product - Start
//					prodModel.setFlagRXAcne(pro.getFlagAcne());
//					prodModel.setFlagRXRetinol(pro.getFlagRetinolt());
//					prodModel.setFlagTropicalSteriods(pro.getFlagSteriods());
					// Get RX flag from the sv_product_rx_warning for ingredients in the product - End
//					prodModel.setWhyRecommended(whyRecomondationModelList);
					prodModel.setProductScoreAnalysis(productScoreAnalysisModel);
					ArrayList<IngredientModel> ingredientList = new ArrayList<>();
					if (pro.getIngredients() != null && pro.getIngredients().length > 0 && ingredientDetail != null
							&& !ingredientDetail.getIngredientIdList().isEmpty()) {
						for (int k = 0; k < pro.getIngredients().length; k++) {
							IngredientModel ingredientModel = new IngredientModel();
							ingredientModel.setIngredientId(ingredientDetail.getIngredientIdList().get(k).toString());
							ingredientModel.setIngredientName(ingredientDetail.getIngredients().get(k).toString().trim());
							ingredientModel.setDisplayOrder(ingredientDetail.getDisplayOrder().get(k).toString());
							if (ingredientsWithGlossary.get(ingredientDetail.getIngredients().get(k).toString().trim().toUpperCase()) != null) {
								ingredientModel.setIngredientDefination((String)ingredientsWithGlossary.get(ingredientDetail.getIngredients().get(k).toString().trim().toUpperCase()));
							} else {
								ingredientModel.setIngredientDefination("");
							}
							ingredientList.add(ingredientModel);
						}
						prodModel.setIngredients(ingredientList);
					}
//					prodModel.setWarningText(getWarningText(pro,cp,master));
					prodModel.setResponseCode(ConstantDeclarations.PROD_SCORE_SUCCESS_CODE);
					prodModel.setResponseMessage(ConstantDeclarations.PROD_SCORE_SUCCESS);
//					if (pmtype.equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_CLEANSE)) {
//						cleanserList.add(prodModel);
//					} else if (pmtype.equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_MOIST) || pmtype.equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_UV) || pmtype.equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_PRIMER)) {
//						moisturizerList.add(prodModel);
//					} else if (pmtype.equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_TREAT)) {
//						if (pro.getComments().equalsIgnoreCase(ConstantDeclarations.DAILY))
//							dailyTreatmentList.add(prodModel);
//						else if (pro.getComments().equalsIgnoreCase(ConstantDeclarations.TARGETEDONE))
//							targetedOneTreatmentList.add(prodModel);
//						else if (pro.getComments().equalsIgnoreCase(ConstantDeclarations.TARGETEDTWO))
//							tagetedTwoTreatmentList.add(prodModel);
//						else if (pro.getComments().equalsIgnoreCase(ConstantDeclarations.WEEKLY))
//							weeklyTreatmentList.add(prodModel);
//						else if (pro.getComments().equalsIgnoreCase(ConstantDeclarations.ATHOMEWEEKLY))
//							atHomeFacialWeeklyTreatmentList.add(prodModel);
//					} else {
//						eyeCareList.add(prodModel);
//					}
				}
			}
			end = System.currentTimeMillis();
			System.out.println("TOTAL SECONDS TAKEN BY res :: " + (end-start)/1000 + " Sec + " + (end-start) + " ms" );
			
//			recomondedProductDetailDTO.setCleanserList(cleanserList);
//			recomondedProductDetailDTO.setMoisturizerList(moisturizerList);
//			recomondedProductDetailDTO.setDailyTreatmentList(dailyTreatmentList);
//			recomondedProductDetailDTO.setTargetedOneTreatmentList(targetedOneTreatmentList);
//			recomondedProductDetailDTO.setTargetedTwoTreatmentList(tagetedTwoTreatmentList);
//			recomondedProductDetailDTO.setWeeklyTreatmentList(weeklyTreatmentList);
//			recomondedProductDetailDTO.setAtHomeFacialWeeklyTreatmentList(atHomeFacialWeeklyTreatmentList);
//			recomondedProductDetailDTO.setEyeProductList(eyeCareList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		productModelDTO = prodModel;
		return productModelDTO;
	}

//	private Hashtable getingredientsName(ProductDetailsBean ingredientDetail) {
//		Hashtable ingredientsName = new Hashtable<>();
//		List ingredientIdlList = ingredientDetail.getIngredientIdList();
//		List ingredientNameList = ingredientDetail.getIngredients();
//		System.out.println(ingredientIdlList.toString());
//		System.out.println(ingredientNameList.toString());
//		if(!ingredientIdlList.isEmpty() && !ingredientNameList.isEmpty() && (ingredientIdlList.size() == ingredientNameList.size())) {
//			for(int i=0; i<ingredientIdlList.size();i++) {
//				ingredientsName.put(ingredientIdlList.get(i), ingredientNameList.get(i));
//			}
//		}
//		
//		return ingredientsName;
//	}

	


	private String getHeaderText(RecomondationProductModel recomondedproductModel, Hashtable<String, String> master) {
		
		StringBuilder headerText = new StringBuilder();
		if(recomondedproductModel.getSkin_type() != null) {
			headerText.append("For your " + master.get(recomondedproductModel.getSkin_type()).toLowerCase());
		}
		List<String> skinList = Arrays.asList(recomondedproductModel.getSkin_concern());
		if(skinList != null && !skinList.isEmpty()) {
			if(!skinList.contains(ConstantDeclarations.SKIN_NO_CONCERNS)) {
				if(skinList.size() == 1) {
					headerText.append(" with concern of "+master.get(skinList.get(0)).toLowerCase()+".");
				}
				else {
					headerText.append(" with concerns of ");								
					headerText.append(master.get(skinList.get(0)).toLowerCase());
					for(int z=1; z<skinList.size();z++) {
						if(z == (skinList.size()-1)) {
							headerText.append(" and " + master.get(skinList.get(z)).toLowerCase() + ".");
						}
						else {
							headerText.append(", " + master.get(skinList.get(z)).toLowerCase());								
						}
					}
				}
		   }  
		}
		
		return headerText.toString();
	}
	
	
	private List<ProductNotRecommededModel> getProductNotRecommededForAllergy(Hashtable<String, String> master) {
		
		List<ProductNotRecommededModel> productNotRecommededModels = new ArrayList<>();
		
		ProductNotRecommededModel model = new ProductNotRecommededModel();
		
		StringBuilder responseMessageText = new StringBuilder(master.get(getAllergicIngredients().get(0)));
		for(int allergyIndex = 1; allergyIndex < getAllergicIngredients().size(); allergyIndex++) {
			if(allergyIndex == getAllergicIngredients().size()-1) {
				responseMessageText.append(" and " + master.get(getAllergicIngredients().get(allergyIndex)));
			}else {
				responseMessageText.append(", " + master.get(getAllergicIngredients().get(allergyIndex)));
			}
		}
		String responseMessage = "<strong>We do not recommend this product</strong> for you because it contains <strong>("+responseMessageText.toString().toLowerCase()+")</strong>,  ingredients you indicated you do NOT want in your products!";
		model.setEliminationText(responseMessage);
		productNotRecommededModels.add(model);
		return productNotRecommededModels;
	}
	
	// Add/Deduct Scores as per Business Rule Changes - Shivam - 04/10/2024 - Start
	public static void addDeductScoresAsPerBusinessRules(ProductVO pro) {
		try {
			Float score = Float.parseFloat(pro.getScore());
			if (pro.getMasterProdType().equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_CLEANSE)) {
				// Add 5 points to all Cleansers
				score += 5f;
			} else if (pro.getMasterProdType().equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_TREAT)) {
				// Add 10 points to all Treatments
				score += 10f;
			} else if (pro.getMasterProdType().equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_MOIST)) {
				// Add 5 points to all Moisturizers
				score += 5f;
			} else if (pro.getMasterProdType().equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_ETREAT)) {
				// Deduct 5 points from EYE
				score -= 5f;
			}
			pro.setScore(score.toString());
		} catch (NumberFormatException | NullPointerException e) {
			System.err.println("Error while parsing: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static void addDeductScoresAsPerBusinessRules(ProductVO pro, float scoreToAdd) {
		try {
			Float score = Float.parseFloat(pro.getScore());
			score += scoreToAdd;
			pro.setScore(score.toString());
			ProductRecommend.addDeductScoresAsPerBusinessRules(pro);
		} catch (NumberFormatException | NullPointerException e) {
			System.err.println("Error while parsing: " + e.getMessage());
			e.printStackTrace();
		}
	}
	// Add/Deduct Scores as per Business Rule Changes - Shivam - 04/10/2024 - End

}