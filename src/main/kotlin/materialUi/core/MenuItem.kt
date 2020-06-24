@file:JsModule("@material-ui/core/MenuItem")
@file:JsNonModule

package materialUi.core

import org.w3c.dom.events.Event
import react.RClass
import react.RProps

@JsName("default")
external val menuItem: RClass<MenuItemProps>

external interface MenuItemProps: RProps {
    var className: String
    var value: String
    var onClick: (Event?) -> Unit
}