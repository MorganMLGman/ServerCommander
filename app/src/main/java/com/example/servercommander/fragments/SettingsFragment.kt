package com.example.servercommander.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.servercommander.MainActivity
import com.example.servercommander.R
import com.example.servercommander.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val clearSettingsButton = binding.clearSettingsButton

        val serverUrlUpdateText = binding.serverUrlUpdateText
        val serverUrlUpdateButton = binding.serverUrlUpdateButton
        val usernameUpdateText = binding.usernameUpdateText
        val usernameUpdateButton = binding.usernameUpdateButton

        val sharedPref = requireActivity().getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE
        )

        serverUrlUpdateText.setText(sharedPref.getString(getString(R.string.server_url), getString(R.string.not_available)))
        usernameUpdateText.setText(sharedPref.getString(getString(R.string.username), getString(R.string.not_available)))

        clearSettingsButton.setOnClickListener{
            with(sharedPref.edit()){
                remove(getString(R.string.server_url))
                remove(getString(R.string.username))
                remove(getString(R.string.pubkey))
                apply()

                requireActivity().finish()
                startActivity(requireActivity().intent)
            }
        }

        serverUrlUpdateButton.setOnClickListener{
            if(serverUrlUpdateText.text.toString().matches(Regex("[A-Za-z0-9.]*")))
            {
                with(sharedPref.edit()){
                    putString(getString(R.string.server_url), serverUrlUpdateText.text.toString())
                    apply()
                }
                Toast.makeText(context, getString(R.string.server_addres_updated), Toast.LENGTH_SHORT).show()
            }
            else
            {
                serverUrlUpdateText.error = getString(R.string.serverUrlError)
            }
        }

        usernameUpdateButton.setOnClickListener{
            if(usernameUpdateText.text.toString().matches(Regex("[A-Za-z0-9.]*")))
            {
                with(sharedPref.edit()){
                    putString(getString(R.string.username), usernameUpdateText.text.toString())
                    apply()
                }
                Toast.makeText(context, getString(R.string.username_updated), Toast.LENGTH_SHORT).show()
            }
            else
            {
                usernameUpdateText.error = getString(R.string.usernameError)
            }
        }
    }
}