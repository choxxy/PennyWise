package com.iogarage.ke.pennywise.views.transactions

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.iogarage.ke.pennywise.R
import com.iogarage.ke.pennywise.databinding.FragmentTransactionBinding
import com.iogarage.ke.pennywise.util.asString
import com.permissionx.guolindev.PermissionX
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.util.Calendar


@AndroidEntryPoint
class TransactionView : Fragment() {
    private val transactionViewModel: TransactionViewModel by viewModels()
    private lateinit var binding: FragmentTransactionBinding
    private var displayName: String? = null
    private var phoneNumber: String? = null
    private var canSetAlarm: Boolean = false
    private lateinit var mCalendar: Calendar
    private var calendar: Calendar = Calendar.getInstance()

    private var localStartDate: LocalDate = LocalDate.now()
    private var localEndDate: LocalDate = LocalDate.now()

    // Chosen values
    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0
    private var mHour = 0
    private var mMinute = 0

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                handleResult(result)
            }
        }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {

        } else {
            Toast.makeText(
                requireContext(),
                "Permission denied or forever denied,cannot set alarm",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun canExactAlarmsBeScheduled(): Boolean {
        val alarmManager =
            requireContext().getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true // below API it can always be scheduled
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_transaction, container, false
        )
        //here data must be an instance of the class MarsDataProvider
        binding.lifecycleOwner = this
        binding.viewModel = transactionViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Set up data binding

        /*if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            canSetAlarm = true
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                showDialog()
            } else {
                // first request or forever denied case
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }*/

        localEndDate = localEndDate.plusMonths(1)
        binding.tvStartDate.text = localStartDate.asString()
        binding.tvEndDate.text = localEndDate.asString()
        calendar = Calendar.getInstance()

        // Load an ad into the AdMob banner view.
        val adRequest = AdRequest.Builder()
            .build()

        binding.adView.loadAd(adRequest)

        binding.adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                binding.adView.visibility = View.VISIBLE
            }

            fun onAdFailedToLoad(errorCode: Int) {
                binding.adView.visibility = View.GONE
            }
        }

        mCalendar = Calendar.getInstance()
        mHour = mCalendar.get(Calendar.HOUR_OF_DAY)
        mMinute = mCalendar.get(Calendar.MINUTE)
        mYear = mCalendar.get(Calendar.YEAR)
        mMonth = mCalendar.get(Calendar.MONTH) + 1
        mDay = mCalendar.get(Calendar.DATE)

        binding.getContact.setOnClickListener {
            getContact()
        }

        binding.reminderDate.setOnClickListener {
            showDatePicker(it.id)
        }

        binding.tvEndDate.setOnClickListener {
            showDatePicker(it.id)
        }

        binding.tvStartDate.setOnClickListener {
            showDatePicker(it.id)
        }

        // The usage of an interface lets you inject your own implementation
        val menuHost: MenuHost = requireActivity()

        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner
        // and an optional Lifecycle.State (here, RESUMED) to indicate when
        // the menu should be visible
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.save, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.action_save) {
                    save()
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onPause() {
        binding.adView.pause()
        super.onPause()
    }

    private fun getContact() {
        PermissionX.init(requireActivity())
            .permissions(Manifest.permission.READ_CONTACTS)
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList,
                    "PennyWise needs this permission to access your contacts",
                    "OK",
                    "Cancel"
                )
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    "You need to allow necessary permissions in Settings manually",
                    "OK",
                    "Cancel"
                )
            }
            .request { allGranted, _, deniedList ->
                if (allGranted) {
                    showContacts()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "These permissions are denied: $deniedList",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun showContacts() {
        val contactsIntent =
            Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
        startForResult.launch(contactsIntent)
    }

    private fun handleResult(result: ActivityResult) {
        val intent = result.data
        val contactUri: Uri? = intent?.data
        val cursor: Cursor? =
            contactUri?.let {
                requireContext().contentResolver.query(it, null, null, null, null)
            }
        cursor?.let {
            it.moveToFirst()
            val number = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val name = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            phoneNumber = it.getString(number)
            displayName = it.getString(name)
            binding.name.setText(displayName)
            binding.phoneNo.setText(phoneNumber)
            it.close()
        }
    }

    private fun save() {
        if (transactionViewModel.validInput()) {
            transactionViewModel.save()
            Toast.makeText(
                requireContext(),
                "Saved successfully!",
                Toast.LENGTH_LONG
            ).show()
            findNavController().navigateUp()
        }
    }

    private fun showDatePicker(viewId: Int) {
        val constraintsBuilder =
            CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())
                .setFirstDayOfWeek(Calendar.SUNDAY)

        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(constraintsBuilder.build())
                .build()

        datePicker.addOnNegativeButtonClickListener {

        }

        datePicker.addOnPositiveButtonClickListener {
            when (viewId) {
                R.id.tv_end_date -> {
                    transactionViewModel.setEndDate(it)
                }

                R.id.tv_start_date -> {
                    transactionViewModel.setStartDate(it)
                }

                R.id.reminder_date -> {
                    transactionViewModel.setReminderDate(it)
                }
            }
        }

        datePicker.show(childFragmentManager, "datePicker")
    }

    private fun showDialog() {
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setMessage("Notification blocked")
        alertDialog.setPositiveButton(
            "Grant Permission"
        ) { dialog, _ -> // Responds to click on the action
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val uri: Uri = Uri.fromParts("package", requireContext().packageName, null)
            intent.data = uri
            startActivity(intent)
            dialog.dismiss()
        }
        alertDialog.setNegativeButton(
            "Cancel"
        ) { dialog, _ -> dialog.dismiss() }
        alertDialog.create().show()
    }

    override fun onResume() {
        super.onResume()
        binding.adView.resume()

        /*if (!canExactAlarmsBeScheduled()) {
            AlertDialog.Builder(requireContext())
                .setMessage(
                    getString(
                        R.string.need_permission_to_schedule_alarms,
                        getString(R.string.app_name)
                    )
                ).setPositiveButton(getString(R.string.dialog_ok)) { _: DialogInterface, _: Int ->
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    startActivity(intent)
                }.show()
        }*/
    }

    override fun onDestroy() {
        binding.adView.destroy()
        super.onDestroy()
    }

    companion object {
        // Request code for READ_CONTACTS. It can be any number > 0.
        private const val PERMISSIONS_REQUEST_READ_CONTACTS = 100
        const val LENDING = 2
        const val BORROWING = 1
        const val ALL = -1
        const val PICK_CONTACT = 2016
    }
}