package com.example.pocketmanager.utils

import android.content.Context
import android.content.SharedPreferences

const val PRIVATE_MODE = 0
const val PREF_NAME = "pocket_manager_android"

class PrefManager(context: Context) {
    private val pref : SharedPreferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
    private val editor : SharedPreferences.Editor = pref.edit()

    fun saveString(key: String, data: String) {
        editor.putString(key, data)
        editor.commit()
    }

    fun saveBoolean(key: String, data: Boolean) {
        editor.putBoolean(key, data)
        editor.commit()
    }

    fun getBoolean(key: String): Boolean? {
        return pref.getBoolean(key, false)
    }

    fun getString(key: String): String? {
        return pref.getString(key, "")
    }
}