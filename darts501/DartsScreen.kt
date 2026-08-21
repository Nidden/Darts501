package com.example.darts501

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF0B0B1E)
private val Panel = Color(0xFF1E1E4B)
private val PanelDark = Color(0xFF12122E)
private val Accent = Color(0xFFE91E63)
private val BlueAccent = Color(0xFF2962FF)
private val Dim = Color(0xFF8E8EA9)

@Composable
fun DartsScreen(
    vm: GameViewModel,
    voice: VoiceController,
    onMicToggle: () -> Unit
) {
    Box(Modifier.fillMaxSize().background(Bg)) {
        Column(Modifier.fillMaxSize()) {

            // ---------- заголовок ----------
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "DARTS 501",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { vm.newMatch() }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Новый матч", tint = Color.White)
                }
                IconButton(onClick = { vm.showSettings = true }) {
                    Icon(Icons.Filled.Settings, contentDescription = "Настройки", tint = Color.White)
                }
            }

            Box(Modifier.fillMaxWidth().height(2.dp).background(Accent))

            // ---------- имена и леги ----------
            Row(
                Modifier.fillMaxWidth().background(Color.Black).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(if (vm.current == 0) Accent else Color.Transparent))
                Spacer(Modifier.size(8.dp))
                Text(
                    "${vm.p1.name} (${vm.p1.legs})",
                    color = if (vm.current == 0) Color.White else Dim,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "(${vm.p2.legs}) ${vm.p2.name}",
                    color = if (vm.current == 1) Color.White else Dim,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.size(8.dp))
                Box(Modifier.size(10.dp).clip(CircleShape).background(if (vm.current == 1) Accent else Color.Transparent))
            }

            Row(
                Modifier.fillMaxWidth().background(PanelDark).padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "легов до победы: ${vm.legsToWin}",
                    color = Dim,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ---------- крупные очки ----------
            Row(Modifier.fillMaxWidth().weight(1f)) {
                ScorePanel(vm.p1, vm.current == 0, Modifier.weight(1f))
                Box(Modifier.fillMaxHeight().width(1.dp).background(Color.Black))
                ScorePanel(vm.p2, vm.current == 1, Modifier.weight(1f))
            }

            // ---------- сообщение ----------
            Box(
                Modifier.fillMaxWidth().background(PanelDark).padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    vm.message ?: " ",
                    color = Accent,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // ---------- строка ввода ----------
            Row(
                Modifier.fillMaxWidth().background(Color.Black).padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.weight(1f).height(56.dp).background(Color.White, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (vm.input.isEmpty()) "Введите очки" else vm.input,
                        color = if (vm.input.isEmpty()) Color(0xFF9E9E9E) else Color.Black,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.size(12.dp))
                IconButton(onClick = { vm.undo() }, modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Filled.Undo, contentDescription = "Отмена", tint = BlueAccent, modifier = Modifier.size(36.dp))
                }
            }

            Box(Modifier.fillMaxWidth().height(2.dp).background(BlueAccent))

            // ---------- клавиатура ----------
            Column(Modifier.fillMaxWidth().weight(1.1f).background(PanelDark)) {
                KeyRow(listOf("1", "2", "3"), vm, Modifier.weight(1f))
                KeyRow(listOf("4", "5", "6"), vm, Modifier.weight(1f))
                KeyRow(listOf("7", "8", "9"), vm, Modifier.weight(1f))
                KeyRow(listOf("C", "0", "OK"), vm, Modifier.weight(1f))
            }

            // ---------- микрофон ----------
            Row(
                Modifier.fillMaxWidth().background(Color.Black).padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(if (voice.isListening) Accent else Color(0xFF303050))
                        .clickable { onMicToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (voice.isListening) Icons.Filled.Mic else Icons.Filled.MicOff,
                        contentDescription = "Микрофон",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.size(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(voice.status, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(
                        if (voice.heard.isBlank()) "скажите: «Счёт сто сорок»" else voice.heard,
                        color = Dim,
                        fontSize = 13.sp,
                        maxLines = 2
                    )
                }
            }
        }

        // ---------- подтверждение на весь экран ----------
        val pending = vm.pending
        if (pending != null) {
            ConfirmOverlay(
                value = pending,
                playerName = vm.currentPlayer.name,
                remaining = vm.currentPlayer.score,
                onYes = { vm.confirmPending() },
                onNo = { vm.cancelPending() }
            )
        }

        // ---------- победа в матче ----------
        val winner = vm.matchWinner
        if (winner != null) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Матч окончен") },
                text = { Text("$winner выигрывает матч ${vm.p1.legs} : ${vm.p2.legs}") },
                confirmButton = {
                    TextButton(onClick = { vm.newMatch() }) { Text("Новый матч") }
                },
                dismissButton = {
                    TextButton(onClick = { vm.undo() }) { Text("Отменить бросок") }
                }
            )
        }

        if (vm.showSettings) {
            SettingsDialog(vm)
        }
    }
}

