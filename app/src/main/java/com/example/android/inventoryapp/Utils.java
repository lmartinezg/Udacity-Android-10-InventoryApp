package com.example.android.inventoryapp;

import android.text.TextUtils;

/**
 * Static helper methods
 */

class Utils {

    // Constants used in multiple classes
    static final int QUANTITY_LIMIT_MIN = 0;
    static final int QUANTITY_LIMIT_MAX = 1000;
    static final int MAX_PRICE = 5000;

    static final int SELECT_PHOTO = 1;

    /**
     * Private default constructor to avoid instantiating this class
     */
    protected Utils() {
    }

    /**
     * Check if a given phone number looks as valid
     *
     * @param target The phone number to check
     * @return if it looks valid or not
     */
    public static boolean isValidPhone(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.PHONE.matcher(target)
                .matches();
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
