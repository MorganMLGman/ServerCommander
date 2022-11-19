package com.example.servercommander.fragments

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
import com.example.servercommander.databinding.FragmentHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.json.JSONTokener

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
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
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val refreshWidget = binding.refreshWidget
        val linuxKernelVersion = binding.dashboardItem1.linuxKernelVersion

        refreshWidget.setOnClickListener {
            if(sharedPref.contains(getString(R.string.server_url)) and
                sharedPref.contains(getString(R.string.username)) and
                sharedPref.contains(getString(R.string.pubkey))) {
                sshConnection = SshConnection(
                    sharedPref.getString(getString(R.string.server_url), "").toString(),
                    22,
                    sharedPref.getString(getString(R.string.username), "").toString(),
                    sharedPref.getString(getString(R.string.pubkey), "").toString()
                )

                val coroutineScope = MainScope()
                coroutineScope.launch {
                    val defer = async(Dispatchers.IO) {
                        sshConnection.executeRemoteCommandOneCall("python3 ~/copilot/main.py --dash")
                    }

                    val output = defer.await()

                    val jsonObject = JSONTokener(output).nextValue() as JSONObject

                    linuxKernelVersion.text = jsonObject.getString("kernel")
                }
            }
            else
            {
                Toast.makeText(context, "Connection to server is not possible with given settings", Toast.LENGTH_SHORT).show()
            }
        }
    }
}