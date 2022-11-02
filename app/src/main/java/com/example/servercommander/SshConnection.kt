package com.example.servercommander

import android.os.AsyncTask
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import java.io.ByteArrayOutputStream
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
    
}