import cellularAutomaton.CellularAutomaton
import cellularAutomaton.ConwayGame
import cellularAutomaton.Elementary
import kotlinx.css.*
import materialUi.core.*
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.MouseEvent
import react.*
import react.dom.div
import react.dom.render
import styled.css
import styled.styledDiv
import styled.styledSpan
import kotlin.browser.document
import kotlin.browser.window
import kotlin.math.ceil
import kotlin.text.Typography.nbsp

const val MIN_DELAY = 20
const val DEFAULT_DELAY = 100
const val MAX_DELAY = 1000
const val DELAY_SLIDER_STEP = 10
const val SIDE = 8.0
const val FIELD_WIDTH = 160
const val FIELD_HEIGHT = 80
val colorByState = mapOf(true to "#ffffff", false to "#000000")

var delay = DEFAULT_DELAY
var currentType = Type.CONWAY
var automaton: CellularAutomaton = currentType.instance()
val canvas = document.getElementById("mainCanvas") as HTMLCanvasElement
val context = canvas.getContext("2d") as CanvasRenderingContext2D

enum class Type {
    CONWAY {
        override val automatonName = "Игра Жизнь"
        override val possibleValues = 0..8
        override var conditions: Map<Boolean, MutableList<Any>> =
            mapOf(true to mutableListOf<Any>(3), false to mutableListOf<Any>(0, 1, 4, 5, 6, 7, 8))

        override fun instance() = ConwayGame(
            FIELD_WIDTH, FIELD_HEIGHT,
            { it in conditions[false]!! },
            { it in conditions[true]!! }
        )

    },
    ELEMENTARY {
        override val automatonName = "Элементарный автомат"
        override val possibleValues = 0..7
        override var conditions: Map<Boolean, MutableList<Any>> =
            mapOf(true to mutableListOf<Any>(0b110, 0b100, 0b011, 0b001))

        override fun instance() = Elementary(FIELD_WIDTH, FIELD_HEIGHT) { it in conditions[true]!! }

    };

    abstract val automatonName: String
    abstract val possibleValues: Iterable<Any>
    abstract var conditions: Map<Boolean, MutableList<Any>>

    abstract fun instance(): CellularAutomaton
}

fun main() {
    canvas.apply {
        width = ceil((FIELD_WIDTH * SIDE)).toInt()
        height = ceil((FIELD_HEIGHT * SIDE)).toInt()
    }

    context.paint(automaton)
    canvas.addEventListener("click", { event ->
        if (event is MouseEvent) {
            context.apply {
                val (x, y) = getCellByCoord(event.offsetX, event.offsetY, SIDE)
                val cell = automaton.tor[x, y]
                if (cell.isAlive)
                    automaton.tor.killCell(cell)
                else
                    automaton.tor.animateCell(cell)

                fillStyle = colorByState[cell.isAlive]
                fillRect(x * SIDE, y * SIDE, SIDE, SIDE)
            }
        }
    })

    var timeout: Int? = null

    val mainDiv = functionalComponent<RProps> {
        val (startButtonText, setStartButtonText) = useState("Старт")
        val (isOpened, setOpened) = useState(false)

        val repeat = { action: () -> Unit ->
            setStartButtonText("Стоп")
            timeout = window.setTimeout(action, delay)
        }
        val finish = {
            if (timeout != null) {
                setStartButtonText("Старт")
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
                    +startButtonText
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
                button {
                    attrs {
                        onClick = { setOpened(true) }
                    }
                    +"Поменять правила"
                }
            }
            child(sliderComponent())
        }
        child(dialogComponent(isOpened, setOpened))
    }

    render(document.getElementById("main")) {
        child(mainDiv)
    }
}

fun getCellByCoord(x: Double, y: Double, side: Double) = (x / side).toInt() to (y / side).toInt()

