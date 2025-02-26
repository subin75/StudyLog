package com.example.studylog

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context): SQLiteOpenHelper(context, "tododb",null,1){
    override fun onCreate(p0: SQLiteDatabase?) {
        val memoSql="create table tb_todo ("+
                "_id integer primary key autoincrement," +
                "title," +
                "content," +
                "date," +
                "completed)"

        p0?.execSQL(memoSql)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        p0?.execSQL("drop table tb_todo")
        onCreate(p0)
    }
}