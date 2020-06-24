@file:JsModule("@material-ui/core/FormControl")
@file:JsNonModule

package materialUi.core

import react.RClass
import react.RProps

@JsName("default")
external val formControl: RClass<FormControlProps>

external interface FormControlProps: RProps {
    var className: String
    var color: String
    var variant: String
}