/*
 * Created on Mar 29, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.ratings.javabeans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import com.svb.common.ConstantDeclarations;
import com.svb.common.dba.SVDBOperation;
import com.svb.common.valueobjects.CustomerProfileVO;
import com.svb.common.valueobjects.ProductVO;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author VBRao
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ProductRecommendationEngine extends RecommendationEngine {
private TreeMap<Float,ArrayList<String>> finalmap = new TreeMap<Float,ArrayList<String>>(Collections.reverseOrder());
	public TreeMap<Float, ArrayList<String>> getFinalmap() {
	return finalmap;
}

public void setFinalmap(TreeMap<Float, ArrayList<String>> finalmap) {
	this.finalmap = finalmap;
}
	private static Logger logger = Logger.getLogger(RecommendationEngine.class
			.getName());

	public ArrayList getProduct(String customid, String prodtype,
			String prodids, String prodline, CustomerProfileVO cp, String prodselect) {
		if (customid == null || customid.trim().length() == 0)
			customid = "C10011";

		ArrayList ProdList = new ArrayList();
		String debugline = "";
		ProductVO productVOList[] = new ProductVO[0];
		Hashtable eliminatedIngredients = new Hashtable();
		Hashtable productsHashtable = new Hashtable();
		Hashtable ingrratings = new Hashtable();
		
		/**
		* Aded by Synoptek (25-05-2020)
		* Seperated eliminated products from all products list
		*/
		Hashtable all_products = new Hashtable();
		Hashtable eliminated_products = new Hashtable();



		boolean sktpflag = false;
		try {

			cp = (CustomerProfileVO) getCustomerProfile(customid, cp, "skin"); 
			if (cp.getSkinType() != null
					&& cp.getSkinType().trim().length() > 0)
				sktpflag = true;
			if (sktpflag) {

				eliminatedIngredients = eliminateIngredients(cp.getSkinType(),
						cp.getSkinConditionList()); //this method gets all the
				// ingredients that r to be
				// eliminated for that
				// skintype and skincondition
				all_products = getProdDetails(prodtype, prodselect, prodline,cp,""); //gets
				// the
				// product
				// details and filel
				// "prodhash" variable
				productsHashtable = (Hashtable) all_products.get("eligible products");
				eliminated_products=(Hashtable) all_products.get("eliminated products");
				productsHashtable = getProductsIngredients(prodtype, prodselect,
						prodline, productsHashtable); //this method
				
//				System.out.println(productsHashtable.size()) ;
				// gets the
				// ingredients of
				// that product and fill in
				//prodhash variable
				ingrratings = getIngredientsRatings(cp, false);
				//fillProductArray(); //this method is importanat this method
				// fills the "product[]" variable with the
				// products present in "prodhash" variable .

				productVOList = getProductArray(productVOList,
						productsHashtable);

				EleminateProducts ep = new EleminateProducts();
				productVOList = ep.getProducts(cp, productVOList,
						eliminatedIngredients);
                
			//	for (int i=0; i < productVOList.length ; i ++)
			//	  System.out.println("vb 2 22 "+productVOList[i].getProdId()  ) ;
				
				productVOList = (ProductVO[]) ratings(cp, productVOList,
						ingrratings);
				BumpRating br = new BumpRating();
   	            productVOList = br.getProducts(cp, productVOList);//pass the
				// customerprofile and
				// product array as the
				// parameters for bump
				// calculations
				// remvoed comments 012909
				ProductBumpRating pbr = new ProductBumpRating();
				productVOList = pbr.getProducts(cp, productVOList);//pass the
				//removed comments
		//		for (int i=0; i < productVOList.length ; i ++)
		//			  System.out.println("vb 2 33 "+productVOList[i].getProdId()  ) ;

				int z1 = 0;
				int z2 = 0;
				ArrayList vbprods = new ArrayList();
				for (int z = 0; z < productVOList.length; z++) {
					//					//logger.log(Level.DEBUG,"prd
					// "+productVOList[z].getProdId()+"
					// "+productVOList[z].isTobeDeleted()+"
					// "+productVOList[z].getProdName()) ;
		//			System.out.println("vb deleted "+productVOList[z].getProdId()+"  "+productVOList[z].isTobeDeleted()) ;
					if (!productVOList[z].isTobeDeleted()) {
						vbprods.add(productVOList[z]);
					}
				}
				ProductVO[] vbproduct = new ProductVO[vbprods.size()];
				
		//		for (int i=0; i < vbprods.size(); i ++)
		//			  System.out.println("vb 2 44 "+((ProductVO)vbprods.get(i)).getProdId()   ) ;
				
				for (int vz = 0; vz < vbprods.size(); vz++)
					vbproduct[vz] = (ProductVO) vbprods.get(vz);

				ArrayList vbnproduct = new ArrayList();
				for (int i = 0; i < vbproduct.length; i++) {
					//						    if ( Math.round(Float.parseFloat(product[i].getScore()))
					// > 60.0 ) // commented
				//	if (vbproduct[i].getMasterProdType().equals("0005"))
				//	  System.out.println("vbscore "+vbproduct[i].getMasterProdType()+"  "+vbproduct[i].getProdId()+"  "+vbproduct[i].getScore()) ;
					vbnproduct.add(vbproduct[i]);
				}
				//				//logger.log(Level.DEBUG,"0000 vbnproduct size
				// "+vbnproduct.size()) ;
				vbnproduct = getSortedProducts(vbnproduct);
				
				//System.out.println("vbbbb 1 "+vbnproduct.size()) ;
			//	for (int i=0; i < vbnproduct.size() ; i ++)
			//		  System.out.println("vb 2 22 "+((ProductVO)vbnproduct.get(i)).getProdId()  ) ;
				
				if (!debugline.equals("debug")) {
					productVOList = new ProductVO[0];
					//logger.log(Level.DEBUG,"aa vbnproduct size
					// "+vbnproduct.size()) ;
					//					vbnproduct = (ArrayList) getProductDetailAssortNew(
					//							vbnproduct, prodtype, cp, prodline);
					//logger.log(Level.DEBUG,"vbnproduct size
					// "+vbnproduct.size()) ;
					vbnproduct = (ArrayList) getBeautyProductAssortNoBrand(
							vbnproduct, prodtype, cp, prodline);
					//System.out.println("vbbbb 2 "+vbnproduct.size()) ;
					ProdList = vbnproduct ;
//ggg					manageCustomerRegimen(customid, repid, prodline, vbnproduct);
//					vbnproduct = (ArrayList) getRegimenAssortNew(vbnproduct,
//							prodtype, cp, prodline);
//					productVOList = new ProductVO[vbnproduct.size()];
//					for (int vz = 0; vz < vbnproduct.size(); vz++)
//						productVOList[vz] = (ProductVO) vbnproduct.get(vz);

				} else {
					ArrayList vbnnprod = new ArrayList();
					//					//logger.log(Level.DEBUG,"prod type " + prodtype);
					for (int vbn = 0; vbn < vbnproduct.size(); vbn++) {
						if (((ProductVO) vbnproduct.get(vbn))
								.getMasterProdType().trim().equals(
										prodtype.trim())) {
							/*
							 * //logger.log(Level.DEBUG,"prodid " + ((ProductVO)
							 * vbnproduct.get(vbn)) .getProdId() + "master prod
							 * type " + ((ProductVO) vbnproduct.get(vbn))
							 * .getMasterProdType());
							 */
							vbnnprod.add(vbnproduct.get(vbn));
						}
					}
					productVOList = new ProductVO[vbnnprod.size()];
					for (int vz = 0; vz < vbnnprod.size(); vz++)
						productVOList[vz] = (ProductVO) vbnnprod.get(vz);
					//logger.log(Level.DEBUG,"debugbug... " +
					// productVOList.length);
				}
				//				if(!externalDBO)
				//					destroy();
			} // sktpflag

		} catch (Exception e) {
			e.printStackTrace();
		}

		return ProdList;

	}

	private ArrayList getSortedProducts(ArrayList vect) {
		ProductVO[] pr = new ProductVO[vect.size()];
		//////logger.log(Level.DEBUG,"sdainfsalkf");
		for (int i = 0; i < vect.size(); i++) {
			//////logger.log(Level.DEBUG,"in getSortedproducts");
			((ProductVO) vect.get(i)).setScore(calculatescore((ProductVO) vect
					.get(i)));
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
				////logger.log(Level.DEBUG,"left over
				// product"+prod.getProdid()+"
				// left over score is"+prod.getScore());
			}
			//////logger.log(Level.DEBUG,"===="+pr[i].prodid+","+pr[i].score);
		}
		return vt;
	}

	private void sort(ProductVO a[], int lo0, int hi0) throws Exception {
		int lo = lo0;
		int hi = hi0;
		//pause(lo, hi);
		if (lo >= hi) {
			return;
		} else if (lo == hi - 1) {
			/*
			 * sort a two element list by swapping if necessary
			 */
			if (Float.parseFloat(a[lo].getScore()) > Float.parseFloat(a[hi]
					.getScore())) {
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
			 * Search forward from a[lo] until an element is found that is
			 * greater than the pivot or lo >= hi
			 */
			while (Float.parseFloat(a[lo].getScore()) <= Float.parseFloat(pivot
					.getScore())
					&& lo < hi) {
				lo++;
			}

			/*
			 * Search backward from a[hi] until element is found that is less
			 * than the pivot, or lo >= hi
			 */
			while (Float.parseFloat(pivot.getScore()) <= Float.parseFloat(a[hi]
					.getScore())
					&& lo < hi) {
				hi--;
			}

			/*
			 * Swap elements a[lo] and a[hi]
			 */
			if (lo < hi) {
				ProductVO T = a[lo];
				a[lo] = a[hi];
				a[hi] = T;
				//pause();
			}

		}

		/*
		 * Put the median in the "center" of the list
		 */
		a[hi0] = a[hi];
		a[hi] = pivot;

		/*
		 * Recursive calls, elements a[lo0] to a[lo-1] are less than or equal to
		 * pivot, elements a[hi+1] to a[hi0] are greater than pivot.
		 */
		sort(a, lo0, lo - 1);
		sort(a, hi + 1, hi0);
	}

	private void printArrayList(ArrayList vt) {
		for (int i = 0; i < vt.size(); i++)
		//	if (((ProductVO) vt.get(i)).getMasterProdType().equals(
			//		ConstantDeclarations.PRODUCT_MASTER_TYPE_ETREAT))
		//	{
			 System.out.println( ((ProductVO) vt.get(i)).getProdName()
					+ "\tscore\t" + ((ProductVO) vt.get(i)).getScore()
					+ "\tingr\t" + ((ProductVO) vt.get(i)).getIngrScore()
					+ "\tbase\t" + ((ProductVO) vt.get(i)).getBaseScore()
					+ "\tbump\t" + ((ProductVO) vt.get(i)).getBumpScore()
					+ "\tfn\t" + ((ProductVO) vt.get(i)).getFunctionScore()
					+ "\tprice\t" + ((ProductVO) vt.get(i)).getPrice());
		//	}
	}
	// To get Check if  product is Base Oil -- Start
	public boolean isOilBased(List<ProductVO> treatmentRule) {
		if (treatmentRule != null) {
			for (ProductVO productVO : treatmentRule) {
				if (ConstantDeclarations.DAILY.equalsIgnoreCase(productVO.getComments())
						&& productVO.getBase().equalsIgnoreCase(ConstantDeclarations.OILBASE)) {
					System.out.println("treatmentRule True");
					return true;
				}
			}
		}
		return false;
	}
	// To get Check if  product is Base Oil -- End
	
	// To get Highest product without Base Oil -- Start
	public Hashtable<String, Object> withoutOilMap(List<ProductVO> treatNightlyTreatForOil,
				List<ProductVO> treatDailyTreatForOil) {
			boolean trtTarHighScoreDailySerumHydrator = false;
			boolean trtTarHighScoreDailySerumRetinol = false;
			boolean trtTarHighScoreDailySerumVitamin = false;
			boolean trtDailyHighScoreSerum = false;
			boolean trtDailyHighScoreExfo = false;
			boolean trtTarHighScoreNightlySerumHydrator = false;
			boolean trtTarHighScoreNightlySerumRetinol = false;
			boolean trtTarHighScoreNightlySerumVitamin = false;
			float trtTarHighScoreDailySerumHydratorProScore = 0;
			float trtTarHighScoreDailySerumRetinolProScore = 0;
			float trtTarHighScoreDailySerumVitaminProScore = 0;
			float trtTarHighScoreNightlySerumHydratorProScore = 0;
			float trtTarHighScoreNightlySerumRetinolProScore = 0;
			float trtTarHighScoreNightlySerumVitaminProScore = 0;
			float trtTarHighScoreDailyOrNightlySerumProScore = 0;
			float trtTarHighScoreDailyOrNightlySerumHydratorProScore = 0;
			float trtTarHighScoreDailyOrNightlySerumRetinolProScore = 0;
			float trtTarHighScoreDailyOrNightlySerumVitaminProScore = 0;
			float trtTarHighScoreDailyOrNightlyExfoProScore = 0;
			int trtTarHighScoreDailyOrNightlySerumHydratorProId = 0;
			int trtTarHighScoreDailyOrNightlySerumRetinolProId = 0;
			int trtTarHighScoreDailyOrNightlySerumVitaminProId = 0;
			int trtTarHighScoreDailyOrNightlyExfoProId = 0;
			boolean trtTarHighScoreNightlySerum = false;
			int trtTarHighScoreDailySerumHydratorProId = 0;
			int trtTarHighScoreDailySerumRetinolProId = 0;
			int trtTarHighScoreDailySerumVitaminProId = 0;
			float trtDailyHighScoreSerumProScore = 0;
			float trtDailyHighScoreExfoProScore = 0;
			int trtTarHighScoreNightlySerumHydratorProId = 0;
			int trtTarHighScoreNightlySerumRetinolProId = 0;
			int trtTarHighScoreNightlySerumVitaminProId = 0;
			int trtTarHighScoreNightlySerumProId = 0;
			int trtTarHighScoreTargetedExfoliProId = 0;
			float trtTarHighScoreNightlySerumProScore = 0;
			float trtTarHighScoreTargetedExfoliProScore = 0;
			int trtDailyHighScoreSerumProId = 0;
			int trtDailyHighScoreExfoProId = 0;

			Hashtable<String, Object> categoryMapTreatmentAll = new Hashtable<String, Object>();
			System.out.println("treatDailyTreatForOil" + treatDailyTreatForOil.size());
			System.out.println("treatNightlyTreatForOil" + treatNightlyTreatForOil.size());
			if (treatNightlyTreatForOil.size() > 0) {
				System.out.println("treatNightlyTreatForOil.isEmpty" + treatNightlyTreatForOil.size());
				for (int i = 0; i < treatNightlyTreatForOil.size(); i++) {
					System.out.println("treatNightlyTreatForOil.size" + treatNightlyTreatForOil.size());
					// no.1 Nightly treatment Serum product
					if (!trtTarHighScoreNightlySerum
							&& treatDailyTreatForOil.get(i).getProdType().equals(ConstantDeclarations.SERUM)) {
						trtTarHighScoreNightlySerumProId = i;
						trtTarHighScoreNightlySerumProScore = Float.valueOf(treatDailyTreatForOil.get(i).getScore());
						trtTarHighScoreNightlySerum = true; // to indicate product received
						// Added For exception22-03-2021-Start
						System.out.println("treatNightlyTreatForOil Step 1" + treatNightlyTreatForOil.size());
						categoryMapTreatmentAll.put("Serum-Night", treatDailyTreatForOil.get(i));

						// Added For exception22-03-2021-End
					}
					// Display rule changes - Start
					// no.1 Exfoliator product where When to Use = �Daily Treatment�
					else if (!trtTarHighScoreNightlySerumHydrator
							&& treatDailyTreatForOil.get(i).getProdType().equals(ConstantDeclarations.SERUMSHYDRATOR)) {
						trtTarHighScoreNightlySerumHydratorProId = i;
						trtTarHighScoreNightlySerumHydratorProScore = Float
								.valueOf(treatDailyTreatForOil.get(i).getScore());
						trtTarHighScoreNightlySerumHydrator = true; // to indicate product received
						System.out.println("treatNightlyTreatForOil Step 2" + treatNightlyTreatForOil.size());

						categoryMapTreatmentAll.put("Serums-Hydrator-Night", treatDailyTreatForOil.get(i));
					}
					// no.1 Exfoliator product where When to Use = �Daily Treatment�
					else if (!trtTarHighScoreNightlySerumRetinol
							&& treatDailyTreatForOil.get(i).getProdType().equals(ConstantDeclarations.SERUM_RETINOL)) {
						trtTarHighScoreNightlySerumRetinolProId = i;
						trtTarHighScoreNightlySerumRetinolProScore = Float
								.valueOf(treatDailyTreatForOil.get(i).getScore());
						trtTarHighScoreNightlySerumRetinol = true; // to indicate product received
						System.out.println("treatNightlyTreatForOil Step 3" + treatNightlyTreatForOil.size());

						categoryMapTreatmentAll.put("Serums-Retinol-Night", treatDailyTreatForOil.get(i));

					}
					// no.1 Exfoliator product where When to Use = �Daily Treatment�
					else if (!trtTarHighScoreNightlySerumVitamin
							&& treatDailyTreatForOil.get(i).getProdType().equals(ConstantDeclarations.SERUMSVITAMINC)) {
						trtTarHighScoreNightlySerumVitaminProId = i;
						trtTarHighScoreNightlySerumVitaminProScore = Float
								.valueOf(treatDailyTreatForOil.get(i).getScore());
						trtTarHighScoreNightlySerumVitamin = true; // to indicate product received
						System.out.println("treatNightlyTreatForOil Step 4" + treatNightlyTreatForOil.size());
						categoryMapTreatmentAll.put("Serums-VitaminC-Night", treatDailyTreatForOil.get(i));

						// Display rule changes - End
					}
				}
			}

			String dailyCatgory = "";
			String treatmentCategory = "";
			TreeMap<Float, ArrayList<String>> map = new TreeMap<Float, ArrayList<String>>(Collections.reverseOrder());
			if (treatDailyTreatForOil.size() >= 0) {
				for (int i = 0; i < treatDailyTreatForOil.size(); i++) {
					// no.1 Serum product where When to Use = �Daily Treatment�
					if (!trtDailyHighScoreSerum
							&& treatDailyTreatForOil.get(i).getProdType().equals(ConstantDeclarations.SERUM)) {
						trtDailyHighScoreSerumProId = i;
						trtDailyHighScoreSerumProScore = Float.valueOf(treatDailyTreatForOil.get(i).getScore());
						trtDailyHighScoreSerum = true;
						if (dailyCatgory.equals("")) {
							dailyCatgory = "Serum-Daily";
						}
						categoryMapTreatmentAll.put("Serum-Daily", treatDailyTreatForOil.get(i));

					}
					// no.1 Exfoliator product where When to Use = �Daily Treatment�
					else if (!trtDailyHighScoreExfo
							&& treatDailyTreatForOil.get(i).getProdType().equals(ConstantDeclarations.EXFOLIATORS)) {// Display
																														// rule
																														// changes
						trtDailyHighScoreExfoProId = i;
						trtDailyHighScoreExfoProScore = Float.valueOf(treatDailyTreatForOil.get(i).getScore());
						trtDailyHighScoreExfo = true;
						if (dailyCatgory.equals("")) {
							dailyCatgory = "Exfoliators-Daily";
						}
						categoryMapTreatmentAll.put("Exfoliators-Daily", treatDailyTreatForOil.get(i));

					}

					// Display rule changes - Start
					// no.1 Exfoliator product where When to Use = �Daily Treatment�
					else if (!trtTarHighScoreDailySerumHydrator
							&& treatDailyTreatForOil.get(i).getProdType().equals(ConstantDeclarations.SERUMSHYDRATOR)) {
						trtTarHighScoreDailySerumHydratorProId = i;
						trtTarHighScoreDailySerumHydratorProScore = Float
								.valueOf(treatDailyTreatForOil.get(i).getScore());
						trtTarHighScoreDailySerumHydrator = true; // to indicate product received
						categoryMapTreatmentAll.put("Serums-Hydrator-Daily", treatDailyTreatForOil.get(i));

					}
					// no.1 Exfoliator product where When to Use = �Daily Treatment�
					else if (!trtTarHighScoreDailySerumRetinol
							&& treatDailyTreatForOil.get(i).getProdType().equals(ConstantDeclarations.SERUM_RETINOL)) {
						trtTarHighScoreDailySerumRetinolProId = i;
						trtTarHighScoreDailySerumRetinolProScore = Float
								.valueOf(treatDailyTreatForOil.get(i).getScore());
						trtTarHighScoreDailySerumRetinol = true; // to indicate product received
						categoryMapTreatmentAll.put("Serums-Retinol-Daily", treatDailyTreatForOil.get(i));

					}
					// no.1 Exfoliator product where When to Use = �Daily Treatment�
					else if (!trtTarHighScoreDailySerumVitamin
							&& treatDailyTreatForOil.get(i).getProdType().equals(ConstantDeclarations.SERUMSVITAMINC)) {
						trtTarHighScoreDailySerumVitaminProId = i;
						trtTarHighScoreDailySerumVitaminProScore = Float
								.valueOf(treatDailyTreatForOil.get(i).getScore());
						trtTarHighScoreDailySerumVitamin = true; // to indicate product received
						categoryMapTreatmentAll.put("Serums-VitaminC-Daily", treatDailyTreatForOil.get(i));

					}
					// Display rule changes - End
				}
			}

			// Display rule changes - Start
			if (trtDailyHighScoreSerumProScore >= trtTarHighScoreNightlySerumProScore) {
				trtTarHighScoreDailyOrNightlySerumProScore = trtTarHighScoreDailySerumHydratorProScore;
			} else if (trtDailyHighScoreSerumProScore < trtTarHighScoreNightlySerumProScore) {
				trtTarHighScoreDailyOrNightlySerumProScore = trtTarHighScoreNightlySerumHydratorProScore;
			}
			ArrayList<String> lst = new ArrayList<String>();
			lst.add("Serum");
			map.put(trtTarHighScoreDailyOrNightlySerumProScore, lst);
			if (trtTarHighScoreDailySerumHydratorProScore >= trtTarHighScoreNightlySerumHydratorProScore) {
				trtTarHighScoreDailyOrNightlySerumHydratorProId = trtTarHighScoreDailySerumHydratorProId;
				trtTarHighScoreDailyOrNightlySerumHydratorProScore = trtTarHighScoreDailySerumHydratorProScore;
			} else if (trtTarHighScoreDailySerumHydratorProScore < trtTarHighScoreNightlySerumHydratorProScore) {
				trtTarHighScoreDailyOrNightlySerumHydratorProId = trtTarHighScoreNightlySerumHydratorProId;
				trtTarHighScoreDailyOrNightlySerumHydratorProScore = trtTarHighScoreNightlySerumHydratorProScore;
			}
			if (map.containsKey(trtTarHighScoreDailyOrNightlySerumHydratorProScore)) {
				map.get(trtTarHighScoreDailyOrNightlySerumHydratorProScore).add("Serums-Hydrator");
			} else {
				lst = new ArrayList<String>();
				lst.add("Serums-Hydrator");
				map.put(trtTarHighScoreDailyOrNightlySerumHydratorProScore, lst);
			}
			if (trtTarHighScoreDailySerumRetinolProScore >= trtTarHighScoreNightlySerumRetinolProScore) {
				trtTarHighScoreDailyOrNightlySerumRetinolProId = trtTarHighScoreDailySerumRetinolProId;
				trtTarHighScoreDailyOrNightlySerumRetinolProScore = trtTarHighScoreDailySerumRetinolProScore;
			} else if (trtTarHighScoreDailySerumRetinolProScore < trtTarHighScoreNightlySerumRetinolProScore) {
				trtTarHighScoreDailyOrNightlySerumRetinolProId = trtTarHighScoreNightlySerumRetinolProId;
				trtTarHighScoreDailyOrNightlySerumRetinolProScore = trtTarHighScoreNightlySerumRetinolProScore;
			}
			if (map.containsKey(trtTarHighScoreDailyOrNightlySerumRetinolProScore)) {
				map.get(trtTarHighScoreDailyOrNightlySerumRetinolProScore).add("Serums-Retinol");
			} else {
				lst = new ArrayList<String>();
				lst.add("Serums-Retinol");
				map.put(trtTarHighScoreDailyOrNightlySerumRetinolProScore, lst);
			}
			if (trtTarHighScoreDailySerumVitaminProScore >= trtTarHighScoreNightlySerumVitaminProScore) {
				trtTarHighScoreDailyOrNightlySerumVitaminProId = trtTarHighScoreDailySerumVitaminProId;
				trtTarHighScoreDailyOrNightlySerumVitaminProScore = trtTarHighScoreDailySerumVitaminProScore;
			} else if (trtTarHighScoreDailySerumVitaminProScore < trtTarHighScoreNightlySerumVitaminProScore) {
				trtTarHighScoreDailyOrNightlySerumVitaminProId = trtTarHighScoreNightlySerumVitaminProId;
				trtTarHighScoreDailyOrNightlySerumVitaminProScore = trtTarHighScoreNightlySerumVitaminProScore;
			}
			if (map.containsKey(trtTarHighScoreDailyOrNightlySerumVitaminProScore)) {
				map.get(trtTarHighScoreDailyOrNightlySerumVitaminProScore).add("Serums-VitaminC");
			} else {
				lst = new ArrayList<String>();
				lst.add("Serums-VitaminC");
				map.put(trtTarHighScoreDailyOrNightlySerumVitaminProScore, lst);
			}
			if (trtDailyHighScoreExfoProScore >= trtTarHighScoreTargetedExfoliProScore) {
				trtTarHighScoreDailyOrNightlyExfoProId = trtDailyHighScoreExfoProId;
				trtTarHighScoreDailyOrNightlyExfoProScore = trtDailyHighScoreExfoProScore;
			} else if (trtDailyHighScoreExfoProScore < trtTarHighScoreTargetedExfoliProScore) {
				trtTarHighScoreDailyOrNightlyExfoProId = trtTarHighScoreTargetedExfoliProId;
				trtTarHighScoreDailyOrNightlyExfoProScore = trtTarHighScoreTargetedExfoliProScore;
			}
			if (map.containsKey(trtTarHighScoreDailyOrNightlyExfoProScore)) {
				map.get(trtTarHighScoreDailyOrNightlyExfoProScore).add("Exfoliators");
			} else {
				lst = new ArrayList<String>();
				lst.add("Exfoliators");
				map.put(trtTarHighScoreDailyOrNightlyExfoProScore, lst);
			}
			System.out.println(map);
			setFinalmap(map);
			System.out.println("categoryMapTreatmentAll==>" + categoryMapTreatmentAll);
			return categoryMapTreatmentAll;
		}
	// To get Highest product without Base Oil -- End

	public ArrayList getBeautyProductAfterDisplayRule(ArrayList product, String prodtype, CustomerProfileVO cp,
			String prodline, String[] skinSelection) {
		
		boolean trtDaily = false;
		boolean trtDailyConcernTwo = false;
		boolean trtDailyOil = false;
        boolean trtDailyHighScoreSerum = false;
        boolean trtDailyHighScoreExfo = false;
        boolean trtTarOne = false;
        boolean trtTarTwo = false;
        boolean trtHighScoreWeeklyExfo = false;
        boolean trtHighScoreWeeklyMask = false;
        boolean trtHighScoreWeeklyPeel = false;
        boolean trtWeeklyExfoOrMaskOrPeel = false;
        boolean atHomeFacialWeekly = false;//Display rule changes
		boolean trtDailyRednessSelect = false;
		boolean trtDailyPersistAcneSelect = false;
		boolean trtDailyAgeSpotSelect = false;
		boolean trtDailyHyperpigSelect = false;
		boolean trtOccAcneSelect = false;
		boolean trtTarOnePersistAcneSelect = false;
		boolean trtTarOneRednessSelect = false;
		boolean trtTarOneAgeSpotSelect = false;
		boolean trtTarOneHyperpigSelect = false;
		boolean trtTarOneOccAcneSelect = false;
		boolean trtTarOneDecNeckSelect = false;
		boolean trtTarTwoPersistAcneSelect = false;
		boolean trtTarTwoRednessSelect = false;
		boolean trtTarTwoAgeSpotSelect = false;
		boolean trtTarTwoHyperpigSelect = false;
		boolean trtTarTwoDecNeckSelect = false;
		boolean trtTarTwoOccAcneSelect = false;
		boolean cleansePreClflag = false;
		// If PRECLEANSE has base='Oil' then don't recommend Daily cleanser with oil base -- Start
		boolean cleansePreOilClflag = false;
		boolean cleanseTonerflag = false;
		boolean cleanseFWipesflag = false;
		boolean moistMOWithoutSPFflag = false;
		boolean moistMOWithSPFflag = false;
		boolean moistNightCreamflag = false;
		boolean eyeDailyETreatflag = false;
		boolean eyeEmaskflag = false;
		boolean sunCareflag = false;
        boolean clMultiFunClflag  = false ;
        boolean trtTarAcneSpot = false;
        boolean trtTarHighScoreTargetedBooster = false;
        boolean trtTarHighScoreNightlySerum = false;
        boolean trtTarHighScoreTargetedExfoli = false;
        //Display rule changes - Start
        boolean trtWeeklyMask = false;
        boolean trtWeeklyPeel = false;
        boolean trtWeeklyExfoliator = false;

        boolean trtTarHighScoreDailySerumHydrator  = false;
        boolean trtTarHighScoreDailySerumRetinol  = false;
        boolean trtTarHighScoreDailySerumVitamin  = false;

        boolean trtTarHighScoreNightlySerumHydrator  = false;
        boolean trtTarHighScoreNightlySerumRetinol  = false;
        boolean trtTarHighScoreNightlySerumVitamin  = false;
        
        boolean treatment_one_two_have_expoliatore  = false;
        //Display rule changes - End
        
		float trtDailyHighScoreSerumProScore = 0;
        float trtDailyHighScoreExfoProScore = 0;
        float trtHighScoreWeeklyExfoProScore = 0;
        float trtHighScoreWeeklyMaskProScore = 0;
        float trtHighScoreWeeklyPeelProScore = 0;
        float trtTarHighScoreTargetedBoosterProScore = 0;
        float trtTarHighScoreNightlySerumProScore = 0;
        float trtTarHighScoreTargetedExfoliProScore = 0;
        //Display rule changes - Start
        float trtTarHighScoreDailySerumHydratorProScore = 0;
        float trtTarHighScoreDailySerumRetinolProScore = 0;       
        float trtTarHighScoreDailySerumVitaminProScore = 0;

        float trtTarHighScoreNightlySerumHydratorProScore = 0;
        float trtTarHighScoreNightlySerumRetinolProScore = 0;       
        float trtTarHighScoreNightlySerumVitaminProScore = 0;
        
        float trtTarHighScoreDailyOrNightlySerumProScore = 0;
        float trtTarHighScoreDailyOrNightlySerumHydratorProScore = 0;
        float trtTarHighScoreDailyOrNightlySerumRetinolProScore = 0;       
        float trtTarHighScoreDailyOrNightlySerumVitaminProScore = 0;
        float trtTarHighScoreDailyOrNightlyExfoProScore = 0;
        //Display rule changes - End
       
       
        int trtDailyHighScoreSerumProId = 0;
        int trtDailyHighScoreExfoProId = 0;
        int trtHighScoreWeeklyExfoProId = -1;
        int trtHighScoreWeeklyMaskProId = -1;
        int trtHighScoreWeeklyPeelProId = -1;
        int trtTarHighScoreTargetedBoosterProId = 0;
        int trtTarHighScoreNightlySerumProId = 0;
        int trtTarHighScoreTargetedExfoliProId = 0;
        //Display rule changes - Start
        
        int trtTarHighScoreDailyOrNightlySerumHydratorProId = 0;
        int trtTarHighScoreDailyOrNightlySerumRetinolProId = 0;
        int trtTarHighScoreDailyOrNightlySerumVitaminProId = 0;
        int trtTarHighScoreDailyOrNightlyExfoProId = 0;
        
        int trtTarHighScoreDailySerumHydratorProId = 0;
        int trtTarHighScoreDailySerumRetinolProId = 0;
        int trtTarHighScoreDailySerumVitaminProId = 0;
        
        int trtTarHighScoreNightlySerumHydratorProId = 0;
        int trtTarHighScoreNightlySerumRetinolProId = 0;
        int trtTarHighScoreNightlySerumVitaminProId = 0;
      //Display rule changes - End
        String cSkinType = cp.getSkinType();
        ArrayList cSkinConcernList = cp.getSkinConcernList();

		ArrayList custSpecificationList = cp.getSpecification();

        List<ProductVO> clenserList = new ArrayList<>();
        List<ProductVO> moisturizerList = new ArrayList<>();
        List<ProductVO> treatmentList = new ArrayList<>();
        List<ProductVO> eyeConcernList = new ArrayList<>();
        List<ProductVO> sunCareList = new ArrayList<>();
        ArrayList recproducts = new ArrayList();
        
        List<String>  skinConcernSelection = Arrays.asList(skinSelection);
        
        //Added to display top 3 products in each category - Shivam - 14/02/2024 - Start
        //For Cleansers Sub-Categories
        int preCleanseCount = 1;
        int faceWashCount = 1;
        int tonersAndMistsCount = 1;
        int facialWipesCount = 1;
        
        //For Cleansers Sub-Categories
        int moisturizerWithoutSPFCount = 1;
        int moisturizerWithSPFCount = 1;
        int nightCreamCount = 1;
        
        //For Eye Sub-Categories
        int eyeTreatSerumCount = 1;
        int eyeMaskCount = 1;
        
        //For Treatment Sub-Categories
        int dailyTreatmentCount = 1;
        int dailyHighScoreSerumCount = 1;
        int dailyHighScoreExfoliatorCount = 1;
        int highScoreDailySerumRetinolCount = 1;
        int highScoreDailySerumVitaminCCount = 1;
        int highScoreDailySerumHydratorCount = 1;
        int targetedOneTreatmentCount = 1;
        int targetedTwoTreatmentCount = 1;
        int weeklyExfoliatorsCount = 1;
        int weeklyMasksCount = 1;
        int weeklyPeelsCount = 1;
        int[] dailyHighScoreSerum = new int[3];
        int[] dailyHighScoreExfoliator = new int[3];
        float[] dailyHighScoreSerumProductScore = new float[3];
        float[] dailyHighScoreExfoliatorProductScore = new float[3];
		int[] highScoreDailySerumHydrator = new int[3];
		int[] highScoreWeeklyExfoliatorProductIndices = new int[3];
		int[] highScoreWeeklyMaskProductIndices = new int[3];
		int[] highScoreWeeklyPeelProductIndices = new int[3];
        
        //For SunCare Sub-Categories
        int facialSunScreenCount = 1;
        
        //Added to display top 3 products in each category - Shivam - 14/02/2024 - End
        
        	// check for 1st Skin Concern selection
     		if (!skinConcernSelection.isEmpty()) {
     			if (skinConcernSelection.get(0).equals(ConstantDeclarations.SKIN_CONCERN_SELECTION_AGESPOT)) // Age spots
     				trtDailyAgeSpotSelect = true;
     			if (skinConcernSelection.get(0).equals(ConstantDeclarations.SKIN_CONCERN_SELECTION_REDNESS)) // Redness/Blotchiness
     				trtDailyRednessSelect = true;
     			if (skinConcernSelection.get(0).equals(ConstantDeclarations.SKIN_CONCERN_SELECTION_PERSACNE)) // Persistent acne
    				trtDailyPersistAcneSelect = true;
    			if (skinConcernSelection.get(0).equals(ConstantDeclarations.SKIN_CONCERN_SELECTION_HYPIG)) // Hyperpigmentation
    				trtDailyHyperpigSelect = true;
    			if (skinConcernSelection.get(0).equals(ConstantDeclarations.SKIN_CONCERN_SELECTION_OCCACNE)) // Occasional acne
    				trtOccAcneSelect = true;
     		}
     		// check for 2nd Skin Concern selection
     		if (skinConcernSelection.size() > 1) {
     			if (skinConcernSelection.get(1).equals(ConstantDeclarations.SKIN_CONCERN_SELECTION_AGESPOT)) // Age spots
     				trtTarOneAgeSpotSelect = true;
     			if (skinConcernSelection.get(1).equals(ConstantDeclarations.SKIN_CONCERN_SELECTION_DECONECK)) // Decollete and Neck
     				trtTarOneDecNeckSelect = true;
     			if (skinConcernSelection.get(1).equals(ConstantDeclarations.SKIN_CONCERN_SELECTION_PERSACNE)) // Persistent acne
    				trtTarOnePersistAcneSelect = true;
    			if (skinConcernSelection.get(1).equals(ConstantDeclarations.SKIN_CONCERN_SELECTION_HYPIG)) // Hyperpigmentation
    				trtTarOneHyperpigSelect = true;
    			if (skinConcernSelection.get(1).equals(ConstantDeclarations.SKIN_CONCERN_SELECTION_OCCACNE)) // Occasional acne
    				trtTarOneOccAcneSelect = true;
    			if (skinConcernSelection.get(1).equals(ConstantDeclarations.SKIN_CONCERN_SELECTION_REDNESS)) 
    				trtTarOneRednessSelect=true;
     		}
     		// check for 3rd Skin Concern selection
     		if (skinConcernSelection.size() > 2) {
     			if (skinConcernSelection.get(2).equals(ConstantDeclarations.SKIN_CONCERN_SELECTION_AGESPOT)) // Age spots
     				trtTarTwoAgeSpotSelect = true;
     			if (skinConcernSelection.get(2).equals(ConstantDeclarations.SKIN_CONCERN_SELECTION_DECONECK)) // Decollete and Neck
     				trtTarTwoDecNeckSelect = true;
     			if (skinConcernSelection.get(2).equals(ConstantDeclarations.SKIN_CONCERN_SELECTION_PERSACNE))// Persistent acne
    				trtTarTwoPersistAcneSelect = true;
    			if (skinConcernSelection.get(2).equals(ConstantDeclarations.SKIN_CONCERN_SELECTION_HYPIG)) // Hyperpigmentation
    				trtTarTwoHyperpigSelect = true;
    			if (skinConcernSelection.get(2).equals(ConstantDeclarations.SKIN_CONCERN_SELECTION_OCCACNE)) // Occasional acne
    				trtTarTwoOccAcneSelect = true;
    			if (skinConcernSelection.get(2).equals(ConstantDeclarations.SKIN_CONCERN_SELECTION_REDNESS)) 
    				trtTarTwoRednessSelect=true;
     		}
		// bifurcate products as per category
		for (int i = product.size() - 1; i >= 0; i--) {
			if (((ProductVO) product.get(i)).getMasterProdType().equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_CLEANSE)) {
				clenserList.add((ProductVO) product.get(i));
			} else if (((ProductVO) product.get(i)).getMasterProdType().equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_MOIST)) {
				moisturizerList.add((ProductVO) product.get(i));
			} else if (((ProductVO) product.get(i)).getMasterProdType().equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_TREAT)) {
				treatmentList.add((ProductVO) product.get(i));
			} else if (((ProductVO) product.get(i)).getMasterProdType().equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_ETREAT)) {
				eyeConcernList.add((ProductVO) product.get(i));
			} else if (((ProductVO) product.get(i)).getMasterProdType().equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_UV)) {
				sunCareList.add((ProductVO) product.get(i));
			}
		}
		System.out.println("CLENSER LIST " + clenserList.size());
		List <ProductVO>cleanserRule = new ArrayList();
		List cleanserProducts = new ArrayList();
		List <ProductVO> cleanserFacialWash = new ArrayList();
		
		System.out.println("Display rule debug 3");
		// Daily Cleanser
		if (clenserList.size() > 1) {
			for (int i = 0; i < clenserList.size(); i++) {
		
				/** #1 highest scoring Pre-cleanser where sub-category �Pre-cleanse� with when to
				* use attribute = �Daily Cleanser�
				*/
				 if (preCleanseCount<=3 //Allows Top 3 products in this category
						&& clenserList.get(i).getProdType().equals(ConstantDeclarations.PRECLEANSE)
						&& clenserList.get(i).getWhenToUse()!= null
						&& clenserList.get(i).getWhenToUse().equals(ConstantDeclarations.WHEN_TO_USE_DLYCLE)
						&& Float.valueOf(clenserList.get(i).getScore()) >= ConstantDeclarations.CLEANER_SCORE_80) {	//Modified Score condition as per new Display Rules - Shivam - 23/10/2023 					
					cleanserRule.add(clenserList.get(i));
					preCleanseCount++;
					// If PRECLEANSE has base='Oil' then don't recommend Daily cleanser with oil base -- Start
					if(clenserList.get(i).getBase().equals(ConstantDeclarations.OILBASE)) {
						cleansePreOilClflag =true;
					}
					// If PRECLEANSE has base='Oil' then don't recommend Daily cleanser with oil base -- End
					
				}
				/** #1 highest scoring Cleanser where sub-category �Facial Wash & Cleansers�,
				*	with when to use attribute = �Daily Cleanser�
				*/
				 else if (clenserList.get(i).getWhenToUse() != null
						&& clenserList.get(i).getWhenToUse().equals(ConstantDeclarations.WHEN_TO_USE_DLYCLE)
						&& clenserList.get(i).getProdType().equals(ConstantDeclarations.FWASHCLEANSE)
						&& Float.valueOf(clenserList.get(i).getScore()) >= ConstantDeclarations.CLEANER_SCORE_80) {//Added Score condition as per new Display Rules - Shivam - 23/10/2023
					// If PRECLEANSE has base='Oil' then don't recommend Daily cleanser with oil base -- Start
					 if(cleansePreOilClflag) {
						 if(!clenserList.get(i).getBase().equals(ConstantDeclarations.OILBASE) ) {
							 cleanserFacialWash.add(clenserList.get(i));
						 }
					 }else {
						 cleanserFacialWash.add(clenserList.get(i));
					 }
					// If PRECLEANSE has base='Oil' then don't recommend Daily cleanser with oil base -- End
				}
				/** #1 highest scoring Toner where sub-category �Toners & Mists� with when to use
				* attribute = �Daily Cleanser�. 
				*/
				else if (tonersAndMistsCount <= 3 //Allows Top 3 products in this category
						&& clenserList.get(i).getProdType().equals(ConstantDeclarations.TONMIST)
						&& clenserList.get(i).getWhenToUse()!= null
						&& clenserList.get(i).getWhenToUse().equals(ConstantDeclarations.WHEN_TO_USE_DLYCLE)
						&& Float.valueOf(clenserList.get(i).getScore()) >= ConstantDeclarations.CLEANER_SCORE_80) {//Modified Score condition as per new Display Rules - Shivam - 23/10/2023
					// If PRECLEANSE has base='Oil' then don't recommend Daily cleanser with oil base -- Start
					if (cleansePreOilClflag) {
						if (!clenserList.get(i).getBase().equals(ConstantDeclarations.OILBASE)) {
							cleanserRule.add(clenserList.get(i));
							tonersAndMistsCount++;
						}
					} else {
						cleanserRule.add(clenserList.get(i));
						tonersAndMistsCount++;
					}
					// If PRECLEANSE has base='Oil' then don't recommend Daily cleanser with oil base -- End
				}
				/** highest scoring Facial Wipes where sub-category �Facial Wipes� and score
				* above 50 and If the customer has answered YES to �exercise� or YES to
				* �travel� %
				*/
				else if (facialWipesCount <= 3 //Allows Top 3 products in this category
						&& clenserList.get(i).getProdType().equals(ConstantDeclarations.FACIAL_WIPES)
						&& Float.valueOf(clenserList.get(i).getScore()) >= ConstantDeclarations.CLEANER_SCORE_80){//Modified Score condition as per new Display Rules - Shivam - 23/10/2023
						//&& (custSpecificationList.contains(ConstantDeclarations.REGULAR_ACTIVITIES_EXCERSISEOFTEN) || custSpecificationList.contains(ConstantDeclarations.REGULAR_ACTIVITIES_TRAVELBYAIR))) {
					//Commented the above condition because of new display rules changes to allow Facial wipes even if the customer has not selected exercise or travel.
					cleanserRule.add(clenserList.get(i));
					facialWipesCount++;
					}
				}
				/** Top 1 highest scoring Cleanser product with Sub-category = �Facial Wash &
				* Cleansers� && Daily Cleanser as when to use attribute
				*/
			if (cleanserFacialWash.size() > 1) {
				for(int cleanserFacialWashIterator = 0; cleanserFacialWashIterator < cleanserFacialWash.size() && faceWashCount <= 3; cleanserFacialWashIterator++, faceWashCount++) {
					cleanserRule.add(cleanserFacialWash.get(cleanserFacialWashIterator));
					
					// Top 1 highest Scoring Multi-function Cleanser as Multibenefit attribute
					if (!clMultiFunClflag && cleanserFacialWash.get(0).getMultiBenefits() != null && cleanserFacialWash
							.get(0).getMultiBenefits().equals(ConstantDeclarations.MULBENIFIT_MULTIFUNCLE)  )// && !custSpecificationList.contains(ConstantDeclarations.REGULAR_ACTIVITIES_NOMAKEUP) Display rule changes No need 
						clMultiFunClflag = true; // to indicate product received
					else {
						for (int i = 1; i < 3; i++) {
							if (!clMultiFunClflag) {
								if (cleanserFacialWash.get(i).getMultiBenefits() != null && cleanserFacialWash.get(i)
										.getMultiBenefits().equals(ConstantDeclarations.MULBENIFIT_MULTIFUNCLE) )//&&!custSpecificationList.contains(ConstantDeclarations.REGULAR_ACTIVITIES_NOMAKEUP) Display rule changes
									cleanserRule.add(cleanserFacialWash.get(i));
								clMultiFunClflag = true; // to indicate product received
							}
						}
					}
				}
			}
		}
		cleanserProducts.add(cleanserRule);
		System.out.println("Display rule debug 4");
		// Moisturizer
		List <ProductVO> moisturizerRule = new ArrayList();
		List moisturizerProducts = new ArrayList();
		boolean isSelectDrySkinorDryness = (cSkinType.contains(ConstantDeclarations.SKIN_TYPE_DRY) || cSkinConcernList.contains(ConstantDeclarations.SKIN_CONCERN_SELECTION_FLAKINESS_DRYNESS));//Added by synoptek 01-18-2021 we show a night moisturizer, 1) Dry Skin or 2) Dryness/Dehydration).
		if (moisturizerList.size()>1) {
			for(int i = 0; i<moisturizerList.size();i++) {
				/** #1 highest scoring Moisturizer Without SPF where sub-category �Moisturizer�,
				* with when to use attribute = �Daily Moisturizer�, with multi-benefit
				* attribute �Does Not Have SPF�
				*/
				if (moisturizerWithoutSPFCount <= 3
						&& moisturizerList.get(i).getProdType().equals(ConstantDeclarations.MOISTURIZERS)
						&& moisturizerList.get(i).getWhenToUse()!= null
						&& moisturizerList.get(i).getWhenToUse().equals(ConstantDeclarations.WHEN_TO_USE_DLYMOI)
						&& moisturizerList.get(i).getMultiBenefits() != null
						&& moisturizerList.get(i).getMultiBenefits().equals(ConstantDeclarations.MULBENIFIT_NOSPF)
						&& Float.valueOf(moisturizerList.get(i).getScore()) >= ConstantDeclarations.MOISTURIZER_SCORE_80) { //Added Score condition as per new Display Rules - Shivam - 23/10/2023
					moisturizerRule.add(moisturizerList.get(i));
					moisturizerWithoutSPFCount ++;
				}
				/** #1 highest scoring Moisturizer With SPF where sub-category �Moisturizer�,
				* with when to use attribute = �Daily Moisturizer with SPF�, with multi-benefit
				* attribute �Has SPF�
				*/
				else if (moisturizerWithSPFCount <= 3
						&& moisturizerList.get(i).getProdType().equals(ConstantDeclarations.MOISTURIZERS)
						&& moisturizerList.get(i).getWhenToUse()!= null
						&& moisturizerList.get(i).getWhenToUse().equals(ConstantDeclarations.WHEN_TO_USE_DLYMOISPF)
						&& moisturizerList.get(i).getMultiBenefits().equals(ConstantDeclarations.MULBENIFIT_HASSPF)
						&& Float.valueOf(moisturizerList.get(i).getScore()) > ConstantDeclarations.MOISTURIZER_SCORE_80) {
					moisturizerRule.add(moisturizerList.get(i));
					moisturizerWithSPFCount++;
				} 
				/** #1 highest scoring Nightly Moisturizer where sub-category �Night Cream�; with
				* when to use attribute = �Nightly Moisturizer� and If the score is above 50
				*/
				else if (nightCreamCount <= 3
						&& moisturizerList.get(i).getProdType().equals(ConstantDeclarations.NIGHTCREAMS)
						&& moisturizerList.get(i).getWhenToUse()!= null
						&& moisturizerList.get(i).getWhenToUse().equals(ConstantDeclarations.WHEN_TO_USE_NGHTLYMOI)
						&& Float.valueOf(moisturizerList.get(i).getScore()) >= ConstantDeclarations.MOISTURIZER_SCORE_80 //Modified Score condition as per new Display Rules - Shivam - 23/10/2023
						&& isSelectDrySkinorDryness) {//Added by synoptek 01-18-2021 we show a night moisturizer, 1) Dry Skin or 2) Dryness/Dehydration).
					moisturizerRule.add(moisturizerList.get(i));
					nightCreamCount++;
				} 
			}
			
		}	
		moisturizerProducts.add(moisturizerRule);
										// Eye Treatment
		List <ProductVO> eyeConcernRule = new ArrayList();
		List eyeConcernProducts = new ArrayList();
		if (eyeConcernList.size()>1) {
			for(int i = 0; i<eyeConcernList.size();i++) {
				/** #1 highest scoring Daily Eye Treatment where sub-category �Eye Treatments &
				* Serums� with when to use attribute = �Daily Eye Treatment�
				*/
				if (eyeTreatSerumCount <= 3
						&& eyeConcernList.get(i).getProdType().equals(ConstantDeclarations.EYETREATSERUM)
						&& eyeConcernList.get(i).getWhenToUse()!= null
						&& eyeConcernList.get(i).getWhenToUse().equals(ConstantDeclarations.WHEN_TO_USE_DLYEYETREAT)
						&& Float.valueOf(eyeConcernList.get(i).getScore()) >= ConstantDeclarations.EYE_SCORE_80) { //Added Score condition as per new Display Rules - Shivam - 23/10/2023
					eyeConcernRule.add(eyeConcernList.get(i));
					eyeTreatSerumCount++;
				} 
				/** #1 highest scoring Eye Mask where sub-category �Eye Masks� with when to use
				* attribute = �Weekly Eye Treatment�.
				*/
				else if (eyeMaskCount <= 3
						&& eyeConcernList.get(i).getProdType().equals(ConstantDeclarations.EYEMASK)
						&& eyeConcernList.get(i).getWhenToUse()!= null
						&& eyeConcernList.get(i).getWhenToUse().equals(ConstantDeclarations.WHEN_TO_USE_WKLYEYETREAT)
						&& Float.valueOf(eyeConcernList.get(i).getScore()) >= ConstantDeclarations.EYE_SCORE_80) { //Added Score condition as per new Display Rules - Shivam - 23/10/2023
					eyeConcernRule.add(eyeConcernList.get(i));
					eyeMaskCount++;
				} 		
			}
		}
		eyeConcernProducts.add(eyeConcernRule);
		
							// Treatment
		List <ProductVO> treatmentRule = new ArrayList();
		List treatmentProducts = new ArrayList();
		List <ProductVO> treatDailyTreat = new ArrayList();
		List <ProductVO> treatDailyTreatForOil = new ArrayList();
		List <ProductVO> treatTargetedTreat = new ArrayList();
		List <ProductVO> treatWeeklyTreat = new ArrayList();
		List <ProductVO> treatNightlyTreat = new ArrayList();
		List <ProductVO> treatNightlyTreatForOil = new ArrayList();
		List <ProductVO> treatTargetedExfoli = new ArrayList();
		//Display rule changes - Start
		//Display rule changes - End
		
		
		Hashtable<String, Object>  categoryMapTreatment= new Hashtable<String, Object>();
		Hashtable<String, Object>  categoryMapTreatmentAll= new Hashtable<String, Object>();
		System.out.println("Display rule debug 5");

		if (treatmentList.size()>1) {
								// bifurcate Treatment products as per When to Use attribute
			for (int i = 0; i < treatmentList.size(); i++) {
				// Daily Treatment products
				if (treatmentList.get(i).getWhenToUse() != null && treatmentList.get(i)
						.getWhenToUse().equals(ConstantDeclarations.WHEN_TO_USE_DLYTREAT)
						&& Float.valueOf(treatmentList.get(i).getScore()) >= ConstantDeclarations.TREATMENT_SCORE_80) {//Added Score condition as per new Display Rules - Shivam - 23/10/2023
					treatDailyTreat.add(treatmentList.get(i));
				}

				// Targeted Treatment products
				else if (treatmentList.get(i).getWhenToUse() != null && treatmentList.get(i)
						.getWhenToUse().equals(ConstantDeclarations.WHEN_TO_USE_TRGTREAT)
						&& Float.valueOf(treatmentList.get(i).getScore()) >= ConstantDeclarations.TREATMENT_SCORE_80) {//Added Score condition as per new Display Rules - Shivam - 23/10/2023
					treatTargetedTreat.add(treatmentList.get(i));
				}
				// Weekly Treatment products
				else if (treatmentList.get(i).getWhenToUse() != null && treatmentList.get(i)
						.getWhenToUse().equals(ConstantDeclarations.WHEN_TO_USE_WKLYTREAT)
						&& Float.valueOf(treatmentList.get(i).getScore()) >= ConstantDeclarations.TREATMENT_SCORE_80) {//Added Score condition as per new Display Rules - Shivam - 23/10/2023
					treatWeeklyTreat.add(treatmentList.get(i));
				}
				// Nightly Treatment products
				else if (treatmentList.get(i).getWhenToUse() != null && treatmentList.get(i)
						.getWhenToUse().equals(ConstantDeclarations.WHEN_TO_USE_NGHTLYTREAT)
						&& Float.valueOf(treatmentList.get(i).getScore()) >= ConstantDeclarations.TREATMENT_SCORE_80) {//Added Score condition as per new Display Rules - Shivam - 08/12/2023
					treatNightlyTreat.add(treatmentList.get(i));
				}

				// Targeted Exfoliator products
				else if (treatmentList.get(i).getWhenToUse() != null && treatmentList.get(i)
						.getWhenToUse().equals(ConstantDeclarations.WHEN_TO_USE_TRGEXFOLI)
						&& Float.valueOf(treatmentList.get(i).getScore()) >= ConstantDeclarations.TREATMENT_SCORE_80) {//Added Score condition as per new Display Rules - Shivam - 08/12/2023
					treatTargetedExfoli.add(treatmentList.get(i));
				}

				// Display rule changes - Start
				else if ((treatmentList.get(i).getWhenToUse() != null && ((treatmentList.get(i)
						.getWhenToUse().equals(ConstantDeclarations.WHEN_TO_USE_DLYTREAT))
						|| treatmentList.get(i).getWhenToUse()
								.equals(ConstantDeclarations.WHEN_TO_USE_NGHTLYTREAT)))
						&& Float.valueOf(treatmentList.get(i).getScore()) >= ConstantDeclarations.TREATMENT_SCORE_80) {
					treatmentList.add(treatmentList.get(i));
				}
			}
			// Getting without Oil Base Product -Start
			for (int i = 0; i < treatmentList.size(); i++) {
				if (treatmentList.get(i).getWhenToUse() != null
						&& treatmentList.get(i).getWhenToUse()
								.equals(ConstantDeclarations.WHEN_TO_USE_NGHTLYTREAT)
						&& !treatmentList.get(i).getBase().equals(ConstantDeclarations.OILBASE)
						&& Float.valueOf(treatmentList.get(i).getScore()) >= ConstantDeclarations.TREATMENT_SCORE_80) {//Added Score condition as per new Display Rules - Shivam - 08/12/2023
					treatNightlyTreatForOil.add(treatmentList.get(i));
				} else if (treatmentList.get(i).getWhenToUse() != null
						&& treatmentList.get(i).getWhenToUse()
								.equals(ConstantDeclarations.WHEN_TO_USE_DLYTREAT)
						&& !treatmentList.get(i).getBase().equals(ConstantDeclarations.OILBASE)
						&& Float.valueOf(treatmentList.get(i).getScore()) >= ConstantDeclarations.TREATMENT_SCORE_80) {//Added Score condition as per new Display Rules - Shivam - 08/12/2023
					treatDailyTreatForOil.add(treatmentList.get(i));
				}
			}
			// Getting without Oil Base Product -End
			int highScoreNightlySerumRetinolCount = 1;
			int highScoreNightlySerumVitaminCount = 1;
			if (!treatNightlyTreat.isEmpty()) {
				System.out.println("Size++" + treatNightlyTreat.size());
				for (int i = 0; i < treatNightlyTreat.size(); i++) {
					// no.1 Nightly treatment Serum product
					if (!trtTarHighScoreNightlySerum && treatNightlyTreat.get(i).getProdType()
							.equals(ConstantDeclarations.SERUM)) {
						trtTarHighScoreNightlySerumProId = i;
						trtTarHighScoreNightlySerumProScore = Float
								.valueOf(treatNightlyTreat.get(i).getScore());
						trtTarHighScoreNightlySerum = true; // to indicate product received
						categoryMapTreatment.put("Serum-Night", treatNightlyTreat.get(i));
					}
										
					//Display rule changes - Start
					// no.1 Exfoliator product where When to Use = �Daily Treatment� 
					else if (!trtTarHighScoreNightlySerumHydrator && treatNightlyTreat.get(i).getProdType().equals(ConstantDeclarations.SERUMSHYDRATOR)) {
						trtTarHighScoreNightlySerumHydratorProId = i;
						trtTarHighScoreNightlySerumHydratorProScore = Float.valueOf(treatNightlyTreat.get(i).getScore());
						trtTarHighScoreNightlySerumHydrator = true; // to indicate product received
						categoryMapTreatment.put("Serums-Hydrator-Night", treatNightlyTreat.get(i));
						
						//Added For exception22-03-2021-End
					}
					// no.1 Exfoliator product where When to Use = �Daily Treatment� 
					else if (highScoreNightlySerumRetinolCount <= 3 && treatNightlyTreat.get(i).getProdType().equals(ConstantDeclarations.SERUM_RETINOL)) {
						trtTarHighScoreNightlySerumRetinolProId = i;
						trtTarHighScoreNightlySerumRetinolProScore = Float.valueOf(treatNightlyTreat.get(i).getScore());
						trtTarHighScoreNightlySerumRetinol = true; // to indicate product received
						categoryMapTreatment.put("Serums-Retinol-Night"+highScoreNightlySerumRetinolCount, treatNightlyTreat.get(i));
						highScoreNightlySerumRetinolCount++;
						//Added For exception22-03-2021-End
					}
					// no.1 Exfoliator product where When to Use = �Daily Treatment� 
					else if (highScoreNightlySerumVitaminCount <= 3 && treatNightlyTreat.get(i).getProdType().equals(ConstantDeclarations.SERUMSVITAMINC)) {
						trtTarHighScoreNightlySerumVitaminProId = i;
						trtTarHighScoreNightlySerumVitaminProScore = Float.valueOf(treatNightlyTreat.get(i).getScore());
						trtTarHighScoreNightlySerumVitamin = true; // to indicate product received
						categoryMapTreatment.put("Serums-VitaminC-Night"+highScoreNightlySerumVitaminCount, treatNightlyTreat.get(i));
						highScoreNightlySerumVitaminCount++;
						//Display rule changes - End
					}
				}
			}
			if (!treatTargetedExfoli.isEmpty()) {
				for (int i = 0; i < treatTargetedExfoli.size(); i++) {
					// no.1 Targeted Exfoliator product
					if (!trtTarHighScoreTargetedExfoli && treatTargetedExfoli.get(i).getProdType().equals(ConstantDeclarations.EXFOLIATORS)) {
						trtTarHighScoreTargetedExfoliProId = i;
						trtTarHighScoreTargetedExfoliProScore = Float.valueOf(treatTargetedExfoli.get(i).getScore());
						trtTarHighScoreTargetedExfoli = true; // to indicate product received
						categoryMapTreatment.put("Exfoliators-Night", treatTargetedExfoli.get(i));
					}
				}
			}
			if (!treatTargetedTreat.isEmpty()) {
				for (int i = 0; i < treatTargetedTreat.size(); i++) {
					// # 1 Targeted treatment booster product 
					if (!trtTarHighScoreTargetedBooster && treatTargetedTreat.get(i).getProdType().equals(ConstantDeclarations.BOOSTERS)) {
						trtTarHighScoreTargetedBoosterProId = i;
						trtTarHighScoreTargetedBoosterProScore = Float.valueOf(treatTargetedTreat.get(i).getScore());
						trtTarHighScoreTargetedBooster = true; // to indicate product received
					}
				}
			}
			String dailyCatgory="";
			String treatmentCategory="";
			
			TreeMap<Float,ArrayList<String>> map = new TreeMap<Float,ArrayList<String>>(Collections.reverseOrder());
			if (!treatDailyTreat.isEmpty()) {
				System.out.println("Display rule debug 6");
				for (int i = 0; i < treatDailyTreat.size(); i++) {
					// no.1 Serum product where When to Use = �Daily Treatment� 
					if (dailyHighScoreSerumCount <= 3 && treatDailyTreat.get(i).getProdType().equals(ConstantDeclarations.SERUM)) {
						dailyHighScoreSerum[dailyHighScoreSerumCount-1] = i;
						dailyHighScoreSerumProductScore[dailyHighScoreSerumCount-1] = Float.valueOf(treatDailyTreat.get(i).getScore());
						dailyHighScoreSerumCount++;
						trtDailyHighScoreSerum = true;
						if(dailyCatgory.equals("")) {
						 dailyCatgory="Serum-Daily";
						}
						categoryMapTreatment.put("Serum-Daily", treatDailyTreat.get(i));
						
					}
					// no.1 Exfoliator product where When to Use = �Daily Treatment� 
					else if (dailyHighScoreExfoliatorCount <= 3 && treatDailyTreat.get(i).getProdType().equals(ConstantDeclarations.EXFOLIATORS)) {//Display rule changes
//						trtDailyHighScoreExfoProId = i;
//						trtDailyHighScoreExfoProScore = Float.valueOf(treatDailyTreat.get(i).getScore());
						dailyHighScoreExfoliator[dailyHighScoreExfoliatorCount-1] = i;
						dailyHighScoreExfoliatorProductScore[dailyHighScoreExfoliatorCount-1] = Float.valueOf(treatDailyTreat.get(i).getScore());
						dailyHighScoreExfoliatorCount++;
						trtDailyHighScoreExfo = true;
						if(dailyCatgory.equals("")) {
							 dailyCatgory="Exfoliators-Daily";
						}
						categoryMapTreatment.put("Exfoliators-Daily", treatDailyTreat.get(i));
						
					}
					//Display rule changes - Start
					// no.1 Exfoliator product where When to Use = �Daily Treatment� 
					else if (highScoreDailySerumHydratorCount <= 3 && treatDailyTreat.get(i).getProdType().equals(ConstantDeclarations.SERUMSHYDRATOR)) {
//						trtTarHighScoreDailySerumHydratorProId = i;
//						trtTarHighScoreDailySerumHydratorProScore = Float.valueOf(treatDailyTreat.get(i).getScore());
						highScoreDailySerumHydrator[highScoreDailySerumHydratorCount-1] = i;
						trtTarHighScoreDailySerumHydratorProScore = Float.valueOf(treatDailyTreat.get(i).getScore());
						trtTarHighScoreDailySerumHydrator = true; // to indicate product received
				
						categoryMapTreatment.put("Serums-Hydrator-Daily", treatDailyTreat.get(i));
						highScoreDailySerumHydratorCount++;
						//Added For exception22-03-2021-END
					}
					// no.1 Exfoliator product where When to Use = �Daily Treatment� 
					else if (highScoreDailySerumRetinolCount <= 3 && treatDailyTreat.get(i).getProdType().equals(ConstantDeclarations.SERUM_RETINOL)) {
						trtTarHighScoreDailySerumRetinolProId = i;
						trtTarHighScoreDailySerumRetinolProScore = Float.valueOf(treatDailyTreat.get(i).getScore());
						trtTarHighScoreDailySerumRetinol = true; // to indicate product received
						categoryMapTreatment.put("Serums-Retinol-Daily"+highScoreDailySerumRetinolCount, treatDailyTreat.get(i));
						highScoreDailySerumRetinolCount++;
						//Added For exception22-03-2021-END
					}
					// no.1 Exfoliator product where When to Use = �Daily Treatment� 
					else if (highScoreDailySerumVitaminCCount <= 3 && treatDailyTreat.get(i).getProdType().equals(ConstantDeclarations.SERUMSVITAMINC)) {
						trtTarHighScoreDailySerumVitaminProId = i;
						trtTarHighScoreDailySerumVitaminProScore = Float.valueOf(treatDailyTreat.get(i).getScore());
						trtTarHighScoreDailySerumVitamin = true; // to indicate product received
						categoryMapTreatment.put("Serums-VitaminC-Daily"+highScoreDailySerumVitaminCCount, treatDailyTreat.get(i));
						highScoreDailySerumVitaminCCount++;
						//Added For exception22-03-2021-END
					}
					//Display rule changes - End
				}
			}
			
			//Display rule changes - Start
			if (trtDailyHighScoreSerumProScore >= trtTarHighScoreNightlySerumProScore) {
				trtTarHighScoreDailyOrNightlySerumProScore = trtTarHighScoreDailySerumHydratorProScore;
			} else if (trtDailyHighScoreSerumProScore < trtTarHighScoreNightlySerumProScore) {				
				trtTarHighScoreDailyOrNightlySerumProScore = trtTarHighScoreNightlySerumHydratorProScore;
			} 
			ArrayList<String> lst = new ArrayList<String>();
			lst.add("Serum");
			map.put(trtTarHighScoreDailyOrNightlySerumProScore, lst);
			if (trtTarHighScoreDailySerumHydratorProScore >= trtTarHighScoreNightlySerumHydratorProScore) {
				trtTarHighScoreDailyOrNightlySerumHydratorProId = trtTarHighScoreDailySerumHydratorProId;
				trtTarHighScoreDailyOrNightlySerumHydratorProScore = trtTarHighScoreDailySerumHydratorProScore;
			} else if (trtTarHighScoreDailySerumHydratorProScore < trtTarHighScoreNightlySerumHydratorProScore) {
				trtTarHighScoreDailyOrNightlySerumHydratorProId = trtTarHighScoreNightlySerumHydratorProId;
				trtTarHighScoreDailyOrNightlySerumHydratorProScore = trtTarHighScoreNightlySerumHydratorProScore;
			}
			if(map.containsKey(trtTarHighScoreDailyOrNightlySerumHydratorProScore)) {
				map.get(trtTarHighScoreDailyOrNightlySerumHydratorProScore).add("Serums-Hydrator");
			}else {
				lst = new ArrayList<String>();
				lst.add("Serums-Hydrator");
				map.put(trtTarHighScoreDailyOrNightlySerumHydratorProScore, lst);
			}
			if (trtTarHighScoreDailySerumRetinolProScore >= trtTarHighScoreNightlySerumRetinolProScore) {
				trtTarHighScoreDailyOrNightlySerumRetinolProId = trtTarHighScoreDailySerumRetinolProId;
				trtTarHighScoreDailyOrNightlySerumRetinolProScore = trtTarHighScoreDailySerumRetinolProScore;
			} else if (trtTarHighScoreDailySerumRetinolProScore < trtTarHighScoreNightlySerumRetinolProScore) {
				trtTarHighScoreDailyOrNightlySerumRetinolProId = trtTarHighScoreNightlySerumRetinolProId;
				trtTarHighScoreDailyOrNightlySerumRetinolProScore = trtTarHighScoreNightlySerumRetinolProScore;
			}
			if(map.containsKey(trtTarHighScoreDailyOrNightlySerumRetinolProScore)) {
				map.get(trtTarHighScoreDailyOrNightlySerumRetinolProScore).add("Serums-Retinol");
			}else {
				lst = new ArrayList<String>();
				lst.add("Serums-Retinol");
				map.put(trtTarHighScoreDailyOrNightlySerumRetinolProScore, lst);
			}
			if (trtTarHighScoreDailySerumVitaminProScore >= trtTarHighScoreNightlySerumVitaminProScore) {
				trtTarHighScoreDailyOrNightlySerumVitaminProId = trtTarHighScoreDailySerumVitaminProId;
				trtTarHighScoreDailyOrNightlySerumVitaminProScore = trtTarHighScoreDailySerumVitaminProScore;
			} else if (trtTarHighScoreDailySerumVitaminProScore < trtTarHighScoreNightlySerumVitaminProScore) {
				trtTarHighScoreDailyOrNightlySerumVitaminProId = trtTarHighScoreNightlySerumVitaminProId;
				trtTarHighScoreDailyOrNightlySerumVitaminProScore = trtTarHighScoreNightlySerumVitaminProScore;
			}
			if(map.containsKey(trtTarHighScoreDailyOrNightlySerumVitaminProScore)) {
				map.get(trtTarHighScoreDailyOrNightlySerumVitaminProScore).add("Serums-VitaminC");
			}else {
				lst = new ArrayList<String>();
				lst.add("Serums-VitaminC");
				map.put(trtTarHighScoreDailyOrNightlySerumVitaminProScore, lst);
			}
			if (trtDailyHighScoreExfoProScore >= trtTarHighScoreTargetedExfoliProScore) {
				trtTarHighScoreDailyOrNightlyExfoProId = trtDailyHighScoreExfoProId;
				trtTarHighScoreDailyOrNightlyExfoProScore = trtDailyHighScoreExfoProScore;
			} else if (trtDailyHighScoreExfoProScore < trtTarHighScoreTargetedExfoliProScore) {
				trtTarHighScoreDailyOrNightlyExfoProId = trtTarHighScoreTargetedExfoliProId;
				trtTarHighScoreDailyOrNightlyExfoProScore = trtTarHighScoreTargetedExfoliProScore;
			}
			if(map.containsKey(trtTarHighScoreDailyOrNightlyExfoProScore)) {
				map.get(trtTarHighScoreDailyOrNightlyExfoProScore).add("Exfoliators");
			}else {
				lst = new ArrayList<String>();
				lst.add("Exfoliators");
				map.put(trtTarHighScoreDailyOrNightlyExfoProScore, lst);
			}
			ArrayList<String> finalCategory= new ArrayList<String>();
			if(!map.isEmpty()) {
				Set<Float> keys= map.keySet();
				for (Float floatKey : keys) {
					 ArrayList lstObject = map.get(floatKey);
					 finalCategory.addAll(lstObject);
				}
			}
			//Display rule changes - End
			
											// 	Daily Treatment
			if (treatDailyTreat.size() > 1) {
//				System.out.println("Display rule debug 7");
				for (int i = 0; i < treatDailyTreat.size(); i++) {
					// Sub-category = �Redness� && customer has selected �Redness/Blotchiness� as  their no. 1 concern
//					if (!trtDailyConcernTwo && treatDailyTreat.get(i).getProdType().equals(ConstantDeclarations.AGESPOTSSUNDAMAGE) && (trtTarOneAgeSpotSelect || trtTarOneHyperpigSelect)) {
////						treatDailyTreat.get(i).setComments(ConstantDeclarations.DAILY);
////						treatmentRule.add(treatDailyTreat.get(i));
////					
////						trtDaily = true; // to indicate product received
//						System.out.println("Your second concern is Agesports or hyperpigmentation");
//					}
					// Sub-category = �Acne� && customer has selected �Persistent Acne� as their no.1 concern
					// Sub-category = �Age Spots/Sun Damage; Hyperpigmentation� && customer has selected �Age Spots/Sun Damage� or �Hyperpigmentation� as their no. 1 concern
					if (dailyTreatmentCount <= 3
							&& ((treatDailyTreat.get(i).getProdType().equals(ConstantDeclarations.REDNESS)
									&& trtDailyRednessSelect)
									|| (treatDailyTreat.get(i).getProdType().equals(ConstantDeclarations.ACNE)
											&& trtDailyPersistAcneSelect)
									|| (treatDailyTreat.get(i).getProdType().equals(ConstantDeclarations.AGESPOTSSUNDAMAGE)
											&& (trtDailyAgeSpotSelect || trtDailyHyperpigSelect)))) {
						treatDailyTreat.get(i).setComments(ConstantDeclarations.DAILY);
						treatmentRule.add(treatDailyTreat.get(i));
						dailyTreatmentCount++;
					}
					/*
					 * Combined conditions of the below code in the above if condition 
					 * & commented below code to avoid code repetition - Shivam - 16/02/2024
					// Sub-category = �Acne� && customer has selected �Persistent Acne� as their no.1 concern
					else if (!trtDaily 
							&& treatDailyTreat.get(i).getProdType().equals(ConstantDeclarations.ACNE) 
							&& trtDailyPersistAcneSelect) {
						treatDailyTreat.get(i).setComments(ConstantDeclarations.DAILY);
						treatmentRule.add(treatDailyTreat.get(i));
						trtDaily = true; // to indicate product received
					}
					// Sub-category = �Age Spots/Sun Damage; Hyperpigmentation� && customer has selected �Age Spots/Sun Damage� or �Hyperpigmentation� as their no. 1 concern
					else if (!trtDaily
							&& treatDailyTreat.get(i).getProdType().equals(ConstantDeclarations.AGESPOTSSUNDAMAGE)
							&& (trtDailyAgeSpotSelect || trtDailyHyperpigSelect)) {
						treatDailyTreat.get(i).setComments(ConstantDeclarations.DAILY);
						treatmentRule.add(treatDailyTreat.get(i));
						trtDaily = true; // to indicate product received
					}*/
					
				}
			}

			/** If the customer did NOT select �Acne (Persistent)� and did not select �Age
			* Spots/Sun Damage� or �Hyperpigmentation� as their #1 concern
			*/
			dailyTreatmentCount = 1;
			int highScoreSerumAdded = 1;
			int highScoreExfoliatorAdded = 1;
			if (treatDailyTreat.size() > 1) {
				try {
					for (int i = 0; i < treatDailyTreat.size(); i++) {
						if (!(trtDailyAgeSpotSelect || trtDailyHyperpigSelect) && !trtDailyRednessSelect && !trtDailyPersistAcneSelect) {
							
							if (!treatDailyTreat.isEmpty()
									&& Objects.nonNull(dailyHighScoreSerum)
									&& Objects.nonNull(dailyHighScoreExfoliator)
									&& highScoreSerumAdded < 4
									&& highScoreExfoliatorAdded < 4
									&& Objects.nonNull(dailyHighScoreSerum[highScoreSerumAdded-1])
									&& Objects.nonNull(dailyHighScoreExfoliator[highScoreExfoliatorAdded-1])
									&& Objects.nonNull(treatDailyTreat.get(dailyHighScoreSerum[highScoreSerumAdded-1]))
									&& Objects.nonNull(treatDailyTreat.get(dailyHighScoreExfoliator[highScoreExfoliatorAdded-1]))) {
								/** compare highest scored product from Sub Category = �Serums� or
								 * �Exfoliators�,and when to use = �Daily Treatment�
								 */
								if (trtDailyHighScoreSerumProScore > trtDailyHighScoreExfoProScore
										&& highScoreSerumAdded < dailyHighScoreSerumCount) {
									treatDailyTreat.get(dailyHighScoreSerum[highScoreSerumAdded-1]).setComments(ConstantDeclarations.DAILY);
									treatmentRule.add(treatDailyTreat.get(dailyHighScoreSerum[highScoreSerumAdded-1]));
									trtDailyHighScoreSerum = false;//Display rule changes
									dailyCatgory="Serum-Daily";
									categoryMapTreatment.put("Serum-Daily", treatDailyTreat.get(dailyHighScoreSerum[highScoreSerumAdded-1]));
									highScoreSerumAdded++;
//							} else if (trtDailyHighScoreSerumProScore < trtDailyHighScoreExfoProScore) {
								} else if (trtDailyHighScoreSerumProScore < trtDailyHighScoreExfoProScore
										&& highScoreExfoliatorAdded < dailyHighScoreExfoliatorCount) {
									treatDailyTreat.get(dailyHighScoreExfoliator[highScoreExfoliatorAdded-1]).setComments(ConstantDeclarations.DAILY);
									treatmentRule.add(treatDailyTreat.get(dailyHighScoreExfoliator[highScoreExfoliatorAdded-1]));
									trtDailyHighScoreExfo = false;
									categoryMapTreatment.put("Exfoliators-Daily", treatDailyTreat.get(dailyHighScoreExfoliator[highScoreExfoliatorAdded-1]));
									dailyCatgory="Exfoliators-Daily";
									highScoreExfoliatorAdded++;
									
								}
								
								// If Score tie
								else {
									// If skin type is �Dry� or �Normal� show �Serum�
									if ((cSkinType.equals(ConstantDeclarations.SKIN_TYPE_DRY) || cSkinType.equals(ConstantDeclarations.SKIN_TYPE_NORMAL))
											&& highScoreSerumAdded < dailyHighScoreSerumCount) {
										treatDailyTreat.get(dailyHighScoreSerum[highScoreSerumAdded-1]).setComments(ConstantDeclarations.DAILY);
										treatmentRule.add(treatDailyTreat.get(dailyHighScoreSerum[highScoreSerumAdded-1]));
										trtDailyHighScoreSerum = false;//Display rule changes
										dailyCatgory="Serum-Daily";
										categoryMapTreatment.put("Serum-Daily", treatDailyTreat.get(dailyHighScoreSerum[highScoreSerumAdded-1]));
										highScoreSerumAdded++;
										
									}
									// If the skin type is �Combination� or �Oily� show �Exfoliator�
									else if ((cSkinType.equals(ConstantDeclarations.SKIN_TYPE_NORMAL_OILY) || cSkinType.equals(ConstantDeclarations.SKIN_TYPE_OILY))
											&& highScoreExfoliatorAdded < dailyHighScoreExfoliatorCount) {
										treatDailyTreat.get(dailyHighScoreExfoliator[highScoreExfoliatorAdded-1]).setComments(ConstantDeclarations.DAILY);
										treatmentRule.add(treatDailyTreat.get(dailyHighScoreExfoliator[highScoreExfoliatorAdded-1]));
										trtDailyHighScoreExfo = false;
										categoryMapTreatment.put("Exfoliators-Daily", treatDailyTreat.get(dailyHighScoreExfoliator[highScoreExfoliatorAdded-1]));
										dailyCatgory="Exfoliators-Daily";
										highScoreExfoliatorAdded++;
										
									}
								}
							}
						}
						if(dailyCatgory.equals("")
								&& Objects.nonNull(highScoreDailySerumHydrator)
								&& Objects.nonNull(highScoreDailySerumHydrator[dailyTreatmentCount-1])
								&& Objects.nonNull(treatDailyTreat.get(highScoreDailySerumHydrator[dailyTreatmentCount-1]))) {
							dailyCatgory="Serums-Hydrator-Daily";
							treatDailyTreat.get(highScoreDailySerumHydrator[dailyTreatmentCount-1]).setComments(ConstantDeclarations.DAILY);
							treatmentRule.add(treatDailyTreat.get(highScoreDailySerumHydrator[dailyTreatmentCount-1]));
							dailyTreatmentCount++;
						}
						
						if(dailyCatgory.equals("")) {
							dailyCatgory="Serums-Retinol-Daily";
							treatDailyTreat.get(trtTarHighScoreDailySerumRetinolProId).setComments(ConstantDeclarations.DAILY);
							treatmentRule.add(treatDailyTreat.get(trtTarHighScoreDailySerumRetinolProId));
							dailyTreatmentCount++;
						}
						if(dailyCatgory.equals("")) {
							dailyCatgory="Serums-VitaminC-Daily";
							treatDailyTreat.get(trtTarHighScoreDailySerumVitaminProId).setComments(ConstantDeclarations.DAILY);
							treatmentRule.add(treatDailyTreat.get(trtTarHighScoreDailySerumVitaminProId));
							dailyTreatmentCount++;
						}
						
					}
				} catch (Exception e) {
					System.err.println("=========== Error In Daily Treatments Section ===========");
					e.printStackTrace();
				}
				
			}
			for(int i=0; i < treatmentRule.size(); i++) {
				if(treatmentRule.get(i).getProdType() == ConstantDeclarations.SERUMSVITAMINC && treatmentRule.get(i).getMcategoryName() == "Treatments") {
					if (treatDailyTreat.get(i).getProdType().equals(ConstantDeclarations.AGESPOTSSUNDAMAGE) && (trtTarOneAgeSpotSelect || trtTarOneHyperpigSelect)) {
						for(int j=0; j < treatDailyTreat.size(); j++) {
							if(treatDailyTreat.get(j).getProdType() == ConstantDeclarations.AGESPOTSSUNDAMAGE && treatDailyTreat.get(i).getMcategoryName() == "Treatments") {
								treatDailyTreat.remove(j);
							}
						}
				}
				}
			}
			
			// Treat - Targeted
			// Changes for OIl base Product -- Start
			if(treatmentRule.size() >0) {
				if (isOilBased(treatmentRule)) {
					categoryMapTreatmentAll.clear();
					categoryMapTreatmentAll = withoutOilMap(treatNightlyTreatForOil, treatDailyTreatForOil);
					if (categoryMapTreatmentAll != null) {
						categoryMapTreatment.clear();
						categoryMapTreatment=categoryMapTreatmentAll;
						if(getFinalmap() != null && !getFinalmap().isEmpty()) {
							 finalCategory.clear();
							Set<Float> keys= getFinalmap().keySet();
							for (Float floatKey : keys) {
								 ArrayList lstObject = getFinalmap().get(floatKey);
								 finalCategory.addAll(lstObject);
							}
						}
					}
				}
				
				System.out.println("finalCategory: "+finalCategory);
				System.out.println("CategoryMapTreatment: "+categoryMapTreatment.keySet());
				System.out.println("CategoryMapTreatmentNew: "+categoryMapTreatmentAll.keySet());
			}
			
			// Changes for OIl base Product -- Start
			//RemoveDailyCategory
			if(dailyCatgory!= null
					&& !dailyCatgory.isEmpty()  //Added empty string & index check - Shivam - 20/03/2024
					&& dailyCatgory.indexOf("-") != -1) {
				dailyCatgory = dailyCatgory.substring(0,dailyCatgory.indexOf("-"));
				Set<String> categorySet = categoryMapTreatment.keySet();
				categoryMapTreatment.remove(dailyCatgory+"-Daily");
				categoryMapTreatment.remove(dailyCatgory+"-Night");
				}
			//Display rule changes - Start

			if (treatTargetedTreat.size() > 1 ) { //Change for Other Map List
				// 							Treat - Targeted -1	
				for (int i = 0; i < treatTargetedTreat.size(); i++) {
//					System.out.println("For 1");
					// Sub-category = �Decollete and Neck� && customer has selected �Decollete andNeck� as their no. 2 concern
					// Sub-category = �Acne Spot Treatment� && customer has selected �Persistent Acne� as their no.2 concern
					// Sub-category = �Age Spots/Sun Damage; Hyperpigmentation� && customer has selected �Age Spots/Sun Damage� or �Hyperpigmentation� as their no. 2 concern
					if (targetedOneTreatmentCount <= 3
							&& ((treatTargetedTreat.get(i).getProdType().equals(ConstantDeclarations.DECNECK)
									&& trtTarOneDecNeckSelect)
									|| (treatTargetedTreat.get(i).getProdType().equals(ConstantDeclarations.ACNESPOT)
											&& (trtTarOnePersistAcneSelect || trtTarOneOccAcneSelect))
									|| (treatTargetedTreat.get(i).getProdType().equals(ConstantDeclarations.AGESPOTSSUNDAMAGE)
											&& (trtTarOneAgeSpotSelect || trtTarOneHyperpigSelect)))) {
						treatTargetedTreat.get(i).setComments(ConstantDeclarations.TARGETEDONE);
						treatmentRule.add(treatTargetedTreat.get(i));
						targetedOneTreatmentCount++;
						trtTarOne = true; // to indicate product received
					}
					
					/*
					 * Combined conditions of the below code in the above if condition 
					 * & commented below code to avoid code repetition - Shivam - 16/02/2024
					// Sub-category = �Acne Spot Treatment� && customer has selected �Persistent Acne� as their no.2 concern
					else if (!trtTarOne 
							&& treatTargetedTreat.get(i).getProdType().equals(ConstantDeclarations.ACNESPOT)
							&& (trtTarOnePersistAcneSelect || trtTarOneOccAcneSelect)) {
						treatTargetedTreat.get(i).setComments(ConstantDeclarations.TARGETEDONE);
						treatmentRule.add(treatTargetedTreat.get(i));
						trtTarOne = true; // to indicate product received
					}
					// Sub-category = �Age Spots/Sun Damage; Hyperpigmentation� && customer has
					//  selected �Age Spots/Sun Damage� or �Hyperpigmentation� as their no. 2 concern
					 
					else if (!trtTarOne
							&& treatTargetedTreat.get(i).getProdType().equals(ConstantDeclarations.AGESPOTSSUNDAMAGE)
							&& (trtTarOneAgeSpotSelect || trtTarOneHyperpigSelect)) {
						treatTargetedTreat.get(i).setComments(ConstantDeclarations.TARGETEDONE);
						treatmentRule.add(treatTargetedTreat.get(i));
						trtTarOne = true; // to indicate product received
					}*/
				}
			}
			targetedOneTreatmentCount = 1;
			for(int i=0;i< treatDailyTreat.size();i++) {
//				System.out.println("For 1");
				// Sub-category = �Redness� && customer has selected �Redness/Blotchiness� as their no. 2 concern
				if (targetedOneTreatmentCount <= 3 
						&& treatDailyTreat.get(i).getProdType().equals(ConstantDeclarations.REDNESS) 
						&& trtTarOneRednessSelect) {
					treatDailyTreat.get(i).setComments(ConstantDeclarations.TARGETEDONE);
					treatmentRule.add(treatDailyTreat.get(i));
					targetedOneTreatmentCount++;
					trtTarOne = true; // to indicate product received
				}
			}
				// Pending for Client clarification
				// If the customer picked none of the above concerns as their #2 concern
				//Display rule changes - End
			targetedOneTreatmentCount = 1;
			for(int categoryMapTreatmentIterator = 0; targetedOneTreatmentCount <= 3 && categoryMapTreatmentIterator < categoryMapTreatment.size(); categoryMapTreatmentIterator++) {
				if (skinConcernSelection.size() >= 1 &&
						!(trtTarOneAgeSpotSelect|| trtTarOneHyperpigSelect) &&
						!(trtTarOnePersistAcneSelect || trtTarOneOccAcneSelect) && 
						!trtTarOneDecNeckSelect && !trtTarOneRednessSelect) {
					// if the No Exfoliators product has recommended in Daily Treatment
					if(dailyCatgory!=null) {
						
						Set<String> categorySet = categoryMapTreatment.keySet();
						
						String removedKey="";
						if(finalCategory.size()>0) {
							for (String key : finalCategory) {
								Object obj1= categoryMapTreatment.get(key+"-Daily"+targetedOneTreatmentCount);
								Object obj2= categoryMapTreatment.get(key+"-Night"+targetedOneTreatmentCount);
								if(obj1!=null && obj2!=null) {
									
									if(Float.valueOf(((ProductVO)obj1).getScore())>Float.valueOf(((ProductVO)obj2).getScore())){
										categoryMapTreatment.remove(key+"-Daily"+targetedOneTreatmentCount);
										categoryMapTreatment.remove(key+"-Night"+targetedOneTreatmentCount);
										removedKey=key+"-Daily";
										if(obj1!=null) {
											((ProductVO)obj1).setComments(ConstantDeclarations.TARGETEDONE);
											treatmentRule.add((ProductVO)obj1);
											trtTarOne = true; // to indicate product received
											targetedOneTreatmentCount++;
											if(key.contains("Exfoliators")) {
												treatment_one_two_have_expoliatore=true;
											}
											break;
										}
									}else {
										categoryMapTreatment.remove(key+"-Daily"+targetedOneTreatmentCount);
										categoryMapTreatment.remove(key+"-Night"+targetedOneTreatmentCount);
										removedKey=key+"-Night";
										if(obj2!=null) {
											((ProductVO)obj2).setComments(ConstantDeclarations.TARGETEDONE);
											treatmentRule.add((ProductVO)obj2);
											trtTarOne = true; // to indicate product received
											targetedOneTreatmentCount++;
											if(key.contains("Exfoliators")) {
												treatment_one_two_have_expoliatore=true;
											}
											break;
										}
									}
								}
								if(obj1!=null){
									categoryMapTreatment.remove(key+"-Daily"+targetedOneTreatmentCount);
									categoryMapTreatment.remove(key+"-Night"+targetedOneTreatmentCount);
									removedKey=key+"-Daily";
									if(obj1!=null) {
										((ProductVO)obj1).setComments(ConstantDeclarations.TARGETEDONE);
										treatmentRule.add((ProductVO)obj1);
										trtTarOne = true; // to indicate product received
										targetedOneTreatmentCount++;
										if(key.contains("Exfoliators")) {
											treatment_one_two_have_expoliatore=true;
										}
										break;
									}
								}
								
								if(obj2!=null){
									categoryMapTreatment.remove(key+"-Night"+targetedOneTreatmentCount);
									removedKey=key+"-Daily";
									if(obj2!=null) {
										((ProductVO)obj2).setComments(ConstantDeclarations.TARGETEDONE);
										treatmentRule.add((ProductVO)obj2);
										trtTarOne = true; // to indicate product received
										targetedOneTreatmentCount++;
										if(key.contains("Exfoliators")) {
											treatment_one_two_have_expoliatore=true;
										}
										break;
									}
								}
							}
						}							
					}
					
				}
				
			}
				//Display rule changes - Start
				// Treat - Targeted -2
			if(skinConcernSelection.size()>2) {//If the customer did not select a #3 concern do not show a product for Targeted #2
				if (treatTargetedTreat.size() > 1 ) {
				
				for (int i = 0; i < treatTargetedTreat.size(); i++) {
					// Sub-category = �Decollete and Neck� && customer has selected �Decollete and Neck� as their no. 3 concern
					// Sub-category = �Acne Spot Treatment� && customer has selected �Persistent Acne� as their no.3 concern
					// Sub-category = �Age Spots/Sun Damage; Hyperpigmentation� && customer has selected �Age Spots/Sun Damage� or �Hyperpigmentation� as their no. 2 concern
					if (targetedTwoTreatmentCount <= 3
							&& ((treatTargetedTreat.get(i).getProdType().equals(ConstantDeclarations.DECNECK)
									&& trtTarTwoDecNeckSelect)
									||(treatTargetedTreat.get(i).getProdType().equals(ConstantDeclarations.ACNESPOT)
											&& (trtTarTwoPersistAcneSelect || trtTarTwoOccAcneSelect))
									||(treatTargetedTreat.get(i).getProdType().equals(ConstantDeclarations.AGESPOTSSUNDAMAGE) 
											&& (trtTarTwoAgeSpotSelect || trtTarTwoHyperpigSelect)))) {
						treatTargetedTreat.get(i).setComments(ConstantDeclarations.TARGETEDTWO);
						treatmentRule.add(treatTargetedTreat.get(i));
						targetedTwoTreatmentCount++;
						trtTarTwo = true; // to indicate product received
					}
					// Sub-category = �Redness� && customer has selected �Redness/Blotchiness� as their no. 3 concern
					
					/*
					 * Combined conditions of the below code in the above if condition 
					 * & commented below code to avoid code repetition - Shivam - 16/02/2024
					// Sub-category = �Acne Spot Treatment� && customer has selected �Persistent Acne� as their no.3 concern
					else if (!trtTarTwo 
							&& treatTargetedTreat.get(i).getProdType().equals(ConstantDeclarations.ACNESPOT)
							&& (trtTarTwoPersistAcneSelect || trtTarTwoOccAcneSelect)) {
						treatTargetedTreat.get(i).setComments(ConstantDeclarations.TARGETEDTWO);
						treatmentRule.add(treatTargetedTreat.get(i));
						trtTarTwo = true; // to indicate product received
					}
					// Sub-category = �Age Spots/Sun Damage; Hyperpigmentation� && customer has
					// selected �Age Spots/Sun Damage� or �Hyperpigmentation� as their no. 2 concern
					
					else if (!trtTarTwo 
							&& treatTargetedTreat.get(i).getProdType().equals(ConstantDeclarations.AGESPOTSSUNDAMAGE) 
							&& (trtTarTwoAgeSpotSelect || trtTarTwoHyperpigSelect)) {
						treatTargetedTreat.get(i).setComments(ConstantDeclarations.TARGETEDTWO);
						treatmentRule.add(treatTargetedTreat.get(i));
						trtTarTwo = true; // to indicate product received
					}*/
				}
			}// if completed for when to use="Targeted Treatment"
				targetedTwoTreatmentCount = 1;
				for(int i=0;i< treatDailyTreat.size();i++) {
					// Sub-category = �Redness� && customer has selected �Redness/Blotchiness� as their no. 2 concern
					 if (targetedTwoTreatmentCount <= 3
							 && treatDailyTreat.get(i).getProdType().equals(ConstantDeclarations.REDNESS) 
							 && trtTarTwoRednessSelect) {
						 treatDailyTreat.get(i).setComments(ConstantDeclarations.TARGETEDTWO);
						 treatmentRule.add(treatDailyTreat.get(i));
						 targetedTwoTreatmentCount++;
						 trtTarTwo = true; // to indicate product received
						}
					}
//				for (ProductVO productVO : treatmentRule) {
//					System.out.println(productVO.getProdType() + " -"+ productVO.getProdName() + " - "+productVO.getComments());
//				}
				// Pending for Client clarification
				// If the customer DID NOT SELECT a #3 concern
				targetedTwoTreatmentCount = 1;
				for(int categoryMapTreatmentIterator = 0; targetedTwoTreatmentCount <= 3 && categoryMapTreatmentIterator < categoryMapTreatment.size(); categoryMapTreatmentIterator++) {
					if (!trtTarTwoDecNeckSelect 
							&& !(trtTarTwoAgeSpotSelect || trtTarTwoHyperpigSelect) 
							&& !(trtTarTwoPersistAcneSelect || trtTarTwoOccAcneSelect) 
							&& !trtTarTwoRednessSelect) {
						//Display rule changes - Start
						// Pending for Client clarification
						if(finalCategory.size()>0) {
							for (String key : finalCategory) {
								
								Object obj1= categoryMapTreatment.get(key+"-Daily"+targetedTwoTreatmentCount);
								Object obj2= categoryMapTreatment.get(key+"-Night"+targetedTwoTreatmentCount);
								if(obj1!=null && obj2!=null) {
									if(Float.valueOf(((ProductVO)obj1).getScore())>Float.valueOf(((ProductVO)obj2).getScore())){
										categoryMapTreatment.remove(key+"-Daily"+targetedTwoTreatmentCount);
										if(obj1!=null) {
											((ProductVO)obj1).setComments(ConstantDeclarations.TARGETEDTWO);
											treatmentRule.add((ProductVO)obj1);
											trtTarTwo=true;
											targetedTwoTreatmentCount++;
											if(key.contains("Exfoliators")) {
												treatment_one_two_have_expoliatore=true;
											}
											break;
										}
									}else {
										categoryMapTreatment.remove(key+"-Night"+targetedTwoTreatmentCount);
										if(obj2!=null) {
											((ProductVO)obj2).setComments(ConstantDeclarations.TARGETEDTWO);
											treatmentRule.add((ProductVO)obj2);
											trtTarTwo=true;
											targetedTwoTreatmentCount++;
											if(key.contains("Exfoliators")) {
												treatment_one_two_have_expoliatore=true;
											}
											break;
										}
									}
								}
								if(obj1!=null){
									categoryMapTreatment.remove(key+"-Daily"+targetedTwoTreatmentCount);
									if(obj1!=null) {
										((ProductVO)obj1).setComments(ConstantDeclarations.TARGETEDTWO);
										treatmentRule.add((ProductVO)obj1);
										trtTarTwo=true;
										targetedTwoTreatmentCount++;
										if(key.contains("Exfoliators")) {
											treatment_one_two_have_expoliatore=true;
										}
										break;
									}
								}
								
								if(obj2!=null){
									categoryMapTreatment.remove(key+"-Night"+targetedTwoTreatmentCount);
									if(obj2!=null) {
										((ProductVO)obj2).setComments(ConstantDeclarations.TARGETEDTWO);
										treatmentRule.add((ProductVO)obj2);
										trtTarTwo=true;
										targetedTwoTreatmentCount++;
										if(key.contains("Exfoliators")) {
											treatment_one_two_have_expoliatore=true;
										}
										break;
									}
								}
							}
						}
						System.out.println("After Treat 2: "+categoryMapTreatment.keySet());
						//Display rule changes - End
					}
					
				}
			}
		
				// 	Weekly Treatment
			// no.1 Weekly treatment Exfoliators product
			
			
			if(treatWeeklyTreat.size()>1) {
				for (int i = 0; i < treatWeeklyTreat.size(); i++) {
					if(Float.valueOf(treatWeeklyTreat.get(i).getScore()) >=0){
						if (weeklyExfoliatorsCount <= 3 && treatWeeklyTreat.get(i).getProdType().equals(ConstantDeclarations.EXFOLIATORS)) {
							trtHighScoreWeeklyExfoProId = i;
							highScoreWeeklyExfoliatorProductIndices[weeklyExfoliatorsCount-1] = i;
							trtHighScoreWeeklyExfoProScore = Float.valueOf(treatWeeklyTreat.get(i).getScore());
							weeklyExfoliatorsCount++;
							trtHighScoreWeeklyExfo = true; // to indicate product received
						}
						// no.1 Weekly treatment Mask product
						if (weeklyMasksCount <= 3 && treatWeeklyTreat.get(i).getProdType().equals(ConstantDeclarations.MASKS)) {
							trtHighScoreWeeklyMaskProId = i;
							highScoreWeeklyMaskProductIndices[weeklyMasksCount-1] = i;
							trtHighScoreWeeklyMaskProScore = Float.valueOf(treatWeeklyTreat.get(i).getScore());
							weeklyMasksCount++;
							trtHighScoreWeeklyMask = true; // to indicate product received
						}
						// no.1 Weekly treatment Peels product
						if (weeklyPeelsCount <= 3 && treatWeeklyTreat.get(i).getProdType().equals(ConstantDeclarations.PEELS)) {
							trtHighScoreWeeklyPeelProId = i;
							highScoreWeeklyPeelProductIndices[weeklyPeelsCount-1] = i;
							trtHighScoreWeeklyPeelProScore = Float.valueOf(treatWeeklyTreat.get(i).getScore());
							weeklyPeelsCount++;
							trtHighScoreWeeklyPeel = true; // to indicate product received
						}
					}
				}
				
				
				// Find the #1 highest scoring product by comparing products from Exfoliators/Mask/Peel Weekly treatment
				
				int weeklyMaskAdded = 1;
				int weeklyPeelAdded = 1;
				try {
					for (int i = 0; i < treatWeeklyTreat.size(); i++) {
						//Display rule changes - c Start
						if (trtHighScoreWeeklyMaskProScore > trtHighScoreWeeklyPeelProScore 
								&& weeklyMaskAdded < weeklyMasksCount
								&& trtHighScoreWeeklyMaskProScore != -1
								&& Objects.nonNull(highScoreWeeklyMaskProductIndices)
								&& Objects.nonNull(highScoreWeeklyMaskProductIndices[weeklyMaskAdded-1])
								&& Objects.nonNull(treatWeeklyTreat.get(highScoreWeeklyMaskProductIndices[weeklyMaskAdded-1]))
								&& weeklyPeelAdded < 2) {
							treatWeeklyTreat.get(highScoreWeeklyMaskProductIndices[weeklyMaskAdded-1]).setComments(ConstantDeclarations.WEEKLY);
							treatmentRule.add(treatWeeklyTreat.get(highScoreWeeklyMaskProductIndices[weeklyMaskAdded-1]));
							trtWeeklyExfoOrMaskOrPeel = true; // to indicate product received
							weeklyMaskAdded++;
							trtWeeklyMask= true;//Display rule changes
						} else if (trtHighScoreWeeklyPeelProScore > trtHighScoreWeeklyMaskProScore 
								&& trtHighScoreWeeklyPeelProScore != -1) {
							//if the #1 or #2 Targeted Treatment = Category �Treatment�, sub-category = �Exfoliators�, when to use = �Targeted� 
							if (!treatment_one_two_have_expoliatore
									&& weeklyPeelAdded < weeklyPeelsCount
									&& Objects.nonNull(highScoreWeeklyPeelProductIndices)
									&& Objects.nonNull(highScoreWeeklyPeelProductIndices[weeklyPeelAdded-1])
									&& Objects.nonNull(treatWeeklyTreat.get(highScoreWeeklyPeelProductIndices[weeklyPeelAdded-1]))
									&& weeklyMaskAdded < 2) {
								treatWeeklyTreat.get(highScoreWeeklyPeelProductIndices[weeklyPeelAdded-1]).setComments(ConstantDeclarations.WEEKLY);
								treatmentRule.add(treatWeeklyTreat.get(highScoreWeeklyPeelProductIndices[weeklyPeelAdded-1]));
								trtWeeklyExfoOrMaskOrPeel = true; // to indicate product received
								weeklyPeelAdded++;
								trtWeeklyPeel = true;//Display rule changes
							} else {
								if(Objects.nonNull(highScoreWeeklyMaskProductIndices)
										&& weeklyMaskAdded < weeklyMasksCount
										&& Objects.nonNull(highScoreWeeklyMaskProductIndices[weeklyMaskAdded-1])
										&& Objects.nonNull(treatWeeklyTreat.get(highScoreWeeklyMaskProductIndices[weeklyMaskAdded-1]))
										&& weeklyPeelAdded < 2) {
									treatWeeklyTreat.get(highScoreWeeklyMaskProductIndices[weeklyMaskAdded-1]).setComments(ConstantDeclarations.WEEKLY);
									treatmentRule.add(treatWeeklyTreat.get(highScoreWeeklyMaskProductIndices[weeklyMaskAdded-1]));
									trtWeeklyExfoOrMaskOrPeel = true; // to indicate product received
									weeklyMaskAdded++;
									trtWeeklyMask= true;//Display rule changes
								}
							}
						}
						else {
							if (trtHighScoreWeeklyMask 
									&& weeklyMaskAdded < weeklyMasksCount
									&& Objects.nonNull(highScoreWeeklyMaskProductIndices)
									&& Objects.nonNull(highScoreWeeklyMaskProductIndices[weeklyMaskAdded-1])
									&& Objects.nonNull(treatWeeklyTreat.get(highScoreWeeklyMaskProductIndices[weeklyMaskAdded-1]))
									&& weeklyPeelAdded < 2) {
								treatWeeklyTreat.get(highScoreWeeklyMaskProductIndices[weeklyMaskAdded-1]).setComments(ConstantDeclarations.WEEKLY);
								treatmentRule.add(treatWeeklyTreat.get(highScoreWeeklyMaskProductIndices[weeklyMaskAdded-1]));
								trtWeeklyExfoOrMaskOrPeel = true; // to indicate product received
								weeklyMaskAdded++;
								trtWeeklyMask= true;//Display rule changes
							}
						}				
						
						// At home weekly changes
						if (trtWeeklyMask 
								&& weeklyPeelAdded < weeklyPeelsCount
								&& Objects.nonNull(highScoreWeeklyPeelProductIndices)
								&& Objects.nonNull(highScoreWeeklyPeelProductIndices[weeklyPeelAdded-1])
								&& Objects.nonNull(treatWeeklyTreat.get(highScoreWeeklyPeelProductIndices[weeklyPeelAdded-1]))) {
							treatWeeklyTreat.get(highScoreWeeklyPeelProductIndices[weeklyPeelAdded-1]).setComments(ConstantDeclarations.ATHOMEWEEKLY);
							treatmentRule.add(treatWeeklyTreat.get(highScoreWeeklyPeelProductIndices[weeklyPeelAdded-1]));
							
							weeklyPeelAdded++;
							atHomeFacialWeekly = true;
						}
						else if (trtWeeklyPeel 
								&& weeklyMaskAdded < weeklyMasksCount
								&& Objects.nonNull(highScoreWeeklyMaskProductIndices)
								&& Objects.nonNull(highScoreWeeklyMaskProductIndices[weeklyMaskAdded-1])
								&& Objects.nonNull(treatWeeklyTreat.get(highScoreWeeklyMaskProductIndices[weeklyMaskAdded-1]))) {
							treatWeeklyTreat.get(highScoreWeeklyMaskProductIndices[weeklyMaskAdded-1]).setComments(ConstantDeclarations.ATHOMEWEEKLY);
							treatmentRule.add(treatWeeklyTreat.get(highScoreWeeklyMaskProductIndices[weeklyMaskAdded-1]));
							weeklyMaskAdded++;
							atHomeFacialWeekly = true;
							//Display rule changes - End
						}
						else if(!trtWeeklyExfoliator && !atHomeFacialWeekly ) {
							if(dailyCatgory.equalsIgnoreCase("Exfoliators-Night") ||dailyCatgory.equalsIgnoreCase("Exfoliators-Daily")) {
								if(finalCategory.size()>0)  {
									for (String key : finalCategory) {
										
										Object obj1= categoryMapTreatment.get(key+"-Daily");
										Object obj2= categoryMapTreatment.get(key+"-Night");
										if(obj1!=null && obj2!=null) {
											if(Float.valueOf(((ProductVO)obj1).getScore())>Float.valueOf(((ProductVO)obj2).getScore())){
												categoryMapTreatment.remove(key+"-Daily");
												if(obj1!=null) {
													((ProductVO)obj1).setComments(ConstantDeclarations.ATHOMEWEEKLY);
													treatmentRule.add((ProductVO)obj1);
													atHomeFacialWeekly = true;
													if(key.contains("Exfoliators")) {
														treatment_one_two_have_expoliatore=true;
													}
													break;
												}
											}else {
												categoryMapTreatment.remove(key+"-Night");
												if(obj2!=null) {
													((ProductVO)obj2).setComments(ConstantDeclarations.ATHOMEWEEKLY);
													treatmentRule.add((ProductVO)obj2);
													atHomeFacialWeekly = true;
													if(key.contains("Exfoliators")) {
														treatment_one_two_have_expoliatore=true;
													}
													break;
												}
											}
										}
										if(obj1!=null){
											categoryMapTreatment.remove(key+"-Daily");
											if(obj1!=null) {
												((ProductVO)obj1).setComments(ConstantDeclarations.ATHOMEWEEKLY);
												treatmentRule.add((ProductVO)obj1);
												atHomeFacialWeekly = true;
												if(key.contains("Exfoliators")) {
													treatment_one_two_have_expoliatore=true;
												}
												break;
											}
										}
										
										if(obj2!=null){
											categoryMapTreatment.remove(key+"-Night");
											if(obj2!=null) {
												((ProductVO)obj2).setComments(ConstantDeclarations.ATHOMEWEEKLY);
												treatmentRule.add((ProductVO)obj2);
												atHomeFacialWeekly = true;
												if(key.contains("Exfoliators")) {
													treatment_one_two_have_expoliatore=true;
												}
												break;
											}
										}
									}
								}
							}
						}
					}
					
				} catch (Exception e) {
					System.err.println("=========== Error In Weekly/At home weekly Treatments Section ===========");
					e.printStackTrace();
				}
			}
		}
		System.out.println("treatmentRule:"+treatmentRule.size());
		treatmentProducts.add(treatmentRule);
													// Sun care 
		List <ProductVO> sunCareRule = new ArrayList();
		List sunCareProducts = new ArrayList();
		List <ProductVO> dailyFacialSunCareProducts = new ArrayList();

		if (!sunCareList.isEmpty()) {
			for (int i = 0; i < sunCareList.size(); i++) {
				if (sunCareList.get(i).getProdType().equals(ConstantDeclarations.FCSUNSCREEN)
						&& sunCareList.get(i).getWhenToUse()!=null 
						&& sunCareList.get(i).getWhenToUse().equals(ConstantDeclarations.WHEN_TO_USE_DLYFCSUN)
						&& Float.valueOf(sunCareList.get(i).getScore()) >= ConstantDeclarations.SCORE_80)//Added Score condition as per new Display Rules - Shivam - 11/12/2023
					dailyFacialSunCareProducts.add(sunCareList.get(i));
				}
		}
		if (!dailyFacialSunCareProducts.isEmpty()) {
				/** #1 highest scoring Facial Sunscreen where sub-category �Facial Sunscreens�
				* with when to use attribute = �Daily Facial Sunscreen� and "Lifestyle Based
				* Recs 2" equals �lots of computer screen time� and If the score is above 80 .
				*/
					for (int i = 0; i < dailyFacialSunCareProducts.size(); i++) {
						if (facialSunScreenCount <= 3
							&& dailyFacialSunCareProducts.get(i).getLifeStyleBasedRecs2()!= null
							&& dailyFacialSunCareProducts.get(i).getLifeStyleBasedRecs2().equals(ConstantDeclarations.LIFESTYLEBASEDRECS2_LOTSCOMPSCNTIME)
							&& Float.valueOf(dailyFacialSunCareProducts.get(i).getScore()) >= ConstantDeclarations.SCORE_80 
							&& custSpecificationList.contains(ConstantDeclarations.REGULAR_ACTIVITIES_LOTSCOMPSCNTIME)) // Changes For UAT testing //Updated Score condition as per new Display Rules - Shivam - 11/12/2023
							{
							sunCareRule.add(dailyFacialSunCareProducts.get(i));
							facialSunScreenCount++;
						}
						if(facialSunScreenCount <= 3
								&& Float.valueOf(dailyFacialSunCareProducts.get(i).getScore()) >= ConstantDeclarations.SCORE_80) {
							sunCareRule.add(dailyFacialSunCareProducts.get(i));
							facialSunScreenCount++;
						}
					}

		}
		sunCareProducts.add(sunCareRule);
		
		recproducts.add(cleanserProducts);
		recproducts.add(moisturizerProducts);
		recproducts.add(treatmentProducts);
		recproducts.add(eyeConcernProducts);
		recproducts.add(sunCareProducts);
		
		return recproducts;
	}
	public ArrayList getBeautyProductAssortNoBrand(ArrayList product,
			String prodtype, CustomerProfileVO cp, String prodline) {
		ArrayList recproducts = new ArrayList();

		String cSkinType = cp.getSkinType();
		ArrayList custSkinConcernList = cp.getSkinConcernList();
		ArrayList custEyeConcernList = cp.getEyeConcernList();
		ArrayList custSkinConditionList = cp.getSkinConditionList();
		boolean dryflag = false;
		boolean normalflag = false;
		boolean comboflag = false;
		boolean oilyflag = false;
		boolean acneflag = false;
		boolean agespotflag = false;
		boolean rosaceaflag = false;
		boolean sensitiveflag = false ;
		boolean hyperflag = false ;
		boolean poresflag = false ;
		boolean rednessflag = false ;
		boolean dceflag = false ;
		boolean pufflag = false ;
        boolean brandflag = false ;
        String cbrandrepeat = "" ;
        String mbrandrepeat = "" ;
        String tbrandrepeat = "" ;
        String ebrandrepeat = "" ;
        ArrayList cbrand = new ArrayList() ;
        ArrayList mbrand = new ArrayList() ;
        ArrayList tbrand = new ArrayList() ;
        ArrayList ebrand = new ArrayList() ;
        ArrayList pbrand = new ArrayList() ;
        Hashtable fbrand = new Hashtable() ;
        
		ArrayList cleanse = new ArrayList();
		ArrayList tone = new ArrayList();
		ArrayList treat = new ArrayList();
		ArrayList etreat = new ArrayList();
		ArrayList moisturize = new ArrayList();
		ArrayList nightmoisturize = new ArrayList();
		ArrayList exfoliant = new ArrayList();
        ArrayList spf = new ArrayList() ;
        boolean spfflag = false ;
		//Hashtable prodids = new Hashtable() ;
        int ccnt = 0 ;
        int mcnt = 0 ;
        int tcnt = 0 ;
        int ecnt = 0;
        /**
		* Added By Synoptek (25-03-2020)
		* Added Display rules 
		*/
        /*START :: ADDED BY Synoptek::HARESH :: Variable Declaration for Business Rule apply for Regimen wise Changes*/
            
        int clregcnt = 0;
        boolean clPreClflag  = false ;
        boolean clTonerflag  = false ;
        boolean clFwipesflag  = false ;
        
        boolean moistMOWithoutSPFflag  = false ;
        boolean moistMOWithSPFflag  = false ;
        boolean moistNightCreamflag  = false ;
        boolean moistMistflag  = false ;
        
        int trtregcnt = 0;
        boolean trtTargetAcneflag = false;
        boolean trtTargetAgeflag = false;
        boolean trtTargetNeckflag = false;
        boolean trtTargetBoostflag = false;
        boolean trtTargetSerumflag = false;
        boolean trtWeekflag = false;
        boolean atLeastOneSerumflag = false;
        
        boolean eyeDailyETreatflag = false;
        boolean eyeEmaskflag = false;
        
        ArrayList sunCarelst = new ArrayList();
        boolean sunCareflag = false;
        
        ArrayList primerlst = new ArrayList();
        boolean primerflag = false;
        
        /*END :: ADDED BY Synoptek::HARESH :: Variable Declaration for Business Rule apply for Regimen wise Changes*/
        
      //  printArrayList(product) ;
        if (cSkinType.equals("T001"))
        	dryflag = true ;
        if (cSkinType.equals("T002"))
        	normalflag = true ;
        if (cSkinType.equals("T003"))
        	comboflag = true ;
        if (cSkinType.equals("T004"))
        	oilyflag = true ;
        
		for (int m = 0; m < custEyeConcernList.size(); m++) {
			if (custEyeConcernList.get(m).equals("C002"))
				dceflag = true;
			if (custEyeConcernList.get(m).equals("E001"))
				pufflag = true;
		}
		for (int m = 0; m < custSkinConcernList.size(); m++) {
			if (custSkinConcernList.get(m).equals("C019"))
				rednessflag = true;
			if (custSkinConcernList.get(m).equals("C007"))
				agespotflag = true;
			if (custSkinConcernList.get(m).equals("C012"))
				poresflag = true;
		}
		for (int m = 0; m < custSkinConditionList.size(); m++) {
			if (custSkinConditionList.get(m).equals("D008"))
				acneflag = true;
			if (custSkinConditionList.get(m).equals("D002"))
				acneflag = true;
			if (custSkinConditionList.get(m).equals("D001"))
				sensitiveflag = true;
			if (custSkinConditionList.get(m).equals("D003"))
				rosaceaflag = true;
			if (custSkinConditionList.get(m).equals("D006"))
				hyperflag = true;
		}
       
        
        /*START :: ADDED BY Synoptek::HARESH :: Method Changes for Regimen wise Business rules */
        
        for (int i = product.size() - 1; i >= 0; i--) {
        	        	
			String prodspf = (String)((ProductVO) product.get(i)).getSPFId() ;
								
			if (((ProductVO) product.get(i)).getMasterProdType().equals(
					ConstantDeclarations.PRODUCT_MASTER_TYPE_CLEANSE)) {
//				System.out.println("1 "+((ProductVO) product.get(i)).getProductLine()) ;
	//			System.out.println("2 "+(cbrandrepeat)) ;
//Synoptek::HARESH		if (!cbrand.contains(((ProductVO) product.get(i)).getProductLine())){ 
					/*
					 * System.out.println( ((ProductVO) product.get(i)).getProdName() + "\tscore\t"
					 * + ((ProductVO) product.get(i)).getScore() + "\tingr\t" + ((ProductVO)
					 * product.get(i)).getIngrScore() + "\tbase\t" + ((ProductVO)
					 * product.get(i)).getBaseScore() + "\tbump\t" + ((ProductVO)
					 * product.get(i)).getBumpScore() + "\tfn\t" + ((ProductVO)
					 * product.get(i)).getFunctionScore() + "\tprice\t" + ((ProductVO)
					 * product.get(i)).getPrice());
					 */
				
                 if (ccnt < 6) 
				  {
                	 
				  if(clregcnt < 3 && "1002".equals(((ProductVO) product.get(i)).getProdType()) &&
             			 "Daily Cleanser".equalsIgnoreCase(((ProductVO) product.get(i)).getWhenToUse())  ) {
             			 cleanse.add(product.get(i));
             			 clregcnt++;
             			 ccnt++;
             	 }else if(!clPreClflag && "1001".equals(((ProductVO) product.get(i)).getProdType()) &&
             			 "Daily Cleanser".equalsIgnoreCase(((ProductVO) product.get(i)).getWhenToUse()) && Float.valueOf(((ProductVO) product.get(i)).getScore()) > ConstantDeclarations.SCORE_MORE_THAN) {
             		 cleanse.add(product.get(i));
             		 clPreClflag = true;
             		ccnt++;
             	 }else if(!clTonerflag && "1004".equals(((ProductVO) product.get(i)).getProdType()) &&
             			 "Daily Cleanser".equalsIgnoreCase(((ProductVO) product.get(i)).getWhenToUse()) && Float.valueOf(((ProductVO) product.get(i)).getScore()) > ConstantDeclarations.SCORE_MORE_THAN) {
             		 cleanse.add(product.get(i));
             		 clTonerflag = true;
             		ccnt++;
             	 }else if(!clFwipesflag && "1005".equals(((ProductVO) product.get(i)).getProdType()) && 
             			 Float.valueOf(((ProductVO) product.get(i)).getScore()) > ConstantDeclarations.SCORE_MORE_THAN) {
             		 cleanse.add(product.get(i));
             		 clFwipesflag = true;
             		ccnt++;
             	 }
				  
				  pbrand.add(((ProductVO) product.get(i)).getProductLine()) ;
			//	  prodids.put(((ProductVO) product.get(i)).getProdId(),product.get(i)) ;
				  }
				  
				//Synoptek::HARESH	                cbrandrepeat = ((ProductVO) product.get(i)).getProductLine() ;
	                cbrand.add(((ProductVO) product.get(i)).getProductLine()) ;
//Synoptek::HARESH       } 
/*				if (((ProductVO) product.get(i)).getSPFId() != null && 
						(!prodspf.equals("U001")))
					spfflag  = true ;
*/					
			}
			if (((ProductVO) product.get(i)).getMasterProdType().equals(
					ConstantDeclarations.PRODUCT_MASTER_TYPE_MOIST)) {
    		//	 System.out.println( ((ProductVO) product.get(i)).getProdName()
    			//		+ "\tscore\t" + ((ProductVO) product.get(i)).getScore()
    			//		+ "\tingr\t" + ((ProductVO) product.get(i)).getIngrScore()
    			//		+ "\tbase\t" + ((ProductVO) product.get(i)).getBaseScore()
    			//		+ "\tbump\t" + ((ProductVO) product.get(i)).getBumpScore()
    			//		+ "\tfn\t" + ((ProductVO) product.get(i)).getFunctionScore()
    			//		+ "\tprice\t" + ((ProductVO) product.get(i)).getPrice());
				//Synoptek::HARESH  if (!mbrand.contains(((ProductVO) product.get(i)).getProductLine())){
				  if (mcnt < 4)
				  {
//				    moisturize.add(product.get(i));
				    if(!moistMOWithoutSPFflag && "1006".equals(((ProductVO) product.get(i)).getProdType()) && "Daily Moisturizer".equalsIgnoreCase(((ProductVO) product.get(i)).getWhenToUse()) && 
				    		"Does not have SPF".equalsIgnoreCase(((ProductVO) product.get(i)).getMultiBenefits())) {
				    	moisturize.add(product.get(i));
				    	moistMOWithoutSPFflag = true;
				    	mcnt++;
				    } else if(!moistMOWithSPFflag && ("1006".equals(((ProductVO) product.get(i)).getProdType()) || "1009".equals(((ProductVO) product.get(i)).getProdType())) 
				    		&& "Daily Moisturizer".equalsIgnoreCase(((ProductVO) product.get(i)).getWhenToUse()) && 
				    		"Has SPF".equalsIgnoreCase(((ProductVO) product.get(i)).getMultiBenefits()) && 
				    		Float.valueOf(((ProductVO) product.get(i)).getScore()) > ConstantDeclarations.SCORE_MORE_THAN) {
				    	moisturize.add(product.get(i));
				    	moistMOWithSPFflag = true;
				    	mcnt++;
				    } else if(!moistNightCreamflag && "1007".equals(((ProductVO) product.get(i)).getProdType()) && "Nightly Moisturizer".equalsIgnoreCase(((ProductVO) product.get(i)).getWhenToUse()) &&
				    		Float.valueOf(((ProductVO) product.get(i)).getScore()) > ConstantDeclarations.SCORE_MORE_THAN) {
				    	moisturize.add(product.get(i));
				    	moistNightCreamflag = true;
				    	mcnt++;
				    }  else if(!moistMistflag && "1010".equals(((ProductVO) product.get(i)).getProdType()) && "Targeted Moisturizer".equalsIgnoreCase(((ProductVO) product.get(i)).getWhenToUse()) &&
				    		Float.valueOf(((ProductVO) product.get(i)).getScore()) > ConstantDeclarations.SCORE_MORE_THAN) {
				    	moisturize.add(product.get(i));
				    	moistMistflag = true;
				    	mcnt++;
				    }
				    
				    pbrand.add(((ProductVO) product.get(i)).getProductLine()) ;
				//    prodids.put(((ProductVO) product.get(i)).getProdId(),product.get(i)) ;
				  }
				  
	                mbrand.add(((ProductVO) product.get(i)).getProductLine()) ;
	              //Synoptek::HARESH  mbrandrepeat = ((ProductVO) product.get(i)).getProductLine() ;
	              //Synoptek::HARESH                }
/*				if (((ProductVO) product.get(i)).getSPFId() != null && 
						(!prodspf.equals("U001")))
					spfflag  = true ;
*/					
/*				if (((ProductVO) product.get(i)).getProdType().equals(
						ConstantDeclarations.PRODUCT_TYPE_MOIST_NIGHT))
					nightmoisturize.add(product.get(i));
				else if (!((ProductVO) product.get(i)).getProdType().equals(
						ConstantDeclarations.PRODUCT_TYPE_MOIST_NIGHT))
				{
					moisturize.add(product.get(i));
					prodids.add(((ProductVO) product.get(i)).getProdId()) ;
				} */
			}
			if (((ProductVO) product.get(i)).getMasterProdType().equals(
					ConstantDeclarations.PRODUCT_MASTER_TYPE_TONE)) {
				tone.add(product.get(i));
			}
			
			if (((ProductVO) product.get(i)).getMasterProdType().equals(
					ConstantDeclarations.PRODUCT_MASTER_TYPE_TREAT)) {
     		//	 System.out.println( ((ProductVO) product.get(i)).getProdName()
    		//			+ "\tscore\t" + ((ProductVO) product.get(i)).getScore()
    		//			+ "\tingr\t" + ((ProductVO) product.get(i)).getIngrScore()
    		//			+ "\tbase\t" + ((ProductVO) product.get(i)).getBaseScore()
    		//			+ "\tbump\t" + ((ProductVO) product.get(i)).getBumpScore()
    		//			+ "\tfn\t" + ((ProductVO) product.get(i)).getFunctionScore()
    		//			+ "\tprice\t" + ((ProductVO) product.get(i)).getPrice());
				//Synoptek::HARESH   if (!tbrand.contains(((ProductVO) product.get(i)).getProductLine())){
                	
                  if (tcnt < 10)
				  {
                	  if(trtregcnt < 3 && ("1018".equals(((ProductVO) product.get(i)).getProdType()) || "1020".equals(((ProductVO) product.get(i)).getProdType()) ||
                			  "1021".equals(((ProductVO) product.get(i)).getProdType()) || "1019".equals(((ProductVO) product.get(i)).getProdType())) &&
                  			 "Daily Treatment".equalsIgnoreCase(((ProductVO) product.get(i)).getWhenToUse())  ) {
//                  		 if(trtregcnt < 3) {
                  			treat.add(product.get(i));
                  			tcnt++;
                  			trtregcnt++;
//                  		 }     
                  			 if("1018".equals(((ProductVO) product.get(i)).getProdType())) {
                  				atLeastOneSerumflag=true;
                  			 }
                  	 } else if(!atLeastOneSerumflag && trtregcnt==2 && "1018".equals(((ProductVO) product.get(i)).getProdType()) && 
                  			"Daily Treatment".equalsIgnoreCase(((ProductVO) product.get(i)).getWhenToUse()) ) {
                   		 	treat.add(product.get(i));
                   		 	atLeastOneSerumflag = true;
                   		 	tcnt++;
                   		 
                   	 } else if(!trtTargetAcneflag && "1015".equals(((ProductVO) product.get(i)).getProdType()) && 
                  			"Targeted Treatment".equalsIgnoreCase(((ProductVO) product.get(i)).getWhenToUse()) ) {
                   		 	treat.add(product.get(i));
                   		 	trtTargetAcneflag = true;
                   		 	tcnt++;
                  		 
                  	 } else if(!trtTargetAgeflag && "1019".equals(((ProductVO) product.get(i)).getProdType()) && 
                   			"Targeted Treatment".equalsIgnoreCase(((ProductVO) product.get(i)).getWhenToUse()) ) {
                   		 	treat.add(product.get(i));
                   		 	trtTargetAgeflag = true;
                   		 	tcnt++;
                  	 } else if(!trtTargetNeckflag && "1024".equals(((ProductVO) product.get(i)).getProdType()) && 
                    			"Targeted Treatment".equalsIgnoreCase(((ProductVO) product.get(i)).getWhenToUse()) ) {
                    		 treat.add(product.get(i));
                    		 trtTargetNeckflag = true;
                    		 tcnt++;
                    		 
                   	 } else if(!trtTargetBoostflag && "1022".equals(((ProductVO) product.get(i)).getProdType()) && 
                    			"Targeted Treatment".equalsIgnoreCase(((ProductVO) product.get(i)).getWhenToUse()) ) {
                    		 treat.add(product.get(i));
                    		 trtTargetBoostflag = true;
                    		 tcnt++;
                    		 
                   	 } else if(!trtTargetSerumflag && "1018".equals(((ProductVO) product.get(i)).getProdType()) && 
                    			"Nightly Treatment".equalsIgnoreCase(((ProductVO) product.get(i)).getWhenToUse()) ) {
                    		 treat.add(product.get(i));
                    		 trtTargetSerumflag = true;
                    		 tcnt++;
                    		 
                   	 } else if(!trtWeekflag && ("1020".equals(((ProductVO) product.get(i)).getProdType()) || "1016".equals(((ProductVO) product.get(i)).getProdType()) ||
                   			 	"1017".equals(((ProductVO) product.get(i)).getProdType())) && 
                    			"Weekly Treatment".equalsIgnoreCase(((ProductVO) product.get(i)).getWhenToUse()) ) {
                    		 treat.add(product.get(i));
                    		 trtWeekflag = true;
                    		 tcnt++;
                   	 }	
                	  
//				    treat.add(product.get(i));
				    pbrand.add(((ProductVO) product.get(i)).getProductLine()) ;
				  //  prodids.put(((ProductVO) product.get(i)).getProdId(),product.get(i)) ;
				  }
				  
	                tbrand.add(((ProductVO) product.get(i)).getProductLine()) ;
	              //Synoptek::HARESH                tbrandrepeat = ((ProductVO) product.get(i)).getProductLine() ;
	              //Synoptek::HARESH    }
/*				if (((ProductVO) product.get(i)).getSPFId() != null && 
						(!prodspf.equals("U001")))
					spfflag  = true ;
*/					
			}
			if (((ProductVO) product.get(i)).getMasterProdType().equals(
					ConstantDeclarations.PRODUCT_MASTER_TYPE_ETREAT)) {
				//Synoptek::HARESH if (!ebrand.contains(((ProductVO) product.get(i)).getProductLine())){
			      if (ecnt < 2)
			      {
			    	  
			    	  if(!eyeDailyETreatflag && "1012".equals(((ProductVO) product.get(i)).getProdType()) && "Daily Eye Treatment".equalsIgnoreCase(((ProductVO) product.get(i)).getWhenToUse()) ) {
			    		  etreat.add(product.get(i));
			    		  eyeDailyETreatflag = true;
			    		  ecnt++;
					  } else if(!eyeEmaskflag && "1013".equals(((ProductVO) product.get(i)).getProdType()) && "Weekly Eye Treatment".equalsIgnoreCase(((ProductVO) product.get(i)).getWhenToUse())) {
						  etreat.add(product.get(i));
						  eyeEmaskflag = true;
						  ecnt++;
					  }
			    	  
//				    etreat.add(product.get(i));
					pbrand.add(((ProductVO) product.get(i)).getProductLine()) ;
				  //  prodids.put(((ProductVO) product.get(i)).getProdId(),product.get(i)) ;
					
			      }
			     
	                ebrand.add(((ProductVO) product.get(i)).getProductLine()) ;
	              //Synoptek::HARESH  ebrandrepeat = ((ProductVO) product.get(i)).getProductLine() ;
	              //Synoptek::HARESH   }
/*				if (((ProductVO) product.get(i)).getSPFId() != null && 
						(!prodspf.equals("U001")))
					spfflag  = true ;
*/					
			}
			if (((ProductVO) product.get(i)).getMasterProdType().equals(
					ConstantDeclarations.PRODUCT_MASTER_TYPE_EXFOLI)) {
				exfoliant.add(product.get(i));
			}
/*			if (((ProductVO) product.get(i)).getSPFId() != null && 
						(!prodspf.equals("U001")))
			{
				spf.add(product.get(i)) ;
			}
*/			
			if (((ProductVO) product.get(i)).getMasterProdType().equals(
					ConstantDeclarations.PRODUCT_MASTER_TYPE_UV)) {
				spf.add(product.get(i));
			}
			
			if( !sunCareflag && ((ProductVO) product.get(i)).getMasterProdType().equals(
					ConstantDeclarations.PRODUCT_MASTER_TYPE_UV) && "1029".equals(((ProductVO) product.get(i)).getProdType()) && 
					"Daily Facial Sunscreen".equalsIgnoreCase(((ProductVO) product.get(i)).getWhenToUse()) && "lots of computer screen time".equalsIgnoreCase(((ProductVO) product.get(i)).getLifeStyleBasedRecs2())) {
				sunCarelst.add(product.get(i));
				sunCareflag = true;
			}
			
			if(!primerflag && ((ProductVO) product.get(i)).getMasterProdType().equals(
					ConstantDeclarations.PRODUCT_MASTER_TYPE_PRIMER)) {
				primerlst.add(product.get(i));
				primerflag=true;
			}
			
		}
		
		
		/*END :: ADDED BY Synoptek::HARESH :: Method Changes for Regimen wise Business rules */
		
/*		Enumeration enp = prodids.keys() ;
		int pcnt  = 0 ;
		String sqlid = "(" ;
		String pid = "" ;
		while (enp.hasMoreElements())
		{
			pid = (String) enp.nextElement() ;
			sqlid = sqlid+"'"+pid+"'"+"," ;
		}
		sqlid = sqlid.substring(0,sqlid.length()-1)+")" ;
*/		
//		prodids = (Hashtable) getBeautyProductInfo(prodids,sqlid) ;
		
		System.out.println("vbbbb cl "+cleanse.size()) ;
		System.out.println("vbbbb tn "+tone.size()) ;
		System.out.println("vbbbb ex "+exfoliant.size()) ;
		System.out.println("vbbbb moi "+moisturize.size()) ;
		System.out.println("vbbbb nmoi "+nightmoisturize.size()) ;
		System.out.println("vbbbb trt "+treat.size()) ;
		System.out.println("vbbbb etrt "+etreat.size()) ;
		System.out.println("vbbbb spf "+spf.size()) ;
//		printArrayList(cleanse) ;		
		printArrayList(treat) ;		
		for (int i=0; i < pbrand.size(); i++)
		{
			String pbrandid = (String) pbrand.get(i) ;
			int cnt = 0 ;
			boolean bflag = false ;
			if (cbrand.contains(pbrandid))
			{
				cnt++;
			}
			if (mbrand.contains(pbrandid))
			{
				cnt++;
			}
			if (tbrand.contains(pbrandid))
			{
				cnt++;
			}
			if (ebrand.contains(pbrandid))
			{
				cnt++;
			}
			if (cnt > 2)
			  bflag = true ;
			if (bflag)
		     fbrand.put(pbrandid,"Y") ;	 
		}
		
		recproducts.add(cleanse);
		recproducts.add(tone);
		recproducts.add(exfoliant);
		recproducts.add(moisturize);
		recproducts.add(nightmoisturize);
		recproducts.add(treat);
		recproducts.add(etreat);
		recproducts.add(spf);
		recproducts.add(fbrand);
		recproducts.add(sunCarelst);
		recproducts.add(primerlst);

		return recproducts;
	}
	
}