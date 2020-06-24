@file:JsModule("@material-ui/core/ButtonGroup")
@file:JsNonModule

package materialUi.core

import react.*

@JsName("default")
external val buttonGroup : RClass<ButtonGroupProps>

external interface ButtonGroupProps: RProps {
    var className: String
    var variant: String
    var component: String
}