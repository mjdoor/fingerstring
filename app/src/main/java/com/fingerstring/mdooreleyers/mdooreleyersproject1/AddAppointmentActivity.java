package com.fingerstring.mdooreleyers.mdooreleyersproject1;

import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;

public class AddAppointmentActivity extends AppCompatActivity implements InflaterListener, TabChangeListener {

    TextView headingBox;

    private NewAppointmentPageAdapter newAppointmentPageAdapter;
    private ViewPager viewPager;

    private ClientInfoFragment clientInfoFragment;
    private NewAppointmentFragment appointmentInfoFragment;

    private Appointment appointment;
    private Client client;

    private Button scheduleBtn;

    private TabLayout tabLayout;

    private LinearLayout btnLayout;

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

        btnLayout = (LinearLayout)findViewById(R.id.addAppointmentBtnLayout);

        // Set up tab layout, for client info and appointment time info
        newAppointmentPageAdapter = new NewAppointmentPageAdapter(getSupportFragmentManager());
        viewPager = (ViewPager)findViewById(R.id.newAppointmentViewPager);
        setupViewPager(viewPager, newAppointmentPageAdapter);

        tabLayout = (TabLayout)findViewById(R.id.infoTabs);
        tabLayout.setupWithViewPager(viewPager);
        // If we are rescheduling an appointment, have the opening tab be the Appointment Info tab, (index 1)
        if(getIntent().getExtras().getString("request_type").equals("update"))
        {
            tabLayout.getTabAt(1).select();
            setBtnLayoutVisibility(View.VISIBLE);
        }
        else
        {
            setBtnLayoutVisibility(View.INVISIBLE);
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //viewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition() == 0)// Client info - don't show Booking buttons
                {
                    setBtnLayoutVisibility(View.INVISIBLE);
                }
                else // Appointment info - show Booking buttons
                {
                    setBtnLayoutVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // Set up fragments, for access to input field views
        clientInfoFragment = (ClientInfoFragment)newAppointmentPageAdapter.getItem(0);
        appointmentInfoFragment = (NewAppointmentFragment)newAppointmentPageAdapter.getItem(1);

        scheduleBtn = (Button)findViewById(R.id.createAppointmentBtn);

        // Check intent to see if we are creating or updating an appointment
        switch(getIntent().getExtras().getString("request_type")) {
            case "new": // adding a new appointment
                scheduleBtn.setOnClickListener(new Button.OnClickListener() {
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
                scheduleBtn.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        updateAppointment();
                    }
                });
                break;
        }

        //Check if appointment type is set, if not, go to settings
        if(SharedPreferenceUtility.getStringPreference(this, "settingAppointmentType", getString(R.string.setting_appointment_type_default)).equals(""))
        {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra("need_appointment_type", true);
            Toast.makeText(this, R.string.need_appointment_type_message, Toast.LENGTH_LONG).show();
            startActivity(intent);
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
        else {
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
        cIF.setTabChangeListener(this);
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
                boolean disableReminders = clientInfoFragment.getDisableReminders();

                if(firstName.isEmpty() || lastName.isEmpty() || phoneNumber.isEmpty())
                {
                    Toast.makeText(getApplicationContext(), "Please enter your client's first and last name, and phone number.", Toast.LENGTH_SHORT).show();
                    tabLayout.getTabAt(0).select();

                    return;
                }

                Client clnt = new Client(firstName, lastName, phoneNumber, disableReminders);

                try
                {
                    clientID = AppointmentDatabase.getInstance(this).clientDAO().addClient(clnt);
                }
                catch(SQLiteConstraintException ex) // will throw an error if a new client is attempted to be created with the info as an existing client
                {
                    AppointmentDatabase.getInstance(this).endTransaction();
                    Toast.makeText(getApplicationContext(), R.string.unique_constraint_violation, Toast.LENGTH_LONG).show();
                    tabLayout.getTabAt(0).select();

                    return;
                }
            }
            else
            {
                clientID = clientInfoFragment.getSelectedClientID();

                if(clientID == -1)
                {
                    Toast.makeText(getApplicationContext(), "Please select a client from the list, or enter information for a new client.", Toast.LENGTH_LONG).show();
                    tabLayout.getTabAt(0).select();

                    return;
                }
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
                    boolean disableReminders = clientInfoFragment.getDisableReminders();

                    if(firstName.isEmpty() || lastName.isEmpty() || phoneNumber.isEmpty())
                    {
                        Toast.makeText(getApplicationContext(), "Please enter your client's first and last names, and phone number.", Toast.LENGTH_SHORT).show();
                        tabLayout.getTabAt(0).select();

                        return;
                    }

                    Client clnt = new Client(firstName, lastName, phoneNumber, disableReminders);
                    try
                    {
                        long newClientID = AppointmentDatabase.getInstance(this).clientDAO().addClient(clnt);
                        this.appointment.setClientID(newClientID);
                    }
                    catch(SQLiteConstraintException ex) // will throw an error if a new client is attempted to be created with the same phone number as an existing client
                    {
                        AppointmentDatabase.getInstance(this).endTransaction();
                        Toast.makeText(getApplicationContext(), R.string.unique_constraint_violation, Toast.LENGTH_LONG).show();
                        tabLayout.getTabAt(0).select();

                        return;
                    }
                }
                else
                {
                    long clientID = clientInfoFragment.getSelectedClientID();

                    if(clientID == -1)
                    {
                        Toast.makeText(getApplicationContext(), "Please select a client from the list, or enter information for a new client.", Toast.LENGTH_LONG).show();
                        tabLayout.getTabAt(0).select();

                        return;
                    }

                    this.appointment.setClientID(clientID);
                }

                // We want to change the reminder sent status for the appointment if the reminder has already been sent (or attempted to be sent), and if the new time of the appointment is after
                // the end of day tomorrow. Any earlier, and we'll assume the client doesn't need another reminder.
                long endOfDayTomorrow = TimeConstants.calcEndOfNextDay(Calendar.getInstance().getTimeInMillis());
                if(!originalReminderStatus.equals(Appointment.ReminderStatus.NOT_SENT.toString()) && this.appointment.getStartTime() >= endOfDayTomorrow)
                {
                    this.appointment.setReminderStatus(Appointment.ReminderStatus.NOT_SENT.toString());
                }
                AppointmentDatabase.getInstance(this).appointmentDAO().updateAppointment(this.appointment);

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

    private void setBtnLayoutVisibility(int vis)
    {
        TransitionSet set = new TransitionSet()
                .addTransition(new Slide(Gravity.RIGHT))
                .addTransition(new Fade())
                .setInterpolator(new FastOutLinearInInterpolator())
                .setDuration(50);

        TransitionManager.beginDelayedTransition(btnLayout, set);
        btnLayout.setVisibility(vis);
    }

    @Override
    public void changeTabTo(int tabNum) {
        tabLayout.getTabAt(tabNum).select();
    }
}