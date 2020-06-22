import automaton.CellularAutomaton
import automaton.ConwayGame
import automaton.Elementary
import jsModules.button
import jsModules.buttonGroup
import jsModules.slider

import kotlinx.css.*
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.MouseEvent
import react.dom.render
import styled.css
import styled.styledDiv
import styled.styledSpan
import kotlin.browser.document
import kotlin.browser.window
import kotlin.math.ceil

val canvas = document.getElementById("mainCanvas") as HTMLCanvasElement
val context = canvas.getContext("2d") as CanvasRenderingContext2D

const val MIN_DELAY = 20
const val DEFAULT_DELAY = 100
const val MAX_DELAY = 1000
const val DELAY_SLIDER_STEP = 10

val side = 8.0
val fieldWidth = 160
val fieldHeight = 80
val colorByState = mapOf(true to "#ffffff", false to "#000000")
var delay = DEFAULT_DELAY

fun main() {
    canvas.apply {
        width = ceil((fieldWidth * side)).toInt()
        height = ceil((fieldHeight * side)).toInt()
    }

    val automaton = ConwayGame(fieldWidth, fieldHeight, { it < 2 || it > 3 }, { it == 3 })
    val automaton2 = Elementary(fieldWidth, fieldHeight,
        { it in listOf(listOf(1, 1, 1), listOf(1, 0, 1), listOf(0, 1, 0), listOf(0, 0, 0)) },
        { it in listOf(listOf(1, 1, 0), listOf(1, 0, 0), listOf(0, 1, 1), listOf(0, 0, 1)) }
    )

    context.paint(automaton)
    canvas.addEventListener("click", { event ->
        if (event is MouseEvent) {
            context.apply {
                val (x, y) = getCellByCoord(event.offsetX, event.offsetY, side)
                val cell = automaton.tor[x, y]
                if (cell.isAlive)
                    automaton.tor.killCell(cell)
                else
                    automaton.tor.animateCell(cell)

                fillStyle = colorByState[cell.isAlive]
                fillRect(x * side, y * side, side, side)
            }
        }
    })

    var timeout: Int? = null
    val buttonText = mutableMapOf("name" to "Старт")

    val repeat = { action: () -> Unit ->
        buttonText["name"] = "Стоп"
        timeout = window.setTimeout(action, delay)
    }
    val finish = {
        buttonText["name"] = "Старт"
        if (timeout != null) {
            window.clearTimeout(timeout!!)
            timeout = null
        }
    }

    fun repaint() {
        val next = automaton.restructure()
        context.paint(automaton)

        if (next)
            repeat(::repaint)
        else
            finish()
    }

    render(document.getElementById("main")) {
        styledDiv {
            css {
                marginBottom = 8.px
            }
            buttonGroup {
                attrs {
                    variant = "text"
                    component = "span"
                }
                button {
                    attrs {
                        onClick = {
                            if (timeout == null)
                                repaint()
                            else
                                finish()
                        }
                    }
                    +"${buttonText["name"]}"
                }
                button {
                    attrs {
                        onClick = {
                            finish()
                            automaton.tor.killAll()
                            context.paint(automaton)
                        }
                    }
                    +"Очистить"
                }
            }
            styledSpan {
                css {
                    display = Display.inlineBlock
                    width = LinearDimension("25%")
                    marginLeft = 16.px
                    verticalAlign = VerticalAlign.middle
                }
                slider {
                    attrs {
                        defaultValue = DEFAULT_DELAY
                        step = DELAY_SLIDER_STEP
                        min = MIN_DELAY
                        max = MAX_DELAY
                        valueLabelDisplay = "off"
                        onChange = { _, value ->
                            delay = value
                        }
                    }
                }
            }
        }
    }
}

fun getCellByCoord(x: Double, y: Double, side: Double) = (x / side).toInt() to (y / side).toInt()

fun CanvasRenderingContext2D.paint(automaton: CellularAutomaton) {
    for (i in 0 until fieldWidth)
        for (j in 0 until fieldHeight) {
            fillStyle = colorByState[automaton.tor[i, j].isAlive]
            fillRect(i * side, j * side, side, side)
        }
}