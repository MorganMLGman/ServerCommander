package com.doyouhost.servercommander.fragments

import android.app.Activity
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
import com.doyouhost.servercommander.R
import com.doyouhost.servercommander.databinding.FragmentYunohostBinding
import com.doyouhost.servercommander.YunohostConnection
import com.doyouhost.servercommander.databinding.AlertDialogPasswordBinding
import com.doyouhost.servercommander.YunohostConnection.Companion.cookie
import kotlinx.coroutines.*
import java.io.File
import kotlin.reflect.KFunction2


class YunohostFragment : Fragment() {
    private var _binding: FragmentYunohostBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPref: SharedPreferences

    private lateinit var idRsaPub : String

    private lateinit var adminLoginPage: String

    private lateinit var ssoWebpage : String
    private lateinit var adminWebPage : String
    private lateinit var adminAPI : String
    private lateinit var getUsersLink : String
    private lateinit var isAPIInstalledLink : String
    private lateinit var getDomainNumberLink : String
    private lateinit var getAppNumberLink : String
    private lateinit var postSshKeysLink : String
    private lateinit var getBackupNumberLink : String


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
        getDomainNumberLink = "https://" + sharedPref.getString(getString(R.string.server_url), "").toString() + "/yunohost/api/domains?exclude_subdomains=false"
        getAppNumberLink = "https://" + sharedPref.getString(getString(R.string.server_url), "").toString() + "/yunohost/api/apps?full=false&upgradable=false"
        getBackupNumberLink = "https://" + sharedPref.getString(getString(R.string.server_url), "").toString() + "/yunohost/api/backups?with_info=false&human_readable=false'"
        postSshKeysLink = "https://" + sharedPref.getString(getString(R.string.server_url), "").toString() + "/yunohost/api/users/ssh/key"


//        idRsaPub = File(keyPath.toString(), "id_rsa.pub").readText()
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
        val pushSshKeysButton = binding.SshYunohostCard.buttonPushNewSshKey
        val appsToUpdateWidget = binding.appsToUpdate

        openSSOButton.setOnClickListener{
            var intent = Intent(Intent.ACTION_VIEW, Uri.parse(ssoWebpage))
            startActivity(intent)
        }

        goToAdminPage.setOnClickListener{
            var intent = Intent(Intent.ACTION_VIEW, Uri.parse(adminWebPage))
            startActivity(intent)
        }

        refreshButton.setOnClickListener {
            refreshButton.isClickable = false
            refreshButton.animate().apply {
                duration = 1000
                rotationBy(360f)
            }.withEndAction{
                refreshButton.isClickable = true
            }.start()

            var password = sharedPref.getString("yunohost_password", "")!!

            if (password.isEmpty() or (password == "")) {
                password = showPasswordModal(getUsersLink, ::getYunohostConnection)
            } else getYunohostConnection(getUsersLink, password)
        }

        appsToUpdateWidget.setOnClickListener {
            if(YunohostConnection.isCookieInitalized()) {
                Toast.makeText(context, YunohostConnection.appToUpdateNames, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Click refresh first", Toast.LENGTH_SHORT).show()
            }


        }



        pushSshKeysButton.setOnClickListener {

            pushSshKeysButton.isClickable = false
            pushSshKeysButton.animate().apply {
                duration = 1000
                rotationXBy(360f)
            }.withEndAction {
                pushSshKeysButton.isClickable = true
            }.start()

            val username = binding.SshYunohostCard.usernameEditText.text.toString()

            if(username.isNotEmpty()) {
                if(YunohostConnection.isCookieInitalized()) {
                    val keyPath = sharedPref.getString(getString(R.string.pubkey), "").toString()
                    val idRsaPub = File(keyPath, "id_rsa.pub").readText()
                    YunohostConnection.postNewSshKey(postSshKeysLink, idRsaPub, username)
                    if(YunohostConnection.IsSshKeysPushed){
                        Toast.makeText(context, "POST request sent", Toast.LENGTH_SHORT).show()

                    } else {
                        Toast.makeText(context, "Wrong username", Toast.LENGTH_SHORT).show()

                    }

                } else {Toast.makeText(context, "Click Refresh first", Toast.LENGTH_SHORT).show()}
            } else {
                Toast.makeText(context, "Invalid username", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun getYunohostConnection(url: String, password: String){

        if(context?.let { isOnline(it) }!!) {

            val coroutineScope = MainScope()
            coroutineScope.launch {
                val defer = async(Dispatchers.IO) {
                    isYHAvailable(password)
                }
                val output = defer.await().trim()
           //     Toast.makeText(context, output, Toast.LENGTH_SHORT).show()
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

    private fun isYHAvailable(password: String): String
    {
        var ret: String = ""
        YunohostConnection.isAPIInstalled(isAPIInstalledLink)


        if(YunohostConnection.boolIsApiInstalled) {

            YunohostConnection.authenticate(adminLoginPage, password)

            if (cookie.isEmpty()) {
                ret += "Wrong Password\n"
//                Toast.makeText(context, "Wrong Password", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    YunohostConnection.getUserNumber(getUsersLink)
                    YunohostConnection.getDomainNumber(getDomainNumberLink)
                    YunohostConnection.getAppToUpdateNumberMethod(getAppNumberLink)
                    YunohostConnection.getCreatedBackupsNumber(getBackupNumberLink)
                    requireActivity().runOnUiThread {
                            binding.yunohostAppToUpdateTextView.text = YunohostConnection.appToUpdateNumberValue.toString()
                            binding.yunohostDomainNumberTextView.text = YunohostConnection.domainNumberValue.toString()
                            binding.yunohostUsersNumberTextView.text = YunohostConnection.usersNumberValue.toString()
                            binding.backupYunohostCard.yunohostCreatedBackupsTextView.text = YunohostConnection.createdBackupsValue.toString()
                    }

                } catch (e: Exception) {
                    ret += "Connection aborted\n"
//                    Toast.makeText(context, "Connection aborted", Toast.LENGTH_SHORT).show()
                }
            }

        } else {
            ret += "Connection aborted\n"
//            Toast.makeText(context, "Connection aborted", Toast.LENGTH_SHORT).show()
        }
        return ret
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