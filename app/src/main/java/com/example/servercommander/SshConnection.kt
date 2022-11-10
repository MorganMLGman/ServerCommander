package com.example.servercommander

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.AsyncTask
import android.os.Environment
import android.widget.Toast
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.KeyPair
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*

class SshConnection {

    class SshTask : AsyncTask<Void, Void, String>() {
        override fun doInBackground(vararg p0: Void?): String {
            val output = executeRemoteCommand("demo", "test.rebex.net")
            print(output)
            return output
        }

        private fun executeRemoteCommand(username: String,
                                         hostname: String,
                                         port: Int = 22): String {

            val jsch = JSch()
//            jsch.addIdentity()
            val session = jsch.getSession(username, hostname, port)

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
            sshChannel.setCommand("ls")
            sshChannel.connect()

            // Sleep needed in order to wait long enough to get result back.
            Thread.sleep(1_000)
            sshChannel.disconnect()

            session.disconnect()

            return outputStream.toString()
        }
    }

    fun generateKeyPair (context: Context) {
        val privateKeyFile: String = "id_rsa"
        val publicKeyFile: String = "id_rsa.pub"

        val jsch = JSch()

        if(Environment.MEDIA_MOUNTED == Environment.getExternalStorageState())
        {
            val keyPair = KeyPair.genKeyPair(jsch, KeyPair.RSA)

            val idRsa = File(context.getExternalFilesDir(null), "id_rsa.txt")
            val idRsaPub = File(context.getExternalFilesDir(null), "id_rsa_pub.txt")

            if(idRsa.exists() or idRsaPub.exists()) {
                println("DUAOANSODIA")
                val builder: AlertDialog.Builder? = context?.let {
                    val builder = AlertDialog.Builder(it)
                    builder.apply {
                        setPositiveButton("Overwrite"
                        ) { dialog, id ->
                            keyPair.writePrivateKey(idRsa.absolutePath)
                            keyPair.writePublicKey(idRsaPub.absolutePath, "morgan@Android")
                            keyPair.dispose()

                            Toast.makeText(
                                context,
                                context.getString(R.string.newRsaKeysGenerated),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        setNegativeButton("Cancel"
                        ) { dialog, id ->
                            Toast.makeText(context, context.getString(R.string.newRsaKeysNotGenerated), Toast.LENGTH_SHORT).show()
                        }
                        setTitle("CAUTION")
                        setMessage("You are about to overwrite previously generated keys! Are you sure you want to do this?")
                    }
                }
                builder?.create()?.show()
            } else {

                keyPair.writePrivateKey(idRsa.absolutePath)
                keyPair.writePublicKey(idRsaPub.absolutePath, "morgan@Android")
                keyPair.dispose()

                Toast.makeText(context, context.getString(R.string.newRsaKeysGenerated), Toast.LENGTH_SHORT).show()
            }

        } else {
            Toast.makeText(context, context.getString(R.string.externalStorageWriteError), Toast.LENGTH_SHORT).show()
        }
    }
    
}