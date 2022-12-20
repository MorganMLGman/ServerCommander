package com.doyouhost.servercommander.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.doyouhost.servercommander.R
import com.doyouhost.servercommander.SshConnection
import com.doyouhost.servercommander.databinding.AlertDialogPasswordBinding
import com.doyouhost.servercommander.databinding.AlertDialogUpdatesBinding
import com.doyouhost.servercommander.databinding.FragmentSystemBinding
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
            "ServerCommander", Context.MODE_PRIVATE
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
        val shutdownButton =  binding.buttonShutdown
        val progressBar = binding.progressBar

        if( !(::sshConnection.isInitialized or sharedPref.getBoolean("connectionTested", false)))
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

            if(sharedPref.contains("serverUrl") and
                sharedPref.contains("username") and
                sharedPref.contains("sshPort") and
                sharedPref.contains("pubkey") and
                sharedPref.contains("connectionTested")) {

                sshConnection = SshConnection(
                    sharedPref.getString("serverUrl", "").toString(),
                    sharedPref.getInt("sshPort", 22),
                    sharedPref.getString("username", "").toString(),
                    sharedPref.getString("pubkey", "").toString()
                )
            }
        }

        rebootButton.setOnClickListener {
            if(::sshConnection.isInitialized and sharedPref.getBoolean("connectionTested", false)) {
                val username = sharedPref.getString("username", "")!!
                val password = sharedPref.getString("sudo_password", "")!!

                progressBar.visibility = View.VISIBLE

                if(password.isEmpty() or ( password == "" )) {
                    showPasswordModal(username, ::callUpdate)
                }
                else callReboot(username, password)
            }
            else Toast.makeText(context, "You need to test your connection first. Please click red server icon at the HOME tab", Toast.LENGTH_LONG).show()
        }

        shutdownButton.setOnClickListener {
            if(::sshConnection.isInitialized and sharedPref.getBoolean("connectionTested", false)) {
                val username = sharedPref.getString("username", "")!!
                val password = sharedPref.getString("sudo_password", "")!!

                progressBar.visibility = View.VISIBLE

                if(password.isEmpty() or ( password == "" )) {
                    showPasswordModal(username, ::callShutdown)
                }
                else callShutdown(username, password)
            }
            else Toast.makeText(context, "You need to test your connection first. Please click red server icon at the HOME tab", Toast.LENGTH_LONG).show()
        }

        updateButton.setOnClickListener {
            if(::sshConnection.isInitialized and sharedPref.getBoolean("connectionTested", false)) {
                val username = sharedPref.getString("username", "")!!
                val password = sharedPref.getString("sudo_password", "")!!

                progressBar.visibility = View.VISIBLE

                if(password.isEmpty() or ( password == "" )) {
                    showPasswordModal(username, ::callUpdate)
                }
                else callUpdate(username, password)
            }
            else Toast.makeText(context, "You need to test your connection first. Please click red server icon at the HOME tab", Toast.LENGTH_LONG).show()
        }

        upgradeButton.setOnClickListener {
            if(::sshConnection.isInitialized and sharedPref.getBoolean("connectionTested", false)) {
                val username = sharedPref.getString("username", "")!!
                val password = sharedPref.getString("sudo_password", "")!!

                progressBar.visibility = View.VISIBLE

                if(password.isEmpty() or ( password == "" )) {
                    showPasswordModal(username, ::callUpgrade)
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

        if( !(::sshConnection.isInitialized or sharedPref.getBoolean("connectionTested", false)))
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
                or (sharedPref.getString("username", "") != sshConnection.username )
                or (sharedPref.getInt("sshPort", 22) != sshConnection.serverPort))
            {
                val serverUrl = sharedPref.getString("serverUrl", "")!!
                val username = sharedPref.getString("username", "")!!
                val sshPort = sharedPref.getInt("sshPort", 22)
                val pubkey = sharedPref.getString("pubkey", "")!!
                sshConnection = SshConnection(serverUrl, sshPort, username, pubkey)
            }
        }


        if(!::sshConnection.isInitialized)
        {
            val serverUrl = sharedPref.getString("serverUrl", "")!!
            val username = sharedPref.getString("username", "")!!
            val sshPort = sharedPref.getInt("sshPort", 22)
            val pubkey = sharedPref.getString("pubkey", "")!!
            sshConnection = SshConnection(serverUrl, sshPort, username, pubkey)
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
                    binding.progressBar.visibility = View.GONE
                }
                    setView(passwordLayout.root)
                }
            }
            builder.create()?.show()
            return password
        }
        binding.progressBar.visibility = View.GONE
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
                        putBoolean("connectionTested", false)
                        apply()
                    }
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Reboot command successfully send.", Toast.LENGTH_LONG).show()
                }
                else{
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Reboot command was not successful.", Toast.LENGTH_LONG).show()
                }
            }
        }

        else{
            binding.progressBar.visibility = View.GONE
            Toast.makeText(context, "Please provide valid SUDO password!", Toast.LENGTH_LONG).show()
        }
    }

    private fun callShutdown(username: String, password: String) {
        if (password != "")
        {
            val coroutineScope = MainScope()
            coroutineScope.launch {
                val defer = async(Dispatchers.IO) {
                    sshConnection.executeRemoteCommandOneCall("python3 /home/$username/copilot/main.py shutdown $password")
                }

                val output = defer.await().trim()
                if (output != "False")
                {
                    with(sharedPref.edit()){
                        putBoolean("connectionTested", false)
                        apply()
                    }
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Shutdown command successfully send.", Toast.LENGTH_LONG).show()
                }
                else{
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Shutdown command was not successful.", Toast.LENGTH_LONG).show()
                }
            }
        }

        else{
            binding.progressBar.visibility = View.GONE
            Toast.makeText(context, "Please provide valid SUDO password!", Toast.LENGTH_LONG).show()
        }
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
                        binding.progressBar.visibility = View.GONE
                    }
                    else{
                        binding.progressBar.visibility = View.GONE
                        return@launch
                    }
                }
                catch ( e: JSONException )
                {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Check updates command was not successful.", Toast.LENGTH_LONG).show()
                    return@launch
                }
            }
        }
        else{
            binding.progressBar.visibility = View.GONE
            Toast.makeText(context, "Please provide valid SUDO password!", Toast.LENGTH_LONG).show()
        }
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
                    builder.apply {
                        setTitle("Upgrades")
                        setMessage("Number of upgraded packages: $output")
                        setCancelable(true)
                    }
                }
                builder.create()?.show()
                binding.progressBar.visibility = View.GONE

            }
        }
        else{
            binding.progressBar.visibility = View.GONE
            Toast.makeText(context, "Please provide valid SUDO password!", Toast.LENGTH_LONG).show()
        }
    }
}