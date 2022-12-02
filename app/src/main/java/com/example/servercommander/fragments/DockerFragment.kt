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
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
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
                Toast.makeText(context, "Refreshing...", Toast.LENGTH_SHORT).show()
                val username = sharedPref.getString(getString(R.string.username), "")!!
                var password = sharedPref.getString("sudo_password", "")!!

                if(password.isEmpty() or ( password == "" )) {
                    password = showPasswordModal(username, ::callGetContainersData)
                }
                else callGetContainersData(username, password)
            }
            else
            {
                Toast.makeText(context, "You need to test your connection first. Please click red server icon at the HOME tab", Toast.LENGTH_LONG).show()
                swipeRefreshLayout.isRefreshing = false
            }
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
                        binding.swipeRefreshLayout.isRefreshing = false
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

        var newContainers = ArrayList<Container>()

        if (username.isNotEmpty() and password.isNotEmpty()){
            val coroutineScope = MainScope()
            coroutineScope.launch {
                val defer = async(Dispatchers.IO) {
                    sshConnection.executeRemoteCommandOneCall("python3 /home/$username/copilot/main.py docker show $password")
                }

                val output = defer.await().trim()

                if (output != "False") newContainers = parseContainersData(output)

                if (newContainers.size == 0){
                    newContainers.clear()
                    newContainers.addAll(Container.createContainersList(1))
                    println("SIZE 0")
                }

                val adapter = binding.dockerRecyclerView.adapter!! as ContainersAdapter
                adapter.updateList(newContainers)

                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
        else{
            Toast.makeText(context, "Connection with given parameters is not possible.", Toast.LENGTH_LONG).show()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun parseContainersData(data: String): ArrayList<Container>{
        val output = ArrayList<Container>()

        if (data.isNotEmpty())
        {
            var running: Int = 0
            try {
                val jsonObject = JSONTokener(data).nextValue() as JSONObject
                val containers: JSONArray = jsonObject.getJSONArray("containers")
                val items: Int = jsonObject.getInt("items")

                for( i: Int in 0 until items)
                {
                    val container = containers.get(i) as JSONObject
                    val name = container.getString("name")
                    val runtime = container.getString("runtime")
                    val isRunning = when(container.getString("state")){
                        "running" -> true
                        else -> false
                    }
                    if (isRunning) running++
                    println("$name $isRunning $runtime")
                    output.add(Container(name, isRunning, runtime))
                }
                binding.dockerAllContainersTextView.text = items.toString()
                binding.dockerRunningContainersTextView.text = running.toString()
                binding.dockerStoppedContainersTextView.text = (items - running).toString()
            }
            catch ( e: JSONException)
            {
                println(e.stackTrace)
            }
        }
        return output
    }

}