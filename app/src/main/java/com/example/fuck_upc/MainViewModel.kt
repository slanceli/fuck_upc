package com.example.fuck_upc

import android.util.Log
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

    private suspend fun setDataStore() {
        DataStoreSettings.dataStore.edit {
            it[Constants.CDSTRING] = cdstringLiveData.value ?: ""
            it[Constants.DATE] = datestringLiveData.value ?: ""
            it[Constants.WXKEY] = wxkeyLiveData.value ?: ""
        }
    }

    fun submit() {
        val coroutineScope = CoroutineScope(EmptyCoroutineContext)
        coroutineScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                setDataStore()
            }
        }
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
        val okHttpClient = OkHttpClient.Builder()
            .callTimeout(100, TimeUnit.SECONDS)
            .connectTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100, TimeUnit.SECONDS)
            .writeTimeout(100, TimeUnit.SECONDS)
            .build()
        val request = Request.Builder()
            .url("http://www.koksoft.com/HomefuntionV2json.aspx")
            .method("POST", formBody)
            .header("Accept", "application/json, text/javascript, */*; q=0.01")
            .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            .addHeader("X-Requested-With", "XMLHttpRequest")
            .build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                isLoadingLiveData.postValue(false)
                responseLiveData.postValue(e.toString() + "请求错误")
            }

            override fun onResponse(call: Call, response: Response) {
                isLoadingLiveData.postValue(false)
                response.use {
                    if (!response.isSuccessful) {
                        responseLiveData.postValue("请求错误")
                    } else {
                        responseLiveData.postValue(response.body!!.string())
                    }
                }
            }

        })
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