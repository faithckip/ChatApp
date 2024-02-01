package com.example.chatapp.util

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.chatapp.Hilt.CAViewModel

@Composable
fun NotificationMessage(vm : CAViewModel) {
    val notifState = vm.popupNotification.value
    val notifMessage = notifState?.getContentorNull()
    if (!notifMessage.isNullOrEmpty())
        Toast.makeText(LocalContext.current, notifMessage, Toast.LENGTH_SHORT).show()

}