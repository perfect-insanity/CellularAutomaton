@file:JsModule("@material-ui/core/InputLabel")
@file:JsNonModule

package materialUi.core

import react.RClass
import react.RProps

@JsName("default")
external val inputLabel: RClass<InputLabelProps>

external interface InputLabelProps: RProps {
    var className: String
}