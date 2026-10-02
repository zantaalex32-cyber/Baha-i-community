package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.ClusterInfo
import com.example.data.model.CoreActivity
import com.example.data.model.EventSchedule
import com.example.data.model.Neighborhood
import com.example.data.security.CryptoManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClusterRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun testAuthenticatedUserCanSaveAndRetrieveCluster() = runBlocking {
        val uid = signInTestUser("coordinator@cluster.org")
        val repo = ClusterRepository(firestore)

        val cluster = ClusterInfo(
            id = "test_cluster_1",
            name = "Pacific Cascade Cluster",
            milestone = "Milestone 2",
            currentCycle = 104,
            userId = uid
        )
        repo.saveCluster(cluster)

        val list = repo.observeClusters().first()
        assertTrue(list.any { it.id == "test_cluster_1" && it.name == "Pacific Cascade Cluster" })
    }

    @Test
    fun testEventCreationWithEncryptedNotesAndDecryption() = runBlocking {
        val uid = signInTestUser("facilitator@cluster.org")
        val repo = ClusterRepository(firestore)

        val plainSecret = "Keybox code: 9912. Confidential host contact: +1-555-0192"
        val cipherSecret = CryptoManager.encrypt(plainSecret)
        assertTrue(CryptoManager.isEncrypted(cipherSecret))

        val event = EventSchedule(
            id = "event_test_feast",
            title = "Nineteen Day Feast of Jalál",
            category = "Nineteen Day Feast",
            location = "Evergreen Hall",
            encryptedCoordinationNotes = cipherSecret,
            userId = uid
        )
        repo.saveEvent(event)

        val retrievedEvents = repo.observeEvents().first()
        val retrieved = retrievedEvents.find { it.id == "event_test_feast" }
        assertNotNull(retrieved)
        assertEquals(cipherSecret, retrieved!!.encryptedCoordinationNotes)

        val decrypted = CryptoManager.decrypt(retrieved.encryptedCoordinationNotes)
        assertEquals(plainSecret, decrypted)
    }

    @Test
    fun testCoreActivityLifecycle() = runBlocking {
        val uid = signInTestUser("animator@cluster.org")
        val repo = ClusterRepository(firestore)

        val activity = CoreActivity(
            id = "act_test_jy",
            title = "Youth Group Test",
            type = "Junior Youth Group",
            animatorOrTutor = "Navid",
            participantsCount = 10,
            userId = uid
        )
        repo.saveCoreActivity(activity)

        val activities = repo.observeActivities().first()
        assertTrue(activities.any { it.id == "act_test_jy" && it.participantsCount == 10 })

        repo.deleteCoreActivity("act_test_jy")
        val afterDelete = repo.observeActivities().first()
        assertTrue(afterDelete.none { it.id == "act_test_jy" })
    }
}
