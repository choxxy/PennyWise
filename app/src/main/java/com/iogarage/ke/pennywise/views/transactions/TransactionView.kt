package com.iogarage.ke.pennywise.views.transactions

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.iogarage.ke.pennywise.R
import com.iogarage.ke.pennywise.databinding.FragmentTransactionBinding
import com.iogarage.ke.pennywise.util.ViewUtil
import com.iogarage.ke.pennywise.util.asString
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.util.Calendar


@AndroidEntryPoint
class TransactionView : Fragment() {

    private val transactionViewModel: TransactionViewModel by viewModels()
    private lateinit var binding: FragmentTransactionBinding
    private var displayName: String? = null
    private var phoneNumber: String? = null
    private lateinit var mCalendar: Calendar
    private var calendar: Calendar = Calendar.getInstance()
    private var transactionType = 0

    private var localStartDate: LocalDate = LocalDate.now()
    private var localEndDate: LocalDate = LocalDate.now()

    // Chosen values
    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0
    private var mHour = 0
    private var mMinute = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, com.iogarage.ke.pennywise.R.layout.fragment_transaction, container, false
        )
        //here data must be an instance of the class MarsDataProvider
        binding.lifecycleOwner = this
        binding.viewModel = transactionViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Set up data binding

        localEndDate = localEndDate.plusMonths(1)
        binding.tvStartDate.text = localStartDate.asString()
        binding.tvEndDate.text = localEndDate.asString()
        calendar = Calendar.getInstance()

        /* if (intent.hasExtra("TRANSACTION_ID")) {
             val id: Long = intent.getLongExtra("TRANSACTION_ID", 0)
             transactionViewModel.getTransaction(id)
         }*/


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
            contact
        }

        binding.reminderText.setOnClickListener {
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

    private val contact: Unit
        private get() {

            // Check the SDK version and whether the permission is already granted or not.
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    PERMISSIONS_REQUEST_READ_CONTACTS
                )
            } else {
                showContacts()
            }*/
        }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                showContacts()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Until you grant the permission, we cannot display your contacts.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showContacts() {
        val i = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
        startActivityForResult(i, PICK_CONTACT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_CONTACT && resultCode == Activity.RESULT_OK) {
            val contactUri: Uri? = data?.data
            val cursor: Cursor? =
                contactUri?.let {
                    requireContext().contentResolver.query(
                        it,
                        null,
                        null,
                        null,
                        null
                    )
                }
            cursor?.let {
                it.moveToFirst()
                val number = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val name =
                    it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                phoneNumber = it.getString(number)
                displayName = it.getString(name)
                binding.name.setText(displayName)
                binding.phoneNo.setText(phoneNumber)
                it.close()
            }
        }
    }

    private fun save() {
        transactionViewModel.save()
    }

    private val isValid: Boolean
        private get() {
            val valid = true
            if (binding.name.text?.isEmpty() == true) {
                ViewUtil.showError(requireActivity(), "Contact is required.")
                return false
            }
            if (binding.etxAmount.text.isEmpty()) {
                ViewUtil.showError(requireActivity(), "Amount is required.")
                return false
            }
            if (transactionType == 0) {
                ViewUtil.showError(
                    requireActivity(),
                    "Please select transaction type (Borrow\\Lend)."
                )
                return false
            }
            return valid
        }


    private fun showDatePicker(viewId: Int) {
        val constraintsBuilder =
            CalendarConstraints.Builder()
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
                com.iogarage.ke.pennywise.R.id.tv_end_date -> {
                    transactionViewModel.setEndDate(it)
                }

                com.iogarage.ke.pennywise.R.id.tv_start_date -> {
                    transactionViewModel.setStartDate(it)
                }

                com.iogarage.ke.pennywise.R.id.reminder_text -> {
                    transactionViewModel.setReminderDate(it)
                }
            }
        }

        datePicker.show(childFragmentManager, "datePicker")
    }

    override fun onResume() {
        super.onResume()
        binding.adView.resume()
        /* if (binding.toolBar != null) {
             binding.toolBar.setNavigationIcon(com.iogarage.ke.pennywise.R.drawable.ic_action_back)
             binding.toolBar.setNavigationOnClickListener {
                 setResult(Activity.RESULT_CANCELED, Intent())
                 finish()
             }
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