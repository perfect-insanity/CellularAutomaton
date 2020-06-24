@file:JsModule("@material-ui/core/Checkbox")
@file:JsNonModule

package materialUi.core

import org.w3c.dom.events.Event
import react.*

@JsName("default")
external val checkbox: RClass<CheckboxProps>

external interface CheckboxProps: RProps {
    var className: String
    var name: String
    var color: String
    var checked: Boolean
    var onChange: (Event?) -> Unit
}