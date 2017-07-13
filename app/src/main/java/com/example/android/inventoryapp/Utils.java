package com.example.android.inventoryapp;

import android.text.TextUtils;

/**
 * Static helper methods
 */

public class Utils {

    /**
     * Private default constructor to avoid instantiate this class
     */
    private Utils() {
    }

    /**
     * Check if a given phone number looks as valid
     *
     * @param target The phone number to check
     * @return if it looks valid or not
     */
    public static boolean isValidPhone(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.PHONE.matcher(target).matches();
    }

    /**
     * Check if a given email address is in a valid format
     *
     * @param target The email address to check
     * @return if it looks valid or not
     */
    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

}
