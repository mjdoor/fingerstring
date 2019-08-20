package com.example.mdooreleyers.mdooreleyersproject1;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class NewAppointmentFragment extends Fragment {
    private Spinner durationSpinner;
    private Spinner hourSpinner;
    private Spinner minuteSpinner;
    private Spinner ampmSpinner;
    private CalendarView calendar;

    private InflaterListener listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.new_appointment_layout, container, false);

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
                }
                catch (com.applandeo.materialcalendarview.exceptions.OutOfDateRangeException ex)
                {
                    // Need to catch, do nothing
                }
            }
        });

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

}
