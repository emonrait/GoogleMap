package eraapps.bankasia.bdinternetbanking.apps.room

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.googlemap.R
import kotlinx.android.synthetic.main.row_atm_locaton.view.*
import java.util.ArrayList

class AtmListAdapter(
    private var atmlist: ArrayList<AtmRoomModel>,
    context: Context,
    listenerInit: OnItemClickListener

) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    // private var atmlist = emptyList<AtmRoomModel>()

    var requestFilterList = ArrayList<AtmRoomModel>()
    lateinit var mcontext: Context

    //lateinit var dialog: Dialog
    var listener: OnItemClickListener


    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


    init {
        requestFilterList = atmlist
        listener = listenerInit


    }

    interface OnItemClickListener {
        fun onItemClick(position: AtmRoomModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val atmListView =
            LayoutInflater.from(parent.context).inflate(R.layout.row_atm_locaton, parent, false)
        val sch = MyViewHolder(atmListView)
        mcontext = parent.context


        return sch
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = requestFilterList[position]
        holder.itemView.atmLocation.text = currentItem.atmLocation.toString().toUpperCase()
        holder.itemView.atmLocationmap.text = "Switch to Map Mode"

        holder.itemView.atmLocationmap.setOnClickListener {
            Toast.makeText(mcontext, currentItem.atmLocation.toString(), Toast.LENGTH_SHORT).show()
            val selectedList = requestFilterList[position]
            listener.onItemClick(selectedList)

        }
    }

    override fun getItemCount(): Int {
        return requestFilterList.size
        notifyDataSetChanged()
        Log.e("atmlistValue--->", requestFilterList.size.toString())
    }

    fun setData(atmdata: ArrayList<AtmRoomModel>) {
        //this.requestFilterList = atmdata
        this.requestFilterList = atmdata
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                if (constraint.toString().isEmpty()) {
                    requestFilterList = atmlist
                } else {

                    val resultList = ArrayList<AtmRoomModel>()
                    for (row in atmlist) {
                        if (
                            row.atmLocation.toString().toLowerCase()
                                ?.contains(constraint.toString().toLowerCase())
                        ) {
                            resultList.add(row)
                        }
                    }
                    requestFilterList = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = requestFilterList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                requestFilterList = results?.values as ArrayList<AtmRoomModel>
                notifyDataSetChanged()
            }

        }
    }
}