package com.runanywhere.startup_hackathon20

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.runanywhere.startup_hackathon20.network.P2PDebateService
import kotlinx.coroutines.delay
import java.util.UUID

// Color Palette
private val CyanPrimary = Color(0xFF00D9FF)
private val PurpleAccent = Color(0xFF9D4EDD)
private val DarkBackground = Color(0xFF0A0A0F)
private val DarkCard = Color(0xFF16213E)
private val TextWhite = Color(0xFFFFFFFF)
private val TextGray = Color(0xFFB0B0B0)

/**
 * Test P2P Button - Shows in Home Screen (Debug builds or always-on for testing)
 * Allows testing P2P flow on a single device
 */
@Composable
fun TestP2PButton(
    onStartTestP2P: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Always show for testing (remove BuildConfig.DEBUG check if needed)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D1B4E).copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Build,
                contentDescription = "Test Mode",
                tint = PurpleAccent,
                modifier = Modifier.size(32.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🧪 Test P2P Mode",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    text = "Test P2P on single device",
                    fontSize = 12.sp,
                    color = TextGray
                )
            }

            Button(
                onClick = onStartTestP2P,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurpleAccent
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("START")
            }
        }
    }
}

/**
 * Test P2P Preparation - Shows test info and starts
 */
@Composable
fun TestP2PPreparationDialog(
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Build,
                contentDescription = "Test",
                tint = PurpleAccent,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "🧪 Test P2P Mode",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "This will start a mock P2P session on this device:",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• You: Test Player 1",
                    fontSize = 13.sp,
                    color = TextGray
                )
                Text(
                    text = "• Opponent: Test Bot (mock)",
                    fontSize = 13.sp,
                    color = TextGray
                )
                Text(
                    text = "• Topic: AI should be regulated",
                    fontSize = 13.sp,
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚠️ This is for testing only. No server connection.",
                    fontSize = 12.sp,
                    color = Color(0xFFFBBF24),
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Generate test session ID
                    val testSessionId = "test_${UUID.randomUUID()}"
                    val testUserId = "test_user_${System.currentTimeMillis()}"
                    onConfirm(testSessionId, testUserId)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurpleAccent
                )
            ) {
                Text("Start Test")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Mock P2P Session Generator
 * Creates a fake session for testing
 */
object TestP2PSessionGenerator {

    fun createMockSession(
        sessionId: String,
        userId: String,
        userName: String
    ): P2PDebateService.P2PSessionResponse {
        val currentTime = System.currentTimeMillis().toString()

        return P2PDebateService.P2PSessionResponse(
            session_id = sessionId,
            topic_id = "1",
            topic_title = "Artificial Intelligence should be regulated by governments",
            topic_description = "Debate whether AI development and deployment should have government oversight and regulation, or if the tech industry should self-regulate.",
            player1_id = userId,
            player1_name = userName,
            player1_side = "FOR",
            player2_id = "test_bot_opponent",
            player2_name = "Test Bot",
            player2_side = "AGAINST",
            current_turn = userId, // Player starts first
            turn_number = 1,
            status = "PREP",
            prep_time_remaining = 30,
            debate_time_remaining = 900000L, // 15 minutes
            start_time = currentTime,
            end_time = null,
            created_at = currentTime,
            updated_at = currentTime
        )
    }

    /**
     * Generate mock opponent responses
     */
    fun generateMockOpponentResponse(playerMessage: String, turnNumber: Int): String {
        val responses = listOf(
            "I understand your point, but I believe the opposite is true. Free markets drive innovation better than government oversight ever could.",
            "While that's a valid concern, government regulation historically stifles technological progress and leads to bureaucratic inefficiency.",
            "That argument overlooks the fact that private companies have stronger incentives to self-regulate than governments to properly regulate them.",
            "I respectfully disagree. The benefits of unrestricted AI development far outweigh the potential risks you're describing.",
            "Your example doesn't account for the unintended consequences that always come with government intervention in rapidly evolving fields.",
            "History has shown that technology self-regulation works better. Consider how the internet developed without heavy-handed government control.",
            "That's an interesting perspective, but it ignores how market forces and competition naturally encourage responsible AI development.",
            "While safety is important, overregulation could push AI development to countries with less oversight, making the problem worse."
        )

        // Return a contextual response based on turn number
        return responses[turnNumber % responses.size]
    }
}
