package com.dolling.view_model.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dolling.modal.InboxModal
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class InboxViewModel(
    application: Application
): AndroidViewModel(application) {

    private val _inboxList = MutableLiveData<List<InboxModal>>()
    val inboxList: LiveData<List<InboxModal>> get() = _inboxList

    fun getInboxListFromFirestore() {
        Firebase.firestore.collection("_inbox")
            .get()
            .addOnSuccessListener { snapshots ->
                val list = arrayListOf<InboxModal>()
                for (document in snapshots.documents) {
                    list.add(document.toObject(InboxModal::class.java)!!)
                }
                _inboxList.value = list
            }
    }

}