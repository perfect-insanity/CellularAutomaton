package cellularAutomaton

import kotlinx.serialization.Serializable

class ConwayGame(
    width: Int, height: Int,
    tor: Tor,
    override var conditions: CellularAutomaton.Conditions
) : CellularAutomaton(width, height, tor) {

    @Serializable
    class Conditions(
        var zeroToOne: MutableList<Int>,
        var oneToZero: MutableList<Int>
    ) : CellularAutomaton.Conditions() {
        override fun copy() = Conditions(zeroToOne.toMutableList(), oneToZero.toMutableList())
    }

    override fun nextGeneration(): NextGeneration {
        val mustDie = mutableSetOf<Cell>()
        val mustBorn = mutableSetOf<Cell>()

        for (row in tor.cells) {
            for (cell in row) {
                val aliveNeighbors = getNeighbors(cell).count { it != null && it.isAlive }

                if (cell.isAlive && aliveNeighbors in (conditions as Conditions).oneToZero) {
                    mustDie.add(cell)
                }
                if (!cell.isAlive && aliveNeighbors in (conditions as Conditions).zeroToOne) {
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
