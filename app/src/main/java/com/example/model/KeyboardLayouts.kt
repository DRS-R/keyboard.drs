package com.example.model

object KeyboardLayouts {

    val devQuickBarKeys = listOf(
        KeyDef("ESC", type = KeyType.ESC, weight = 1f, isSpecial = true),
        KeyDef("TAB", type = KeyType.TAB, weight = 1f, isSpecial = true),
        KeyDef("CTRL", type = KeyType.CTRL, weight = 1.1f, isSpecial = true),
        KeyDef("ALT", type = KeyType.ALT, weight = 1f, isSpecial = true),
        KeyDef("{", weight = 0.9f),
        KeyDef("}", weight = 0.9f),
        KeyDef("(", weight = 0.9f),
        KeyDef(")", weight = 0.9f),
        KeyDef("[", weight = 0.9f),
        KeyDef("]", weight = 0.9f),
        KeyDef("<", weight = 0.9f),
        KeyDef(">", weight = 0.9f),
        KeyDef("=", weight = 0.9f),
        KeyDef(";", weight = 0.9f),
        KeyDef(":", weight = 0.9f),
        KeyDef("\"", weight = 0.9f),
        KeyDef("'", weight = 0.9f),
        KeyDef("`", weight = 0.9f),
        KeyDef("$", weight = 0.9f),
        KeyDef("#", weight = 0.9f),
        KeyDef("~", weight = 0.9f),
        KeyDef("|", weight = 0.9f),
        KeyDef("&", weight = 0.9f),
        KeyDef("->", weight = 1f),
        KeyDef("=>", weight = 1f),
        KeyDef("←", type = KeyType.ARROW_LEFT, weight = 1f, isSpecial = true),
        KeyDef("→", type = KeyType.ARROW_RIGHT, weight = 1f, isSpecial = true),
        KeyDef("↑", type = KeyType.ARROW_UP, weight = 1f, isSpecial = true),
        KeyDef("↓", type = KeyType.ARROW_DOWN, weight = 1f, isSpecial = true)
    )

