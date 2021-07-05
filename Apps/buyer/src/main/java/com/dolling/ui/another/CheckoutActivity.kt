package com.dolling.ui.another

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dolling.adapter.OrderAdapter
import com.dolling.databinding.ActivityCheckoutBinding
import com.dolling.view_model.another.RestaurantViewModel

class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private lateinit var adapterOrder: OrderAdapter

    companion object {
        private lateinit var viewModel: RestaurantViewModel

        fun setViewModel(_viewModel: RestaurantViewModel) {
            viewModel = _viewModel
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setViewOnClickListener()

        setOrderAdapter()

        viewModel.menuList.observe(this, { orderList ->
            if (!orderList.isNullOrEmpty()) {
                adapterOrder.setList(orderList)
                binding.buttonCheckout.visibility = View.VISIBLE
            } else {
                adapterOrder.delList()
                binding.buttonCheckout.visibility = View.GONE
            }
        })

        binding.tvSubTotalAmount.text = "Rp. ${viewModel.countSubTotal()}"
        binding.tvOrderFeeAmount.text = "Rp. 10000"
        binding.tvTotalAmount.text = "Rp. ${viewModel.countSubTotal() + 10000}"
    }

    private fun setViewOnClickListener() {
        binding.buttonAddItem.setOnClickListener {
            finish()
        }

        binding.buttonCancel.setOnClickListener {
            finish()
        }

        binding.buttonCheckout.setOnClickListener {
            Intent(this, OrderInProcessActivity::class.java).apply {
                OrderInProcessActivity.setViewModel(viewModel)
                startActivity(this)
            }
        }

        binding.buttonChange.setOnClickListener {
            Toast.makeText(this, "coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.tvPaymentType.setOnClickListener {
            Toast.makeText(this, "coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.tvCouponSelect.setOnClickListener {
            Toast.makeText(this, "coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setOrderAdapter() {
        adapterOrder = OrderAdapter()
        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(this@CheckoutActivity)
            adapter = adapterOrder
        }
    }
}