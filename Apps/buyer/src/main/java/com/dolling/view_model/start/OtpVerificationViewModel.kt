package com.dolling.view_model.start

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.dolling.listeners.FirestoreListener
import com.dolling.modal.BuyerModal
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class OtpVerificationViewModel(
    application: Application
) : AndroidViewModel(application) {

    fun checkIfAccountAlreadyAvailable(phoneNumber: String, callback: FirestoreListener) {
        Firebase.firestore.collection("_user")
            .whereEqualTo("id", phoneNumber)
            .get()
            .addOnSuccessListener {
                callback.onSuccessListener(it.documents)
            }
            .addOnFailureListener {
                callback.onFailureListener(it.message.toString())
            }
    }

    fun createNewAccountByPhoneNumber(
        phoneNumber: String,
        name: String,
        callback: FirestoreListener
    ) {
        Firebase.firestore.collection("_user")
            .document(phoneNumber)
            .set(
                BuyerModal(phoneNumber, name)
            )
            .addOnSuccessListener {
                callback.onSuccessListener(name)
            }
            .addOnFailureListener {
                callback.onFailureListener(it.message.toString())
            }
    }
}