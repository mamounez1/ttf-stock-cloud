package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.db.Contact
import com.example.viewmodel.AppViewModel

@Composable
fun ContactsScreen(viewModel: AppViewModel) {
    val contacts by viewModel.contactsList.collectAsState()
    val filterType by viewModel.contactFilterType.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedContact by remember { mutableStateOf<Contact?>(null) }

    // Dialog Input fields
    var nameInput by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("fournisseur") } // "client" | "fournisseur" | "both"
    var phoneInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var addressInput by remember { mutableStateOf("") }

    val userRole = currentUser?.role ?: "consultateur"
    val canWrite = userRole == "admin" || userRole == "user"

    // Multi-criteria filter
    val filteredContacts = contacts.filter { contact ->
        when (filterType) {
            "Tous" -> true
            "Clients" -> contact.type == "client" || contact.type == "both"
            "Fournisseurs" -> contact.type == "fournisseur" || contact.type == "both"
            else -> true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "CARNET DE CONTACTS",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Text(
                text = "Gérez l'ensemble des Clients et Fournisseurs de l'entreprise.",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Horizontal filters mapping Lucide tags
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Tous", "Clients", "Fournisseurs").forEach { typeLabel ->
                    val isSelected = filterType == typeLabel
                    Button(
                        onClick = { viewModel.contactFilterType.value = typeLabel },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.25f),
                            contentColor = if (isSelected) Color.White else Color.DarkGray
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(typeLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Results List
            if (filteredContacts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucun contact correspondant.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredContacts) { contact ->
                        ContactCardItem(
                            contact = contact,
                            onEdit = {
                                selectedContact = contact
                                nameInput = contact.name
                                selectedType = contact.type
                                phoneInput = contact.phone
                                emailInput = contact.email
                                addressInput = contact.address
                                showEditDialog = true
                            },
                            onDelete = {
                                selectedContact = contact
                                showDeleteDialog = true
                            },
                            canWrite = canWrite
                        )
                    }
                }
            }
        }

        // FAB layout
        if (canWrite) {
            FloatingActionButton(
                onClick = {
                    nameInput = ""
                    selectedType = "fournisseur"
                    phoneInput = ""
                    emailInput = ""
                    addressInput = ""
                    showAddDialog = true
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 80.dp, end = 16.dp)
                    .testTag("add_contact_fab"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nouveau contact")
            }
        }

        // CREATE NEW DIALOG
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Nouveau Contact", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Nom de l'entreprise / Particulier *") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Type :", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = selectedType == "fournisseur", onClick = { selectedType = "fournisseur" })
                                Text("Fournisseur", modifier = Modifier.clickable { selectedType = "fournisseur" })
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = selectedType == "client", onClick = { selectedType = "client" })
                                Text("Client", modifier = Modifier.clickable { selectedType = "client" })
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = selectedType == "both", onClick = { selectedType = "both" })
                                Text("Les deux", modifier = Modifier.clickable { selectedType = "both" })
                            }
                        }

                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = { Text("Téléphone") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Courriel") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = addressInput,
                            onValueChange = { addressInput = it },
                            label = { Text("Adresse postale complète") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.addContact(
                                name = nameInput.trim(),
                                type = selectedType,
                                address = addressInput.trim(),
                                phone = phoneInput.trim(),
                                email = emailInput.trim()
                            )
                            showAddDialog = false
                        },
                        enabled = nameInput.isNotBlank()
                    ) {
                        Text("Créer le contact")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Annuler")
                    }
                }
            )
        }

        // EDIT DIALOG
        if (showEditDialog && selectedContact != null) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Modifier le Contact", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Nom de l'entreprise *") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Type :", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = selectedType == "fournisseur", onClick = { selectedType = "fournisseur" })
                                Text("Fournisseur", modifier = Modifier.clickable { selectedType = "fournisseur" })
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = selectedType == "client", onClick = { selectedType = "client" })
                                Text("Client", modifier = Modifier.clickable { selectedType = "client" })
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = selectedType == "both", onClick = { selectedType = "both" })
                                Text("Les deux", modifier = Modifier.clickable { selectedType = "both" })
                            }
                        }

                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = { Text("Téléphone") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Courriel") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = addressInput,
                            onValueChange = { addressInput = it },
                            label = { Text("Adresse postale complète") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            selectedContact?.let { contact ->
                                viewModel.updateContact(
                                    id = contact.id,
                                    name = nameInput.trim(),
                                    type = selectedType,
                                    address = addressInput.trim(),
                                    phone = phoneInput.trim(),
                                    email = emailInput.trim()
                                )
                            }
                            showEditDialog = false
                        },
                        enabled = nameInput.isNotBlank()
                    ) {
                        Text("D'accord")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("Annuler")
                    }
                }
            )
        }

        // DELETE CONFIRMED DIALOG
        if (showDeleteDialog && selectedContact != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Supprimer le Contact ?", fontWeight = FontWeight.Bold) },
                text = { Text("Confirmez-vous la radiation de '${selectedContact?.name}' du carnet d'adresses ?") },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        onClick = {
                            selectedContact?.let { viewModel.deleteContact(it.id) }
                            showDeleteDialog = false
                        }
                    ) {
                        Text("Confirmer la suppression", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Annuler")
                    }
                }
            )
        }
    }
}

@Composable
fun ContactCardItem(
    contact: Contact,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    canWrite: Boolean
) {
    val typeBadgeColor = when (contact.type) {
        "client" -> Color(0xFF0284C7)
        "fournisseur" -> Color(0xFF0F766E)
        else -> Color(0xFF6D28D9)
    }

    val typeBadgeText = when (contact.type) {
        "client" -> "CLIENT REVISEU"
        "fournisseur" -> "FOURNISSEUR"
        else -> "CLIENT & FOURN."
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(typeBadgeColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Business, null, modifier = Modifier.size(18.dp), tint = typeBadgeColor)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = typeBadgeText,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = typeBadgeColor,
                            modifier = Modifier
                                .background(typeBadgeColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                if (canWrite) {
                    Row {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Contact", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Contact", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body info (Details fields)
            if (contact.phone.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                    Icon(Icons.Default.Phone, null, modifier = Modifier.size(14.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(contact.phone, fontSize = 12.sp, color = Color.DarkGray)
                }
            }

            if (contact.email.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                    Icon(Icons.Default.Email, null, modifier = Modifier.size(14.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(contact.email, fontSize = 12.sp, color = Color.DarkGray)
                }
            }

            if (contact.address.isNotBlank()) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(14.dp).padding(top = 2.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(contact.address, fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}
