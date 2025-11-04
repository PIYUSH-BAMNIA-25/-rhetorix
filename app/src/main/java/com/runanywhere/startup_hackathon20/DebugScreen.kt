package com.runanywhere.startup_hackathon20

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.runanywhere.startup_hackathon20.database.RhetorixDatabase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val database = remember { RhetorixDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    var users by remember { mutableStateOf(listOf<com.runanywhere.startup_hackathon20.database.UserEntity>()) }
    var debates by remember { mutableStateOf(listOf<com.runanywhere.startup_hackathon20.database.DebateHistoryEntity>()) }
    var selectedUserId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            database.userDao().getAllUsers().collect {
                users = it
            }
        }
    }

    LaunchedEffect(selectedUserId) {
        selectedUserId?.let { userId ->
            scope.launch {
                database.debateHistoryDao().getUserDebateHistory(userId).collect {
                    debates = it
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Database Debug View") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("← Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Users Section
            Text(
                text = "Users (${users.size})",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (users.isEmpty()) {
                Text(
                    text = "No users found. Sign up to create your first user!",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(users) { user ->
                        UserCard(
                            user = user,
                            isSelected = user.id == selectedUserId,
                            onClick = { selectedUserId = user.id }
                        )
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Debate History Section
            selectedUserId?.let {
                Text(
                    text = "Debate History (${debates.size})",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (debates.isEmpty()) {
                    Text(
                        text = "No debates yet. Start a debate to see history!",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(debates) { debate ->
                            DebateCard(debate = debate)
                        }
                    }
                }
            } ?: Text(
                text = "Select a user to view their debate history",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun UserCard(
    user: com.runanywhere.startup_hackathon20.database.UserEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = user.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                if (user.isLoggedIn) {
                    Text(
                        text = "🟢 Logged In",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                text = "Email: ${user.email}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "DOB: ${user.dateOfBirth}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Games: ${user.totalGames}",
                    fontSize = 12.sp
                )
                Text(
                    text = "Wins: ${user.wins}",
                    fontSize = 12.sp
                )
                Text(
                    text = "Losses: ${user.losses}",
                    fontSize = 12.sp
                )
                Text(
                    text = "Avg: ${String.format("%.1f", user.averageScore)}",
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun DebateCard(debate: com.runanywhere.startup_hackathon20.database.DebateHistoryEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (debate.won)
                MaterialTheme.colorScheme.tertiaryContainer
            else
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (debate.won) "✅ Won" else "❌ Lost",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${debate.userScore} - ${debate.opponentScore}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = debate.topic,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Side: ${debate.userSide}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "vs ${debate.opponentType}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (debate.feedback.isNotBlank()) {
                Text(
                    text = "Feedback: ${debate.feedback.take(100)}...",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
