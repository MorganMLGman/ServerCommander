package com.doyouhost.servercommander.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.doyouhost.servercommander.R
import com.doyouhost.servercommander.databinding.FragmentSettingsBinding
import com.doyouhost.servercommander.viewModels.RefreshViewModel
import com.google.android.material.slider.Slider

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val refreshViewModel: RefreshViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bmcButton = binding.bmcButton
        val clearSettingsButton = binding.clearSettingsButton

        val serverUrlUpdateText = binding.serverUrlUpdateText
        val serverUrlUpdateButton = binding.serverUrlUpdateButton
        val usernameUpdateText = binding.usernameUpdateText
        val usernameUpdateButton = binding.usernameUpdateButton
        val sshPortUpdateText = binding.sshPortUpdateText
        val sshPortUpdateButton = binding.sshPortUpdateButton
        val refreshSlider = binding.refreshIntervalValue
        val refreshSwitch = binding.refreshSwitch

        val sudoPassword = binding.sudoPassword
        val sudoSave = binding.sudoSaveButton

        val yunohostPassword = binding.yunohostPassword
        val yunohostSave = binding.saveYunohostPassword

        refreshSwitch.isChecked = refreshViewModel.enabled.value == true

        val sharedPref = requireActivity().getSharedPreferences(
            "ServerCommander", Context.MODE_PRIVATE
        )

        serverUrlUpdateText.setText(sharedPref.getString("serverUrl", getString(R.string.not_available)))
        usernameUpdateText.setText(sharedPref.getString("username", getString(R.string.not_available)))
        sshPortUpdateText.setText(sharedPref.getInt("sshPort", 22).toString())

        bmcButton.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.buymeacoffee.com/doyouhost"))
            startActivity(browserIntent)
        }

        clearSettingsButton.setOnClickListener{
            with(sharedPref.edit()){
                remove("serverUrl")
                remove("username")
                remove("pubkey")
                remove("sshPort")
                remove("sudo_password")
                putBoolean("connectionTested", false)
                apply()

                requireActivity().finish()
                startActivity(requireActivity().intent)
            }
        }

        serverUrlUpdateButton.setOnClickListener{
            if(serverUrlUpdateText.text.toString().matches(Regex("[A-Za-z0-9.]*")))
            {
                with(sharedPref.edit()){
                    putString("serverUrl", serverUrlUpdateText.text.toString())
                    putBoolean("connectionTested", false)
                    apply()
                }
                Toast.makeText(context, getString(R.string.server_addres_updated), Toast.LENGTH_SHORT).show()
            }
            else
            {
                serverUrlUpdateText.error = getString(R.string.serverUrlError)
            }
        }

        usernameUpdateButton.setOnClickListener{
            if(usernameUpdateText.text.toString().matches(Regex("[A-Za-z0-9.]*")))
            {
                with(sharedPref.edit()){
                    putString("username", usernameUpdateText.text.toString())
                    putBoolean("connectionTested", false)
                    apply()
                }
                Toast.makeText(context, getString(R.string.username_updated), Toast.LENGTH_SHORT).show()
            }
            else
            {
                usernameUpdateText.error = getString(R.string.usernameError)
            }
        }

        sshPortUpdateButton.setOnClickListener {
            if(sshPortUpdateText.text.toString().matches(Regex("[0-9.]*")) and
                (sshPortUpdateText.text.toString().toInt() > 0) and
                (sshPortUpdateText.text.toString().toInt() <= 65535))
            {
                with(sharedPref.edit()){
                    putInt("sshPort", sshPortUpdateText.text.toString().toInt())
                    putBoolean("connectionTested", false)
                    apply()
                }
                Toast.makeText(context, getString(R.string.sshPortUpdated), Toast.LENGTH_SHORT).show()
            }
            else
            {
                usernameUpdateText.error = getString(R.string.sshPortError)
            }
        }

        refreshSlider.addOnChangeListener(Slider.OnChangeListener {
                _, value, _ ->

            refreshViewModel.interval(value.toInt())
        })

        refreshSwitch.setOnClickListener {
            refreshViewModel.enabled(refreshSwitch.isChecked)
        }

        sudoSave.setOnClickListener {
            val password = sudoPassword.text.toString()

            if (password.isNotEmpty() and (password != ""))
            {
                with(sharedPref.edit()){
                    putString("sudo_password", password)
                    apply()
                }
                Toast.makeText(context, "Password saved", Toast.LENGTH_SHORT).show()
            }
            else Toast.makeText(context, "Password cannot be saved", Toast.LENGTH_SHORT).show()
        }

        yunohostSave.setOnClickListener{
            val yunohostPasswordText = yunohostPassword.text.toString()

            if (yunohostPasswordText.isNotEmpty() and (yunohostPasswordText != ""))
            {
                with(sharedPref.edit()){
                    putString("yunohost_password", yunohostPasswordText)
                    apply()
                }
                Toast.makeText(context, "Password saved", Toast.LENGTH_SHORT).show()
            }
            else Toast.makeText(context, "Password cannot be saved", Toast.LENGTH_SHORT).show()


        }
    }
}
