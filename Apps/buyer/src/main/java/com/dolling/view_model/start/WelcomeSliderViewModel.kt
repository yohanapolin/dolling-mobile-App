package com.dolling.view_model.start

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.dolling.listeners.FirestoreListener
import com.dolling.modal.BuyerModal
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class WelcomeSliderViewModel(
    application: Application
) : AndroidViewModel(application) {

    fun checkIfAccountAlreadyAvailable(callback: FirestoreListener) {
        Firebase.firestore.collection("_user")
            .whereEqualTo("id", Firebase.auth.currentUser!!.email!!)
            .get()
            .addOnSuccessListener {
                callback.onSuccessListener(it.documents)
            }
            .addOnFailureListener {
                callback.onFailureListener(it.message.toString())
            }
    }

    fun createNewAccountByEmail(callback: FirestoreListener) {
        Firebase.firestore.collection("_user")
            .document(Firebase.auth.currentUser!!.email!!)
            .set(
                BuyerModal(
                    Firebase.auth.currentUser!!.email!!,
                    Firebase.auth.currentUser!!.displayName!!
                )
            )
            .addOnSuccessListener {
                callback.onSuccessListener("hai")
            }
            .addOnFailureListener {
                callback.onFailureListener(it.message.toString())
            }
    }
}