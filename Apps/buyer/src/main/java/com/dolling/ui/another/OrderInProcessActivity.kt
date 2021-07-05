package com.dolling.ui.another

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dolling.databinding.ActivityOrderInProcessBinding
import com.dolling.listeners.FirestoreListener
import com.dolling.modal.OrderModal
import com.dolling.view_model.another.RestaurantViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class OrderInProcessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderInProcessBinding
    private lateinit var orderId: String

    companion object {
        private lateinit var viewModel: RestaurantViewModel

        fun setViewModel(_viewModel: RestaurantViewModel) {
            viewModel = _viewModel
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderInProcessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isLoggedInWithEmail: Boolean
        getSharedPreferences("dolling", Context.MODE_PRIVATE).apply {
            isLoggedInWithEmail = this.getBoolean("is_logged_in_with_email", false)
        }

        val userId =
            if (isLoggedInWithEmail) Firebase.auth.currentUser!!.email!! else Firebase.auth.currentUser!!.phoneNumber!!

        createOrderInFirestore(userId)

        binding.simulateRejectOrder.setOnClickListener {
            viewModel.dummyActionSetOrderStatus(orderId, OrderModal.Companion.StatusType.REJECTED)
        }

        binding.simulateConfirmOrder.setOnClickListener {
            viewModel.dummyActionSetOrderStatus(orderId, OrderModal.Companion.StatusType.CONFIRMED)
        }
    }

    override fun onStop() {
        viewModel.unsubsribeForOrderChange()
        super.onStop()
    }

    private fun createOrderInFirestore(userId: String) {
        viewModel.createOrderInFirestore(userId, object : FirestoreListener {
            override fun onSuccessListener(data: Any) {
                val order = data as OrderModal
                orderId = order.id

                subsribeForOrderChange(userId, order)
            }

            override fun onFailureListener(errorMsg: String) {
                Toast.makeText(this@OrderInProcessActivity, errorMsg, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createOrderedMenuInFirestore(userId: String, order: OrderModal) {
        viewModel.createOrderedMenuInFirestore(orderId, object : FirestoreListener {
            override fun onSuccessListener(data: Any) {
                createOrderForUser(userId, order)
            }

            override fun onFailureListener(errorMsg: String) {
                Toast.makeText(this@OrderInProcessActivity, errorMsg, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createOrderForUser(userId: String, order: OrderModal) {
        viewModel.createOrderForUser(userId, order, object : FirestoreListener {
            override fun onSuccessListener(data: Any) {
                goToMapActivity(order)
            }

            override fun onFailureListener(errorMsg: String) {
                Toast.makeText(this@OrderInProcessActivity, errorMsg, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun subsribeForOrderChange(userId: String, order: OrderModal) {
        binding.simulateConfirmOrder.visibility = View.VISIBLE
        binding.simulateRejectOrder.visibility = View.VISIBLE

        viewModel.subscribeForOrderChange(orderId)

        viewModel.order.observe(this, {
            it?.let {
                if (it.status == OrderModal.Companion.StatusType.CONFIRMED.value)
                    createOrderedMenuInFirestore(userId, it)
                else if (it.status == OrderModal.Companion.StatusType.REJECTED.value) {
                    viewModel.deleteOrder(orderId, object : FirestoreListener {
                        override fun onSuccessListener(data: Any) {
                            Toast.makeText(
                                this@OrderInProcessActivity,
                                "order rejected!",
                                Toast.LENGTH_LONG
                            ).show()
                            finish()
                        }

                        override fun onFailureListener(errorMsg: String) {
                            Toast.makeText(
                                this@OrderInProcessActivity,
                                errorMsg,
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                    })
                }
            }
        })
    }

    private fun goToMapActivity(order: OrderModal) {
        Intent(this, MapActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(MapActivity.EXTRA_SELLER_ID, order.seller_id)
            putExtra(MapActivity.EXTRA_ORDER_ID, order.id)
            startActivity(this)
        }
    }
}