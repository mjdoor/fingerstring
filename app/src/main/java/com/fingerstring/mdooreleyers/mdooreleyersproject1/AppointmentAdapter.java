package com.fingerstring.mdooreleyers.mdooreleyersproject1;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView appointmentTimeBox;
        public TextView clientNameBox;
        public TextView reminderStatusBox;
        public Button rescheduleBtn;
        public Button textBtn;
        public Button callBtn;
        public Button cancelBtn;
        public CardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);

            appointmentTimeBox = (TextView) itemView.findViewById(R.id.appointmentItemTimeTxt);
            clientNameBox = (TextView) itemView.findViewById(R.id.appointmentItemNameTxt);
            reminderStatusBox = (TextView)itemView.findViewById(R.id.reminderStatusTxt);
            rescheduleBtn = (Button)itemView.findViewById(R.id.rescheduleBtn);
            textBtn = (Button)itemView.findViewById(R.id.textBtn);
            callBtn = (Button)itemView.findViewById(R.id.callBtn);
            cancelBtn = (Button)itemView.findViewById(R.id.cancelBtn);
            cardView = (CardView) itemView.findViewById(R.id.aptCardView);
        }
    }

    // Store today's appointments in a member variable
    private List<Appointment> appointments;
    Context context;
    private AppointmentCancellationListener cancellationListener; // used by ViewAppointmentsActivity to update the calendar icons in case the only appointment of a day is cancelled

    public AppointmentAdapter(List<Appointment> apts)
    {
        this.appointments = apts;
    }

    @Override
    public AppointmentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Define what layout to use ("inflate") for each item in the recycler
        View appointmentItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.appointment_item, parent, false);
        context = parent.getContext();
        return new ViewHolder(appointmentItem);
    }

    @Override
    public void onBindViewHolder(final AppointmentAdapter.ViewHolder viewHolder, final int position) {
        final Appointment app = appointments.get(position);
        Client clnt = AppointmentDatabase.getInstance(context).clientDAO().getClientByID(app.getClientID());

        // set values for appointment
        viewHolder.appointmentTimeBox.setText(app.getTimeSpan());
        viewHolder.clientNameBox.setText(clnt.getFullName());

        if(clnt.getDisableReminders())
        {
            viewHolder.reminderStatusBox.setText(R.string.reminders_disabled);
            viewHolder.reminderStatusBox.setTextColor(Color.DKGRAY);
        }
        else if(app.getReminderStatus().equals(Appointment.ReminderStatus.NOT_SENT.toString()))
        {
            viewHolder.reminderStatusBox.setText(R.string.reminder_not_sent);
            viewHolder.reminderStatusBox.setTextColor(Color.DKGRAY);
        }
        else if(app.getReminderStatus().equals(Appointment.ReminderStatus.SENT.toString()))
        {
            viewHolder.reminderStatusBox.setText(R.string.reminder_sent);
            viewHolder.reminderStatusBox.setTextColor(Color.WHITE);
        }
        else
        {
            viewHolder.reminderStatusBox.setText(R.string.reminder_failed);
            viewHolder.reminderStatusBox.setTextColor(Color.RED);
        }

        // RESCHEDULE BUTTON EVENT
        viewHolder.rescheduleBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
            Intent intent = new Intent(context, AddAppointmentActivity.class);
            intent.putExtra("request_type", "update");
            intent.putExtra("aptID", app.aptID);
            context.startActivity(intent);
            }
        });

        // TEXT BUTTON EVENT
        viewHolder.textBtn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setData(Uri.parse("smsto:"));  // This ensures only SMS apps respond
                intent.setType("text/plain");
                intent.putExtra("address", clnt.getPhoneNumber());
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(intent);
                }
            }
        });

        // CALL BUTTON EVENT
        viewHolder.callBtn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions((MainActivity)context, new String[]{Manifest.permission.CALL_PHONE},1);
                }
                else
                {   // Permission already granted
                    try {
                        String fullNumber = "tel:" + clnt.getPhoneNumber();
                        Intent intent = new Intent(Intent.ACTION_CALL);
                        intent.setData(Uri.parse(fullNumber));
                        context.startActivity(intent);
                    }
                    catch (SecurityException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });

        // CANCEL BUTTON EVENT
        viewHolder.cancelBtn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(context) // set up confirmation box for deleting appointment
                        .setTitle(R.string.confirm_cancel)
                        .setMessage("Are you sure you want to cancel " + clnt.getFirstName() + "'s appointment?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int btn) {
                                try
                                {
                                    // using this "realPosition" instead of input arg "position" because sometimes an error would occur when deleting appointments where position would be off by 1
                                    int realPosition = viewHolder.getAdapterPosition();
                                    AppointmentDatabase.getInstance(context).appointmentDAO().deleteAppointment(app);
                                    appointments.remove(realPosition);
                                    notifyItemRemoved(realPosition); // so recycler view is updated
                                    Toast.makeText(context, R.string.cancelled_appointment, Toast.LENGTH_SHORT).show();
                                    if(cancellationListener != null && appointments.size() == 0)
                                    {
                                        cancellationListener.OnAppointmentCancelled();
                                    }
                                }
                                catch(Exception e)
                                {
                                    Toast.makeText(context, R.string.cancelling_error, Toast.LENGTH_SHORT).show();
                                }
                            }})
                        .setNegativeButton(R.string.deny, null).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public void changeData(List<Appointment> newList)
    {
        this.appointments = newList;
        notifyDataSetChanged();
    }

    public void setCancellationListener(AppointmentCancellationListener cancellationListener) {
        this.cancellationListener = cancellationListener;
    }
}
