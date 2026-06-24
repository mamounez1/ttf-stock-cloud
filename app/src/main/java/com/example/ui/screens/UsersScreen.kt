package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Security
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
import com.example.db.User
import com.example.viewmodel.AppViewModel

@Composable
fun UsersScreen(viewModel: AppViewModel) {
    val users by viewModel.usersList.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }

    // Forms
    var usernameInput by remember { mutableStateOf("") }
    var pinInput by remember { mutableStateOf("") }
    var fullnameInput by remember { mutableStateOf("") }
    var roleInput by remember { mutableStateOf("user") } // "admin" | "user" | "consultateur"

    val isAdmin = currentUser?.role == "admin"

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
                text = "PANEL UTILISATEURS",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Text(
                text = "Administration exclusive des comptes opérateurs et accès.",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (!isAdmin) {
                // Warning if non-admin tries to peek (secured fallback)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Security, null, modifier = Modifier.size(32.dp), tint = Color(0xFFEF4444))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Accès Limité", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                        Text(
                            "Vous devez posséder le statut Administrateur pour modifier les profils utilisateurs.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(users) { usr ->
                        UserCard(
                            user = usr,
                            onEdit = {
                                selectedUser = usr
                                usernameInput = usr.username
                                fullnameInput = usr.fullName
                                roleInput = usr.role
                                pinInput = "" // empty means unchanged
                                showEditDialog = true
                            },
                            onDelete = {
                                selectedUser = usr
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }

        // Float button to trigger profile creation
        if (isAdmin) {
            FloatingActionButton(
                onClick = {
                    usernameInput = ""
                    pinInput = ""
                    fullnameInput = ""
                    roleInput = "user"
                    showAddDialog = true
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 80.dp, end = 16.dp)
                    .testTag("add_user_fab"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter un opérateur")
            }
        }

        // Add Operator profile
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Nouvel Utilisateur", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = fullnameInput,
                            onValueChange = { fullnameInput = it },
                            label = { Text("Nom complet") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = usernameInput,
                            onValueChange = { usernameInput = it },
                            label = { Text("Identifiant / Login") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = { pinInput = it },
                            label = { Text("Mot de passe / PIN") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Rôle :", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = roleInput == "admin", onClick = { roleInput = "admin" })
                                Text("Administrateur (Admin)", modifier = Modifier.clickable { roleInput = "admin" })
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = roleInput == "user", onClick = { roleInput = "user" })
                                Text("Opérateur (Lecture-Écriture)", modifier = Modifier.clickable { roleInput = "user" })
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = roleInput == "consultateur", onClick = { roleInput = "consultateur" })
                                Text("Consultateur (Lecture Seule)", modifier = Modifier.clickable { roleInput = "consultateur" })
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.addUser(
                                username = usernameInput.trim().lowercase(),
                                pin = pinInput.trim(),
                                fullName = fullnameInput.trim(),
                                role = roleInput
                            )
                            showAddDialog = false
                        }
                    ) {
                        Text("Créer le compte")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Annuler")
                    }
                }
            )
        }

        // Edit Account profile dropdown
        if (showEditDialog && selectedUser != null) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Éditer l'opérateur", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = fullnameInput,
                            onValueChange = { fullnameInput = it },
                            label = { Text("Nom complet") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = { pinInput = it },
                            placeholder = { Text("Laisser vide pour ne pas changer") },
                            label = { Text("Nouveau mot de passe") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Rôle :", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = roleInput == "admin", onClick = { roleInput = "admin" })
                                Text("Administrateur", modifier = Modifier.clickable { roleInput = "admin" })
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = roleInput == "user", onClick = { roleInput = "user" })
                                Text("Opérateur", modifier = Modifier.clickable { roleInput = "user" })
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = roleInput == "consultateur", onClick = { roleInput = "consultateur" })
                                Text("Consultateur", modifier = Modifier.clickable { roleInput = "consultateur" })
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            selectedUser?.let { usr ->
                                viewModel.updateUser(
                                    id = usr.id,
                                    username = usr.username,
                                    pin = pinInput.trim(),
                                    fullName = fullnameInput.trim(),
                                    role = roleInput
                                )
                            }
                            showEditDialog = false
                        }
                    ) {
                        Text("Enregistrer les modifications")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("Annuler")
                    }
                }
            )
        }

        // Delete Dialog
        if (showDeleteDialog && selectedUser != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Supprimer l'opérateur ?", fontWeight = FontWeight.Bold) },
                text = { Text("Confirmez-vous la révocation et suppression de ${selectedUser?.fullName} ?") },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        onClick = {
                            selectedUser?.let { viewModel.deleteUser(it.id) }
                            showDeleteDialog = false
                        }
                    ) {
                        Text("Radier le compte", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Conserver")
                    }
                }
            )
        }
    }
}

@Composable
fun UserCard(
    user: User,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    // Badges color coding: Admin (Crimson/Red), User (Navy Blue), Consultateur (Forest Green)
    val badgeColor = when (user.role) {
        "admin" -> Color(0xFFDC2626) // Deep Red
        "user" -> Color(0xFF0B49FF) // Blue
        else -> Color(0xFF059669) // Forest Green
    }

    val badgeLabel = when (user.role) {
        "admin" -> "ADMINISTRATEUR"
        "user" -> "OPÉRATEUR"
        else -> "CONSULTATEUR"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(badgeColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AdminPanelSettings, null, modifier = Modifier.size(20.dp), tint = badgeColor)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("@" + user.username, fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = badgeLabel,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor,
                    modifier = Modifier
                        .background(badgeColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            Row {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit User", tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete User", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
