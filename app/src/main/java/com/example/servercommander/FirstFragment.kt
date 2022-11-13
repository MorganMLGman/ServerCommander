package com.example.servercommander

import android.Manifest
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.servercommander.databinding.FragmentFirstBinding
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    val sharedPref = activity?.getSharedPreferences(
        getString(R.string.app_name), Context.MODE_PRIVATE
    )

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)

//        val callback = object : OnBackPressedCallback(true){
//            override fun handleOnBackPressed() {
//                isEnabled = true
//            }
//
//        }
//        requireActivity().onBackPressedDispatcher.addCallback(callback)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.buttonFirst.setOnClickListener {
//
//
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}