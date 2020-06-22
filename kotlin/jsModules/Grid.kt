@file:JsModule("@material-ui/core/jsModules.getGrid")
@file:JsNonModule

package jsModules

import react.RClass
import react.RProps

@JsName("default")
external val grid : RClass<SliderProps>

external interface GridProps: RProps {
    var className: String

}