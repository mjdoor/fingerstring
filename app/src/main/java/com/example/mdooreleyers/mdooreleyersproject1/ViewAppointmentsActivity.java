package com.example.mdooreleyers.mdooreleyersproject1;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ViewAppointmentsActivity extends AppCompatActivity implements AppointmentCancellationListener {

    CalendarView calendarView;
    TextView headingBox;
    List<Appointment> allAppointments;
    List<Appointment> selectedAppointments;
    RecyclerView allAppointmentsRecycler;
    AppointmentAdapter appointmentAdapter;

    Calendar selectedDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_appointments);

        headingBox = (TextView)findViewById(R.id.viewAppointmentDateTxt);
        allAppointmentsRecycler = (RecyclerView)findViewById(R.id.allAppointmentsRecycler);

        selectedDay = Calendar.getInstance();
        setAllAppointments(); // all saved appointments
        setSelectedDayAppointments();

        calendarView = findViewById(R.id.viewAppointmentsCalendar);
        calendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                selectedDay = eventDay.getCalendar();
                setSelectedDayAppointments();
                appointmentAdapter.changeData(selectedAppointments);
                setHeadingBox();
            }
        });
        setupCalendarIcons();

        // Create the adapter with the appointment info
        appointmentAdapter = new AppointmentAdapter(selectedAppointments);
        appointmentAdapter.setCancellationListener(this);
        // Set the adapter for the recycler
        allAppointmentsRecycler.setAdapter(appointmentAdapter);
        // Set layout manager
        allAppointmentsRecycler.setLayoutManager(new LinearLayoutManager(this));

        setHeadingBox();
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences settings = getSharedPreferences("fingerstring", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("viewAppointmentSelectedDay", selectedDay.getTimeInMillis()); // represents today
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences settings = getSharedPreferences("fingerstring", Context.MODE_PRIVATE);
        // When this activity is opened with onCreate, we want the selected date to be today.
        // This is dealt with in the getSelectedDayAppointments method when this shared preference is used to find the selected day - if this preference isn't found, today is used.
        // So, this preference needs to be deleted during onDestroy
        settings.edit().remove("viewAppointmentSelectedDay").apply();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences settings = getSharedPreferences("fingerstring", Context.MODE_PRIVATE);
        selectedDay.setTimeInMillis(settings.getLong("viewAppointmentSelectedDay", selectedDay.getTimeInMillis())); // defaults to the current selected today, which should be today, as defined in onCreate
        try
        {
            calendarView.setDate(selectedDay);
        }
        catch(com.applandeo.materialcalendarview.exceptions.OutOfDateRangeException ex) {
            // need the catch, do nothing
        }

        // update allAppointments and calendar icons in case of reschedule
        setAllAppointments();
        setupCalendarIcons();
        //update selectedAppointment data incase an appointment was rescheduled
        setSelectedDayAppointments();
        appointmentAdapter.changeData(selectedAppointments);
        setHeadingBox();
    }

    @Override
    public void OnAppointmentCancelled() {
        // read in all appointments again
        setAllAppointments();
        setupCalendarIcons();
        setHeadingBox();
    }

    private List<Appointment> getAllAppointments()
    {
        return AppointmentDatabase.getInstance(this).appointmentDAO().getAll();
    }

    private void setAllAppointments()
    {
        allAppointments = getAllAppointments();
    }

    private void setSelectedDayAppointments()
    {
        Calendar cal = (Calendar)selectedDay.clone();//calendar.getSelectedDate();
        // Get time for start of selected day
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        long startOfDay = cal.getTimeInMillis();
        // Move forward 24 hours for end of day
        cal.setTimeInMillis(startOfDay + TimeConstants.MILLISECONDS_PER_DAY);
        long endOfDay = cal.getTimeInMillis();

        selectedAppointments = AppointmentDatabase.getInstance(this).appointmentDAO().getUpcoming(startOfDay, endOfDay);
    }

    // Sets icons on calendar for days with appointments
    private void setupCalendarIcons()
    {
        // Need to generate a list of Calendar objects to set selected dates on calendarView
        Set<Calendar> uniqDays = new HashSet<Calendar>();
        for(int i = 0; i < allAppointments.size(); i++)
        {
            // get the date of the appointment
            long aptDate = allAppointments.get(i).getStartTime();
            Calendar dayCal = Calendar.getInstance();
            dayCal.setTimeInMillis(aptDate);
            // set time to midnight, for consistency between all appointment days
            dayCal.set(Calendar.HOUR_OF_DAY, 0);
            dayCal.set(Calendar.MINUTE, 0);
            uniqDays.add(dayCal);
        }

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        long todayTime = today.getTimeInMillis();

        List<EventDay> appointmentDays = new ArrayList<EventDay>();
        for(Iterator<Calendar> it = uniqDays.iterator(); it.hasNext(); /*nothing*/)
        {
            Calendar cal = it.next(); //advances the loop
            appointmentDays.add(new EventDay(cal, (cal.getTimeInMillis() < todayTime ? R.drawable.appointment_icon_past : R.drawable.appointment_icon))); //sets different icon for old appointments vs present/future appointments
        };
        calendarView.setEvents(appointmentDays);
    }

    private void setHeadingBox()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat(TimeConstants.datePattern);
        String lead = appointmentAdapter.getItemCount() > 0 ? getResources().getString(R.string.view_day_appointments) : getResources().getString(R.string.view_day_appointments_none);
        lead += " ";
        headingBox.setText(lead + dateFormat.format(new Date(selectedDay.getTimeInMillis())));
    }

    public void backClick(View view) {
        finish();
    }
}