fun dialogComponent(
    isOpened: Boolean,
    setOpened: RSetState<Boolean>
) = functionalComponent<RProps> {
    val (selectedType, selectType) = useState(currentType)
    dialog {
        val conditions = selectedType.conditions.mapValues { it.value.toMutableList() }
        attrs {
            open = isOpened
            onClose = {
                setOpened(false)
            }
        }
        dialogTitle {
            +"Выберите новые правила"
        }
        dialogContent {
            child(dialogContentComponent(selectedType, selectType, conditions))
        }
        dialogActions {
            button {
                attrs {
                    color = "primary"
                    onClick = {
                        setOpened(false)
                    }
                }
                +"Отмена"
            }
            button {
                attrs {
                    color = "primary"
                    onClick = {
                        if (selectedType != currentType) {
                            currentType = selectedType
                            automaton = currentType.instance()
                        }
                        currentType.conditions = conditions.mapValues { it.value.toMutableList() }

                        setOpened(false)
                    }
                }
                +"ОК"
            }
        }
    }
}

fun dialogContentComponent(
    selectedType: Type,
    selectType: RSetState<Type>,
    conditions: Map<Boolean, MutableList<Any>>
) = functionalComponent<RProps> {
    formControlLabel {
        attrs {
            label = "Тип автомата:"
            labelPlacement = "start"
            control = styledSpan {
                css {
                    marginLeft = 16.px
                }
                formControl {
                    attrs {
                        color = "primary"
                        variant = "standard"
                    }
                    select {
                        attrs {
                            value = selectedType.automatonName
                            autoWidth = true
                        }
                        for (type in Type.values()) {
                            menuItem {
                                attrs {
                                    value = type.automatonName
                                    onClick = { selectType(type) }
                                }
                                +attrs.value
                            }
                        }
                    }
                }
            }
        }
    }
    when (selectedType) {
        Type.CONWAY -> {
            for ((k, v) in conditions) {
                div {
                    formControlLabel {
                        attrs {
                            label = if (k)
                                "0${nbsp}→${nbsp}1:"
                            else
                                "1${nbsp}→${nbsp}0:"
                            labelPlacement = "start"
                            control = styledSpan {
                                css {
                                    marginLeft = 16.px
                                }
                                selectedType.possibleValues.forEach { condition: Any ->
                                    child(checkboxComponent(v, condition))
                                }
                            }
                        }
                    }
                }
            }
        }
        Type.ELEMENTARY -> {
            div {
                formControlLabel {
                    attrs {
                        label = "0${nbsp}/${nbsp}1${nbsp}→${nbsp}1:"
                        labelPlacement = "start"
                        control = styledSpan {
                            css {
                                marginLeft = 16.px
                            }
                            selectedType.possibleValues.forEach { condition: Any ->
                                child(
                                    checkboxComponent(
                                        conditions[true]!!,
                                        condition
                                    ) { (it as Int).toBinaryString(3) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun checkboxComponent(
    values: MutableList<Any>,
    value: Any,
    toString: (Any) -> String = Any::toString
) = functionalComponent<RProps> {
    styledSpan {
        css {
            marginLeft = -(32.px)
        }
        formControlLabel {
            attrs {
                label = toString(value)
                control = checkbox {
                    val (isChecked, setChecked) = useState(value in values)
                    attrs {
                        color = "primary"
                        labelPlacement = "bottom"
                        checked = isChecked
                        onChange = {
                            setChecked(!isChecked)
                            if (!isChecked)
                                values += value
                            else
                                values -= value
                        }
                    }
                }
            }
        }
    }
}

fun sliderComponent() = functionalComponent<RProps> {
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

fun CanvasRenderingContext2D.paint(automaton: CellularAutomaton) {
    for (i in 0 until FIELD_WIDTH)
        for (j in 0 until FIELD_HEIGHT) {
            fillStyle = colorByState[automaton.tor[i, j].isAlive]
            fillRect(i * SIDE, j * SIDE, SIDE, SIDE)
        }
}

fun Int.toBinaryString(length: Int): String {
    val res = toString(2)
    val zeros = with(length - res.length) { if (this > 0) "0".repeat(this) else "" }
    return zeros + res
}