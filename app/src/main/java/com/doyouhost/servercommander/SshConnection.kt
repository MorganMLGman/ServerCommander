package com.doyouhost.servercommander

import android.app.AlertDialog
import android.content.Context
import android.os.Environment
import android.widget.Toast
import com.jcraft.jsch.*
import java.io.*
import java.util.*


class SshConnection(val serverAddress: String,
                    val serverPort: Int = 22,
                    val username: String,
                    private val keyPath: String ) {

    private var jsch = JSch()
    private val sessionTimeout: Int = 30000

    private lateinit var session: Session
    private lateinit var shell: ChannelShell
    private lateinit var fromServer: BufferedReader
    private lateinit var toServer: OutputStream

    fun executeRemoteCommandOneCall(command: String): String {
        if (serverAddress.isNotEmpty() and username.isNotEmpty() and keyPath.isNotEmpty() and command.isNotEmpty()) {
            if (File("$keyPath/id_rsa").exists() and File("$keyPath/id_rsa.pub").exists()) {
                try {
                    if (jsch.identityNames.isEmpty()) {
                        jsch.addIdentity("$keyPath/id_rsa", "$keyPath/id_rsa.pub", null)
                    }

                    val session = jsch.getSession(username, serverAddress, serverPort)
                    val properties = Properties()
                    properties["StrictHostKeyChecking"] = "no"
                    session.setConfig(properties)
                    session.timeout = sessionTimeout
                    session.connect()

                    val sshChannel = session.openChannel("exec") as ChannelExec
                    val outputStream = ByteArrayOutputStream()
                    sshChannel.outputStream = outputStream
                    sshChannel.setCommand(command)

                    sshChannel.connect()
                    while (!sshChannel.isClosed) {
                        Thread.sleep(100)
                    }

//                    val exitCode = sshChannel.exitStatus

                    sshChannel.disconnect()

                    session.disconnect()

                    return outputStream.toString()
                } catch (e: JSchException) {
                    println(e.message.toString())
                    return ""
                }
            }
            return ""
        }
        return ""
    }

    fun checkRequirements(): Pair<Boolean, String> {
        var returnComment = ""
        var requirementsOK = true
        var answer: String

//        Check if ssh connection is OK and specified user matches system user
        run {
            answer = executeRemoteCommandOneCall("whoami").trim()

            if (answer.isEmpty()) {
                requirementsOK = false
                returnComment += "\nConnection cannot be established. Have you added pubkey to your server?\n"
            } else if (answer != this.username) {
                requirementsOK = false
                returnComment += "\nProvided username is not identical to server username.\n"
            }

            if (!requirementsOK) {
                return Pair(false, returnComment)
            }
        }

//        Check python version
        run {
            answer = executeRemoteCommandOneCall("python3 --version").trim()

            if (!answer.contains("Python 3.")) {
                requirementsOK = false
                returnComment += "\nRequired version on Python3 is not available on server. Please run sudo apt install python3\n"
            }
        }

//        Check if python3-pip is installed
        run {
            answer = executeRemoteCommandOneCall("dpkg -s python3-pip").trim()

            if (!answer.contains("Status: install ok")) {
                requirementsOK = false
                returnComment += "\npython3-pip is not available on server. Please run sudo apt install python3-pip\n"
            }
        }

//        Check if git is installed
        run {
            answer = executeRemoteCommandOneCall("whereis git").trim()

            if (!answer.contains("git: /")) {
                requirementsOK = false
                returnComment += "\ngit is not available on server. Please run sudo apt install git\n"
            }
        }

//        If everything OK check if copilot exists else try to git clone
        run {
            if (requirementsOK) {
                answer =
                    executeRemoteCommandOneCall("ls /home/${this.username} | grep copilot").trim()

                if (answer == "copilot") // Copilot exists, perform git pull and can finish
                {
                    executeRemoteCommandOneCall("cd /home/${this.username}/copilot && git pull")
                    executeRemoteCommandOneCall("pip3 install -r /home/${this.username}/copilot/requirements.txt")
                } else // No copilot, try to git clone
                {
                    executeRemoteCommandOneCall("git clone https://github.com/MorganMLGman/copilot.git /home/${this.username}/copilot")
                    answer =
                        executeRemoteCommandOneCall("ls /home/${this.username} | grep copilot").trim()

                    if (answer == "copilot") // Copilot now exists, install requirements
                    {
                        executeRemoteCommandOneCall("pip3 install -r /home/${this.username}/copilot/requirements.txt")
                    } else {
                        requirementsOK = false
                        returnComment += "\ngit clone not successful. Please run git clone https://github.com/MorganMLGman/copilot.git /home/${this.username}/copilot\n"
                    }
                }
            }
        }

        return Pair(requirementsOK, returnComment)
    }

//    private fun isAlive(session: Session): Session {
//        lateinit var newSession: Session
//        try {
//            val testChannel = session.openChannel("exec") as ChannelExec
//            testChannel.setCommand("true")
//            testChannel.connect()
//            testChannel.disconnect()
//            newSession = session
//        } catch (e: JSchException) {
//            newSession = jsch.getSession(username, serverAddress, serverPort)
//            val properties = Properties()
//            properties["StrictHostKeyChecking"] = "no"
//            newSession.setConfig(properties)
//            newSession.connect()
//        }
//        return newSession
//    }

    fun openConnection() {
        if (serverAddress.isNotEmpty() and username.isNotEmpty() and keyPath.isNotEmpty()) {
            if (File("$keyPath/id_rsa").exists() and File("$keyPath/id_rsa.pub").exists()) {
                try {
                    if (jsch.identityNames.isEmpty()) {
                        jsch.addIdentity("$keyPath/id_rsa", "$keyPath/id_rsa.pub", null)
                    }

                    val session = jsch.getSession(username, serverAddress, serverPort)
                    val properties = Properties()
                    properties["StrictHostKeyChecking"] = "no"
                    session.setConfig(properties)
                    session.connect()

                    this.session = session
                    shell = session.openChannel("shell") as ChannelShell
                    shell.setPtyType("dumb")

                    fromServer = BufferedReader(InputStreamReader(shell.inputStream))
                    toServer = shell.outputStream

                    shell.connect()

                } catch (e: JSchException) {
                    println(e.message.toString())
                }
            }
        }
    }

    fun isOpen(): Boolean
    {
        return if(::session.isInitialized and ::shell.isInitialized) {
            session.isConnected and shell.isConnected
        } else {
            false
        }
    }

//    fun closeConnection() {
//        if(::session.isInitialized and ::shell.isInitialized)
//        {
//            shell.disconnect()
//            session.disconnect()
//        }
//    }

    fun executeRemoteCommand(command: String): String {
        if(!isOpen()){
            return ""
        }
        if (command.isEmpty() or (command == "")) {
            return ""
        }

        toServer.write((command.trim() + "\r\n").toByteArray())
        toServer.flush()

        val builder = StringBuilder()
        var line: String

        while (true) {
            try {
                line = fromServer.readLine()
                builder.append(line).append("\n")
            } catch (e: java.lang.NullPointerException) {
                break
            }

            if (line.endsWith("$ ")) break
            else if (line.endsWith("# ")) break
            else if (line.endsWith("> ")) break
        }

        return builder.toString()
    }

    companion object {
        fun generateKeyPair(context: Context): Boolean {
            var ret = false

            if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
                val keyPair = KeyPair.genKeyPair(JSch(), KeyPair.RSA)

                val idRsa = File(context.getExternalFilesDir(null), "id_rsa")
                val idRsaPub = File(context.getExternalFilesDir(null), "id_rsa.pub")

                if (idRsa.exists() or idRsaPub.exists()) {
                    val builder: AlertDialog.Builder = context.let {
                        val builder = AlertDialog.Builder(it)
                        builder.apply {
                            setPositiveButton(
                                context.getString(R.string.overwriteButtonLabel)
                            ) { _, _ ->
                                keyPair.writePrivateKey(idRsa.absolutePath)
                                keyPair.writePublicKey(
                                    idRsaPub.absolutePath,
                                    context.getString(R.string.pubkeyComment)
                                )
                                keyPair.dispose()

                                Toast.makeText(
                                    context,
                                    context.getString(R.string.newRsaKeysGenerated),
                                    Toast.LENGTH_SHORT
                                ).show()

                                ret = true
                            }
                            setNegativeButton(
                                context.getString(R.string.cancelButtonLabel)
                            ) { _, _ ->
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.newRsaKeysNotGenerated),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            setTitle(context.getString(R.string.cautionLabel))
                            setMessage(context.getString(R.string.overwriteMessage))
                        }
                    }
                    builder.create()?.show()
                } else {

                    keyPair.writePrivateKey(idRsa.absolutePath)
                    keyPair.writePublicKey(
                        idRsaPub.absolutePath,
                        context.getString(R.string.pubkeyComment)
                    )
                    keyPair.dispose()

                    Toast.makeText(
                        context,
                        context.getString(R.string.newRsaKeysGenerated),
                        Toast.LENGTH_SHORT
                    ).show()
                    ret = true
                }

            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.externalStorageWriteError),
                    Toast.LENGTH_SHORT
                ).show()
            }

            return ret
        }
    }
}
