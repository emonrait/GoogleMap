package com.example.googlemap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eraapps.bankasia.bdinternetbanking.apps.room.AtmListAdapter
import eraapps.bankasia.bdinternetbanking.apps.room.AtmRoomModel
import kotlinx.android.synthetic.main.activity_main.*
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: AtmListAdapter
    private lateinit var requestList: ArrayList<AtmRoomModel>

    private lateinit var atmRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        atmRecyclerView = findViewById(R.id.atmRecyclerView)

        requestList = ArrayList<AtmRoomModel>()

        //23.7496845,90.414654

        var a = AtmRoomModel(1, "1", "A", "23.7496845", "90.414654", 25f)
        var b = AtmRoomModel(2, "1", "B", "23.7487874", "90.4088899", 25f)
        var c = AtmRoomModel(3, "1", "C", "23.7551324", "90.380352", 25f)
        var d = AtmRoomModel(4, "1", "D", "23.7496845", "90.414654", 25f)

        requestList.add(a)
        requestList.add(b)
        requestList.add(c)
        requestList.add(d)

        dataShow()


        atmname.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                adapter?.filter?.filter(text)

            }
        })


    }

    fun dataShow() {
        //  Recylerview
        adapter = AtmListAdapter(requestList, this, object : AtmListAdapter.OnItemClickListener {
            override fun onItemClick(item: AtmRoomModel?) {

            }
        })
        atmRecyclerView.layoutManager = LinearLayoutManager(applicationContext)
        atmRecyclerView.adapter = adapter
        atmRecyclerView.setHasFixedSize(true)
    }
}