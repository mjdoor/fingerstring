package com.example.mdooreleyers.mdooreleyersproject1;

import android.content.DialogInterface;
import android.database.sqlite.SQLiteConstraintException;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditClientActivity extends AppCompatActivity {
    private Button updateBtn;
    private Button deleteBtn;
    private EditText firstNameBox;
    private EditText lastNameBox;
    private EditText phoneBox;

    private Client client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_client);

        updateBtn = (Button)findViewById(R.id.updateClientBtn);
        deleteBtn = (Button)findViewById(R.id.deleteClientBtn);
        firstNameBox = (EditText)findViewById(R.id.firstNameTxt);
        lastNameBox = (EditText)findViewById(R.id.lastNameTxt);
        phoneBox = (EditText)findViewById(R.id.phoneTxt);

        getClient(); // sets this.client to a client with values from the intent
        fillFields();

        // set listeners for buttons
        updateBtn.setOnClickListener((ev)->
        {
            updateClient();
        });

        deleteBtn.setOnClickListener((ev)->
        {
            deleteClient();
        });

    }

    private void getClient()
    {
        long id = getIntent().getExtras().getLong("client_id");

        this.client = AppointmentDatabase.getInstance(this).clientDAO().getClientByID(id);
    }

    private void fillFields()
    {
        firstNameBox.setText(this.client.getFirstName());
        lastNameBox.setText(this.client.getLastName());
        phoneBox.setText(this.client.getPhoneNumber());
    }

    private void updateClient()
    {
        this.client.setFirstName(firstNameBox.getText().toString());
        this.client.setLastName(lastNameBox.getText().toString());
        this.client.setPhoneNumber(phoneBox.getText().toString());

        // checking with unique constraint on phone number
        try
        {
            AppointmentDatabase.getInstance(this).clientDAO().updateClient(this.client);
        }
        catch(SQLiteConstraintException ex) // will throw an error if a new client is attempted to be created with the same phone number as an existing client
        {
            Toast.makeText(getApplicationContext(), "The phone number you've supplied for this client is already in use by another client. Please double check the phone number before continuing.", Toast.LENGTH_LONG).show();

            return;
        }
        Toast.makeText(getApplicationContext(), client.getFullName() + " successfully updated", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void deleteClient()
    {
        new AlertDialog.Builder(this) // set up confirmation box for deleting client
                .setTitle(R.string.confirm_client_delete)
                .setMessage("Are you sure you want to delete " + this.client.getFullName() + " as a client? This will also remove all of " + this.client.getFirstName() + "'s appointments.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int btn) {
                        try
                        {
                            AppointmentDatabase.getInstance(getApplicationContext()).clientDAO().deleteClient(client);
                            Toast.makeText(getApplicationContext(), client.getFullName() + " successfully deleted as a client", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        catch(Exception e)
                        {
                            Toast.makeText(getApplicationContext(), R.string.deleting_client_error, Toast.LENGTH_SHORT).show();
                        }
                    }})
                .setNegativeButton(R.string.deny, null).show();
    }




    public void backClick(View view) {
        finish();
    }
}
