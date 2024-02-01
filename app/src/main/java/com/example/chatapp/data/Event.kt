package com.example.chatapp.data

import androidx.activity.result.contract.ActivityResultContracts

open class Event <out T>(private  val content: T){
    var hasBeenHandled = false
        private set

    fun getContentorNull(): T? {
        return if ( hasBeenHandled)
            null
        else
        {
            hasBeenHandled = true
            content
        }
    }
}