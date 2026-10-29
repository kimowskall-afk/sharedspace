
package uk.ac.sharedspace;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private String selectedDate = "";
    private String selectedTime = "";

    private final ArrayList<String> myBookings = new ArrayList<>();

    private static final String PREFS_NAME = "SharedSpacePreferences";
    private static final String KEY_BOOKINGS = "savedBookings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load previously saved bookings.
        loadBookings();

        Button dateButton = findViewById(R.id.btnSelectDate);
        Button timeButton = findViewById(R.id.btnSelectTime);
        Button bookingsButton = findViewById(R.id.btnMyBookings);

        dateButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            DatePickerDialog picker = new DatePickerDialog(
                    this,
                    (view, year, month, day) -> {
                        selectedDate = String.format(
                                Locale.getDefault(),
                                "%02d/%02d/%04d",
                                day, month + 1, year
                        );

                        dateButton.setText(selectedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            picker.getDatePicker().setMinDate(
                    System.currentTimeMillis() - 1000
            );

            picker.show();
        });

        timeButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            TimePickerDialog picker = new TimePickerDialog(
                    this,
                    (view, hour, minute) -> {
                        selectedTime = String.format(
                                Locale.getDefault(),
                                "%02d:%02d",
                                hour, minute
                        );

                        timeButton.setText(selectedTime);
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
            );

            picker.show();
        });

        setupRoomButton(R.id.btnRequestRoom1, "Meeting Room 01");
        setupRoomButton(R.id.btnRequestRoom2, "Meeting Room 02");
        setupRoomButton(R.id.btnRequestRoom4, "Meeting Room 03");
        setupRoomButton(R.id.btnRequestRoom5, "Meeting Room 04");
        setupRoomButton(R.id.btnRequestRoom6, "Meeting Room 05");
        setupRoomButton(R.id.btnRequestRoom3, "Seminar Room 01");
        setupRoomButton(R.id.btnRequestRoom7, "Seminar Room 02");

        bookingsButton.setOnClickListener(v -> showMyBookings());
    }

    private void setupRoomButton(int buttonId, String roomName) {
        Button button = findViewById(buttonId);

        button.setOnClickListener(v -> requestBooking(roomName));
    }

    private void requestBooking(String roomName) {

        if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
            Toast.makeText(
                    this,
                    "Please select a date and time first.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        String booking = roomName + " | "
                + selectedDate + " | "
                + selectedTime + " | Pending (demo)";

        if (myBookings.contains(booking)) {
            Toast.makeText(
                    this,
                    "This demo request already exists.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirm booking request")
                .setMessage(
                        "Room: " + roomName
                                + "\nDate: " + selectedDate
                                + "\nTime: " + selectedTime
                                + "\n\nSend this demo request?"
                )
                .setNegativeButton("Go back", null)
                .setPositiveButton("Send request", (dialog, which) -> {

                    myBookings.add(booking);

                    // Save immediately after adding a booking.
                    saveBookings();

                    Toast.makeText(
                            this,
                            "Demo request saved successfully.",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .show();
    }

    private void saveBookings() {
        JSONArray jsonArray = new JSONArray();

        for (String booking : myBookings) {
            jsonArray.put(booking);
        }

        SharedPreferences preferences =
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        preferences.edit()
                .putString(KEY_BOOKINGS, jsonArray.toString())
                .apply();
    }

    private void loadBookings() {
        SharedPreferences preferences =
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        String savedBookings = preferences.getString(KEY_BOOKINGS, "[]");

        myBookings.clear();

        try {
            JSONArray jsonArray = new JSONArray(savedBookings);

            for (int i = 0; i < jsonArray.length(); i++) {
                myBookings.add(jsonArray.getString(i));
            }

        } catch (JSONException e) {
            myBookings.clear();

            Toast.makeText(
                    this,
                    "Saved bookings could not be read.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void showMyBookings() {

        if (myBookings.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("My bookings")
                    .setMessage("You have no demo booking requests yet.")
                    .setPositiveButton("Close", null)
                    .show();

            return;
        }

        String[] bookings = myBookings.toArray(new String[0]);

        new AlertDialog.Builder(this)
                .setTitle("My bookings")
                .setItems(bookings, (dialog, position) -> {

                    String selectedBooking = bookings[position];

                    new AlertDialog.Builder(this)
                            .setTitle("Booking details")
                            .setMessage(selectedBooking)
                            .setNegativeButton("Close", null)
                            .setNeutralButton(
                                    "Cancel request",
                                    (detailDialog, which) -> {

                                        myBookings.remove(selectedBooking);

                                        // Save the updated list after cancellation.
                                        saveBookings();

                                        Toast.makeText(
                                                this,
                                                "Demo request cancelled.",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }
                            )
                            .show();
                })
                .setPositiveButton("Done", null)
                .show();
    }
}
