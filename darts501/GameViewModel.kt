package com.example.darts501

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class Player(
    val name: String,
    val score: Int = 501,
    val legs: Int = 0,
    val matchDarts: Int = 0,
    val matchScored: Int = 0,
    val legDarts: Int = 0,
    val legScored: Int = 0,
    val lastScore: Int? = null
) {
    val matchAvg: Double? get() = if (matchDarts == 0) null else matchScored * 3.0 / matchDarts
    val legAvg: Double? get() = if (legDarts == 0) null else legScored * 3.0 / legDarts
}

private data class Snapshot(
    val p1: Player,
    val p2: Player,
    val current: Int,
    val starter: Int,
    val matchWinner: String?
)

class GameViewModel : ViewModel() {

    /** Результаты, которые нельзя набрать тремя дротиками. */
    private val impossible = setOf(179, 178, 176, 175, 173, 172, 171, 169)

    var p1 by mutableStateOf(Player("Olga"))
        private set
    var p2 by mutableStateOf(Player("Oleg"))
        private set

    var current by mutableStateOf(0)      // 0 = p1, 1 = p2
        private set
    var starter by mutableStateOf(0)      // кто начинал текущий лег
        private set

    var legsToWin by mutableStateOf(3)
        private set

    var input by mutableStateOf("")
        private set

    /** Число, ожидающее подтверждения (показывается на весь экран). */
    var pending by mutableStateOf<Int?>(null)
        private set

    var message by mutableStateOf<String?>(null)
        private set

    var matchWinner by mutableStateOf<String?>(null)
        private set

    var showSettings by mutableStateOf(false)

    private val history = ArrayDeque<Snapshot>()

    val currentPlayer: Player get() = if (current == 0) p1 else p2

    // ---------- ввод с клавиатуры ----------

    fun onDigit(d: Int) {
        if (pending != null || matchWinner != null) return
        val next = (input + d.toString()).trimStart('0').ifEmpty { "0" }
        if (next.length > 3) return
        val v = next.toIntOrNull() ?: return
        if (v > 180) return
        input = next
        message = null
    }

    fun onBackspace() {
        if (input.isNotEmpty()) input = input.dropLast(1)
    }

    fun onClear() {
        input = ""
    }

    /** Кнопка OK — отправляет введённое число на подтверждение. */
    fun submitInput() {
        val v = input.toIntOrNull() ?: return
        requestScore(v)
    }

    // ---------- подтверждение ----------

    fun requestScore(value: Int) {
        if (matchWinner != null) return
        if (value < 0 || value > 180) {
            message = "Недопустимый результат"
            return
        }
        if (value in impossible) {
            message = "$value набрать невозможно"
            return
        }
        input = value.toString()
        pending = value
        message = null
    }

    fun confirmPending() {
        val v = pending ?: return
        pending = null
        applyScore(v)
        input = ""
    }

    fun cancelPending() {
        pending = null
        input = ""
        message = "Отменено"
    }

    // ---------- игровая логика ----------

    private fun applyScore(value: Int) {
        history.addLast(Snapshot(p1, p2, current, starter, matchWinner))
        if (history.size > 100) history.removeFirst()

        val p = currentPlayer
        val remaining = p.score - value

        when {
            remaining < 0 || remaining == 1 -> {
                // перебор: очки не меняются, но 3 дротика засчитываются
                val np = p.copy(
                    matchDarts = p.matchDarts + 3,
                    legDarts = p.legDarts + 3,
                    lastScore = 0
                )
                setPlayer(np)
                message = "Перебор! ${p.name} остаётся на ${p.score}"
                switchTurn()
            }

            remaining == 0 -> {
                val np = p.copy(
                    score = 0,
                    legs = p.legs + 1,
                    matchDarts = p.matchDarts + 3,
                    matchScored = p.matchScored + value,
                    legDarts = p.legDarts + 3,
                    legScored = p.legScored + value,
                    lastScore = value
                )
                setPlayer(np)
                message = "${np.name} выигрывает лег!"
                if (np.legs >= legsToWin) {
                    matchWinner = np.name
                } else {
                    startNewLeg()
                }
            }

            else -> {
                val np = p.copy(
                    score = remaining,
                    matchDarts = p.matchDarts + 3,
                    matchScored = p.matchScored + value,
                    legDarts = p.legDarts + 3,
                    legScored = p.legScored + value,
                    lastScore = value
                )
                setPlayer(np)
                message = null
                switchTurn()
            }
        }
    }

    private fun setPlayer(pl: Player) {
        if (current == 0) p1 = pl else p2 = pl
    }

    private fun switchTurn() {
        current = 1 - current
    }

    private fun startNewLeg() {
        p1 = p1.copy(score = 501, legDarts = 0, legScored = 0, lastScore = null)
        p2 = p2.copy(score = 501, legDarts = 0, legScored = 0, lastScore = null)
        starter = 1 - starter
        current = starter
    }

    fun undo() {
        val s = history.removeLastOrNull() ?: run {
            message = "Отменять нечего"
            return
        }
        p1 = s.p1
        p2 = s.p2
        current = s.current
        starter = s.starter
        matchWinner = s.matchWinner
        pending = null
        input = ""
        message = "Отмена последнего броска"
    }

    fun newMatch() {
        history.clear()
        p1 = Player(p1.name)
        p2 = Player(p2.name)
        starter = 0
        current = 0
        matchWinner = null
        pending = null
        input = ""
        message = null
    }

    fun updateSettings(name1: String, name2: String, legs: Int) {
        p1 = p1.copy(name = name1.ifBlank { "Player 1" })
        p2 = p2.copy(name = name2.ifBlank { "Player 2" })
        legsToWin = legs.coerceIn(1, 21)
    }

    // ---------- голос ----------

    /** Обрабатывает все варианты распознавания, берёт первый подходящий. */
    fun onVoice(variants: List<String>) {
        for (raw in variants) {
            val tokens = VoiceParser.normalize(raw)
            if (tokens.isEmpty()) continue

            if (pending != null) {
                if (VoiceParser.isYes(tokens)) {
                    confirmPending()
                    return
                }
                if (VoiceParser.isNo(tokens)) {
                    cancelPending()
                    return
                }
                continue
            }

            if (matchWinner != null) continue

            val v = VoiceParser.parseScoreCommand(tokens)
            if (v != null) {
                requestScore(v)
                return
            }
        }
    }
}
