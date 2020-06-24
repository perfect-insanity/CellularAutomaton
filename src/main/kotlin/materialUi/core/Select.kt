@file:JsModule("@material-ui/core/Select")
@file:JsNonModule

package materialUi.core

import react.RClass
import react.RProps

@JsName("default")
external val select: RClass<SelectProps>

external interface SelectProps: RProps {
    var className: String
    var value: String
    var autoWidth: Boolean
}