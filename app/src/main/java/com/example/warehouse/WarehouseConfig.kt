package com.example.warehouse

object WarehouseConfig {

    data class LocationComponents(
        val chamber: Int,
        val rack: Int,
        val level: Int,
        val place: Int
    )

    // Regex to match "CHx-Ryy-Ez-Pww" format precisely
    private val regex = Regex("^CH([1-3])-R(0[1-9]|[1-2][0-9]|3[0-2])-E([1-4])-P(0[1-9]|1[0-1])$")

    fun parseLocationCode(code: String): LocationComponents? {
        val matchResult = regex.matchEntire(code) ?: return null
        val (chStr, rStr, eStr, pStr) = matchResult.destructured
        val ch = chStr.toInt()
        val r = rStr.toInt()
        val e = eStr.toInt()
        val p = pStr.toInt()

        // Validate rack ranges per chamber
        val maxRack = when (ch) {
            1 -> 26
            2 -> 26
            3 -> 32
            else -> return null
        }
        if (r < 1 || r > maxRack) return null

        // Validate place ranges per chamber and rack
        val maxPlace = getMaxPlace(ch, r) ?: return null
        if (p < 1 || p > maxPlace) return null

        return LocationComponents(ch, r, e, p)
    }

    fun isValidLocationCode(code: String): Boolean {
        return parseLocationCode(code) != null
    }

    fun getMaxRack(chamber: Int): Int {
        return when (chamber) {
            1 -> 26
            2 -> 26
            3 -> 32
            else -> 0
        }
    }

    fun getMaxPlace(chamber: Int, rack: Int): Int? {
        return when (chamber) {
            1 -> {
                if (rack in 1..13) 11
                else if (rack in 14..26) 4
                else null
            }
            2 -> {
                if (rack in 1..13) 9
                else if (rack in 14..26) 4
                else null
            }
            3 -> {
                if (rack in 1..17) 11
                else if (rack in 18..32) 5
                else null
            }
            else -> null
        }
    }

    fun formatLocationCode(chamber: Int, rack: Int, level: Int, place: Int): String {
        val rStr = rack.toString().padStart(2, '0')
        val pStr = place.toString().padStart(2, '0')
        return "CH$chamber-R$rStr-E$level-P$pStr"
    }
}
