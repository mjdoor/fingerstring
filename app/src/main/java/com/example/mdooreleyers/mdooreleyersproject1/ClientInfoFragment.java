package com.example.mdooreleyers.mdooreleyersproject1;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class ClientInfoFragment extends Fragment implements ClientAdapter.OnClientClickListener {

    private RecyclerView clientRecycler;
    private ClientAdapter clientAdapter;
    private long selectedClientID;
    private boolean creatingNewClient;

    private TextView firstNameTxt;
    private TextView lastNameTxt;
    private TextView phoneNumberTxt;

    // buttons to grey out either option (add new or choose client)
    private Button clientSelectionBtn;
    private ConstraintLayout chooseExistingLayout;
    private LinearLayout addNewLayout;

    private TextView chooseClientMessage;
    private LinearLayout buttonLayout;

    private InflaterListener listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.client_info_layout, container, false);

        clientRecycler = (RecyclerView)view.findViewById(R.id.clientSelectionRecycler);
        clientRecycler.setHasFixedSize(true);
        firstNameTxt = (TextView)view.findViewById(R.id.newFirstNameTxt);
        lastNameTxt = (TextView)view.findViewById(R.id.newLastNameTxt);
        phoneNumberTxt = (TextView)view.findViewById(R.id.newPhoneTxt);

        clientSelectionBtn = (Button)view.findViewById(R.id.clientPageBtn);
        chooseExistingLayout = (ConstraintLayout)view.findViewById(R.id.chooseExistingLayout);
        addNewLayout = (LinearLayout)view.findViewById(R.id.addNewLayout);

        chooseClientMessage = (TextView)view.findViewById(R.id.clientPageMessageTxt);
        buttonLayout = (LinearLayout)view.findViewById(R.id.chooseBtnLayout);

        // to start, assume we're creating a new client, so the grey button over create new client will be invisible
        displayForCreatingClient();

        clientSelectionBtn.setOnClickListener((ev)->
        {
            displayForClientSelectionChoice();
        });

        this.listener.onClientInfoFragCreated();

        return view;
    }

    public String getFirstName() {
        return firstNameTxt.getText().toString();
    }

    public String getLastName() {
        return lastNameTxt.getText().toString();
    }

    public String getPhoneNumber() {
        return phoneNumberTxt.getText().toString();
    }

    public void setFirstName(String firstName)
    {
        firstNameTxt.setText(firstName);
    }

    public void setLastName(String firstName)
    {
        lastNameTxt.setText(firstName);
    }

    public void setPhoneNumber(String phoneNumber)
    {
        phoneNumberTxt.setText(phoneNumber);
    }

    public void setListener(InflaterListener listener)
    {
        this.listener = listener;
    }

    public void setupClientRecycler(List<Client> clients)
    {
        if(clients.size() == 0)
        {
            displayNoClients();
        }
        else
        {
            // Create the adapter with the client info
            this.clientAdapter = new ClientAdapter(clients, this);
            // Set the adapter for the recycler
            clientRecycler.setAdapter(clientAdapter);
            // Set layout manager
            clientRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        }
    }

    @Override
    public void onClientClick(int position) {
        this.selectedClientID = clientAdapter.getClientID(position);
        clientAdapter.setSelectedColour(position);
    }

    public boolean isCreatingNewClient()
    {
        return this.creatingNewClient;
    }

    public void setSelectedClient(long clientID)
    {
        this.selectedClientID = clientID;
        int position = clientAdapter.setSelectedClient(clientID);
        clientRecycler.getLayoutManager().scrollToPosition(position);
        displayForChoosingClient();
    }

    public long getSelectedClientID()
    {
        return this.selectedClientID;
    }

    private void displayForClientSelectionChoice()
    {
        if(this.creatingNewClient)
        {
            displayForChoosingClient();
        }
        else
        {
            displayForCreatingClient();
        }
    }


    private void displayForCreatingClient()
    {
        creatingNewClient = true;

        chooseClientMessage.setText(R.string.choose_client_instead);
        clientSelectionBtn.setText(R.string.choose_client_instead_btn);

        TransitionSet set2 = new TransitionSet()
            .addTransition(new Slide(Gravity.BOTTOM))
            .addTransition(new Fade())
            .setInterpolator(new LinearOutSlowInInterpolator())
            .setDuration(500);

        TransitionManager.beginDelayedTransition(chooseExistingLayout, set2);
        chooseExistingLayout.setVisibility(View.INVISIBLE);

        TransitionSet set1 = new TransitionSet()
                .addTransition(new Slide(Gravity.TOP))
                .addTransition(new Fade())
                .setInterpolator(new LinearOutSlowInInterpolator())
                .setStartDelay(400)
                .setDuration(500);

        TransitionManager.beginDelayedTransition(addNewLayout, set1);
        addNewLayout.setVisibility(View.VISIBLE);
    }

    private void displayForChoosingClient()
    {
        creatingNewClient = false;

        chooseClientMessage.setText(R.string.add_client_instead);
        clientSelectionBtn.setText(R.string.add_client_instead_btn);

        TransitionSet set1 = new TransitionSet()
                .addTransition(new Slide(Gravity.BOTTOM))
                .addTransition(new Fade())
                .setInterpolator(new LinearOutSlowInInterpolator())
                .setDuration(500);

        TransitionManager.beginDelayedTransition(addNewLayout, set1);
        addNewLayout.setVisibility(View.INVISIBLE);

        TransitionSet set2 = new TransitionSet()
                .addTransition(new Slide(Gravity.TOP))
                .addTransition(new Fade())
                .setInterpolator(new LinearOutSlowInInterpolator())
                .setStartDelay(400)
                .setDuration(500);

        TransitionManager.beginDelayedTransition(chooseExistingLayout, set2);
        chooseExistingLayout.setVisibility(View.VISIBLE);
    }

    private void displayNoClients()
    {
        creatingNewClient = true;
        buttonLayout.setVisibility(View.INVISIBLE);
        chooseExistingLayout.setVisibility(View.INVISIBLE);
    }
}
