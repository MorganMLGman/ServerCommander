package com.example.servercommander.fragments

import android.app.AlertDialog
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
import com.example.servercommander.databinding.AlertDialogPasswordBinding
import com.example.servercommander.databinding.AlertDialogUpdatesBinding
import com.example.servercommander.databinding.FragmentSystemBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import kotlin.reflect.KFunction2

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
        val updateButton = binding.updateButton
        val upgradeButton = binding.upgradeButton

        if( !(::sshConnection.isInitialized or sharedPref.getBoolean(getString(R.string.connectionTested), false)))
        {
            rebootButton.isEnabled = false
            updateButton.isEnabled = false
            upgradeButton.isEnabled = false
        }
        else
        {
            rebootButton.isEnabled = true
            updateButton.isEnabled = true
            upgradeButton.isEnabled = true

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

        rebootButton.setOnClickListener {
            if(::sshConnection.isInitialized and sharedPref.getBoolean(getString(R.string.connectionTested), false)) {
                val username = sharedPref.getString(getString(R.string.username), "")!!
                var password = sharedPref.getString("sudo_password", "")!!

                if(password.isEmpty() or ( password == "" )) {
                    password = showPasswordModal(username, ::callUpdate)
                }
                else callReboot(username, password)
            }
            else Toast.makeText(context, "You need to test your connection first. Please click red server icon at the HOME tab", Toast.LENGTH_LONG).show()
        }

        updateButton.setOnClickListener {
            if(::sshConnection.isInitialized and sharedPref.getBoolean(getString(R.string.connectionTested), false)) {
                val username = sharedPref.getString(getString(R.string.username), "")!!
                var password = sharedPref.getString("sudo_password", "")!!

                if(password.isEmpty() or ( password == "" )) {
                    password = showPasswordModal(username, ::callUpdate)
                }
                else callUpdate(username, password)
            }
            else Toast.makeText(context, "You need to test your connection first. Please click red server icon at the HOME tab", Toast.LENGTH_LONG).show()
        }

        upgradeButton.setOnClickListener {
            if(::sshConnection.isInitialized and sharedPref.getBoolean(getString(R.string.connectionTested), false)) {
                val username = sharedPref.getString(getString(R.string.username), "")!!
                var password = sharedPref.getString("sudo_password", "")!!

                if(password.isEmpty() or ( password == "" )) {
                    password = showPasswordModal(username, ::callUpgrade)
                }
                else callUpgrade(username, password)
            }
            else Toast.makeText(context, "You need to test your connection first. Please click red server icon at the HOME tab", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()

        val rebootButton = binding.rebootButton
        val updateButton = binding.updateButton
        val upgradeButton = binding.upgradeButton

        if( !(::sshConnection.isInitialized or sharedPref.getBoolean(getString(R.string.connectionTested), false)))
        {
            rebootButton.isEnabled = false
            updateButton.isEnabled = false
            upgradeButton.isEnabled = false
        }
        else
        {
            rebootButton.isEnabled = true
            updateButton.isEnabled = true
            upgradeButton.isEnabled = true
        }

        if( ::sshConnection.isInitialized )
        {
            if ((sharedPref.getString("serverUrl", "") != sshConnection.serverAddress )
                or  (sharedPref.getString("username", "") != sshConnection.username ))
            {
                val serverUrl = sharedPref.getString("serverUrl", "")!!
                val username = sharedPref.getString("username", "")!!
                val pubkey = sharedPref.getString("pubkey", "")!!
                sshConnection = SshConnection(serverUrl, 22, username, pubkey)
            }
        }


        if(!::sshConnection.isInitialized)
        {
            val serverUrl = sharedPref.getString("serverUrl", "")!!
            val username = sharedPref.getString("username", "")!!
            val pubkey = sharedPref.getString("pubkey", "")!!
            sshConnection = SshConnection(serverUrl, 22, username, pubkey)
        }
    }

    private fun showPasswordModal(username: String, func: KFunction2<String, String, Unit>): String {
        var password = ""

        val inflater = activity?.layoutInflater
        if (inflater != null) {
            val passwordLayout = AlertDialogPasswordBinding.inflate(inflater)

            val builder: AlertDialog.Builder = context.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                val input = passwordLayout.passwordText
                setPositiveButton(
                    "Send"
                ) { _, _ ->
                    password = if(input.text.toString() != "") {
                        input.text.toString()
                    } else ""
                    println(password)
                    func(username, password)
                }
                setNegativeButton(
                    context.getString(R.string.cancelButtonLabel)
                ) { _, _ ->
                    password = ""
                }
                    setView(passwordLayout.root)
                }
            }
            builder.create()?.show()
            return password
        }
        return ""
    }

    private fun callReboot(username: String, password: String) {
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

    private fun callUpdate(username: String, password: String){
        if (password != "")
        {
            val coroutineScope = MainScope()
            coroutineScope.launch {
                val defer = async(Dispatchers.IO) {
                    sshConnection.executeRemoteCommandOneCall("python3 /home/$username/copilot/main.py update check $password")
                }

                val output = defer.await()
                try {
                    val jsonObject = JSONTokener(output).nextValue() as JSONObject
                    val updates: JSONArray = jsonObject.getJSONArray("updates")
                    val updatesArray = Array<String>(updates.length()){
                        i -> updates.getString(i)
                    }

                    val inflater = activity?.layoutInflater
                    if (inflater != null) {
                        val updateLayout = AlertDialogUpdatesBinding.inflate(inflater)

                        val builder: AlertDialog.Builder = context.let {
                            val builder = AlertDialog.Builder(it)
                            var text = ""
                            builder.apply {
                                if(updatesArray.isEmpty()){
                                    setTitle("No updates")
                                    setMessage("No available updates")
                                }
                                else{
                                    updatesArray.forEach { it_string ->
                                        text += it_string + "\n"
                                    }
                                    updateLayout.updatesText.setText(text)
                                    setView(updateLayout.root)
                                }
                                setCancelable(true)
                            }
                        }
                        builder.create()?.show()
                    }
                    else return@launch
                }
                catch ( e: JSONException )
                {
                    Toast.makeText(context, "Check updates command was not successful.", Toast.LENGTH_LONG).show()
                    return@launch
                }
            }
        }
        else Toast.makeText(context, "Please provide valid SUDO password!", Toast.LENGTH_LONG).show()
    }

    private fun callUpgrade(username: String, password: String){
        if (password != "")
        {
            val coroutineScope = MainScope()
            coroutineScope.launch {
                val defer = async(Dispatchers.IO) {
                    sshConnection.executeRemoteCommandOneCall("python3 /home/$username/copilot/main.py update run $password")
                }

                val output = defer.await().trim()

                val builder: AlertDialog.Builder = context.let {
                    val builder = AlertDialog.Builder(it)
                    var text = ""
                    builder.apply {
                        setTitle("Upgrades")
                        setMessage("Number of upgraded packages: $output")
                        setCancelable(true)
                    }
                }
                builder.create()?.show()

            }
        }
        else Toast.makeText(context, "Please provide valid SUDO password!", Toast.LENGTH_LONG).show()
    }
}