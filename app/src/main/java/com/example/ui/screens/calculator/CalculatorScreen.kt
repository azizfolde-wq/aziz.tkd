package com.example.ui.screens.calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.CalculatorHistoryEntity
import com.example.ui.components.KurdistanFlag
import com.example.ui.theme.KurdishGold
import com.example.ui.theme.KurdishGoldDark
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("calculator_screen"),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header with Title and Kurdistan Flag on Top-Left
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ژمێرەری پێشکەوتوو",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            KurdistanFlag(
                width = 36.dp,
                height = 24.dp,
                cornerRadius = 6.dp,
                elevation = 3.dp,
                modifier = Modifier.testTag("calculator_top_kurdistan_flag")
            )
        }

        // Top Toolbar (Deg/Rad, Scientific toggle, History)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Deg / Rad toggle
                FilledTonalButton(
                    onClick = { viewModel.toggleDegreeMode() },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("calc_deg_rad_toggle")
                ) {
                    Text(
                        text = if (state.isDegreeMode) "DEG (پلە)" else "RAD (ڕادیان)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Scientific Keypad expander
                FilledTonalButton(
                    onClick = { viewModel.toggleScientificKeypad() },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("calc_sci_toggle")
                ) {
                    Text(
                        text = if (state.isScientificExpanded) "سادە" else "زانستی",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // History Button
            IconButton(
                onClick = { viewModel.toggleHistorySheet() },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("calc_history_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = "مێژووی ژماردن",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Display Screen Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .shadow(4.dp, RoundedCornerShape(20.dp))
                .testTag("calc_display_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Bottom
            ) {
                // Expression
                Text(
                    text = if (state.expression.isEmpty()) "0" else state.expression,
                    fontSize = if (state.expression.length > 15) 24.sp else 36.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                // Live Preview Result
                if (state.liveResult.isNotEmpty()) {
                    Text(
                        text = "= ${state.liveResult}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                }

                // Error Message
                if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage ?: "",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Scientific Keypad Rows (Collapsible)
        AnimatedVisibility(visible = state.isScientificExpanded) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Sci Row 1
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CalcButton("√", Modifier.weight(1f), isFunc = true) { viewModel.onFunction("sqrt") }
                    CalcButton("x²", Modifier.weight(1f), isFunc = true) { viewModel.onInput("^2") }
                    CalcButton("xʸ", Modifier.weight(1f), isFunc = true) { viewModel.onInput("^") }
                    CalcButton("π", Modifier.weight(1f), isFunc = true) { viewModel.onInput("π") }
                    CalcButton("e", Modifier.weight(1f), isFunc = true) { viewModel.onInput("e") }
                }

                // Sci Row 2
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CalcButton("sin", Modifier.weight(1f), isFunc = true) { viewModel.onFunction("sin") }
                    CalcButton("cos", Modifier.weight(1f), isFunc = true) { viewModel.onFunction("cos") }
                    CalcButton("tan", Modifier.weight(1f), isFunc = true) { viewModel.onFunction("tan") }
                    CalcButton("ln", Modifier.weight(1f), isFunc = true) { viewModel.onFunction("ln") }
                    CalcButton("log", Modifier.weight(1f), isFunc = true) { viewModel.onFunction("log") }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Main Standard Keypad Rows
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: AC, Del, ( ), ÷
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalcButton("AC", Modifier.weight(1f), isSpecial = true) { viewModel.onClear() }
                CalcButton("DEL", Modifier.weight(1f), isSpecial = true) { viewModel.onBackspace() }
                CalcButton("(", Modifier.weight(0.5f), isOperator = true) { viewModel.onInput("(") }
                CalcButton(")", Modifier.weight(0.5f), isOperator = true) { viewModel.onInput(")") }
                CalcButton("÷", Modifier.weight(1f), isOperator = true) { viewModel.onInput("÷") }
            }

            // Row 2: 7, 8, 9, %, ×
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalcButton("7", Modifier.weight(1f)) { viewModel.onInput("7") }
                CalcButton("8", Modifier.weight(1f)) { viewModel.onInput("8") }
                CalcButton("9", Modifier.weight(1f)) { viewModel.onInput("9") }
                CalcButton("%", Modifier.weight(1f), isOperator = true) { viewModel.onInput("%") }
                CalcButton("×", Modifier.weight(1f), isOperator = true) { viewModel.onInput("×") }
            }

            // Row 3: 4, 5, 6, +/-, -
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalcButton("4", Modifier.weight(1f)) { viewModel.onInput("4") }
                CalcButton("5", Modifier.weight(1f)) { viewModel.onInput("5") }
                CalcButton("6", Modifier.weight(1f)) { viewModel.onInput("6") }
                CalcButton("+/-", Modifier.weight(1f), isOperator = true) { viewModel.onToggleSign() }
                CalcButton("-", Modifier.weight(1f), isOperator = true) { viewModel.onInput("-") }
            }

            // Row 4 & 5: 1, 2, 3, +, 0, ., =
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalcButton("1", Modifier.weight(1f)) { viewModel.onInput("1") }
                CalcButton("2", Modifier.weight(1f)) { viewModel.onInput("2") }
                CalcButton("3", Modifier.weight(1f)) { viewModel.onInput("3") }
                CalcButton("!", Modifier.weight(1f), isFunc = true) { viewModel.onInput("!") }
                CalcButton("+", Modifier.weight(1f), isOperator = true) { viewModel.onInput("+") }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalcButton("0", Modifier.weight(2f)) { viewModel.onInput("0") }
                CalcButton(".", Modifier.weight(1f)) { viewModel.onInput(".") }
                CalcButton("=", Modifier.weight(2f), isEquals = true) { viewModel.onEquals() }
            }
        }
    }

    // Calculation History Bottom Sheet
    if (state.isHistoryOpen) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.toggleHistorySheet() },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مێژووی ژماردنەکان",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (state.historyList.isNotEmpty()) {
                        TextButton(
                            onClick = { viewModel.clearAllHistory() },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("سڕینەوەی هەمووی")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (state.historyList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "هیچ مێژوویەکی پێشوو نییە",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.historyList) { item ->
                            HistoryItemCard(
                                item = item,
                                onUseExpression = { viewModel.useHistoryItem(item, useResult = false) },
                                onUseResult = { viewModel.useHistoryItem(item, useResult = true) },
                                onDelete = { viewModel.deleteHistoryItem(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalcButton(
    text: String,
    modifier: Modifier = Modifier,
    isOperator: Boolean = false,
    isSpecial: Boolean = false,
    isEquals: Boolean = false,
    isFunc: Boolean = false,
    onClick: () -> Unit
) {
    val containerColor = when {
        isEquals -> KurdishGold
        isOperator -> MaterialTheme.colorScheme.primaryContainer
        isSpecial -> MaterialTheme.colorScheme.errorContainer
        isFunc -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }

    val contentColor = when {
        isEquals -> Color.Black
        isOperator -> MaterialTheme.colorScheme.onPrimaryContainer
        isSpecial -> MaterialTheme.colorScheme.onErrorContainer
        isFunc -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    FilledTonalButton(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .height(52.dp)
            .testTag("calc_btn_$text")
    ) {
        Text(
            text = text,
            fontSize = if (isFunc) 14.sp else 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif
        )
    }
}

@Composable
fun HistoryItemCard(
    item: CalculatorHistoryEntity,
    onUseExpression: () -> Unit,
    onUseResult: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onUseResult() }
            ) {
                Text(
                    text = item.expression,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "= ${item.result}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "سڕینەوە",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
