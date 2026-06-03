package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.model.VaultDoc
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudyViewModel

// ==========================================
// SCREEN 6: KNOWLEDGE VAULT (NOTEBOOKLM)
// ==========================================
@Composable
fun KnowledgeVaultPage(viewModel: StudyViewModel) {
    val documents by viewModel.allVaultDocs.collectAsState()
    val loading by viewModel.aiLoading.collectAsState()
    val ragResult by viewModel.ragResult.collectAsState()
    
    var queryText by remember { mutableStateOf("") }
    var mockDocName by remember { mutableStateOf("") }
    var mockDocContent by remember { mutableStateOf("") }
    var showAddDocDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "NotebookLM Knowledge Vault 📁",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = ActiveWhite
        )
        Text(
            text = "Upload NCERT notes or syllabus PDFs. The AI uses local retrieval-augmented generation (RAG) to ensure answers cite verified textual sources.",
            fontSize = 12.sp,
            color = MutedSlate
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Document Section Row
        Text(
            text = "Your Indexed Knowledge Sources",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = SpaceNeonTeal
        )
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                Card(
                    modifier = Modifier
                        .size(width = 130.dp, height = 90.dp)
                        .clickable { showAddDocDialog = true },
                    colors = CardDefaults.cardColors(containerColor = SlateGray),
                    border = borderIndicator(true, SpaceNeonTeal)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(10.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.FileUpload, contentDescription = "Upload", tint = SpaceNeonTeal)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Add Source", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ActiveWhite)
                    }
                }
            }

            if (documents.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.size(width = 200.dp, height = 90.dp),
                        colors = CardDefaults.cardColors(containerColor = SlateLight)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(10.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "No custom documents yet.", color = MutedSlate, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Click Add Source to simulate upload.", color = MutedSlate, fontSize = 9.sp)
                        }
                    }
                }
            } else {
                items(documents) { doc ->
                    Card(
                        modifier = Modifier.size(width = 160.dp, height = 90.dp),
                        colors = CardDefaults.cardColors(containerColor = SlateGray),
                        border = borderIndicator(false, BorderSlate)
                    ) {
                        Column(modifier = Modifier.padding(8.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Icon(
                                    imageVector = if (doc.mimeType.contains("Audio")) Icons.Filled.Audiotrack else Icons.Filled.InsertDriveFile,
                                    contentDescription = "doc format",
                                    tint = if (doc.mimeType.contains("Audio")) SpaceNeonTeal else NeonPurple,
                                    modifier = Modifier.size(16.dp)
                                )
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "del",
                                    tint = CoralOrange,
                                    modifier = Modifier.size(14.dp).clickable { viewModel.deleteDocument(doc.id) }
                                )
                            }
                            Column {
                                Text(text = doc.title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = ActiveWhite, maxLines = 1)
                                Text(
                                    text = if (doc.mimeType.contains("Audio")) "Podcast Summary" else "2,500 words indexed",
                                    fontSize = 9.sp,
                                    color = MutedSlate
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Grounded Semantic search input
        Text(text = "Query your Vault", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ActiveWhite)
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = queryText,
                onValueChange = { queryText = it },
                placeholder = { Text("Ask about accounts qualitative indicators...", color = MutedSlate) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonPurple,
                    unfocusedBorderColor = BorderSlate,
                    focusedTextColor = ActiveWhite,
                    unfocusedTextColor = ActiveWhite,
                    focusedContainerColor = SlateGray,
                    unfocusedContainerColor = SlateGray
                ),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = {
                    if (queryText.isNotBlank()) {
                        viewModel.groundedQueryNotebookLM(queryText)
                    }
                },
                enabled = !loading,
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(56.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = ActiveWhite, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.Search, contentDescription = "RAG search", tint = ActiveWhite)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large output scrollbox for grounded answer
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.dp, BorderSlate, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SlateGray)
        ) {
            LazyColumn(modifier = Modifier.padding(14.dp).fillMaxSize()) {
                if (ragResult.isEmpty()) {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(top = 40.dp)
                        ) {
                            Icon(Icons.Filled.Gavel, "Notebook icon", tint = BorderSlate, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Source Grounded Output Panel",
                                fontWeight = FontWeight.Bold,
                                color = MutedSlate,
                                fontSize = 14.sp
                            )
                            Text(
                                "Your answers will display grounded cites to verified page numbers and snippets.",
                                color = MutedSlate,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                } else {
                    item {
                        Text(
                            text = ragResult,
                            color = ActiveWhite,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Add doc Dialog
        if (showAddDocDialog) {
            AlertDialog(
                onDismissRequest = { showAddDocDialog = false },
                containerColor = SlateGray,
                title = { Text("Upload Simulated Source Document", color = ActiveWhite, fontSize = 16.sp) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Add study materials directly into Class 11 workspace data catalog", fontSize = 11.sp, color = MutedSlate)
                        OutlinedTextField(
                            value = mockDocName,
                            onValueChange = { mockDocName = it },
                            placeholder = { Text("E.g., Accountancy_Chapter_1_NCERT.pdf", color = MutedSlate) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SpaceNeonTeal,
                                focusedTextColor = ActiveWhite,
                                unfocusedTextColor = ActiveWhite
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = mockDocContent,
                            onValueChange = { mockDocContent = it },
                            placeholder = { Text("Paste NCERT text: Qualitative traits of accounting include Reliability, Relevance, Comparability...", color = MutedSlate) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SpaceNeonTeal,
                                focusedTextColor = ActiveWhite,
                                unfocusedTextColor = ActiveWhite
                            ),
                            maxLines = 6,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = SpaceNeonTeal),
                        onClick = {
                            if (mockDocName.isNotBlank() && mockDocContent.isNotBlank()) {
                                viewModel.uploadDocumentMock(mockDocName, mockDocContent)
                                mockDocName = ""
                                mockDocContent = ""
                                showAddDocDialog = false
                            }
                        }
                    ) {
                        Text("Index Document", color = DeepSpace)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDocDialog = false }) {
                        Text("Cancel", color = MutedSlate)
                    }
                }
            )
        }
    }
}

// ==========================================
// SCREEN 7: AI STUDY COACH CHAT
// ==========================================
@Composable
fun AiCoachPage(viewModel: StudyViewModel) {
    val chatHistory by viewModel.chatHistory.collectAsState()
    val loading by viewModel.aiLoading.collectAsState()
    var promptInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "AI study Coach 🎯",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = ActiveWhite
        )
        Text(
            text = "Your personal educational director. Ask about time management, curriculum questions, study roadmaps, or why you're struggling with Accounts ledger entries.",
            fontSize = 12.sp,
            color = MutedSlate
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Chat logs bubble scroll area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(SlateGray)
                .border(1.dp, BorderSlate, RoundedCornerShape(16.dp))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(chatHistory) { (msg, isUser) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 12.dp,
                                        topEnd = 12.dp,
                                        bottomStart = if (isUser) 12.dp else 0.dp,
                                        bottomEnd = if (isUser) 0.dp else 12.dp
                                    )
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUser) SlateLight else BorderSlate
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (isUser) "You" else "Mission Control AI Coach",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUser) SpaceNeonTeal else NeonPurple
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = msg,
                                    fontSize = 12.sp,
                                    color = ActiveWhite,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Text query send box
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = promptInput,
                onValueChange = { promptInput = it },
                placeholder = { Text("Ask anything... 'Design study guide for Sunday'", color = MutedSlate) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SpaceNeonTeal,
                    unfocusedBorderColor = BorderSlate,
                    focusedTextColor = ActiveWhite,
                    unfocusedTextColor = ActiveWhite,
                    focusedContainerColor = SlateGray,
                    unfocusedContainerColor = SlateGray
                ),
                maxLines = 2,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = {
                    if (promptInput.isNotBlank()) {
                        viewModel.sendCoachMessage(promptInput)
                        promptInput = ""
                    }
                },
                enabled = !loading,
                colors = ButtonDefaults.buttonColors(containerColor = SpaceNeonTeal),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(56.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = DeepSpace, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.Send, contentDescription = "Send text prompt", tint = DeepSpace)
                }
            }
        }
    }
}

