package com.smart.aicalculator.ui.screens.aisolve

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smart.aicalculator.ui.components.AppChip
import com.smart.aicalculator.ui.components.AppHeader
import com.smart.aicalculator.ui.components.CoralButton
import com.smart.aicalculator.ui.components.EmptyState
import com.smart.aicalculator.ui.components.SectionHeader
import com.smart.aicalculator.ui.components.SmartCard
import com.smart.aicalculator.ui.theme.AppTheme
import com.smart.aicalculator.ui.theme.UrbanistFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSolveScreen(
    showHeader: Boolean = true,
    viewModel: AiSolveViewModel = viewModel()
) {
    val context = LocalContext.current
    val query by viewModel.inputQuery.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val examples = listOf(
        "18% GST on 2500",
        "10% discount on 1999",
        "15% of 3500",
        "Split 2400 between 4",
        "Solve 2x + 5 = 15"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        if (showHeader) {
            AppHeader(
                title = "Smart Solve",
                subtitle = "Type a calculation in plain words"
            )
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Input card
        SmartCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { viewModel.updateInputQuery(it) },
                    placeholder = {
                        Text(
                            text = "e.g. 18% GST on 2500",
                            fontSize = 15.sp,
                            color = AppTheme.colors.textTertiary
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(108.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = AppTheme.colors.border,
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background
                    ),
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateInputQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear input",
                                    tint = AppTheme.colors.textTertiary
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            viewModel.solveQuery(query)
                        }
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                CoralButton(
                    text = "Solve",
                    onClick = {
                        keyboardController?.hide()
                        viewModel.solveQuery(query)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = query.isNotBlank()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        SectionHeader(title = "Try an example")
        Spacer(modifier = Modifier.height(10.dp))
        FlowExamples(examples = examples) { example ->
            viewModel.updateInputQuery(example)
            viewModel.solveQuery(example)
            keyboardController?.hide()
        }

        Spacer(modifier = Modifier.height(20.dp))

        when (val state = uiState) {
            is SmartSolveUiState.Idle -> {
                EmptyState(
                    icon = Icons.Default.AutoAwesome,
                    title = "Solve in plain words",
                    subtitle = "Type a question or tap an example. Everything runs offline on your device.",
                    modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
                )
            }
            is SmartSolveUiState.Success -> {
                ResultCard(
                    answer = state.result.finalAnswer,
                    steps = state.result.steps,
                    onCopy = { viewModel.copyToClipboard(context, state.result) }
                )
            }
            is SmartSolveUiState.Unsupported -> {
                SmartCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    border = false
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = state.message,
                            fontFamily = UrbanistFontFamily,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowExamples(
    examples: List<String>,
    onClick: (String) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        examples.forEach { example ->
            AppChip(text = example, onClick = { onClick(example) })
        }
    }
}

@Composable
private fun ResultCard(
    answer: String,
    steps: List<String>,
    onCopy: () -> Unit
) {
    SmartCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(title = "Answer", modifier = Modifier.weight(1f))
                IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy result",
                        tint = AppTheme.colors.textTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = answer,
                fontFamily = UrbanistFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                lineHeight = 32.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (steps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = AppTheme.colors.border)
                Spacer(modifier = Modifier.height(18.dp))
                SectionHeader(title = "How it's solved")
                Spacer(modifier = Modifier.height(12.dp))
                steps.forEachIndexed { index, step ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = UrbanistFontFamily
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = step,
                            fontFamily = UrbanistFontFamily,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
