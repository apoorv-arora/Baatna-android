package com.application.baatna.utils;

/**
 * Includes all the get asyncs.*/
public class DataManager {

	private static DataManager dataManager;

    public static synchronized DataManager getInstance() {

        if(dataManager == null)
            dataManager = new DataManager();
        return  dataManager;
    }

    private DataManager(){}

}
