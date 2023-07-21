package com.iogarage.ke.pennywise.views.home

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.iogarage.ke.pennywise.R
import com.iogarage.ke.pennywise.databinding.FragmentHomeBinding
import com.iogarage.ke.pennywise.domain.entity.Transaction
import com.iogarage.ke.pennywise.domain.entity.TransactionType
import com.iogarage.ke.pennywise.util.Util
import com.iogarage.ke.pennywise.util.toCurrency
import com.iogarage.ke.pennywise.util.viewBinding
import com.iogarage.ke.pennywise.views.transactions.TransactionAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home), SearchView.OnQueryTextListener {

    private val viewModel: HomeViewModel by viewModels()
    private val binding by viewBinding(FragmentHomeBinding::bind)
    private lateinit var mAdapter: TransactionAdapter
    private var currency: String? = ""
    private var sharedPref: SharedPreferences? = null
    private var showPaid = false
    private var borrowed = 0.0
    private var lent = 0.0
    private var dataset: List<Transaction> = emptyList()
    private var showAll = true
    private var transactionView = 0
    private val TIME_TO_AUTOMATICALLY_DISMISS_ITEM = 3000


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.transaction.observe(viewLifecycleOwner) { transactions ->
            showTransactions(transactions)
        }

        binding.fab.setOnClickListener { addTransaction() }
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

        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.myRecyclerView.layoutManager = layoutManager
        sharedPref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        if (sharedPref != null) {
            currency = sharedPref!!.getString(getString(R.string.prefCurrency), "")
            showPaid = sharedPref!!.getBoolean(getString(R.string.prefShowPaid), false)
        }

        if (currency?.isNotEmpty() == true) {
            if (currency.equals("146", ignoreCase = true)) currency = ""
        }

        updateView(transactionView)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("transactionView", transactionView)
        super.onSaveInstanceState(outState)
    }

    private fun addTransaction() {
        val direction = HomeFragmentDirections.actionToTransactionView(0L)
        findNavController().navigate(direction)
    }

    private fun getTotal(transactions: List<Transaction>, type: TransactionType): Double {
        return transactions.filter { trx -> trx.type == type }.sumOf { it.balance }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.search_menu, menu)
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView?
        searchView?.setOnQueryTextListener(this)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        mAdapter.filter.filter(newText)
        return true
    }

    private fun updateView(view: Int) {
        // loadData(view)
        binding.borrowedLabel.text = currency + " " + Util.formatCurrency(borrowed)
        binding.lentLabel.text = currency + " " + Util.formatCurrency(lent)
    }

    private fun showTransactions(transactions: List<Transaction>) {
        transactionView = 0
        binding.emptyView.visibility = if (transactions.isEmpty())
            View.VISIBLE else View.GONE
        borrowed = getTotal(transactions, TransactionType.BORROWING)
        lent = getTotal(transactions, TransactionType.LENDING)

        mAdapter = TransactionAdapter(transactions) {
            val direction = HomeFragmentDirections.actionToPaymentView(it)
            findNavController().navigate(direction)
        }

        binding.myRecyclerView.adapter = mAdapter

        binding.borrowedLabel.text = currency + " " + borrowed.toCurrency()
        binding.lentLabel.text = currency + " " + lent.toCurrency()
    }

    fun onAction(action: Int) {
        //swap status
        showAll = !showAll
        updateView(action)
        mAdapter.setData(dataset)
    }

    override fun onResume() {
        super.onResume()
        binding.adView.resume()
    }

    override fun onPause() {
        binding.adView.pause()
        super.onPause()
    }

    override fun onDestroy() {
        binding.adView.destroy()
        super.onDestroy()
    }

    /* @Subscribe
     fun updateData(updateData: UpdateData?) {
         updateView(0)
     }*/

}