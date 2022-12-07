package com.example.servercommander

import android.util.Log
import android.widget.Toast
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
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

            return response.headers.values("Set-Cookie")

        }
    }

    fun getUserNumber(url: String, cookie: String) {

        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .header(
                name = "Cookie",
                value = cookie.toString()
            )
            .header("accept", "*/*")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val output = response.body!!.string()
                    val rsp = JSONTokener(output).nextValue() as JSONObject

                    println(rsp.getString("users"))

                    println()

                }
            }
        })

    }

    fun isAPIInstalled (url: String) : Boolean {

            val request = Request.Builder()
                .url(url)
                .header("accept", "*/*")
                .build()

            client.newCall(request).execute().use { response ->


                val output = response.body!!.string()
                println(output)
                if (output.isEmpty()){
                    return false
                } else {

                    //TODO: Make If statement when response is not JSON but something different
                    val resp = JSONTokener(output).nextValue() as JSONObject

                    if (!response.isSuccessful){
                        return false
                    }

                    else {
                        if (resp.getBoolean("installed") ) {
                            return true
                        }
                        return false
                    }
                }

            }

    }

}
