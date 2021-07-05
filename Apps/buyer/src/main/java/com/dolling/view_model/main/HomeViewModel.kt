package com.dolling.view_model.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dolling.modal.SellerModal
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeViewModel(
    application: Application
) : AndroidViewModel(application) {

    private var sellerListListenerReg: ListenerRegistration? = null

    private val _sellerList = MutableLiveData<List<SellerModal>>()
    val sellerList: LiveData<List<SellerModal>> get() = _sellerList

    fun getLiveSellerListFromFirestore() {
        sellerListListenerReg = Firebase.firestore.collection("_seller")
            .whereEqualTo("available", true)
            .addSnapshotListener { snapshots, _ ->
                snapshots?.let {
                    val result = arrayListOf<SellerModal>()
                    for (document in it.documents) {
                        result.add(document.toObject(SellerModal::class.java)!!)
                    }
                    _sellerList.value = result
                }
            }
    }

    fun removeSellerListener() {
        sellerListListenerReg?.remove()
    }
}