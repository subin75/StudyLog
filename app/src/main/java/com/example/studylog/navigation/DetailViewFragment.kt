package com.example.studylog.navigation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.StateSet.TAG
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.studylog.LoginActivity
import com.example.studylog.R
import com.example.studylog.navigation.model.AlarmDTO
import com.example.studylog.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.fragment_user.view.*
import kotlinx.android.synthetic.main.item_datail.*
import kotlinx.android.synthetic.main.item_datail.view.*
import java.util.*
import kotlin.collections.ArrayList

class DetailViewFragment : Fragment() {
    var firestore: FirebaseFirestore? = null
    var uid: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail, container, false)
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid
        view.detailviewfragment_recyclerview.adapter = DetailViewRecyclerViewAdapter()
        view.detailviewfragment_recyclerview.layoutManager = LinearLayoutManager(activity)

        return view
    }
    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        var contentUidList: ArrayList<String> = arrayListOf()

        init {
            firestore?.collection("images")?.orderBy("timestamp", Query.Direction.DESCENDING)
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    contentDTOs.clear()
                    contentUidList.clear()
                    //Sometimes, This code return null of querySnapshot when it signout
                    if (querySnapshot == null) return@addSnapshotListener

                    for (snapshot in querySnapshot!!.documents) {
                        var item = snapshot.toObject(ContentDTO::class.java)
                        contentDTOs.add(item!!)
                        contentUidList.add(snapshot.id)
                    }
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(p0.context).inflate(R.layout.item_datail, p0, false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            var viewholder = (p0 as CustomViewHolder).itemView

            //UserId
            viewholder.detailviewitem_profile_textview.text = contentDTOs!![p1].userId

            // fixme
            // profile image
            firestore?.collection("profileImages")?.document(contentDTOs!![p1].uid!!)
                ?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                    if (documentSnapshot == null) return@addSnapshotListener
                    if (documentSnapshot.data != null) {
                        var url = documentSnapshot.data!!["image"]
                        val context = viewholder.detailviewitem_profile_image.context
                        Glide.with(context).load(url).apply(RequestOptions().circleCrop())
                            .into(viewholder.detailviewitem_profile_image as ImageView)
                    }
                }

            //Image
            Glide.with(p0.itemView.context).load(contentDTOs!![p1].imageUrl)
                .into(viewholder.detailviewitem_imageview_content)

            //Explain of content
            viewholder.detailviewitem_explain_textview.text = contentDTOs!![p1].explain

            //likes
            viewholder.detailviewitem_favoritecounter_textview.text =
                "Likes" + contentDTOs!![p1].favoriteCount

            //This code is when the button is clicked
            viewholder.detailviewitem_favorite_imageview.setOnClickListener {
                favoriteEvent(p1)
            }

            // 찜(북마크) 이미지뷰 클릭 이벤트 처리
            viewholder.zzim.setOnClickListener {
                zzimEvent(p1)
//                val lecture = hashMapOf(
//                    "lecture_title" to contentDTOs[p1].title
//                )
//            val db = FirebaseFirestore.getInstance()
//                .collection("zzim")
//                .document(uid!!)
//                .set(lecture)
//                .addOnSuccessListener {
//                    Toast.makeText(requireContext(), "성공", Toast.LENGTH_LONG).show()
//                }
//                .addOnFailureListener {
//                    Toast.makeText(requireContext(), "실패", Toast.LENGTH_LONG).show()
//                }
            }

            //This code is when the page is loaded
            if (contentDTOs!![p1].favorites.containsKey(uid)) {
                //This is like status
                viewholder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite)
            } else {
                //This is unlike status
                viewholder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite_border)
            }

            //찜 버튼을 눌렀는지 여부 판단(수정 후 / 감자)
            if(contentDTOs!![p1].zzimList.contains(uid)){
                //북마크 완료
                viewholder.zzim.setImageResource(R.drawable.bookmark)
            } else {
                //북마크 취소
                viewholder.zzim.setImageResource(R.drawable.bookmark_border)
            }

            //This code is when the profile image is clicked
            viewholder.detailviewitem_profile_image.setOnClickListener {
                var fragment = UserFragment()
                var bundle = Bundle()
                bundle.putString("destinationUid", contentDTOs[p1].uid)
                bundle.putString("userId", contentDTOs[p1].userId)
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()
                    ?.replace(R.id.main_content, fragment)?.commit()
            }
            viewholder.detailviewitem_comment_imageview.setOnClickListener { v ->
                var intent = Intent(v.context, CommentActivity::class.java)
                intent.putExtra("contentUid", contentUidList[p1])
                startActivity(intent)
            }
        }

        fun zzimEvent(position: Int) {
            if (uid == null) {
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
            val tsDoc = firestore?.collection("images")?.document(contentUidList[position])
            val zzimdata = firestore?.collection("zzim")?.document(uid.toString())?.collection("MyZzimList")

//            val lecture = hashMapOf(
//                "lecture_title" to contentDTOs[position].title
//            )
            firestore?.runTransaction { transaction ->
                val contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

                if (contentDTO!!.zzimList.contains(uid)) {
                    // 이미 찜(북마크)한 경우
                    contentDTO.zzimCount = contentDTO.zzimCount - 1
                    contentDTO.zzimList.remove(uid)
                    zzimdata?.get()?.addOnSuccessListener { res ->
                        for(doc in res){
                            if(contentUidList[position] == doc.data?.get("ContentUid").toString()){
                                zzimdata?.document(doc.data?.get("ContentUid").toString())?.delete()
                            }
                        }
                    }
                } else {
                    // 찜(북마크)하지 않은 경우 , 북마크 취소한 경우
                    contentDTO.zzimCount = contentDTO.zzimCount + 1
                    contentDTO.zzimList.add(uid!!)
                    zzimAlarm(contentDTO.uid!!)
                    //(수정 후 / 감자: 리스트에 추가 할 사진 URI를 firestore로 전송)
                    zzimdata?.document(contentUidList[position])?.set(
                        hashMapOf(
                            "ContentUid" to contentUidList[position],
                            "ContentImageUrl" to contentDTOs[position].imageUrl
                        )
                    )
                }
                transaction.set(tsDoc, contentDTO)
            }
        }

        fun zzimAlarm(destinationUid: String) {
            val alarmDTO = AlarmDTO().apply {
                this.destinationUid = destinationUid
                this.userId = FirebaseAuth.getInstance().currentUser?.email
                this.uid = FirebaseAuth.getInstance().currentUser?.uid
                this.kinds = 1  // 찜(북마크) 알림의 경우 kinds를 1로 설정
                this.timestamp = System.currentTimeMillis()
            }
            FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)
        }

        fun favoriteEvent(position: Int) {
            //fixme
            if (uid == null) {
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }

            var tsDoc = firestore?.collection("images")?.document(contentUidList[position])
            firestore?.runTransaction { transaction ->

                var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

                if (contentDTO!!.favorites.containsKey(uid)) {
                    //when the button is clicked
                    contentDTO.favoriteCount = contentDTO.favoriteCount - 1
                    contentDTO.favorites.remove(uid)
                } else {
                    //When the Button is not clicked
                    contentDTO.favoriteCount = contentDTO.favoriteCount + 1
                    contentDTO.favorites[uid!!] = true
                    favoriteAlarm(contentDTOs[position].uid!!)
                }
                transaction.set(tsDoc, contentDTO)
            }
        }

        fun favoriteAlarm(destinationUid: String) {
            var alarmDTO = AlarmDTO()
            alarmDTO.destinationUid = destinationUid
            alarmDTO.userId = FirebaseAuth.getInstance().currentUser?.email
            alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
            alarmDTO.kinds = 0
            alarmDTO.timestamp = System.currentTimeMillis()
            FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)
        }
    }
}