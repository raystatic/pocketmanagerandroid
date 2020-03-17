package com.example.pocketmanager.utils

import android.content.Context
import android.widget.Toast

class Utility {
    companion object {
        fun showToast(context: Context, msg: String) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }
}