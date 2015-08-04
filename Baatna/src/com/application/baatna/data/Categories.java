package com.application.baatna.data;

import java.io.Serializable;

public class Categories implements Serializable {
	
	private int categoryId;
	private String category;
	
	public Categories(){
		categoryId = 0;
		category = "";
	}
	
	public Categories(int categoryId, String category){
		this.categoryId = categoryId;
		this.category = category;
	}
	
	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}
	
	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

}
