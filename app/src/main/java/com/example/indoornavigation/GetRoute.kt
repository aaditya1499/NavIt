package com.example.indoornavigation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_get_route.*
import org.json.JSONObject

class GetRoute : AppCompatActivity() {

    lateinit var building:String
    lateinit var floorMap:String
    val list=ArrayList<String>()
    val progressBar = CustomProgressBar()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_route)
        building = intent.getStringExtra("buildingName")
        progressBar.show(this,"Loading Floor rooms")
        callAPI("roomAll/$building/floor0") {response ->

            val mapJson = JSONObject(response)
            var roomsList = mapJson.getJSONArray("result")
            for(i in 0 until roomsList.length()) {
                list.add(roomsList[i].toString())
            }
            progressBar.dialog.dismiss()
        }
        var arrayAdapter: ArrayAdapter<String> = ArrayAdapter(this,android.R.layout.select_dialog_item,list)
        destination.threshold = 1
        destination.setAdapter(arrayAdapter)
        val sharedPreferences = this.getSharedPreferences("currentFloorMap",0)
        Log.d("store",sharedPreferences.getString("floorMap","default"))

    }

    fun sendRouteInfo(view: View) {
        var destination = destination.text.toString()
        if(destination!="") {
            val intent = Intent(this,ShowMap::class.java)
            intent.putExtra("building",building)
            intent.putExtra("origin","shop1")
            intent.putExtra("destination",destination)
            startActivity(intent)
            finishAffinity()
        }
    }

    fun callAPI(param: String,callback: (String) -> Unit){
        readData("http://gosmartsoftsolutions.com/indoornav/$param") { strResp ->
            callback(strResp)
        }

    }

    fun readData(url:String,callback: (String) -> Unit) {
        Toast.makeText(this,"APi call", Toast.LENGTH_LONG).show()
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
