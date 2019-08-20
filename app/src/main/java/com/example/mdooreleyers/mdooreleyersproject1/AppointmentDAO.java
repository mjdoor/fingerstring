package com.example.mdooreleyers.mdooreleyersproject1;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface AppointmentDAO {
    @Query("SELECT * FROM appointment")
    List<Appointment> getAll();

    @Query("SELECT * FROM appointment WHERE startTime >= :start AND startTime < :cutOff ORDER BY startTime ASC")
    List<Appointment> getUpcoming(long start, long cutOff);

    @Query("SELECT * FROM appointment WHERE aptID = :id LIMIT 1")
    Appointment getAppointmentById(int id);

    // (startTime + duration * 1000*60) = endTime of existing appointment
    @Query("SELECT * FROM appointment WHERE ((startTime >= :propStart AND startTime <= :propEnd) OR ((startTime + duration * 1000*60) >= :propStart AND (startTime + duration * 1000*60) <= :propEnd)) AND aptID != :propID LIMIT 1")
    Appointment checkConflicts(int propID, long propStart, long propEnd);

    @Insert
    void bookAppointment(Appointment apt);

    @Update
    void updateAppointment(Appointment apt);

    @Delete
    void deleteAppointment(Appointment... apt);

    @Query("DELETE FROM appointment WHERE startTime < :cutOff")
    void deleteAppointmentsBeforeDate(long cutOff);
}