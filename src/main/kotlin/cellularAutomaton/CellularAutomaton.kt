package cellularAutomaton

abstract class CellularAutomaton(width: Int, height: Int) {
    val tor = Tor(width, height)
    private var generation = 1

    data class NextGeneration(val mustDie: MutableSet<Cell>, val mustBorn: MutableSet<Cell>)

    abstract fun nextGeneration(): NextGeneration

    fun restructure(): Boolean {
        val (mustDie, mustBorn) = nextGeneration()
        val newGenLiveCells = HashSet(tor.liveCells)

        for (cell in mustBorn) {
            cell.isAlive = true
            newGenLiveCells.add(cell)
        }
        for (cell in mustDie) {
            cell.isAlive = false
            newGenLiveCells.remove(cell)
        }

        if (newGenLiveCells == tor.liveCells)
            return false
        else {
            tor.liveCells = newGenLiveCells
            generation++
        }

        return true
    }
}
