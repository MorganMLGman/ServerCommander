package com.example.servercommander

import android.util.Log
import okhttp3.*
import okhttp3.internal.parseCookie
import java.io.IOException

class YunohostAuthenticate {
    private val client = OkHttpClient()

    fun run() {

        val formBody = FormBody.Builder()
            .add("credentials", "password")
            .build()

        val request = Request.Builder()
            .url("https://demo.yunohost.org/yunohost/api/login")
            .header("X-Requested-With", "serverCommander")
            .post(formBody)
            .build()

        client.newCall(request).execute().use { response ->
            Log.d("Czy działa?", response.body!!.string())
        }
    }

}

fun main() {
    YunohostAuthenticate().run()
}