@Composable
private fun ScorePanel(player: Player, active: Boolean, modifier: Modifier) {
    Column(
        modifier
            .fillMaxHeight()
            .background(if (active) Panel else PanelDark)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            player.score.toString(),
            color = if (active) Color.White else Dim,
            fontSize = 76.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Spacer(Modifier.size(8.dp))
        Text(
            "ma: ${fmt(player.matchAvg)} | la: ${fmt(player.legAvg)}",
            color = Dim,
            fontSize = 14.sp
        )
        Text(
            "посл.: ${player.lastScore?.toString() ?: "-"}",
            color = Dim,
            fontSize = 14.sp
        )
    }
}

private fun fmt(v: Double?): String =
    if (v == null) "-" else String.format("%.1f", v)

@Composable
private fun KeyRow(keys: List<String>, vm: GameViewModel, modifier: Modifier) {
    Row(modifier.fillMaxWidth()) {
        for (k in keys) {
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(2.dp)
                    .background(
                        when (k) {
                            "OK" -> BlueAccent
                            "C" -> Color(0xFF3A2040)
                            else -> Color(0xFF1A1A3A)
                        },
                        RoundedCornerShape(6.dp)
                    )
                    .clickable {
                        when (k) {
                            "OK" -> vm.submitInput()
                            "C" -> vm.onClear()
                            else -> vm.onDigit(k.toInt())
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    k,
                    color = Color.White,
                    fontSize = if (k.length > 1) 22.sp else 30.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ConfirmOverlay(
    value: Int,
    playerName: String,
    remaining: Int,
    onYes: () -> Unit,
    onNo: () -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xF2000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(playerName, color = Dim, fontSize = 22.sp)
            Spacer(Modifier.size(8.dp))
            Text(
                value.toString(),
                color = Color.White,
                fontSize = 180.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(Modifier.size(8.dp))
            Text("останется ${remaining - value}", color = Dim, fontSize = 20.sp)
            Spacer(Modifier.size(24.dp))
            Text("скажите «ДА» или «НЕТ»", color = Accent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.size(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(
                    onClick = onYes,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    modifier = Modifier.weight(1f).height(72.dp).padding(end = 8.dp)
                ) {
                    Text("ДА", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Button(
                    onClick = onNo,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                    modifier = Modifier.weight(1f).height(72.dp).padding(start = 8.dp)
                ) {
                    Text("НЕТ", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun SettingsDialog(vm: GameViewModel) {
    var name1 by remember { mutableStateOf(vm.p1.name) }
    var name2 by remember { mutableStateOf(vm.p2.name) }
    var legs by remember { mutableStateOf(vm.legsToWin.toString()) }

    AlertDialog(
        onDismissRequest = { vm.showSettings = false },
        title = { Text("Настройки") },
        text = {
            Column {
                OutlinedTextField(
                    value = name1,
                    onValueChange = { name1 = it },
                    label = { Text("Игрок 1") },
                    singleLine = true
                )
                Spacer(Modifier.size(8.dp))
                OutlinedTextField(
                    value = name2,
                    onValueChange = { name2 = it },
                    label = { Text("Игрок 2") },
                    singleLine = true
                )
                Spacer(Modifier.size(8.dp))
                OutlinedTextField(
                    value = legs,
                    onValueChange = { s -> legs = s.filter { it.isDigit() }.take(2) },
                    label = { Text("Легов до победы") },
                    singleLine = true
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    "Голос: «Счёт 140» → число на весь экран → «Да» или «Нет».",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                vm.updateSettings(name1, name2, legs.toIntOrNull() ?: vm.legsToWin)
                vm.showSettings = false
            }) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = { vm.showSettings = false }) { Text("Отмена") }
        }
    )
}
