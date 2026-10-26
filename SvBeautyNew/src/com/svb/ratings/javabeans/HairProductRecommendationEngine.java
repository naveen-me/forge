/*
 * Created on Mar 29, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.ratings.javabeans;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

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
public class HairProductRecommendationEngine extends HairRecommendationEngine {
	private static Logger logger = Logger.getLogger(HairRecommendationEngine.class
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

		boolean sktpflag = false;
		try {

			cp = (CustomerProfileVO) getCustomerProfile(customid, cp, "hair"); 
			sktpflag = true;
			if (sktpflag) {

			//vbh	eliminatedIngredients = eliminateIngredients(cp.getSkinType(),
			//vbh		cp.getSkinConditionList()); //this method gets all the
				
				// ingredients that r to be
				// eliminated for that
				// skintype and skincondition
				productsHashtable = getProdDetails(prodtype, prodselect, prodline,cp); //gets
				// the
				// product
				// details and filel
				// "prodhash" variable
				productsHashtable = getProductsIngredients(prodtype, prodselect,
						prodline, productsHashtable); //this method
				
//				System.out.println(productsHashtable.size()) ;
				// gets the
				// ingredients of
				// that product and fill in
				//prodhash variable
				
			    ingrratings = getManualRatings(cp, true);
				
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
				HairBumpRating br = new HairBumpRating();
		 	    productVOList = br.getProducts(cp, productVOList);//pass the
				
				// customerprofile and
				// product array as the
				// parameters for bump
				// calculations
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
				
				System.out.println("vbbbb 1 "+vbnproduct.size()) ;
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
					vbnproduct = (ArrayList) getHairProductAssortNoBrand(
							vbnproduct, prodtype, cp, prodline);
					System.out.println("vbbbb 2 "+vbnproduct.size()) ;
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
			if (((ProductVO) vt.get(i)).getMasterProdType().equals(
					ConstantDeclarations.PRODUCT_MASTER_TYPE_ETREAT))
			{
			 System.out.println( ((ProductVO) vt.get(i)).getProdName()
					+ "\tscore\t" + ((ProductVO) vt.get(i)).getScore()
					+ "\tingr\t" + ((ProductVO) vt.get(i)).getIngrScore()
					+ "\tbase\t" + ((ProductVO) vt.get(i)).getBaseScore()
					+ "\tbump\t" + ((ProductVO) vt.get(i)).getBumpScore()
					+ "\tfn\t" + ((ProductVO) vt.get(i)).getFunctionScore()
					+ "\tprice\t" + ((ProductVO) vt.get(i)).getPrice());
			}
	}

	public ArrayList getHairProductAssortNoBrand(ArrayList product,
			String prodtype, CustomerProfileVO cp, String prodline) {
		ArrayList recproducts = new ArrayList();

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
        
		ArrayList shampoo = new ArrayList();
		ArrayList conditioner = new ArrayList();
		ArrayList htreat = new ArrayList();
		ArrayList styling = new ArrayList();
        ArrayList finish = new ArrayList() ;
        boolean finishflag = false ;
		Hashtable prodids = new Hashtable() ;
        int ccnt = 0 ;
        int mcnt = 0 ;
        int tcnt = 0 ;
        int ecnt = 0;
  //      printArrayList(product) ;
  // vb 01/21/10
        ArrayList dryshampoos = new ArrayList() ;
        dryshampoos.add("228247") ;
        dryshampoos.add("228248") ;
        dryshampoos.add("228528") ;
        dryshampoos.add("163768") ;
        dryshampoos.add("188987") ;
        dryshampoos.add("218125") ;
        dryshampoos.add("226201") ;
        boolean dryshampooflag = false ;
        
		for (int i = product.size() - 1; i >= 0; i--) {
			//vbh String prodfinish = (String)((ProductVO) product.get(i)).getSPFId() ;
			if (((ProductVO) product.get(i)).getMasterProdType().equals(
					"1001")) {
//				System.out.println("1 "+((ProductVO) product.get(i)).getProductLine()) ;
	//			System.out.println("2 "+(cbrandrepeat)) ;
                if (!cbrand.contains(((ProductVO) product.get(i)).getProductLine()))
                {
                  if (!dryshampooflag)
                  {
				   if (ccnt < 4)
				   {
				   shampoo.add(product.get(i));
				   pbrand.add(((ProductVO) product.get(i)).getProductLine()) ;
				   // prodids.put(((ProductVO) product.get(i)).getProdId(),product.get(i)) ;
				   //  System.out.println("1 "+((ProductVO) product.get(i)).getProdId()+" name "+((ProductVO) product.get(i)).getProdName()+" score "+((ProductVO) product.get(i)).getScore()+" bump "+((ProductVO) product.get(i)).getBumpScore()+" manual "+((ProductVO) product.get(i)).getManualScore()  ) ;
				   }
				   ccnt++;
	                cbrandrepeat = ((ProductVO) product.get(i)).getProductLine() ;
	                cbrand.add(((ProductVO) product.get(i)).getProductLine()) ;
	               if ( dryshampoos.contains((((ProductVO) product.get(i)).getProdId())) )
	              	dryshampooflag = true ;
                  }
                }
/*				if (((ProductVO) product.get(i)).getSPFId() != null && 
						(!prodfinish.equals("U001")))
					finishflag  = true ;
*/					
			}
			if (((ProductVO) product.get(i)).getMasterProdType().equals(
					"1003")) {
                if (!mbrand.contains(((ProductVO) product.get(i)).getProductLine()))
                {
				  if (mcnt < 4)
				  {
				    conditioner.add(product.get(i));
					pbrand.add(((ProductVO) product.get(i)).getProductLine()) ;
			//	    prodids.put(((ProductVO) product.get(i)).getProdId(),product.get(i)) ;
			//		  System.out.println("2 "+((ProductVO) product.get(i)).getProdId()+" name "+((ProductVO) product.get(i)).getProdName()+" score "+((ProductVO) product.get(i)).getScore()+" bump "+((ProductVO) product.get(i)).getBumpScore()+" manual "+((ProductVO) product.get(i)).getManualScore()  ) ;
				  }
				  mcnt++;
	              mbrand.add(((ProductVO) product.get(i)).getProductLine()) ;
	              mbrandrepeat = ((ProductVO) product.get(i)).getProductLine() ;
                }
/*				if (((ProductVO) product.get(i)).getSPFId() != null && 
						(!prodfinish.equals("U001")))
					finishflag  = true ;
*/					
			}
			if (((ProductVO) product.get(i)).getMasterProdType().equals(
					"1005")) {
                if (!tbrand.contains(((ProductVO) product.get(i)).getProductLine()))
                {
				  if (tcnt < 4)
				  {
				    htreat.add(product.get(i));
					pbrand.add(((ProductVO) product.get(i)).getProductLine()) ;
			//	    prodids.put(((ProductVO) product.get(i)).getProdId(),product.get(i)) ;
			//		  System.out.println("3 "+((ProductVO) product.get(i)).getProdId()+" name "+((ProductVO) product.get(i)).getProdName()+" score "+((ProductVO) product.get(i)).getScore()+" bump "+((ProductVO) product.get(i)).getBumpScore()+" manual "+((ProductVO) product.get(i)).getManualScore()  ) ;
				  }
				  tcnt++;
	                tbrand.add(((ProductVO) product.get(i)).getProductLine()) ;
	                tbrandrepeat = ((ProductVO) product.get(i)).getProductLine() ;
                }
/*				if (((ProductVO) product.get(i)).getSPFId() != null && 
						(!prodfinish.equals("U001")))
					finishflag  = true ;
*/					
			}
			if (((ProductVO) product.get(i)).getMasterProdType().equals(
					"1004")) {
                if (!ebrand.contains(((ProductVO) product.get(i)).getProductLine()))
                {
			      if (ecnt < 4)
			      {
				    styling.add(product.get(i));
					pbrand.add(((ProductVO) product.get(i)).getProductLine()) ;
		//		    prodids.put(((ProductVO) product.get(i)).getProdId(),product.get(i)) ;
		//			  System.out.println("4 "+((ProductVO) product.get(i)).getProdId()+" name "+((ProductVO) product.get(i)).getProdName()+" score "+((ProductVO) product.get(i)).getScore()+" bump "+((ProductVO) product.get(i)).getBumpScore()+" manual "+((ProductVO) product.get(i)).getManualScore()  ) ;
			      }
			      ecnt++;
	                ebrand.add(((ProductVO) product.get(i)).getProductLine()) ;
	                ebrandrepeat = ((ProductVO) product.get(i)).getProductLine() ;
                }
			}
			if (((ProductVO) product.get(i)).getMasterProdType().equals(
					"1009")) {
				finish.add(product.get(i));
			}
		}
		
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
		
		System.out.println("vbbbb sh "+shampoo.size()) ;
		System.out.println("vbbbb con "+conditioner.size()) ;
		System.out.println("vbbbb htrt "+htreat.size()) ;
		System.out.println("vbbbb sty "+styling.size()) ;
		System.out.println("vbbbb finish "+finish.size()) ;

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
		
		recproducts.add(shampoo);
		recproducts.add(conditioner);
		recproducts.add(htreat);
		recproducts.add(styling);
		recproducts.add(finish);
		recproducts.add(fbrand);

		return recproducts;
	}
	
	}