// ==========================================
// SCREEN 8: COMMERCE COMPANION (Numerical Ledger Engine)
// ==========================================
@Composable
fun CommerceCompanionPage(viewModel: StudyViewModel) {
    var pastedQuestion by remember { mutableStateOf("") }
    val decodedResult by viewModel.decodedResult.collectAsState()
    val loading by viewModel.aiLoading.collectAsState()

    // Guided accounting journal interactive practice parameters
    val currentLedgerIndex = remember { mutableStateOf(0) }
    val selectedDr = remember { mutableStateOf("") }
    val selectedCr = remember { mutableStateOf("") }
    val balanceOutput = remember { mutableStateOf<String?>(null) }
    
    val transactionsPractice = listOf(
        Triple("Goods purchased from Ramesh for Rs. 10,000 credit", "Purchases", "Ramesh"),
        Triple("Cash Sales made to Suresh for Rs. 5,000", "Cash", "Sales"),
        Triple("Deposited excess cash of Rs. 2,000 into Bank", "Bank", "Cash"),
        Triple("Paid Ramesh Rs. 1,000 cash discount allowed Rs. 100", "Ramesh", "Cash")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Commerce Companion module 🧮",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = ActiveWhite
            )
            Text(
                text = "Accountancy Numericals and qualitative business/economics solvers. Test yourself on double-entry concepts or query guided solutions directly.",
                fontSize = 12.sp,
                color = MutedSlate
            )
        }

        // 1. AI QUESTION DECODER SECTION
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSlate, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SlateGray)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Decoder Engine (Solve complex questions)",
                        fontWeight = FontWeight.Bold,
                        color = SpaceNeonTeal,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Paste your bookkeeping transactions or study syllabus questions. Gemini extracts variables and shows step-by-step solutions.",
                        color = MutedSlate,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = pastedQuestion,
                        onValueChange = { pastedQuestion = it },
                        placeholder = { Text("Type or paste question: E.g., What is contra entry in a Three Column Cash Book with cash deposited?", color = MutedSlate) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SpaceNeonTeal,
                            focusedTextColor = ActiveWhite,
                            unfocusedTextColor = ActiveWhite
                        ),
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (pastedQuestion.isNotBlank()) {
                                viewModel.analyzeAndDecodeQuestion(pastedQuestion)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SpaceNeonTeal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !loading
                    ) {
                        if (loading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = DeepSpace)
                        } else {
                            Text("Decode accounting steps", color = DeepSpace, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (decodedResult.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = BorderSlate)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "Solution & Breakdown from AI Coach:", fontWeight = FontWeight.Bold, color = GoldYellow, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = decodedResult, color = ActiveWhite, fontSize = 11.sp, lineHeight = 16.sp)
                    }
                }
            }
        }

        // 2. INTERACTIVE JOURNAL DR/CR BUILDER
        item {
            val trans = transactionsPractice[currentLedgerIndex.value]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSlate, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SlateGray)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Journal Entry Practice Laboratory",
                        fontWeight = FontWeight.Bold,
                        color = NeonPurple,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Review ledger accounts and match appropriate Debit (Dr.) and Credit (Cr.) channels below.",
                        color = MutedSlate,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SlateLight)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Transaction ${currentLedgerIndex.value + 1}: \"${trans.first}\"",
                            color = ActiveWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Select Debit Account (Dr.)", color = MutedSlate, fontSize = 11.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Purchases", "Cash", "Bank", "Ramesh", "Sales").forEach { acct ->
                            val isDr = selectedDr.value == acct
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isDr) SpaceNeonTeal.copy(alpha = 0.2f) else SlateLight)
                                    .border(0.5.dp, if (isDr) SpaceNeonTeal else BorderSlate, RoundedCornerShape(6.dp))
                                    .clickable { selectedDr.value = acct }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(text = acct, fontSize = 10.sp, color = if (isDr) SpaceNeonTeal else ActiveWhite)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Select Credit Account (Cr.)", color = MutedSlate, fontSize = 11.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Purchases", "Cash", "Bank", "Ramesh", "Sales").forEach { acct ->
                            val isCr = selectedCr.value == acct
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isCr) CoralOrange.copy(alpha = 0.2f) else SlateLight)
                                    .border(0.5.dp, if (isCr) CoralOrange else BorderSlate, RoundedCornerShape(6.dp))
                                    .clickable { selectedCr.value = acct }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(text = acct, fontSize = 10.sp, color = if (isCr) CoralOrange else ActiveWhite)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (selectedDr.value == trans.second && selectedCr.value == trans.third) {
                                    balanceOutput.value = "CORRECT ENTRY! Double-Entry tallied (+20 XP)"
                                    viewModel.awardXp(20)
                                } else {
                                    balanceOutput.value = "WRONG. Hint: Debit what comes in or your expenses (${trans.second} A/c Dr. to ${trans.third} A/c)."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Submit Entry", color = ActiveWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                selectedDr.value = ""
                                selectedCr.value = ""
                                balanceOutput.value = null
                                currentLedgerIndex.value = (currentLedgerIndex.value + 1) % transactionsPractice.size
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SlateLight),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Next Trans", color = ActiveWhite, fontSize = 11.sp)
                        }
                    }

                    balanceOutput.value?.let { response ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = response,
                            color = if (response.contains("CORRECT")) SpaceNeonTeal else CoralOrange,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 9: ANALYTICS PAGE (Custom Drawing Chart)
// ==========================================
@Composable
fun AnalyticsPage(viewModel: StudyViewModel) {
    val logs by viewModel.allStudyLogs.collectAsState()
    val allTasks by viewModel.allBrainItems.collectAsState()
    val chapters by viewModel.allChapters.collectAsState()

    val totalHours = logs.sumOf { it.durationMinutes } / 60.0
    val tasksFinishedCount = allTasks.filter { it.isCompleted }.size
    val chaptersCount = chapters.filter { it.isCompleted }.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Performance Command Center 📊",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = ActiveWhite
            )
            Text(
                text = "Analyze your visual study habits, streak trends, focus sessions, and syllabus retention rates.",
                fontSize = 12.sp,
                color = MutedSlate
            )
        }

        // Simple Stat Cards Grid
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = SlateGray)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "Focus log Hours", fontSize = 10.sp, color = MutedSlate)
                        Text(text = String.format("%.1fh", totalHours), fontSize = 20.sp, fontWeight = FontWeight.Black, color = SpaceNeonTeal)
                    }
                }
                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = SlateGray)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "Brain completed", fontSize = 10.sp, color = MutedSlate)
                        Text(text = "$tasksFinishedCount Tasks", fontSize = 20.sp, fontWeight = FontWeight.Black, color = NeonPurple)
                    }
                }
                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = SlateGray)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "Completed chapters", fontSize = 10.sp, color = MutedSlate)
                        Text(text = "$chaptersCount syllabus", fontSize = 20.sp, fontWeight = FontWeight.Black, color = GoldYellow)
                    }
                }
            }
        }

        // CUSTOM BAR CHART USING COMPOSE CANVAS (Avoiding external charts elegantly)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSlate, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SlateGray)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "📅 Focus Records Week-to-Date (m)", fontWeight = FontWeight.Bold, color = ActiveWhite, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Canvas chart drawing
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        val barData = listOf(25f, 50f, 0f, 75f, 40f, 60f, 25f) // simulated weekly minutes
                        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                        val maxVal = 100f
                        val barWidth = 24.dp.toPx()
                        val spacing = (size.width - (barData.size * barWidth)) / (barData.size + 1)

                        // Base horizontal guide line
                        drawLine(
                            color = BorderSlate,
                            start = Offset(0f, size.height - 20.dp.toPx()),
                            end = Offset(size.width, size.height - 20.dp.toPx()),
                            strokeWidth = 1.dp.toPx()
                        )

                        barData.forEachIndexed { idx, value ->
                            val left = spacing + idx * (barWidth + spacing)
                            val heightRatio = value / maxVal
                            val top = (size.height - 20.dp.toPx()) - ((size.height - 40.dp.toPx()) * heightRatio)
                            val rectHeight = (size.height - 20.dp.toPx()) - top

                            // Draw the bar
                            drawRect(
                                brush = Brush.verticalGradient(listOf(SpaceNeonTeal, NeonPurple.copy(alpha = 0.5f))),
                                topLeft = Offset(left, top),
                                size = Size(barWidth, rectHeight)
                            )

                            // Label
                            // (We could write overlay text, but for simple rendering, drawing standard rectangles looks exceptionally clean!)
                        }
                    }
                    
                    // Simple legends row
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                            Text(text = day, fontSize = 10.sp, color = MutedSlate, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }

        // Daily Analytics Insights summary content
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSlate, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SlateGray)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "💡 Study Habit Insights", fontWeight = FontWeight.Bold, color = SpaceNeonTeal, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Your preferred study hours peak between 08:00 AM and 10:30 AM on \"Fresh\" energy states.\n" +
                               "• Accountancy remains at risk (Qualitative chapters are finished, but Recording entries lacks 1 day of practice reviews).\n" +
                               "• Spaced repetition accuracy star average is 4.2. Keep revision cycles on schedule to sustain level progress!",
                        fontSize = 11.sp,
                        color = MutedSlate,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

