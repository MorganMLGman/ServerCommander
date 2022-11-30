package com.example.servercommander.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.servercommander.R
import com.example.servercommander.SshConnection
import com.example.servercommander.databinding.FragmentTerminalBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class TerminalFragment : Fragment() {

    private var _binding: FragmentTerminalBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPref: SharedPreferences
    private lateinit var sshConnection: SshConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = requireActivity().getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE
        )

        if (sharedPref.contains("serverUrl") and sharedPref.contains("username") and sharedPref.contains("pubkey"))
        {
            val serverUrl = sharedPref.getString("serverUrl", "")!!
            val username = sharedPref.getString("username", "")!!
            val pubkey = sharedPref.getString("pubkey", "")!!
            sshConnection = SshConnection(serverUrl, 22, username, pubkey)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTerminalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val terminalText = binding.terminalText
        val commandText = binding.commandText
        val commandSendButton = binding.commandSendButton

        terminalText.movementMethod = ScrollingMovementMethod()

        if (sharedPref.getBoolean("connectionTested", false)){
            if (::sshConnection.isInitialized){
                val coroutineScope = MainScope()
                coroutineScope.launch {
                    val defer = async(Dispatchers.IO) {
                        if (!sshConnection.isOpen()) {
                            sshConnection.openConnection()
                        }
                    }
                    defer.await()
                }
            }
        }

        commandSendButton.setOnClickListener{

            if (sharedPref.getBoolean("connectionTested", false)){
                if (::sshConnection.isInitialized){
                    val coroutineScope = MainScope()
                    coroutineScope.launch {
                        var text = terminalText.text.toString()
                        val command = commandText.text.toString()
                        val defer = async(Dispatchers.IO) {
                            sshConnection.executeRemoteCommand(command)
                        }
                        val output = defer.await()
                        text += output
                        terminalText.text = text

                        val scrollAmount: Int = terminalText.layout.getLineTop(terminalText.lineCount) - terminalText.height

                        if (scrollAmount > 0)
                            terminalText.scrollTo(0, scrollAmount);
                        else
                            terminalText.scrollTo(0, 0);

                        commandText.setText("")
                    }
                }
                else Toast.makeText(context, "Connection with provided settings is not possible", Toast.LENGTH_LONG).show()
            }
            else Toast.makeText(context, "You need to test your connection first. Please click red server icon at the HOME tab", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()

        if( (sharedPref.getString("serverUrl", "") != sshConnection.serverAddress )
            or  (sharedPref.getString("username", "") != sshConnection.username )   )
        {
            val serverUrl = sharedPref.getString("serverUrl", "")!!
            val username = sharedPref.getString("username", "")!!
            val pubkey = sharedPref.getString("pubkey", "")!!
            sshConnection = SshConnection(serverUrl, 22, username, pubkey)
        }

        if(::sshConnection.isInitialized)
        {
            val coroutineScope = MainScope()
            coroutineScope.launch {
                val defer = async(Dispatchers.IO) {
                    if (!sshConnection.isOpen()) {
                        sshConnection.openConnection()
                    }
                }
                defer.await()
            }
        }
    }

    override fun onPause() {
        super.onPause()

        if (::sshConnection.isInitialized){
            sshConnection.closeConnection()
        }

        val terminalText = binding.terminalText
        terminalText.text = ""
    }

}