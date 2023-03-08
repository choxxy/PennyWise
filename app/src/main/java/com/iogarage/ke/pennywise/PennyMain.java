package com.iogarage.ke.pennywise;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.files.FileMetadata;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.iogarage.ke.pennywise.backup.DropboxClient;
import com.iogarage.ke.pennywise.backup.DropboxDownloadFileTask;
import com.iogarage.ke.pennywise.backup.DropboxUploadFileTask;
import com.iogarage.ke.pennywise.backup.ExportAsyncTask;
import com.iogarage.ke.pennywise.backup.ImportAsyncTask;
import com.iogarage.ke.pennywise.entities.DaoSession;
import com.iogarage.ke.pennywise.entities.Debt;
import com.iogarage.ke.pennywise.entities.DebtDao;
import com.iogarage.ke.pennywise.entities.Payment;
import com.iogarage.ke.pennywise.entities.PaymentDao;
import com.iogarage.ke.pennywise.entities.ReminderDao;
import com.iogarage.ke.pennywise.util.BackUpUtil;
import com.iogarage.ke.pennywise.util.Prefs;
import com.iogarage.ke.pennywise.util.TaskDelegate;
import com.iogarage.ke.pennywise.util.Util;
import com.tapadoo.alerter.Alert;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.greendao.query.QueryBuilder;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import angtrim.com.fivestarslibrary.FiveStarsDialog;
import autodagger.AutoInjector;
import autodagger.AutoSubcomponent;
import io.multimoon.colorful.CAppCompatActivity;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static io.multimoon.colorful.ColorfulKt.Colorful;


