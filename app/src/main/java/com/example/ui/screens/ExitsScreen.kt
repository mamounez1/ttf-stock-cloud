package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.db.Article
import com.example.db.Contact
import com.example.viewmodel.AppViewModel
import com.example.viewmodel.ReceiptDocument
import com.example.viewmodel.ReceiptItem

@Composable
fun ExitsScreen(viewModel: AppViewModel) {
    val history by viewModel.stockExitsList.collectAsState()
    val articles by viewModel.articlesList.collectAsState()
    val contacts by viewModel.contactsList.collectAsState()
    val slots by viewModel.slotAssignmentsList.collectAsState()

    val isSessionActive by viewModel.isExitSessionActive.collectAsState()
    val clientName by viewModel.exitClientName.collectAsState()
    val matricule by viewModel.exitMatricule.collectAsState()
    val exitDate by viewModel.exitDate.collectAsState()
    val addedPallets by viewModel.exitPallets.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showAddPalletDialog by remember { mutableStateOf(false) }
    var showStartDialog by remember { mutableStateOf(false) }

    var inputClientName by remember { mutableStateOf("") }
    var inputMatricule by remember { mutableStateOf("") }
    var inputDate by remember { mutableStateOf("") }

    val clients = contacts.filter { it.type == "client" || it.type == "both" }
    val userRole = currentUser?.role ?: "consultateur"
    val canWrite = userRole == "admin" || userRole == "user"

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SORTIES DE STOCK",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (isSessionActive) "Session d'expédition / enlèvement en cours" else "Historique et retraits d'articles",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                if (!isSessionActive && canWrite) {
                    Button(
                        onClick = {
                            inputClientName = ""
                            inputMatricule = ""
                            inputDate = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.FRANCE).format(java.util.Date())
                            showStartDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.testTag("start_exit_btn")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("START", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isSessionActive) {
                // ACTIVE SESSION WRAPPER
                Column(modifier = Modifier.weight(1f)) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Expédition :", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                                Text("Client: $clientName", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Chauffeur/Matricule: $matricule", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Date de sortie: $exitDate", fontSize = 12.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Palettes :", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                                Text("${addedPallets.size} de sortie", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Palettes à charger :", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Button(
                            onClick = { showAddPalletDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.testTag("add_pallet_exit_session_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ajouter Palette", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (addedPallets.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Aucune palette ajoutée à la sortie.\nCliquez sur 'Ajouter Palette' pour commencer le retrait.", color = Color.Gray, textAlign = TextAlign.Center)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(addedPallets) { pallet ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(24.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(pallet.id, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            Text(pallet.articleName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                            Text("Quantité: ${pallet.quantity} • Qualité: ${pallet.quality}", fontSize = 12.sp, color = Color.Gray)
                                            Text("Fournisseur d'origine: ${pallet.supplierName}", fontSize = 11.sp, color = Color.Gray)
                                            if (pallet.sourceLocation.isNotBlank()) {
                                                Text("Retrait de l'emplacement: ${pallet.sourceLocation}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFEF4444))
                                            }
                                        }
                                        IconButton(onClick = { viewModel.removePalletFromExitSession(pallet.id) }) {
                                            Icon(Icons.Default.Close, contentDescription = "Retirer", tint = Color(0xFFEF4444))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.cancelExitSession() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray.copy(alpha = 0.25f), contentColor = Color.DarkGray),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text("Annuler", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { viewModel.commitExitSession() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            enabled = addedPallets.isNotEmpty()
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Terminer & Imprimer", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            } else {
                // HISTORICAL EXITS LIST
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("HISTORIQUE DES EXPÉDITIONS / SORTIES", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (history.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Aucune sortie enregistrée.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(history) { exit ->
                                val matchingArticle = articles.find { it.id == exit.articleId }
                                val dateStr = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.FRANCE).format(java.util.Date(exit.createdAt))

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(24.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(exit.palletNumber, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                Text(dateStr, fontSize = 11.sp, color = Color.Gray)
                                            }
                                            Text(matchingArticle?.name ?: "Article inconnu", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                            Text("Qté retirée: ${exit.quantity} • Qualité: ${exit.quality}", fontSize = 12.sp, color = Color.Gray)
                                            Text("Client: ${exit.clientName} • Matricule: ${exit.matricule}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                            if (exit.sourceLocation.isNotBlank()) {
                                                Text("Emplacement retiré: ${exit.sourceLocation}", fontSize = 11.sp, color = Color(0xFFEF4444))
                                            }
                                        }

                                        // Re-print
                                        IconButton(onClick = {
                                            val doc = ReceiptDocument(
                                                title = "BON DE SORTIE INDIVIDUEL",
                                                documentNumber = "SRT-${exit.id.toString().padStart(4, '0')}",
                                                date = dateStr,
                                                extraDetails = mapOf(
                                                    "Client" to exit.clientName,
                                                    "Véhicule / Matricule" to exit.matricule,
                                                    "Fournisseur" to exit.supplierName
                                                ),
                                                items = listOf(
                                                    ReceiptItem(
                                                        palletNo = exit.palletNumber,
                                                        articleInfo = "[${matchingArticle?.reference ?: "N/A"}] ${matchingArticle?.name ?: "N/A"}",
                                                        supplier = exit.supplierName,
                                                        qty = exit.quantity,
                                                        quality = exit.quality,
                                                        location = exit.sourceLocation
                                                    )
                                                )
                                            )
                                            viewModel.showReceipt(doc)
                                        }) {
                                            Icon(Icons.Default.Print, contentDescription = "Ré-imprimer")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // START DIALOG
        if (showStartDialog) {
            var selectedClientObj by remember { mutableStateOf<Contact?>(null) }
            var clientMenuExpanded by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showStartDialog = false },
                title = { Text("Nouvelle session d'expédition", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Veuillez désigner le client final ainsi que le matricule du véhicule de chargement.", fontSize = 12.sp, color = Color.Gray)

                        // Client Select dropdown
                        Box {
                            OutlinedTextField(
                                value = selectedClientObj?.name ?: "Sélectionner un Client",
                                onValueChange = {},
                                label = { Text("Client destinataire *") },
                                readOnly = true,
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { clientMenuExpanded = true }
                            )
                            DropdownMenu(expanded = clientMenuExpanded, onDismissRequest = { clientMenuExpanded = false }) {
                                clients.forEach { cl ->
                                    DropdownMenuItem(text = { Text(cl.name) }, onClick = {
                                        selectedClientObj = cl
                                        inputClientName = cl.name
                                        clientMenuExpanded = false
                                    })
                                }
                            }
                        }

                        OutlinedTextField(
                            value = inputMatricule,
                            onValueChange = { inputMatricule = it },
                            label = { Text("Matricule du camion (ex: 1234-A-16)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.startExitSession(inputClientName.trim(), inputMatricule.trim(), inputDate.trim())
                            showStartDialog = false
                        },
                        enabled = inputClientName.isNotBlank() && inputMatricule.isNotBlank()
                    ) {
                        Text("Démarrer")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showStartDialog = false }) {
                        Text("Annuler")
                    }
                }
            )
        }

        // ADD PALLET DIALOG
        if (showAddPalletDialog) {
            var selectedArticleObj by remember { mutableStateOf<Article?>(null) }
            var sourceLocationCode by remember { mutableStateOf("") }
            var palletQty by remember { mutableStateOf("100") }
            var palletQuality by remember { mutableStateOf("A") }
            var origSupplier by remember { mutableStateOf("") }

            var articleMenuExpanded by remember { mutableStateOf(false) }
            var locationMenuExpanded by remember { mutableStateOf(false) }

            // Dynamic filter: list only occupied slots to easily withdraw from!
            val occupiedSlots = slots.filter { it.articleId != null }

            AlertDialog(
                onDismissRequest = { showAddPalletDialog = false },
                title = { Text("Retirer une Palette", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Recherchez un produit pour charger le bon de sortie, l'emplacement en stock sera détecté automatiquement.", fontSize = 12.sp, color = Color.Gray)

                        // 1. SELECT PRODUIT FIRST
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { articleMenuExpanded = true }
                        ) {
                            OutlinedTextField(
                                value = selectedArticleObj?.name ?: "Sélectionner un produit *",
                                onValueChange = {},
                                label = { Text("Produit *") },
                                readOnly = true,
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            DropdownMenu(
                                expanded = articleMenuExpanded,
                                onDismissRequest = { articleMenuExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                // Find all unique articles that currently occupy some slot
                                val stockedArticleIds = occupiedSlots.mapNotNull { it.articleId }.distinct()
                                val stockedArticles = articles.filter { it.id in stockedArticleIds }

                                if (stockedArticles.isEmpty()) {
                                    DropdownMenuItem(text = { Text("Aucun produit en stock actuellement !") }, onClick = {})
                                } else {
                                    stockedArticles.forEach { art ->
                                        DropdownMenuItem(
                                            text = { Text("[${art.reference}] ${art.name} (${art.quantity} p.)") },
                                            onClick = {
                                                selectedArticleObj = art
                                                // Pre-select the first slot occupied by this article
                                                val matchingSlots = occupiedSlots.filter { it.articleId == art.id }
                                                if (matchingSlots.isNotEmpty()) {
                                                    sourceLocationCode = matchingSlots.first().locationCode
                                                    origSupplier = matchingSlots.first().supplierName
                                                } else {
                                                    sourceLocationCode = ""
                                                    origSupplier = ""
                                                }
                                                articleMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // 2. SELECT EMPLACEMENT SOURCE FROM PRODUCT
                        val availableSlotsForProduct = if (selectedArticleObj != null) {
                            occupiedSlots.filter { it.articleId == selectedArticleObj?.id }
                        } else {
                            emptyList()
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    if (selectedArticleObj != null && availableSlotsForProduct.isNotEmpty()) {
                                        locationMenuExpanded = true 
                                    }
                                }
                        ) {
                            val displayText = if (selectedArticleObj == null) {
                                "Sélectionnez d'abord un produit"
                            } else if (sourceLocationCode.isBlank()) {
                                "Aucun emplacement trouvé"
                            } else {
                                "$sourceLocationCode (Fourn: $origSupplier)"
                            }

                            OutlinedTextField(
                                value = displayText,
                                onValueChange = {},
                                label = { Text("Emplacement source *") },
                                readOnly = true,
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = if (selectedArticleObj == null) Color.Gray else MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (selectedArticleObj != null && availableSlotsForProduct.isNotEmpty()) {
                                DropdownMenu(
                                    expanded = locationMenuExpanded,
                                    onDismissRequest = { locationMenuExpanded = false },
                                    modifier = Modifier.fillMaxWidth(0.9f)
                                ) {
                                    availableSlotsForProduct.forEach { slot ->
                                        DropdownMenuItem(
                                            text = { Text("${slot.locationCode} (Fournisseur: ${slot.supplierName})") },
                                            onClick = {
                                                sourceLocationCode = slot.locationCode
                                                origSupplier = slot.supplierName
                                                locationMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Display product info locked
                        selectedArticleObj?.let { art ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.LocalShipping, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(art.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Réf: ${art.reference} • Dispo total: ${art.quantity} p.", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }

                        // Qty + quality
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = palletQty,
                                onValueChange = { palletQty = it },
                                label = { Text("Quantité à retirer") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = palletQuality,
                                onValueChange = { palletQuality = it },
                                label = { Text("Qualité") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val art = selectedArticleObj
                            if (art == null || sourceLocationCode.isBlank()) {
                                return@Button
                            }
                            viewModel.addPalletToExitSession(
                                articleId = art.id,
                                articleRef = art.reference,
                                articleName = art.name,
                                supplierName = origSupplier,
                                quantity = palletQty.toIntOrNull() ?: 0,
                                quality = palletQuality.trim(),
                                sourceLocation = sourceLocationCode
                            )
                            showAddPalletDialog = false
                        },
                        enabled = selectedArticleObj != null && sourceLocationCode.isNotBlank()
                    ) {
                        Text("Confirmer le retrait")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddPalletDialog = false }) {
                        Text("Annuler")
                    }
                }
            )
        }
    }
}