    fun getEnglishLayout(caps: Boolean): List<List<KeyDef>> {
        val r1 = listOf(
            KeyDef(if (caps) "Q" else "q", topLeft = "1", topRight = "[", bottomLeft = "!", bottomRight = "\\"),
            KeyDef(if (caps) "W" else "w", topLeft = "2", topRight = "]", bottomLeft = "@", bottomRight = "|"),
            KeyDef(if (caps) "E" else "e", topLeft = "3", topRight = "{", bottomLeft = "#", bottomRight = "€"),
            KeyDef(if (caps) "R" else "r", topLeft = "4", topRight = "}", bottomLeft = "$", bottomRight = "₹"),
            KeyDef(if (caps) "T" else "t", topLeft = "5", topRight = "(", bottomLeft = "%", bottomRight = "™"),
            KeyDef(if (caps) "Y" else "y", topLeft = "6", topRight = ")", bottomLeft = "^", bottomRight = "¥"),
            KeyDef(if (caps) "U" else "u", topLeft = "7", topRight = "<", bottomLeft = "&", bottomRight = "§"),
            KeyDef(if (caps) "I" else "i", topLeft = "8", topRight = ">", bottomLeft = "*", bottomRight = "¿"),
            KeyDef(if (caps) "O" else "o", topLeft = "9", topRight = "=", bottomLeft = "(", bottomRight = "º"),
            KeyDef(if (caps) "P" else "p", topLeft = "0", topRight = "+", bottomLeft = ")", bottomRight = "¶")
        )

        val r2 = listOf(
            KeyDef(if (caps) "A" else "a", topLeft = "~", topRight = "`", bottomLeft = "-", bottomRight = "±"),
            KeyDef(if (caps) "S" else "s", topLeft = "_", topRight = "/", bottomLeft = "+", bottomRight = "§"),
            KeyDef(if (caps) "D" else "d", topLeft = "$", topRight = "\\", bottomLeft = "=", bottomRight = "°"),
            KeyDef(if (caps) "F" else "f", topLeft = "#", topRight = "|", bottomLeft = "_", bottomRight = "•"),
            KeyDef(if (caps) "G" else "g", topLeft = "&", topRight = "^", bottomLeft = ";", bottomRight = "©"),
            KeyDef(if (caps) "H" else "h", topLeft = "*", topRight = "%", bottomLeft = ":", bottomRight = "®"),
            KeyDef(if (caps) "J" else "j", topLeft = "-", topRight = ";", bottomLeft = "'", bottomRight = "∆"),
            KeyDef(if (caps) "K" else "k", topLeft = "+", topRight = ":", bottomLeft = "\"", bottomRight = "×"),
            KeyDef(if (caps) "L" else "l", topLeft = "=", topRight = "'", bottomLeft = "/", bottomRight = "÷")
        )

        val r3 = listOf(
            KeyDef("⇧", type = KeyType.SHIFT, weight = 1.3f, isSpecial = true),
            KeyDef(if (caps) "Z" else "z", topLeft = "<", topRight = ">", bottomLeft = ",", bottomRight = "«"),
            KeyDef(if (caps) "X" else "x", topLeft = ">", topRight = "/", bottomLeft = ".", bottomRight = "»"),
            KeyDef(if (caps) "C" else "c", topLeft = "\"", topRight = "'", bottomLeft = "?", bottomRight = "¢"),
            KeyDef(if (caps) "V" else "v", topLeft = "'", topRight = "\"", bottomLeft = "!", bottomRight = "√"),
            KeyDef(if (caps) "B" else "b", topLeft = ";", topRight = ":", bottomLeft = "@", bottomRight = "∞"),
            KeyDef(if (caps) "N" else "n", topLeft = ":", topRight = ",", bottomLeft = "#", bottomRight = "≠"),
            KeyDef(if (caps) "M" else "m", topLeft = ",", topRight = ".", bottomLeft = "%", bottomRight = "≈"),
            KeyDef("⌫", type = KeyType.DELETE, weight = 1.3f, isSpecial = true)
        )

        val r4 = listOf(
            KeyDef("?123", type = KeyType.MODE_SWITCH, weight = 1.2f, isSpecial = true),
            KeyDef("🌐", type = KeyType.LANGUAGE_SWITCH, weight = 1.0f, isSpecial = true),
            KeyDef("⌨ Dev", type = KeyType.PROGRAMMER_BAR, weight = 1.1f, isSpecial = true),
            KeyDef(" ", displayLabel = "Space", type = KeyType.SPACE, weight = 3.6f),
            KeyDef(".", weight = 0.9f),
            KeyDef("↵", type = KeyType.ENTER, weight = 1.3f, isSpecial = true)
        )

        return listOf(r1, r2, r3, r4)
    }

    fun getArabicLayout(): List<List<KeyDef>> {
        val r1 = listOf(
            KeyDef("ض", topLeft = "1", topRight = "َ"),
            KeyDef("ص", topLeft = "2", topRight = "ً"),
            KeyDef("ث", topLeft = "3", topRight = "ُ"),
            KeyDef("ق", topLeft = "4", topRight = "ٌ"),
            KeyDef("ف", topLeft = "5", topRight = "لإ"),
            KeyDef("غ", topLeft = "6", topRight = "إ"),
            KeyDef("ع", topLeft = "7", topRight = "‘"),
            KeyDef("ه", topLeft = "8", topRight = "÷"),
            KeyDef("خ", topLeft = "9", topRight = "×"),
            KeyDef("ح", topLeft = "0", topRight = "؛"),
            KeyDef("ج", topLeft = "-", topRight = "<"),
            KeyDef("د", topLeft = "=", topRight = ">")
        )

        val r2 = listOf(
            KeyDef("ش", topLeft = "ِ", topRight = "ٍ"),
            KeyDef("س", topLeft = "ْ", topRight = "ّ"),
            KeyDef("ي", topLeft = "]", topRight = "["),
            KeyDef("ب", topLeft = "}", topRight = "{"),
            KeyDef("ل", topLeft = "لأ", topRight = "لآ"),
            KeyDef("ا", topLeft = "أ", topRight = "آ"),
            KeyDef("ت", topLeft = "~", topRight = "ـ"),
            KeyDef("ن", topLeft = "؟", topRight = "!"),
            KeyDef("م", topLeft = "«", topRight = "»"),
            KeyDef("ك", topLeft = ":", topRight = "\""),
            KeyDef("ط", topLeft = "؛", topRight = "'")
        )

        val r3 = listOf(
            KeyDef("حركات", type = KeyType.SHIFT, weight = 1.2f, isSpecial = true),
            KeyDef("ذ", topLeft = "`"),
            KeyDef("ء", topLeft = "ئ"),
            KeyDef("ؤ", topLeft = "،"),
            KeyDef("ر", topLeft = "."),
            KeyDef("ى", topLeft = "؟"),
            KeyDef("ة", topLeft = "؛"),
            KeyDef("و", topLeft = ","),
            KeyDef("ز", topLeft = "/"),
            KeyDef("ظ", topLeft = "\\"),
            KeyDef("⌫", type = KeyType.DELETE, weight = 1.3f, isSpecial = true)
        )

        val r4 = listOf(
            KeyDef("?123", type = KeyType.MODE_SWITCH, weight = 1.2f, isSpecial = true),
            KeyDef("🌐", type = KeyType.LANGUAGE_SWITCH, weight = 1.0f, isSpecial = true),
            KeyDef("⌨ Dev", type = KeyType.PROGRAMMER_BAR, weight = 1.1f, isSpecial = true),
            KeyDef(" ", displayLabel = "مسافة", type = KeyType.SPACE, weight = 3.6f),
            KeyDef("،", weight = 0.9f),
            KeyDef("↵", type = KeyType.ENTER, weight = 1.3f, isSpecial = true)
        )

        return listOf(r1, r2, r3, r4)
    }

