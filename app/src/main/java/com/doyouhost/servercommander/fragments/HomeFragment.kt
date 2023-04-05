package com.doyouhost.servercommander.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.doyouhost.servercommander.NotificationHandler
import com.doyouhost.servercommander.R
import com.doyouhost.servercommander.SshConnection
import com.doyouhost.servercommander.databinding.FragmentHomeBinding
import com.doyouhost.servercommander.viewModels.RefreshViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val refreshViewModel: RefreshViewModel by activityViewModels()

    private lateinit var sharedPref: SharedPreferences
    private lateinit var sshConnection: SshConnection
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = requireActivity().getSharedPreferences(
            "ServerCommander", Context.MODE_PRIVATE
        )

        handler = Handler(Looper.getMainLooper())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val refreshWidget = binding.refreshWidget
        val connectionTest = binding.connectionTest

        refreshWidget.setOnClickListener {
            if(refreshWidget.isClickable)
            {
                refreshWidget.isClickable = false
                refreshDash(auto = false)
            }
        }

        val swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {

            if(::sshConnection.isInitialized and sharedPref.getBoolean("connectionTested", false)) {
                refreshDash(false)
            }
            else
            {
                Toast.makeText(context, "You need to test your connection first. Please click red server icon at the HOME tab", Toast.LENGTH_LONG).show()
                swipeRefreshLayout.isRefreshing = false
            }
        }



        connectionTest.setOnClickListener {
            if(connectionTest.isClickable)
            {
                connectionTest.isClickable = false

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

                    var rotation = true

                    val coroutineScope = MainScope()
                    coroutineScope.launch {
                        val defer = async(Dispatchers.IO) {
                            sshConnection.checkRequirements()
                        }

                        val (output, comment) = defer.await()
                        rotation = false

                        if (output){
                            with(sharedPref.edit()){
                                putBoolean("connectionTested", true)
                                apply()
                            }

                            context?.getColor(R.color.brightGreen)
                                ?.let { it1 -> connectionTest.setColorFilter(it1, android.graphics.PorterDuff.Mode.SRC_IN) }
                            connectionTest.setImageResource(R.drawable.server_network)

                            // Refresh dash after successful connection
                            refreshDash(auto = true)

                            //Update notification
                            context?.let { it1 -> NotificationHandler.updateNotification(it1, sharedPref) }
                        }
                        else
                        {
                            val builder: AlertDialog.Builder = context.let {
                                val builder = AlertDialog.Builder(it)
                                builder.apply {
                                    setCancelable(true)
                                    setTitle("Something went wrong :(")
                                    setMessage(comment.trim())
                                }
                            }
                            builder.create()?.show()

                            context?.getColor(R.color.brightRed)
                                ?.let { it1 -> connectionTest.setColorFilter(it1, android.graphics.PorterDuff.Mode.SRC_IN) }
                            connectionTest.setImageResource(R.drawable.server_network_off)
                        }
                    }

                    fun rotate(){
                        connectionTest.animate().apply {
                            duration = 1000
                            rotationBy(360f)
                        }.withEndAction{
                            if (rotation)  rotate()
                            else connectionTest.isClickable = true
                        }.start()
                    }
                    rotate()
                }
                else
                {
                    Toast.makeText(context, "Connection to server is not possible with given settings", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val connectionTest = binding.connectionTest

        if (sharedPref.getBoolean("connectionTested", false))
        {
            context?.getColor(R.color.brightGreen)
                ?.let { it1 -> connectionTest.setColorFilter(it1, android.graphics.PorterDuff.Mode.SRC_IN) }
            connectionTest.setImageResource(R.drawable.server_network)
        }
        else
        {
            context?.getColor(R.color.brightRed)
                ?.let { it1 -> connectionTest.setColorFilter(it1, android.graphics.PorterDuff.Mode.SRC_IN) }
            connectionTest.setImageResource(R.drawable.server_network_off)
        }

        if(refreshViewModel.enabled.value == true)
        {
            handler.post(autoRefreshRunner)
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

        context?.let { NotificationHandler.updateNotification(it, sharedPref) }
    }

    override fun onPause() {
        super.onPause()

        handler.removeCallbacks(autoRefreshRunner)
    }

    private val autoRefreshRunner = object: Runnable {
        override fun run() {
            refreshDash(auto = true)
            refreshViewModel.interval.value?.let { handler.postDelayed(this, it.toLong() * 1000) }
        }
    }

    private fun refreshDash(auto: Boolean = false){
        val refreshWidget = binding.refreshWidget

        val tempText = binding.dashTemperatureTextView
        val cpuUsage = binding.dashCpuTextView
        val ramUsage = binding.dashRamTextView

        val linuxKernelVersion = binding.kernelInfo.linuxKernelVersion
        val hostname = binding.kernelInfo.hostname
        val uptime = binding.uptimeInfo.upTimeValue
        val uptimeSince = binding.uptimeInfo.upTimeInfo
        val localIpAddress = binding.localIpInfo.localIpAddresValue
        val publicIpAddress = binding.publicIpInfo.publicAddressValue
        val diskUsage = binding.diskInfo.diskUsageValue
        val diskName = binding.diskInfo.diskNameText
        val heaviestApp = binding.HeaviestProcessInfo.heaviestProcessValue
        val packageNumber = binding.packageNumInfo.packagesNumValue

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

            if ( sharedPref.getBoolean("connectionTested", false) ){

                var rotation = true

                val username = sharedPref.getString("username", "")

                val coroutineScope = MainScope()
                coroutineScope.launch {
                    val defer = async(Dispatchers.IO) {
                        sshConnection.executeRemoteCommandOneCall("python3 /home/${username}/copilot/main.py --dash")
                    }

                    val output = defer.await()

                    rotation = false

                    try {
                        val jsonObject = JSONTokener(output).nextValue() as JSONObject

                        try { tempText.text = jsonObject.getString("cpu_temp") }
                        catch ( e: JSONException ){ tempText.text = getString(R.string.read_error) }

                        try { cpuUsage.text = jsonObject.getString("cpu_usage") }
                        catch ( e: JSONException ) { cpuUsage.text = getString(R.string.read_error) }

                        try { ramUsage.text = jsonObject.getString("ram_usage") }
                        catch ( e: JSONException ) { ramUsage.text = getString(R.string.read_error) }

                        try { linuxKernelVersion.text = jsonObject.getString("kernel") }
                        catch ( e: JSONException ) { linuxKernelVersion.text = getString(R.string.read_error) }

                        try { hostname.text = jsonObject.getString("hostname") }
                        catch ( e: JSONException ) { hostname.text = getString(R.string.read_error) }

                        try { hostname.text = jsonObject.getString("hostname") }
                        catch ( e: JSONException ) { hostname.text = getString(R.string.read_error) }

                        try { hostname.text = jsonObject.getString("hostname") }
                        catch ( e: JSONException ) { hostname.text = getString(R.string.read_error) }

                        try { hostname.text = jsonObject.getString("hostname") }
                        catch ( e: JSONException) { hostname.text = getString(R.string.read_error) }

                        try { uptime.text = jsonObject.getString("uptime") }
                        catch ( e: JSONException) { uptime.text = getString(R.string.read_error) }

                        try { uptimeSince.text = jsonObject.getString("uptime_since") }
                        catch ( e: JSONException) { uptimeSince.text = getString(R.string.read_error) }

                        try { localIpAddress.text = jsonObject.getString("local_ip") }
                        catch ( e: JSONException)  { localIpAddress.text = getString(R.string.read_error) }

                        try { publicIpAddress.text = jsonObject.getString("public_ip")  }
                        catch ( e: JSONException)  { publicIpAddress.text = getString(R.string.read_error) }

                        try { diskUsage.text = jsonObject.getString("disk_usage")  }
                        catch ( e: JSONException) { diskUsage.text = getString(R.string.read_error) }

                        try { diskName.text = jsonObject.getString("disk_name") }
                        catch ( e: JSONException) { diskName.text = getString(R.string.read_error) }

                        try { heaviestApp.text = jsonObject.getString("stress_app") }
                        catch ( e: JSONException ){ heaviestApp.text = getString(R.string.read_error) }

                        try {  packageNumber.text = jsonObject.getString("packages") }
                        catch ( e: JSONException ){ packageNumber.text = getString(R.string.read_error) }
                    }
                    catch ( e: JSONException){
                        tempText.text = getString(R.string.read_error)
                        cpuUsage.text = getString(R.string.read_error)
                        ramUsage.text = getString(R.string.read_error)

                        linuxKernelVersion.text = getString(R.string.read_error)
                        hostname.text = getString(R.string.read_error)
                        uptime.text = getString(R.string.read_error)
                        uptimeSince.text = getString(R.string.read_error)
                        localIpAddress.text = getString(R.string.read_error)
                        publicIpAddress.text = getString(R.string.read_error)
                        diskUsage.text = getString(R.string.read_error)
                        diskName.text = getString(R.string.read_error)
                        heaviestApp.text = getString(R.string.read_error)
                        packageNumber.text = getString(R.string.read_error)
                    }

                    binding.swipeRefreshLayout.isRefreshing = false

                }
                fun rotate(){
                    refreshWidget.animate().apply {
                        duration = 1000
                        rotationBy(360f)
                    }.withEndAction{
                        if (rotation) rotate()
                        else
                        {
                            refreshWidget.isClickable = true
                            Toast.makeText(context, "Refreshed manually", Toast.LENGTH_SHORT).show()
                        }
                    }.start()
                }
                if(!auto)
                {
                    rotate()
                }
            }
            else
            {
                Toast.makeText(context, "You need to test your connection first. Please click red server icon at the top right corner", Toast.LENGTH_LONG).show()
            }
        }
        else
        {
            Toast.makeText(context, "Connection to server is not possible with given settings", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val tempText = binding.dashTemperatureTextView
        val cpuUsage = binding.dashCpuTextView
        val ramUsage = binding.dashRamTextView
        val linuxKernelVersion = binding.kernelInfo.linuxKernelVersion
        val hostname = binding.kernelInfo.hostname
        val uptime = binding.uptimeInfo.upTimeValue
        val uptimeSince = binding.uptimeInfo.upTimeInfo
        val localIpAddress = binding.localIpInfo.localIpAddresValue
        val publicIpAddress = binding.publicIpInfo.publicAddressValue
        val diskUsage = binding.diskInfo.diskUsageValue
        val diskName = binding.diskInfo.diskNameText
        val heaviestApp = binding.HeaviestProcessInfo.heaviestProcessValue
        val packageNumber = binding.packageNumInfo.packagesNumValue

        outState.apply {
            if (tempText.text.toString().isNotEmpty()) putString("tempText", tempText.text.toString())
            if (cpuUsage.text.toString().isNotEmpty()) putString("cpuUsage", cpuUsage.text.toString())
            if (ramUsage.text.toString().isNotEmpty()) putString("ramUsage", ramUsage.text.toString())
            if (linuxKernelVersion.text.toString().isNotEmpty()) putString("linuxKernelVersion", linuxKernelVersion.text.toString())
            if (hostname.text.toString().isNotEmpty()) putString("hostname", hostname.text.toString())
            if (uptime.text.toString().isNotEmpty()) putString("uptime", uptime.text.toString())
            if (uptimeSince.text.toString().isNotEmpty()) putString("uptimeSince", uptimeSince.text.toString())
            if (localIpAddress.text.toString().isNotEmpty()) putString("localIpAddress", localIpAddress.text.toString())
            if (publicIpAddress.text.toString().isNotEmpty()) putString("publicIpAddress", publicIpAddress.text.toString())
            if (diskUsage.text.toString().isNotEmpty()) putString("diskUsage", diskUsage.text.toString())
            if (diskName.text.toString().isNotEmpty()) putString("diskName", diskName.text.toString())
            if (heaviestApp.text.toString().isNotEmpty()) putString("heaviestApp", heaviestApp.text.toString())
            if (packageNumber.text.toString().isNotEmpty()) putString("packageNumber", packageNumber.text.toString())
        }

    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        val tempText = binding.dashTemperatureTextView
        val cpuUsage = binding.dashCpuTextView
        val ramUsage = binding.dashRamTextView
        val linuxKernelVersion = binding.kernelInfo.linuxKernelVersion
        val hostname = binding.kernelInfo.hostname
        val uptime = binding.uptimeInfo.upTimeValue
        val uptimeSince = binding.uptimeInfo.upTimeInfo
        val localIpAddress = binding.localIpInfo.localIpAddresValue
        val publicIpAddress = binding.publicIpInfo.publicAddressValue
        val diskUsage = binding.diskInfo.diskUsageValue
        val diskName = binding.diskInfo.diskNameText
        val heaviestApp = binding.HeaviestProcessInfo.heaviestProcessValue
        val packageNumber = binding.packageNumInfo.packagesNumValue

        tempText.text =             when(savedInstanceState){ null -> getString(R.string.nothingString) else -> savedInstanceState.getString("tempText", getString(R.string.nothingString))}
        cpuUsage.text =             when(savedInstanceState){ null -> getString(R.string.nothingString) else -> savedInstanceState.getString("cpuUsage", getString(R.string.nothingString))}
        ramUsage.text =             when(savedInstanceState){ null -> getString(R.string.nothingString) else -> savedInstanceState.getString("ramUsage", getString(R.string.nothingString))}
        linuxKernelVersion.text =   when(savedInstanceState){ null -> getString(R.string.nothingString) else -> savedInstanceState.getString("linuxKernelVersion", getString(R.string.nothingString))}
        hostname.text =             when(savedInstanceState){ null -> getString(R.string.nothingString) else -> savedInstanceState.getString("hostname", getString(R.string.nothingString))}
        uptime.text =               when(savedInstanceState){ null -> getString(R.string.nothingString) else -> savedInstanceState.getString("uptime", getString(R.string.nothingString))}
        uptimeSince.text =          when(savedInstanceState){ null -> getString(R.string.nothingString) else -> savedInstanceState.getString("uptimeSince", getString(R.string.nothingString))}
        localIpAddress.text =       when(savedInstanceState){ null -> getString(R.string.nothingString) else -> savedInstanceState.getString("localIpAddress", getString(R.string.nothingString))}
        publicIpAddress.text =      when(savedInstanceState){ null -> getString(R.string.nothingString) else -> savedInstanceState.getString("publicIpAddress", getString(R.string.nothingString))}
        diskUsage.text =            when(savedInstanceState){ null -> getString(R.string.nothingString) else -> savedInstanceState.getString("diskUsage", getString(R.string.nothingString))}
        diskName.text =             when(savedInstanceState){ null -> getString(R.string.nothingString) else -> savedInstanceState.getString("diskName", getString(R.string.nothingString))}
        heaviestApp.text =          when(savedInstanceState){ null -> getString(R.string.nothingString) else -> savedInstanceState.getString("heaviestApp", getString(R.string.nothingString))}
        packageNumber.text =        when(savedInstanceState){ null -> getString(R.string.nothingString) else -> savedInstanceState.getString("packageNumber", getString(R.string.nothingString))}
    }
}