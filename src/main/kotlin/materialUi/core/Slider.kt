@file:JsModule("@material-ui/core/Slider")
@file:JsNonModule

package materialUi.core

import org.w3c.dom.events.Event
import react.*

@JsName("default")
external val slider: RClass<SliderProps>

external interface SliderProps: RProps {
    var className: String
    var defaultValue: Int
    var step: Int
    var min: Int
    var max: Int
    var valueLabelDisplay: String
    var onChange: (Event?, Int) -> Unit
}
