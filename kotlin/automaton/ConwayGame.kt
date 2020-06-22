package automaton

class ConwayGame(
        width: Int, height: Int,
        val deathCondition: (Int) -> Boolean, val birthCondition: (Int) -> Boolean
) : CellularAutomaton(width, height) {

    override fun nextGeneration(): NextGeneration {
        val mustDie = mutableSetOf<Cell>()
        val mustBorn = mutableSetOf<Cell>()

        for (row in tor.cells) {
            for (cell in row) {
                val aliveNeighbors = getNeighbors(cell).count { it != null && it.isAlive }

                if (cell.isAlive && deathCondition(aliveNeighbors)) {
                    mustDie.add(cell)
                }
                if (!cell.isAlive && birthCondition(aliveNeighbors)) {
                    mustBorn.add(cell)
                }
            }
        }

        return NextGeneration(mustDie, mustBorn)
    }

    private fun getNeighbors(cell: Cell): Set<Cell?> {
        val neighbors: MutableSet<Cell?> = mutableSetOf()
        for (i in -1..1) {
            for (j in -1..1) {
                if (i != 0 || j != 0)
                    neighbors.add(tor[cell.i + i, cell.j + j])
            }
        }

        return neighbors
    }
}
