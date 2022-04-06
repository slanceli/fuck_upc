package com.example.fuck_upc;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainViewModel extends ViewModel {
    private final MutableLiveData<String> datestringLiveData;
    private final MutableLiveData<Boolean> isLoadingLiveData;
    private final MutableLiveData<String> cdstringLiveData;
    private final MutableLiveData<String> wxkeyLiveData;
    private final MutableLiveData<String> backLiveData;

    public MainViewModel() {
        datestringLiveData = new MutableLiveData<>();
        isLoadingLiveData = new MutableLiveData<>();
        cdstringLiveData = new MutableLiveData<>();
        wxkeyLiveData = new MutableLiveData<>();
        backLiveData = new MutableLiveData<>();
    }

    public void submit() {
        isLoadingLiveData.setValue(true);
        String classname = "saasbllclass.CommonFuntion";
        String funname = "MemberOrderfromWx";
        String paytype = "M";
        OkHttpClient okHttpClient = new OkHttpClient();
        String wxkey = wxkeyLiveData.getValue();
        if (wxkey == null) {
            isLoadingLiveData.setValue(false);
            return;
        }
        Map<String, String> m = new HashMap<>();
        m.put("datestring", datestringLiveData.getValue());
        m.put("cdstring", cdstringLiveData.getValue());
        m.put("paytype", paytype);
        JSONObject searchparam = new JSONObject(m);
        RequestBody requestBody = new FormBody.Builder()
                .add("classname", classname)
                .add("funname", funname)
                .add("wxkey", wxkey)
                .add("searchparam", searchparam.toString())
                .build();
        Request request = new Request.Builder()
                .url("http://www.koksoft.com/HomefuntionV2json.aspx")
                .post(requestBody)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                backLiveData.postValue("发送请求失败" + e.getMessage());
                isLoadingLiveData.postValue(false);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.code() != 200) {
                    backLiveData.postValue("网络错误");
                } else {
                    backLiveData.postValue(Objects.requireNonNull(response.body()).string());
                }
                isLoadingLiveData.postValue(false);
            }
        });
    }


    public MutableLiveData<String> getDateStringLiveData() {return datestringLiveData; }
    public MutableLiveData<Boolean> getIsLoadingLiveData() {return isLoadingLiveData; }
    public MutableLiveData<String> getCdstringLiveData() {return cdstringLiveData; }
    public MutableLiveData<String> getWxkeyLiveData() {return wxkeyLiveData; }
    public MutableLiveData<String> getBackLiveData() {return backLiveData; }
}
