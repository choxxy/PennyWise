package com.iogarage.ke.pennywise.views

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.google.android.gms.ads.MobileAds
import com.google.android.material.navigation.NavigationView
import com.iogarage.ke.pennywise.R
import com.iogarage.ke.pennywise.databinding.ActivityPennyMainBinding
import com.iogarage.ke.pennywise.util.Util
import dagger.hilt.android.AndroidEntryPoint
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@AndroidEntryPoint
class PennyMain : BaseActivity(),
    NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityPennyMainBinding
    private lateinit var navController: NavController
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPennyMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this)

        setSupportActionBar(binding.toolBar);
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val navHostFragment = supportFragmentManager
            .findFragmentById(com.iogarage.ke.pennywise.R.id.nav_host_fragment) as NavHostFragment?
        navController = navHostFragment!!.navController
        /* val appBarConfiguration: AppBarConfiguration =
            AppBarConfiguration.Builder(navController.graph)
                .setOpenableLayout(binding.drawerLayout)
                .build()
        setupWithNavController(
            binding.toolBar, navController, appBarConfiguration
        )*/

        NavigationUI.setupActionBarWithNavController(this, navController, binding.drawerLayout)

        setupWithNavController(binding.nvView, navController)

        binding.nvView.setNavigationItemSelectedListener(this)

        // Find our drawer view
        val version = binding.nvView.getHeaderView(0)
            .findViewById<TextView>(com.iogarage.ke.pennywise.R.id.text_version)
        version.text = String.format(
            getString(com.iogarage.ke.pennywise.R.string.version_name),
            Util.getVersionName(this)
        )

        val today = Date()
        val fmt: DateFormat = SimpleDateFormat("d MMM, y", Locale.getDefault())
        title = ""
        binding.toolBar.title = fmt.format(today)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (navController.currentDestination?.id != R.id.homeFragment)
                    navController.navigateUp()
                else
                    binding.drawerLayout.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    public override fun onResume() {
        super.onResume()
        title = ""
    }

    override fun onPause() {
        super.onPause()
    }

    fun DeleteTransaction() {
        /* Transaction t = event.transaction;

        for (Payment p : t.getPayments()) {
            paymentDao.delete(p);
        }

        if (t.getReminder() != null)
            reminderDao.delete(t.getReminder());

        //db upgrade hack
        t.setBalance(0.0);
        debtDao.update(t);

        debtDao.delete(t);*/
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private val TAG = PennyMain::class.java.name
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        binding.drawerLayout.closeDrawers()
        navController.navigate(item.itemId)
        return true
    }
}