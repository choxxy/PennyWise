package com.iogarage.ke.pennywise;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.iogarage.ke.pennywise.dialogs.AdvancedRepeatSelector;
import com.iogarage.ke.pennywise.dialogs.DaysOfWeekSelector;
import com.iogarage.ke.pennywise.dialogs.RepeatSelector;
import com.iogarage.ke.pennywise.entities.DaoSession;
import com.iogarage.ke.pennywise.entities.Debt;
import com.iogarage.ke.pennywise.entities.DebtDao;
import com.iogarage.ke.pennywise.entities.Reminder;
import com.iogarage.ke.pennywise.entities.ReminderDao;
import com.iogarage.ke.pennywise.receivers.AlarmReceiver;
import com.iogarage.ke.pennywise.util.AlarmUtil;
import com.iogarage.ke.pennywise.util.DateAndTimeUtil;
import com.iogarage.ke.pennywise.util.DateUtil;
import com.iogarage.ke.pennywise.util.TextFormatUtil;
import com.iogarage.ke.pennywise.util.Util;
import com.iogarage.ke.pennywise.util.ViewUtil;

import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

import autodagger.AutoInjector;
import autodagger.AutoSubcomponent;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multimoon.colorful.CAppCompatActivity;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


@AutoSubcomponent
@AutoInjector
@DaggerScope(TransactionView.class)
public class TransactionView extends CAppCompatActivity implements AdvancedRepeatSelector.AdvancedRepeatSelectionListener,
        DaysOfWeekSelector.DaysOfWeekSelectionListener, RepeatSelector.RepeatSelectionListener {


    public final int PICK_CONTACT = 2016;
    // Request code for READ_CONTACTS. It can be any number > 0.
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    private EditText amount;
    private EditText note;
    private SwitchCompat swForever;
    private AppCompatSpinner transactionSpinner;
    private TextView endDate;
    private TextView startDate;
    private EditText phoneRetv;
    private TextInputLayout tilAmount;
    private TextInputLayout tilContact;
    private TextView alarmDate, alarmTime, tvReminder;

    @Inject
    DaoSession session;

    private DebtDao transactionDao;
    private ReminderDao reminderDao;

    public static final int LENDING = 2;
    public static final int BORROWING = 1;
    public static final int ALL = -1;
    private Toolbar toolbar;
    private boolean edit = false;
    String displayName = "";
    String phoneNumber = "";
    private Debt transaction;
    private Reminder reminder;
    private DatePickerDialog datePickerDialog;
    private Calendar mCalendar;
    private TimePickerDialog timePickerDialog;
    private LinearLayout foreverRow;
    private LinearLayout bottomRow;
    private ImageView alarmIcon;
    private Calendar calendar;
    private int transactionType;


    enum DateType {
        START_DATE,
        DUE_DATE,
        REMINDER
    }


    private String mTime;
    private String mDate;

    private boolean[] daysOfWeek = new boolean[7];
    private int timesShown = 0;
    private int timesToShow = 1;
    private int repeatType = Reminder.NO_REMINDER;
    private int id;
    private int interval = 1;

    private DateType dateType;
    Date localStartdate;
    Date localEnddate;
    private boolean editMode;
    // Chosen values
    private int mYear, mMonth, mDay, mHour, mMinute;
    AdView adView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);


        ButterKnife.bind(this);

        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        setTitle("New Transaction");

        TextView title = findViewById(R.id.title);
        title.setText("");

        amount = findViewById(R.id.etx_amount);

        note = findViewById(R.id.etx_note);
        swForever = findViewById(R.id.switch_toggle);
        swForever.setEnabled(false);
        transactionSpinner = findViewById(R.id.transaction_type);
        endDate = findViewById(R.id.tv_enddate);
        startDate = findViewById(R.id.tv_startdate);
        phoneRetv = findViewById(R.id.etx_name);
        tilAmount = findViewById(R.id.til_amount);
        tilContact = findViewById(R.id.til_contact);
        tvReminder = findViewById(R.id.reminder_text);
        foreverRow = findViewById(R.id.forever_row);
        bottomRow = findViewById(R.id.bottom_row);
        alarmIcon = findViewById(R.id.alarm_icon);

        ((PennyApp) getApplication())
                .getComponent()
                .plusTransactionViewComponent()
                .inject(this);

        transactionDao = session.getDebtDao();
        reminderDao = session.getReminderDao();

        localStartdate = new Date();
        localEnddate = DateUtil.plusDays(localStartdate, 30);

        startDate.setText(getTodayDate(0));
        endDate.setText(Util.formatDate(localEnddate, true, false));
        calendar = Calendar.getInstance();

        if (getIntent().hasExtra("TRANSACTIONID")) {
            long id = getIntent().getLongExtra("TRANSACTIONID", 0);
            populateData(id);
            editMode = true;
        }

        // Load an ad into the AdMob banner view.
        adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        adView.loadAd(adRequest);

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adView.setVisibility(View.VISIBLE);
            }

            public void onAdFailedToLoad(int errorCode) {
                adView.setVisibility(View.GONE);
            }
        });

        mCalendar = Calendar.getInstance();
        mHour = mCalendar.get(Calendar.HOUR_OF_DAY);
        mMinute = mCalendar.get(Calendar.MINUTE);
        mYear = mCalendar.get(Calendar.YEAR);
        mMonth = mCalendar.get(Calendar.MONTH) + 1;
        mDay = mCalendar.get(Calendar.DATE);

        mDate = DateUtil.parseDate(mCalendar.getTime());
        mTime = DateUtil.parseTime(mCalendar.getTime());

        datePickerDialog = new DatePickerDialog(
                this,
                datePickerListener,
                mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        timePickerDialog = new TimePickerDialog(
                this,
                timePickerListener,
                mCalendar.get(Calendar.HOUR), mCalendar.get(Calendar.MINUTE), true);

        transactionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                transactionType = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                transactionType = 0;
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onPause() {
        adView.pause();
        super.onPause();

    }

    private void populateData(long transactionId) {
        edit = true;
        transaction = transactionDao.loadDeep(transactionId);

        displayName = transaction.getPersonname();
        phoneNumber = transaction.getPhonenumber();
        phoneRetv.setText(displayName);
        phoneRetv.invalidate();
        note.setText(transaction.getNote());
        amount.setText(String.valueOf(transaction.getAmount()));
        startDate.setText(Util.formatDate(transaction.getTransactiondate(), true, false));
        endDate.setText(Util.formatDate(transaction.getPaydate(), true, false));
        transactionSpinner.setSelection(transaction.getType());


        localEnddate = transaction.getPaydate();

        //load reminder
        reminder = transaction.getReminder();

        if (reminder != null)
            assignReminderValues(reminder);

    }

    public void assignReminderValues(Reminder reminder) {

        timesShown = reminder.getNumberShown();
        repeatType = reminder.getRepeatType();
        interval = reminder.getInterval();

        calendar = DateAndTimeUtil.parseDateAndTime(reminder.getDateAndTime());

        if (reminder.getRepeatType() != Reminder.NO_REMINDER) {
            if (reminder.getInterval() > 1) {
                tvReminder.setText(TextFormatUtil.formatAdvancedRepeatText(this, repeatType, interval));
            } else {
                tvReminder.setText(getResources().getStringArray(R.array.repeat_array)[reminder.getRepeatType()]);
            }
        } else
            alarmIcon.setImageResource(R.drawable.alarm_off);


        if (reminder.getRepeatType() == Reminder.SPECIFIC_DAYS) {
            daysOfWeek = TextFormatUtil.fromDatsOfWeekText(reminder.getDaysOfWeek());
            tvReminder.setText(TextFormatUtil.formatDaysOfWeekText(this, daysOfWeek));

        }

        if (Boolean.parseBoolean(reminder.getForeverState())) {
            swForever.setChecked(true);
            bottomRow.setVisibility(View.GONE);
        }
    }

    public void showFrequency(boolean show) {
        if (show) {
            foreverRow.setVisibility(View.VISIBLE);
            bottomRow.setVisibility(View.VISIBLE);
        } else {
            swForever.setChecked(false);
            foreverRow.setVisibility(View.GONE);
            bottomRow.setVisibility(View.GONE);
        }
    }

    private String getTodayDate(int days) {
        Date localDate = new Date();
        localDate = DateUtil.plusDays(localDate, days);
        return Util.formatDate(localDate, true, false);
    }

    @OnClick(value = {R.id.showcontacts, R.id.reminder_text})
    public void onClick(View view) {

        if (view.getId() == R.id.showcontacts)
            getContact();
        else if (view.getId() == R.id.reminder_text) {
            DialogFragment dialog = new RepeatSelector();
            dialog.show(getSupportFragmentManager(), "RepeatSelector");
        } else
            showTimePicker(view.getId());
    }

    private void getContact() {

        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            showContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                showContacts();
            } else {
                Toast.makeText(this, "Until you grant the permission, we cannot display your contacts.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showContacts() {

        Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(i, PICK_CONTACT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_CONTACT && resultCode == RESULT_OK) {
            Uri contactUri = data.getData();
            Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
            cursor.moveToFirst();

            int number = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            int name = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

            phoneNumber = cursor.getString(number);
            displayName = cursor.getString(name);

            phoneRetv.setText(displayName);

            cursor.close();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.action_save:
                save();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void save() {

        try {

            if (!isValid())
                return;

            if (transaction == null)
                transaction = new Debt();

            String textNote = note.getText().toString();

            transaction.setPersonname(TextUtils.isEmpty(displayName) ? phoneRetv.getText().toString() : displayName);
            transaction.setPhonenumber(phoneNumber);
            transaction.setAmount(Double.parseDouble(amount.getText().toString()));
            transaction.setBalance(Double.parseDouble(amount.getText().toString()));
            transaction.setPaid(false);
            transaction.setStatus(1);
            transaction.setNote(textNote);
            transaction.setTransactiondate(DateUtil.removeTime(localStartdate));
            transaction.setPaydate(DateUtil.removeTime(localEnddate));
            transaction.setType(transactionType);

            if (repeatType != Reminder.NO_REMINDER) {
                long reminderId = saveReminder();
                transaction.setReminderId(reminderId);
            }

            if (editMode) {
                transactionDao.update(transaction);
            } else
                transactionDao.insert(transaction);

            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();

            finish();


        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public long saveReminder() {

        long reminderId = 0;

        if (reminder == null) {
            reminder = new Reminder()
                    .setId(null)
                    .setTitle(getString(R.string.app_name))
                    .setContent("")
                    .setDateAndTime(DateAndTimeUtil.toStringDateAndTime(calendar))
                    .setRepeatType(repeatType)
                    .setForeverState(Boolean.toString(true))
                    .setActive(true)
                    .setNumberShown(timesShown)
                    .setInterval(interval);
        } else {
            reminder.setTitle(getString(R.string.app_name))
                    .setContent("")
                    .setDateAndTime(DateAndTimeUtil.toStringDateAndTime(calendar))
                    .setRepeatType(repeatType)
                    .setForeverState(Boolean.toString(swForever.isChecked()))
                    .setActive(true)
                    .setNumberShown(timesShown)
                    .setInterval(interval);
        }

        if (repeatType == Reminder.SPECIFIC_DAYS) {
            reminder.setDaysOfWeek(TextFormatUtil.formatDaysOfWeekText(this, daysOfWeek));
        }

        if (reminder.getId() != null) {
            reminderDao.update(reminder);
            reminderId = reminder.getId();
        } else {
            reminderId = reminderDao.insert(reminder);
        }

        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        calendar.set(Calendar.SECOND, 0);
        AlarmUtil.setAlarm(this, alarmIntent, (int) reminderId, calendar);

        return reminderId;
    }

    public void validateInput() {
      /*  imageWarningShow.setVisibility(View.GONE);
        imageWarningTime.setVisibility(View.GONE);
        imageWarningDate.setVisibility(View.GONE);
        Calendar nowCalendar = Calendar.getInstance();

        if (timeText.getText().equals(getString(R.string.time_now))) {
            calendar.set(Calendar.HOUR_OF_DAY, nowCalendar.get(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, nowCalendar.get(Calendar.MINUTE));
        }

        if (dateText.getText().equals(getString(R.string.date_today))) {
            calendar.set(Calendar.YEAR, nowCalendar.get(Calendar.YEAR));
            calendar.set(Calendar.MONTH, nowCalendar.get(Calendar.MONTH));
            calendar.set(Calendar.DAY_OF_MONTH, nowCalendar.get(Calendar.DAY_OF_MONTH));
        }

        // Check if the number of times to show notification is empty
        if (timesEditText.getText().toString().isEmpty()) {
            timesEditText.setText("1");
        }

        timesToShow = Integer.parseInt(timesEditText.getText().toString());
        if (repeatType == Reminder.NO_REMINDER) {
            timesToShow = timesShown + 1;
        }

        // Check if selected date is before today's date
        if (DateAndTimeUtil.toLongDateAndTime(calendar) < DateAndTimeUtil.toLongDateAndTime(nowCalendar)) {
            Snackbar.make(coordinatorLayout, R.string.toast_past_date, Snackbar.LENGTH_SHORT).show();
            imageWarningTime.setVisibility(View.VISIBLE);
            imageWarningDate.setVisibility(View.VISIBLE);

            // Check if title is empty
        } else if (titleEditText.getText().toString().trim().isEmpty()) {
            Snackbar.make(coordinatorLayout, R.string.toast_title_empty, Snackbar.LENGTH_SHORT).show();
            AnimationUtil.shakeView(titleEditText, this);

            // Check if times to show notification is too low
        } else if (timesToShow <= timesShown && !foreverSwitch.isChecked()) {
            Snackbar.make(coordinatorLayout, R.string.toast_higher_number, Snackbar.LENGTH_SHORT).show();
            imageWarningShow.setVisibility(View.VISIBLE);
        } else {
            saveNotification();
        }*/
    }

    private boolean isValid() {

        boolean valid = true;

        if (phoneRetv.getText().length() == 0) {
            ViewUtil.showError(this, "Contact is required.");
            return  false;
        }

        if (amount.getText().length() == 0) {
            ViewUtil.showError(this, "Amount is required.");
            return  false;
        }

        if (transactionType == 0) {
            ViewUtil.showError(this, "Please select transaction type (Borrow\\Lend).");
            return  false;
        }

        return valid;
    }

    private DatePickerDialog.OnDateSetListener datePickerListener
            = new DatePickerDialog.OnDateSetListener() {

        // when dialog box is closed, below method will be called.
        public void onDateSet(DatePicker view, int selectedYear,
                              int selectedMonth, int selectedDay) {
            mYear = selectedYear;
            mMonth = selectedMonth;
            mDay = selectedDay;


            Calendar cal = Calendar.getInstance();
            cal.set(mYear, mMonth, mDay);

            mMonth++;
            mDate = mDay + "/" + mMonth + "/" + mYear;

            switch (dateType) {
                case START_DATE:
                    calendar.set(mYear, mMonth, mDay);
                    startDate.setText(Util.formatDate(cal.getTime(), true, false));
                    localStartdate = cal.getTime();
                    break;
                case DUE_DATE:
                    localEnddate = cal.getTime();
                    endDate.setText(Util.formatDate(cal.getTime(), true, false));
                    break;
                case REMINDER:
                    alarmDate.setText(mDate);
                    break;

            }
        }
    };


    private TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
            alarmTime.setText(selectedHour + ":" + selectedMinute);

            mHour = selectedHour;
            mMinute = selectedMinute;

        }
    };


    private void showTimePicker(int viewId) {

        switch (viewId) {
            case R.id.tv_enddate:
                dateType = DateType.DUE_DATE;
                Calendar cal = Calendar.getInstance();
                cal.setTime(localEnddate);
                datePickerDialog.getDatePicker().updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
                break;
            case R.id.tv_startdate:
                dateType = DateType.START_DATE;
                datePickerDialog.show();
                break;
          /*  case R.id.reminder_date:
                dateType = DateType.REMINDER;
                datePickerDialog.show();
                break;
            case R.id.reminder_time:
                timePickerDialog.show();
                break;*/
        }


    }

    @Override
    public void onResume() {
        super.onResume();

        adView.resume();

        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.ic_action_back);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setResult(RESULT_CANCELED, new Intent());
                    finish();
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        adView.destroy();
        super.onDestroy();
    }


    @Override
    public void onAdvancedRepeatSelection(int type, int interval, String repeatText) {
        repeatType = type;
        this.interval = interval;
        this.tvReminder.setText(repeatText);
        alarmIcon.setImageResource(R.drawable.alarm);
        //showFrequency(true);
    }


    @Override

    public void onDaysOfWeekSelected(boolean[] days) {
        tvReminder.setText(TextFormatUtil.formatDaysOfWeekText(this, days));
        daysOfWeek = days;
        repeatType = Reminder.SPECIFIC_DAYS;
        alarmIcon.setImageResource(R.drawable.alarm);
        //showFrequency(true);
    }

    @Override
    public void onRepeatSelection(DialogFragment dialog, int interval, String repeatText) {
        interval = 1;
        repeatType = interval;
        this.tvReminder.setText(repeatText);
        alarmIcon.setImageResource(R.drawable.alarm);
    }


}
