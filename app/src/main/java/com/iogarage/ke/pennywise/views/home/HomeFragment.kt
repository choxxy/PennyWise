package com.iogarage.ke.pennywise.views.home

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemDragListener
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemSwipeListener
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnListScrollListener
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.material.snackbar.Snackbar
import com.iogarage.ke.pennywise.R
import com.iogarage.ke.pennywise.databinding.FragmentHomeBinding
import com.iogarage.ke.pennywise.domain.entity.Transaction
import com.iogarage.ke.pennywise.domain.entity.TransactionType
import com.iogarage.ke.pennywise.util.AppPreferences
import com.iogarage.ke.pennywise.util.toCurrency
import com.iogarage.ke.pennywise.util.viewBinding
import com.iogarage.ke.pennywise.views.settings.CurrencyPreference.Companion.CURRENCY_SYMBOL
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home), SearchView.OnQueryTextListener {

    @Inject
    lateinit var appPreferences: AppPreferences

    private val viewModel: HomeViewModel by viewModels()
    private val binding by viewBinding(FragmentHomeBinding::bind)
    private lateinit var mAdapter: TransactionAdapter
    private var currency: String? = ""
    private var showPaid = false
    private var borrowed = 0.0
    private var lent = 0.0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.homeUiState.observe(viewLifecycleOwner) { state ->
            updateViewState(state)
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

        mAdapter = TransactionAdapter {
            val direction = HomeFragmentDirections.actionToPaymentView(it)
            findNavController().navigate(direction)
        }

        binding.myRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.myRecyclerView.adapter = mAdapter
        binding.myRecyclerView.swipeListener = onItemSwipeListener
        //binding.myRecyclerView.dragListener = onItemDragListener
        binding.myRecyclerView.scrollListener = onListScrollListener


        setupLayoutBehindItemLayoutOnSwiping(binding.myRecyclerView)
        setupFadeItemLayoutOnSwiping(binding.myRecyclerView)
        setCardViewItemLayoutAndNoDivider(binding.myRecyclerView)
        setupListOrientation(binding.myRecyclerView)
        currency = appPreferences.getString(CURRENCY_SYMBOL, "")
        showPaid = appPreferences.getBoolean(getString(R.string.prefShowPaid), false)

    }

    private fun setCardViewItemLayoutAndNoDivider(list: DragDropSwipeRecyclerView) {
        // In XML: app:item_layout="@layout/list_item_vertical_list_cardview"
        list.itemLayoutId = R.layout.recycle_item
        // In XML: app:divider="@null"
        list.dividerDrawableId = null
    }

    private fun setupListOrientation(list: DragDropSwipeRecyclerView) {
        // It is necessary to set the orientation in code so the list can work correctly
        list.orientation =
            DragDropSwipeRecyclerView.ListOrientation.VERTICAL_LIST_WITH_VERTICAL_DRAGGING
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

    private fun updateViewState(homeUiState: HomeUiState) {

        binding.emptyView.visibility = if (homeUiState.transactions.isEmpty())
            View.VISIBLE else View.GONE

        if (homeUiState.transactions.isNotEmpty()) {

            val sorted = homeUiState.transactions.sortedBy { it.payDate }

            mAdapter.dataSet = sorted

            borrowed = getTotal(sorted, TransactionType.BORROWING)
            lent = getTotal(sorted, TransactionType.LENDING)

        }

        binding.borrowedLabel.text =
            getString(R.string.formatted_amount, currency, borrowed.toCurrency())
        binding.lentLabel.text =
            getString(R.string.formatted_amount, currency, lent.toCurrency())


    }

    override fun onResume() {
        super.onResume()
        binding.adView.resume()
    }

    override fun onPause() {
        binding.adView.pause()
        super.onPause()
    }

    private fun setupLayoutBehindItemLayoutOnSwiping(list: DragDropSwipeRecyclerView) {
        // We set certain properties to show an icon and a background colour behind swiped items
        // In XML: app:behind_swiped_item_icon="@drawable/ic_remove_item"
        list.behindSwipedItemIconDrawableId = R.drawable.ic_remove_item

        // In XML: app:behind_swiped_item_icon_secondary="@drawable/ic_archive_item"
        list.behindSwipedItemIconSecondaryDrawableId = R.drawable.ic_archive_item

        // In XML: app:behind_swiped_item_bg_color="@color/swipeBehindBackground"
        list.behindSwipedItemBackgroundColor =
            ContextCompat.getColor(requireContext(), R.color.swipeBehindBackground)

        // In XML: app:behind_swiped_item_bg_color_secondary="@color/swipeBehindBackgroundSecondary"
        list.behindSwipedItemBackgroundSecondaryColor =
            ContextCompat.getColor(requireContext(), R.color.swipeBehindBackgroundSecondary)

        // In XML: app:behind_swiped_item_icon_margin="@dimen/spacing_normal"
        list.behindSwipedItemIconMargin = resources.getDimension(R.dimen.spacing_normal)

        list.behindSwipedItemLayoutId = R.layout.behind_swiped_vertical_list

        // In XML: app:behind_swiped_item_custom_layout_secondary="@layout/behind_swiped_vertical_list_secondary"
        list.behindSwipedItemSecondaryLayoutId = R.layout.behind_swiped_vertical_list_secondary
    }

    private fun setupFadeItemLayoutOnSwiping(list: DragDropSwipeRecyclerView) {
        // In XML: app:swiped_item_opacity_fades_on_swiping="true/false"
        list.reduceItemAlphaOnSwiping = true
    }

    private val onItemSwipeListener = object : OnItemSwipeListener<Transaction> {
        override fun onItemSwiped(
            position: Int,
            direction: OnItemSwipeListener.SwipeDirection,
            item: Transaction
        ): Boolean {
            when (direction) {
                OnItemSwipeListener.SwipeDirection.RIGHT_TO_LEFT -> onItemSwipedLeft(item, position)
                OnItemSwipeListener.SwipeDirection.LEFT_TO_RIGHT -> onItemSwipedRight(item, position)
                OnItemSwipeListener.SwipeDirection.DOWN_TO_UP -> onItemSwipedUp(item, position)
                OnItemSwipeListener.SwipeDirection.UP_TO_DOWN -> onItemSwipedDown(item, position)
            }

            return false
        }
    }

    private val onListScrollListener = object : OnListScrollListener {
        override fun onListScrollStateChanged(scrollState: OnListScrollListener.ScrollState) {
            // Call commented out to avoid saturating the log
            //Logger.log("List scroll state changed to $scrollState")
        }

        override fun onListScrolled(
            scrollDirection: OnListScrollListener.ScrollDirection,
            distance: Int
        ) {
            // Call commented out to avoid saturating the log
            //Logger.log("List scrolled $distance pixels $scrollDirection")
        }
    }

    private fun onItemSwipedLeft(item: Transaction, position: Int) {
        removeItem(item, position)
    }

    private fun onItemSwipedRight(item: Transaction, position: Int) {
        archiveItem(item, position)
    }

    private fun onItemSwipedUp(item: Transaction, position: Int) {
        //archiveItem(item, position)
    }

    private fun onItemSwipedDown(item: Transaction, position: Int) {
        Log.d("LOGS", "$item (position $position) swiped down")
        //removeItem(item, position)
    }

    private fun removeItem(item: Transaction, position: Int) {
        removeItemFromList(item, position, R.string.itemRemovedMessage)
    }

    private fun archiveItem(item: Transaction, position: Int) {
        removeItemFromList(item, position, R.string.itemArchivedMessage)
    }

    private fun removeItemFromList(item: Transaction, position: Int, stringResourceId: Int) {

        val itemSwipedSnackBar =
            Snackbar.make(
                binding.root,
                getString(stringResourceId, item.personName),
                Snackbar.LENGTH_SHORT
            )
        itemSwipedSnackBar.setAction(getString(R.string.undoCaps)) {
            viewModel.restoreItem(item, position)
        }

        itemSwipedSnackBar.addCallback(object : Snackbar.Callback() {
            override fun onDismissed(snackbar: Snackbar, event: Int) {
                if (event == DISMISS_EVENT_TIMEOUT) {
                    viewModel.deleteItem(item)
                }
            }

            override fun onShown(snackbar: Snackbar) {

            }
        })

        itemSwipedSnackBar.show()
    }


}