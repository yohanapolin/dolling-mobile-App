package com.dolling.view_model.another

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dolling.listeners.FirestoreListener
import com.dolling.modal.MenuLocalModal
import com.dolling.modal.MenuModal
import com.dolling.modal.OrderModal
import com.dolling.modal.OrderedMenuModal
import com.dolling.modal.SellerModal
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RestaurantViewModel(application: Application) : AndroidViewModel(application) {

    private val _seller = MutableLiveData<SellerModal>()
    val seller: LiveData<SellerModal> get() = _seller

    private val _menuList = MutableLiveData<List<MenuLocalModal>>()
    val menuList: LiveData<List<MenuLocalModal>> get() = _menuList

    private val _order = MutableLiveData<OrderModal>()
    val order: LiveData<OrderModal> get() = _order

    private var orderListenerReg: ListenerRegistration? = null

    fun getSellerFirestore(sellerId: String) {
        Firebase.firestore.collection("_seller")
            .document(sellerId)
            .get()
            .addOnSuccessListener {
                _seller.value = it.toObject(SellerModal::class.java)
            }
    }

    fun getMenuListFromFirestore(sellerId: String) {
        Firebase.firestore.collection("_seller")
            .document(sellerId)
            .collection("menu")
            .get()
            .addOnSuccessListener { snapshots ->
                snapshots?.let {
                    val result = arrayListOf<MenuLocalModal>()
                    for (document in it.documents) {
                        val menuModal = document.toObject(MenuModal::class.java)!!
                        result.add(menuModal.convertToLocalModal())
                    }
                    _menuList.value = result
                }
            }
    }

    fun updateQuantityMenu(position: Int, isIncreaseQty: Boolean) {
        val list = _menuList.value

        var quantity = 0

        if (isIncreaseQty)
            quantity = list!![position].quantity + 1
        else {
            if (list!![position].quantity != 0)
                quantity = list[position].quantity - 1
        }

        list[position].quantity = quantity

        _menuList.value = list!!
    }

    fun countSubTotal(): Int {
        var result = 0

        for (menu in _menuList.value!!) {
            result += menu.quantity * menu.price
        }

        return result
    }

    fun createOrderInFirestore(
        userId: String,
        firestoreListener: FirestoreListener
    ) {
        val orderId = System.currentTimeMillis().toString()

        val order = OrderModal(
            id = orderId,
            total_price = countSubTotal(),
            user_id = userId,
            created_at = Timestamp.now(),
            seller_id = _seller.value!!.id,
            store_name = _seller.value!!.store_name,
            item_count = _menuList.value!!.size
        )

        Firebase.firestore.collection("_seller")
            .document(_seller.value!!.id)
            .collection("ongoing_order")
            .document(orderId)
            .set(order)
            .addOnSuccessListener {
                firestoreListener.onSuccessListener(order)
            }
            .addOnFailureListener {
                firestoreListener.onFailureListener(it.localizedMessage!!)
            }
    }

    fun deleteOrder(orderId: String, firestoreListener: FirestoreListener) {
        Firebase.firestore.collection("_seller")
            .document(_seller.value!!.id)
            .collection("ongoing_order")
            .document(orderId)
            .delete()
            .addOnSuccessListener {
                firestoreListener.onSuccessListener(orderId)
            }
            .addOnFailureListener {
                firestoreListener.onFailureListener(it.localizedMessage!!)
            }
    }

    fun createOrderedMenuInFirestore(
        orderId: String,
        firestoreListener: FirestoreListener
    ) {
        val orderedMenuList: List<OrderedMenuModal> = _menuList.value!!.map {
            it.convertToOrderedMenuModal()
        }

        for (orderedMenu in orderedMenuList) {
            Firebase.firestore.collection("_seller")
                .document(_seller.value!!.id)
                .collection("ongoing_order")
                .document(orderId)
                .collection("ordered_menu")
                .document(orderedMenu.id)
                .set(orderedMenu)
        }

        firestoreListener.onSuccessListener(orderId)
    }

    fun createOrderForUser(
        userId: String,
        order: OrderModal,
        firestoreListener: FirestoreListener
    ) {
        Firebase.firestore.collection("_user")
            .document(userId)
            .collection("order")
            .document(order.id)
            .set(order)
            .addOnSuccessListener {
                firestoreListener.onSuccessListener(order)
            }
            .addOnFailureListener {
                firestoreListener.onFailureListener(it.localizedMessage!!)
            }
    }

    fun subscribeForOrderChange(orderId: String) {
        orderListenerReg = Firebase.firestore.collection("_seller")
            .document(_seller.value!!.id)
            .collection("ongoing_order")
            .document(orderId)
            .addSnapshotListener { snapshots, _ ->
                snapshots?.let { documentSnapshot ->
                    _order.value = documentSnapshot.toObject(OrderModal::class.java)
                }
            }
    }

    fun unsubsribeForOrderChange() {
        orderListenerReg?.remove()
    }

    fun dummyActionSetOrderStatus(orderId: String, type: OrderModal.Companion.StatusType) {
        Firebase.firestore.collection("_seller")
            .document(_seller.value!!.id)
            .collection("ongoing_order")
            .document(orderId)
            .update("status", type.value)
    }

    fun getIsRestaurantFavorited(
        userId: String,
        sellerId: String,
        firestoreListener: FirestoreListener
    ) {
        Firebase.firestore.collection("_user")
            .document(userId)
            .collection("favorite")
            .document(sellerId)
            .get()
            .addOnSuccessListener {
                val seller = it.toObject(SellerModal::class.java)
                seller?.let {
                    firestoreListener.onSuccessListener(true)
                }
                if (seller == null)
                    firestoreListener.onSuccessListener(false)
            }
            .addOnFailureListener {
                firestoreListener.onFailureListener(it.localizedMessage!!)
            }
    }

    fun setRestaurantIntoFavorite(
        userId: String,
        sellerId: String,
        firestoreListener: FirestoreListener
    ) {
        Firebase.firestore.collection("_user")
            .document(userId)
            .collection("favorite")
            .document(sellerId)
            .set(mapOf("id" to sellerId))
            .addOnSuccessListener {
                firestoreListener.onSuccessListener("")
            }
            .addOnFailureListener {
                firestoreListener.onFailureListener(it.localizedMessage!!)
            }
    }

    fun deleteRestaurantFromFavorite(
        userId: String,
        sellerId: String,
        firestoreListener: FirestoreListener
    ) {
        Firebase.firestore.collection("_user")
            .document(userId)
            .collection("favorite")
            .document(sellerId)
            .delete()
            .addOnSuccessListener {
                firestoreListener.onSuccessListener("")
            }
            .addOnFailureListener {
                firestoreListener.onFailureListener(it.localizedMessage!!)
            }
    }
}