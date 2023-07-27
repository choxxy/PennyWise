package com.iogarage.ke.pennywise.views.lockscreenalarm

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.iogarage.ke.pennywise.databinding.ActivityLockScreenAlarmBinding
import com.iogarage.ke.pennywise.ext.activateLockScreen
import com.iogarage.ke.pennywise.ext.deactivateLockScreen


class ActivityLockScreenAlarm : AppCompatActivity() {

    private lateinit var binding: ActivityLockScreenAlarmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityLockScreenAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val a = intent.getStringExtra("SmplrText")
        binding.textView.append(a)

        activateLockScreen()
    }

    override fun onDestroy() {
        super.onDestroy()
        deactivateLockScreen()
    }
}