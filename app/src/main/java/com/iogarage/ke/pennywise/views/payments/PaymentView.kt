package com.iogarage.ke.pennywise.views.payments

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import coil.transform.CircleCropTransformation
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.getActionButton
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.google.android.gms.ads.AdRequest
import com.google.android.material.textfield.TextInputLayout
import com.iogarage.ke.pennywise.R
import com.iogarage.ke.pennywise.api.ContactManager
import com.iogarage.ke.pennywise.databinding.FragmentPaymentBinding
import com.iogarage.ke.pennywise.domain.entity.Payment
import com.iogarage.ke.pennywise.domain.entity.ReminderStatus
import com.iogarage.ke.pennywise.domain.entity.Transaction
import com.iogarage.ke.pennywise.domain.entity.TransactionType
import com.iogarage.ke.pennywise.domain.entity.TransactionWithPayments
import com.iogarage.ke.pennywise.util.AppPreferences
import com.iogarage.ke.pennywise.util.asDateString
import com.iogarage.ke.pennywise.util.asString
import com.iogarage.ke.pennywise.util.toCurrency
import com.iogarage.ke.pennywise.util.viewBinding
import com.iogarage.ke.pennywise.views.settings.CurrencyPreference
import dagger.hilt.android.AndroidEntryPoint
import de.coldtea.smplr.smplralarm.smplrAlarmCancel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.abs

@AndroidEntryPoint
class PaymentView : Fragment(R.layout.fragment_payment) {

    @Inject
    lateinit var appPreferences: AppPreferences

