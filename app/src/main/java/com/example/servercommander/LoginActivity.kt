package com.example.servercommander

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class LoginActivity : AppCompatActivity() {

    private var backPressed: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val sharedPref = getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE
        )

        val serverUrl = findViewById<EditText>(R.id.serverUrl)
        val username = findViewById<EditText>(R.id.username)
        val pubkey = findViewById<EditText>(R.id.pubkey)
        val radioYunohost = findViewById<RadioButton>(R.id.radioYH)
        val radioDocker = findViewById<RadioButton>(R.id.radioDocker)
        val generateButton = findViewById<Button>(R.id.generatePubKey)
        val readButton = findViewById<Button>(R.id.readPubkeyButton)
        val loginButton = findViewById<Button>(R.id.loginButton)

        generateButton.setOnClickListener {
            SshConnection.generateKeyPair(this)

            val pubKeyPath = getExternalFilesDir(null)?.absolutePath

            pubkey.setText(pubKeyPath)

            val idRsaPub = File(pubKeyPath, "id_rsa.pub").readText()

            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("id_rsa.pub", idRsaPub)
            clipboard.setPrimaryClip(clip)
        }

        readButton.setOnClickListener {
            val pubKeyPath = getExternalFilesDir(null)?.absolutePath

            if (pubKeyPath != null) {
                if( File(pubKeyPath, "id_rsa").exists() and File(pubKeyPath, "id_rsa.pub").exists() ) {
                    pubkey.setText(pubKeyPath)
                }
                else
                {
                    Toast.makeText(this, getString(R.string.no_pubkey_found), Toast.LENGTH_SHORT).show()
                }
            }
            else
            {
                Toast.makeText(this, getString(R.string.pubkey_read_error), Toast.LENGTH_SHORT).show()
            }
        }

        loginButton.setOnClickListener{
            if (sharedPref != null) {
                if (!validateInput())
                {
                    Toast.makeText(
                        this,
                        getString(R.string.correctErrorToast),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else
                {
                    with(sharedPref.edit()) {
                        putString(getString(R.string.server_url), serverUrl.text.toString())
                        putString(getString(R.string.username), username.text.toString())
                        putString(getString(R.string.pubkey), pubkey.text.toString())

                        if(radioYunohost.isChecked) putString("server_type", "yunohost")
                        else                        putString("server_type", "docker")

                        apply()
                    }

                    loginButton.animate().apply {
                        duration = 1000
                        rotationXBy(360f)
                    }.withEndAction{
                        Toast.makeText(this, getString(R.string.connectionSaved), Toast.LENGTH_LONG).show()
                        val intent = Intent()
                        intent.putExtra("server_type", sharedPref.getString("server_type", ""))
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }.start()
                }
            }
            else
            {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }

    }

    private fun validateInput(pubKeyRequired: Boolean = true, radioRequired: Boolean = true): Boolean{
        var wrongData = false

        val serverUrl = findViewById<EditText>(R.id.serverUrl)
        val username = findViewById<EditText>(R.id.username)
        val pubkey = findViewById<EditText>(R.id.pubkey)
        val radioYunohost = findViewById<RadioButton>(R.id.radioYH)
        val radioDocker = findViewById<RadioButton>(R.id.radioDocker)

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

        return !wrongData
    }

    override fun onBackPressed() {
        if (!backPressed){
            backPressed = true
            Toast.makeText(this, "Press one more time to exit", Toast.LENGTH_SHORT).show()
        }
        else
        {
            finishAffinity()
        }
    }

}