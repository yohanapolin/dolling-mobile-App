package com.dolling.view_model.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.dolling.listeners.FirestoreListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    fun getProfileInfo(isLoggedInWithEmail: Boolean, callback: FirestoreListener) {
        val firebaseId = if (isLoggedInWithEmail) Firebase.auth.currentUser!!.email!!
        else Firebase.auth.currentUser!!.phoneNumber!!

        Firebase.firestore.collection("_user")
            .whereEqualTo("id", firebaseId)
            .get()
            .addOnSuccessListener {
                callback.onSuccessListener(it.documents)
            }
            .addOnFailureListener {
                callback.onFailureListener(it.message.toString())
            }
    }
}