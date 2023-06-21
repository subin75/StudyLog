package com.example.studylog.navigation.model

import java.sql.Timestamp

data class ContentDTO(
    var explain : String? = null,
    var imageUrl : String? = null,
    var uid : String? = null,
    var userId : String? = null,
    var timestamp: Long? = null,

    var title: String? = null,
    var description: String? = null,

    //추가
    var id: String? = null,

    var favoriteCount : Int = 0,
    var favorites : MutableMap<String,Boolean> = HashMap(),

    //찜 추가(수정 후)
    var zzimCount: Int = 0, // 찜 개수
    var zzimList: ArrayList<String> = arrayListOf() // 찜한 사용자 목록
){
    data class Comment(
        var uid: String? = null,
        var userId: String? = null,
        var comment : String? = null,
        var timestamp : Long? = null)

}