package com.svb.ratings.javabeans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import com.svb.common.valueobjects.CustomerProfileVO;
import com.svb.common.valueobjects.ProductVO;
import com.svb.struts.service.DetailsService;
import com.svb.webservices.model.RecIngredientModel;
import com.svb.webservices.model.RecProductModel;
import com.svb.webservices.model.RecProductWithIngrScoreModel;
import com.svb.webservices.serviceImpl.ProductRecommend;

public class ExportDataFeature extends RecommendationEngine {
	/**
	* Added by Synoptewk (20-01-2020)
	* added for export score related functionality
	*/
	private static Logger logger = Logger.getLogger(RecommendationEngine.class.getName());

	public ProductVO[] getProduct(Hashtable eligibleProductsHashtable, Hashtable eliminatedIngredients, 
			String customid, String prodtype,String prodids, String prodline, 
			CustomerProfileVO cp, String prodselect) {
		ProductVO productVOList[] = new ProductVO[0];
		RecommendationEngine rec = new RecommendationEngine();
		Hashtable ingrratings = new Hashtable();
		Hashtable master = new Hashtable();
		RecProductWithIngrScoreModel recProductWithIngrScoreModel = new RecProductWithIngrScoreModel();
				master = (Hashtable) rec.getMasterDataOne();
				eligibleProductsHashtable = getProductsIngredients(prodtype, prodselect, prodline, eligibleProductsHashtable); 
				ingrratings = getIngredientsRatingsAboveZero(cp, false);
				productVOList = getProductArray(productVOList, eligibleProductsHashtable);
				EleminateProducts ep = new EleminateProducts();
				System.out.println("ELE BEFORE");
				productVOList = ep.getProducts(cp, productVOList, eliminatedIngredients);
				System.out.println("ELE AFTER");
				productVOList = (ProductVO[]) ratings(cp, productVOList, ingrratings);
				BumpRating br = new BumpRating();
				productVOList = br.getProducts(cp, productVOList);
				ProductBumpRating pbr = new ProductBumpRating();
				productVOList = pbr.getProducts(cp, productVOList);
		 return productVOList;
	}

