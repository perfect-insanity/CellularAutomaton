package cellularAutomaton

class Tor(width: Int, height: Int) {
    val cells: MutableList<MutableList<Cell>> = mutableListOf()
    var liveCells: MutableSet<Cell> = mutableSetOf()

    init {
        for (i in 0 until width) {
            val col = mutableListOf<Cell>()
            for (j in 0 until height) {
                col += Cell(i, j)
            }
            cells.add(col)
        }
    }

    fun animateCell(cell: Cell) = liveCells.add(cell).also { cell.isAlive = true }

    fun killCell(cell: Cell) = liveCells.remove(cell).also { cell.isAlive = false }

    private fun convert(_i: Int, _j: Int): Pair<Int, Int> {
        var i = _i
        var j = _j

        while (i >= cells.size) {
            i -= cells.size
        }
        while (i < 0) {
            i += cells.size
        }
        while (j >= cells[i].size) {
            j -= cells[i].size
        }
        while (j < 0) {
            j += cells[i].size
        }
        return i to j
    }
//        Pair(
//                when {
//                    i >= cells.size -> i % cells.size
//                    i < 0 -> i % cells.size + cells.size
//                    else -> i
//                },
//                when {
//                    j >= cells[i].size -> j % cells[i].size
//                    j < 0 -> j % cells[i].size + cells[i].size
//                    else -> j
//                }
//        )

    operator fun get(i: Int, j: Int): Cell {
        val converted = convert(i, j)
        return cells[converted.first][converted.second]
    }

    fun killAll() {
        liveCells.forEach { it.isAlive = false }
        liveCells.clear()
    }
}
