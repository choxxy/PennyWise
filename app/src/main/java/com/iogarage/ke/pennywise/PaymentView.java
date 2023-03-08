package com.iogarage.ke.pennywise;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.google.android.material.textfield.TextInputLayout;
import androidx.core.view.LayoutInflaterCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.iogarage.ke.pennywise.entities.DaoSession;
import com.iogarage.ke.pennywise.entities.Debt;
import com.iogarage.ke.pennywise.entities.DebtDao;
import com.iogarage.ke.pennywise.entities.Payment;
import com.iogarage.ke.pennywise.entities.PaymentDao;
import com.iogarage.ke.pennywise.entities.Reminder;
import com.iogarage.ke.pennywise.entities.ReminderDao;
import com.iogarage.ke.pennywise.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import javax.inject.Inject;

import autodagger.AutoInjector;
import autodagger.AutoSubcomponent;
import io.multimoon.colorful.CAppCompatActivity;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static io.multimoon.colorful.ColorfulKt.Colorful;


@AutoSubcomponent
@AutoInjector
@DaggerScope(PaymentView.class)
public class PaymentView extends CAppCompatActivity {
    public static final String TRANSACTION_ID = "param1";

    private static final int PAY = 1;
    private static final int EDIT = 2;

    private long transactionId;
    private Debt transaction;

    @Inject
    DaoSession daoSession;

    private DebtDao transactionDao;

    private TextView phoneNumber;
    private TextView amount;
    private TextView paidAmount;
    private TextView balance;
    private TextView name;
    private TextView note;
    private LinearLayout noteLayout;
    private TextView dueDate;
    private RecyclerView paymentList;
    private Toolbar toolbar;
    private LinearLayout paymentListHeader;

