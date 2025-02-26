package com.example.studylog.navigation.alarm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studylog.navigation.alarm.datamodel.ProcessingStudyLog
import com.example.studylog.navigation.alarm.datamodel.StudyLogDTO
import com.example.studylog.navigation.alarm.repository.FirestoreRepository
import com.example.studylog.navigation.alarm.shared.toLocalDateTime
import com.example.studylog.navigation.alarm.shared.toSecondFormat
import com.example.studylog.navigation.alarm.shared.toStringFormat
import com.example.studylog.navigation.alarm.shared.toTimeFormat
import com.example.studylog.navigation.alarm.stopwatch.Stopwatch
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import java.time.*

class AlarmFragmentViewModel : ViewModel() {
    private val TAG = AlarmFragmentViewModel::class.java.simpleName
    private val firestoreRepo = FirestoreRepository()

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean>
        get() = _loading

    private val _nowDate = MutableLiveData<Long>()
    val nowDate: LiveData<Long>
        get() = _nowDate
    private val _studyLogList = MutableLiveData<List<StudyLogDTO>>()
    val studyLogList: LiveData<List<StudyLogDTO>>
        get() = _studyLogList
    private val _selectStudyLog = MutableLiveData<StudyLogDTO?>()
    val selectStudyLog: LiveData<StudyLogDTO?>
        get() = _selectStudyLog
    private val _timerTime = MutableLiveData("00:00:00")
    val timerTime: LiveData<String>
        get() = _timerTime
    private val _nowPlaying = MutableLiveData(false)
    val nowPlaying: LiveData<Boolean>
        get() = _nowPlaying
    private val _todayTime = MutableLiveData("00:00:00")
    val todayTime: LiveData<String>
        get() = _todayTime
    private val _processData = MutableLiveData<Map<LocalDate, List<ProcessingStudyLog>>>()
    val processData: LiveData<Map<LocalDate, List<ProcessingStudyLog>>>
        get() = _processData
    private val _processDataMonth =
        MutableLiveData<Map<Int, Map<Month?, List<ProcessingStudyLog>>>>()
    val processDataMonth: LiveData<Map<Int, Map<Month?, List<ProcessingStudyLog>>>>
        get() = _processDataMonth
    val stopWatch = Stopwatch()

    init {
        stopWatch.tickListener = object :  Stopwatch.TickListener{
            override fun onTick(nowTime: Long, todayTime : Long) {
                _timerTime.postValue(nowTime.toTimeFormat())
                _todayTime.postValue(todayTime.toTimeFormat())
            }
        }
    }


