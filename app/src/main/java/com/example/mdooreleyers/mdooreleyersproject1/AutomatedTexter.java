package com.example.mdooreleyers.mdooreleyersproject1;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;

import static android.content.Context.NOTIFICATION_SERVICE;

public class AutomatedTexter extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Gather all appointments happening tomorrow
        Calendar calUtil = Calendar.getInstance();
        long now = calUtil.getTimeInMillis();
        // set the time range for appointments to send reminders for
        // start the range at midnight tonight, and end the range whatever number of days is set in preferences past midnight tonight
        // appointments may fall in this range over multiple days (if the reminder advance days setting is greater than 1)
        // This is okay, because it will still remind clients who may have book with short notice (i.e. within the reminder window)
        // These clients will still get reminders
        // Before sending reminders, the appointment is checked if it has already had a reminder sent, and won't send a reminder twice
        long midnightTonight = TimeConstants.calcEndOfDay(now);
        int reminderAdvanceDays = SharedPreferenceUtility.getIntPreference(context, "settingReminderAdvance", context.getResources().getInteger(R.integer.setting_reminder_advance_default));
        long endOfReminderRange = midnightTonight + reminderAdvanceDays * TimeConstants.MILLISECONDS_PER_DAY;
        List<Appointment> appointmentsToRemind = AppointmentDatabase.getInstance(context).appointmentDAO().getUpcoming(midnightTonight, endOfReminderRange);

        // Also, set up alarm for tomorrow before the conditional below checking for appointmentsToRemind, so tomorrow's alarm is guaranteed to be set
        createAlarmForAutoText(context, now);

        if(appointmentsToRemind == null || appointmentsToRemind.size() == 0)
        {
            return;
        }

        // set up channel for notifications
        createNotificationChannel(context);

        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) // READ_PHONE_STATE - tells you if you're talking on the phone or not
        {
            // Don't ask for permissions for automatic texting, just set message status to FAILED TO SEND for appointments
            for(int i = 0; i < appointmentsToRemind.size(); i++)
            {
                Appointment apt = appointmentsToRemind.get(i);
                apt.setReminderStatus(Appointment.ReminderStatus.FAILED.toString());
                AppointmentDatabase.getInstance(context).appointmentDAO().updateAppointment(apt);
            }

            // Passing intent ACTION_SETTINGS - so tapping this notification will open the settings menu, to easily change permission
            sendNotification(   context,
                                new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + context.getPackageName())),
                                context.getResources().getString(R.string.notification_title_failed),
                                context.getResources().getString(R.string.notification_auto_remind_permissions));
        }
        else
        {   // Permission already granted
            SmsManager manager = SmsManager.getDefault();
            int sendCount = 0;
            for(int i = 0; i < appointmentsToRemind.size(); i++)
            {
                Appointment apt = appointmentsToRemind.get(i);
                Client clnt = AppointmentDatabase.getInstance(context).clientDAO().getClientByID(apt.getClientID());
                // Prevent any appointments that have already had a reminder sent, have ANOTHER reminder sent
                // Also, don't send a reminder if the client has disabled autoreminders
                if(apt.getReminderStatus().equals(Appointment.ReminderStatus.SENT.toString()) || clnt.getDisableReminders())
                {
                    continue;
                }

                try
                {
                    String appointmentType = SharedPreferenceUtility.getStringPreference(context, "settingAppointmentType", context.getResources().getString(R.string.setting_appointment_type_default)).toLowerCase();
                    String message = "Hi " + clnt.getFirstName() +
                            ". This text is just reminding you of your upcoming " + appointmentType + " appointment, booked for " + apt.getDateTimeString() +
                            " See you then!";
                    manager.sendTextMessage(clnt.getPhoneNumber(),null,message,null,null);
                    apt.setReminderStatus(Appointment.ReminderStatus.SENT.toString());
                    sendCount++;
                }
                catch(IllegalArgumentException ex) // could occur if the phone number is formatted incorrectly
                {
                    apt.setReminderStatus(Appointment.ReminderStatus.FAILED.toString());
                }

                AppointmentDatabase.getInstance(context).appointmentDAO().updateAppointment(apt);
            }

            if(sendCount > 0)
            {
                // Notification message will default to the string in the string table, which uses the word "Tomorrow" - only applies if reminderAdvanceDays == 1
                String notificationMessage = context.getResources().getString(R.string.notification_auto_remind_sent);
                if(reminderAdvanceDays > 1)
                {
                    // use notification message that contains the reminderAdvanceDays instead of the word "tomorrow"
                    notificationMessage = "Reminders sent for appointments in the next " + reminderAdvanceDays + " days!";
                }
                sendNotification(
                        context,
                        new Intent(context, MainActivity.class),
                        context.getResources().getString(R.string.notification_title_sent),
                        notificationMessage);
            }
        }
    }

    // Include the method here that will set up the alarm for sending automatic texts. We are using setExact for the alarm, which doesn't support repeating, so we have to set the next alarm when an alarm fires
    // param: now, current time, in milliseconds
    public static void createAlarmForAutoText(Context context, long now)
    {
        Intent intent = new Intent(context, AutomatedTexter.class);

        // Using FLAG_UPDATE_CURRENT, an existing alarm will be overwritten by a new one created here. This is okay, because we always check the time before setting the alarm.
        // No send sequence should ever be missed due to an updated alarm, regardless of when/how many times this method is called, since we set the time relative to if we are
        // before or after the send time.
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        // We want to send text reminders daily at the specified time in preferences for appointments happening the next day
        Calendar calUtil = Calendar.getInstance();
        calUtil.setTimeInMillis(now);
        int sendHour = SharedPreferenceUtility.getIntPreference(context, "settingReminderTime", context.getResources().getInteger(R.integer.setting_reminder_time_default));
        // if it's before the desired reminder time, set the alarm for the time today. Else, if it's after, set the alarm for the time tomorrow.
        if(calUtil.get(Calendar.HOUR_OF_DAY) >= sendHour)
        {
           calUtil.setTimeInMillis(now + TimeConstants.MILLISECONDS_PER_DAY); // move forward to tomorrow
        }
        // set time to the set sendHour
        calUtil.set(Calendar.HOUR_OF_DAY, sendHour);
        calUtil.set(Calendar.MINUTE, 0);
        calUtil.set(Calendar.SECOND, 0);

        long sendTime = calUtil.getTimeInMillis();

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, sendTime, pendingIntent);
    }


    // CODE FOR SENDING NOTIFICATIONS TO USER
    public static final int NOTIFICATION_ID = 1;
    public static final String CHANNEL_ID = "channel_01";
    public static final String CHANNEL_NAME = "FingerString_autotext_channel";

    private void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            channel.setDescription(context.getResources().getString(R.string.notification_description));
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // This method sets up a notification and sends it, with the desired click intent, notification title and message provided
    public void sendNotification(Context context, Intent intent, String title, String message) {
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);

        builder.setSmallIcon(R.drawable.fingerstring_notification);

        // Set the intent that will fire when the user taps the notification.
        builder.setContentIntent(pendingIntent);

        // Set the notification to auto-cancel. This means that the notification will disappear
        // after the user taps it, rather than remaining until it's explicitly dismissed.
        builder.setAutoCancel(true);

        builder.setContentTitle(title);
        builder.setContentText(message);

        // send the notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
    // END NOTIFICATION CODE
}
