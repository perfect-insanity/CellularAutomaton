@file:JsModule("@material-ui/core/Grid")
@file:JsNonModule

package materialUi.core

import react.RClass
import react.RProps

@JsName("default")
external val grid : RClass<GridProps>

external interface GridProps: RProps {
    var className: String

}