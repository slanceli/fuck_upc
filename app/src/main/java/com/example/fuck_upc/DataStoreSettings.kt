package com.example.fuck_upc

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

object Constants {
    const val DATA_STORE_NAME = "param"
    val WXKEY = stringPreferencesKey("wxkey")
    val CDSTRING = stringPreferencesKey("cdstring")
    val DATE = stringPreferencesKey("date")
}

object DataStoreSettings {
    // 创建DataStore
    private val MainActivity.dataStore: DataStore<Preferences> by preferencesDataStore(Constants.DATA_STORE_NAME)
    // 对外开放的DataStore变量
    val dataStore = MainActivity.instance.dataStore
}