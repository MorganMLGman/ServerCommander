package com.example.servercommander.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.servercommander.R
import com.example.servercommander.SshConnection
import com.example.servercommander.databinding.FragmentTerminalBinding
import com.jcraft.jsch.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class TerminalFragment : Fragment() {

    private var _binding: FragmentTerminalBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPref: SharedPreferences
    private lateinit var sshConnection: SshConnection

    private lateinit var session: Session

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = requireActivity().getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE
        )

        if(sharedPref.getBoolean(getString(R.string.connectionTested), false)) {
            if (!::sshConnection.isInitialized) {
                if (sharedPref.contains("serverUrl") and sharedPref.contains("username") and sharedPref.contains("pubkey")) {
                    sshConnection = SshConnection(
                        sharedPref.getString(getString(R.string.server_url), "").toString(),
                        22,
                        sharedPref.getString(getString(R.string.username), "").toString(),
                        sharedPref.getString(getString(R.string.pubkey), "").toString()
                    )
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentTerminalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val terminalText = binding.terminalText
        val commandText = binding.commandText
        val commandSendButton = binding.commandSendButton

        commandSendButton.setOnClickListener{
            if(sharedPref.getBoolean(getString(R.string.connectionTested), false))
            {
                if(::sshConnection.isInitialized)
                {
                    val coroutineScope = MainScope()
                    coroutineScope.launch {
                        val defer = async(Dispatchers.IO) {
                            sshConnection.executeRemoteCommand(commandText.text.toString())
                        }

                        val output = defer.await()
                    }
                }
                else
                {
                    if (sharedPref.contains("serverUrl") and sharedPref.contains("username") and sharedPref.contains("pubkey")) {
                        sshConnection = SshConnection(
                            sharedPref.getString(getString(R.string.server_url), "").toString(),
                            22,
                            sharedPref.getString(getString(R.string.username), "").toString(),
                            sharedPref.getString(getString(R.string.pubkey), "").toString()
                        )
                    }
                    else Toast.makeText(context, "Connection to server is not possible with given settings", Toast.LENGTH_SHORT).show()
                }
            }
            else Toast.makeText(context, "You need to test your connection first. Please click red server icon at the HOME tab", Toast.LENGTH_LONG).show()
        }
    }
}