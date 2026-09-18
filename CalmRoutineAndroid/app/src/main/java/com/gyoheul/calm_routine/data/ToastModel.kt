package com.gyoheul.calm_routine.data

import android.content.Context
import android.widget.Toast
import com.gyoheul.calm_routine.common.EnumClass

object ToastModel {
    fun showToast(context: Context, toastType: EnumClass.ToastType) {
        showToast(context, context.getString(toastType.stringId), toastType.length)
    }

    fun showToast(context: Context, msg: String, length: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, msg, length).show()
    }
}