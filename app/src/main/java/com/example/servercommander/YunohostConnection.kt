package com.example.servercommander

import android.util.Log
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
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
                    println(rsp.toString())
                    val users = rsp.getJSONObject("users")
                    println(users.length())
                    usersNumberValue = users.length()
//

//                    val array = rsp.getJSONArray("users")
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
                    println(domainNumberValue)
//                    val domains = rsp.getJSONObject("domains")
//                    Log.d("Domeny", domains.length().toString())
//                    domainNumberValue = domains.length()


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
                    Log.d("Czy wchodzi do try w JSON", "Tak")
                    if (resp.getBoolean("installed")) {
                        Log.d("Czy wchodzi w ifa w JSON", "Tak")
                        boolIsApiInstalled = true

                    }

                } catch (_: Exception) {}
            }
        }
    }
}
