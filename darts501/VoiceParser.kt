package com.example.darts501

/**
 * Разбор распознанной русской речи:
 *  - ключевое слово "счёт" / "счет" + число  -> результат броска
 *  - "да" / "нет" -> подтверждение
 * Числа понимаются и цифрами ("счет 140"), и словами ("счет сто сорок").
 */
object VoiceParser {

    private val KEYWORDS = setOf(
        "счет", "счеты", "счета", "щет", "счёт", "score", "скор", "цвет", "свет"
    )

    private val YES = setOf("да", "ага", "верно", "точно", "ок", "окей", "yes", "подтверждаю")
    private val NO = setOf("нет", "не", "отмена", "отменить", "неверно", "no", "стереть")

    private val WORDS: Map<String, Int> = mapOf(
        "ноль" to 0, "нуль" to 0,
        "один" to 1, "одна" to 1, "одно" to 1, "раз" to 1,
        "два" to 2, "две" to 2,
        "три" to 3,
        "четыре" to 4,
        "пять" to 5,
        "шесть" to 6,
        "семь" to 7,
        "восемь" to 8,
        "девять" to 9,
        "десять" to 10,
        "одиннадцать" to 11,
        "двенадцать" to 12,
        "тринадцать" to 13,
        "четырнадцать" to 14,
        "пятнадцать" to 15,
        "шестнадцать" to 16,
        "семнадцать" to 17,
        "восемнадцать" to 18,
        "девятнадцать" to 19,
        "двадцать" to 20,
        "тридцать" to 30,
        "сорок" to 40,
        "пятьдесят" to 50,
        "шестьдесят" to 60,
        "семьдесят" to 70,
        "восемьдесят" to 80,
        "девяносто" to 90,
        "сто" to 100
    )

    /** Приводит строку к нижнему регистру, ё -> е, убирает всё кроме букв и цифр. */
    fun normalize(raw: String): List<String> {
        val s = raw.lowercase().replace('ё', 'е')
        val sb = StringBuilder()
        for (ch in s) {
            sb.append(if (ch.isLetterOrDigit()) ch else ' ')
        }
        return sb.toString().split(' ').filter { it.isNotBlank() }
    }

    fun isYes(tokens: List<String>): Boolean = tokens.any { it in YES }

    fun isNo(tokens: List<String>): Boolean = tokens.any { it in NO }

    /**
     * Ищет ключевое слово и число после него.
     * Возвращает null, если команды нет.
     */
    fun parseScoreCommand(tokens: List<String>): Int? {
        val idx = tokens.indexOfFirst { it in KEYWORDS }
        if (idx < 0) return null
        val tail = tokens.subList(idx + 1, tokens.size)
        return parseNumber(tail)
    }

    /** Число из последовательности токенов: "сто сорок" -> 140, "140" -> 140. */
    fun parseNumber(tokens: List<String>): Int? {
        var sum = 0
        var found = false
        for (t in tokens) {
            val digits = t.toIntOrNull()
            if (digits != null) {
                sum += digits
                found = true
                continue
            }
            val w = WORDS[t]
            if (w != null) {
                sum += w
                found = true
                continue
            }
            if (found) break   // число закончилось, дальше не смотрим
        }
        if (!found) return null
        if (sum < 0 || sum > 180) return null
        return sum
    }
}
