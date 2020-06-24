@file:JsModule("@material-ui/core/FormControlLabel")
@file:JsNonModule

package materialUi.core

import react.RClass
import react.RProps
import react.ReactElement

@JsName("default")
external val formControlLabel : RClass<FormControlLabelProps>

external interface FormControlLabelProps: RProps {
    var className: String
    var labelPlacement: String
    var control: ReactElement
    var label: String
}