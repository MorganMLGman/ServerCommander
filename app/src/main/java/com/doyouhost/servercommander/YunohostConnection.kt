package com.doyouhost.servercommander

import android.util.Log
import android.widget.Toast
import com.doyouhost.servercommander.fragments.YunohostFragment
import okhttp3.*
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException

class YunohostConnection {

    companion object
    {
        var boolIsApiInstalled: Boolean  = false
        lateinit var cookie: List<String>
        var usersNumberValue = 0
        var domainNumberValue = 0
        var appToUpdateNumberValue = 0
        var appToUpdateNames = ""
        var IsSshKeysPushed : Boolean = true
        var createdBackupsValue = 0

        fun authenticate(url: String, password: String) {
            val client = OkHttpClient()

            val formBody = FormBody.Builder()
                .add("credentials", password)
                .build()

            val request = Request.Builder()
                .url(url)
                .header("X-Requested-With", "serverCommander")
                .post(formBody)
                .build()

            client.newCall(request).execute().use { response ->
                cookie =  response.headers.values("Set-Cookie")
            }
        }

        fun isCookieInitalized() = ::cookie.isInitialized

        fun getUserNumber(url: String) {

            val client = OkHttpClient()
            if(::cookie.isInitialized){
                val request = Request.Builder()
                    .url(url)
                    .header(
                        name = "Cookie",
                        value = cookie[0]
                    )
                    .header("accept", "*/*")
                    .build()

                client.newCall(request).execute().use { response ->
                    val output = response.body!!.string()

                    val rsp = JSONTokener(output).nextValue() as JSONObject
                    val users = rsp.getJSONObject("users")
                    usersNumberValue = users.length()

                }
            }

        }

        fun getCreatedBackupsNumber(url: String) {

            val client = OkHttpClient()
            if(::cookie.isInitialized){
                val request = Request.Builder()
                    .url(url)
                    .header(
                        name = "Cookie",
                        value = cookie[0]
                    )
                    .header("accept", "*/*")
                    .build()

                client.newCall(request).execute().use { response ->
                    val output = response.body!!.string()

                    val rsp = JSONTokener(output).nextValue() as JSONObject
                    val archives = rsp.getJSONObject("archives")
                    createdBackupsValue = archives.length()

                }
            }

        }




        fun getDomainNumber(url: String) {

            val client = OkHttpClient()
            if(::cookie.isInitialized){
                val request = Request.Builder()
                    .url(url)
                    .header(
                        name = "Cookie",
                        value = cookie[0]
                    )
                    .header("accept", "*/*")
                    .build()


                client.newCall(request).execute().use { response ->
                    val output = response.body!!.string()

                    val rsp = JSONTokener(output).nextValue() as JSONObject
                    val array = rsp.getJSONArray("domains")
                    domainNumberValue = array.length()
                }
            }

        }

        fun getAppToUpdateNumberMethod(url: String) {

            val client = OkHttpClient()
            if(::cookie.isInitialized){
                val request = Request.Builder()
                    .url(url)
                    .header(
                        name = "Cookie",
                        value = cookie[0]
                    )
                    .header("accept", "*/*")
                    .build()

                client.newCall(request).execute().use { response ->
                    val output = response.body!!.string()

                    val rsp = JSONTokener(output).nextValue() as JSONObject
                    val array = rsp.getJSONArray("apps")

                    appToUpdateNumberValue = array.length()

                    var names = ""

                    for (i in 0 until array.length()) {
                        names += " " + array.getJSONObject(i).getString("name") + ","
                    }
                    Log.d("names", names)

                    appToUpdateNames = names

                }
            }

        }

        fun isAPIInstalled (url: String) {

            val client = OkHttpClient()
            boolIsApiInstalled = false

            val request = Request.Builder()
                .url(url)
                .header("accept", "*/*")
                .build()

            client.newCall(request).execute().use{response ->
                try {
                    val resp = JSONTokener(response.body!!.string()).nextValue() as JSONObject
                    if (resp.getBoolean("installed")) {
                        boolIsApiInstalled = true
                    }
                } catch (_: Exception) {}
            }
        }

        fun postNewSshKey(url: String, pubkey: String, username: String) {
            val client = OkHttpClient()
            IsSshKeysPushed = true

            val formBody = FormBody.Builder()
                .add("username", username)
                .add("key", pubkey)
                .add("comment", "Added with Server Commander")
                .build()

            val request = Request.Builder()
                .url(url)
                .header("accept", "*/*")
                .header("Content-type", "multipart/form-data")
                .header("X-Requested-With", "serverCommander")
                .header("Cookie", cookie[0])
                .post(formBody)
                .build()

            client.newCall(request).execute().use { response ->
                val request = (response.body!!.string())

                Log.d ("Req", request)

                if (request.contains("error")) {
                    IsSshKeysPushed = false
                }
            }
        }
    }
}
