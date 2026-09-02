package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ConsoleLog
import com.example.ui.MainViewModel
import com.example.ui.theme.NaturalBackground
import com.example.ui.theme.NaturalDanger
import com.example.ui.theme.NaturalOnBackground
import com.example.ui.theme.NaturalOnPrimary
import com.example.ui.theme.NaturalOnSurfaceVariant
import com.example.ui.theme.NaturalOutline
import com.example.ui.theme.NaturalOutlineVariant
import com.example.ui.theme.NaturalPrimary
import com.example.ui.theme.NaturalPrimaryContainer
import com.example.ui.theme.NaturalSecondaryContainer
import com.example.ui.theme.NaturalSuccess
import com.example.ui.theme.NaturalSuccessContainer
import com.example.ui.theme.NaturalSurface

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConsoleTab(
    viewModel: MainViewModel,
    logs: List<ConsoleLog>,
    isBusy: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var commandInput by remember { mutableStateOf("") }

    val quickCommands = listOf(
        "pm grant ${context.packageName} android.permission.WRITE_SETTINGS",
        "pm grant ${context.packageName} android.permission.WRITE_SECURE_SETTINGS",
        "settings get global window_animation_scale",
        "settings get system screen_off_timeout",
        "pm list packages -3",
        "dumpsys battery",
        "getprop ro.build.version.release",
        "whoami"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NaturalBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(2.dp))

        // Command Prompt Input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = commandInput,
                onValueChange = { commandInput = it },
                placeholder = { Text("ADB command (e.g. pm list packages)") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("shell_command_input"),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (commandInput.isNotBlank()) {
                            viewModel.runCustomShellCommand(commandInput)
                            commandInput = ""
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NaturalPrimary,
                    unfocusedBorderColor = NaturalOutline,
                    focusedTextColor = NaturalOnBackground,
                    unfocusedTextColor = NaturalOnBackground
                ),
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Terminal, contentDescription = null, tint = NaturalPrimary)
                }
            )

            Button(
                onClick = {
                    if (commandInput.isNotBlank()) {
                        viewModel.runCustomShellCommand(commandInput)
                        commandInput = ""
                    }
                },
                modifier = Modifier
                    .size(54.dp)
                    .testTag("send_command_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NaturalPrimary, contentColor = NaturalOnPrimary),
                enabled = !isBusy
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "Run", modifier = Modifier.size(20.dp))
            }
        }

        // Quick Command Chips
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "QUICK SHIZUKU ADB COMMANDS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = NaturalOnSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickCommands.forEach { cmd ->
                    FilterChip(
                        selected = false,
                        onClick = { viewModel.runCustomShellCommand(cmd) },
                        shape = RoundedCornerShape(14.dp),
                        label = {
                            Text(
                                text = if (cmd.length > 28) cmd.take(28) + "..." else cmd,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = NaturalSecondaryContainer,
                            labelColor = NaturalPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = NaturalOutlineVariant,
                            selectedBorderColor = NaturalPrimary,
                            enabled = true,
                            selected = false
                        )
                    )
                }
            }
        }

        // Console Output View
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("console_output_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = NaturalSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, NaturalOutline)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(NaturalSuccess)
                        )
                        Text(
                            text = "PRIVILEGED ADB CONSOLE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NaturalPrimary,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }

                    IconButton(
                        onClick = { viewModel.clearLogs() },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("clear_console_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear logs",
                            tint = NaturalOnSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),
                    color = NaturalSecondaryContainer.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NaturalOutlineVariant)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (logs.isEmpty()) {
                            item {
                                Text(
                                    text = "Privileged shell active. Run commands above or tap quick chips to execute with Shizuku.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = NaturalOnSurfaceVariant,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        } else {
                            items(logs) { log ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = log.time,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = NaturalOnSurfaceVariant,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp
                                        )
                                    )
                                    Text(
                                        text = log.text,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (log.isError) NaturalDanger else NaturalOnBackground,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            fontWeight = if (log.isError) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}
