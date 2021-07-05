package com.dolling.listeners

interface FirestoreListener {
    fun onSuccessListener(data: Any)

    fun onFailureListener(errorMsg: String)
}