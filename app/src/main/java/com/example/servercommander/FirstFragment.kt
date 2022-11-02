package com.example.servercommander

import android.Manifest
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.servercommander.databinding.FragmentFirstBinding
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    val sharedPref = activity?.getSharedPreferences(
        getString(R.string.app_name), Context.MODE_PRIVATE
    )

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)

//        val callback = object : OnBackPressedCallback(true){
//            override fun handleOnBackPressed() {
//                isEnabled = true
//            }
//
//        }
//        requireActivity().onBackPressedDispatcher.addCallback(callback)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {

            SshTask().execute()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class SshTask : AsyncTask<Void, Void, String>() {
        override fun doInBackground(vararg p0: Void?): String {
            val output = executeRemoteCommand("", "")
            print(output)
            return output
        }

        val path: String = Environment.DIRECTORY_DOWNLOADS + File.pathSeparator + "ssh_priv_key.txt"

        private fun executeRemoteCommand(username: String,
                                         hostname: String,
                                         port: Int = 22): String {

            val jsch = JSch()

            val session = jsch.getSession(username, hostname, port)

            session.setPassword("!");
            // Avoid asking for key confirmation.
            val properties = Properties()
            properties["StrictHostKeyChecking"] = "no"
            session.setConfig(properties)

            session.connect()

            // Create SSH Channel.
            val sshChannel = session.openChannel("exec") as ChannelExec
            val outputStream = ByteArrayOutputStream()
            sshChannel.outputStream = outputStream

            // Execute command.
            sshChannel.setCommand("ls -lha")
            sshChannel.connect()

            // Sleep needed in order to wait long enough to get result back.
            Thread.sleep(1_000)
            sshChannel.disconnect()

            session.disconnect()

            return outputStream.toString()
        }
    }
}