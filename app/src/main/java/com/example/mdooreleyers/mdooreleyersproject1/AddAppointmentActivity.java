package com.example.mdooreleyers.mdooreleyersproject1;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.CalendarView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AddAppointmentActivity extends AppCompatActivity implements InflaterListener {

    TextView headingBox;

    private NewAppointmentPageAdapter newAppointmentPageAdapter;
    private ViewPager viewPager;

    private ClientInfoFragment clientInfoFragment;
    private NewAppointmentFragment appointmentInfoFragment;

    private Appointment appointment;
    private Client client;

    private Button scheduleBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_appointment);

        headingBox = (TextView)findViewById(R.id.bookAppointmentHeading);
        if(getIntent().getExtras().getString("request_type").equals("update"))
        {
            headingBox.setText(R.string.reschedule_appointment);
        }
        else
        {
            headingBox.setText(R.string.add_appointment);
        }

        // Set up tab layout, for client info and appointment time info
        newAppointmentPageAdapter = new NewAppointmentPageAdapter(getSupportFragmentManager());
        viewPager = (ViewPager)findViewById(R.id.newAppointmentViewPager);
        setupViewPager(viewPager, newAppointmentPageAdapter);

        TabLayout tabLayout = (TabLayout)findViewById(R.id.infoTabs);
        tabLayout.setupWithViewPager(viewPager);
        // If we are rescheduling an appointment, have the opening tab be the Appointment Info tab, (index 1)
        if(getIntent().getExtras().getString("request_type").equals("update"))
        {
            tabLayout.getTabAt(1).select();
        }

        // Set up fragments, for access to input field views
        clientInfoFragment = (ClientInfoFragment)newAppointmentPageAdapter.getItem(0);
        appointmentInfoFragment = (NewAppointmentFragment)newAppointmentPageAdapter.getItem(1);

        scheduleBtn = (Button)findViewById(R.id.createAppointmentBtn);

        // Check intent to see if we are creating or updating an appointment
        switch(getIntent().getExtras().getString("request_type"))
        {
            case "new": // adding a new appointment
                scheduleBtn.setOnClickListener(new Button.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        createAppointment();
                    }
                });
                break;
            case "update":
                int aptID = getIntent().getExtras().getInt("aptID");
                this.appointment = AppointmentDatabase.getInstance(this).appointmentDAO().getAppointmentById(aptID);
                this.client = AppointmentDatabase.getInstance(this).clientDAO().getClientByID(this.appointment.getClientID());
                scheduleBtn.setOnClickListener(new Button.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        updateAppointment();
                    }
                });
                break;
        }
    }

    @Override
    public void onClientInfoFragCreated() {
        // Get all clients
        List<Client> allClients = AppointmentDatabase.getInstance(this).clientDAO().getAll();
        // populate the recycler here
        clientInfoFragment.setupClientRecycler(allClients);

        if(getIntent().getExtras().getString("request_type").equals("update"))
        {
            clientInfoFragment.setSelectedClient(this.client.getClientID());
        }
    }

    @Override
    public void onAppointmentInfoFragCreated() {
        if(getIntent().getExtras().getString("request_type").equals("update"))
        {
            appointmentInfoFragment.setViewsWithDate(this.appointment.getStartTime(), this.appointment.getDuration());
        }
        else
        {
            // set default ampm to PM
            int defaultAmPmPosition = 1; // AmPm: [ AM, PM ]
            appointmentInfoFragment.setAmPm(defaultAmPmPosition);

            // set default duration to 60 minutes
            int defaultDurationPosition = 2;  // Durations: [ 30, 45, 60, 75, 90, 120]
            appointmentInfoFragment.setDuration(defaultDurationPosition);
        }
    }

    private void setupViewPager(ViewPager pager, NewAppointmentPageAdapter adapter)
    {
        ClientInfoFragment cIF = new ClientInfoFragment();
        cIF.setListener(this);
        NewAppointmentFragment nAF = new NewAppointmentFragment();
        nAF.setListener(this);
        adapter.addFragment(cIF, getString(R.string.client_info));
        adapter.addFragment(nAF, getString(R.string.appointment_info));
        pager.setAdapter(adapter);
    }


    private void createAppointment() {
        int duration = appointmentInfoFragment.getDuration();
        long dateTime = appointmentInfoFragment.getDateTime();

        // setup transaction for adding client and appointment. using lambda function to represent new Runnable, which runIntransaction uses
        AppointmentDatabase.getInstance(this).beginTransaction();
        //transaction block
        {
            long clientID=0;
            if(clientInfoFragment.isCreatingNewClient())
            {
                String firstName = clientInfoFragment.getFirstName();
                String lastName = clientInfoFragment.getLastName();
                String phoneNumber = clientInfoFragment.getPhoneNumber();

                Client clnt = new Client(firstName, lastName, phoneNumber);
                clientID = AppointmentDatabase.getInstance(this).clientDAO().addClient(clnt);
            }
            else
            {
                clientID = clientInfoFragment.getSelectedClientID();
            }

            Appointment apt = new Appointment(clientID, dateTime, duration);

            // Book appointment. CHECK FOR CONFLICTS FIRST
            AppointmentDAO aptDAO = AppointmentDatabase.getInstance(this).appointmentDAO();
            Appointment conflictingAppointment = aptDAO.checkConflicts(0, apt.getStartTime(), apt.getEndTime());
            if(conflictingAppointment != null)
            {
                popConflictingAppointmentToast(conflictingAppointment);
                AppointmentDatabase.getInstance(this).endTransaction();
            }
            else
            {
                aptDAO.bookAppointment(apt);
                AppointmentDatabase.getInstance(this).setTransactionSuccessful();
                AppointmentDatabase.getInstance(this).endTransaction();

                Toast.makeText(getApplicationContext(), "Appointment made for " + AppointmentDatabase.getInstance(this).clientDAO().getClientByID(clientID).getFullName(), Toast.LENGTH_LONG).show();

                finish();
            }
        }
        // end transaction block
    }

    private void updateAppointment()
    {
        if(this.appointment == null)
        {
            Toast.makeText(this, R.string.updating_error, Toast.LENGTH_SHORT).show();
            finish();
        }

        this.appointment.setStartTime(appointmentInfoFragment.getDateTime());
        this.appointment.setDuration(appointmentInfoFragment.getDuration());

        String originalReminderStatus = this.appointment.getReminderStatus();

        // Update appointment, checking for conflicts first
        AppointmentDAO aptDAO = AppointmentDatabase.getInstance(this).appointmentDAO();
        Appointment conflictingAppointment = aptDAO.checkConflicts(this.appointment.aptID, this.appointment.getStartTime(), this.appointment.getEndTime());
        if(conflictingAppointment != null)
        {
            popConflictingAppointmentToast(conflictingAppointment);
        }
        else
        {
            AppointmentDatabase.getInstance(this).beginTransaction();
            // transaction block
            {
                if(clientInfoFragment.isCreatingNewClient())
                {
                    String firstName = clientInfoFragment.getFirstName();
                    String lastName = clientInfoFragment.getLastName();
                    String phoneNumber = clientInfoFragment.getPhoneNumber();

                    Client clnt = new Client(firstName, lastName, phoneNumber);
                    long newClientID = AppointmentDatabase.getInstance(this).clientDAO().addClient(clnt);
                    this.appointment.setClientID(newClientID);
                }
                else
                {
                    this.appointment.setClientID(clientInfoFragment.getSelectedClientID());
                }
                AppointmentDatabase.getInstance(this).appointmentDAO().updateAppointment(this.appointment);

                // We want to change the reminder sent status for the appointment if the reminder has already been sent (or attempted to be sent), and if the new time of the appointment is after
                // the end of day tomorrow. Any earlier, and we'll assume the client doesn't need another reminder.
                long endOfDayTomorrow = TimeConstants.calcEndOfNextDay(Calendar.getInstance().getTimeInMillis());
                if(!originalReminderStatus.equals(Appointment.ReminderStatus.NOT_SENT.toString()) && this.appointment.getStartTime() >= endOfDayTomorrow)
                {
                    this.appointment.setReminderStatus(Appointment.ReminderStatus.NOT_SENT.toString());
                }

                AppointmentDatabase.getInstance(this).setTransactionSuccessful();
                AppointmentDatabase.getInstance(this).endTransaction();

                Toast.makeText(getApplicationContext(), "Appointment rescheduled for " + AppointmentDatabase.getInstance(this).clientDAO().getClientByID(this.appointment.getClientID()).getFullName(), Toast.LENGTH_LONG).show();

                finish();
            } // end transaction block
        }
    }

    private void popConflictingAppointmentToast(Appointment conflictingAppointment)
    {
        Client conflictingClient  = AppointmentDatabase.getInstance(this).clientDAO().getClientByID(conflictingAppointment.getClientID());
        String message = getResources().getString(R.string.conflicting_appointment_message) + "\n" + conflictingClient.getFullName() + ", " + conflictingAppointment.getTimeSpan();
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    public void backClick(View view) {
        Toast.makeText(getApplicationContext(), R.string.back_toast_message, Toast.LENGTH_SHORT).show();
        finish();
    }

}