    private PaymentDao paymentDao;
    private String currency;
    private String msg_template;
    private ReminderDao reminderDao;
    private AdView adView;
    private TextView penaltyText;
    private TextView topUpText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_payment);
        setTitle("Payment History");

        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);


        adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("F76CADC28B22D55BB68E255C034A3996")
                .addTestDevice("825E1DA812A6CB10A5CCB7314FF4387B")
                .build();
        adView.loadAd(adRequest);

        TextView title = findViewById(R.id.title);
        title.setText("");


        phoneNumber = findViewById(R.id.loan_details_phone_no_text);
        amount = findViewById(R.id.loan_details_loanamount);
        paidAmount = findViewById(R.id.loan_details_paid);
        balance = findViewById(R.id.loan_details_balance);
        name = findViewById(R.id.person_name);
        note = findViewById(R.id.note);
        noteLayout = findViewById(R.id.note_layout);
        dueDate = findViewById(R.id.loan_details_due_date);
        paymentList = findViewById(R.id.loansdetailslist);
        penaltyText = findViewById(R.id.loan_penalty_paid);
        topUpText = findViewById(R.id.loan_topup_amount);
        paymentListHeader = findViewById(R.id.loan_payment_list_header);

        ((PennyApp) getApplication())
                .getComponent()
                .plusPaymentViewComponent()
                .inject(this);

        transactionDao = daoSession.getDebtDao();
        paymentDao = daoSession.getPaymentDao();
        reminderDao = daoSession.getReminderDao();

        transactionId = getIntent().getLongExtra(TRANSACTION_ID, 0);
        transaction = transactionDao.loadDeep(transactionId);
    }

    public PaymentDao getPaymentDao() {
        return paymentDao;
    }

    private boolean hasPayment() {
        boolean payments = false;

        if (transaction != null)
            payments = (transaction.getPayments().size() > 0);

        return payments;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    protected void loadData() {

        if (transaction != null) {

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

            if (sharedPref != null) {
                currency = sharedPref.getString(getString(R.string.prefCurrency), "");
                msg_template = sharedPref.getString(getString(R.string.prefTemplate), "");
            }

            if (!currency.isEmpty()) {
                if (currency.equalsIgnoreCase("146"))
                    currency = "";
            }

            amount.setText(Util.formatCurrency(transaction.getAmount()));
            balance.setText(currency + " " + Util.formatCurrency(transaction.getBalance()));

            phoneNumber.setText(transaction.getPhonenumber());
            name.setText(transaction.getPersonname());

            if (transaction.getNote().isEmpty())
                noteLayout.setVisibility(View.GONE);
            else
                noteLayout.setVisibility(View.VISIBLE);

            note.setText(transaction.getNote());

            LinearLayoutManager layoutManager = new LinearLayoutManager(
                    this);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            paymentList.setLayoutManager(layoutManager);
            paymentList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
            paymentList.setItemAnimator(new DefaultItemAnimator());

            List<Payment> dataset = transaction.getPayments();

            double[] totals = new double[]{0.0, 0.0, 0.0};

            for (Payment payment : dataset) {
                if (payment.getDescription().equalsIgnoreCase(getString(R.string.payment)))
                    totals[0] += payment.getAmountpaid();
                if (payment.getDescription().equalsIgnoreCase(getString(R.string.penalty)))
                    totals[1] += payment.getAmountpaid();
                if (payment.getDescription().equalsIgnoreCase(getString(R.string.topup)))
                    totals[2] += payment.getAmountpaid();

            }


            paidAmount.setText(String.format(getString(R.string.loan_details_place_holder_text), Util.formatCurrency(totals[0])));

            if (totals[1] != 0) {
                penaltyText.setVisibility(View.VISIBLE);
                findViewById(R.id.textView8).setVisibility(View.VISIBLE);
                penaltyText.setText(String.format(getString(R.string.loan_details_place_holder_text), Util.formatCurrency(Math.abs(totals[1]))));
            } else {
                penaltyText.setVisibility(View.GONE);
                findViewById(R.id.textView8).setVisibility(View.GONE);
            }

            if (totals[2] != 0) {
                topUpText.setVisibility(View.VISIBLE);
                findViewById(R.id.textView9).setVisibility(View.VISIBLE);
                topUpText.setText(String.format(getString(R.string.loan_details_place_holder_text), Util.formatCurrency(Math.abs(totals[2]))));
            } else {
                topUpText.setVisibility(View.GONE);
                findViewById(R.id.textView9).setVisibility(View.GONE);
            }

            PaymentAdapter mAdapter = new PaymentAdapter(dataset, this);
            paymentList.setAdapter(mAdapter);

            dueDate.setText("Due date: " + Util.formatDate(transaction.getPaydate()));

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pay_menu, menu);

        if (transaction != null) {
            if (transaction.getPaid()) {
                menu.removeItem(R.id.action_pay);
                menu.removeItem(R.id.action_edit);
            }

            if (transaction.getType() == TransactionView.BORROWING)
                menu.removeItem(R.id.action_message);

            if (hasPayment())
                menu.removeItem(R.id.action_edit);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.action_pay:
                payDialog(R.id.action_pay);
                return true;
            case R.id.action_topup:
                payDialog(R.id.action_topup);
                return true;
            case R.id.action_penalty:
                payDialog(R.id.action_penalty);
                return true;
            case R.id.action_edit:
                if (hasPayment())
                    return true;
                Intent intent = new Intent(this, TransactionView.class);
                intent.putExtra("TRANSACTIONID", transactionId);
                startActivity(intent);
                return true;
            case R.id.action_message:
                sendMessage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sendMessage() {
        if (transaction != null) {
            if (!msg_template.isEmpty()) {
                msg_template = msg_template.replace("@name", transaction.getPersonname().split(" ")[0]);
                msg_template = msg_template.replace("@amount", currency + " " + Util.formatCurrency(transaction.getBalance()));
            }

            Intent smsIntent = new Intent(android.content.Intent.ACTION_VIEW);
            smsIntent.setType("vnd.android-dir/mms-sms");
            smsIntent.putExtra("address", transaction.getPhonenumber());
            smsIntent.putExtra("sms_body", msg_template);
            startActivity(Intent.createChooser(smsIntent, "Send SMS reminder"));
        }
    }

    private void editTransaction() {
        Intent intent = new Intent(this, TransactionView.class);
        intent.putExtra("TRANSACTIONID", transactionId);
        startActivity(intent);
    }

    @Subscribe
    public void paymentSelected(final PaymentSelected event) {

        //String amount = event.getPayment().getAmountpaid().toString();

        /*MaterialDialog materialDialog = new MaterialDialog.Builder(this)
                .title(R.string.update_payment)
                .content(String.format(getString(R.string.payment_amount), currency, Util.formatCurrency(transaction.getBalance())))
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .positiveText(R.string.update)
                .negativeText(R.string.cancel)
                .neutralText(R.string.delete)
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Payment payment = event.getPayment();
                        double original = event.getPayment().getAmountpaid();

                        if (payment.getDescription().equals(getString(R.string.payment)))
                            original *= -1;

                        paymentDao.delete(payment);
                        updateBalance(original);
                        loadData();
                    }
                })
                .input(null, amount, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        double amount = Util.parseDouble(input.toString());
                        double original = event.getPayment().getAmountpaid();
                        double difference = amount - original;
                        Payment payment = event.getPayment();
                        payment.setAmountpaid(amount);
                        paymentDao.update(payment);

                        if (!payment.getDescription().equals(getString(R.string.payment)))
                            difference *= -1;

                        updateBalance(difference);
                        loadData();
                    }
                }).build();

        materialDialog.show();*/

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////
        String content = String.format(getString(R.string.payment_amount), currency, Util.formatCurrency(transaction.getBalance()));
        String title = getString(R.string.update_payment);

        boolean wrapInScrollView = true;
        final MaterialDialog materialDialog = new MaterialDialog.Builder(this)
                .title(title)
                .customView(R.layout.payment_dialog, wrapInScrollView)
                .positiveText(R.string.update)
                .neutralText(R.string.delete)
                .negativeText(R.string.cancel)
                .build();
        TextView contentView = materialDialog.getCustomView().findViewById(R.id.content);
        final TextView date = materialDialog.getCustomView().findViewById(R.id.payment_date);
        final TextView amount = materialDialog.getCustomView().findViewById(R.id.payment_amount);
        final TextView note = materialDialog.getCustomView().findViewById(R.id.payment_note);
        contentView.setText(content);
        date.setText(Util.formatDate(event.getPayment().getPaymentdate()));
        amount.setText(event.getPayment().getAmountpaid().toString());
        note.setText(event.getPayment().getNote());
        materialDialog.show();

        materialDialog.getActionButton(DialogAction.NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Payment payment = event.getPayment();
                double original = event.getPayment().getAmountpaid();

                if (payment.getDescription().equals(getString(R.string.payment)))
                    original *= -1;

                paymentDao.delete(payment);
                updateBalance(original);
                loadData();
                materialDialog.dismiss();
            }
        });

        materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String input = amount.getText().toString();

                double amount = Util.parseDouble(input.toString());
                double original = event.getPayment().getAmountpaid();
                double difference = amount - original;
                Payment payment = event.getPayment();
                payment.setAmountpaid(amount);
                paymentDao.update(payment);

                if (!payment.getDescription().equals(getString(R.string.payment)))
                    difference *= -1;

                updateBalance(difference);
                loadData();
                materialDialog.dismiss();
            }
        });
    }

    protected void payDialog(final int action) {
        String content, title, label;

        if (action == R.id.action_penalty) {
            content = String.format(getString(R.string.penalty_amount), currency, Util.formatCurrency(transaction.getBalance()));
            title = getString(R.string.penalty);
            label = getString(R.string.submit);
        } else if (action == R.id.action_topup) {
            content = String.format(getString(R.string.topup_amount), currency, Util.formatCurrency(transaction.getBalance()));
            title = getString(R.string.action_topup);
            label = "Top Up";
        } else {
            content = String.format(getString(R.string.payment_amount), currency, Util.formatCurrency(transaction.getBalance()));
            title = getString(R.string.payment);
            label = getString(R.string.pay);
        }

        boolean wrapInScrollView = true;
        final MaterialDialog materialDialog = new MaterialDialog.Builder(this)
                .title(title)
                .customView(R.layout.payment_dialog, wrapInScrollView)
                .positiveText(label)
                .negativeText(R.string.cancel)
                .build();
        TextView contentView = materialDialog.getCustomView().findViewById(R.id.content);
        final TextView date = materialDialog.getCustomView().findViewById(R.id.payment_date);
        final TextView amount = materialDialog.getCustomView().findViewById(R.id.payment_amount);
        final TextView note = materialDialog.getCustomView().findViewById(R.id.payment_note);
        final TextInputLayout til = (TextInputLayout) materialDialog.findViewById(R.id.amount_input_layout);

        contentView.setText(content);
        materialDialog.show();

        materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String input = amount.getText().toString();
                String noteStr = note.getText().toString();
                String dateStr = date.getText().toString();

                double amount = Util.parseDouble(input);


                String description;

                if (action == R.id.action_penalty) {
                    amount *= -1;
                    description = getString(R.string.penalty);
                } else if (action == R.id.action_topup) {
                    amount *= -1;
                    description = getString(R.string.topup);
                } else {
                    description = getString(R.string.payment);

                    if (amount > transaction.getBalance()) {
                        til.setError("Payment amount exceeds balance");
                        return;
                    } else
                        til.setError(null);
                }

                Payment payment = new Payment(null, transactionId, Util.parseDate(dateStr),
                        description, noteStr.trim(), Math.abs(amount));
                paymentDao.insert(payment);
                updateBalance(amount);
                loadData();
                materialDialog.dismiss();
            }
        });

    }

    protected void updateReminder() {
        Reminder reminder = transaction.getReminder();

        if (reminder != null) {
            reminder.setActive(false);
            reminderDao.update(reminder);
        }
    }

    protected void updateBalance(double paid) {

        double balance = transaction.getBalance() - paid;

        if (balance <= 0) {
            transaction.setPaid(true);
            updateReminder();
        }

        transaction.setBalance(balance);
        transactionDao.update(transaction);
        transaction.resetPayments();
    }

    @Override
    public void onResume() {
        super.onResume();
        adView.resume();
        loadData();
        EventBus.getDefault().register(this);

        paymentListHeader.setBackgroundColor(Colorful().getPrimaryColor().getColorPack().normal().asInt());

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
    public void onPause() {
        adView.pause();
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        adView.destroy();
        super.onDestroy();
    }

}
