package com.fingerstring.mdooreleyers.mdooreleyersproject1;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(indices = {
        @Index(name = "client_unique_index", value = {"firstName", "lastName", "phoneNumber"}, unique = true)
})
public class Client {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "clientID")
    private long clientID;
    @ColumnInfo(name = "firstName")
    private String firstName;
    @ColumnInfo(name = "lastName")
    private String lastName;
    @ColumnInfo(name = "phoneNumber")
    private String phoneNumber;
    @ColumnInfo(name="disableReminders")
    private boolean disableReminders;

    public Client(String firstName, String lastName, String phoneNumber, boolean disableReminders)
    {
        setFirstName(firstName);
        setLastName(lastName);
        setPhoneNumber(phoneNumber);
        setDisableReminders(disableReminders);
    }

    public long getClientID() { return clientID; }

    public void setClientID(long clientID) { this.clientID = clientID; }

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

    public String getFullName()
    {
        return firstName + " " + lastName;
    }

    public void setDisableReminders (boolean d) { this.disableReminders = d;}

    public boolean getDisableReminders() { return this.disableReminders; }
}
