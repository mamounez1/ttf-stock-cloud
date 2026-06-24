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
import com.example.warehouse.WarehouseConfig
import com.example.viewmodel.AppViewModel
import com.example.viewmodel.ReceiptDocument
import com.example.viewmodel.ReceiptItem

@Composable
fun EntriesScreen(viewModel: AppViewModel) {
    val history by viewModel.stockEntriesList.collectAsState()
    val articles by viewModel.articlesList.collectAsState()
    val contacts by viewModel.contactsList.collectAsState()

    val isSessionActive by viewModel.isEntrySessionActive.collectAsState()
    val prodDate by viewModel.entryProdDate.collectAsState()
    val embDate by viewModel.entryEmbDate.collectAsState()
    val addedPallets by viewModel.entryPallets.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showAddPalletDialog by remember { mutableStateOf(false) }
    var inputProdDate by remember { mutableStateOf("") }
    var inputEmbDate by remember { mutableStateOf("") }
    var showStartDialog by remember { mutableStateOf(false) }

    val suppliers = contacts.filter { it.type == "fournisseur" || it.type == "both" }
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
                        text = "ENTRÉES DE STOCK",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (isSessionActive) "Session de réception de palettes en cours" else "Historique et réceptions de marchandise",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                if (!isSessionActive && canWrite) {
                    Button(
                        onClick = {
                            val today = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.FRANCE).format(java.util.Date())
                            inputProdDate = today
                            inputEmbDate = today
                            showStartDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.testTag("start_entry_btn")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("START", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isSessionActive) {
                // Active Session Panel
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
                                Text("Dates du lot :", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                                Text("Production: $prodDate", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Emballage: $embDate", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Palettes :", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                                Text("${addedPallets.size} ajoutée(s)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Palettes de la session :", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Button(
                            onClick = { showAddPalletDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.testTag("add_pallet_session_btn")
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
                            Text("Aucune palette ajoutée.\nCliquez sur 'Ajouter Palette' pour commencer.", color = Color.Gray, textAlign = TextAlign.Center)
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
                                            Text("Qté: ${pallet.quantity} • Qualité: ${pallet.quality}", fontSize = 12.sp, color = Color.Gray)
                                            Text("Fourn.: ${pallet.supplierName}", fontSize = 11.sp, color = Color.Gray)
                                            if (pallet.destination.isNotBlank()) {
                                                Text("Emplacement: ${pallet.destination}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF10B981))
                                            }
                                        }
                                        IconButton(onClick = { viewModel.removePalletFromEntrySession(pallet.id) }) {
                                            Icon(Icons.Default.Close, contentDescription = "Retirer", tint = Color(0xFFEF4444))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Foot Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.cancelEntrySession() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray.copy(alpha = 0.25f), contentColor = Color.DarkGray),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text("Annuler", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { viewModel.commitEntrySession() },
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
                // Historical Mode
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("HISTORIQUE DES ENTRÉES", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (history.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Aucune entrée enregistrée pour le moment.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(history) { entry ->
                                val matchingArticle = articles.find { it.id == entry.articleId }
                                val dateStr = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.FRANCE).format(java.util.Date(entry.createdAt))

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
                                                Text(entry.palletNumber, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                Text(dateStr, fontSize = 11.sp, color = Color.Gray)
                                            }
                                            Text(matchingArticle?.name ?: "Article inconnu", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                            Text("Quantité: ${entry.quantity} • Qualité: ${entry.quality}", fontSize = 12.sp, color = Color.Gray)
                                            Text("Emplacement: ${entry.destination}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                            Text("Fournisseur: ${entry.supplierName}", fontSize = 11.sp, color = Color.Gray)
                                        }

                                        // Re-print button
                                        IconButton(onClick = {
                                            val doc = ReceiptDocument(
                                                title = "BON D'ENTRÉE INDIVIDUEL",
                                                documentNumber = "ENT-${entry.id.toString().padStart(4, '0')}",
                                                date = dateStr,
                                                extraDetails = mapOf(
                                                    "Fournisseur" to entry.supplierName,
                                                    "Date Prod." to entry.productionDate,
                                                    "Date Emb." to entry.emballageDate
                                                ),
                                                items = listOf(
                                                    ReceiptItem(
                                                        palletNo = entry.palletNumber,
                                                        articleInfo = "[${matchingArticle?.reference ?: "N/A"}] ${matchingArticle?.name ?: "N/A"}",
                                                        supplier = entry.supplierName,
                                                        qty = entry.quantity,
                                                        quality = entry.quality,
                                                        location = entry.destination
                                                    )
                                                )
                                            )
                                            viewModel.showReceipt(doc)
                                        }) {
                                            Icon(Icons.Default.Print, contentDescription = "Afficher le bon")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // START SESSION DIALOG
        if (showStartDialog) {
            AlertDialog(
                onDismissRequest = { showStartDialog = false },
                title = { Text("Nouvelle session d'entrée", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Veuillez saisir les dates de récolte, production et emballage pour ce lot de palettes.", fontSize = 12.sp, color = Color.Gray)
                        OutlinedTextField(
                            value = inputProdDate,
                            onValueChange = { inputProdDate = it },
                            label = { Text("Date de Production (JJ/MM/AAAA)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = inputEmbDate,
                            onValueChange = { inputEmbDate = it },
                            label = { Text("Date d'Emballage (JJ/MM/AAAA)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.startEntrySession(inputProdDate.trim(), inputEmbDate.trim())
                            showStartDialog = false
                        }
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
            var selectedSupplierObj by remember { mutableStateOf<Contact?>(null) }
            var palletQty by remember { mutableStateOf("100") }
            var palletQuality by remember { mutableStateOf("A") }

            // Cascading warehouse location selection states
            var selChamber by remember { mutableStateOf(1) }
            var selRack by remember { mutableStateOf(1) }
            var selLevel by remember { mutableStateOf(1) }
            var selPlace by remember { mutableStateOf(1) }

            // Menus expanded states
            var articleMenuExpanded by remember { mutableStateOf(false) }
            var supplierMenuExpanded by remember { mutableStateOf(false) }
            var chamberMenuExpanded by remember { mutableStateOf(false) }
            var rackMenuExpanded by remember { mutableStateOf(false) }
            var levelMenuExpanded by remember { mutableStateOf(false) }
            var placeMenuExpanded by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showAddPalletDialog = false },
                title = { Text("Ajouter une Palette", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 1. Article Selection dropdown
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { articleMenuExpanded = true }
                        ) {
                            OutlinedTextField(
                                value = selectedArticleObj?.name ?: "Sélectionner un produit",
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
                                articles.forEach { art ->
                                    DropdownMenuItem(
                                        text = { Text("[${art.reference}] ${art.name}") },
                                        onClick = {
                                            selectedArticleObj = art
                                            articleMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // 2. Supplier Selection dropdown
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { supplierMenuExpanded = true }
                        ) {
                            OutlinedTextField(
                                value = selectedSupplierObj?.name ?: "Sélectionner un fournisseur",
                                onValueChange = {},
                                label = { Text("Fournisseur *") },
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
                                expanded = supplierMenuExpanded,
                                onDismissRequest = { supplierMenuExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                suppliers.forEach { sup ->
                                    DropdownMenuItem(
                                        text = { Text(sup.name) },
                                        onClick = {
                                            selectedSupplierObj = sup
                                            supplierMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // 3. Qty & Quality in Row
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = palletQty,
                                onValueChange = { palletQty = it },
                                label = { Text("Quantité") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = palletQuality,
                                onValueChange = { palletQuality = it },
                                label = { Text("Qualité") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // 4. Warehouse Cascading Selectors
                        Text("Emplacement de stockage :", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Ch
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = "CH$selChamber",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Ch.") },
                                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(14.dp)) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable { chamberMenuExpanded = true }
                                )
                                DropdownMenu(expanded = chamberMenuExpanded, onDismissRequest = { chamberMenuExpanded = false }) {
                                    listOf(1, 2, 3).forEach { chNum ->
                                        DropdownMenuItem(text = { Text("Chambre $chNum") }, onClick = {
                                            selChamber = chNum
                                            selRack = 1
                                            selPlace = 1
                                            chamberMenuExpanded = false
                                        })
                                    }
                                }
                            }

                            // Rack
                            Box(modifier = Modifier.weight(1.2f)) {
                                val maxRack = WarehouseConfig.getMaxRack(selChamber)
                                OutlinedTextField(
                                    value = "R" + selRack.toString().padStart(2, '0'),
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Ray.") },
                                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(14.dp)) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable { rackMenuExpanded = true }
                                )
                                DropdownMenu(expanded = rackMenuExpanded, onDismissRequest = { rackMenuExpanded = false }) {
                                    (1..maxRack).forEach { rId ->
                                        val label = "R" + rId.toString().padStart(2, '0')
                                        DropdownMenuItem(text = { Text(label) }, onClick = {
                                            selRack = rId
                                            selPlace = 1
                                            rackMenuExpanded = false
                                        })
                                    }
                                }
                            }

                            // Level
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = "E$selLevel",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Etg.") },
                                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(14.dp)) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable { levelMenuExpanded = true }
                                )
                                DropdownMenu(expanded = levelMenuExpanded, onDismissRequest = { levelMenuExpanded = false }) {
                                    (1..4).forEach { eId ->
                                        DropdownMenuItem(text = { Text("Étage $eId") }, onClick = {
                                            selLevel = eId
                                            levelMenuExpanded = false
                                        })
                                    }
                                }
                            }

                            // Place
                            Box(modifier = Modifier.weight(1.2f)) {
                                val maxPlace = WarehouseConfig.getMaxPlace(selChamber, selRack) ?: 5
                                OutlinedTextField(
                                    value = "P" + selPlace.toString().padStart(2, '0'),
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Plc.") },
                                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(14.dp)) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable { placeMenuExpanded = true }
                                )
                                DropdownMenu(expanded = placeMenuExpanded, onDismissRequest = { placeMenuExpanded = false }) {
                                    (1..maxPlace).forEach { pId ->
                                        val label = "P" + pId.toString().padStart(2, '0')
                                        DropdownMenuItem(text = { Text(label) }, onClick = {
                                            selPlace = pId
                                            placeMenuExpanded = false
                                        })
                                    }
                                }
                            }
                        }

                        val parsedCode = WarehouseConfig.formatLocationCode(selChamber, selRack, selLevel, selPlace)
                        Text(
                            text = "Code généré: $parsedCode",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val art = selectedArticleObj
                            val sup = selectedSupplierObj
                            if (art == null || sup == null) {
                                return@Button
                            }
                            val destinationCode = WarehouseConfig.formatLocationCode(selChamber, selRack, selLevel, selPlace)
                            viewModel.addPalletToEntrySession(
                                articleId = art.id,
                                articleRef = art.reference,
                                articleName = art.name,
                                supplierName = sup.name,
                                quantity = palletQty.toIntOrNull() ?: 0,
                                quality = palletQuality.trim(),
                                destination = destinationCode
                            )
                            showAddPalletDialog = false
                        },
                        enabled = selectedArticleObj != null && selectedSupplierObj != null
                    ) {
                        Text("Ajouter")
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
