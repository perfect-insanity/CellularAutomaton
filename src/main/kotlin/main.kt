import cellularAutomaton.*
import kotlinext.js.jsObject
import kotlinx.css.*
import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.serialization.json.*
import materialUi.core.*
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.FileReader
import org.w3c.files.get
import react.*
import react.dom.div
import react.dom.input
import react.dom.label
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
val colorByStateExport = mapOf(true to "#dcdcdc", false to "#696969")

val process = AutomatonProcess(Type.CONWAY)
val canvas = document.getElementById("mainCanvas") as HTMLCanvasElement
val context = canvas.getContext("2d") as CanvasRenderingContext2D
val json = Json(JsonConfiguration.Stable)

class AutomatonProcess(initType: Type) {
    var currentType = initType
        set(value) {
            field = value
            automaton = value.instance(automaton.tor)
        }
    var automaton: CellularAutomaton = currentType.instance()
        private set
    var generation = 1
        private set
    private var timeout: Int? = null
    var delay = DEFAULT_DELAY
    var onChangeAutomatonState: () -> Unit = {}
    var onRepeat: () -> Unit = {}
    var onFinish: () -> Unit = {}
    var mode = Mode.DEFAULT
    val export = mutableListOf<Cell>()

    private fun repeat() {
        val next = automaton.restructure()
        onChangeAutomatonState()

        if (next) {
            next(::repeat)
        }
        else
            finish()
    }

    private fun next(action: () -> Unit) {
        generation++
        onRepeat()
        timeout = window.setTimeout(action, delay)
    }

    private fun finish() {
        if (timeout != null) {
            onFinish()
            window.clearTimeout(timeout!!)
            timeout = null
        }
    }

    fun invState() {
        if (timeout == null)
            repeat()
        else
            finish()
    }

    fun clear() {
        finish()
        automaton.tor.killAll()
        onChangeAutomatonState()
        generation = 1
    }

    fun isStarted() = timeout != null
}

