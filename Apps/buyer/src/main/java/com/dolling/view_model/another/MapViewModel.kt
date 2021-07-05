package com.dolling.view_model.another

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dolling.listeners.FirestoreListener
import com.dolling.modal.OrderModal
import com.dolling.modal.SellerModal
import com.dolling.utils.Utils
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private var sellerListenerReg: ListenerRegistration? = null

    private var orderListenerReg: ListenerRegistration? = null

    private val _seller = MutableLiveData<SellerModal>()
    val seller: LiveData<SellerModal> get() = _seller

    private val _order = MutableLiveData<OrderModal>()
    val order: LiveData<OrderModal> get() = _order

    private val sellerDummyPoints = Utils.getDummyMovingSellerGeoPoints()

    private var counter = 0

    fun getLiveSellerFromFirestore(sellerId: String) {
        sellerListenerReg = Firebase.firestore.collection("_seller")
            .document(sellerId)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    _seller.value = it.toObject(SellerModal::class.java)
                }
            }
    }

    fun removeSellerListener() {
        sellerListenerReg?.remove()
    }

    fun getLiveOrder(orderId: String, userId: String) {
        orderListenerReg = Firebase.firestore.collection("_user")
            .document(userId)
            .collection("order")
            .document(orderId)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    _order.value = it.toObject(OrderModal::class.java)
                }
            }
    }

    fun removeOrderListener() {
        orderListenerReg?.remove()
    }

    fun simulateMovingSeller() {
        var sellerTemp: SellerModal

        _seller.value!!.apply {
            sellerTemp = SellerModal(
                id,
                available,
                mapOf(
                    "latitude" to sellerDummyPoints[counter].latitude,
                    "longitude" to sellerDummyPoints[counter].longitude
                ),
                rating,
                rating_total,
                store_name,
                store_photo_url
            )
        }

        Firebase.firestore.collection("_seller")
            .document(_seller.value!!.id)
            .set(sellerTemp)

        if (counter == 5) counter = 0
        else counter++
    }

    fun simulateTransactionDone(userId: String) {
        Firebase.firestore.collection("_seller")
            .document(_seller.value!!.id)
            .collection("ongoing_order")
            .document(_order.value!!.id)
            .update("status", OrderModal.Companion.StatusType.DONE.value)

        Firebase.firestore.collection("_user")
            .document(userId)
            .collection("order")
            .document(_order.value!!.id)
            .update("status", OrderModal.Companion.StatusType.DONE.value)
    }

    fun removeOrderFromSeller(userId: String, firestoreListener: FirestoreListener) {
        Firebase.firestore.collection("_seller")
            .document(_seller.value!!.id)
            .collection("ongoing_order")
            .document(_order.value!!.id)
            .delete()
            .addOnSuccessListener {
                getOrderedMenuFromSeller(userId, firestoreListener)
            }
            .addOnFailureListener {
                firestoreListener.onFailureListener(it.localizedMessage!!)
            }
    }

    private fun getOrderedMenuFromSeller(userId: String, firestoreListener: FirestoreListener) {
        Firebase.firestore.collection("_seller")
            .document(_seller.value!!.id)
            .collection("ongoing_order")
            .document(_order.value!!.id)
            .collection("ordered_menu")
            .get()
            .addOnSuccessListener {
                it?.let {
                    var orderedMenuListId = arrayListOf<String>()
                    for (doc in it.documents) {
                        orderedMenuListId.add(doc.id)
                    }
                    removeOrderedMenuFromSeller(userId, orderedMenuListId, firestoreListener)
                }
                if (it == null || it.isEmpty) {
                    firestoreListener.onFailureListener("empty ordered menu!")
                }
            }
    }

    private fun removeOrderedMenuFromSeller(
        userId: String,
        orderedMenuListId: List<String>,
        firestoreListener: FirestoreListener
    ) {
        for (id in orderedMenuListId) {
            Firebase.firestore.collection("_seller")
                .document(_seller.value!!.id)
                .collection("ongoing_order")
                .document(_order.value!!.id)
                .collection("ordered_menu")
                .document(id)
                .delete()
        }
        firestoreListener.onSuccessListener("success")
        changeUserOrderStatusToDone(userId, firestoreListener)
    }

    private fun changeUserOrderStatusToDone(userId: String, firestoreListener: FirestoreListener) {
        Firebase.firestore.collection("_user")
            .document(userId)
            .collection("order")
            .document(_order.value!!.id)
            .update("status", OrderModal.Companion.StatusType.DONE.value)
            .addOnSuccessListener {
                firestoreListener.onSuccessListener("")
            }
            .addOnFailureListener {
                firestoreListener.onFailureListener(it.localizedMessage!!)
            }
    }
}