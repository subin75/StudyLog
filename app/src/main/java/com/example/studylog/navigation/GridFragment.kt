package com.example.studylog.navigation

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studylog.*
import kotlinx.android.synthetic.main.activity_add_todo.*
import kotlinx.android.synthetic.main.fragment_grid.*
import kotlinx.android.synthetic.main.fragment_grid.view.*
import kotlinx.android.synthetic.main.item_header.view.*
import kotlinx.android.synthetic.main.item_main.view.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.SimpleFormatter

class GridFragment : Fragment() {

    var list: MutableList<ItemYO> = mutableListOf()
    lateinit var recyclerView: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_grid, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        selectDB()

        view.fab.setOnClickListener{
            val intent = Intent(requireContext(),AddTodoActivity::class.java)
            startActivityForResult(intent,10)
        }
        return view
    }

    private fun selectDB(){
        list = mutableListOf()
        val helper = DBHelper(requireContext())
        val db = helper.readableDatabase
        val cursor = db.rawQuery("select * from tb_todo order by date desc", null)

        var preDate: Calendar? = null
        while (cursor.moveToNext()) {
            val dbdate=cursor.getString(3)
            val date = SimpleDateFormat("yyyy-MM-dd").parse(dbdate)
            val currentDate = GregorianCalendar()
            currentDate.time = date

            if (!currentDate.equals(preDate)) {
                val headerItem = HeaderItem(dbdate)
                list.add(headerItem)
                preDate=currentDate
            }

            val completed= cursor.getInt(4) != 0
            val dataItem = DataItem(cursor.getInt(0), cursor.getString(1), cursor.getString(2), completed)
            list.add(dataItem)
        }

        Log.d("kkang", "list size ${list.size}")
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = MyAdapter(list)
        recyclerView.addItemDecoration(MyDecoration())

    }

    // 추가: 아이템 최신화
    override fun onResume() {
        super.onResume()
        selectDB()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode==10 && resultCode==Activity.RESULT_OK){
            selectDB()
        }
    }

    class HeaderViewHolder(view: View): RecyclerView.ViewHolder(view){
        val headerView = view.itemHeaderView

    }
    class DataViewHolder(view: View): RecyclerView.ViewHolder(view){
        val completedIconView = view.completedIconView
        val itemTitleView = view.itemTitleView
        val itemContentView = view.itemContentView
        //추가
        val itemDeleteView = view.itemDelete
        val itemModifyView = view.itemModify
    }


    inner class MyAdapter(val list: MutableList<ItemYO>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        override fun getItemViewType(position: Int): Int {
            return list.get(position).type
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            if (viewType == ItemYO.TYPE_HEADER) {
                val layoutInflater = LayoutInflater.from(parent?.context)
                return HeaderViewHolder(layoutInflater.inflate(R.layout.item_header, parent,false))
            } else {
                val layoutInflater = LayoutInflater.from(parent?.context)
                return DataViewHolder(layoutInflater.inflate(R.layout.item_main, parent, false))
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val itemYO = list.get(position)
            if (itemYO.type == ItemYO.TYPE_HEADER) {
                val viewHolder = holder as HeaderViewHolder
                val headerItem = itemYO as HeaderItem
                viewHolder.headerView.setText(headerItem.data) //  viewHolder.headerView.setText(headerItem.date)
            } else {
                val viewHolder = holder as DataViewHolder
                val dataItem = itemYO as DataItem
                viewHolder.itemTitleView.setText(dataItem.title)
                viewHolder.itemContentView.setText(dataItem.content)

                if(dataItem.completed){
                    viewHolder.completedIconView.setImageResource(R.drawable.icon_completed)
                }else {
                    viewHolder.completedIconView.setImageResource(R.drawable.icon_png)
                }

                viewHolder.completedIconView.setOnClickListener{
                    val helper = DBHelper(requireContext()) // val helper = DBHelper(this@GridFragment)
                    val db=helper.writableDatabase

                    if (dataItem.completed){
                        db.execSQL("update tb_todo set completed=? where _id=?", arrayOf(0, dataItem.id))
                        viewHolder.completedIconView.setImageResource(R.drawable.icon_png)
                    }else {
                        db.execSQL("update tb_todo set completed=? where _id=?", arrayOf(1, dataItem.id))
                        viewHolder.completedIconView.setImageResource(R.drawable.icon_completed)
                    }
                    dataItem.completed = !dataItem.completed
                    db.close()
                }
                //추가 : 수정 버튼 클릭시
                viewHolder.itemModifyView.setOnClickListener {
                    val intent = Intent(requireContext(), ModifyTodoActivity::class.java)
                    intent.putExtra("dataID", dataItem.id)
                    startActivity(intent)

                }

                //추가
                viewHolder.itemDeleteView.setOnClickListener {
                    val helper = DBHelper(requireContext()) // val helper = DBHelper(this@GridFragment)
                    val db=helper.writableDatabase

                    AlertDialog.Builder(requireContext())
                        .setTitle("삭제")
                        .setMessage("삭제하시겠습니까?")
                        .setPositiveButton("삭제") { dialog, _ ->

                            db.execSQL("DELETE FROM tb_todo WHERE _id=?", arrayOf(dataItem.id))

                            selectDB()

                            dialog.dismiss()
                        }
                        .setNegativeButton("취소") { dialog, _ ->

                            dialog.dismiss()
                        }
                        .show()

                }
            }
        }

        override fun getItemCount(): Int {
            return list.size
        }
    }
    inner class MyDecoration(): RecyclerView.ItemDecoration(){
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            super.getItemOffsets(outRect, view, parent, state)
            val index = parent!!.getChildAdapterPosition(view)
            Log.d("kkang","index $index....list size: ${list.size}")
            val itemYO = list.get(index)
            if (itemYO.type == ItemYO.TYPE_DATA) {
                view!!.setBackgroundColor(0xFFFFFFFF.toInt())
                ViewCompat.setElevation(view, 10.0f)
            }
            outRect!!.set(20,10, 20,10)
        }
    }
}

abstract class ItemYO {
    abstract val type :Int
    companion object {
        val TYPE_HEADER = 0
        val TYPE_DATA = 1
    }
}
class HeaderItem(var data: String): ItemYO(){
    override val type: Int
    get() = ItemYO.TYPE_HEADER
}

class DataItem(var id: Int, var title: String, var content: String, var completed: Boolean=false): ItemYO() {
    override val type: Int
    get() = ItemYO.TYPE_DATA
}

