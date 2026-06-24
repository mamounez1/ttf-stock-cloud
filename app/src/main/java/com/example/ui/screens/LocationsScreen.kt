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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.db.Article
import com.example.db.SlotAssignment
import com.example.warehouse.WarehouseConfig
import com.example.viewmodel.AppViewModel

@Composable
fun LocationsScreen(viewModel: AppViewModel) {
    val slots by viewModel.slotAssignmentsList.collectAsState()
    val articles by viewModel.articlesList.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Interactive selections
    var selectedChamber by remember { mutableStateOf(1) }
    var selectedRack by remember { mutableStateOf(1) }
    var selectedLevel by remember { mutableStateOf(1) }

    // Dialog trigger state
    var showSlotActionDialog by remember { mutableStateOf(false) }
    var clickedLocationCode by remember { mutableStateOf("") }
    var clickedSlotInfo by remember { mutableStateOf<SlotAssignment?>(null) }

    // Dropdown expansion triggers
    var chamberExpanded by remember { mutableStateOf(false) }
    var rackExpanded by remember { mutableStateOf(false) }
    var levelExpanded by remember { mutableStateOf(false) }

    val userRole = currentUser?.role ?: "consultateur"
    val canWrite = userRole == "admin" || userRole == "user"

    val maxRack = WarehouseConfig.getMaxRack(selectedChamber)
    val maxPlace = WarehouseConfig.getMaxPlace(selectedChamber, selectedRack) ?: 5

    // Build the grid of current places
    val placesList = (1..maxPlace).map { place ->
        val code = WarehouseConfig.formatLocationCode(selectedChamber, selectedRack, selectedLevel, place)
        val matchingSlot = slots.find { it.locationCode == code }
        Pair(code, matchingSlot)
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
                text = "PLANIFICATION DES EMPLACEMENTS",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Text(
                text = "Visualisez la répartition spatiale de l'entrepôt par cellule.",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Segmented Cascading Dropdowns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // CH
                Box(modifier = Modifier.weight(1f)) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { chamberExpanded = true },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Ch. $selectedChamber", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                        }
                    }
                    DropdownMenu(expanded = chamberExpanded, onDismissRequest = { chamberExpanded = false }) {
                        listOf(1, 2, 3).forEach { ch ->
                            DropdownMenuItem(text = { Text("Chambre $ch") }, onClick = {
                                selectedChamber = ch
                                selectedRack = 1
                                chamberExpanded = false
                            })
                        }
                    }
                }

                // RACK
                Box(modifier = Modifier.weight(1.2f)) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { rackExpanded = true },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Ray. R" + selectedRack.toString().padStart(2, '0'), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                        }
                    }
                    DropdownMenu(expanded = rackExpanded, onDismissRequest = { rackExpanded = false }) {
                        (1..maxRack).forEach { r ->
                            val label = "R" + r.toString().padStart(2, '0')
                            DropdownMenuItem(text = { Text(label) }, onClick = {
                                selectedRack = r
                                rackExpanded = false
                            })
                        }
                    }
                }

                // LEVEL
                Box(modifier = Modifier.weight(1.2f)) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { levelExpanded = true },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Étg. E$selectedLevel", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                        }
                    }
                    DropdownMenu(expanded = levelExpanded, onDismissRequest = { levelExpanded = false }) {
                        (1..4).forEach { l ->
                            DropdownMenuItem(text = { Text("Étage $l") }, onClick = {
                                selectedLevel = l
                                levelExpanded = false
                            })
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Summary Info Panel of current row
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.MeetingRoom, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cellule CH$selectedChamber • Rayonnage R${selectedRack.toString().padStart(2, '0')} • Étage E$selectedLevel ($maxPlace places/row)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // GRILLE DES PLACES
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(placesList) { (locCode, sObj) ->
                    val isOccupied = sObj?.articleId != null
                    val matchingArt = articles.find { it.id == sObj?.articleId }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clickable {
                                clickedLocationCode = locCode
                                clickedSlotInfo = sObj
                                showSlotActionDialog = true
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isOccupied) MaterialTheme.colorScheme.surface
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isOccupied) 1.5.dp else 1.dp,
                            color = if (isOccupied) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Slot identifier
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "P" + locCode.takeLast(2),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = if (isOccupied) MaterialTheme.colorScheme.primary else Color.Gray
                                )

                                if (isOccupied) {
                                    Icon(
                                        Icons.Default.LocalShipping,
                                        contentDescription = "Occupé",
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Text(
                                        text = "VIDE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            if (isOccupied && matchingArt != null) {
                                Column {
                                    Text(
                                        text = matchingArt.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Réf: ${matchingArt.reference}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                    val slotRepDetails = mutableListOf<String>()
                                    if (!sObj?.supplierName.isNullOrBlank()) {
                                        slotRepDetails.add("🚛 " + sObj?.supplierName)
                                    }
                                    if (!sObj?.palletNumber.isNullOrBlank()) {
                                        slotRepDetails.add("📦 " + sObj?.palletNumber)
                                    }
                                    if (slotRepDetails.isNotEmpty()) {
                                        Text(
                                            text = slotRepDetails.joinToString(" • "),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.DarkGray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Emplacement libre",
                                    fontSize = 12.sp,
                                    color = Color.LightGray,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }

        // SLOOT ASSIGN / EMPTY DIALOG
        if (showSlotActionDialog) {
            val isOccupied = clickedSlotInfo?.articleId != null
            var selectedArticleToAssign by remember { mutableStateOf<Article?>(null) }
            var writeSupplier by remember { mutableStateOf("") }
            var writePalletNumber by remember { mutableStateOf("") }
            var assignMenuExpanded by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showSlotActionDialog = false },
                title = { Text("Emplacement $clickedLocationCode", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (isOccupied) {
                            val matched = articles.find { it.id == clickedSlotInfo?.articleId }
                            Text("Cet emplacement est actuellement occupé par :", fontSize = 12.sp, color = Color.Gray)
                            Text("Produit: ${matched?.name} (${matched?.reference})", fontWeight = FontWeight.Bold)
                            Text("Fournisseur: ${clickedSlotInfo?.supplierName}", fontWeight = FontWeight.SemiBold)
                            if (!clickedSlotInfo?.palletNumber.isNullOrBlank()) {
                                Text("Numéro Palette: ${clickedSlotInfo?.palletNumber}", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Voulez-vous libérer cet emplacement de stockage ?")
                        } else {
                            if (!canWrite) {
                                Text("L'emplacement est libre. (Droit de lecture seule)", color = Color.Gray)
                            } else {
                                Text("Assigner manuellement un article à cet emplacement :", fontSize = 12.sp, color = Color.Gray)

                                // Article Dropdown list
                                Box {
                                    OutlinedTextField(
                                        value = selectedArticleToAssign?.name ?: "Choisir l'article",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Article") },
                                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clickable { assignMenuExpanded = true }
                                    )
                                    DropdownMenu(expanded = assignMenuExpanded, onDismissRequest = { assignMenuExpanded = false }) {
                                        articles.forEach { product ->
                                            DropdownMenuItem(
                                                text = { Text("[${product.reference}] ${product.name}") },
                                                onClick = {
                                                    selectedArticleToAssign = product
                                                    assignMenuExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = writeSupplier,
                                    onValueChange = { writeSupplier = it },
                                    label = { Text("Raison Fournisseur (ex: TTF Primeur)") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = writePalletNumber,
                                    onValueChange = { writePalletNumber = it },
                                    label = { Text("Numéro Palette (ex: P-045)") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    if (isOccupied) {
                        if (canWrite) {
                            Button(
                                onClick = {
                                    viewModel.assignSlotManually(clickedLocationCode, null, "")
                                    showSlotActionDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                            ) {
                                Text("Libérer / Vider", color = Color.White)
                            }
                        }
                    } else {
                        if (canWrite) {
                            Button(
                                onClick = {
                                    selectedArticleToAssign?.let { art ->
                                        viewModel.assignSlotManually(clickedLocationCode, art.id, writeSupplier.trim(), writePalletNumber.trim())
                                    }
                                    showSlotActionDialog = false
                                },
                                enabled = selectedArticleToAssign != null
                            ) {
                                Text("Assigner")
                            }
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSlotActionDialog = false }) {
                        Text("Fermer")
                    }
                }
            )
        }
    }
}
