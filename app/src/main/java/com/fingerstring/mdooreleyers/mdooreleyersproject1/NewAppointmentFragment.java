package com.fingerstring.mdooreleyers.mdooreleyersproject1;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class NewAppointmentFragment extends Fragment {
    private Spinner durationSpinner;
    private Spinner hourSpinner;
    private Spinner minuteSpinner;
    private Spinner ampmSpinner;
    private CalendarView calendar;

    private CoordinatorLayout snackbarHolder;
    private Snackbar snackbar;

    private InflaterListener listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.new_appointment_layout, container, false);

        snackbarHolder = (CoordinatorLayout)view.findViewById(R.id.snackbarHolder);
        snackbar = Snackbar.make(snackbarHolder, "", 5000);

        durationSpinner = (Spinner) view.findViewById(R.id.durationSpinner);
        calendar = (CalendarView) view.findViewById(R.id.newAppointmentCal);
        hourSpinner = (Spinner) view.findViewById(R.id.hourSpinner);
        minuteSpinner = (Spinner) view.findViewById(R.id.minuteSpinner);
        ampmSpinner = (Spinner) view.findViewById(R.id.ampmSpinner);

        calendar.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay selDay) {
                try
                {
                    calendar.setDate(selDay.getCalendar());
                    setSelectedDayAppointments(selDay.getCalendar());
                }
                catch (com.applandeo.materialcalendarview.exceptions.OutOfDateRangeException ex)
                {
                    // Need to catch, do nothing
                }
            }
        });

        setupCalendarIcons();

        this.listener.onAppointmentInfoFragCreated();

        return view;
    }

    public int getDuration() {
        return Integer.parseInt(durationSpinner.getSelectedItem().toString());
    }

    public long getDateTime() {
        int hour = Integer.parseInt(hourSpinner.getSelectedItem().toString());
        if(hour == 12)
        {
            hour = 0;
        }
        int minute = Integer.parseInt(minuteSpinner.getSelectedItem().toString());
        int ampm = ampmSpinner.getSelectedItem().toString().equalsIgnoreCase("AM") ? 0 : 1;

        Calendar calUtil = calendar.getSelectedDates().get(0);
        calUtil.set(Calendar.HOUR, hour);
        calUtil.set(Calendar.MINUTE, minute);
        calUtil.set(Calendar.SECOND, 0);
        calUtil.set(Calendar.MILLISECOND, 0);
        calUtil.set(Calendar.AM_PM, ampm);

        return calUtil.getTimeInMillis();
    }

    // Sets all spinners to their appropriate values, given a date and duration of an existing appointment
    public void setViewsWithDate(long date, int duration)
    {
        Calendar calUtil = Calendar.getInstance();
        calUtil.setTimeInMillis(date);

        int hour = calUtil.get(Calendar.HOUR); //Calendar.HOUR returns 0 for hour 12
        hourSpinner.setSelection(hour);

        String[] minutesArray = getResources().getStringArray(R.array.minutes);
        int minutePosition = 0;
        // given what minute on the hour the appointment is at, we need to set the minute spinner to that value. Cycle through the possible minutes from the minute string-array resource to find the appropriate spinner position
        for(int i = 0; i < minutesArray.length; i++)
        {
            if(calUtil.get(Calendar.MINUTE) == Integer.parseInt(minutesArray[i]))
            {
                minutePosition = i;
                break;
            }
        }
        minuteSpinner.setSelection(minutePosition);
        ampmSpinner.setSelection(calUtil.get(Calendar.AM_PM));

        String[] durationsArray = getResources().getStringArray(R.array.durations);
        int durationPosition = 0;
        for(int i =0; i< durationsArray.length; i++)
        {
            if(duration == Integer.parseInt(durationsArray[i]))
            {
                durationPosition = i;
                break;
            }
        }
        durationSpinner.setSelection(durationPosition);

        try
        {
            calendar.setDate(new Date(date));
        }
        catch (com.applandeo.materialcalendarview.exceptions.OutOfDateRangeException ex)
        {
            // Need to catch, do nothing
        }
    }

    public void setAmPm(int position)
    {
        ampmSpinner.setSelection(position);
    }

    public void setDuration(int position)
    {
        durationSpinner.setSelection(position);
    }

    public void setListener(InflaterListener listener)
    {
        this.listener = listener;
    }

    private void setSelectedDayAppointments(Calendar selectedDay)
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

        List<Appointment> selectedAppointments = AppointmentDatabase.getInstance(getActivity()).appointmentDAO().getUpcoming(startOfDay, endOfDay);
        String existingAppInfo = "Existing Appointments:\n";
        for(int i = 0; i < selectedAppointments.size(); i++)
        {
            Client client = AppointmentDatabase.getInstance(getActivity()).clientDAO().getClientByID(selectedAppointments.get(i).getClientID());
            String info = client.getFullName() + ": " + selectedAppointments.get(i).getPlainTimeSpan();
            existingAppInfo += info;
            if(i < selectedAppointments.size() - 1)
            {
                existingAppInfo += "\n";
            }
        }

        if(snackbar.isShown())
        {
            snackbar.dismiss(); //dismiss snackbar if it happens to still be showing from another calendar selection
        }
        if(selectedAppointments.size() > 0)
        {
            snackbar = Snackbar.make(snackbarHolder, existingAppInfo, 5000); // need to reset with new instance, or else snackbar would glitch and not show if you selected a day with appointments if the snackbar was already showing.
            ((TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text)).setMaxLines(selectedAppointments.size() + 1);
            snackbar.show();
        }
    }

    // Sets icons on calendar for days with appointments
    private void setupCalendarIcons()
    {
        List<Appointment> allAppointments = AppointmentDatabase.getInstance(getActivity()).appointmentDAO().getAll();

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
        calendar.setEvents(appointmentDays);
    }

}