	public List<List<RecProductModel>> getProductWithIngrScoreList(ArrayList<ProductVO> vbnproductList) {
		List<List<RecProductModel>> recProductModelList = new ArrayList<>();
		List<RecProductModel> cleanserList = new ArrayList<>();
		List<RecProductModel> moisturizerList = new ArrayList<>();
		List<RecProductModel> skinTreatmentList = new ArrayList<>();
		List<RecProductModel> eyeCareList = new ArrayList<>();
		for (int i = 0; i < vbnproductList.size(); i++) {
			ProductVO prod = (ProductVO) vbnproductList.get(i);
			DetailsService service = new DetailsService();			
			RecProductModel recProduct = new RecProductModel();
			String [] subCategoryName = service.getProductCategorySubCategory(prod.getProdId());
			recProduct.setProductId(prod.getProdId());
			recProduct.setProductName(prod.getProdName());
			recProduct.setProductCategory(prod.getMcategoryName());
			recProduct.setProductSubCategory(subCategoryName[1]);
			recProduct.setProductWhenToUse(prod.getWhenToUse());
			
			// Add/Deduct Scores as per Business Rule Changes - Shivam - 07/10/2024 - Start
			ProductRecommend.addDeductScoresAsPerBusinessRules(prod, 10f);
			// Add/Deduct Scores as per Business Rule Changes - Shivam - 07/10/2024 - End
			
			recProduct.setScore(prod.getScore());
			recProduct.setBaseScore(prod.getBaseScore());
			recProduct.setIngrScore(prod.getIngrScore());
			recProduct.setIngrCount(prod.getIngrfnccnt());
			recProduct.setBaseCount(prod.getBaseCount());
			recProduct.setBrandName(prod.getProductLine());
			recProduct.setBaseWeightage(prod.getBaseWeightage());
			recProduct.setIngrWeightage(prod.getIngrWeightage());
			recProduct.setTotalIngrCount(String.valueOf(prod.getIngredients().length));
			if (prod.getBumpScore() == null)
				recProduct.setBumpScore(String.valueOf(0));
			else
				recProduct.setBumpScore(prod.getBumpScore());
			String[][] ingrArray = prod.getIngredients();
			ArrayList<RecIngredientModel> ingredientList = new ArrayList<>();
			if (ingrArray != null && ingrArray.length > 0) {
				for (int j = 0; j < ingrArray.length; j++) {
					RecIngredientModel ingredientModel = new RecIngredientModel();
					String ingrNamewithScore = ingrArray[j][2];
					if (ingrNamewithScore.contains("===") && ingrNamewithScore != null) {
						String[] arrOfStr = ingrNamewithScore.split("===");
						ingredientModel.setIngredientName(arrOfStr[0]);
						ingredientModel.setScore(arrOfStr[1]);
						ingredientList.add(ingredientModel);
					}
				}
			}
			recProduct.setIngredients(ingredientList);
			if (prod.getMcategoryName().equalsIgnoreCase("Cleansers")) {
				cleanserList.add(recProduct);
			} else if (prod.getMcategoryName().equalsIgnoreCase("Moisturizers")) {
				moisturizerList.add(recProduct);
			} else if (prod.getMcategoryName().equalsIgnoreCase("Treatments-Daily")) {
				skinTreatmentList.add(recProduct);
			} else {
				eyeCareList.add(recProduct);
			}

		}
		recProductModelList.add(cleanserList);
		recProductModelList.add(moisturizerList);
		recProductModelList.add(skinTreatmentList);
		recProductModelList.add(eyeCareList);
		return recProductModelList;
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
		// pause(lo, hi);
		if (lo >= hi) {
			return;
		} else if (lo == hi - 1) {
			/*
			 * sort a two element list by swapping if necessary
			 */
			if (Float.parseFloat(a[lo].getScore()) > Float.parseFloat(a[hi].getScore())) {
				ProductVO T = a[lo];
				a[lo] = a[hi];
				a[hi] = T;
			}
			return;
		}

		/*
		 * Pick a pivot and move it out of the way
		 */
		ProductVO pivot = a[(lo + hi) / 2];
		a[(lo + hi) / 2] = a[hi];
		a[hi] = pivot;

		while (lo < hi) {
			/*
			 * Search forward from a[lo] until an element is found that is greater than the
			 * pivot or lo >= hi
			 */
			while (Float.parseFloat(a[lo].getScore()) <= Float.parseFloat(pivot.getScore()) && lo < hi) {
				lo++;
			}

			/*
			 * Search backward from a[hi] until element is found that is less than the
			 * pivot, or lo >= hi
			 */
			while (Float.parseFloat(pivot.getScore()) <= Float.parseFloat(a[hi].getScore()) && lo < hi) {
				hi--;
			}

			/*
			 * Swap elements a[lo] and a[hi]
			 */
			if (lo < hi) {
				ProductVO T = a[lo];
				a[lo] = a[hi];
				a[hi] = T;
				// pause();
			}

		}

		/*
		 * Put the median in the "center" of the list
		 */
		a[hi0] = a[hi];
		a[hi] = pivot;

		/*
		 * Recursive calls, elements a[lo0] to a[lo-1] are less than or equal to pivot,
		 * elements a[hi+1] to a[hi0] are greater than pivot.
		 */
		sort(a, lo0, lo - 1);
		sort(a, hi + 1, hi0);
	}

	public ArrayList getProductListFromVO(ProductVO[] productVOList) {
		ArrayList vbprods = new ArrayList();
		ArrayList prodList = new ArrayList();

		for (int z = 0; z < productVOList.length; z++) {
			if (!productVOList[z].isTobeDeleted()) {
				vbprods.add(productVOList[z]);
			} 
		}
		vbprods = getSortedProducts(vbprods);
		return vbprods;
	}

