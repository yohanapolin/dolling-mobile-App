package com.dolling.view_model.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dolling.listeners.FirestoreListener
import com.dolling.modal.SellerModal
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FavoriteViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val _favoriteListAvailable = MutableLiveData<List<SellerModal>>()
    val favoriteListAvailable: LiveData<List<SellerModal>> get() = _favoriteListAvailable

    private val _favoriteListNotAvailable = MutableLiveData<List<SellerModal>>()
    val favoriteListNotAvailable: LiveData<List<SellerModal>> get() = _favoriteListNotAvailable

    private val _favoriteListId = MutableLiveData<List<String>>()

    fun getLiveFavoriteListIdFromFirestore(userId: String, firestoreListener: FirestoreListener) {
        Firebase.firestore.collection("_user")
            .document(userId)
            .collection("favorite")
            .get()
            .addOnSuccessListener {
                val tempResult = arrayListOf<String>()
                for (doc in it.documents) {
                    tempResult.add(doc.get("id") as String)
                }
                _favoriteListId.value = tempResult

                if (tempResult.size != 0)
                    getLiveFavoriteListFromFirestore(firestoreListener)
                else
                    firestoreListener.onFailureListener("empty")
            }
    }

    private fun getLiveFavoriteListFromFirestore(firestoreListener: FirestoreListener) {
        val tempResultAvailable = arrayListOf<SellerModal>()
        val tempResultNotAvailable = arrayListOf<SellerModal>()

        for (eachId in _favoriteListId.value!!) {
            if (eachId != _favoriteListId.value!![_favoriteListId.value!!.size - 1])
                Firebase.firestore.collection("_seller")
                    .document(eachId)
                    .get()
                    .addOnSuccessListener {
                        val seller = it.toObject(SellerModal::class.java)!!
                        if (seller.available)
                            tempResultAvailable.add(seller)
                        else tempResultNotAvailable.add(seller)
                    }
            else
                Firebase.firestore.collection("_seller")
                    .document(eachId)
                    .get()
                    .addOnSuccessListener {
                        val seller = it.toObject(SellerModal::class.java)!!
                        if (seller.available)
                            tempResultAvailable.add(seller)
                        else tempResultNotAvailable.add(seller)

                        _favoriteListAvailable.value = tempResultAvailable
                        _favoriteListNotAvailable.value = tempResultNotAvailable

                        firestoreListener.onSuccessListener("")
                    }
        }
    }
}