package com.example.indoornavigation

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_select_building.*
import org.json.JSONObject

class SelectBuilding : AppCompatActivity(), BuildingsAdapter.onItemClickListner {

    val buildings = ArrayList<String>()
    lateinit var bmp:Bitmap
    lateinit var adapter:BuildingsAdapter
    val progressBar = CustomProgressBar()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_building)

        adapter = BuildingsAdapter(buildings)
        progressBar.show(this,"Loading Buildings")

        callAPI("mapAll") {response ->
            val mapJson = JSONObject(response)
            var buildingList = mapJson.getJSONArray("result")
            for(i in 0 until buildingList.length()) {
                buildings.add(buildingList[i].toString())
            }
            buildings.add("Building 1")
            buildings.add("Building 2")
            buildings.add("Building 3")
            buildings.add("Building 4")
            adapter.notifyDataSetChanged()
            progressBar.dialog.dismiss()
        }

//
//        buildings.add("Building 1")
//        buildings.add("Building 2")



        buildingRecView.layoutManager = LinearLayoutManager(this)
        buildingRecView.adapter = adapter

        adapter.onItemClick = { building ->
            Log.d("build",building)
            val intent = Intent(this,ShowMap::class.java)
            intent.putExtra("building",building)
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
