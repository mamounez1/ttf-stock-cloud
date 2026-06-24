package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.db.Article
import com.example.viewmodel.AppViewModel
import com.example.viewmodel.ReceiptDocument
import com.example.viewmodel.ReceiptItem

data class ComptageRow(
    val article: Article,
    val totalIn: Int,
    val totalOut: Int,
    val balance: Int,
    val actualCount: Int
)

@Composable
fun ComptageScreen(viewModel: AppViewModel) {
    val articles by viewModel.articlesList.collectAsState()
    val entries by viewModel.stockEntriesList.collectAsState()
    val exits by viewModel.stockExitsList.collectAsState()

    val coloredBrandBlue = Color(0xFF00A3FF)

    // Aggregate Calculations
    val comptageList = articles.map { art ->
        val totalIn = entries.filter { it.articleId == art.id }.sumOf { it.quantity }
        val totalOut = exits.filter { it.articleId == art.id }.sumOf { it.quantity }
        val theoreticalBalance = totalIn - totalOut
        ComptageRow(
            article = art,
            totalIn = totalIn,
            totalOut = totalOut,
            balance = theoreticalBalance,
            actualCount = art.quantity
        )
    }

    // Grand Totals
    val grandTotalIn = comptageList.sumOf { it.totalIn }
    val grandTotalOut = comptageList.sumOf { it.totalOut }
    val grandTotalActual = comptageList.sumOf { it.actualCount }

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
                        text = "COMPTAGE DE L'INVENTAIRE",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "STOCK FINAL = ENTRÉES - SORTIES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }

                // Global print summary of count
                IconButton(
                    onClick = {
                        val doc = ReceiptDocument(
                            title = "RAPPORT DE COMPTAGE GLOBAL",
                            documentNumber = "RPT-" + System.currentTimeMillis().toString().takeLast(6),
                            date = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.FRANCE).format(java.util.Date()),
                            extraDetails = mapOf("Statut" to "Clôturé / Vérifié"),
                            items = comptageList.map { c ->
                                ReceiptItem(
                                    palletNo = c.article.reference,
                                    articleInfo = c.article.name,
                                    supplier = "Soldes In/Out: ${c.totalIn} p / ${c.totalOut} p",
                                    qty = c.actualCount,
                                    quality = "Théorique: ${c.balance}",
                                    location = if (c.actualCount == c.balance) "En règle ✅" else "Écart ⚠️"
                                )
                            }
                        )
                        viewModel.showReceipt(doc)
                    },
                    modifier = Modifier.testTag("print_comptage_btn")
                ) {
                    Icon(Icons.Default.Print, contentDescription = "Imprimer l'inventaire")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // STATS TILES ROW
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Total Entrées",
                    value = grandTotalIn.toString(),
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Total Sorties",
                    value = grandTotalOut.toString(),
                    color = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Stock Réel",
                    value = grandTotalActual.toString(),
                    color = coloredBrandBlue,
                    modifier = Modifier.weight(1.1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // TABULAR ROW HEADER
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ARTICLE / RÉF", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.4f))
                    Text("ENTRÉES", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                    Text("SORTIES", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                    Text("ST. FINAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.9f), textAlign = TextAlign.Center)
                    Text("VÉRIF", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f), textAlign = TextAlign.End)
                }
            }

            // LIST COMPTAGE
            if (comptageList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aucun article disponible pour comptage.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(comptageList) { item ->
                        ComptageRowItem(row = item)
                        Divider(color = Color.LightGray.copy(alpha = 0.15f))
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun ComptageRowItem(row: ComptageRow) {
    val discrepancy = row.actualCount != row.balance
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1.4f)) {
            Text(row.article.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            Text(row.article.reference, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
        }
        Text("+${row.totalIn}", fontSize = 11.sp, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center, color = Color(0xFF10B981))
        Text("-${row.totalOut}", fontSize = 11.sp, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center, color = Color(0xFFEF4444))
        Text("${row.balance}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.9f), textAlign = TextAlign.Center, color = Color.Black)
        
        Row(
            modifier = Modifier.weight(0.7f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${row.actualCount}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (discrepancy) Color(0xFFEF4444) else Color(0xFF10B981)
            )
            if (discrepancy) {
                Spacer(modifier = Modifier.width(3.dp))
                Text("⚠️", fontSize = 10.sp)
            }
        }
    }
}
