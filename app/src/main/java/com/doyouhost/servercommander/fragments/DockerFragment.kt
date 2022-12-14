package com.doyouhost.servercommander.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.doyouhost.servercommander.Container
import com.doyouhost.servercommander.ContainersAdapter
import com.doyouhost.servercommander.R
import com.doyouhost.servercommander.SshConnection
import com.doyouhost.servercommander.databinding.AlertDialogContainerStatsBinding
import com.doyouhost.servercommander.databinding.AlertDialogPasswordBinding
import com.doyouhost.servercommander.databinding.FragmentDockerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import kotlin.reflect.KFunction2
import kotlin.reflect.KFunction3

class DockerFragment : Fragment(), ContainersAdapter.OnViewClickListener {

    private var _binding: FragmentDockerBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPref: SharedPreferences
    private lateinit var sshConnection: SshConnection
    private lateinit var containers: ArrayList<Container>

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
        _binding = FragmentDockerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = binding.dockerRecyclerView
        containers = Container.createContainersList(1)
        val adapter = ContainersAdapter(containers, this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        if( !(::sshConnection.isInitialized or sharedPref.getBoolean("connectionTested", false)))
        {
            // TODO: DISABLE BUTTONS
        }
        else
        {
            // TODO: ENABLE BUTTONS
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

        val swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {

            if(::sshConnection.isInitialized and sharedPref.getBoolean("connectionTested", false)) {
                Toast.makeText(context, getString(R.string.refreshing), Toast.LENGTH_SHORT).show()
                val username = sharedPref.getString("username", "")!!
                val password = sharedPref.getString("sudo_password", "")!!

                if(password.isEmpty() or ( password == "" )) {
                    showPasswordModal(username, ::callGetContainersData)
                }
                else callGetContainersData(username, password)
            }
            else
            {
                Toast.makeText(context, "You need to test your connection first. Please click red server icon at the HOME tab", Toast.LENGTH_LONG).show()
                swipeRefreshLayout.isRefreshing = false
            }
        }

        val refreshButton = binding.refreshDocker
        refreshButton.setOnClickListener {
            refreshButton.isClickable = false
            refreshButton.animate().apply {
                duration = 1000
                rotationBy(360f)
            }.withEndAction{
                refreshButton.isClickable = true
            }.start()
            if(::sshConnection.isInitialized and sharedPref.getBoolean("connectionTested", false)) {
                Toast.makeText(context, getString(R.string.refreshing), Toast.LENGTH_SHORT).show()
                val username = sharedPref.getString("username", "")!!
                val password: String = sharedPref.getString("sudo_password", "")!!

                if(password.isEmpty() or ( password == "" )) {
                    showPasswordModal(username, ::callGetContainersData)
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

    private fun showPasswordModal(username: String, container: Container, func: KFunction3<String, String, Container, Unit>): String {
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
                        func(username, password, container)
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

                if ((output != "False") and (output != "{False}")) newContainers = parseContainersData(output)

                if (newContainers.size == 0){
                    newContainers.clear()
                    newContainers.addAll(Container.createContainersList(1))
                    Toast.makeText(context, "Refresh was not successful. Please try again or run  \"python3 /home/$username/copilot/main.py docker show $password\" on your server to check the output", Toast.LENGTH_LONG).show()
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

    private fun callContainerStart(username: String, password: String, container: Container){
        if (username.isNotEmpty() and password.isNotEmpty()){
            val coroutineScope = MainScope()
            coroutineScope.launch {
                val defer = async(Dispatchers.IO) {
                    sshConnection.executeRemoteCommandOneCall("python3 /home/$username/copilot/main.py docker start --container ${container.name} $password")
                }

                val output = defer.await().trim()

                if ( (output != "False") or (output != "{False}"))
                {
                    callGetContainersData(username, password)
                }
                else{
                    Toast.makeText(context, "Start of ${container.name} was unsuccessful", Toast.LENGTH_LONG).show()
                }
            }
        }
        else{
            Toast.makeText(context, "Connection with given parameters is not possible.", Toast.LENGTH_LONG).show()
        }
    }

    private fun callContainerStop(username: String, password: String, container: Container){
        if (username.isNotEmpty() and password.isNotEmpty()){
            val coroutineScope = MainScope()
            coroutineScope.launch {
                val defer = async(Dispatchers.IO) {
                    sshConnection.executeRemoteCommandOneCall("python3 /home/$username/copilot/main.py docker stop --container ${container.name} $password")
                }

                val output = defer.await().trim()

                if ( (output != "False") or (output != "{False}"))
                {
                    callGetContainersData(username, password)
                }
                else{
                    Toast.makeText(context, "Stop of ${container.name} was unsuccessful", Toast.LENGTH_LONG).show()
                }
            }
        }
        else{
            Toast.makeText(context, "Connection with given parameters is not possible.", Toast.LENGTH_LONG).show()
        }
    }

    private fun callContainerRestart(username: String, password: String, container: Container){
        if (username.isNotEmpty() and password.isNotEmpty()){
            val coroutineScope = MainScope()
            coroutineScope.launch {
                val defer = async(Dispatchers.IO) {
                    sshConnection.executeRemoteCommandOneCall("python3 /home/$username/copilot/main.py docker restart --container ${container.name} $password")
                }

                val output = defer.await().trim()

                if ( (output != "False") or (output != "{False}"))
                {
                    callGetContainersData(username, password)
                }
                else{
                    Toast.makeText(context, "Restart of ${container.name} was unsuccessful", Toast.LENGTH_LONG).show()
                }
            }
        }
        else{
            Toast.makeText(context, "Connection with given parameters is not possible.", Toast.LENGTH_LONG).show()
        }
    }

    private fun callContainerStats(username: String, password: String, container: Container) {
        if (username.isNotEmpty() and password.isNotEmpty()){
            val coroutineScope = MainScope()
            coroutineScope.launch {
                val defer = async(Dispatchers.IO) {
                    sshConnection.executeRemoteCommandOneCall("python3 /home/$username/copilot/main.py docker stats --container ${container.name} $password")
                }

                val output = defer.await().trim()

                if ( (output != "False") or (output != "{False}"))
                {
                    try {
                        val map = mutableMapOf<String, String>()
                        val jsonObject = JSONTokener(output).nextValue() as JSONObject

                        map["name"] = jsonObject.getString("name")
                        map["cpu"] = jsonObject.getString("cpu")
                        map["ram"] = jsonObject.getString("ram")
                        map["net_io"] = jsonObject.getString("net_io")
                        map["disk_io"] = jsonObject.getString("disk_io")

                        showContainerStatsAlert(map)
                    }
                    catch ( e: JSONException)
                    {
                        //
                    }
                    catch (e: ClassCastException){
                        //
                    }
                }
                else{
                    Toast.makeText(context, "Stats of ${container.name} not available", Toast.LENGTH_LONG).show()
                }
            }
        }
        else{
            Toast.makeText(context, "Connection with given parameters is not possible.", Toast.LENGTH_LONG).show()
        }
    }

    private fun showContainerStatsAlert(map: Map<String, String>){

        val inflater = activity?.layoutInflater
        if (inflater != null) {
            val statsLayout = AlertDialogContainerStatsBinding.inflate(inflater)
            statsLayout.containerNameValue.text = map["name"]
            statsLayout.containerCpuValue.text = map["cpu"]
            statsLayout.containerRamValue.text = map["ram"]
            statsLayout.containerDiskValue.text = map["disk_io"]
            statsLayout.containerNetValue.text = map["net_io"]

            val builder: AlertDialog.Builder = context.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setView(statsLayout.root)

                    setCancelable(true)
                }
            }
            builder.create()?.show()
        }
        else
        {
            Toast.makeText(context, "Unable to render stats window", Toast.LENGTH_LONG).show()
        }
    }

    private fun parseContainersData(data: String): ArrayList<Container>{
        val output = ArrayList<Container>()

        if (data.isNotEmpty())
        {
            var running = 0
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
                    output.add(Container(name, isRunning, runtime))
                }
                binding.dockerAllContainersTextView.text = items.toString()
                binding.dockerRunningContainersTextView.text = running.toString()
                binding.dockerStoppedContainersTextView.text = (items - running).toString()
            }
            catch ( e: JSONException)
            {
                //
            }
            catch (e: ClassCastException){
                //
            }
        }
        return output
    }

    override fun onRowClickListener(view: View, container: Container) {
        if(::sshConnection.isInitialized and sharedPref.getBoolean("connectionTested", false)) {
            val username = sharedPref.getString("username", "")!!
            val password = sharedPref.getString("sudo_password", "")!!

            if (container.isRunning){
                Toast.makeText(context, getString(R.string.refreshing), Toast.LENGTH_SHORT).show()
                if(password.isEmpty() or ( password == "" )) {
                    showPasswordModal(username, container, ::callContainerStats)
                }
                else callContainerStats(username, password, container)
            }
            else
            {
                Toast.makeText(context, "You cannot check stats of stopped container.", Toast.LENGTH_LONG).show()
            }

        }
        else
        {
            Toast.makeText(context, "You need to test your connection first. Please click red server icon at the HOME tab", Toast.LENGTH_LONG).show()
        }
    }

    override fun onButtonStartClickListener(button: AppCompatImageButton, container: Container) {
        button.isClickable = false
        button.animate().apply {
            duration = 1000
            rotationBy(360f)
        }.withEndAction{

        }.start()
        if(::sshConnection.isInitialized and sharedPref.getBoolean("connectionTested", false)) {
            Toast.makeText(context, getString(R.string.refreshing), Toast.LENGTH_SHORT).show()
            val username = sharedPref.getString("username", "")!!
            val password = sharedPref.getString("sudo_password", "")!!

            if(password.isEmpty() or ( password == "" )) {
                showPasswordModal(username, container, ::callContainerStart)
            }
            else callContainerStart(username, password, container)
        }
        else
        {
            Toast.makeText(context, "You need to test your connection first. Please click red server icon at the HOME tab", Toast.LENGTH_LONG).show()
        }
    }

    override fun onButtonStopClickListener(button: AppCompatImageButton, container: Container) {
        button.isClickable = false
        button.animate().apply {
            duration = 1000
            rotationBy(360f)
        }.withEndAction{

        }.start()
        if(::sshConnection.isInitialized and sharedPref.getBoolean("connectionTested", false)) {
            Toast.makeText(context, getString(R.string.refreshing), Toast.LENGTH_SHORT).show()
            val username = sharedPref.getString("username", "")!!
            val password = sharedPref.getString("sudo_password", "")!!

            if(password.isEmpty() or ( password == "" )) {
                showPasswordModal(username, container, ::callContainerStop)
            }
            else callContainerStop(username, password, container)
        }
        else
        {
            Toast.makeText(context, "You need to test your connection first. Please click red server icon at the HOME tab", Toast.LENGTH_LONG).show()
        }
    }

    override fun onButtonRestartClickListener(button: AppCompatImageButton, container: Container) {
        button.isClickable = false
        button.animate().apply {
            duration = 1000
            rotationBy(360f)
        }.withEndAction{

        }.start()
        if(::sshConnection.isInitialized and sharedPref.getBoolean("connectionTested", false)) {
            Toast.makeText(context, getString(R.string.refreshing), Toast.LENGTH_SHORT).show()
            val username = sharedPref.getString("username", "")!!
            val password = sharedPref.getString("sudo_password", "")!!

            if(password.isEmpty() or ( password == "" )) {
                showPasswordModal(username, container, ::callContainerRestart)
            }
            else callContainerRestart(username, password, container)
        }
        else
        {
            Toast.makeText(context, "You need to test your connection first. Please click red server icon at the HOME tab", Toast.LENGTH_LONG).show()
        }
    }

}