package com.example.servercommander

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.servercommander.databinding.FragmentLoginBinding

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

            if (!validateInput())
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

            if(validateInput(pubKeyRequired = false, radioRequired = false))
            {
                context?.let { it1 -> sshConnection.generateKeyPair(it1) }
            }

            val pubKeyPath = requireActivity().getExternalFilesDir(null)?.absolutePath

            pubkey.setText(pubKeyPath)

            val clipboard = requireActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("pubKeyPath", pubKeyPath)
            clipboard.setPrimaryClip(clip)
        }
    }

    private fun validateInput(pubKeyRequired: Boolean = true, radioRequired: Boolean = true): Boolean{
        var wrongData = false

        val serverUrl = binding.serverUrl
        val username = binding.username
        val pubkey = binding.pubkey
        val radioYunohost = binding.radioYH
        val radioDocker = binding.radioDocker

        if (!serverUrl.text.toString().matches(Regex("[A-Za-z0-9.]*"))
            or serverUrl.text.toString().isEmpty())
        {
            wrongData = true
            serverUrl.error = getString(R.string.serverUrlError)
        }

        if (!username.text.toString().matches(Regex("[A-Za-z0-9]*"))
            or username.text.toString().isEmpty())
        {
            wrongData = true
            username.error = getString(R.string.usernameError)
        }

        if (pubKeyRequired and pubkey.text.toString().isEmpty())
        {
            wrongData = true
            pubkey.error = getString(R.string.pubkeyError)
        }

        if (radioRequired and username.text.toString().contentEquals("admin") and radioYunohost.isChecked)
        {
            username.error = getString(R.string.yhAdminUserError)
            wrongData = true
        }

        if (radioRequired and radioDocker.isChecked)
        {
            wrongData = true
        }

        return !wrongData
    }
}