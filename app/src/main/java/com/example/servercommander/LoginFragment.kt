package com.example.servercommander

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
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
                username.error = "YunoHost needs 'admin' user"
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

    }
}