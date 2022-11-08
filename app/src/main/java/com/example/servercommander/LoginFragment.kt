package com.example.servercommander

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.servercommander.databinding.FragmentLoginBinding
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.io.PrintWriter
import java.time.Instant
import java.time.format.DateTimeFormatter

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null

    private val binding get() = _binding!!

    private val sshConnection:SshConnection = SshConnection();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = activity?.getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE
        )

        if (sharedPref != null) {
            if (sharedPref.contains("serverUrl") and
                sharedPref.contains("username") and
                sharedPref.contains("pubkey")
            ) {
                findNavController().navigate(R.id.action_loginFragment_to_FirstFragment)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val serverUrl = binding.serverUrl
        val username = binding.username
        val pubkey = binding.pubkey
        val radioYunohost = binding.radioYH
        val radioDocker = binding.radioDocker
        val generateButton = binding.generatePubKey


        val sharedPref = activity?.getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE
        )

        binding.loginButton.setOnClickListener {
            var wrongData: Boolean = false

            if (!serverUrl.text.toString().matches(Regex("[A-Za-z0-9.]*")))
            {
                wrongData = true
                serverUrl.error = getString(R.string.serverUrlError)
            }

            if (!username.text.toString().matches(Regex("[A-Za-z0-9]*")))
            {
                wrongData = true
                username.error = getString(R.string.usernameError)
            }

            if (pubkey.text.toString().isEmpty())
            {
                wrongData = true
                pubkey.error = getString(R.string.pubkeyError)
            }

            if (username.toString()!="admin" && radioYunohost.isChecked)
            {
                username.error = getString(R.string.yhAdminUserError)
                wrongData = true
            }

            if (wrongData)
            {
                Toast.makeText(
                    context,
                    getString(R.string.correctErrorToast),
                    Toast.LENGTH_SHORT
                ).show()
            }
            else
            {
                if (sharedPref != null) {
                    with(sharedPref.edit()) {
                        putString("serverUrl", serverUrl.text.toString())
                        putString("username", username.text.toString())
                        putString("pubkey", pubkey.text.toString())
                        apply()
                    }

                    Toast.makeText(context, getString(R.string.connectionSaved), Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_loginFragment_to_FirstFragment)
                }
                else {
                    Toast.makeText(
                        context,
                        "Internal app error!\nCannot save connection details.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.generatePubKey.setOnClickListener {
//            sshConnection.generateKeyPair()
//            TODO: Write proper implementation this is just for testing

            val file = File(context?.getExternalFilesDir(null), "testFile.txt")
            println(file.absolutePath)

            val outStream = PrintWriter(FileOutputStream(file, true))

            outStream.println("DUPA: "+DateTimeFormatter.ISO_INSTANT.format(Instant.now()))

            outStream.close()

        }

    }
}