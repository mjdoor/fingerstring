package com.fingerstring.mdooreleyers.mdooreleyersproject1;

import java.util.Calendar;

public interface TimeConstants {
    long MILLISECONDS_PER_SECOND = 1000;
    long MILLISECONDS_PER_MINUTE = 60*MILLISECONDS_PER_SECOND;
    long MILLISECONDS_PER_HOUR = 60*MILLISECONDS_PER_MINUTE;
    long MILLISECONDS_PER_DAY = 24*MILLISECONDS_PER_HOUR;
    long MILLISECONDS_PER_WEEK = 7 * MILLISECONDS_PER_DAY;

    String datePattern = "MMMM d, YYYY";
    String timePattern = "h:mm a";
    String dateTimePattern = datePattern + " @ " + timePattern;

    // Given the currentTime, will return the time, in milliseconds, of the next midnight (i.e. midnight tonight)
    static long calcEndOfDay(long currentTime)
    {
        Calendar calUtil = Calendar.getInstance();
        calUtil.setTimeInMillis(currentTime + TimeConstants.MILLISECONDS_PER_DAY); // move forward a day
        calUtil.set(Calendar.HOUR_OF_DAY, 0); // set time to midnight
        calUtil.set(Calendar.MINUTE, 0);
        calUtil.set(Calendar.SECOND, 0);
        return calUtil.getTimeInMillis();
    }

    //Given the current time, will return the time, in milliseconds, two midnights past the current time (i.e. midnight tomorrow night)
    static long calcEndOfNextDay(long currentTime)
    {
        return calcEndOfDay(currentTime) + MILLISECONDS_PER_DAY;
    }
}
