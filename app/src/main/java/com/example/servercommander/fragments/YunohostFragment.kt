package com.example.servercommander.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import com.example.servercommander.R
import com.example.servercommander.SshConnection
import com.example.servercommander.databinding.FragmentYunohostBinding

class YunohostFragment : Fragment() {
    private var _binding: FragmentYunohostBinding? = null
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
    ): View {
        _binding = FragmentYunohostBinding.inflate(inflater, container, false)
        return binding.root



    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val openSSOButton = binding.openSSOButton
        val goToAdminPage = binding.moreButton

        val ssoWebpage = "https://" + sharedPref.getString(getString(R.string.server_url), "").toString() + "/yunohost/sso/portal.html"
        val adminWebPage = "https://" + sharedPref.getString(getString(R.string.server_url), "").toString() + "/yunohost/admin/"

        openSSOButton.setOnClickListener{
            var intent = Intent(Intent.ACTION_VIEW, Uri.parse(ssoWebpage))
            startActivity(intent)
        }

        goToAdminPage.setOnClickListener{
            var intent = Intent(Intent.ACTION_VIEW, Uri.parse(adminWebPage))
            startActivity(intent)
        }





    }

}