    fun getProgrammerSymbolsLayout(): List<List<KeyDef>> {
        val r1 = listOf(
            KeyDef("`"), KeyDef("~"), KeyDef("!"), KeyDef("@"), KeyDef("#"),
            KeyDef("$"), KeyDef("%"), KeyDef("^"), KeyDef("&"), KeyDef("*"),
            KeyDef("-"), KeyDef("+")
        )

        val r2 = listOf(
            KeyDef("{"), KeyDef("}"), KeyDef("["), KeyDef("]"), KeyDef("("),
            KeyDef(")"), KeyDef("<"), KeyDef(">"), KeyDef("="), KeyDef("_"),
            KeyDef("\\"), KeyDef("|")
        )

        val r3 = listOf(
            KeyDef("123", type = KeyType.MODE_SWITCH, weight = 1.2f, isSpecial = true),
            KeyDef("/"), KeyDef("?"), KeyDef(":"), KeyDef(";"), KeyDef("\""),
            KeyDef("'"), KeyDef(","), KeyDef("."), KeyDef("`"),
            KeyDef("⌫", type = KeyType.DELETE, weight = 1.3f, isSpecial = true)
        )

        val r4 = listOf(
            KeyDef("ABC", type = KeyType.MODE_SWITCH, weight = 1.2f, isSpecial = true),
            KeyDef("🌐", type = KeyType.LANGUAGE_SWITCH, weight = 1.0f, isSpecial = true),
            KeyDef("0x", weight = 1.0f),
            KeyDef(" ", displayLabel = "Space", type = KeyType.SPACE, weight = 3.6f),
            KeyDef(";", weight = 0.9f),
            KeyDef("↵", type = KeyType.ENTER, weight = 1.3f, isSpecial = true)
        )

        return listOf(r1, r2, r3, r4)
    }

    fun getNumberHexLayout(): List<List<KeyDef>> {
        val r1 = listOf(
            KeyDef("1"), KeyDef("2"), KeyDef("3"), KeyDef("+"), KeyDef("A", isAccent = true),
            KeyDef("B", isAccent = true), KeyDef("(")
        )
        val r2 = listOf(
            KeyDef("4"), KeyDef("5"), KeyDef("6"), KeyDef("-"), KeyDef("C", isAccent = true),
            KeyDef("D", isAccent = true), KeyDef(")")
        )
        val r3 = listOf(
            KeyDef("7"), KeyDef("8"), KeyDef("9"), KeyDef("*"), KeyDef("E", isAccent = true),
            KeyDef("F", isAccent = true), KeyDef("⌫", type = KeyType.DELETE, isSpecial = true)
        )
        val r4 = listOf(
            KeyDef("ABC", type = KeyType.MODE_SWITCH, weight = 1.2f, isSpecial = true),
            KeyDef("0"), KeyDef("."), KeyDef("/"), KeyDef("="), KeyDef("0x"),
            KeyDef("↵", type = KeyType.ENTER, weight = 1.2f, isSpecial = true)
        )

        return listOf(r1, r2, r3, r4)
    }
}
