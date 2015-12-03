package com.application.baatna.utils;

/**
 * Created by yogeshmadaan on 01/12/15.
 */
public class ValidateUtils {
    public static boolean validatePhone(String phone)
    {

        try{
            if(phone.trim().length()==0)
            {
                return false;
            }
            double n = Double.parseDouble(phone);
            if(!(phone.trim().toString().startsWith("7")||phone.trim().toString().startsWith("8")||phone.trim().toString().startsWith("9")))
                return false;
            if(phone.trim().length()==10)
                return true;

        }catch(Exception e)
        {
            return false;
        }
        return false;
    }
}
