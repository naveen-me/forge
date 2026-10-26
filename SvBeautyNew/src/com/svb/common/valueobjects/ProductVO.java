package com.svb.common.valueobjects;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * <p>
 * Company:
 * </p>
 *
 * @author not attributable
 * @version 1.0
 */

import java.util.Hashtable;
import java.util.List;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class ProductVO implements Serializable{

	/**
	 * Added By Synoptek (29-11-2020)
	 * Added variables to gets required result for ingredient score calculation
	 */
	 
	private static final long serialVersionUID = 746074754336300177L;
	private String functionScore;
	private String prodId;
	private String price;
	private String prodType;
	private String prodName;
	private String productLine;
	private String productLinePrime;
	private String preferenceRating;
	private String proddate;
	private String image;
	private String ingredients[][]; // first element is the ingredient id , second one
	// is the ingredient concentration in this product
	private String ingrFuncCnt;
	private String ingrScore;
	private String replenishmentStatus;
	private String shortDesc;
	private String score;
	private String manualScore;
	private String masterProdType;
	private String base;
	private String baseScore;
	private String bumpScore;
	private String spfid;
	private boolean tobeDeleted;
	private boolean complProduct;
	private ArrayList ingrRatingsList;
	private Hashtable individualIngrRatings = new Hashtable();
	private String sellcopy;

	private boolean oldornew = false;
	private String prodlaunchdate;
	private String prodregimendate;
	private boolean bestSeller = false;
	private String prodregimenflag = "";
	private String shadeImage = "";
	private String shadeName = "";
	private String comments = "";
	/** Added by Synoptek(29-11-2019) [ Added upc and sku fields ] */
	private String upc ="";
	private String sku = "";
	/** Added by Synoptek(30-12-2019) [ Added master category and base count fields ] */  
	private String mcategoryName;
	private String baseCount;
	private Hashtable profileBaseRatings = new Hashtable();
	private Hashtable profileIngrRatings = new Hashtable();
	private String ingrWeightage;
	private String baseWeightage;
	/** Added by Synoptek(21-02-2020) [ Added 4 attributes ] */  
	private String whenToUse;
	private String multiBenefits;
	private String lifeStyleBasedRecs1;
	private String lifeStyleBasedRecs2;
	private String reason;
	private String eliminatedIngrName;
	private List eliminatedIngrList;
	private String warningText;

// Get RX flag from the sv_product_rx_warning for ingredients in the product - Start
	private String flagAcne="false";
	private String flagRetinolt="false";
	private String flagSteriods="false";
// Get RX flag from the sv_product_rx_warning for ingredients in the product - End
	
	//Added to display RX Warning Text - Shivam - 19/12/2023 - Start
	private String rxWarningText;
	
	public String getRxWarningText() {
		return rxWarningText;
	}
	public void setRxWarningText(String rxWarningText) {
		this.rxWarningText = rxWarningText;
	}
	//Added to display RX Warning Text - Shivam - 19/12/2023 - End
	
	// Get brand name for product recommendation api - Deep - Start
	private String productLineName;

	public String getProductLineName() {
		return productLineName;
	}
	public void setProductLineName(String productLineName) {
		this.productLineName = productLineName;
	}	
	// Get brand name for product recommendation api - Deep - End
	
	// for so and so skintype for so and so producttype the ingredient ratings
	// are stored in a vector and placed in the
	// hastable which has key as
	// 01-ingrvalue/basevalue-concetration-prodmastertype and the value are the
	// ingredients that have ratings
	
	public static Comparator<ProductVO> RatingComparator = new Comparator<ProductVO>() {

		public int compare(ProductVO p1, ProductVO p2) {
		   Long product1 = (long)Double.parseDouble(p1.getScore().trim());
		   Long product2 = (long)Double.parseDouble(p2.getScore().trim());

		   //ascending order
//		   return product1.compareTo(product2);

		   //descending order
		   return product2.compareTo(product1);
	    }		
	};






	/**
	 * @return Returns the base.
	 */
	public String getBase() {
		return base;
	}
	/**
	 * @param base The base to set.
	 */
	public void setBase(String base) {
		this.base = base;
	}

	/**
	 * @return Returns the basescore.
	 */
	public String getBaseScore() {
		return baseScore;
	}

// Get RX flag from the sv_product_rx_warning for ingredients in the product - Start
	public String getFlagSteriods() {
		return flagSteriods;
	}
	public void setFlagSteriods(String flagSteriods) {
		this.flagSteriods = flagSteriods;
	}
	public String getFlagAcne() {
		return flagAcne;
	}
	public void setFlagAcne(String flagAcne) {
		this.flagAcne = flagAcne;
	}
	public String getFlagRetinolt() {
		return flagRetinolt;
	}
	public void setFlagRetinolt(String flagRetinolt) {
		this.flagRetinolt = flagRetinolt;
	}
// Get RX flag from the sv_product_rx_warning for ingredients in the product - End
	/**
	 * @param basescore The basescore to set.
	 */
	public void setBaseScore(String basescore) {
		this.baseScore = basescore;
	}

	/**
	 * @return Returns the bumpscore.
	 */
	public String getBumpScore() {
		return bumpScore;
	}

	/**
	 * @param bumpscore The bumpscore to set.
	 */
	public void setBumpScore(String bumpscore) {
		this.bumpScore = bumpscore;
	}

	/**
	 * @return Returns the complproduct.
	 */
	public boolean isComplproduct() {
		return complProduct;
	}

	/**
	 * @param complproduct The complproduct to set.
	 */
	public void setComplProduct(boolean complproduct) {
		this.complProduct = complproduct;
	}

	public boolean getComplProduct() {
		return complProduct;
	}

	/**
	 * @return Returns the functionscore.
	 */
	public String getFunctionScore() {
		return functionScore;
	}

	/**
	 * @param functionscore The functionscore to set.
	 */
	public void setFunctionscore(String functionscore) {
		this.functionScore = functionscore;
	}

	/**
	 * @return Returns the imgfilename.
	 */
	public String getImage() {
		return image;
	}

	/**
	 * @param imgfilename The imgfilename to set.
	 */
	public void setImage(String imgfilename) {
		this.image = imgfilename;
	}

	/**
	 * @return Returns the individualingrratings.
	 */
	public Hashtable getIndividualingrratings() {
		return individualIngrRatings;
	}

	/**
	 * @param individualingrratings The individualingrratings to set.
	 */
	public void setIndividualingrratings(Hashtable individualingrratings) {
		this.individualIngrRatings = individualingrratings;
	}

	/**
	 * @return Returns the ingredients.
	 */
	public String[][] getIngredients() {
		return ingredients;
	}

	/**
	 * @param ingredients The ingredients to set.
	 */
	public void setIngredients(String[][] ingredients) {
		this.ingredients = ingredients;
	}

	/**
	 * @return Returns the ingrfnccnt.
	 */
	public String getIngrfnccnt() {
		return ingrFuncCnt;
	}

	/**
	 * @param ingrfnccnt The ingrfnccnt to set.
	 */
	public void setIngrfnccnt(String ingrfnccnt) {
		this.ingrFuncCnt = ingrfnccnt;
	}

	/**
	 * @return Returns the ingrratings.
	 */
	public ArrayList getIngrratings() {
		return ingrRatingsList;
	}

	/**
	 * @param ingrratings The ingrratings to set.
	 */
	public void setIngrratings(ArrayList ingrratings) {
		this.ingrRatingsList = ingrratings;
	}


	/**
	 * @return Returns the manualscore.
	 */
	public String getManualScore() {
		return manualScore;
	}

	/**
	 * @param manualscore The manualscore to set.
	 */
	public void setManualScore(String manualscore) {
		this.manualScore = manualscore;
	}

	/**
	 * @return Returns the masterprodtype.
	 */
	public String getMasterProdType() {
		return masterProdType;
	}

	/**
	 * @param masterprodtype The masterprodtype to set.
	 */
	public void setMasterProdType(String masterprodtype) {
		this.masterProdType = masterprodtype;
	}

	/**
	 * @return Returns the preferncerating.
	 */
	public String getPreferncerating() {
		return preferenceRating;
	}

	/**
	 * @param preferncerating The preferncerating to set.
	 */
	public void setPreferncerating(String preferncerating) {
		this.preferenceRating = preferncerating;
	}

	/**
	 * @return Returns the price.
	 */
	public String getPrice() {
		return price;
	}

	/**
	 * @param price The price to set.
	 */
	public void setPrice(String price) {
		this.price = price;
	}

	/**
	 * @return Returns the proddate.
	 */
	public String getProddate() {
		return proddate;
	}

	/**
	 * @param proddate The proddate to set.
	 */
	public void setProddate(String proddate) {
		this.proddate = proddate;
	}

	/**
	 * @return Returns the prodid.
	 */
	public String getProdId() {
		return prodId;
	}

	/**
	 * @param prodid The prodid to set.
	 */
	public void setProdId(String prodid) {
		this.prodId = prodid;
	}

	/**
	 * @return Returns the prodname.
	 */
	public String getProdName() {
		return prodName;
	}

	/**
	 * @param prodname The prodname to set.
	 */
	public void setProdName(String prodname) {
		this.prodName = prodname;
	}

	/**
	 * @return Returns the prodtype.
	 */
	public String getProdType() {
		return prodType;
	}

	/**
	 * @param prodtype The prodtype to set.
	 */
	public void setProdType(String prodtype) {
		this.prodType = prodtype;
	}

	/**
	 * @return Returns the productline.
	 */
	public String getProductLine() {
		return productLine;
	}

	/**
	 * @param productline The productline to set.
	 */
	public void setProductLine(String productline) {
		this.productLine = productline;
	}

	/**
	 * @return Returns the replenishmentstat.
	 */
	public String getProductLinePrime() {
		return productLinePrime;
	}

	/**
	 * @param productline The productline to set.
	 */
	public void setProductLinePrime(String productlinePrime) {
		this.productLinePrime = productlinePrime;
	}

	public String getReplenishmentstat() {
		return replenishmentStatus;
	}

	/**
	 * @param replenishmentstat The replenishmentstat to set.
	 */
	public void setReplenishmentstat(String replenishmentstat) {
		this.replenishmentStatus = replenishmentstat;
	}

	/**
	 * @return Returns the score.
	 */
	public String getScore() {
		return score;
	}

	/**
	 * @param score The score to set.
	 */
	public void setScore(String score) {
		this.score = score;
	}

	/**
	 * @return Returns the shortdesc.
	 */
	public String getShortDesc() {
		return shortDesc;
	}

	/**
	 * @param shortdesc The shortdesc to set.
	 */
	public void setShortDesc(String shortdesc) {
		this.shortDesc = shortdesc;
	}

	/**
	 * @return Returns the tobedeleted.
	 */
	public boolean isTobeDeleted() {
		return tobeDeleted;
	}

	/**
	 * @param tobedeleted The tobedeleted to set.
	 */
	public void setTobeDeleted(boolean tobedeleted) {
		this.tobeDeleted = tobedeleted;
	}

	/**
	 * @return Returns the ingrScore.
	 */
	public String getIngrScore() {
		return ingrScore;
	}

	/**
	 * @param ingrScore The ingrScore to set.
	 */
	public void setIngrScore(String ingrScore) {
		this.ingrScore = ingrScore;
	}

	/**
	 * @return Returns the oldornew.
	 */
	public boolean isOldorNew() {
		return oldornew;
	}

	/**
	 * @param oldornew The oldornew to set.
	 */
	public void setOldorNew(boolean oldornew) {
		this.oldornew = oldornew;
	}

	/**
	 * @param string
	 */
	public void setProductLaunchDate(String prodlaunchdate) {
		this.prodlaunchdate = prodlaunchdate;
	}

	public String getProductLaunchDate() {
		return prodlaunchdate;
	}

	public void setProductRegimenDate(String prodregimendate) {
		this.prodregimendate = prodregimendate;
	}

	public String getProductRegimenDate() {
		return prodregimendate;
	}

	/**
	 * @param b
	 */
	public void setBestSeller(boolean b) {
		// TODO Auto-generated method stub
		this.bestSeller = b;
	}

	public boolean isBestSeller() {
		// TODO Auto-generated method stub
		return this.bestSeller;
	}

	/**
	 * @param string
	 */
	public void setProductRegimenFlag(String prflag) {
		// TODO Auto-generated method stub
		this.prodregimenflag = prflag;
	}

	public String getProductRegimenFlag() {
		return this.prodregimenflag;
	}

	public void setShadeImage(String shadeImage) {
		this.shadeImage = shadeImage;
	}

	public String getShadeImage() {
		return shadeImage;
	}

	public void setShadeName(String shadeName) {
		this.shadeName = shadeName;
	}

	public String getShadeName() {
		return shadeName;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	/**
	 * @param string
	 */
	public void setSPFId(String spfid) {
		// TODO Auto-generated method stub
		this.spfid = spfid;
	}

	public String getSPFId() {
		return spfid;
	}

	/**
	 * @return Returns the profileBaseRatings.
	 */
	public Hashtable getProfileBaseRatings() {
		return profileBaseRatings;
	}

	/**
	 * @param profileBaseRatings The profileBaseRatings to set.
	 */
	public void setProfileBaseRatings(Hashtable profileBaseRatings) {
		this.profileBaseRatings = profileBaseRatings;
	}

	/**
	 * @return Returns the profileIngrRatings.
	 */
	public Hashtable getProfileIngrRatings() {
		return profileIngrRatings;
	}

	/**
	 * @param profileIngrRatings The profileIngrRatings to set.
	 */
	public void setProfileIngrRatings(Hashtable profileIngrRatings) {
		this.profileIngrRatings = profileIngrRatings;
	}

	/**
	 * @return Returns the spfid.
	 */
	public String getSpfid() {
		return spfid;
	}

	/**
	 * @param spfid The spfid to set.
	 */
	public void setSpfid(String spfid) {
		this.spfid = spfid;
	}

	/**
	 * @param string
	 */
	public void setSellCopy(String sellcopy) {
		// TODO Auto-generated method stub
		this.sellcopy = sellcopy;
	}

	public String getSellCopy() {
		return this.sellcopy;
	}

	public String getUpc() {
		return upc;
	}

	public void setUpc(String upc) {
		this.upc = upc;
	}

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}
	public String getMcategoryName() {
		return mcategoryName;
	}

	public void setMcategoryName(String mcategoryName) {
		this.mcategoryName = mcategoryName;
	}
	public String getBaseCount() {
		return baseCount;
	}

	public void setBaseCount(String baseCount) {
		this.baseCount = baseCount;
	}
	
	
	/**
	 * @return the ingrWeightage
	 */
	public String getIngrWeightage() {
		return ingrWeightage;
	}

	/**
	 * @param ingrWeightage the ingrWeightage to set
	 */
	public void setIngrWeightage(String ingrWeightage) {
		this.ingrWeightage = ingrWeightage;
	}

	/**
	 * @return the baseWeightage
	 */
	public String getBaseWeightage() {
		return baseWeightage;
	}

	/**
	 * @param baseWeightage the baseWeightage to set
	 */
	public void setBaseWeightage(String baseWeightage) {
		this.baseWeightage = baseWeightage;
	}

	/**
	 * @return the whenToUse
	 */
	public String getWhenToUse() {
		return whenToUse;
	}

	/**
	 * @param whenToUse the whenToUse to set
	 */
	public void setWhenToUse(String whenToUse) {
		this.whenToUse = whenToUse;
	}

	/**
	 * @return the multiBenefits
	 */
	public String getMultiBenefits() {
		return multiBenefits;
	}

	/**
	 * @param multiBenefits the multiBenefits to set
	 */
	public void setMultiBenefits(String multiBenefits) {
		this.multiBenefits = multiBenefits;
	}

	/**
	 * @return the lifeStyleBasedRecs1
	 */
	public String getLifeStyleBasedRecs1() {
		return lifeStyleBasedRecs1;
	}

	/**
	 * @param lifeStyleBasedRecs1 the lifeStyleBasedRecs1 to set
	 */
	public void setLifeStyleBasedRecs1(String lifeStyleBasedRecs1) {
		this.lifeStyleBasedRecs1 = lifeStyleBasedRecs1;
	}

	/**
	 * @return the lifeStyleBasedRecs2
	 */
	public String getLifeStyleBasedRecs2() {
		return lifeStyleBasedRecs2;
	}

	/**
	 * @param lifeStyleBasedRecs2 the lifeStyleBasedRecs2 to set
	 */
	public void setLifeStyleBasedRecs2(String lifeStyleBasedRecs2) {
		this.lifeStyleBasedRecs2 = lifeStyleBasedRecs2;
	}

	
	
	/**
	 * @return the reason
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * @param reason the reason to set
	 */
	public void setReason(String reason) {
		this.reason = reason;
	}

	/**
	 * @return the eliminatedIngrName
	 */
	public String getEliminatedIngrName() {
		return eliminatedIngrName;
	}

	/**
	 * @param eliminatedIngrName the eliminatedIngrName to set
	 */
	public void setEliminatedIngrName(String eliminatedIngrName) {
		this.eliminatedIngrName = eliminatedIngrName;
	}
	
	/**
	 * @return the eliminatedIngrList
	 */
	public List getEliminatedIngrList() {
		return eliminatedIngrList;
	}
	
	/**
	 * @param eliminatedIngrList the eliminatedIngrList to set
	 */
	public void setEliminatedIngrList(List eliminatedIngrList) {
		this.eliminatedIngrList = eliminatedIngrList;
	}
	
	/**
	 * @return the warningText
	 */
	public String getWarningText() {
		return warningText;
	}
	/**
	 * @param warningText the warningText to set
	 */
	public void setWarningText(String warningText) {
		this.warningText = warningText;
	}
	@Override
	public String toString() {
		return "ProductVO [functionScore=" + functionScore + ", prodId=" + prodId + ", price=" + price + ", prodType="
				+ prodType + ", prodName=" + prodName + ", productLine=" + productLine + ", productLinePrime="
				+ productLinePrime + ", preferenceRating=" + preferenceRating + ", proddate=" + proddate + ", image="
				+ image + ", ingredients=" + Arrays.toString(ingredients) + ", ingrFuncCnt=" + ingrFuncCnt
				+ ", ingrScore=" + ingrScore + ", replenishmentStatus=" + replenishmentStatus + ", shortDesc="
				+ shortDesc + ", score=" + score + ", manualScore=" + manualScore + ", masterProdType=" + masterProdType
				+ ", base=" + base + ", baseScore=" + baseScore + ", bumpScore=" + bumpScore + ", spfid=" + spfid
				+ ", tobeDeleted=" + tobeDeleted + ", complProduct=" + complProduct + ", ingrRatingsList="
				+ ingrRatingsList + ", individualIngrRatings=" + individualIngrRatings + ", sellcopy=" + sellcopy
				+ ", oldornew=" + oldornew + ", prodlaunchdate=" + prodlaunchdate + ", prodregimendate="
				+ prodregimendate + ", bestSeller=" + bestSeller + ", prodregimenflag=" + prodregimenflag
				+ ", shadeImage=" + shadeImage + ", shadeName=" + shadeName + ", comments=" + comments + ", upc=" + upc
				+ ", sku=" + sku + ", mcategoryName=" + mcategoryName + ", baseCount=" + baseCount
				+ ", profileBaseRatings=" + profileBaseRatings + ", profileIngrRatings=" + profileIngrRatings
				+ ", ingrWeightage=" + ingrWeightage + ", baseWeightage=" + baseWeightage + ", whenToUse=" + whenToUse
				+ ", multiBenefits=" + multiBenefits + ", lifeStyleBasedRecs1=" + lifeStyleBasedRecs1
				+ ", lifeStyleBasedRecs2=" + lifeStyleBasedRecs2 + ", reason=" + reason + ", eliminatedIngrName="
				+ eliminatedIngrName + ", eliminatedIngrList=" + eliminatedIngrList + ", warningText=" + warningText
				+ ", flagAcne=" + flagAcne + ", flagRetinolt=" + flagRetinolt + ", flagSteriods=" + flagSteriods + "]";
	}
	
	
	
}