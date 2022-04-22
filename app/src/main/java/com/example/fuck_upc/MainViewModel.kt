package com.example.fuck_upc

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.provider.Settings.Global.getString
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import okhttp3.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.EmptyCoroutineContext

data class InputParam(
    val wxkey: String,
    val cdstring: String,
    val date: String
)

class MainViewModel : ViewModel() {
    private val datestringLiveData: MutableLiveData<String> = MutableLiveData()
    private val cdstringLiveData: MutableLiveData<String> = MutableLiveData()
    private val wxkeyLiveData: MutableLiveData<String> = MutableLiveData()
    private val isLoadingLiveData: MutableLiveData<Boolean> = MutableLiveData()
    private val responseLiveData: MutableLiveData<String> = MutableLiveData()
    lateinit var request: Request
    lateinit var okHttpClient: OkHttpClient
    lateinit var currentDate: String

    private suspend fun setDataStoreFun() {
        DataStoreSettings.dataStore.edit {
            it[Constants.CDSTRING] = cdstringLiveData.value ?: ""
            it[Constants.DATE] = datestringLiveData.value ?: ""
            it[Constants.WXKEY] = wxkeyLiveData.value ?: ""
        }
    }

    fun setDataStore() {
        val coroutineScope = CoroutineScope(EmptyCoroutineContext)
        coroutineScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                setDataStoreFun()
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    suspend fun waitTime() {
        var currentTime = System.currentTimeMillis()
        val targetTime = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").parse(
            datestringLiveData.value + "-08-00-00")
        while (currentTime < targetTime!!.time) {
            currentTime = System.currentTimeMillis()
        }
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        currentDate =sdf.format(currentTime)
    }

    suspend fun startOrdedr() {
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                isLoadingLiveData.postValue(false)
                responseLiveData.postValue(e.toString() + "请求错误")
            }

            override fun onResponse(call: Call, response: Response) {
                isLoadingLiveData.postValue(false)
                response.use {
                    if (!response.isSuccessful) {
                        responseLiveData.postValue("请求错误" + "response is not successful")
                    } else {
                        responseLiveData.postValue(response.body!!.string() + "\n" + currentDate)
                    }
                }
            }
        })
    }

    fun submit() {
        setDataStore()
        isLoadingLiveData.value = true
        @Serializable
        data class ParamJson (
            val datestring: String,
            val cdstring: String,
            val paytype: String
        )
        val jsonString = Json.encodeToString(ParamJson(datestringLiveData.value!!, cdstringLiveData.value!!, "M"))
        Log.i("hhhhh", jsonString)
        val formBody = FormBody.Builder()
            .add("searchparam", jsonString)
            .add("classname", "saasbllclass.CommonFuntion")
            .add("funname", "MemberOrderfromWx")
            .add("wxkey", wxkeyLiveData.value!!)
            .build()
        okHttpClient = OkHttpClient.Builder()
            .callTimeout(100, TimeUnit.SECONDS)
            .connectTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100, TimeUnit.SECONDS)
            .writeTimeout(100, TimeUnit.SECONDS)
            .build()
        request = Request.Builder()
            .url("http://www.koksoft.com/HomefuntionV2json.aspx")
            .method("POST", formBody)
            .header("Accept", "application/json, text/javascript, */*; q=0.01")
            .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            .addHeader("X-Requested-With", "XMLHttpRequest")
            .build()
        val coroutineScope = CoroutineScope(EmptyCoroutineContext)
        coroutineScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.Default) {
                waitTime()
                startOrdedr()
            }
        }
    }

    fun initLiveData() {
        val inputFlow: Flow<InputParam> = DataStoreSettings.dataStore.data.map {
            InputParam(
                it[Constants.WXKEY] ?: "",
                it[Constants.CDSTRING] ?: "",
                it[Constants.DATE] ?: ""
            )
        }
        val coroutineScope = CoroutineScope(EmptyCoroutineContext)
        coroutineScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                datestringLiveData.postValue(inputFlow.first().date)
                cdstringLiveData.postValue(inputFlow.first().cdstring)
                wxkeyLiveData.postValue(inputFlow.first().wxkey)
            }
        }
    }

    fun getDateStringLiveData(): MutableLiveData<String> {
        return datestringLiveData
    }

    fun getCdStringLiveData(): MutableLiveData<String> {
        return cdstringLiveData
    }

    fun getWxKeyLiveData(): MutableLiveData<String> {
        return wxkeyLiveData
    }

    fun getIsLoadingLiveData(): LiveData<Boolean> {
        return isLoadingLiveData
    }

    fun getResponseLiveData(): LiveData<String> {
        return responseLiveData
    }
}