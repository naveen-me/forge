package com.svb.webservices.model;

import java.util.Arrays;
import java.util.List;

public class CleanScoreModel {

	private String score;
	private List<DoesContainModel> doesNotContain;
	private List<DoesContainModel> doesContain;
	
	public String getScore() {
		return score;
	}
	public void setScore(String score) {
		this.score = score;
	}
	
	public List<DoesContainModel> getDoesNotContain() {
		return doesNotContain;
	}
	public void setDoesNotContain(List<DoesContainModel> doesNotContain) {
		this.doesNotContain = doesNotContain;
	}
	public List<DoesContainModel> getDoesContain() {
		return doesContain;
	}
	public void setDoesContain(List<DoesContainModel> doesContain) {
		this.doesContain = doesContain;
	}
	
	
	@Override
	public String toString() {
		return "CleanScoreModel [score=" + score + ", doesNotContain=" + doesNotContain + ", doesContain=" + doesContain
				+ "]";
	}
	
	
}
