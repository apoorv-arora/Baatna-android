package com.application.baatna.data;

import java.io.Serializable;

public class BLocation implements Serializable {

	int entityId;
	String entityType;
	String title;
	String parameter;
	int cityId;

	private Double latitude = 0.0;
	private Double longitude = 0.0;

	public int getEntityId() {
		return entityId;
	}

	public void setEntityId(int entityId) {
		this.entityId = entityId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public int getCityId() {
		return cityId;
	}

	public void setCityId(int cityId) {
		this.cityId = cityId;
	}

	public boolean equals(Object object) {

		if (object instanceof BLocation) {
			return parameter.equals(((BLocation) object).getParameter()) && entityId == (((BLocation) object).getEntityId());
		}
		return false;
	}


	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
}
