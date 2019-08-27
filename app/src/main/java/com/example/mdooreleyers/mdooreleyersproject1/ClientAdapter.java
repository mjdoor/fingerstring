package com.example.mdooreleyers.mdooreleyersproject1;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public CardView clientCard;
        public TextView clientNameBox;
        public TextView clientPhoneBox;
        public OnClientClickListener onClientClickListener;

        public ViewHolder(View itemView, OnClientClickListener onClientClickListener) {
            super(itemView);

            clientNameBox = (TextView) itemView.findViewById(R.id.clientNameTxt);
            clientPhoneBox = (TextView) itemView.findViewById(R.id.clientPhoneTxt);
            clientCard = (CardView)itemView.findViewById(R.id.clientCard);

            itemView.setOnClickListener(this);
            this.onClientClickListener = onClientClickListener;
        }

        @Override
        public void onClick(View view) {
            onClientClickListener.onClientClick(getAdapterPosition());
        }
    }

    // Listener for client click
    private OnClientClickListener onClientClickListener;

    private int selectedPosition;

    // Store clients in a member variable
    private List<Client> clients;
    Context context;

    public ClientAdapter(List<Client> clnts, OnClientClickListener onClientClickListener)
    {
        this.clients = clnts;
        this.onClientClickListener = onClientClickListener;
        this.selectedPosition = -1;
    }

    @Override
    public ClientAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Define what layout to use ("inflate") for each item in the recycler
        View clientItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.client_item, parent, false);
        context = parent.getContext();
        return new ViewHolder(clientItem, this.onClientClickListener);
    }

    @Override
    public void onBindViewHolder(final ClientAdapter.ViewHolder viewHolder, final int position) {
        final Client client = clients.get(position);

        // set values for appointment
        viewHolder.clientNameBox.setText(client.getFullName());
        viewHolder.clientPhoneBox.setText(client.getPhoneNumber());

        if(selectedPosition == position)
        {
            viewHolder.clientCard.setCardBackgroundColor(Color.rgb(169, 223, 247));
        }
        else
        {
            viewHolder.clientCard.setCardBackgroundColor(Color.WHITE);
        }
    }

    @Override
    public int getItemCount() {
        return clients.size();
    }

    public void changeData(List<Client> newList)
    {
        this.clients = newList;
        notifyDataSetChanged();
    }

    // get the client id for a client at a provided position
    public long getClientID(int position)
    {
        return clients.get(position).getClientID();
    }

    public void setSelectedColour(int position)
    {
        selectedPosition = position;
        notifyDataSetChanged();
    }

    public int setSelectedClient(long clientID)
    {
        // get position of this client
        int position = -1;
        for(int i = 0; i < clients.size(); i++)
        {
            if(clients.get(i).getClientID() == clientID)
            {
                position = i;
                break;
            }
        }

        setSelectedColour(position);
        return position;
    }


    // interface to allow for on click events for items in the recycler
    public interface OnClientClickListener
    {
        void onClientClick(int position);
    }

}
