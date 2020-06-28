import cellularAutomaton.CellularAutomaton
import cellularAutomaton.ConwayGame
import cellularAutomaton.Elementary
import cellularAutomaton.Tor
import kotlinext.js.jsObject
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

var timeout: Int? = null
var delay = DEFAULT_DELAY
var currentType = Type.CONWAY
var automaton: CellularAutomaton = currentType.instance()
var generation = 1
val canvas = document.getElementById("mainCanvas") as HTMLCanvasElement
val context = canvas.getContext("2d") as CanvasRenderingContext2D

enum class Type {
    CONWAY {
        override val automatonName = "Игра Жизнь"
        override val possibleValues = 0..8
        override var conditions: Map<Boolean, MutableList<Any>> =
            mapOf(true to mutableListOf<Any>(3), false to mutableListOf<Any>(0, 1, 4, 5, 6, 7, 8))
        override var checkboxesLabelComponent = functionalComponent<CheckboxesLabelProps> { props ->
            for ((k, v) in props.conditions) {
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
                                possibleValues.forEach { value: Any ->
                                    child(rulesCheckboxComponent(v, value, Any::toString))
                                }
                            }
                        }
                    }
                }
            }
        }

        override fun instance(tor: Tor) = ConwayGame(
            FIELD_WIDTH, FIELD_HEIGHT,
            tor,
            { it in conditions[false]!! }, { it in conditions[true]!! }
        )
    },
    ELEMENTARY {
        override val automatonName = "Элементарный автомат"
        override val possibleValues = 0..7
        override var conditions: Map<Boolean, MutableList<Any>> =
            mapOf(true to mutableListOf<Any>(0b110, 0b100, 0b011, 0b001))
        override var checkboxesLabelComponent = functionalComponent<CheckboxesLabelProps> { props ->
            div {
                formControlLabel {
                    attrs {
                        label = "0${nbsp}/${nbsp}1${nbsp}→${nbsp}1:"
                        labelPlacement = "start"
                        control = styledSpan {
                            css {
                                marginLeft = 16.px
                            }
                            possibleValues.forEach { value: Any ->
                                child(
                                    rulesCheckboxComponent(props.conditions[true]!!, value) {
                                        (it as Int).toBinaryString(3)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        override fun instance(tor: Tor) = Elementary(
            FIELD_WIDTH, FIELD_HEIGHT,
            tor
        ) { it in conditions[true]!! }
    };

    abstract val automatonName: String
    abstract val possibleValues: Iterable<Any>
    abstract var conditions: Map<Boolean, MutableList<Any>>
    abstract var checkboxesLabelComponent: FunctionalComponent<CheckboxesLabelProps>

    abstract fun instance(tor: Tor = Tor(FIELD_WIDTH, FIELD_HEIGHT)): CellularAutomaton
}

fun main() {
    canvas.apply {
        width = ceil((FIELD_WIDTH * SIDE)).toInt()
        height = ceil((FIELD_HEIGHT * SIDE)).toInt()
    }

    context.paint(automaton)
    canvas.apply {
        var isMouseDown = false
        var lastCellCoords: Pair<Int, Int>? = null

        addEventListener("mousedown", { event ->
            isMouseDown = true
            if (event is MouseEvent) {
                lastCellCoords = getCellByCoords(event.offsetX, event.offsetY, SIDE)
                context.paint(lastCellCoords!!)
            }
        })
        addEventListener("mouseup", {
            isMouseDown = false
            lastCellCoords = null
        })
        addEventListener("mousemove", { event ->
            if (event is MouseEvent && isMouseDown) {
                val curCellCoords = getCellByCoords(event.offsetX, event.offsetY, SIDE)
                if (curCellCoords != lastCellCoords) {
                    lastCellCoords = curCellCoords
                    context.paint(curCellCoords)
                }
            }
        })
    }

    render(document.getElementById("main")) {
        child(mainComponent)
    }
}

val mainComponent = functionalComponent<RProps> {
    val (isDialogOpened, openDialog) = useState(false)

    child(menuComponent, jsObject {
        this.openDialog = openDialog
    })
    child(rulesDialogComponent, jsObject {
        this.isDialogOpened = isDialogOpened
        this.openDialog = openDialog
    })
}

interface MenuProps : RProps {
    var openDialog: RSetState<Boolean>
}

val menuComponent = functionalComponent<MenuProps> { props ->
    styledDiv {
        val (generationText, setGenerationText) = useState(1)
        css {
            marginBottom = 8.px
        }
        child(menuButtonGroupComponent, jsObject {
            this.openDialog = props.openDialog
            this.setGenerationText = setGenerationText
        })
        child(delaySliderComponent)
        child(counterComponent, jsObject {
            this.generationText = generationText
        })
    }
}

interface MenuButtonGroupProps : RProps {
    var openDialog: RSetState<Boolean>
    var setGenerationText: RSetState<Int>
}

val menuButtonGroupComponent = functionalComponent<MenuButtonGroupProps> { props ->
    buttonGroup {
        val buttonTextVariants = mapOf(true to "Старт", false to "Стоп")
        val (startButtonText, setStartButtonText) = useState(buttonTextVariants[timeout == null]!!)
        val repeat = { action: () -> Unit ->
            setStartButtonText(buttonTextVariants[false]!!)
            props.setGenerationText(++generation)
            timeout = window.setTimeout(action, delay)
        }
        val finish = {
            if (timeout != null) {
                setStartButtonText(buttonTextVariants[true]!!)
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
                    generation = 1
                    props.setGenerationText(generation)
                }
            }
            +"Очистить"
        }
        button {
            attrs {
                onClick = { props.openDialog(true) }
            }
            +"Поменять правила"
        }
    }
}

val delaySliderComponent = functionalComponent<RProps> {
    styledSpan {
        css {
            display = Display.inlineBlock
            width = LinearDimension("25%")
            marginLeft = 16.px
            verticalAlign = VerticalAlign.middle
        }
        slider {
            attrs {
                defaultValue = delay
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

interface CounterProps : RProps {
    var generationText: Int
}

val counterComponent = functionalComponent<CounterProps> { props ->
    styledSpan {
        css {
            marginLeft = 16.px
        }
        +props.generationText.toString()
    }
}

interface RulesDialogProps : RProps {
    var isDialogOpened: Boolean
    var openDialog: RSetState<Boolean>
}

val rulesDialogComponent = functionalComponent<RulesDialogProps> { props ->
    val (selectedType, selectType) = useState(currentType)
    dialog {
        val conditions = selectedType.conditions.mapValues { it.value.toMutableList() }
        attrs {
            open = props.isDialogOpened
            onClose = { props.openDialog(false) }
        }
        dialogTitle {
            +"Выберите новые правила"
        }
        dialogContent {
            child(typeSelectComponent, jsObject {
                this.selectedType = selectedType
                this.selectType = selectType
            })
            child(selectedType.checkboxesLabelComponent, jsObject {
                this.conditions = conditions
            })
        }
        dialogActions {
            button {
                attrs {
                    color = "primary"
                    onClick = {
                        props.openDialog(false)
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
                            automaton = currentType.instance(automaton.tor)
                            context.paint(automaton)
                        }
                        currentType.conditions = conditions.mapValues { it.value.toMutableList() }

                        props.openDialog(false)
                    }
                }
                +"ОК"
            }
        }
    }
}

interface TypeSelectProps : RProps {
    var selectedType: Type
    var selectType: RSetState<Type>
}

val typeSelectComponent = functionalComponent<TypeSelectProps> { props ->
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
                            value = props.selectedType.automatonName
                            autoWidth = true
                        }
                        for (type in Type.values()) {
                            menuItem {
                                attrs {
                                    value = type.automatonName
                                    onClick = { props.selectType(type) }
                                }
                                +attrs.value
                            }
                        }
                    }
                }
            }
        }
    }
}

interface CheckboxesLabelProps : RProps {
    var conditions: Map<Boolean, MutableList<Any>>
}

fun rulesCheckboxComponent(
    values: MutableList<Any>,
    value: Any,
    getLabel: (Any) -> String
) = functionalComponent<RProps> {
    styledSpan {
        css {
            marginLeft = -(32.px)
        }
        formControlLabel {
            attrs {
                label = getLabel(value)
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

fun getCellByCoords(x: Double, y: Double, side: Double) = (x / side).toInt() to (y / side).toInt()

fun CanvasRenderingContext2D.paint(coords: Pair<Int, Int>) {
    val (x, y) = coords
    val cell = automaton.tor[x, y]
    if (cell.isAlive)
        automaton.tor.killCell(cell)
    else
        automaton.tor.animateCell(cell)

    fillStyle = colorByState[cell.isAlive]
    fillRect(x * SIDE, y * SIDE, SIDE, SIDE)
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