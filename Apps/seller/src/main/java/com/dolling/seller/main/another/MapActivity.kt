package com.dolling.seller.main.another

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dolling.seller.R
import com.dolling.seller.databinding.ActivityMapBinding

class MapActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMapBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
    }
}