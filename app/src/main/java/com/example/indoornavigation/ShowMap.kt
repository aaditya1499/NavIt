package com.example.indoornavigation

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.util.JsonReader
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.scale
import androidx.core.view.setMargins
import androidx.drawerlayout.widget.DrawerLayout
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.JsonRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_show_map.*
import kotlinx.android.synthetic.main.content_show_map.*
import kotlinx.android.synthetic.main.menu_ticket.view.*
import org.json.JSONObject


class ShowMap : AppCompatActivity() {

    lateinit var mapBmp: Bitmap
    lateinit var floorMap:String
    lateinit var img:TouchImageView
    var menuAdapter:MenuAdapter?=null
    var menuList = ArrayList<HamburgerMenu>()
    lateinit var sharedPreferences: SharedPreferences
    val progressBar = CustomProgressBar()

    lateinit var navAdapter:ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_map)
        setSupportActionBar(toolbar)

        sharedPreferences = this.getSharedPreferences("currentFloorMap",0)

        addDrawerItems()
        setupDrawer()

        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        supportActionBar!!.setHomeButtonEnabled(true)

        val building = intent.getStringExtra("building")
        val origin = intent.getStringExtra("origin")
        val destination = intent.getStringExtra("destination")

        title = building

        val fab1 = FloatingActionButton(this)
        val lay = CoordinatorLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lay.setMargins(15, 15, 45, 225)
        fab1.foregroundGravity = Gravity.BOTTOM
        lay.gravity = Gravity.BOTTOM or Gravity.END
        fab1.layoutParams = lay
        fab1.setImageBitmap(textAsBitmap("1", 40f, Color.WHITE))


        fab1.size = FloatingActionButton.SIZE_MINI
        show_map.addView(fab1)




        if(origin!=null) {
            val jsonrequest = JSONObject()
            jsonrequest.put("origin",origin)
            jsonrequest.put("destination",destination)

            progressBar.show(this,"Calculating shortest route from $origin to $destination")

            val mapJson = JSONObject(sharedPreferences.getString("floorMap","default"))
            var encodedImg = mapJson.getString("result")
            val byteArr = Base64.decode(encodedImg, Base64.DEFAULT)

            val options = BitmapFactory.Options()
            options.inMutable = true
            mapBmp = BitmapFactory.decodeByteArray(byteArr, 0, byteArr.size, options)

            getRoute("http://gosmartsoftsolutions.com/indoornav/routeRev/$building/floor0",jsonrequest) { jsonResponse ->
                var encodedImg = jsonResponse.getString("result")
                val byteArr = Base64.decode(encodedImg, Base64.DEFAULT)
                val options = BitmapFactory.Options()
                val routeBmp = BitmapFactory.decodeByteArray(byteArr, 0, byteArr.size, options)

                val mergedImage = createSingleImageFromMultipleImages(mapBmp,routeBmp)
                val mapWithPointer = putPointer(mergedImage)

                img = iv_touchImageView
                img.setImageBitmap(mapWithPointer)
                img.setMaxZoom(4f)
                progressBar.dialog.dismiss()
            }

            fab.setOnClickListener { view ->
                val intent = Intent(this,GetRoute::class.java)
                intent.putExtra("buildingName",building)
                startActivity(intent)
            }
        }
        else {
            progressBar.show(this,"Loading Floor Map")
            callAPI("map/$building/floor0") {response ->
                floorMap = response
                val editor:SharedPreferences.Editor =  sharedPreferences.edit()
                editor.putString("floorMap",floorMap)
                editor.apply()
                editor.commit()

                val mapJson = JSONObject(response)
                var encodedImg = mapJson.getString("result")
                val byteArr = Base64.decode(encodedImg, Base64.DEFAULT)

                val options = BitmapFactory.Options()
                options.inMutable = true
                mapBmp = BitmapFactory.decodeByteArray(byteArr, 0, byteArr.size, options)
                val mapWithPointer = putPointer(mapBmp)

                img = iv_touchImageView
                img.setImageBitmap(mapWithPointer)
                img.setMaxZoom(4f)


                progressBar.dialog.dismiss()

                fab.setOnClickListener { view ->
                    val intent = Intent(this,GetRoute::class.java)
                    intent.putExtra("buildingName",building)
                    startActivity(intent)
                }
            }
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

    fun getRoute(url:String,json:JSONObject,callback: (JSONObject) -> Unit) {
        Toast.makeText(this,"APi call", Toast.LENGTH_LONG).show()
        val queue = Volley.newRequestQueue(this)
        var str:String?=null

        // Request a string response from the provided URL.

        val jsonreq = JsonObjectRequest(url,json,
            Response.Listener<JSONObject> { response ->
                callback(response)
            },
            Response.ErrorListener {
                Toast.makeText(this,"That didn't work!", Toast.LENGTH_LONG).show()
                Log.d("error",it.toString())
            })

        // Add the request to the RequestQueue.
        queue.add(jsonreq)
    }

    private fun createSingleImageFromMultipleImages(
        firstImage: Bitmap,
        secondImage: Bitmap
    ): Bitmap {
        val result = Bitmap.createBitmap(
            firstImage.width,
            firstImage.height,
            firstImage.config
        )
        val canvas = Canvas(result)
        canvas.drawBitmap(firstImage, 0f, 0f, null)
        canvas.drawBitmap(secondImage, 0f, 0f, null)

        return result
    }

    private fun putPointer(
        map: Bitmap
    ): Bitmap {
        val result = Bitmap.createBitmap(
            map.width,
            map.height,
            map.config
        )
        val canvas = Canvas(result)
        val pointer = BitmapFactory.decodeResource(resources, R.drawable.pointer_small)
        Log.d("ht","${pointer.height},,${pointer.width}")
        canvas.drawBitmap(map, 0f, 0f, null)
        canvas.drawBitmap(pointer, 1352f, 730f, null)
        return result
    }

    fun addDrawerItems() {

        var navArray = arrayOf("Show Buildings")

        menuList.add(HamburgerMenu("Show Building",R.drawable.show_buildings))
        menuList.add(HamburgerMenu("Bookmark",R.drawable.bookmark))
        menuList.add(HamburgerMenu("Favourite",R.drawable.heart))
        menuList.add(HamburgerMenu("Add Building Description",R.drawable.label))
        menuList.add(HamburgerMenu("Location Sharing",R.drawable.users))
        menuList.add(HamburgerMenu("Offline Maps",R.drawable.map))
        menuList.add(HamburgerMenu("Settings",R.drawable.settings))

//        navAdapter = ArrayAdapter(this, R.layout.menu_ticket, navArray)

        menuAdapter = MenuAdapter(this,menuList)
        navList.adapter = menuAdapter

        val headerView = getLayoutInflater().inflate(R.layout.nav_header, null)
        navList.addHeaderView(headerView)

        navList.onItemClickListener = AdapterView.OnItemClickListener {parent,view, position, id ->
            // Get the selected item text from ListView
            val selectedItem = parent.getItemAtPosition(position) as String
            
            val intent = Intent(this, SelectBuilding::class.java)
            startActivity(intent)
        }

    }

    fun setupDrawer() {
        val mDrawerToggle:ActionBarDrawerToggle = object : ActionBarDrawerToggle(
        this,
        drawer_layout,
        toolbar,
        R.string.drawer_open,
        R.string.drawer_close
        ){
            override fun onDrawerClosed(view:View){
                super.onDrawerClosed(view)
                //toast("Drawer closed")
            }

            override fun onDrawerOpened(drawerView: View){
                super.onDrawerOpened(drawerView)
                //toast("Drawer opened")
            }
        }

        // Configure the drawer layout to add listener and show icon on toolbar
        mDrawerToggle.isDrawerIndicatorEnabled = true
        drawer_layout.addDrawerListener(mDrawerToggle)
        mDrawerToggle.syncState()

    }

    fun textAsBitmap(text:String,textSize:Float ,textColor:Int) :Bitmap {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.textSize = textSize
        paint.color = textColor
        paint.textAlign = Paint.Align.LEFT
        var baseline:Float = -paint.ascent()
        val width:Int = (paint.measureText(text) + 0.0).toInt()
        val height:Int = (baseline + paint.descent() + 0.0).toInt()
        val image:Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(image)
        canvas.drawText(text, 0f, baseline, paint)
        return image
}

    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Please BACK again to exit", Toast.LENGTH_SHORT).show()

        Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }


    inner class MenuAdapter: BaseAdapter {                      //,Filterable aayega shayad
        var menuList = ArrayList<HamburgerMenu>()
        var context: Context?=null
        constructor(context: Context, menuList:ArrayList<HamburgerMenu>):super(){

            this.menuList=menuList
            this.context=context
        }
        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {

            val menu=menuList[p0]
            var inflater=context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            var myView=inflater.inflate(R.layout.menu_ticket,null)
            myView.menu_name.text = menu.menuItem
            myView.menu_icon.setImageResource(menu.menuIcon!!)

            myView.setOnClickListener {
                when(myView.menu_name.text) {
                    "Show Building" -> {
                        val intent = Intent(context, SelectBuilding::class.java)
                        startActivity(intent)
                    }
                    else -> Toast.makeText(context,"Not Functional yet",Toast.LENGTH_LONG).show()
                }
            }

            return myView
        }

//        override fun getFilter(): Filter {
//            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//
//        }
//
//        private inner class ValueFilter:Filter() {
//            override fun performFiltering(constraint: CharSequence?): FilterResults {
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//            }
//
//            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//            }
//        }


        override fun getItem(p0: Int): Any {
            return menuList[p0]
        }

        override fun getItemId(p0: Int): Long {

            return p0.toLong()
        }

        override fun getCount(): Int {

            return menuList.size
        }
    }




}
