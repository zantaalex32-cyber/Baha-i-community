package com.example.data.model

import com.google.firebase.Timestamp

data class UserProfile(
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val clusterName: String = "Northern Cascades Cluster",
    val neighborhood: String = "Riverside",
    val role: String = "Neighborhood Facilitator",
    val phone: String = "",
    val encryptedNotes: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

data class ClusterInfo(
    val id: String = "",
    val name: String = "",
    val region: String = "",
    val milestone: String = "Milestone 2",
    val currentCycle: Int = 1,
    val goalsSummary: String = "",
    val userId: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

data class Neighborhood(
    val id: String = "",
    val clusterId: String = "",
    val name: String = "",
    val focusSector: String = "Sector A",
    val facilitatorName: String = "",
    val facilitatorContact: String = "",
    val activeActivitiesCount: Int = 0,
    val notes: String = "",
    val userId: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

data class CoreActivity(
    val id: String = "",
    val clusterId: String = "",
    val neighborhoodId: String = "",
    val neighborhoodName: String = "",
    val title: String = "",
    val type: String = "Study Circle", // Study Circle, Junior Youth Group, Children's Class, Devotional Gathering, Community Service
    val animatorOrTutor: String = "",
    val meetingDay: String = "",
    val location: String = "",
    val participantsCount: Int = 0,
    val bookOrTopic: String = "",
    val status: String = "Active", // Active, Planning, Completed
    val userId: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

data class EventSchedule(
    val id: String = "",
    val clusterId: String = "",
    val neighborhoodName: String = "",
    val title: String = "",
    val category: String = "Nineteen Day Feast", // Nineteen Day Feast, Holy Day, Cluster Reflection Meeting, Core Activity, Youth Gathering, Special Event
    val dateTimeMillis: Long = 0L,
    val location: String = "",
    val host: String = "",
    val description: String = "",
    val encryptedCoordinationNotes: String = "",
    val attendeesCount: Int = 0,
    val userId: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
