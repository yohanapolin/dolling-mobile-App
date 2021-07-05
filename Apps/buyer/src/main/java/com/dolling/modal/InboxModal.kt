package com.dolling.modal

import com.google.firebase.Timestamp

data class InboxModal(
    val title: String = "",
    val message: String = "",
    val timestamp: Timestamp = Timestamp.now()
)
