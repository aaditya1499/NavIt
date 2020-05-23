package com.example.indoornavigation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    lateinit var mapBmp: Bitmap
    lateinit var img:TouchImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val building = intent.getStringExtra("building")
        Log.d("building",building)
//        aa.text = building

        callAPI("map/$building/floor0") {response ->
            val mapJson = JSONObject(response)
            var encodedImg = mapJson.getString("result")
            val byteArr = Base64.decode(encodedImg, Base64.DEFAULT)

            val options = BitmapFactory.Options()
            options.inMutable = true
            mapBmp = BitmapFactory.decodeByteArray(byteArr, 0, byteArr.size, options)

            img = TouchImageView(this)
            img.setImageBitmap(mapBmp)
            img.setMaxZoom(4f)
            setContentView(img)
        }

    }

    fun callAPI(param: String,callback: (String) -> Unit){
        readData("http://gosmartsoftsolutions.com/indoornav/$param") { strResp ->
            callback(strResp)
        }

    }

    fun readData(url:String,callback: (String) -> Unit) {
        Toast.makeText(this,"APi call",Toast.LENGTH_LONG).show()
        val queue = Volley.newRequestQueue(this)
        var str:String?=null

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                var strResp = response.toString()
                callback(strResp)
            },
            Response.ErrorListener {
                Toast.makeText(this,"That didn't work!", Toast.LENGTH_LONG).show()
                Log.d("error",it.toString())
            })

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

}
