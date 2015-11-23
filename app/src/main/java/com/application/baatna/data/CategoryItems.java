package com.application.baatna.data;

public class CategoryItems {

	private int id;
	private String name;
	private String resId;

	public CategoryItems() {
		id = 0;
		name = "";
		resId = "";
	}

	public CategoryItems(int id, String name, String resId) {
		this.id = id;
		this.name = name;
		this.resId = resId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getResId() {
		return resId;
	}

	public void setResId(String resId) {
		this.resId = resId;
	}

}
