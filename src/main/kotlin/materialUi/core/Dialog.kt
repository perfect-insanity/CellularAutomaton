@file:JsModule("@material-ui/core/Dialog")
@file:JsNonModule

package materialUi.core

import react.RClass
import react.RProps

@JsName("default")
external val dialog: RClass<DialogProps>

external interface DialogProps: RProps {
    var className: String
    var open: Boolean
    var onClose: () -> Unit
}