	public ArrayList getEliminatedProductListFromVO(ProductVO[] productVOList) {
		ArrayList vbprods = new ArrayList();
		for (int z = 0; z < productVOList.length; z++) {
			if (productVOList[z].isTobeDeleted()) {
				vbprods.add(productVOList[z]);
			}
		}
		return vbprods;
	}

	public List<List<RecProductModel>> getEliminatedProductList(ArrayList prodlist) {
		ProductRecommendationEngine productRec = new ProductRecommendationEngine();
		Hashtable mastertwo = productRec.getMasterDataTwo();
		List<List<RecProductModel>> recProductModelList = new ArrayList<>();
		List<RecProductModel> cleanserList = new ArrayList<>();
		List<RecProductModel> moisturizerList = new ArrayList<>();
		List<RecProductModel> skinTreatmentList = new ArrayList<>();
		List<RecProductModel> eyeCareList = new ArrayList<>();
		for (int i = 0; i < prodlist.size(); i++) {
			ProductVO prod = (ProductVO) prodlist.get(i);
			DetailsService service = new DetailsService();			
			RecProductModel recProduct = new RecProductModel();
			String [] subCategoryName = service.getProductCategorySubCategory(prod.getProdId());
			recProduct.setProductId(prod.getProdId());
			recProduct.setProductName(prod.getProdName());
			recProduct.setProductCategory(prod.getMcategoryName());
			recProduct.setProductSubCategory(subCategoryName[1]);
			recProduct.setProductWhenToUse(prod.getWhenToUse());
			recProduct.setScore(prod.getScore());
			recProduct.setBaseScore(prod.getBaseScore());
			recProduct.setIngrScore(prod.getIngrScore());
			recProduct.setIngrCount(prod.getIngrfnccnt());
			recProduct.setBaseCount(prod.getBaseCount());
			recProduct.setBrandName(prod.getProductLine());
			recProduct.setReason(prod.getReason());
			if(mastertwo.get(prod.getEliminatedIngrName())!=null) {
				recProduct.setEliminatedIngrName(mastertwo.get(prod.getEliminatedIngrName()).toString());				
			}
			else {
				recProduct.setEliminatedIngrName(prod.getEliminatedIngrName());
			}
			if (prod.getMcategoryName().equalsIgnoreCase("Cleansers")) {
				cleanserList.add(recProduct);
			} else if (prod.getMcategoryName().equalsIgnoreCase("Moisturizers")) {
				moisturizerList.add(recProduct);
			} else if (prod.getMcategoryName().equalsIgnoreCase("Treatments-Daily")) {
				skinTreatmentList.add(recProduct);
			} else {
				eyeCareList.add(recProduct);
			}
		}
		recProductModelList.add(cleanserList);
		recProductModelList.add(moisturizerList);
		recProductModelList.add(skinTreatmentList);
		recProductModelList.add(eyeCareList);
		return recProductModelList;
	}

	public Hashtable getProductList(String customerId, String prodtype, String prodids, String prodline,
			CustomerProfileVO cp, String prodselect) {
		if (customerId == null || customerId.trim().length() == 0)
			customerId = "C10011";
		Hashtable zeroRatingEliminatedIngredients = new Hashtable();
		Hashtable all_products = new Hashtable();
		Hashtable products = new Hashtable();
		boolean sktpflag = false;
		try {
			cp = (CustomerProfileVO) getCustomerProfile(customerId, cp, "skin");
			if (cp.getSkinType() != null && cp.getSkinType().trim().length() > 0)
				sktpflag = true;
			if (sktpflag) {
				zeroRatingEliminatedIngredients = eliminateIngredientsBasedOnAttribute(cp.getSkinType(), cp.getSkinConditionList(), cp.getSpecification());
				all_products = getProdDetails(prodtype, prodselect, prodline, cp,"");
				products.put("product under Zero ratings elimination list",zeroRatingEliminatedIngredients);
				products.put("all products",all_products);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return products;
	}
}
