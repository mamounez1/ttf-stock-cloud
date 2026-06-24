package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.viewmodel.AppViewModel
import com.example.viewmodel.ReceiptDocument

@Composable
fun ReceiptOverlay(viewModel: AppViewModel, receipt: ReceiptDocument) {
    val context = LocalContext.current

    // Build plain string to share receipt details cleanly
    val shareContent = buildString {
        appendLine("==================================")
        appendLine("           TTF STOCK              ")
        appendLine("        ${receipt.title}          ")
        appendLine("==================================")
        appendLine("Document: ${receipt.documentNumber}")
        appendLine("Date: ${receipt.date}")
        receipt.extraDetails.forEach { (k, v) ->
            appendLine("$k: $v")
        }
        appendLine("----------------------------------")
        appendLine("Palettes / Articles :")
        receipt.items.forEach { item ->
            appendLine("- ${item.palletNo} | ${item.articleInfo}")
            appendLine("  Qté: ${item.qty} | Qualité: ${item.quality} | Loc: ${item.location}")
            appendLine("  Fournisseur: ${item.supplier}")
        }
        appendLine("==================================")
        appendLine("Signatures requises d'usine et de dechargement")
    }

    Dialog(
        onDismissRequest = { viewModel.clearActiveReceipt() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF0F172A).copy(alpha = 0.95f) // atmospheric dark glass overlay!
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header of preview
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("APPREÇU BON D'ENTREPRÔT", color = Color.White, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { viewModel.clearActiveReceipt() }) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Printable Receipt Canvas
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White), // high precision white sheet
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Logo of enterprise & title
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_ttf_logo),
                                contentDescription = "TTF Logo",
                                modifier = Modifier
                                    .size(width = 120.dp, height = 52.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = receipt.title,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black
                                )
                                Text(
                                    text = "N° " + receipt.documentNumber,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0B49FF)
                                )
                            }
                        }

                        Divider(color = Color.Black, modifier = Modifier.padding(vertical = 10.dp))

                        // Header details of transaction
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Émetteur : TTF Stock S.A.", fontSize = 11.sp, color = Color.Gray)
                                Text("Date : ${receipt.date}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                receipt.extraDetails.forEach { (label, value) ->
                                    Text("$label : $value", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Table header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9))
                                .padding(6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("PALETTE / PRODUIT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(1.5f))
                            Text("FOURN.", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text("LOC", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                            Text("QTY / QL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(0.7f), textAlign = TextAlign.End)
                        }

                        // List of items
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(receipt.items) { item ->
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${item.palletNo} - ${item.articleInfo}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            modifier = Modifier.weight(1.5f)
                                        )
                                        Text(
                                            text = item.supplier,
                                            fontSize = 9.sp,
                                            color = Color.DarkGray,
                                            modifier = Modifier.weight(1f),
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = item.location,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0D9488),
                                            modifier = Modifier.weight(0.8f),
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "${item.qty} p. [${item.quality}]",
                                            fontSize = 11.sp,
                                            color = Color.Black,
                                            modifier = Modifier.weight(0.7f),
                                            textAlign = TextAlign.End
                                        )
                                    }
                                    Divider(color = Color.LightGray.copy(alpha = 0.5f), modifier = Modifier.padding(top = 4.dp))
                                }
                            }
                        }

                        // Signatures
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                                Text("VISA RESPONSABLE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray, textDecoration = TextDecoration.Underline)
                                Spacer(modifier = Modifier.height(30.dp))
                                Text("..................................", color = Color.LightGray)
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                Text("VISA CHAUFFEUR / ACCUSÉ", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray, textDecoration = TextDecoration.Underline)
                                Spacer(modifier = Modifier.height(30.dp))
                                Text("..................................", color = Color.LightGray)
                            }
                        }

                        // Code-like footer representation
                        Text(
                            text = "Généré numériquement via TTF Stock (Android Unified Application File System Module)",
                            fontSize = 8.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions toolbar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Native share action
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareContent)
                            }
                            context.startActivity(Intent.createChooser(intent, "Partager le bon de stock"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Partager / SMS", color = Color.White)
                    }

                    // Native printing mock indicator or alerts
                    Button(
                        onClick = {
                            // Direct feedback and standard android notification
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Impression " + receipt.title)
                                putExtra(Intent.EXTRA_TEXT, shareContent)
                            }
                            context.startActivity(Intent.createChooser(intent, "Imprimer via Print Spooler"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B49FF)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Print, null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Imprimer PDF", color = Color.White)
                    }
                }
            }
        }
    }
}
