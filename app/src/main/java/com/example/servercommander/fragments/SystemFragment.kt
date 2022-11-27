package com.example.servercommander.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.servercommander.R
import com.example.servercommander.SshConnection
import com.example.servercommander.databinding.FragmentSystemBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class SystemFragment : Fragment() {

    private var _binding: FragmentSystemBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPref: SharedPreferences
    private lateinit var sshConnection: SshConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = requireActivity().getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE
        )

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSystemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rebootButton = binding.rebootButton

        if( !(::sshConnection.isInitialized or sharedPref.getBoolean(getString(R.string.connectionTested), false)))
        {
            rebootButton.isEnabled = false
        }

        rebootButton.setOnClickListener {
            if(::sshConnection.isInitialized and sharedPref.getBoolean(getString(R.string.connectionTested), false)) {
                val username = sharedPref.getString(getString(R.string.username), "")!!
                var password = sharedPref.getString("sudo_password", "")!!

                if(password.isEmpty() or ( password == "" )) {
                    val builder: AlertDialog.Builder = context.let {
                        val builder = AlertDialog.Builder(it)
                        builder.apply {
                            val input = EditText(it)
                            input.hint = "Enter SUDO password"
                            input.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                            setView(input)
                            setPositiveButton(
                                "Send"
                            ) { _, _ ->
                                password = if(input.text.toString() != "") {
                                    input.text.toString()
                                } else ""
                                callReboot(username, password)
                            }
                            setNegativeButton(
                                context.getString(R.string.cancelButtonLabel)
                            ) { _, _ ->
                                password = ""
                            }

                            setTitle("Password")
                            setMessage("Please provide SUDO password.")
                        }
                    }
                    builder.create()?.show()
                }
                else callReboot(username, password)
            }
            else Toast.makeText(context, "You need to test your connection first. Please click red server icon at the HOME tab", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()

        val rebootButton = binding.rebootButton
    }

    private fun callReboot(username: String, password: String)
    {
        if (password != "")
        {
            val coroutineScope = MainScope()
            coroutineScope.launch {
                val defer = async(Dispatchers.IO) {
                    sshConnection.executeRemoteCommandOneCall("python3 /home/$username/copilot/main.py reboot $password")
                }

                val output = defer.await().trim()
                if (output != "False")
                {
                    with(sharedPref.edit()){
                        putBoolean(getString(R.string.connectionTested), false)
                        apply()
                    }
                    Toast.makeText(context, "Reboot command successfully send.", Toast.LENGTH_LONG).show()
                }
                else Toast.makeText(context, "Reboot command was not successful.", Toast.LENGTH_LONG).show()
            }
        }
        else Toast.makeText(context, "Please provide valid SUDO password!", Toast.LENGTH_LONG).show()
    }
}