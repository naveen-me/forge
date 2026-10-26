/*
 * Created on Mar 22, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.common.valueobjects;

/**
 * @author madhav
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
import java.util.ArrayList;

public class AttribRatingVO {

	private String score 		   = "0";
	private int count			   = 0;
	private ArrayList ingrConcList = new ArrayList();
	
	/**
	* Added By Synoptek (20-01-2020)
	* To get Ingredient wise total score
	*/
	private int IngrWiseTotalScore = 0;

	/**
	 * @return Returns the count.
	 */
	public int getCount() {
		return count;
	}
	/**
	 * @param count The count to set.
	 */
	public void setCount(int count) {
		this.count = count;
	}
	/**
	 * @return Returns the ingrConcList.
	 */
	public ArrayList getIngrConcList() {
		return ingrConcList;
	}
	/**
	 * @param ingrConcList The ingrConcList to set.
	 */
	public void setIngrConcList(ArrayList ingrConcList) {
		this.ingrConcList = ingrConcList;
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
	 * @return the ingrWiseTotalScore
	 */
	public int getIngrWiseTotalScore() {
		return IngrWiseTotalScore;
	}
	/**
	 * @param ingrWiseTotalScore the ingrWiseTotalScore to set
	 */
	public void setIngrWiseTotalScore(int ingrWiseTotalScore) {
		IngrWiseTotalScore = ingrWiseTotalScore;
	}
	@Override
	public String toString() {
		return "AttribRatingVO [score=" + score + ", count=" + count + ", ingrConcList=" + ingrConcList
				+ ", IngrWiseTotalScore=" + IngrWiseTotalScore + "]";
	}
	
	
}
