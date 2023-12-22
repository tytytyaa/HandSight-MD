package com.example.handsight

import java.util.concurrent.TimeUnit

fun getTimestampFormatString(seconds: Long): String {
    val currentTimeSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
    val timeDifferenceSeconds = currentTimeSeconds - seconds

    return when {
        timeDifferenceSeconds < 60 -> "Just now"
        timeDifferenceSeconds < 3600 -> "${timeDifferenceSeconds / 60} minutes ago"
        timeDifferenceSeconds < 86400 -> "${timeDifferenceSeconds / 3600} hours ago"
        timeDifferenceSeconds < 2592000 -> "${timeDifferenceSeconds / 86400} days ago"
        else -> "Long time ago"
    }
}