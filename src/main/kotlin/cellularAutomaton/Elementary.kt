package cellularAutomaton

import kotlinx.serialization.Serializable

class Elementary(
    private val width: Int, private val height: Int,
    tor: Tor,
    override var conditions: CellularAutomaton.Conditions
) : CellularAutomaton(width, height, tor) {

    @Serializable
    class Conditions(
        val toOne: MutableList<Int>
    ) : CellularAutomaton.Conditions() {
        override fun copy() = Conditions(toOne.toMutableList())
    }

    override fun nextGeneration(): NextGeneration {
        val mustDie = HashSet<Cell>()
        val mustBorn = HashSet<Cell>()
        for (i in 0 until width) {
            for (j in 0 until height) {
                val cell = tor[i, j]
                val neighbors = getNeighbors(cell)
                var seq = 0
                for (neighbor in neighbors) {
                    seq *= 2
                    seq += if (neighbor.isAlive) 1 else 0
                }
                if (cell.isAlive && seq !in (conditions as Conditions).toOne)
                    mustDie.add(cell)
                else if (!cell.isAlive && seq in (conditions as Conditions).toOne)
                    mustBorn.add(cell)
            }
        }
        return NextGeneration(mustDie, mustBorn)
    }

    private fun getNeighbors(cell: Cell): List<Cell> {
        val res = ArrayList<Cell>()
        for (i in -1..1) {
            res.add(tor[cell.i + i, cell.j])
        }
        return res
    }
}