// ==========================================
// SCREEN 10: PROFILE & SETTINGS
// ==========================================
@Composable
fun ProfileSettingsPage(viewModel: StudyViewModel) {
    val profile by viewModel.userProfile.collectAsState()
    
    var nameField by remember { mutableStateOf(profile.name) }
    var streamField by remember { mutableStateOf(profile.stream) }
    var careerField by remember { mutableStateOf(profile.careerGoal) }
    var sleepField by remember { mutableStateOf(profile.sleepTime) }
    var editStatus by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Profile Settings Deck ⚙",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = ActiveWhite
        )
        Text(
            text = "Personalize your target exam stream, carrier trajectory, sleep parameters, and manual triggers for study panic mode.",
            fontSize = 12.sp,
            color = MutedSlate
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.weight(1f)) {
            // Edit profile details
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, BorderSlate, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = SlateGray)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Personal Identity Credentials", fontWeight = FontWeight.Bold, color = SpaceNeonTeal, fontSize = 14.sp)
                        
                        OutlinedTextField(
                            value = nameField,
                            onValueChange = { nameField = it },
                            label = { Text("Student Name", color = MutedSlate) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = ActiveWhite, unfocusedTextColor = ActiveWhite),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = careerField,
                            onValueChange = { careerField = it },
                            label = { Text("Target Career Target (E.g. Chartered Accountant)", color = MutedSlate) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = ActiveWhite, unfocusedTextColor = ActiveWhite),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Stream toggle option buttons
                        Text("Academic Stream Selector", fontSize = 12.sp, color = MutedSlate)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Commerce", "Science").forEach { streamOption ->
                                val isSelected = streamField == streamOption
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) NeonPurple.copy(alpha = 0.2f) else SlateLight)
                                        .border(0.5.dp, if (isSelected) NeonPurple else BorderSlate, RoundedCornerShape(8.dp))
                                        .clickable { streamField = streamOption }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = streamOption, fontSize = 12.sp, color = if (isSelected) NeonPurple else MutedSlate, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.saveUserProfile(
                                    profile.copy(
                                        name = nameField,
                                        stream = streamField,
                                        careerGoal = careerField
                                    )
                                )
                                editStatus = "Syllabus tracking adjusted! Preferences stored."
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SpaceNeonTeal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Apply Credentials Customizer", color = DeepSpace, fontWeight = FontWeight.Bold)
                        }

                        editStatus?.let { msg ->
                            Text(text = msg, color = SpaceNeonTeal, fontSize = 11.sp)
                        }
                    }
                }
            }

            // EXAM EMERGENCY MODE TRIGGER
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, BorderSlate, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = SlateGray)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Exam Emergency Engine Override", fontWeight = FontWeight.Bold, color = CoralOrange, fontSize = 13.sp)
                                Text(text = "Tap to simulate when final exams are inside 30 days.", fontSize = 10.sp, color = MutedSlate)
                            }
                            Switch(
                                checked = profile.isEmergencyMode,
                                onCheckedChange = { viewModel.runExamEmergencyMode(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = CoralOrange, checkedTrackColor = SlateLight)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Border custom helper mapping
fun borderIndicator(isActive: Boolean, color: Color): androidx.compose.foundation.BorderStroke? {
    return if (isActive) androidx.compose.foundation.BorderStroke(1.dp, color) else null
}
