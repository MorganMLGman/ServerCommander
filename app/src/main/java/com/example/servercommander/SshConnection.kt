package com.example.servercommander

import android.app.AlertDialog
import android.content.Context
import android.os.Environment
import android.widget.Toast
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.KeyPair
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*

sealed class Result<out R> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}

class SshConnection(private val serverAddress: String,
                    private val serverPort: Int = 22,
                    private val username: String,
                    private val keyPath: String ) {

    private var jsch = JSch()

    fun executeRemoteCommandOneCall(command: String): String {
        if (serverAddress.isNotEmpty() and username.isNotEmpty() and keyPath.isNotEmpty() and command.isNotEmpty())
        {
            if(File("$keyPath/id_rsa").exists() and File("$keyPath/id_rsa.pub").exists())
            {
                if(jsch.identityNames.isEmpty())
                {
                    jsch.addIdentity("$keyPath/id_rsa", "$keyPath/id_rsa.pub", null)
                }

                val session = jsch.getSession(username, serverAddress, serverPort)
                val properties = Properties()
                properties["StrictHostKeyChecking"] = "no"
                session.setConfig(properties)
                session.connect()

                val sshChannel = session.openChannel("exec") as ChannelExec
                val outputStream = ByteArrayOutputStream()
                sshChannel.outputStream = outputStream
                sshChannel.setCommand(command)

                sshChannel.connect()
                while(!sshChannel.isClosed)
                {
                    Thread.sleep(100)
                }

                val exitCode = sshChannel.exitStatus

                sshChannel.disconnect()

                session.disconnect()

                return outputStream.toString()
            }
            return ""
        }
        return ""
    }

    fun checkRequirements(): Pair<Boolean, String>
    {
        var returnComment = ""
        var requirementsOK: Boolean = true
        var answer: String = ""

//        Check if ssh connection is OK and specified user matches system user
        run {
            try {
                answer = executeRemoteCommandOneCall("whoami").trim()
            }
            catch ( e: JSchException )
            {
                requirementsOK = false
                returnComment += "\nConnection cannot be established. Have you added pubkey to your server?"
            }

            if (answer != this.username){
                requirementsOK = false
                returnComment += "\nProvided username is not identical to server username."
            }
            answer = ""
        }

//        Check python version
        run {
            answer = executeRemoteCommandOneCall("python3 --version").trim()

            if (!answer.contains("Python 3."))
            {
                requirementsOK = false
                returnComment += "\nRequired version on Python3 is not available on server. Please run sudo apt install python3"
            }
            answer = ""
        }

//        Check if python3-pip is installed
        run {
            answer = executeRemoteCommandOneCall("dpkg -s python3-pip").trim()

            if (!answer.contains("Status: install ok"))
            {
                requirementsOK = false
                returnComment += "\npython3-pip in not available on server. Please run sudo apt install python3-pip"
            }

            answer = ""
        }

        return Pair(requirementsOK, returnComment)
    }

    companion object
    {
        fun generateKeyPair (context: Context): Boolean {
            var ret = false

            if(Environment.MEDIA_MOUNTED == Environment.getExternalStorageState())
            {
                val keyPair = KeyPair.genKeyPair(JSch(), KeyPair.RSA)

                val idRsa = File(context.getExternalFilesDir(null), "id_rsa")
                val idRsaPub = File(context.getExternalFilesDir(null), "id_rsa.pub")

                if(idRsa.exists() or idRsaPub.exists()) {
                    val builder: AlertDialog.Builder? = context.let {
                        val builder = AlertDialog.Builder(it)
                        builder.apply {
                            setPositiveButton(context.getString(R.string.overwriteButtonLabel)
                            ) { dialog, id ->
                                keyPair.writePrivateKey(idRsa.absolutePath)
                                keyPair.writePublicKey(idRsaPub.absolutePath, context.getString(R.string.pubkeyComment))
                                keyPair.dispose()

                                Toast.makeText(
                                    context,
                                    context.getString(R.string.newRsaKeysGenerated),
                                    Toast.LENGTH_SHORT
                                ).show()

                                ret = true
                            }
                            setNegativeButton(context.getString(R.string.cancelButtonLabel)
                            ) { dialog, id ->
                                Toast.makeText(context, context.getString(R.string.newRsaKeysNotGenerated), Toast.LENGTH_SHORT).show()
                            }
                            setTitle(context.getString(R.string.cautionLabel))
                            setMessage(context.getString(R.string.overwriteMessage))
                        }
                    }
                    builder?.create()?.show()
                } else {

                    keyPair.writePrivateKey(idRsa.absolutePath)
                    keyPair.writePublicKey(idRsaPub.absolutePath, context.getString(R.string.pubkeyComment))
                    keyPair.dispose()

                    Toast.makeText(context, context.getString(R.string.newRsaKeysGenerated), Toast.LENGTH_SHORT).show()
                    ret = true
                }

            } else {
                Toast.makeText(context, context.getString(R.string.externalStorageWriteError), Toast.LENGTH_SHORT).show()
            }

            return ret
        }
    }
}