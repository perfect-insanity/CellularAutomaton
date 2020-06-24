@file:JsModule("@material-ui/core/Button")
@file:JsNonModule

package materialUi.core

import org.w3c.dom.events.Event
import react.*

@JsName("default")
external val button: RClass<ButtonProps>

external interface ButtonProps: RProps {
    var className: String
    var onClick: (Event?) -> Unit
    var color: String
}

