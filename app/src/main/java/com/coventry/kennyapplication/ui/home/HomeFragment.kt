package com.coventry.kennyapplication.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.coventry.kennyapplication.SubmitRecordActivity
import com.coventry.kennyapplication.databinding.FragmentHomeBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {

private var _binding: FragmentHomeBinding? = null
  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentHomeBinding.inflate(inflater, container, false)
    val root: View = binding.root
    val user = Firebase.auth.currentUser

      val buttonUpperBody: Button = binding.buttonUpperBody
      buttonUpperBody.setOnClickListener{
        user?.let {
          val intent = Intent(context, SubmitRecordActivity::class.java)
          intent.putExtra("option", "upper")
          requireActivity().startActivity(intent)
        } ?: run {
          Toast.makeText(requireContext(), "Login your account first.", Toast.LENGTH_LONG).show()
        }
      }
      val buttonLowerBody: Button = binding.buttonLowerBody
      buttonLowerBody.setOnClickListener {
        user?.let {
          val intent = Intent(context, SubmitRecordActivity::class.java)
          intent.putExtra("option", "lower")
          requireActivity().startActivity(intent)
        } ?: run {
          Toast.makeText(requireContext(), "Login your account first.", Toast.LENGTH_LONG).show()
        }
      }

    return root
  }

override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}