package com.dolling.ui.main.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.dolling.databinding.FragmentProfileBinding
import com.dolling.listeners.FirestoreListener
import com.dolling.modal.BuyerModal
import com.dolling.ui.start.SignInActivity
import com.dolling.view_model.main.ProfileViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isLoggedInWithEmail: Boolean
        requireContext().getSharedPreferences("dolling", Context.MODE_PRIVATE).apply {
            isLoggedInWithEmail = this.getBoolean("is_logged_in_with_email", false)
        }

        viewModel.getProfileInfo(isLoggedInWithEmail, object : FirestoreListener {
            override fun onSuccessListener(data: Any) {
                val buyer = (data as List<DocumentSnapshot>)[0].toObject(BuyerModal::class.java)!!
                binding.textViewId.text = buyer.id
                binding.textViewName.text = buyer.name
            }

            override fun onFailureListener(errorMsg: String) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
            }
        })

        binding.btnSignOut.setOnClickListener {
            Firebase.auth.signOut()
            Intent(context, SignInActivity::class.java).apply {
                this.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                Toast.makeText(context, "GoodBye", Toast.LENGTH_SHORT).show()
                startActivity(this)
            }
        }
    }
}