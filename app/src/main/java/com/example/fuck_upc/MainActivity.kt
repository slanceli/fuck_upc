package com.example.fuck_upc

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.Contacts.Data
import android.text.Editable
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import com.example.fuck_upc.databinding.ActivityMainBinding
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.dialog.MaterialDialogs
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val mainActivityViewModel: MainViewModel by viewModels()
    companion object {
        lateinit var instance: MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setObserver()
        mainActivityViewModel.initLiveData()
        setListener()
        Log.i("hhhhh", mainActivityViewModel.getDateStringLiveData().value ?: "122222")
    }

    @SuppressLint("SimpleDateFormat")
    private fun setListener() {
        binding.textInputDate.setOnClickListener {
            val today = MaterialDatePicker.todayInUtcMilliseconds()
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

            calendar.timeInMillis = today
            calendar[Calendar.MONTH] = Calendar.JANUARY
            val janThisYear = calendar.timeInMillis

            calendar.timeInMillis = today
            calendar[Calendar.MONTH] = Calendar.DECEMBER
            val decThisYear = calendar.timeInMillis
            val calendarConstraints = CalendarConstraints.Builder()
                .setStart(janThisYear)
                .setEnd(decThisYear)
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("选择场地日期")
                .setCalendarConstraints(calendarConstraints.build())
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()
            datePicker.addOnPositiveButtonClickListener {
                mainActivityViewModel.getDateStringLiveData().value = SimpleDateFormat("yyyy-MM-dd").format(it)
            }
            datePicker.show(supportFragmentManager, "date")
        }

        binding.outlinedButtonSubmit.setOnClickListener {
            mainActivityViewModel.getCdStringLiveData().value = binding.textInputParameter.text.toString()
            mainActivityViewModel.getWxKeyLiveData().value = binding.textInputWxkey.text.toString()
            mainActivityViewModel.getDateStringLiveData().value = binding.textInputDate.text.toString()
            mainActivityViewModel.submit()
        }
    }

    private fun setObserver() {
        mainActivityViewModel.getDateStringLiveData().observe(this) {
            if (it != "") {
                binding.textInputDate.setText(it)
            }
        }
        mainActivityViewModel.getWxKeyLiveData().observe(this) {
            if (it != "") {
                binding.textInputWxkey.setText(it)
            }
        }
        mainActivityViewModel.getCdStringLiveData().observe(this) {
            if (it != "") {
                binding.textInputParameter.setText(it)
            }
        }
        mainActivityViewModel.getIsLoadingLiveData().observe(this) {
            if (it) {
                //TODO:更新UI
                binding.loadingCircul.visibility = View.VISIBLE
                binding.outlinedButtonSubmit.isEnabled = false
            } else {
                binding.loadingCircul.visibility = View.INVISIBLE
                binding.outlinedButtonSubmit.isEnabled = true
            }
        }
        mainActivityViewModel.getResponseLiveData().observe(this) {
            val materialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
                .setTitle(R.string.title)
                .setPositiveButton(resources.getString(R.string.positive)) { dialog, which -> }
            if (it == "") {
                materialAlertDialogBuilder.setMessage("软件系统错误").show()
            } else {
                materialAlertDialogBuilder.setMessage(it).show()
            }
        }
    }
}