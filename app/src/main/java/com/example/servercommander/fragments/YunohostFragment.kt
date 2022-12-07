package com.example.servercommander.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.servercommander.R
import com.example.servercommander.SshConnection
import com.example.servercommander.databinding.FragmentYunohostBinding
import com.example.servercommander.YunohostConnection
import com.example.servercommander.databinding.AlertDialogPasswordBinding
import kotlin.reflect.KFunction2


class YunohostFragment : Fragment() {
    private var _binding: FragmentYunohostBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPref: SharedPreferences
    private lateinit var sshConnection: SshConnection

    private lateinit var adminLoginPage: String

    private lateinit var ssoWebpage : String
    private lateinit var adminWebPage : String
    private lateinit var adminAPI : String
    private lateinit var getUsersLink : String
    private lateinit var isAPIInstalledLink : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        sharedPref = requireActivity().getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE
        )


        //TODO: Add "http://" too
        adminLoginPage = "https://" + sharedPref.getString(getString(R.string.server_url), "").toString() + "/yunohost/api/login"
        ssoWebpage = "https://" + sharedPref.getString(getString(R.string.server_url), "").toString() + "/yunohost/sso/portal.html"
        adminWebPage = "https://" + sharedPref.getString(getString(R.string.server_url), "").toString() + "/yunohost/admin/"
        adminAPI = "https://" + sharedPref.getString(getString(R.string.server_url), "").toString() + "/yunohost/api"
        getUsersLink = "https://" + sharedPref.getString(getString(R.string.server_url), "").toString() + "/yunohost/api/users?fields=username"
        isAPIInstalledLink = "https://" + sharedPref.getString(getString(R.string.server_url), "").toString() + "/yunohost/api/installed"
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
        val refreshButton = binding.refreshYunohostConnection

        openSSOButton.setOnClickListener{
            var intent = Intent(Intent.ACTION_VIEW, Uri.parse(ssoWebpage))
            startActivity(intent)
        }

        goToAdminPage.setOnClickListener{
            var intent = Intent(Intent.ACTION_VIEW, Uri.parse(adminWebPage))
            startActivity(intent)
        }

        refreshButton.setOnClickListener {

            var password = sharedPref.getString("yunohost_password", "")!!

            if (password.isEmpty() or (password == "")) {
                password = showPasswordModal(getUsersLink, ::getYunohostConnection)
            } else getYunohostConnection(getUsersLink, password)
        }


    }

    private fun getYunohostConnection(url: String, password: String){


        if(context?.let { isOnline(it) }!!) {


            Log.d("Czy zwraca prawde?", YunohostConnection().isAPIInstalled(isAPIInstalledLink).toString())
                if(YunohostConnection().isAPIInstalled(isAPIInstalledLink)) {

                    Log.d("Elo", "Elo")
                    val cookie = YunohostConnection().authenticate(adminLoginPage, password)
                    if (cookie.isEmpty()) {
                        Log.d("Elo2", "Elo2")
                        Toast.makeText(context, "Wrong Password", Toast.LENGTH_SHORT).show()
                    } else {
                        try {
                            YunohostConnection().getUserNumber(getUsersLink, cookie[0])
                            Log.d("Elo3", "Elo3")
                        } catch (e: Exception) {
                            Toast.makeText(context, "Connection aborted", Toast.LENGTH_SHORT).show()
                            Log.d("Elo4", "Elo4")
                        }
                    }
                } else {
                    Log.d("Elo5", "Elo5")
                    Toast.makeText(context, "Connection aborted", Toast.LENGTH_SHORT).show()
                }
        }

        else {
            Log.d("Elo7", "Elo7")
            Toast.makeText(context, "Connection aborted", Toast.LENGTH_SHORT).show()
        }



    }




    private fun showPasswordModal(url: String, func: KFunction2<String, String, Unit>): String {
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
                        func(url, password)
                    }
                    setNegativeButton(
                        context.getString(R.string.cancelButtonLabel)
                    ) { _, _ ->
                        password = ""
                    }
                    setView(passwordLayout.root)
                }
            }
            builder.create()?.show()
            return password
        }
        return ""
    }

    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }

    override fun onResume() {
        super.onResume()


        //TODO: Add "http://" too
        adminLoginPage = "https://" + sharedPref.getString(getString(R.string.server_url), "").toString() + "/yunohost/api/login"
        ssoWebpage = "https://" + sharedPref.getString(getString(R.string.server_url), "").toString() + "/yunohost/sso/portal.html"
        adminWebPage = "https://" + sharedPref.getString(getString(R.string.server_url), "").toString() + "/yunohost/admin/"
        adminAPI = "https://" + sharedPref.getString(getString(R.string.server_url), "").toString() + "/yunohost/api"
        getUsersLink = "https://" + sharedPref.getString(getString(R.string.server_url), "").toString() + "/yunohost/api/users?fields=username"
        isAPIInstalledLink = "https://" + sharedPref.getString(getString(R.string.server_url), "").toString() + "/yunohost/api/installed"
    }

}