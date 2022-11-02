package com.example.servercommander

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        val sharedPref = activity?.getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE
        )

        val serverUrl = binding.serverUrl
        val username = binding.username
        val pubkey = binding.pubkey


        if (sharedPref != null) {
            if (sharedPref.contains("serverUrl") and
                sharedPref.contains("username") and
                sharedPref.contains("pubkey")
            ) {
                serverUrl.setText(sharedPref.getString("serverUrl", ""))
                username.setText(sharedPref.getString("username", ""))
                pubkey.setText(sharedPref.getString("pubkey", ""))
            }


            binding.loginButton.setOnClickListener {

                with(sharedPref.edit()) {
                    putString("serverUrl", serverUrl.text.toString())
                    putString("username", username.text.toString())
                    putString("pubkey", pubkey.text.toString())
                    apply()
                }
            }
        }
    }
}