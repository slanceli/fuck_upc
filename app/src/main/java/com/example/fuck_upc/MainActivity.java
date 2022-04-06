package com.example.fuck_upc;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    private MainViewModel mainViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViewModel();
        setListener();
        observeLiveData();
    }

    private void initViewModel() {
        ViewModelProvider viewModelProvider = new ViewModelProvider(this);
        mainViewModel = viewModelProvider.get(MainViewModel.class);
    }

    private void setListener() {
        findViewById(R.id.textInputDate).setOnClickListener(view -> {
            long today = MaterialDatePicker.todayInUtcMilliseconds();
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(today);
            calendar.set(Calendar.MONTH, Calendar.DECEMBER);
            long decThisYear = calendar.getTimeInMillis();
            CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder().setEnd(decThisYear);

            MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
            builder.setTitleText(getString(R.string.please_enter_date));
            builder.setSelection(MaterialDatePicker.todayInUtcMilliseconds());
            builder.setCalendarConstraints(constraintsBuilder.build());
            MaterialDatePicker<Long> picker = builder.build();
            //设置确认监听
            picker.addOnPositiveButtonClickListener((view1) -> {
                //保存到viewmodel且显示
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE).format(picker.getSelection());
                EditText editText = findViewById(R.id.textInputDate);
                editText.setText(date);
                mainViewModel.getDateStringLiveData().setValue(date);
            });
            picker.show(getSupportFragmentManager(), picker.toString());
        });
        findViewById(R.id.outlinedButtonSubmit).setOnClickListener(view -> {
            EditText editText = findViewById(R.id.textInputParameter);
            mainViewModel.getCdstringLiveData().setValue(editText.getText().toString());
            EditText editTextWx = findViewById(R.id.textInputWxkey);
            mainViewModel.getWxkeyLiveData().setValue(editTextWx.getText().toString());
            mainViewModel.submit();
        });
    }

    private void observeLiveData() {
        mainViewModel.getIsLoadingLiveData().observe(this, aBoolean -> {
            if (aBoolean) {
                Log.i("isLoading", "isLoading");
                findViewById(R.id.loadingCircul).setVisibility(View.VISIBLE);
                findViewById(R.id.text_view_loading).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.loadingCircul).setVisibility(View.INVISIBLE);
                findViewById(R.id.text_view_loading).setVisibility(View.INVISIBLE);
                Log.i("completeLoading", "completeLoading");
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
                builder.setPositiveButton(R.string.positive, (v1, v2) -> {});
                builder.setNeutralButton(R.string.cancel, (v1,v2) -> {});
                builder.setTitle(R.string.title);
                builder.setMessage(mainViewModel.getBackLiveData().getValue());
                builder.show();
            }
        });
    }
}