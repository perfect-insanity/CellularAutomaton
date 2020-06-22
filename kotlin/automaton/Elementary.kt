package automaton

class Elementary(
        private val width: Int, private val height: Int,
        val deathCondition: (List<Int>) -> Boolean, val birthCondition: (List<Int>) -> Boolean
) : CellularAutomaton(width, height) {
    override fun nextGeneration(): NextGeneration {
        val mustDie = HashSet<Cell>()
        val mustBorn = HashSet<Cell>()
        for (i in 0 until width) {
            for (j in 0 until height) {
                val cell = tor[i, j]
                val neighbors = getNeighbors(cell)
                val seq = mutableListOf<Int>()
                for (neighbor in neighbors) {
                    seq += if (neighbor.isAlive) 1 else 0
                }
                if (cell.isAlive && deathCondition(seq))
                    mustDie.add(cell)
                else if (!cell.isAlive && birthCondition(seq))
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
