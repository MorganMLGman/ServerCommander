package com.example.servercommander

import android.util.Log
import okhttp3.*
import java.io.IOException

class YunohostConnection {
    private val client = OkHttpClient()



    fun authenticate(url: String, password: String): List<String> {


        val formBody = FormBody.Builder()
            .add("credentials", password)
            .build()

        val request = Request.Builder()
            .url(url)
            .header("X-Requested-With", "serverCommander")
            .post(formBody)
            .build()

        client.newCall(request).execute().use { response ->
            Log.d("Czy działa?", response.body!!.string())
            val cookieList = response.headers.values("Set-Cookie").also {
                println(it)
            }

            return@authenticate cookieList
        }
    }

    fun getUserNumber(url: String, cookie: List<String>) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .header(
                name = "admin.yunohost",
                value = cookie.toString()
            )
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    Log.d("Response from GET",response.body!!.string())
                }
            }
        })
    }

}
