package com.dolling.view_model.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dolling.modal.OrderModal
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val _inProcessOrderList = MutableLiveData<List<OrderModal>>()
    val inProcessOrderList: LiveData<List<OrderModal>> get() = _inProcessOrderList

    private val _doneOrderList = MutableLiveData<List<OrderModal>>()
    val doneOrderList: LiveData<List<OrderModal>> get() = _doneOrderList

    fun getInProcessOrderList(userId: String) {
        Firebase.firestore.collection("_user")
            .document(userId)
            .collection("order")
            .whereEqualTo("status", OrderModal.Companion.StatusType.CONFIRMED.value)
            .get()
            .addOnSuccessListener {
                val temp = arrayListOf<OrderModal>()
                for (document in it.documents) {
                    temp.add(document.toObject(OrderModal::class.java)!!)
                }
                _inProcessOrderList.value = temp
            }
    }

    fun getDoneOrderList(userId: String) {
        Firebase.firestore.collection("_user")
            .document(userId)
            .collection("order")
            .whereEqualTo("status", OrderModal.Companion.StatusType.DONE.value)
            .get()
            .addOnSuccessListener {
                val temp = arrayListOf<OrderModal>()
                for (document in it.documents) {
                    temp.add(document.toObject(OrderModal::class.java)!!)
                }
                _doneOrderList.value = temp
            }
    }
}