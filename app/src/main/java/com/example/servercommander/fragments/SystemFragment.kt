package com.example.servercommander.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.servercommander.R
import com.example.servercommander.SshConnection
import com.example.servercommander.databinding.FragmentSystemBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener

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
        val updateButtom = binding.updateButton

        if( !(::sshConnection.isInitialized or sharedPref.getBoolean(getString(R.string.connectionTested), false)))
        {
            rebootButton.isEnabled = false
            updateButtom.isEnabled = false
        }
        else
        {
            rebootButton.isEnabled = true
            updateButtom.isEnabled = true

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
                    password = showPasswordModal()
                    callReboot(username, password)
                }
                else callReboot(username, password)
            }
            else Toast.makeText(context, "You need to test your connection first. Please click red server icon at the HOME tab", Toast.LENGTH_LONG).show()
        }

        updateButtom.setOnClickListener {
            if(::sshConnection.isInitialized and sharedPref.getBoolean(getString(R.string.connectionTested), false)) {
                val username = sharedPref.getString(getString(R.string.username), "")!!
                var password = sharedPref.getString("sudo_password", "")!!

                if(password.isEmpty() or ( password == "" )) {
                    password = showPasswordModal()
                    callUpdate(username, password)
                }
                else callUpdate(username, password)
            }
            else Toast.makeText(context, "You need to test your connection first. Please click red server icon at the HOME tab", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()

        val rebootButton = binding.rebootButton
        val updateButtom = binding.updateButton

        if( !(::sshConnection.isInitialized or sharedPref.getBoolean(getString(R.string.connectionTested), false)))
        {
            rebootButton.isEnabled = false
            updateButtom.isEnabled = false
        }
        else
        {
            rebootButton.isEnabled = true
            updateButtom.isEnabled = true

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

    private fun showPasswordModal(): String {
        var password = ""
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
        return password
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

                    val builder: AlertDialog.Builder = context.let {
                        val builder = AlertDialog.Builder(it)
                        var text = ""
                        builder.apply {
                            if(updatesArray.isEmpty()){
                                setTitle("No updates")
                                setMessage("No available updates")
                            }
                            else{
                                setTitle("Available updates")
                                updatesArray.forEach { it_string ->
                                    text += it_string + "\n"
                                }
                                val item = TextView(it)
                                item.text = text
                                item.typeface = Typeface.MONOSPACE
                                item.textSize = resources.getDimension(com.intuit.ssp.R.dimen._4ssp)
                                item.setPadding(resources.getDimension(com.intuit.ssp.R.dimen._8ssp).toInt(),
                                    0,
                                    4,
                                    resources.getDimension(com.intuit.ssp.R.dimen._8ssp).toInt())
                                setView(item)
                            }
                            setCancelable(true)
                        }
                    }
                    builder.create()?.show()

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
}