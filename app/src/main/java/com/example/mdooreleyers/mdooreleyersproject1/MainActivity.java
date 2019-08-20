package com.example.mdooreleyers.mdooreleyersproject1;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AppointmentCancellationListener {

    List<Appointment> appointments;
    RecyclerView appointmentsRecycler;
    TextView upcomingBox;

    String TAG = "OKIE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupPermissions();
        appointmentsRecycler = (RecyclerView)findViewById(R.id.appointmentsRecycler);
        upcomingBox = (TextView)findViewById(R.id.mainUpcomingTxt);
    }



    @Override
    protected void onResume() {
        super.onResume();

        // Get today's appointment data
        appointments = getUpcomingAppointments();
        // Create the adapter with the appointment info
        AppointmentAdapter appointmentAdapter = new AppointmentAdapter(appointments);
        appointmentAdapter.setCancellationListener(this);
        // Set the adapter for the recycler
        appointmentsRecycler.setAdapter(appointmentAdapter);
        // Set layout manager
        appointmentsRecycler.setLayoutManager(new LinearLayoutManager(this));

        // set text of upcoming appointments label
        setUpcomingLabel();

        // calling to setup auto texter in onResume so that it will update in the case of updating the settings, as well as when the app is opened
        setupAutoTexter();
    }

    @Override
    public void OnAppointmentCancelled() {
        setUpcomingLabel();
    }

    private List<Appointment> getUpcomingAppointments() {
        Calendar calUtil = Calendar.getInstance();
        int previewLengthInDays = SharedPreferenceUtility.getIntPreference(this, "settingUpcomingCutoff", getResources().getInteger(R.integer.setting_upcoming_cutoff_default));
        long previewLengthInHours = previewLengthInDays * TimeConstants.MILLISECONDS_PER_DAY;
        long now = calUtil.getTimeInMillis();
        long cutOff = now + previewLengthInHours;

        // will get all appointments occurring within the specified preview time
        return AppointmentDatabase.getInstance(this).appointmentDAO().getUpcoming(now, cutOff);
    }

    public void newAppointmentClick(View view) {
        //Opening a new window (activity)
        Intent intent = new Intent(this, AddAppointmentActivity.class);
        intent.putExtra("request_type", "new");
        startActivity(intent);
    }

    public void viewAppointmentsClick(View view) {
        Intent intent = new Intent(this, ViewAppointmentsActivity.class);
        startActivity(intent);
    }

    private void setupPermissions()
    {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS},1);
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE},2);
        }

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_PHONE_STATE},3);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1: // SEND_SMS code
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // if texting permission is granted, ask for calling permission
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
                    {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE},2);
                    }
                }
                else
                {
                    popPermissionsErrorToast();
                }
                break;
            case 2: // CALL_PHONE code
                // ask for phone state permissions if necessary
                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_PHONE_STATE},3);
                }
                break;
            case 3: // READ_PHONE_STATE code
                if(grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED)
                {
                    popPermissionsErrorToast();
                }
                break;
        }
    }

    private void popPermissionsErrorToast()
    {
        int duration = 6; // seconds
        Toast.makeText(this, R.string.permissions_warning, duration*(int)TimeConstants.MILLISECONDS_PER_SECOND).show();
    }

    private void setupAutoTexter()
    {
        AutomatedTexter.createAlarmForAutoText(this, Calendar.getInstance().getTimeInMillis());
    }

    private void setUpcomingLabel()
    {
        String labelText="";
        if(appointmentsRecycler.getAdapter().getItemCount() > 0)
        {
            labelText += getString(R.string.upcoming_appointments);

        }
        else
        {
            labelText += getString(R.string.no_upcoming_appointments);
        }

        int upcomingCutoffDays = SharedPreferenceUtility.getIntPreference(this, "settingUpcomingCutoff", getResources().getInteger(R.integer.setting_upcoming_cutoff_default));
        int upcomingCutoffHours = upcomingCutoffDays * 24; // 24 hours per day
        labelText += " (within the next " + upcomingCutoffHours + " hours)";
        upcomingBox.setText(labelText);
    }


    public void aboutClick(View view) {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    public void settingsClick(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
