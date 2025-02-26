package com.example.studylog.navigation.model

import java.nio.file.StandardWatchEventKinds
import java.sql.Timestamp

data class AlarmDTO (
    var destinationUid : String? = null,
    var userId : String? = null,
    var uid : String? = null,
    var kinds: Int? = null,
    var message : String? = null,
    var timestamp: Long? = null
)
