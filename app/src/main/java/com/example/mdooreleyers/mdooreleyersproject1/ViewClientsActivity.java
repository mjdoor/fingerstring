package com.example.mdooreleyers.mdooreleyersproject1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.List;

public class ViewClientsActivity extends AppCompatActivity implements ClientAdapter.OnClientClickListener {

    private RecyclerView clientRecycler;
    private TextView noClientMsgBox;
    private List<Client> clients;

    private ClientAdapter clientAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_clients);

        clientRecycler = (RecyclerView)findViewById(R.id.ClientsRecycler);
        noClientMsgBox = (TextView)findViewById(R.id.noClientMsgTxt);
    }

    @Override
    protected void onResume() {
        super.onResume();

        clients = getAllClients();

        if(clients.size() == 0)
        {
            noClientMsgBox.setVisibility(View.VISIBLE);
            clientRecycler.setVisibility(View.INVISIBLE);
        }
        else
        {
            noClientMsgBox.setVisibility(View.INVISIBLE);
            clientRecycler.setVisibility(View.VISIBLE);

            // Create the adapter with the client info
            clientAdapter = new ClientAdapter(clients, this);
            // Set the adapter for the recycler
            clientRecycler.setAdapter(clientAdapter);
            // Set layout manager
            clientRecycler.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    private List<Client> getAllClients()
    {
        return AppointmentDatabase.getInstance(this).clientDAO().getAll();
    }

    @Override
    public void onClientClick(int position) {
        clientAdapter.setSelectedColour(position);
        Client selectedClient = clients.get(position);

        Intent intent = new Intent(this, EditClientActivity.class);
        intent.putExtra("client_id", selectedClient.getClientID());

        startActivity(intent);
    }

    public void backClick(View view) {
        finish();
    }
}
