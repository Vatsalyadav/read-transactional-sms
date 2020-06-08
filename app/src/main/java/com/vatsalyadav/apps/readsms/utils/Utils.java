package com.vatsalyadav.apps.readsms.utils;

import android.provider.Telephony;
import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Utils {

    private static final long DAY_IN_MS = 1000 * 60 * 60 * 24;
    private static final Date DATE_SEVEN_DAYS_AGO = new Date(System.currentTimeMillis() - (7 * DAY_IN_MS));
    public static final String AMOUNT_PATTERN = "(?i)(?:(?:RS|INR|MRP)\\.?\\s?)(\\d+(:?\\,\\d+)?(\\,\\d+)?(\\.\\d{1,2})?)";
    public static final String TRANSACTIONAL_MESSAGE_ADDRESS_PATTERN = "[a-zA-Z0-9]{8}";

    public static String dateFormat(String oldStringDate) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(Long.parseLong(oldStringDate));
        return DateFormat.format("E, d MMM yyyy, HH:mm:ss", cal).toString();
    }

    public static String filterOptions() {
        return Telephony.Sms.DATE +
                ">=" +
                DATE_SEVEN_DAYS_AGO.getTime() +
                " and " +
                Telephony.Sms.ADDRESS +
                " REGEXP ?" +
                " and " +
                Telephony.Sms.TYPE +
                " = " +
                Telephony.Sms.MESSAGE_TYPE_INBOX;
    }
}
