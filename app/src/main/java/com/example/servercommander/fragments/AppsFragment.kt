package com.example.servercommander.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.example.servercommander.R
import com.example.servercommander.SshConnection
import com.example.servercommander.databinding.FragmentDockerBinding
import com.example.servercommander.databinding.FragmentYunohostBinding

class AppsFragment : Fragment() {

    private var _binding: ViewBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPref: SharedPreferences
    private lateinit var sshConnection: SshConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = requireActivity().getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val serverType = sharedPref.getString("server_type", "")
        _binding =
            when (serverType) {
                "yunohost" -> FragmentYunohostBinding.inflate(inflater, container, false)
                "docker" -> FragmentDockerBinding.inflate(inflater, container, false)
                else -> null
            }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if( !(::sshConnection.isInitialized or sharedPref.getBoolean(getString(R.string.connectionTested), false)))
        {
            Toast.makeText(context, "You need to test your connection first. Please click red server icon at the HOME tab", Toast.LENGTH_LONG).show()
            TODO("Disable buttons")
        }
        else
        {
            if (sharedPref.contains(getString(R.string.server_url)) and
                sharedPref.contains(getString(R.string.username)) and
                sharedPref.contains(getString(R.string.pubkey)) and
                sharedPref.contains(getString(R.string.connectionTested))) {

                sshConnection = SshConnection(
                    sharedPref.getString(getString(R.string.server_url), "").toString(),
                    22,
                    sharedPref.getString(getString(R.string.username), "").toString(),
                    sharedPref.getString(getString(R.string.pubkey), "").toString()
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()

        TODO("Something")
    }
}