package com.example.servercommander.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.servercommander.Container
import com.example.servercommander.ContainersAdapter
import com.example.servercommander.R
import com.example.servercommander.SshConnection
import com.example.servercommander.databinding.AlertDialogPasswordBinding
import com.example.servercommander.databinding.FragmentDockerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.reflect.KFunction2

class DockerFragment : Fragment() {

    private var _binding: FragmentDockerBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPref: SharedPreferences
    private lateinit var sshConnection: SshConnection
    private lateinit var containers: ArrayList<Container>

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
        _binding = FragmentDockerBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = binding.dockerRecyclerView
        containers = Container.createContainersList(1)
        val adapter = ContainersAdapter(containers)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        if( !(::sshConnection.isInitialized or sharedPref.getBoolean(getString(R.string.connectionTested), false)))
        {
            // TODO: DISABLE BUTTONS
        }
        else
        {
            // TODO: ENABLE BUTTONS
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

        val swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {

            if(::sshConnection.isInitialized and sharedPref.getBoolean(getString(R.string.connectionTested), false)) {
                val username = sharedPref.getString(getString(R.string.username), "")!!
                var password = sharedPref.getString("sudo_password", "")!!

                if(password.isEmpty() or ( password == "" )) {
                    password = showPasswordModal(username, ::callGetContainersData)
                }
                else callGetContainersData(username, password)
            }
            else Toast.makeText(context, "You need to test your connection first. Please click red server icon at the HOME tab", Toast.LENGTH_LONG).show()

            Toast.makeText(context, "Refresh", Toast.LENGTH_SHORT).show()
            containers.clear()
            containers.addAll(Container.createContainersList(1))

            recyclerView.adapter!!.notifyDataSetChanged()
            swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onResume() {
        super.onResume()

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

    override fun onPause() {
        super.onPause()

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

    private fun callGetContainersData(username: String, password: String){
        if (username.isNotEmpty() and password.isNotEmpty()){
            val coroutineScope = MainScope()
            coroutineScope.launch {
                val defer = async(Dispatchers.IO) {
                    sshConnection.executeRemoteCommandOneCall("python3 /home/$username/copilot/main.py docker show $password")
                }

                val output = defer.await()
                println(output)
            }
        }
        else{
            Toast.makeText(context, "Connection with given parameters is not possible.", Toast.LENGTH_LONG).show()
        }
    }

}