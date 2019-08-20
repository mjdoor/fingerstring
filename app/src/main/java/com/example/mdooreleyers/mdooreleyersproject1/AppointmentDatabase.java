package com.example.mdooreleyers.mdooreleyersproject1;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;



@Database(entities = {Appointment.class}, version = 1)
public abstract class AppointmentDatabase extends RoomDatabase {
    private static final String DB_NAME = "appointments_DB";
    private static AppointmentDatabase instance;

    public static synchronized AppointmentDatabase getInstance(Context context)
    {
        if(instance == null)
        {
            instance = Room.databaseBuilder(context.getApplicationContext(), AppointmentDatabase.class, DB_NAME).fallbackToDestructiveMigration().allowMainThreadQueries().build();
        }

        return instance;
    }

    public abstract AppointmentDAO appointmentDAO();
}
