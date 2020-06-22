package automaton

data class Cell(
        val i: Int, val j: Int
) {
    var isAlive: Boolean = false

    override fun toString(): String = "$i, $j"
}
