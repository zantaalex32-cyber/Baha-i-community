package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.ClusterInfo
import com.example.data.model.CoreActivity
import com.example.data.model.EventSchedule
import com.example.data.model.Neighborhood
import com.example.data.model.UserProfile
import com.example.data.security.CryptoManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ClusterRepository(val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) snapshot.toObject(UserProfile::class.java) else null
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                    throw error
                }
        )
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        val uid = requireUserId()
        val path = "users/$uid"
        try {
            val data = hashMapOf<String, Any>(
                "userId" to uid,
                "email" to profile.email,
                "displayName" to profile.displayName,
                "clusterName" to profile.clusterName,
                "neighborhood" to profile.neighborhood,
                "role" to profile.role,
                "phone" to profile.phone,
                "encryptedNotes" to profile.encryptedNotes,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val doc = db.collection("users").document(uid).get().await()
            if (!doc.exists()) {
                data["createdAt"] = FieldValue.serverTimestamp()
            }
            db.collection("users").document(uid).set(data).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            throw e
        }
    }

    fun observeClusters(): Flow<List<ClusterInfo>> = flow {
        val path = "clusters"
        emitAll(
            db.collection("clusters")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(ClusterInfo::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveCluster(cluster: ClusterInfo) {
        val uid = requireUserId()
        val docId = if (cluster.id.isNotBlank()) cluster.id else db.collection("clusters").document().id
        val path = "clusters/$docId"
        try {
            val data = hashMapOf<String, Any>(
                "id" to docId,
                "name" to cluster.name,
                "region" to cluster.region,
                "milestone" to cluster.milestone,
                "currentCycle" to cluster.currentCycle,
                "goalsSummary" to cluster.goalsSummary,
                "userId" to uid,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val existing = db.collection("clusters").document(docId).get().await()
            if (!existing.exists()) {
                data["createdAt"] = FieldValue.serverTimestamp()
            }
            db.collection("clusters").document(docId).set(data).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            throw e
        }
    }

    fun observeNeighborhoods(): Flow<List<Neighborhood>> = flow {
        val path = "neighborhoods"
        emitAll(
            db.collection("neighborhoods")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(Neighborhood::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveNeighborhood(neighborhood: Neighborhood) {
        val uid = requireUserId()
        val docId = if (neighborhood.id.isNotBlank()) neighborhood.id else db.collection("neighborhoods").document().id
        val path = "neighborhoods/$docId"
        try {
            val data = hashMapOf<String, Any>(
                "id" to docId,
                "clusterId" to neighborhood.clusterId,
                "name" to neighborhood.name,
                "focusSector" to neighborhood.focusSector,
                "facilitatorName" to neighborhood.facilitatorName,
                "facilitatorContact" to neighborhood.facilitatorContact,
                "activeActivitiesCount" to neighborhood.activeActivitiesCount,
                "notes" to neighborhood.notes,
                "userId" to uid,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val existing = db.collection("neighborhoods").document(docId).get().await()
            if (!existing.exists()) {
                data["createdAt"] = FieldValue.serverTimestamp()
            }
            db.collection("neighborhoods").document(docId).set(data).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            throw e
        }
    }

    suspend fun deleteNeighborhood(id: String) {
        val path = "neighborhoods/$id"
        try {
            db.collection("neighborhoods").document(id).delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            throw e
        }
    }

    fun observeActivities(): Flow<List<CoreActivity>> = flow {
        val path = "activities"
        emitAll(
            db.collection("activities")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(CoreActivity::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveCoreActivity(activity: CoreActivity) {
        val uid = requireUserId()
        val docId = if (activity.id.isNotBlank()) activity.id else db.collection("activities").document().id
        val path = "activities/$docId"
        try {
            val data = hashMapOf<String, Any>(
                "id" to docId,
                "clusterId" to activity.clusterId,
                "neighborhoodId" to activity.neighborhoodId,
                "neighborhoodName" to activity.neighborhoodName,
                "title" to activity.title,
                "type" to activity.type,
                "animatorOrTutor" to activity.animatorOrTutor,
                "meetingDay" to activity.meetingDay,
                "location" to activity.location,
                "participantsCount" to activity.participantsCount,
                "bookOrTopic" to activity.bookOrTopic,
                "status" to activity.status,
                "userId" to uid,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val existing = db.collection("activities").document(docId).get().await()
            if (!existing.exists()) {
                data["createdAt"] = FieldValue.serverTimestamp()
            }
            db.collection("activities").document(docId).set(data).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            throw e
        }
    }

    suspend fun deleteCoreActivity(id: String) {
        val path = "activities/$id"
        try {
            db.collection("activities").document(id).delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            throw e
        }
    }

    fun observeEvents(): Flow<List<EventSchedule>> = flow {
        val path = "events"
        emitAll(
            db.collection("events")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(EventSchedule::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveEvent(event: EventSchedule) {
        val uid = requireUserId()
        val docId = if (event.id.isNotBlank()) event.id else db.collection("events").document().id
        val path = "events/$docId"
        try {
            val data = hashMapOf<String, Any>(
                "id" to docId,
                "clusterId" to event.clusterId,
                "neighborhoodName" to event.neighborhoodName,
                "title" to event.title,
                "category" to event.category,
                "dateTimeMillis" to event.dateTimeMillis,
                "location" to event.location,
                "host" to event.host,
                "description" to event.description,
                "encryptedCoordinationNotes" to event.encryptedCoordinationNotes,
                "attendeesCount" to event.attendeesCount,
                "userId" to uid,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val existing = db.collection("events").document(docId).get().await()
            if (!existing.exists()) {
                data["createdAt"] = FieldValue.serverTimestamp()
            }
            db.collection("events").document(docId).set(data).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            throw e
        }
    }

    suspend fun deleteEvent(id: String) {
        val path = "events/$id"
        try {
            db.collection("events").document(id).delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            throw e
        }
    }

    suspend fun seedSampleDataIfEmpty() {
        val uid = requireUserId()
        val clustersSnap = db.collection("clusters").get().await()
        if (clustersSnap.isEmpty) {
            val clusterId = "cluster_northern_cascades"
            saveCluster(
                ClusterInfo(
                    id = clusterId,
                    name = "Northern Cascades Cluster",
                    region = "Pacific Northwest",
                    milestone = "Milestone 2",
                    currentCycle = 104,
                    goalsSummary = "Strengthen vibrant neighborhood nuclei, empower 30 youth animators, and expand devotional gatherings to 45 homes.",
                    userId = uid
                )
            )

            // Seed Neighborhoods
            val n1 = Neighborhood(
                id = "neigh_riverside",
                clusterId = clusterId,
                name = "Riverside Neighborhood",
                focusSector = "Sector East",
                facilitatorName = "Layli & Tahir",
                facilitatorContact = "(555) 349-2180",
                activeActivitiesCount = 4,
                notes = "Growing focus neighborhood with active youth and regular bi-weekly devotionals.",
                userId = uid
            )
            val n2 = Neighborhood(
                id = "neigh_evergreen",
                clusterId = clusterId,
                name = "Evergreen Heights",
                focusSector = "Sector North",
                facilitatorName = "Farhad K.",
                facilitatorContact = "(555) 782-9912",
                activeActivitiesCount = 3,
                notes = "Strong children's class program and cooperative community garden service project.",
                userId = uid
            )
            val n3 = Neighborhood(
                id = "neigh_downtown",
                clusterId = clusterId,
                name = "Downtown Cultural Quarter",
                focusSector = "Central Hub",
                facilitatorName = "Naveed & Mona",
                facilitatorContact = "(555) 412-6750",
                activeActivitiesCount = 5,
                notes = "High transit accessibility; central venue for inter-neighborhood study intensives.",
                userId = uid
            )
            saveNeighborhood(n1)
            saveNeighborhood(n2)
            saveNeighborhood(n3)

            // Seed Activities
            val now = System.currentTimeMillis()
            saveCoreActivity(
                CoreActivity(
                    id = "act_ruhi_book1",
                    clusterId = clusterId,
                    neighborhoodId = n1.id,
                    neighborhoodName = n1.name,
                    title = "Ruhi Book 1: Reflections on the Life of the Spirit",
                    type = "Study Circle",
                    animatorOrTutor = "Mona Alavi",
                    meetingDay = "Thursdays 7:00 PM",
                    location = "142 Maple St, Community Room",
                    participantsCount = 8,
                    bookOrTopic = "Ruhi Book 1 - Life & Prayer Units",
                    status = "Active",
                    userId = uid
                )
            )
            saveCoreActivity(
                CoreActivity(
                    id = "act_jy_group",
                    clusterId = clusterId,
                    neighborhoodId = n1.id,
                    neighborhoodName = n1.name,
                    title = "Junior Youth Group: Radiant Stars",
                    type = "Junior Youth Group",
                    animatorOrTutor = "Darius Vance",
                    meetingDay = "Saturdays 10:30 AM",
                    location = "Riverside Park Pavilion",
                    participantsCount = 11,
                    bookOrTopic = "Breezes of Confirmation (JYSEP)",
                    status = "Active",
                    userId = uid
                )
            )
            saveCoreActivity(
                CoreActivity(
                    id = "act_children_class",
                    clusterId = clusterId,
                    neighborhoodId = n2.id,
                    neighborhoodName = n2.name,
                    title = "Children's Spiritual Education (Grade 2)",
                    type = "Children's Class",
                    animatorOrTutor = "Soraya Eghbal",
                    meetingDay = "Sundays 10:00 AM",
                    location = "Evergreen Center",
                    participantsCount = 14,
                    bookOrTopic = "Ruhi Grade 2: Virtues & Generosity",
                    status = "Active",
                    userId = uid
                )
            )
            saveCoreActivity(
                CoreActivity(
                    id = "act_devotional_1",
                    clusterId = clusterId,
                    neighborhoodId = n3.id,
                    neighborhoodName = n3.name,
                    title = "Heart & Soul Interfaith Devotional Gathering",
                    type = "Devotional Gathering",
                    animatorOrTutor = "Kaveh & Sarah",
                    meetingDay = "Friday Evenings 7:30 PM",
                    location = "Downtown Bahá'í Center & Virtual",
                    participantsCount = 22,
                    bookOrTopic = "Prayers for Unity and Healing",
                    status = "Active",
                    userId = uid
                )
            )

            // Seed Events (Feasts, Reflection Meetings, Holy Days)
            saveEvent(
                EventSchedule(
                    id = "event_feast_mashiyyat",
                    clusterId = clusterId,
                    neighborhoodName = n1.name,
                    title = "Nineteen Day Feast of Mashíyyat (Will)",
                    category = "Nineteen Day Feast",
                    dateTimeMillis = now + (2L * 86400000L),
                    location = "Riverside Fellowship Hall, 204 Oak Ave",
                    host = "Rahmani Family & Local Spiritual Assembly",
                    description = "Devotional reading, consultative agenda on community expansion, and fellowship hospitality.",
                    encryptedCoordinationNotes = CryptoManager.encrypt("Host committee code: 4921. Security team lead: Brother Sam. Audio equipment will be set up at 6:30 PM."),
                    attendeesCount = 38,
                    userId = uid
                )
            )
            saveEvent(
                EventSchedule(
                    id = "event_reflection_cycle104",
                    clusterId = clusterId,
                    neighborhoodName = n3.name,
                    title = "Cluster Reflection Meeting - Cycle 104",
                    category = "Cluster Reflection Meeting",
                    dateTimeMillis = now + (9L * 86400000L),
                    location = "Civic Auditorium & Zoom Hybrid",
                    host = "Area Teaching Committee (ATC)",
                    description = "Quarterly gathering of all believers to consult on progress of the Nine Year Plan, review statistics across neighborhoods, and set milestones for the next expansion phase.",
                    encryptedCoordinationNotes = CryptoManager.encrypt("Confidential statistical charts from regional institute council to be projected. Breakout room assignments in private binder."),
                    attendeesCount = 85,
                    userId = uid
                )
            )
            saveEvent(
                EventSchedule(
                    id = "event_holy_day_bab",
                    clusterId = clusterId,
                    neighborhoodName = n2.name,
                    title = "Twin Holy Birthdays: Birth of the Báb Celebration",
                    category = "Holy Day",
                    dateTimeMillis = now + (18L * 86400000L),
                    location = "Evergreen Botanical Amphitheater",
                    host = "Inter-Neighborhood Holy Day Committee",
                    description = "Joyous commemoration with youth choir performance, historical narrative presentations, and children's theatrical presentation.",
                    encryptedCoordinationNotes = CryptoManager.encrypt("Catering order confirmed with Green Lotus. Park permit #P-8820 in glove compartment."),
                    attendeesCount = 120,
                    userId = uid
                )
            )
        }
    }
}
