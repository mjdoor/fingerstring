package com.fingerstring.mdooreleyers.mdooreleyersproject1;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    EditText appointmentType;
    Spinner reminderTime;
    Spinner reminderAmPm;
    Spinner reminderAdvanceTime;
    Spinner upcomingAppointmentCutoff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        appointmentType  = (EditText)findViewById(R.id.settingAppointmentType);
        reminderTime = (Spinner)findViewById(R.id.settingReminderTime);
        reminderAmPm = (Spinner)findViewById(R.id.settingReminderAmPm);
        reminderAdvanceTime = (Spinner)findViewById(R.id.settingReminderAdvance);
        upcomingAppointmentCutoff = (Spinner)findViewById(R.id.settingUpcomingCutoff);

        setupDefaults();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    // Sets the text fields and values of spinners depending on existing preferences
    private void setupDefaults()
    {
        SharedPreferences preferences = getSharedPreferences("fingerstring", Context.MODE_PRIVATE);

        // Appointment type
        appointmentType.setText(SharedPreferenceUtility.getStringPreference(this, "settingAppointmentType", getString(R.string.setting_appointment_type_default)));

        // Reminder time spinners
        int reminderTimeHourOfDay = SharedPreferenceUtility.getIntPreference(this, "settingReminderTime", getResources().getInteger(R.integer.setting_reminder_time_default));
        int ampmSpinnerPosition = 0; // initialize to 0 (am)
        if(reminderTimeHourOfDay > 11)
        {
            reminderTimeHourOfDay -= 12;
            ampmSpinnerPosition = 1;
        }
        int reminderHourPosition = reminderTimeHourOfDay;
        reminderTime.setSelection(reminderHourPosition);
        reminderAmPm.setSelection(ampmSpinnerPosition);

        // Reminder advance time
        reminderAdvanceTime.setSelection(SharedPreferenceUtility.getIntPreference(this, "settingReminderAdvance", getResources().getInteger(R.integer.setting_reminder_advance_default)) - 1); // -1 to map to 0 based index array

        // Upcoming cutoff
        upcomingAppointmentCutoff.setSelection(SharedPreferenceUtility.getIntPreference(this, "settingUpcomingCutoff", getResources().getInteger(R.integer.setting_upcoming_cutoff_default)) - 1);
    }

    public void updateClick(View view) {
        SharedPreferences preferences = getSharedPreferences("fingerstring", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // Appointment type
        // Ensure the appointment type is filled in
        if(appointmentType.getText().toString().isEmpty())
        {
            Toast.makeText(this, R.string.update_settings_error, Toast.LENGTH_SHORT).show();
            return;
        }
        editor.putString("settingAppointmentType", appointmentType.getText().toString());

        // Reminder time, as HOUR_OF_DAY
        int reminderHour = Integer.parseInt(reminderTime.getSelectedItem().toString());
        if(reminderHour == 12) { reminderHour = 0; }
        String ampm = reminderAmPm.getSelectedItem().toString();
        if(ampm.equals("PM")) { reminderHour += 12; }
        editor.putInt("settingReminderTime", reminderHour);

        // Reminder advance time
        editor.putInt("settingReminderAdvance", Integer.parseInt(reminderAdvanceTime.getSelectedItem().toString()));

        // Upcoming Cutoff Time
        editor.putInt("settingUpcomingCutoff", Integer.parseInt(upcomingAppointmentCutoff.getSelectedItem().toString()));

        editor.apply();

        Toast.makeText(this, R.string.update_settings_success, Toast.LENGTH_SHORT).show();
        finish();
    }

    public void backClick(View view) {
        Toast.makeText(this, R.string.update_settings_back, Toast.LENGTH_SHORT).show();
        finish();
    }


}
