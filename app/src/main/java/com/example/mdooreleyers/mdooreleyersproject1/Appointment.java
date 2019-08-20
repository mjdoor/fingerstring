package com.example.mdooreleyers.mdooreleyersproject1;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Entity
public class Appointment {
    public enum ReminderStatus { NOT_SENT, SENT, FAILED};

    @PrimaryKey(autoGenerate = true)
    public int aptID;
    @ColumnInfo(name = "firstName")
    private String firstName;
    @ColumnInfo(name = "lastName")
    private String lastName;
    @ColumnInfo(name = "phoneNumber")
    private String phoneNumber;
    @ColumnInfo(name = "startTime")
    private long startTime;
    @ColumnInfo(name = "duration")
    private int duration; //minutes
    @ColumnInfo(name = "reminderStatus")
    private String reminderStatus;

    // Used to set the reminder status to NOT SENT by default
    public Appointment(String first, String last, String phone, long start, int duration)
    {
        setFirstName(first);
        setLastName(last);
        setPhoneNumber(phone);
        setStartTime(start);
        setDuration(duration);
        setReminderStatus(ReminderStatus.NOT_SENT.toString());
    }
    // For database to work, the public contructor needs to have the same parameters as the fields
    public Appointment(String firstName, String lastName, String phoneNumber, long startTime, int duration, String reminderStatus) {
        setFirstName(firstName);
        setLastName(lastName);
        setPhoneNumber(phoneNumber);
        setStartTime(startTime);
        setDuration(duration);
        setReminderStatus(reminderStatus);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getReminderStatus() {
        return reminderStatus;
    }

    public void setReminderStatus(String reminderStatus) {
        this.reminderStatus = reminderStatus;
    }

    public long getEndTime()
    {
        return getStartTime() + getDuration()*TimeConstants.MILLISECONDS_PER_MINUTE;
    }

    public String getTimeSpan() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(TimeConstants.timePattern);

        String start = dateFormat.format(new Date(startTime));
        long endTime = getEndTime();
        String end = dateFormat.format(new Date(endTime));

        Calendar calUtil = Calendar.getInstance();
        long now = calUtil.getTimeInMillis();
        long midnightTonight = TimeConstants.calcEndOfDay(now);
        long midnightTomorrow = TimeConstants.calcEndOfNextDay(now);

        String relDay = "";
        if(startTime >= now && startTime < midnightTonight)
        {
            relDay = "Today";
        }
        else if(startTime >= midnightTonight && startTime < midnightTomorrow) // data coming in should only contain appointments in the future.
        {
            relDay = "Tomorrow";
        }
        else
        {
            relDay = getDateString();
        }
        return relDay + " @ " + start + " - " + end;
    }

    public String getDateString()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat(TimeConstants.datePattern);
        return dateFormat.format(new Date(startTime));
    }

    public String getDateTimeString()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat(TimeConstants.dateTimePattern);
        return dateFormat.format(new Date(startTime));
    }



    public String getFullName() {
        return firstName + " " + lastName;
    }


}