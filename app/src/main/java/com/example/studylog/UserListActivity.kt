package com.example.studylog

import android.media.Image
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.StateSet.TAG
import androidx.recyclerview.widget.GridLayoutManager
import com.example.studylog.model.ZzimListItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_user_list.*

class UserListActivity : AppCompatActivity() {

    lateinit var auth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)

        auth = Firebase.auth

        loadPage()

        backButton.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        loadPage()
    }
    private fun loadPage() {
        val db = Firebase.firestore
        val RamList = ArrayList<ZzimListItem>()
        val curUser = auth.currentUser?.uid.toString()
        UserZzimList.layoutManager = GridLayoutManager(this, 3)

        db.collection("zzim").document(curUser).collection("MyZzimList").get().addOnSuccessListener { res ->
            if(res != null) {
                for(doc in res) {
                    val Name = doc.get("ContentUid").toString()
                    val ImageUrl = doc.get("ContentImageUrl").toString()
                    Log.d(TAG, "Name: $Name")
                    Log.d(TAG, "ImageUrl: ${ImageUrl}")
                    //firestore에서 데이터를 가져와 RecyclerView에 추가
                    if(ImageUrl != "" && ImageUrl != null){
                        RamList.add(ZzimListItem(Name, ImageUrl))
                        Log.d(TAG,"리스트: $RamList")
                    }

                }

                val RamAdapter = UserZzimListAdapter(this, RamList)
                UserZzimList.adapter = RamAdapter
            }
        }

    }
}