@AutoSubcomponent
@AutoInjector
@DaggerScope(PennyMain.class)
public class PennyMain extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = PennyMain.class.getName();

    private static final int REQUEST_EXPORT_FILE = 0x44;
    public static final int REQUEST_OPEN_DOCUMENT = 0x25;
    private static final int GOOGLE_DRIVE = 1;
    private static final int DROP_BOX = 2;

    public FragmentCommunicator fragmentCommunicator;

    TransactionListFragment transactionListFragment;


    @Inject
    DaoSession session;

    private DebtDao debtDao;
    private PaymentDao paymentDao;
    private ReminderDao reminderDao;

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;

    private TextView title;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penny_main);

        ((PennyApp) getApplication())
                .getComponent()
                .plusPennyMainComponent()
                .inject(this);


        // Set a Toolbar to replace the ActionBar.
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);


        // Find our drawer view
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nvView);
        navigationView.setNavigationItemSelectedListener(this);

        TextView version = navigationView.getHeaderView(0).findViewById(R.id.text_version);

        version.setText(String.format(getString(R.string.version_name), Util.getVersionName(this)));


        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we don't
                // want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we don't
                // want anything to happen so we leave this blank

                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();

        title = findViewById(R.id.title);

        Date today = new Date();
        DateFormat fmt = new SimpleDateFormat("d MMM, y", Locale.getDefault());
        setTitle("");
        title.setText(fmt.format(today));

        debtDao = session.getDebtDao();
        reminderDao = session.getReminderDao();
        paymentDao = session.getPaymentDao();


        if (savedInstanceState == null) {
            // showPinScreen();
        }


        FragmentManager fragmentManager = getSupportFragmentManager();
        transactionListFragment = TransactionListFragment.newInstance();
        fragmentManager.beginTransaction().add(R.id.content, transactionListFragment).commit();


        FiveStarsDialog fiveStarsDialog = new FiveStarsDialog(this, "pennywise.ke@gmail.com");
        fiveStarsDialog.setRateText("If you enjoy using PennyWise, would you mind taking  a moment to rate it? It will only take a moment. Thank you for your suppport.")
                .setTitle("Debt Log")
                .setForceMode(false)
                .setUpperBound(2)
                .showAfter(10);
    }


    public void selectDrawerItem(MenuItem menuItem) {

        int itemId = menuItem.getItemId();
        FragmentManager fragmentManager = null;
        switch (itemId) {
            case R.id.home:
                fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content, TransactionListFragment.newInstance()).commit();
                break;
            /*case R.id.summary:
                fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content, SummaryFragment.newInstance()).commit();
                break;*/
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                setTitle(menuItem.getTitle());
                break;
            case R.id.action_backup:
                startBackUp();
                break;
            case R.id.action_restore:
                startRestoration();
                break;

        }


        drawerLayout.closeDrawers();
    }

    private void startRestoration() {

        //1 - Google SAF , 2 - Dropbox
        String index = Prefs.getString(getString(R.string.prefBackUpService), "1");
        int backUpOption = Integer.parseInt(index);

        if (backUpOption == GOOGLE_DRIVE) {
            //use the storage access framework
            Intent openDocument = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            openDocument.addCategory(Intent.CATEGORY_OPENABLE);
            openDocument.setType("application/zip");
            startActivityForResult(openDocument, REQUEST_OPEN_DOCUMENT);
        }

        if (backUpOption == DROP_BOX) {
            //check if we have access to user account
            if (tokenExists()) {
                //we proceed with backup
                dropBoxRestore();
            } else {
                Auth.startOAuth2Authentication(getApplicationContext(), getString(R.string.DROPBOX_APP_KEY));
                SharedPreferences prefs = getSharedPreferences("com.iogarage.ke.pennywise.dropboxintegration"
                        , Context.MODE_PRIVATE);
                prefs.edit().putBoolean("do-backup", true).apply();
            }

        }

    }

    public void onClick(View view) {

        int viewId = view.getId();

        switch (viewId) {
            case R.id.lentContainer:
                fragmentCommunicator.onAction(viewId);
                break;
            case R.id.borrowedContainer:
                fragmentCommunicator.onAction(viewId);
                break;
            default:
                fragmentCommunicator.onAction(0);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public DebtDao getDebtDao() {
        return debtDao;
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        selectDrawerItem(menuItem);
        return false;
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        setTitle("");

        fragmentCommunicator.onAction(0);

        getAccessToken();

    }

    public void getAccessToken() {

        String accessToken = Auth.getOAuth2Token(); //generate Access Token

        if (accessToken != null) {
            //Store accessToken in SharedPreferences
            SharedPreferences prefs = getSharedPreferences("com.iogarage.ke.pennywise.dropboxintegration"
                    , Context.MODE_PRIVATE);
            prefs.edit().putString("access-token", accessToken).apply();

            boolean doBackUp = prefs.getBoolean("do-backup", false);

            boolean doRestore = prefs.getBoolean("do-restore", false);

            if (doBackUp) {
                prefs.edit().putBoolean("do-backup", false).apply();
                dropBoxBackUp();
            }

            if (doRestore) {
                prefs.edit().putBoolean("do-restore", false).apply();
                dropBoxRestore();
            }
        }


    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }


    @Subscribe
    public void DeleteTransaction(DeleteTransaction event) {
        Debt t = event.getDebt();

        for (Payment p : t.getPayments()) {
            paymentDao.delete(p);
        }

        if (t.getReminder() != null)
            reminderDao.delete(t.getReminder());

        //db upgrade hack
        t.setBalance(0.0);
        debtDao.update(t);

        debtDao.delete(t);
    }

    public interface FragmentCommunicator {
        void onAction(int action);
    }

    public void startBackUp() {

        //0 - Google SAF , 1 - Dropbox
        String index = Prefs.getString(getString(R.string.prefBackUpService), "1");
        int backUpOption = Integer.parseInt(index);

        if (backUpOption == GOOGLE_DRIVE)
            selectExportFile();

        if (backUpOption == DROP_BOX) {
            //check if we have access to user account
            if (tokenExists()) {
                //we proceed with backup
                dropBoxBackUp();
            } else {
                Auth.startOAuth2Authentication(getApplicationContext(), getString(R.string.DROPBOX_APP_KEY));
                Prefs.putBoolean("do-restore", true);
            }

        }
    }

    private void dropBoxBackUp() {
        String token = retrieveAccessToken();

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Backing up data");
        dialog.show();

        new DropboxUploadFileTask(this, DropboxClient.getClient(token), new DropboxUploadFileTask.Callback() {
            @Override
            public void onUploadComplete(FileMetadata result) {
                dialog.dismiss();
                String message = "Data has been backed up successfully";
                showInfoAlert("Success!", message);
                EventBus.getDefault().post(new UpdateData());
            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();
                showErrorAlert("Error!", e.getMessage());
            }
        }).execute();
    }

    private void dropBoxRestore() {
        String token = retrieveAccessToken();

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Restoring data");
        dialog.show();

        new DropboxDownloadFileTask(PennyMain.this, DropboxClient.getClient(token), new DropboxDownloadFileTask.CallBack() {
            @Override
            public void onDownloadComplete(File result) {
                dialog.dismiss();
                if (result != null) {
                    EventBus.getDefault().post(new UpdateData());
                    showInfoAlert("Success!", "Data restored. Please restart your app to complete the process.");
                }
            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();
                showErrorAlert("Error!", "Unable to restore backup.");
            }
        }).execute();


    }


    /**
     * Open a chooser for user to pick a file to export to
     */
    private void selectExportFile() {
        Intent createIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        createIntent.setType("application/zip").addCategory(Intent.CATEGORY_OPENABLE);
        String filename = BackUpUtil.buildExportFilename();
        createIntent.putExtra(Intent.EXTRA_TITLE, filename);
        startActivityForResult(createIntent, REQUEST_EXPORT_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        switch (requestCode) {
            case REQUEST_OPEN_DOCUMENT: //this uses the Storage Access Framework
                final int takeFlags = data.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                importFileFromIntent(this, data, new TaskDelegate() {
                    @Override
                    public void onUploadComplete(FileMetadata result) {

                    }

                    @Override
                    public void onError(Exception e) {

                    }

                    @Override
                    public void onDownloadComplete(File result) {

                    }
                });
                getContentResolver().takePersistableUriPermission(data.getData(), takeFlags);
                break;
            case REQUEST_EXPORT_FILE:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        Uri exportUri = data.getData();
                        backUpFile(exportUri);
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void backUpFile(Uri uri) {
        new ExportAsyncTask(this, new TaskDelegate() {
            @Override
            public void onUploadComplete(FileMetadata result) {

            }

            @Override
            public void onError(Exception e) {

            }

            @Override
            public void onDownloadComplete(File result) {

            }
        }).execute(uri);
    }

    private void importFileFromIntent(Activity context, Intent data, TaskDelegate onFinishTask) {
        new ImportAsyncTask(context, onFinishTask).execute(data.getData());
    }

    private boolean tokenExists() {
        SharedPreferences prefs = getSharedPreferences("com.iogarage.ke.pennywise.dropboxintegration", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("access-token", null);
        return accessToken != null;
    }

    private String retrieveAccessToken() {
        //check if ACCESS_TOKEN is stored on previous app launches
        SharedPreferences prefs = getSharedPreferences("com.iogarage.ke.pennywise.dropboxintegration", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("access-token", null);
        if (accessToken == null) {
            Log.d("AccessToken Status", "No token found");
            return null;
        } else {
            //accessToken already exists
            Log.d("AccessToken Status", "Token exists");
            return accessToken;
        }
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class TransactionListFragment extends Fragment implements SearchView.OnQueryTextListener,
            FragmentCommunicator {

        private RecyclerView mRecyclerView;
        private TextView emptyView;
        private TextView amntBorrowed;
        private TextView amntLent;
        private TransactionAdapter mAdapter;
        private String currency = "";
        private SharedPreferences sharedPref;
        private boolean showPaid;
        private double borrowed = 0;
        private double lent = 0;
        List<Debt> dataset = new LinkedList<>();
        private LinearLayout lendContainer, burrowContainer;
        private View divider;
        private boolean showAll = true;
        private int transactionView = 0;
        private AdView adView;
        private static final int TIME_TO_AUTOMATICALLY_DISMISS_ITEM = 3000;
        private RelativeLayout dashBoard;


        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static TransactionListFragment newInstance() {
            TransactionListFragment fragment = new TransactionListFragment();
            return fragment;
        }

        public TransactionListFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);


        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_penny_main, container, false);

            mRecyclerView = rootView.findViewById(R.id.my_recycler_view);
            amntLent = rootView.findViewById(R.id.lentLabel);
            amntBorrowed = rootView.findViewById(R.id.borrowedLabel);
            emptyView = rootView.findViewById(R.id.empty_view);
            dashBoard = rootView.findViewById(R.id.dashboard);

            lendContainer = rootView.findViewById(R.id.lentContainer);
            burrowContainer = rootView.findViewById(R.id.borrowedContainer);

            divider = rootView.findViewById(R.id.divider);

            final FloatingActionButton fab = rootView.findViewById(R.id.fab);

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addTransaction();
                }
            });

            adView = rootView.findViewById(R.id.adView);
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


            return rootView;
        }


        @Override
        public void onSaveInstanceState(Bundle outState) {
            outState.putInt("transactionView", transactionView);
            super.onSaveInstanceState(outState);

        }

        private void addTransaction() {
            startActivity(new Intent(getActivity(), TransactionView.class));
        }

        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            if (savedInstanceState != null)
                transactionView = savedInstanceState.getInt("transactionView", 0);
        }


        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(
                    getActivity());
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(layoutManager);
            //mRecyclerView.setItemAnimator(new DefaultItemAnimator());

            sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

            if (sharedPref != null) {
                currency = sharedPref.getString(getActivity().getString(R.string.prefCurrency), "");
                showPaid = sharedPref.getBoolean(getActivity().getString(R.string.prefShowPaid), false);
            }

            if (!currency.isEmpty()) {
                if (currency.equalsIgnoreCase("146"))
                    currency = "";
            }

            updateView(transactionView);

            mAdapter = new TransactionAdapter(dataset, getActivity());
            mRecyclerView.setAdapter(mAdapter);

            super.onViewCreated(view, savedInstanceState);
        }

        protected double getTotal(List<Debt> dataset, int trxType) {

            double total = 0.0;
            for (Debt trx : dataset) {
                if (trx.getType() == trxType)
                    total += trx.getBalance();
            }

            return total;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            menu.clear();
            inflater.inflate(R.menu.search_menu, menu);
            SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
            searchView.setOnQueryTextListener(this);
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            mAdapter.getFilter().filter(newText);
            return true;
        }


        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            try {
                ((PennyMain) context).fragmentCommunicator = this;
            } catch (ClassCastException e) {
                throw new ClassCastException(context.toString()
                        + " must implement FragmentListener");
            }
        }

        private void updateView(int view) {
            loadData(view);
            amntBorrowed.setText(currency + " " + Util.formatCurrency(borrowed));
            amntLent.setText(currency + " " + Util.formatCurrency(lent));
        }

        private void loadData(int view) {

            transactionView = view;

            QueryBuilder queryBuilder = ((PennyMain) getActivity()).getDebtDao().queryBuilder();

            if (transactionView == R.id.lentContainer) {

                if (showAll) {
                    divider.setVisibility(View.VISIBLE);
                    burrowContainer.setVisibility(View.VISIBLE);
                    transactionView = 0;
                } else {
                    divider.setVisibility(View.GONE);
                    burrowContainer.setVisibility(View.GONE);
                }

                if (showPaid)
                    queryBuilder.where(DebtDao.Properties.Type.eq(TransactionView.LENDING));
                else
                    queryBuilder.where(DebtDao.Properties.Type.eq(TransactionView.LENDING),
                            DebtDao.Properties.Balance.gt(0));
            }

            if (transactionView == R.id.borrowedContainer) {

                if (showAll) {
                    divider.setVisibility(View.VISIBLE);
                    lendContainer.setVisibility(View.VISIBLE);
                    transactionView = 0;
                } else {
                    divider.setVisibility(View.GONE);
                    lendContainer.setVisibility(View.GONE);
                }

                if (showPaid)
                    queryBuilder.where(DebtDao.Properties.Type.eq(TransactionView.BORROWING));
                else
                    queryBuilder.where(DebtDao.Properties.Type.eq(TransactionView.BORROWING),
                            DebtDao.Properties.Balance.gt(0));
            }

            if (transactionView == 0) {

                queryBuilder = ((PennyMain) getActivity()).getDebtDao().queryBuilder();

                if (!showPaid)
                    queryBuilder.where(DebtDao.Properties.Balance.gt(0));

            }

            queryBuilder.orderAsc(DebtDao.Properties.Paydate);
            dataset = queryBuilder.list();

            if (dataset == null)
                return;

            if (dataset.isEmpty())
                emptyView.setVisibility(View.VISIBLE);
            else
                emptyView.setVisibility(View.GONE);

            borrowed = getTotal(dataset, TransactionView.BORROWING);
            lent = getTotal(dataset, TransactionView.LENDING);

        }

        @Override
        public void onAction(int action) {
            //swap status
            showAll = !showAll;
            updateView(action);
            mAdapter.setData(dataset);
        }

        @Override
        public void onResume() {
            super.onResume();
            adView.resume();
            dashBoard.setBackgroundColor(Colorful().getPrimaryColor().getColorPack().normal().asInt());
            EventBus.getDefault().register(this);

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

        @Subscribe
        public void updateData(UpdateData updateData) {
            updateView(0);
        }
    }


    private class UpdateData {
    }
}
