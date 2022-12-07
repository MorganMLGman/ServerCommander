package com.example.servercommander

import android.util.Log
import android.widget.Toast
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
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

        try {
            client.run {
                newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            e.printStackTrace()
                            throw IOException()
                        }


                       override fun onResponse(call: Call, response: Response) {

                            response.use {
                                if (!response.isSuccessful) {
                                    throw IOException("Unexpected code $response")
                                }

                                val output = response.body!!.string()
                                //println(output)

                                if (output.isEmpty()) {
                                    throw IOException()
                                }

                                    try {
                                        val resp = JSONTokener(output).nextValue() as JSONObject
                                        Log.d("Czy wchodzi do try w JSON", "Tak")
                                        if (resp.getBoolean("installed")) {
                                            Log.d("Czy wchodzi w ifa w JSON", "Tak")

                                        } else {throw IOException()}

                                    } catch (e: JSONException) {
                                        throw IOException("Unexpected code $response")
                                    } catch (e: java.lang.ClassCastException){
                                        throw IOException("Unexpected code $response")
                                    } catch (e: IOException){throw IOException()}
                            }
                        }
                    })
            }

        } catch (e: IOException) {
         return false
        }
        return false
    }


}
