package com.example.studylog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_add_todo.*
import kotlinx.android.synthetic.main.activity_modify_todo.*
import kotlinx.android.synthetic.main.activity_modify_todo.addContentEditView
import kotlinx.android.synthetic.main.activity_modify_todo.addDateView
import kotlinx.android.synthetic.main.activity_modify_todo.addTitleEditView
import kotlinx.android.synthetic.main.fragment_grid.*
import java.text.SimpleDateFormat
import java.util.*

class ModifyTodoActivity : AppCompatActivity() {
    var dataid = -1
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_todo)

        val data = Date()
        val sdFormat = SimpleDateFormat("yyyy-MM-dd")
        addDateView.text = sdFormat.format(data)
        addDateView.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val dateDialog = DatePickerDialog(this, object : DatePickerDialog.OnDateSetListener {
                override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
                    addDateView.text = "$year-${month+1}-$dayOfMonth"  //addDateView.text = "$year-${monthOfYear+1}-$dayOfMonth"
                }
            }, year, month, day).show()
        }

        dataid = intent.getIntExtra("dataID",-1)
        getItemFromDB(dataid)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add, menu)
        return super.onCreateOptionsMenu(menu)
    }

    fun getItemFromDB(id: Int){
        val helper = DBHelper(this)
        val db = helper.writableDatabase
        val cursor = db.query(
            "tb_todo", // 테이블 이름
            null, // 반환할 열 배열 (null일 경우 모든 열을 반환합니다)
            "_id=?", // 조건
            arrayOf(id.toString()), // 조건 매개변수
            null, // GROUP BY
            null, // HAVING
            null // ORDER BY
        )

        if (cursor.moveToFirst()) {
            val titleIndex = cursor.getColumnIndex("title")
            val contentIndex = cursor.getColumnIndex("content")
            val dateIndex = cursor.getColumnIndex("date")

            if (titleIndex >= 0) {
                val title = cursor.getString(titleIndex)
                addTitleEditView.setText(title)
            }

            if (contentIndex >= 0) {
                val content = cursor.getString(contentIndex)
                addContentEditView.setText(content)
            }

            if (dateIndex >= 0) {
                val date = cursor.getString(dateIndex)
                addDateView.setText(date)
            }
        }

        cursor.close()
        db.close()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item?.itemId==R.id.menu_add){
            if(addTitleEditView.text.toString() != null && addContentEditView.text.toString() != null){
                val helper = DBHelper(this)
                val db = helper.writableDatabase

                val id = dataid
                val contentValues = ContentValues()

                contentValues.put("title",addTitleEditView.text.toString())
                contentValues.put("content",addContentEditView.text.toString())
                contentValues.put("date", addDateView.text.toString());
                contentValues.put("completed",0)

                db.update("tb_todo",contentValues,"_id=?", arrayOf(id.toString()))


                db.close()

                finish()
            }else{
                Toast.makeText(this,"모든 데이터가 입력되지 않았습니다", Toast.LENGTH_LONG).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
