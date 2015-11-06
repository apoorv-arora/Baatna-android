package com.application.baatna.data;

import java.util.ArrayList;

public class Institution {

	private String institutionName = "";
	private ArrayList<String> branches = new ArrayList<String>();
	
	public String getInstitutionName() {
		return institutionName;
	}
	public void setInstitutionName(String institutionName) {
		this.institutionName = institutionName;
	}
	public ArrayList<String> getBranches() {
		return branches;
	}
	public void setBranches(ArrayList<String> branches) {
		this.branches = branches;
	}
}
