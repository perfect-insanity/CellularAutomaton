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

    private fun convert(i: Int, j: Int): Pair<Int, Int> {
        var remI = i % cells.size
        if (remI < 0)
            remI += cells.size

        var remJ = j % cells[remI].size
        if (remJ < 0)
            remJ += cells[remI].size

        return remI to remJ
    }

    operator fun get(i: Int, j: Int): Cell {
        val converted = convert(i, j)
        return cells[converted.first][converted.second]
    }

    operator fun set(i: Int, j: Int, value: Cell) {
        if (value.isAlive)
            liveCells.add(value)
        else
            liveCells.remove(value)
        cells[i][j] = value
    }

    fun killAll() {
        liveCells.forEach { it.isAlive = false }
        liveCells.clear()
    }
}
