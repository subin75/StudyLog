package com.example.studylog

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.app.AlertDialog
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.StateSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.studylog.databinding.ItemUserPhotoBinding
import com.example.studylog.databinding.ItemUserPostBinding
import com.example.studylog.model.ZzimListItem
import com.example.studylog.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_add_todo.*
import kotlinx.android.synthetic.main.activity_user_list.*
import kotlinx.android.synthetic.main.activity_user_post.*
import kotlinx.android.synthetic.main.fragment_user.view.*
import kotlinx.android.synthetic.main.item_datail.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class UserPostActivity : AppCompatActivity() {
    var firestore: FirebaseFirestore? = null
    var uid: String? = null
    var userId: String? = null
    var fromUser: Boolean = false
    var auth: FirebaseAuth? = null
    var currentUserUid: String? = null

    var contentDTOs: ArrayList<ContentDTO> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_user_post)


        uid = intent.getStringExtra("uid")
        if(uid == null){
            uid = FirebaseAuth.getInstance().uid
        }
        userId = intent.getStringExtra("userid")
        fromUser = intent.getBooleanExtra("fromUser",true)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        currentUserUid = auth?.currentUser?.uid
        auth = Firebase.auth

    }


    override fun onResume() {
        super.onResume()
        loadPage()

    }
    private fun loadPage() {
        uid = FirebaseAuth.getInstance().uid
        contentDTOs.clear()
        if(fromUser) {
            firestore?.collection("images")?.whereEqualTo("uid", uid)
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    //Sometimes, This code return null of querySnapshot when it signout
                    if (querySnapshot == null) return@addSnapshotListener

                    //Get data
                    for (snapshot in querySnapshot.documents) {
                        contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                    }

                    user_post_rc.adapter = UserFragmentRecyclerViewAdapter()
                    user_post_rc.layoutManager = LinearLayoutManager(baseContext);


                }
        } else {
            //찜목록 로딩
            uid?.let {
                firestore?.collection("images")?.whereArrayContains("zzimList", it)
                    ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                        //Sometimes, This code return null of querySnapshot when it signout
                        if (querySnapshot == null) return@addSnapshotListener
                        //Get data
                        for (snapshot in querySnapshot.documents) {
                            contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                        }

                        user_post_rc.adapter = UserFragmentRecyclerViewAdapter()
                        user_post_rc.layoutManager = LinearLayoutManager(baseContext);
                    }

            }

        }
    }



    inner class UserFragmentRecyclerViewAdapter :
        RecyclerView.Adapter<UserFragmentRecyclerViewAdapter.CustomViewHolder>() {


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
            return CustomViewHolder(
                ItemUserPostBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }

        fun deletePhotoItem(item: ContentDTO) {
            if(fromUser){
                firestore?.collection("images")?.whereEqualTo("uid", uid)
                    ?.whereEqualTo("imageUrl", item.imageUrl)
                    ?.get()
                    ?.addOnSuccessListener { querySnapshot ->
                        for (document in querySnapshot.documents) {
                            document.reference.delete()
                        }
                    }
                    ?.addOnFailureListener { exception ->
                    }
                val zzimdata = firestore?.collection("zzim")?.document(uid.toString())
                    ?.collection("MyZzimList")
                firestore?.runTransaction { transaction ->
                    zzimdata?.get()?.addOnSuccessListener { res ->
                        for (doc in res) {
                            if (item.imageUrl.toString()
                                    .equals(doc.data["ContentImageUrl"].toString())
                            ) {
                                zzimdata?.document(doc.id)?.delete()
                            }
                        }
                    }
                }
                firestore?.collection("images")
                    ?.whereEqualTo("imageUrl", item.imageUrl)
                    ?.get()
                    ?.addOnSuccessListener { querySnapshot ->
                        for (document in querySnapshot.documents) {
                            val zzimList = document.get("zzimList") as MutableList<*>
                            val zzimCount = document.getLong("zzimCount")

                            // zzimList 필드에서 uid 제거
                            zzimList?.remove(uid)

                            // zzimCount 필드 감소
                            val newZzimCount = (zzimCount ?: 0) - 1

                            // 업데이트할 데이터 생성
                            val updatedData = hashMapOf<String, Any>(
                                "zzimList" to zzimList,
                                "zzimCount" to newZzimCount
                            )

                            // 문서 업데이트
                            firestore?.collection("images")?.document(document.id)
                                ?.update(updatedData)
                                ?.addOnSuccessListener {
                                    // 업데이트 성공 처리
                                }
                                ?.addOnFailureListener { exception ->
                                    // 업데이트 실패 처리
                                }
                        }
                    }
                    ?.addOnFailureListener { exception ->
                    }

            } else {
                val zzimdata = firestore?.collection("zzim")?.document(uid.toString())
                    ?.collection("MyZzimList")
                firestore?.runTransaction { transaction ->
                    zzimdata?.get()?.addOnSuccessListener { res ->
                        for (doc in res) {
                            if (item.imageUrl.toString()
                                    .equals(doc.data["ContentImageUrl"].toString())
                            ) {
                                zzimdata?.document(doc.id)?.delete()
                            }
                        }
                    }
                }
                firestore?.collection("images")
                ?.whereEqualTo("imageUrl", item.imageUrl)
                    ?.get()
                    ?.addOnSuccessListener { querySnapshot ->
                        for (document in querySnapshot.documents) {
                            val zzimList = document.get("zzimList") as MutableList<*>
                            val zzimCount = document.getLong("zzimCount")

                            // zzimList 필드에서 uid 제거
                            zzimList?.remove(uid)

                            // zzimCount 필드 감소
                            val newZzimCount = (zzimCount ?: 0) - 1

                            // 업데이트할 데이터 생성
                            val updatedData = hashMapOf<String, Any>(
                                "zzimList" to zzimList,
                                "zzimCount" to newZzimCount
                            )

                            // 문서 업데이트
                            firestore?.collection("images")?.document(document.id)
                                ?.update(updatedData)
                                ?.addOnSuccessListener {
                                    // 업데이트 성공 처리
                                }
                                ?.addOnFailureListener { exception ->
                                    // 업데이트 실패 처리
                                }
                        }
                    }
                    ?.addOnFailureListener { exception ->
                    }
            }

            contentDTOs.clear()
            notifyDataSetChanged()

        }


        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            holder.bind(contentDTOs[position])

        }

        inner class CustomViewHolder(private val binding: ItemUserPostBinding) :
            BaseViewHolder<ContentDTO>(binding) {
            override fun define(item: ContentDTO) {
                Glide.with(binding.root.context).load(item.imageUrl).into(binding.itemImage)
                binding.itemExplain.text = item.explain
                firestore?.collection("profileImages")?.document(contentDTOs!![adapterPosition].uid!!)
                    ?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                        if (documentSnapshot == null) return@addSnapshotListener
                        if (documentSnapshot.data != null) {
                            var url = documentSnapshot.data!!["image"]
                            Glide.with(this@UserPostActivity).load(url).apply(RequestOptions().circleCrop())
                                .into(binding.detailviewitemProfileImage)
                        }
                    }
                binding.detailviewitemProfileTextview.text = contentDTOs!![adapterPosition].userId
                binding.itemOption.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {

                        //item 클릭시, option dialog 를 보여줌

                        AlertDialog.Builder(this@UserPostActivity)
                            .setTitle("수정/삭제")
                            .setMessage("수정하시겠습니까?" + "삭제하시겠습니까?")
                            .setPositiveButton("삭제") { dialog, _ ->
                                deletePhotoItem(contentDTOs[position])
                                dialog.dismiss()
                            }
                            .setNegativeButton("수정") { dialog, _ ->
                                val intent =
                                    Intent(this@UserPostActivity, ModifyPhotoActivity::class.java)
                                intent.putExtra("uid", contentDTOs[position].uid)
                                intent.putExtra("url", contentDTOs[position].imageUrl)
                                startActivity(intent)

                                dialog.dismiss()
                            }
                            .setNeutralButton("취소") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                    }

                }
            }
        }
    }
}