enum class Type {
    CONWAY {
        override val automatonName = "Игра Жизнь"
        override val possibleValues = 0..8
        override var conditions: CellularAutomaton.Conditions =
            ConwayGame.Conditions(zeroToOne = mutableListOf(3), oneToZero = mutableListOf(0, 1, 4, 5, 6, 7, 8))
        override var checkboxesLabelComponent = functionalComponent<CheckboxesLabelProps> { props ->
            val createLabel = { label: String, values: MutableList<Int> ->
                div {
                    formControlLabel {
                        attrs {
                            this.label = label
                            labelPlacement = "start"
                            control = styledSpan {
                                css {
                                    marginLeft = 16.px
                                }
                                possibleValues.forEach { value ->
                                    child(rulesCheckboxComponent(values, value, Any::toString))
                                }
                            }
                        }
                    }
                }
            }

            createLabel("0${nbsp}→${nbsp}1:", (props.conditions as ConwayGame.Conditions).zeroToOne)
            createLabel("1${nbsp}→${nbsp}0:", (props.conditions as ConwayGame.Conditions).oneToZero)
        }

        override fun instance(tor: Tor) = ConwayGame(
            FIELD_WIDTH, FIELD_HEIGHT,
            tor,
            conditions as ConwayGame.Conditions
        )

        override fun serialize() =
            json.toJson(ConwayGame.Conditions.serializer(), conditions as ConwayGame.Conditions)

        override fun deserialize(jsonElement: JsonElement) =
            json.fromJson(ConwayGame.Conditions.serializer(), jsonElement)
    },
    ELEMENTARY {
        override val automatonName = "Элементарный автомат"
        override val possibleValues = 0..7
        override var conditions: CellularAutomaton.Conditions =
            Elementary.Conditions(toOne = mutableListOf(0b110, 0b100, 0b011, 0b001))
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
                            possibleValues.forEach { value ->
                                child(
                                    rulesCheckboxComponent(
                                        (props.conditions as Elementary.Conditions).toOne,
                                        value
                                    ) {
                                        it.toBinaryString(3)
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
            tor,
            conditions as Elementary.Conditions
        )

        override fun serialize() =
            json.toJson(Elementary.Conditions.serializer(), conditions as Elementary.Conditions)

        override fun deserialize(jsonElement: JsonElement) =
            json.fromJson(Elementary.Conditions.serializer(), jsonElement)
    };

    abstract val automatonName: String
    abstract val possibleValues: Iterable<Any>
    abstract var conditions: CellularAutomaton.Conditions
    abstract var checkboxesLabelComponent: FunctionalComponent<CheckboxesLabelProps>

    abstract fun instance(tor: Tor = Tor(FIELD_WIDTH, FIELD_HEIGHT)): CellularAutomaton
    abstract fun serialize(): JsonElement
    abstract fun deserialize(jsonElement: JsonElement): CellularAutomaton.Conditions
}

enum class Mode {
    DEFAULT, IMPORT, EXPORT
}

fun main() {
    process.onChangeAutomatonState = {
        context.paint(process.automaton)
    }
    canvas.draw()
    render(document.getElementById("main")) {
        child(mainComponent)
    }

    onUploadTextFile { result ->
        val (type, conditions, cells) = fromJSON(result)
        for (cell in cells) {
            process.automaton.tor[cell.i, cell.j] = cell
        }
        context.paint(process.automaton)
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
        val startButtonTextVariants = mapOf(false to "Старт", true to "Стоп")
        val (isStarted, setStarted) = useState(process.isStarted())

        process.apply {
            onRepeat = {
                setStarted(true)
                props.setGenerationText(generation)
            }

            onFinish = {
                setStarted(false)
            }
        }

        attrs {
            variant = "text"
            component = "span"
        }
        button {
            attrs {
                onClick = {
                    process.invState()
                }
            }
            +startButtonTextVariants[isStarted]!!
        }
        button {
            attrs {
                onClick = {
                    process.clear()
                    props.setGenerationText(process.generation)
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

        val saveButtonTextVariants = mapOf(true to "Готово", false to "Сохранить")
        val (isSaving, setSaving) = useState(false)

        button {
            attrs {
                onClick = {
                    setSaving(!isSaving)
                    process.mode = if (!isSaving) {
                        Mode.EXPORT
                    } else {
                        downloadTextFile(toJSON().toString(), "file.txt")
                        process.export.clear()
                        context.paint(process.automaton)
                        Mode.DEFAULT
                    }
                }
            }
            +saveButtonTextVariants[isSaving]!!
        }
        input {
            attrs {
                id = "download-file"
                multiple = false
                accept = "text/plain"
                type = InputType.file
            }
        }
        label {
            attrs {
                htmlFor = "download-file"
            }

            button {
                attrs {
                    onClick = { process.mode = Mode.IMPORT }
                }
                +"Загрузить"
            }
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
                defaultValue = process.delay
                step = DELAY_SLIDER_STEP
                min = MIN_DELAY
                max = MAX_DELAY
                valueLabelDisplay = "off"
                onChange = { _, value ->
                    process.delay = value
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
    val (selectedType, selectType) = useState(process.currentType)
    dialog {
        val conditions = selectedType.conditions.copy()
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
                        if (selectedType != process.currentType) {
                            process.currentType = selectedType
                        }
                        selectedType.conditions = conditions.copy()
                        process.automaton.conditions = conditions.copy()

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
    var conditions: CellularAutomaton.Conditions
}

fun <T> rulesCheckboxComponent(
    values: MutableList<T>,
    value: T,
    getLabel: (T) -> String
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

fun HTMLCanvasElement.draw() {
    width = ceil((FIELD_WIDTH * SIDE)).toInt()
    height = ceil((FIELD_HEIGHT * SIDE)).toInt()

    context.paint(process.automaton)

    var isMouseDown = false
    var lastCellCoords: Pair<Int, Int>? = null

    val onMouseDownOrMove = {
        val cell = process.automaton.tor[lastCellCoords!!.first, lastCellCoords!!.second]
        when (process.mode) {
            Mode.DEFAULT -> context.paint(lastCellCoords!!)
            Mode.EXPORT -> {
                context.apply {
                    if (cell in process.export) {
                        process.export -= cell
                        fillStyle = colorByState[cell.isAlive]
                    }
                    else {
                        process.export += cell
                        fillStyle = colorByStateExport[cell.isAlive]
                    }
                    fillRect(lastCellCoords!!.first * SIDE, lastCellCoords!!.second * SIDE, SIDE, SIDE)
                }
            }
            Mode.IMPORT -> {
            }
        }
    }
    addEventListener("mousedown", { event ->
        isMouseDown = true
        if (event is MouseEvent) {
            lastCellCoords = getCellByCoords(event.offsetX, event.offsetY, SIDE)
            onMouseDownOrMove()
        }
    })
    addEventListener("mousemove", { event ->
        if (event is MouseEvent && isMouseDown) {
            val curCellCoords = getCellByCoords(event.offsetX, event.offsetY, SIDE)
            if (curCellCoords != lastCellCoords) {
                lastCellCoords = curCellCoords
                onMouseDownOrMove()
            }
        }
    })

    val onMouseUpOrLeave: (Event) -> Unit = {
        isMouseDown = false
        lastCellCoords = null
    }
    addEventListener("mouseup", onMouseUpOrLeave)
    addEventListener("mouseleave", onMouseUpOrLeave)
}

fun getCellByCoords(x: Double, y: Double, side: Double) = (x / side).toInt() to (y / side).toInt()

fun CanvasRenderingContext2D.paint(coords: Pair<Int, Int>) {
    val (x, y) = coords
    val cell = process.automaton.tor[x, y]
    if (cell.isAlive)
        process.automaton.tor.killCell(cell)
    else
        process.automaton.tor.animateCell(cell)

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

fun toJSON() = JsonObject(
    mapOf(
        "type" to JsonPrimitive(process.currentType.toString()),
        "conditions" to process.currentType.serialize(),
        "cells" to JsonArray(process.export.map {
            json.toJson(Cell.serializer(), it)
        })
    )
)

fun fromJSON(jsonString: String): Triple<Type, CellularAutomaton.Conditions, List<Cell>> {
    val parsed = json.parseJson(jsonString)
    val type = Type.valueOf(
        parsed.jsonObject["type"]?.primitive?.content
            ?: throw IllegalStateException("type not defined")
    )
    val conditions = process.currentType.deserialize(
        parsed.jsonObject["conditions"]
            ?: throw IllegalStateException("conditions not defined")
    )
    val cells = parsed.jsonObject["cells"]?.jsonArray?.content?.map { json.fromJson(Cell.serializer(), it) }
        ?: throw IllegalStateException("cells not defined")

    return Triple(type, conditions, cells)
}

fun downloadTextFile(text: String, fileName: String) =
    (document.createElement("a") as HTMLAnchorElement).apply {
        href = URL.createObjectURL(Blob(arrayOf(text)))
        download = fileName
        document.body!!.appendChild(this)
        click()
        document.body!!.removeChild(this)
    }

fun onUploadTextFile(onLoad: (String) -> Unit) =
    (document.getElementById("download-file") as HTMLInputElement).apply {
        onchange = {
            val fileReader = FileReader()
            files!![0]?.let { fileReader.readAsText(it) }
            fileReader.onload = {
                onLoad(fileReader.result.toString())
            }
            Unit.asDynamic()
        }
    }