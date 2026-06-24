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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.db.Article
import com.example.viewmodel.AppViewModel

@Composable
fun StockScreen(viewModel: AppViewModel) {
    val articles by viewModel.articlesList.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val searchQuery by viewModel.articleSearchQuery.collectAsState()
    val slots by viewModel.slotAssignmentsList.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedArticle by remember { mutableStateOf<Article?>(null) }

    // Dialog Input States
    var refInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }

    val userRole = currentUser?.role ?: "consultateur"
    val canWrite = userRole == "admin" || userRole == "user"

    // Filtered list
    val filteredArticles = articles.filter { article ->
        val articleMatches = article.name.contains(searchQuery, ignoreCase = true) ||
                article.reference.contains(searchQuery, ignoreCase = true) ||
                article.description.contains(searchQuery, ignoreCase = true)
        
        val matchingSlotsForArticle = slots.filter { it.articleId == article.id }
        val slotMatches = matchingSlotsForArticle.any { slot ->
            slot.locationCode.contains(searchQuery, ignoreCase = true) ||
            slot.supplierName.contains(searchQuery, ignoreCase = true)
        }
        
        articleMatches || slotMatches
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
                text = "GESTION DU STOCK",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Text(
                text = "Suivez et administrez le catalogue d'articles du stock principal.",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.articleSearchQuery.value = it },
                placeholder = { Text("Rechercher par référence, désignation...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("article_search_input"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                )
            )

            if (filteredArticles.isEmpty()) {
                // Beautiful Empty State
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Inventory,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isEmpty()) "Aucun article enregistré." else "Aucun résultat trouvé.",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    Text(
                        text = if (searchQuery.isEmpty()) "Ajoutez votre premier produit à l'inventaire en cliquant sur le bouton '+'." else "Ajustez vos termes de recherche.",
                        fontSize = 12.sp,
                        color = Color.Gray.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredArticles) { article ->
                        ArticleCard(
                            article = article,
                            slots = slots,
                            canWrite = canWrite,
                            onEdit = {
                                selectedArticle = article
                                refInput = article.reference
                                nameInput = article.name
                                descInput = article.description
                                showEditDialog = true
                            },
                            onDelete = {
                                selectedArticle = article
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }

        // Floating Action Button to Add Article
        if (canWrite) {
            FloatingActionButton(
                onClick = {
                    refInput = ""
                    nameInput = ""
                    descInput = ""
                    showAddDialog = true
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 80.dp, end = 16.dp) // float above bottom navigation gracefully!
                    .testTag("add_article_fab"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter un article")
            }
        }

        // Add Product Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Créer un article", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = refInput,
                            onValueChange = { refInput = it },
                            label = { Text("Référence (Unique, ex: ART01)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Nom / Désignation") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = descInput,
                            onValueChange = { descInput = it },
                            label = { Text("Description / Informations") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.addArticle(refInput.trim(), nameInput.trim(), descInput.trim())
                            showAddDialog = false
                        }
                    ) {
                        Text("Enregistrer")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Annuler")
                    }
                }
            )
        }

        // Edit Product Dialog
        if (showEditDialog && selectedArticle != null) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Modifier l'article", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = refInput,
                            onValueChange = { refInput = it },
                            label = { Text("Référence") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Nom / Désignation") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = descInput,
                            onValueChange = { descInput = it },
                            label = { Text("Description / Informations") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            selectedArticle?.let { art ->
                                viewModel.updateArticle(art.id, refInput.trim(), nameInput.trim(), descInput.trim())
                            }
                            showEditDialog = false
                        }
                    ) {
                        Text("Mettre à jour")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("Annuler")
                    }
                }
            )
        }

        // Delete Product Dialog
        if (showDeleteDialog && selectedArticle != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444)) },
                title = { Text("Supprimer l'article ?", fontWeight = FontWeight.Bold) },
                text = {
                    Text("Voulez-vous vraiment supprimer définitivement l'article ${selectedArticle?.reference} : ${selectedArticle?.name} de la base de données ?\nCette opération est irréversible.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            selectedArticle?.let { art ->
                                viewModel.deleteArticle(art.id)
                            }
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("Supprimer", color = Color.White)
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
fun ArticleCard(
    article: Article,
    slots: List<com.example.db.SlotAssignment>,
    canWrite: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isOutOfStock = article.quantity <= 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Letter Emblem
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isOutOfStock) Color(0xFFEF4444).copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = article.reference.take(2).uppercase(),
                    color = if (isOutOfStock) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Main Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = article.reference,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )

                    if (isOutOfStock) {
                        Text(
                            text = "RUPTURE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444),
                            modifier = Modifier
                                .background(Color(0xFFEF4444).copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = article.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (article.description.isNotBlank()) {
                    Text(
                        text = article.description,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Affichage dynamique des emplacements et fournisseurs associés
                val matchingSlots = slots.filter { it.articleId == article.id }
                if (matchingSlots.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        matchingSlots.forEach { slot ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "📍 ${slot.locationCode}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                                if (slot.supplierName.isNotBlank() || slot.palletNumber.isNotBlank()) {
                                    val details = mutableListOf<String>()
                                    if (slot.supplierName.isNotBlank()) details.add("Fourn: ${slot.supplierName}")
                                    if (slot.palletNumber.isNotBlank()) details.add("Pal: ${slot.palletNumber}")
                                    Text(
                                        text = details.joinToString(" • "),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Gray,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Non stocké (Pas d'emplacement)",
                        fontSize = 10.sp,
                        color = Color.Gray.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Quantity Indicator & CRUD Operations
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                // Large Quantity Count
                Text(
                    text = article.quantity.toString(),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = if (isOutOfStock) Color(0xFFEF4444) else Color(0xFF10B981)
                )
                Text(
                    text = "palettes",
                    fontSize = 10.sp,
                    color = Color.Gray
                )

                if (canWrite) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