    fun getNowDate() {
        Log.d(TAG, (LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) * 1000).toString())
        _nowDate.value =
            LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun getStudyLogData() {
        _loading.value = true
        val uid = Firebase.auth.uid
        viewModelScope.launch(Dispatchers.IO) {
            if (uid != null) {
                // 최초 문서 존재여부 확인
                val isDocumentExist = firestoreRepo.existDocument(uid)
                if (!isDocumentExist) firestoreRepo.addDocument(uid)

                val documentSnapshot = firestoreRepo.getStudyLogByUid(uid)
                if (documentSnapshot.exists()) {
                    Log.d(TAG, "data exists")
                    val studyLogList = mappingToStudyLogDTOList(documentSnapshot)
                    withContext(Dispatchers.Main) {
                        _studyLogList.value = studyLogList
                        _loading.value = false
                    }
                } else {
                    Log.d(TAG, "data no exist")
                    withContext(Dispatchers.Main) {
                        _loading.value = false
                    }
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun mappingToStudyLogDTOList(documentSnapshot: DocumentSnapshot): List<StudyLogDTO> {
        val studyLogList = mutableListOf<StudyLogDTO>()
        documentSnapshot.data!!.forEach {
            val subjectName = it.key
            val dataMap = it.value as Map<String, Long>
            val dataList = mutableListOf<StudyLogDTO.ProgressData>()
            Log.d(TAG, dataMap.toString())
            dataMap.forEach { document ->
                val date = document.key
                val progressTime = document.value

                val data = StudyLogDTO.ProgressData(date, progressTime)
                dataList.add(data)
            }
            val studyLogDTO = StudyLogDTO(subjectName, dataList)
            Log.d(TAG, studyLogDTO.toString())

            studyLogList.add(studyLogDTO)
        }

        return studyLogList
    }


    fun selectStudyLog(studyLogDTO: StudyLogDTO) {
        _selectStudyLog.value = studyLogDTO
        _timerTime.value = getTodayTimeByStudyLog().toTimeFormat()
    }

    fun getTodayTime() {
        val todayStudyLog = _studyLogList.value!!
        var todayTotalTime = 0L
        for (studyLog in todayStudyLog) {
            for(data in studyLog.dataList){
                if (data.date == LocalDateTime.now().toStringFormat()) {
                    todayTotalTime += data.progressTime
                }
            }
        }
        if(!stopWatch.isProgress){
            _todayTime.value = todayTotalTime.toTimeFormat()
        }
    }

    fun addSubject(name: String) {
        _loading.value = true
        val uid = Firebase.auth.uid
        viewModelScope.launch(Dispatchers.IO) {
            if (uid != null) {
                firestoreRepo.addSubjectByUid(uid, name)
                reload(uid)
            }
        }
    }

    fun alterSubjectName(alterName: String, studyLogDTO: StudyLogDTO) {
        _loading.value = true
        val uid = Firebase.auth.uid
        viewModelScope.launch(Dispatchers.IO) {
            if (uid != null) {
                if (_selectStudyLog.value == studyLogDTO) {
                    withContext(Dispatchers.Main) {
                        _selectStudyLog.value = null
                    }
                }
                firestoreRepo.alterSubjectNameByUid(
                    uid, alterName, studyLogDTO.subjectName, studyLogDTO.dataList
                )
                reload(uid)
            }
        }
    }

    fun deleteSubject(studyLogDTO: StudyLogDTO) {
        _loading.value = true
        val uid = Firebase.auth.uid
        viewModelScope.launch(Dispatchers.IO) {
            if (uid != null) {
                if (_selectStudyLog.value != null) {
                    if (_selectStudyLog.value == studyLogDTO) {
                        withContext(Dispatchers.Main) {
                            _selectStudyLog.value = null
                        }
                    }
                }
                firestoreRepo.deleteSubject(uid, studyLogDTO.subjectName)
                reload(uid)
            }
        }
    }

    private suspend fun reload(uid: String) {
        val documentSnapshot = firestoreRepo.getStudyLogByUid(uid)
        if (documentSnapshot.exists()) {
            Log.d(TAG, "data exists")
            val studyLogList = mappingToStudyLogDTOList(documentSnapshot)
            withContext(Dispatchers.Main) {
                _studyLogList.value = studyLogList
                _loading.value = false
            }

            val prevSelectStudyLogDTO = _selectStudyLog.value
            if (prevSelectStudyLogDTO != null) {
                reloadSelectSubject(prevSelectStudyLogDTO, studyLogList)
            }

        } else {
            Log.d(TAG, "data no exist")
            _studyLogList.value = emptyList()
            _selectStudyLog.value = null
        }
    }

    private suspend fun reloadSelectSubject(
        studyLogDTO: StudyLogDTO, studyLogDtoList: List<StudyLogDTO>
    ) {
        val findSubject = studyLogDTO.subjectName
        studyLogDtoList.forEach {
            if (it.subjectName == findSubject) {
                withContext(Dispatchers.Main) {
                    selectStudyLog(it)
                }
            }
        }
    }

    fun startTimer() {
        _nowPlaying.value = true
        stopWatch.startTime = getTodayTimeByStudyLog()
        stopWatch.todayTime = _todayTime.value.toString().toSecondFormat()
        stopWatch.start()
    }

    fun stopTimer() {
        _nowPlaying.value = false
        val progressTime = stopWatch.progressTime
        Log.d(TAG, progressTime.toString())
        saveTimeInSubject(progressTime)
        stopWatch.pause()
    }

    private fun saveTimeInSubject(progressTime: Long) {
        _loading.value = true
        val uid = Firebase.auth.uid
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, getTodayTimeByStudyLog().toString())
            val updateTime = getTodayTimeByStudyLog() + progressTime
            if (uid != null) {
                firestoreRepo.updateTimeByDate(
                    uid,
                    selectStudyLog.value!!.subjectName,
                    LocalDateTime.now().toStringFormat(),
                    updateTime,
                    _selectStudyLog.value!!.dataList
                )
                reload(uid)
                withContext(Dispatchers.Main) {
                    _loading.value = false
                }
            }
        }

    }

    private fun getTodayTimeByStudyLog() : Long{
        var totalTime = 0L
        for(data in _selectStudyLog.value!!.dataList){
            if(data.date == LocalDateTime.now().toStringFormat()){
                totalTime += data.progressTime
            }
        }

        return totalTime
    }

    fun dataProcessingForStatistic() {
        val studyLogDtoList = _studyLogList.value
        _loading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            // make list
            val processingDataList = mutableListOf<ProcessingStudyLog>()
            if (studyLogDtoList != null) {
                for (studyLogDto in studyLogDtoList) {
                    val subjectName = studyLogDto.subjectName
                    for (progressData in studyLogDto.dataList) {
                        val date = progressData.date.toLocalDateTime()
                        val progressTime = progressData.progressTime
                        val processingStudyLog = ProcessingStudyLog(subjectName, date, progressTime)
                        processingDataList.add(processingStudyLog)
                    }
                }
            }

            // list grouping to map
            val groupingData = processingDataList.groupBy {
                it.date
            }
            withContext(Dispatchers.Main) {
                _processData.value = groupingData
                _loading.value = false
            }
        }
    }

    fun dataProcessingForStatisticMonth() {
        val studyLogDtoList = _studyLogList.value
        if (studyLogDtoList != null) {
            _loading.value = true

            viewModelScope.launch(Dispatchers.IO) {
                // make list
                val processingDataList = mutableListOf<ProcessingStudyLog>()
                for (studyLogDto in studyLogDtoList) {
                    val subjectName = studyLogDto.subjectName
                    for (progressData in studyLogDto.dataList) {
                        val date = progressData.date.toLocalDateTime()
                        val progressTime = progressData.progressTime
                        val processingStudyLog = ProcessingStudyLog(subjectName, date, progressTime)
                        processingDataList.add(processingStudyLog)
                    }
                }

                // list grouping to map
                val groupingDataByYear = processingDataList.groupBy {
                    it.date.year
                }

                val statisticsForMonth = mutableMapOf<Int, Map<Month?, List<ProcessingStudyLog>>>()

                for (key in groupingDataByYear.keys) {
                    val list = groupingDataByYear[key]
                    val mappingByMonth = list?.groupBy {
                        it.date.month
                    }

                    if (mappingByMonth != null) {
                        statisticsForMonth[key] = mappingByMonth
                    }
                }

                withContext(Dispatchers.Main) {
                    _processDataMonth.value = statisticsForMonth
                    _loading.value = false
                }
            }
        }
    }
}