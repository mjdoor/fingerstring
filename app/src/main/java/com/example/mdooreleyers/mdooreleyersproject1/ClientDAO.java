package com.example.mdooreleyers.mdooreleyersproject1;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface ClientDAO {
    @Query("SELECT * FROM client")
    List<Client> getAll();

    @Query("SELECT * FROM client WHERE clientID = :id")
    Client getClientByID(long id);

    @Insert
    long addClient(Client client);

    @Update
    void updateClient(Client client);

    @Delete
    void deleteClient(Client client);
}