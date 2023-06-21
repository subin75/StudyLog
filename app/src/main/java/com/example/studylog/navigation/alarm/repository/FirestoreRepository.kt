package com.example.studylog.navigation.alarm.repository

import com.example.studylog.navigation.alarm.datamodel.StudyLogDTO
import com.example.studylog.navigation.alarm.shared.toStringFormat
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.ZoneOffset

class FirestoreRepository {
    private val TAG = FirestoreRepository::class.java.simpleName
    private val db = Firebase.firestore
    private val studyLog = "studyLog"

    suspend fun existDocument(uid:String) : Boolean{
        val docRef = db.collection(studyLog).document(uid)
        val result = docRef.get().await()
        return result.exists()
    }

    suspend fun addDocument(uid: String){
        val docRef = db.collection(studyLog).document(uid)
        val map = mutableMapOf<String, Long>()
        val tempKey = (LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) * 1000).toString()
        map.put(tempKey, 0)
        docRef.set(map).await()

        val updates = hashMapOf<String, Any>(
            tempKey to FieldValue.delete(),
        )
        docRef.update(updates).await()
    }

    suspend fun getStudyLogByUid(uid: String) : DocumentSnapshot {
        val docRef = db.collection(studyLog).document(uid)
        val result = docRef.get().await()
        return result
    }

    suspend fun addSubjectByUid(uid : String, subjectName : String){
        val docRef = db.collection(studyLog).document(uid)
        val map = mutableMapOf<String, Long>()
        map.put(LocalDateTime.of(1, 1, 1, 1, 1).toStringFormat(), 0)
        docRef.update(subjectName, map).await()
    }

    suspend fun alterSubjectNameByUid(uid : String, newSubjectName: String, prevSubjectName : String,dataList : List<StudyLogDTO.ProgressData>){
        val docRef = db.collection(studyLog).document(uid)

        val updates = hashMapOf<String, Any>(
            prevSubjectName to FieldValue.delete(),
        )

        docRef.update(updates).await()

        val map = mutableMapOf<String, Long>()
        for (progressData in dataList){
            map.put(progressData.date, progressData.progressTime)
        }

        docRef.update(newSubjectName, map).await()
    }

    suspend fun deleteSubject(uid : String, deleteSubjectName : String){
        val docRef = db.collection(studyLog).document(uid)

        val updates = hashMapOf<String, Any>(
            deleteSubjectName to FieldValue.delete(),
        )
        docRef.update(updates).await()
    }

    suspend fun updateTimeByDate(uid : String, subjectName: String, date:String, updateTime : Long, prevData : List<StudyLogDTO.ProgressData>){
        val docRef = db.collection(studyLog).document(uid)
        val map =  mutableMapOf<String, Long>()
        for(progressData in prevData){
            map[progressData.date] = progressData.progressTime
        }
        map[date] = updateTime

        docRef.update(subjectName, map).await()
    }
}