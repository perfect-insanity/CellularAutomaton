package cellularAutomaton

import kotlinx.serialization.*

@Serializable
data class Cell(
    val i: Int,
    val j: Int,
    var isAlive: Boolean = false
)
