package com.example.servercommander.fragments

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
import com.example.servercommander.R
import com.example.servercommander.SshConnection
import com.example.servercommander.databinding.FragmentHomeBinding
import com.example.servercommander.viewModels.RefreshViewModel
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
            getString(R.string.app_name), Context.MODE_PRIVATE
        )

        handler = Handler(Looper.getMainLooper())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

        connectionTest.setOnClickListener {
            if(connectionTest.isClickable)
            {
                connectionTest.isClickable = false

                if(sharedPref.contains(getString(R.string.server_url)) and
                    sharedPref.contains(getString(R.string.username)) and
                    sharedPref.contains(getString(R.string.pubkey)) and
                    sharedPref.contains(getString(R.string.connectionTested))) {

                    sshConnection = SshConnection(
                        sharedPref.getString(getString(R.string.server_url), "").toString(),
                        22,
                        sharedPref.getString(getString(R.string.username), "").toString(),
                        sharedPref.getString(getString(R.string.pubkey), "").toString()
                    )

                    var rotation: Boolean = true

                    val coroutineScope = MainScope()
                    coroutineScope.launch {
                        val defer = async(Dispatchers.IO) {
                            sshConnection.checkRequirements()
                        }

                        val (output, comment) = defer.await()
                        rotation = false

                        if (output){
                            with(sharedPref.edit()){
                                putBoolean(getString(R.string.connectionTested), true)
                                apply()
                            }

                            context?.getColor(R.color.brightGreen)
                                ?.let { it1 -> connectionTest.setColorFilter(it1, android.graphics.PorterDuff.Mode.SRC_IN) }
                            connectionTest.setImageResource(R.drawable.server_network)
                        }
                        else
                        {
                            val builder: AlertDialog.Builder? = context.let {
                                val builder = AlertDialog.Builder(it)
                                builder.apply {
                                    setCancelable(true)
                                    setTitle("Something went wrong :(")
                                    setMessage(comment.trim())
                                }
                            }
                            builder?.create()?.show()

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

        if (sharedPref.getBoolean(getString(R.string.connectionTested), false))
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
        val localIpAddress = binding.localIpInfo.localIpAddresValue
        val publicIpAddress = binding.publicIpInfo.publicAddressValue
        val diskUsage = binding.diskInfo.diskUsageValue
        val diskName = binding.diskInfo.diskNameText
        val heaviestApp = binding.HeaviestProcessInfo.heaviestProcessValue
        val packageNumber = binding.packageNumInfo.packagesNumValue

        if(sharedPref.contains(getString(R.string.server_url)) and
            sharedPref.contains(getString(R.string.username)) and
            sharedPref.contains(getString(R.string.pubkey)) and
            sharedPref.contains(getString(R.string.connectionTested))) {

            sshConnection = SshConnection(
                sharedPref.getString(getString(R.string.server_url), "").toString(),
                22,
                sharedPref.getString(getString(R.string.username), "").toString(),
                sharedPref.getString(getString(R.string.pubkey), "").toString()
            )

            if ( sharedPref.getBoolean(getString(R.string.connectionTested), false) ){

                var rotation: Boolean = true

                val coroutineScope = MainScope()
                coroutineScope.launch {
                    val defer = async(Dispatchers.IO) {
                        sshConnection.executeRemoteCommandOneCall("python3 ~/copilot/main.py --dash")
                    }

                    val output = defer.await()

                    rotation = false

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
}