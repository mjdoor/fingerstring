package com.example.mdooreleyers.mdooreleyersproject1;

import android.net.sip.SipSession;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ClientInfoFragment extends Fragment {

    private TextView firstNameTxt;
    private TextView lastNameTxt;
    private TextView phoneNumberTxt;

    private InflaterListener listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.client_info_layout, container, false);

        firstNameTxt = (TextView)view.findViewById(R.id.newFirstNameTxt);
        lastNameTxt = (TextView)view.findViewById(R.id.newLastNameTxt);
        phoneNumberTxt = (TextView)view.findViewById(R.id.newPhoneTxt);

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

}