    private var hasPayments: Boolean = false
    private val binding by viewBinding(FragmentPaymentBinding::bind)
    private val viewModel: PaymentViewModel by viewModels()
    private var transaction: Transaction? = null
    private var currency: String? = null
    private var msg_template: String? = null
    private lateinit var overflowMenu: Menu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.payments.collect { state ->
                    when (state) {
                        PaymentUiState.Loading -> {

                        }

                        is PaymentUiState.TransactionWithPaymentList -> {
                            updateUi(state.transaction)
                        }
                    }

                }
            }
        }
    }

    private fun updateUi(item: TransactionWithPayments) {

        transaction = item.transaction
        hasPayments = item.payments.isNotEmpty()



        if (transaction?.paid == true) {
            overflowMenu.removeItem(R.id.action_pay)
            overflowMenu.removeItem(R.id.action_edit)
        }
        if (transaction?.type == TransactionType.BORROWING)
            overflowMenu.removeItem(R.id.action_message)
        if (hasPayments)
            overflowMenu.removeItem(R.id.action_edit)

        currency = appPreferences.getString(CurrencyPreference.CURRENCY_SYMBOL, "")
        msg_template = appPreferences.getString(getString(R.string.prefTemplate), "")


        var photo: Bitmap? = null
        try {
            photo = ContactManager.getPhoto(requireContext(), item.transaction.phoneNumber)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        if (photo != null) {
            binding.image.load(photo) {
                placeholder(R.drawable.placeholder)
                transformations(CircleCropTransformation())
            }
        }

        binding.loanDetailsLoanAmount.text = transaction?.amount?.toCurrency()
        binding.loanDetailsBalance.text = transaction?.balance?.toCurrency()
        binding.loanDetailsPhoneNoText.text = transaction?.phoneNumber
        binding.personName.text = transaction?.personName

        binding.layoutTransactionNote.noteLayout.visibility =
            if (transaction?.note?.isEmpty() == true)
                View.GONE
            else
                View.VISIBLE

        binding.layoutTransactionNote.note.text = transaction?.note

        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.loanDetailsListLayout.loansDetailsList.layoutManager = layoutManager

        val dataset: List<Payment> = item.payments
        val totals = doubleArrayOf(0.0, 0.0, 0.0)
        for (payment in dataset) {
            if (payment.description.equals(
                    getString(R.string.payment),
                    ignoreCase = true
                )
            ) totals[0] += payment.amountPaid
            if (payment.description.equals(
                    getString(R.string.penalty),
                    ignoreCase = true
                )
            ) totals[1] += payment.amountPaid
            if (payment.description.equals(
                    getString(R.string.topup),
                    ignoreCase = true
                )
            ) totals[2] += payment.amountPaid
        }
        binding.loanDetailsPaid.text = totals[0].toCurrency()
        if (totals[1] != 0.0) {
            binding.loanPenaltyPaid.visibility = View.VISIBLE
            binding.textView8.visibility = View.VISIBLE
            binding.loanPenaltyPaid.text = totals[1].toCurrency()
        } else {
            binding.loanPenaltyPaid.visibility = View.GONE
            binding.textView8.visibility = View.GONE
        }
        if (totals[2] != 0.0) {
            binding.loanTopupAmount.visibility = View.VISIBLE
            binding.textView9.visibility = View.VISIBLE
            binding.loanTopupAmount.text = totals[2].toCurrency()
        } else {
            binding.loanTopupAmount.visibility = View.GONE
            binding.textView9.visibility = View.GONE
        }
        val mAdapter = PaymentAdapter(dataset) {
            paymentSelected(it)
        }
        binding.loanDetailsListLayout.loansDetailsList.adapter = mAdapter
        val payDate = transaction?.payDate?.let { LocalDate.ofEpochDay(it).asString() }
        binding.dueDate.text = payDate
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adRequest = AdRequest.Builder()
            .build()
        binding.adView.loadAd(adRequest)

        // The usage of an interface lets you inject your own implementation
        val menuHost: MenuHost = requireActivity()

        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner
        // and an optional Lifecycle.State (here, RESUMED) to indicate when
        // the menu should be visible
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.pay_menu, menu)

                overflowMenu = menu

                if (transaction?.paid == true) {
                    menu.removeItem(R.id.action_pay)
                    menu.removeItem(R.id.action_edit)
                }
                if (transaction?.type == TransactionType.BORROWING)
                    menu.removeItem(R.id.action_message)
                if (hasPayments)
                    menu.removeItem(R.id.action_edit)

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_pay -> {
                        payDialog(R.id.action_pay)
                        true
                    }

                    R.id.action_topup -> {
                        payDialog(R.id.action_topup)
                        true
                    }

                    R.id.action_penalty -> {
                        payDialog(R.id.action_penalty)
                        true
                    }

                    R.id.action_edit -> {
                        editTransaction()
                        true
                    }

                    R.id.action_message -> {
                        sendMessage()
                        true
                    }


                    else -> false
                }

            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun sendMessage() {
        transaction?.let {
            if (!msg_template!!.isEmpty()) {
                msg_template = msg_template!!.replace(
                    "@name",
                    it.personName.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray().get(0))
                msg_template = msg_template!!.replace(
                    "@amount",
                    currency + " " + it.balance
                )
            }
            val smsIntent = Intent(Intent.ACTION_VIEW)
            smsIntent.type = "vnd.android-dir/mms-sms"
            smsIntent.putExtra("address", it.phoneNumber)
            smsIntent.putExtra("sms_body", msg_template)
            startActivity(Intent.createChooser(smsIntent, "Send SMS reminder"))
        }
    }

    private fun editTransaction() {
        if (hasPayments)
            return
        transaction?.let {
            val direction =
                PaymentViewDirections.actionToTransactionView(it.transactionId)
            findNavController().navigate(direction)
        }
    }

    private fun paymentSelected(payment: Payment) {
        val content = String.format(
            getString(R.string.payment_amount),
            currency,
            transaction?.balance
        )

        val materialDialog: MaterialDialog = MaterialDialog(requireContext())
            .title(R.string.update_payment)
            .customView(R.layout.payment_dialog, scrollable = true)
            .positiveButton(R.string.update)
            .neutralButton(R.string.delete)
            .negativeButton(R.string.cancel)

        val contentView: TextView =
            materialDialog.getCustomView().findViewById(R.id.content)
        val date: TextView =
            materialDialog.getCustomView().findViewById(R.id.payment_date)
        val amount: TextView =
            materialDialog.getCustomView().findViewById(R.id.payment_amount)
        val note: TextView =
            materialDialog.getCustomView().findViewById(R.id.payment_note)
        contentView.text = content
        date.text = payment.paymentDate.asDateString("dd/MM/yyyy")
        amount.text = payment.amountPaid.toString()
        note.text = payment.note
        materialDialog.show()
        materialDialog.getActionButton(WhichButton.NEUTRAL)
            .setOnClickListener {
                var original = payment.amountPaid
                if (payment.description == getString(R.string.payment))
                    original *= -1.0
                viewModel.deletePayment(payment)
                updateBalance(original)
                materialDialog.dismiss()
            }
        materialDialog.getActionButton(WhichButton.POSITIVE)
            .setOnClickListener {
                val input: String = amount.text.toString()
                val amount = input.toDouble()
                val original = payment.amountPaid
                var difference = amount - original
                payment.amountPaid = amount
                viewModel.updatePayment(payment)
                if (payment.description != getString(R.string.payment)) difference *= -1.0
                updateBalance(difference)
                materialDialog.dismiss()
            }
    }

    private fun payDialog(action: Int) {
        val content: String
        val title: String
        val label: String
        when (action) {
            R.id.action_penalty -> {
                content = String.format(
                    getString(R.string.penalty_amount),
                    currency,
                    transaction?.balance
                )
                title = getString(R.string.penalty)
                label = getString(R.string.submit)
            }

            R.id.action_topup -> {
                content = String.format(
                    getString(R.string.topup_amount),
                    currency,
                    transaction?.balance
                )
                title = getString(R.string.action_topup)
                label = "Top Up"
            }

            else -> {
                content = String.format(
                    getString(R.string.payment_amount),
                    currency,
                    transaction?.balance
                )
                title = getString(R.string.payment)
                label = getString(R.string.pay)
            }
        }

        val materialDialog: MaterialDialog = MaterialDialog(requireContext())
            .title(text = title)
            .customView(R.layout.payment_dialog, scrollable = true)
            .positiveButton(text = label) {
                saveData(action, content, it)
            }
            .negativeButton(R.string.cancel) {

            }

        materialDialog.show()
    }

    private fun saveData(action: Int, content: String, materialDialog: MaterialDialog) {
        val contentView: TextView =
            materialDialog.getCustomView().findViewById(R.id.content)
        val date: TextView =
            materialDialog.getCustomView().findViewById(R.id.payment_date)
        val amount: TextView =
            materialDialog.getCustomView().findViewById(R.id.payment_amount)
        val note: TextView =
            materialDialog.getCustomView().findViewById(R.id.payment_note)
        val til: TextInputLayout =
            materialDialog.findViewById(R.id.amount_input_layout) as TextInputLayout
        contentView.text = content


        val input: String = amount.text.toString()
        val noteStr: String = note.text.toString()
        val dateStr: String = date.text.toString()
        var paidAmount = input.toDouble()
        val description: String

        if (action == R.id.action_penalty) {
            paidAmount *= -1.0
            description = getString(R.string.penalty)
        } else if (action == R.id.action_topup) {
            paidAmount *= -1.0
            description = getString(R.string.topup)
        } else {
            description = getString(R.string.payment)
            if (paidAmount > transaction?.balance!!) {
                til.error = "Payment amount exceeds balance"
                return
            } else til.error = null
        }
        val payment = Payment(
            transactionId = transaction?.transactionId!!,
            paymentDate = LocalDate.now().toEpochDay(),
            description = description,
            note = noteStr.trim { it <= ' ' },
            amountPaid = abs(paidAmount)

        )

        viewModel.addPayment(payment)
        updateBalance(paidAmount)
        materialDialog.dismiss()
    }

    private fun updateReminder() {
        transaction?.let {
            smplrAlarmCancel(requireContext()) {
                requestCode { it.alarmId }
            }
        }
    }

    private fun updateBalance(paid: Double) {
        val balance: Double = transaction?.balance?.minus(paid) ?: 0.0
        if (balance <= 0) {
            transaction?.paid = true
            transaction?.reminderStatus = ReminderStatus.OFF
            updateReminder()
        }
        transaction?.balance = balance
        if (transaction != null)
            viewModel.updateTransaction(transaction!!)
    }

    override fun onResume() {
        super.onResume()
        binding.adView.resume()
    }

    override fun onPause() {
        binding.adView.pause()
        super.onPause()
    }

    companion object {
        private const val PAY = 1
        private const val EDIT = 2
    }
}