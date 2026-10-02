package com.example.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CoreActivity
import com.example.data.model.EventSchedule
import com.example.data.model.Neighborhood
import com.example.data.model.UserProfile
import com.example.data.security.CryptoManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEventDialog(
    initialEvent: EventSchedule? = null,
    neighborhoods: List<Neighborhood>,
    onDismiss: () -> Unit,
    onSave: (EventSchedule, Boolean) -> Unit
) {
    var title by remember { mutableStateOf(initialEvent?.title ?: "") }
    var category by remember { mutableStateOf(initialEvent?.category ?: "Nineteen Day Feast") }
    var neighborhoodName by remember {
        mutableStateOf(initialEvent?.neighborhoodName ?: (neighborhoods.firstOrNull()?.name ?: "General Cluster"))
    }
    var location by remember { mutableStateOf(initialEvent?.location ?: "") }
    var host by remember { mutableStateOf(initialEvent?.host ?: "") }
    var description by remember { mutableStateOf(initialEvent?.description ?: "") }
    var coordinationNotes by remember {
        mutableStateOf(
            if (initialEvent != null && CryptoManager.isEncrypted(initialEvent.encryptedCoordinationNotes)) {
                CryptoManager.decrypt(initialEvent.encryptedCoordinationNotes)
            } else {
                initialEvent?.encryptedCoordinationNotes ?: ""
            }
        )
    }
    var encryptNotes by remember { mutableStateOf(true) }
    var attendeesCountStr by remember { mutableStateOf(initialEvent?.attendeesCount?.toString() ?: "20") }

    val categories = listOf(
        "Nineteen Day Feast",
        "Holy Day",
        "Cluster Reflection Meeting",
        "Core Activity",
        "Youth Gathering",
        "Special Event"
    )

    var categoryExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialEvent == null) "Schedule Cluster Event" else "Edit Event Schedule",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title *") },
                    placeholder = { Text("e.g. Nineteen Day Feast of 'Ilm") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("event_title_input"),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = neighborhoodName,
                    onValueChange = { neighborhoodName = it },
                    label = { Text("Host Neighborhood / Sector") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Venue / Location Address") },
                    placeholder = { Text("e.g. 104 Main St or Zoom link") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = host,
                    onValueChange = { host = it },
                    label = { Text("Host / Organizing Team") },
                    placeholder = { Text("e.g. LSA / Family name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = attendeesCountStr,
                    onValueChange = { attendeesCountStr = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Expected Attendees (RSVP)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Public Program Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                // Encrypted Notes Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Lock",
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Confidential Coordination Notes",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Switch(
                                checked = encryptNotes,
                                onCheckedChange = { encryptNotes = it },
                                modifier = Modifier.testTag("encrypt_switch")
                            )
                        }
                        Text(
                            text = if (encryptNotes) "AES-256-GCM client encryption enabled" else "Plaintext storage",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (encryptNotes) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = coordinationNotes,
                            onValueChange = { coordinationNotes = it },
                            placeholder = { Text("Confidential logistics, door entry codes, private contact numbers") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("encrypted_notes_input"),
                            minLines = 2
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val event = EventSchedule(
                            id = initialEvent?.id ?: "",
                            clusterId = initialEvent?.clusterId ?: "cluster_northern_cascades",
                            neighborhoodName = neighborhoodName,
                            title = title.trim(),
                            category = category,
                            dateTimeMillis = initialEvent?.dateTimeMillis ?: (System.currentTimeMillis() + 86400000L),
                            location = location.trim(),
                            host = host.trim(),
                            description = description.trim(),
                            encryptedCoordinationNotes = coordinationNotes.trim(),
                            attendeesCount = attendeesCountStr.toIntOrNull() ?: 0
                        )
                        onSave(event, encryptNotes)
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("save_event_button")
            ) {
                Text("Save Schedule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditActivityDialog(
    initialActivity: CoreActivity? = null,
    neighborhoods: List<Neighborhood>,
    onDismiss: () -> Unit,
    onSave: (CoreActivity) -> Unit
) {
    var title by remember { mutableStateOf(initialActivity?.title ?: "") }
    var type by remember { mutableStateOf(initialActivity?.type ?: "Study Circle") }
    var neighborhoodName by remember {
        mutableStateOf(initialActivity?.neighborhoodName ?: (neighborhoods.firstOrNull()?.name ?: "Riverside"))
    }
    var animatorOrTutor by remember { mutableStateOf(initialActivity?.animatorOrTutor ?: "") }
    var meetingDay by remember { mutableStateOf(initialActivity?.meetingDay ?: "") }
    var location by remember { mutableStateOf(initialActivity?.location ?: "") }
    var bookOrTopic by remember { mutableStateOf(initialActivity?.bookOrTopic ?: "") }
    var participantsStr by remember { mutableStateOf(initialActivity?.participantsCount?.toString() ?: "8") }
    var status by remember { mutableStateOf(initialActivity?.status ?: "Active") }

    val activityTypes = listOf(
        "Study Circle",
        "Junior Youth Group",
        "Children's Class",
        "Devotional Gathering",
        "Community Service"
    )
    var typeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialActivity == null) "Add Core Activity" else "Edit Core Activity",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Activity Title / Group Name *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("activity_title_input"),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Activity Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        activityTypes.forEach { actType ->
                            DropdownMenuItem(
                                text = { Text(actType) },
                                onClick = {
                                    type = actType
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = neighborhoodName,
                    onValueChange = { neighborhoodName = it },
                    label = { Text("Neighborhood") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = animatorOrTutor,
                    onValueChange = { animatorOrTutor = it },
                    label = { Text("Tutor / Animator / Facilitator") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = bookOrTopic,
                    onValueChange = { bookOrTopic = it },
                    label = { Text("Ruhi Book / Study Topic") },
                    placeholder = { Text("e.g. Ruhi Book 1, Breezes of Confirmation") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = meetingDay,
                    onValueChange = { meetingDay = it },
                    label = { Text("Meeting Schedule") },
                    placeholder = { Text("e.g. Saturdays 10:00 AM") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Meeting Location") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = participantsStr,
                    onValueChange = { participantsStr = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Number of Participants") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val activity = CoreActivity(
                            id = initialActivity?.id ?: "",
                            clusterId = initialActivity?.clusterId ?: "cluster_northern_cascades",
                            neighborhoodName = neighborhoodName.trim(),
                            title = title.trim(),
                            type = type,
                            animatorOrTutor = animatorOrTutor.trim(),
                            meetingDay = meetingDay.trim(),
                            location = location.trim(),
                            bookOrTopic = bookOrTopic.trim(),
                            participantsCount = participantsStr.toIntOrNull() ?: 0,
                            status = status
                        )
                        onSave(activity)
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("save_activity_button")
            ) {
                Text("Save Activity")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddEditNeighborhoodDialog(
    initialNeighborhood: Neighborhood? = null,
    onDismiss: () -> Unit,
    onSave: (Neighborhood) -> Unit
) {
    var name by remember { mutableStateOf(initialNeighborhood?.name ?: "") }
    var focusSector by remember { mutableStateOf(initialNeighborhood?.focusSector ?: "Sector East") }
    var facilitatorName by remember { mutableStateOf(initialNeighborhood?.facilitatorName ?: "") }
    var facilitatorContact by remember { mutableStateOf(initialNeighborhood?.facilitatorContact ?: "") }
    var notes by remember { mutableStateOf(initialNeighborhood?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialNeighborhood == null) "Register Neighborhood Area" else "Edit Neighborhood",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Neighborhood Name *") },
                    placeholder = { Text("e.g. Riverside District") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("neighborhood_name_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = focusSector,
                    onValueChange = { focusSector = it },
                    label = { Text("Sector / Classification") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = facilitatorName,
                    onValueChange = { facilitatorName = it },
                    label = { Text("Neighborhood Coordinator / Team") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = facilitatorContact,
                    onValueChange = { facilitatorContact = it },
                    label = { Text("Contact Phone / Info") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Neighborhood Context & Growth Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val n = Neighborhood(
                            id = initialNeighborhood?.id ?: "",
                            clusterId = initialNeighborhood?.clusterId ?: "cluster_northern_cascades",
                            name = name.trim(),
                            focusSector = focusSector.trim(),
                            facilitatorName = facilitatorName.trim(),
                            facilitatorContact = facilitatorContact.trim(),
                            notes = notes.trim()
                        )
                        onSave(n)
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("save_neighborhood_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditUserProfileDialog(
    initialProfile: UserProfile?,
    onDismiss: () -> Unit,
    onSave: (UserProfile, Boolean) -> Unit
) {
    var displayName by remember { mutableStateOf(initialProfile?.displayName ?: "") }
    var clusterName by remember { mutableStateOf(initialProfile?.clusterName ?: "Northern Cascades Cluster") }
    var neighborhood by remember { mutableStateOf(initialProfile?.neighborhood ?: "Riverside") }
    var role by remember { mutableStateOf(initialProfile?.role ?: "Neighborhood Facilitator") }
    var phone by remember { mutableStateOf(initialProfile?.phone ?: "") }
    var encryptedNotes by remember {
        mutableStateOf(
            if (initialProfile != null && CryptoManager.isEncrypted(initialProfile.encryptedNotes)) {
                CryptoManager.decrypt(initialProfile.encryptedNotes)
            } else {
                initialProfile?.encryptedNotes ?: ""
            }
        )
    }
    var encryptNotes by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cluster Member Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Full Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = clusterName,
                    onValueChange = { clusterName = it },
                    label = { Text("Bahá'í Cluster") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = neighborhood,
                    onValueChange = { neighborhood = it },
                    label = { Text("Local Neighborhood Focus") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Service Role (e.g. Coordinator, Tutor)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Contact Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = encryptedNotes,
                    onValueChange = { encryptedNotes = it },
                    label = { Text("Private Coordination Notes (Encrypted)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (displayName.isNotBlank()) {
                        val profile = (initialProfile ?: UserProfile()).copy(
                            displayName = displayName.trim(),
                            clusterName = clusterName.trim(),
                            neighborhood = neighborhood.trim(),
                            role = role.trim(),
                            phone = phone.trim(),
                            encryptedNotes = encryptedNotes.trim()
                        )
                        onSave(profile, encryptNotes)
                    }
                },
                enabled = displayName.isNotBlank()
            ) {
                Text("Save Profile")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
