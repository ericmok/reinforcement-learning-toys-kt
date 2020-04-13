import kotlinx.html.CommonAttributeGroupFacade
import kotlinx.html.HTMLTag
import kotlinx.html.TagConsumer
import kotlinx.html.attributesMapOf
import react.RBuilder
import react.ReactElement
import react.dom.RDOMBuilder
import react.dom.tag
import styled.StyledDOMBuilder
import styled.styledTag

open class RECT(initialAttributes : Map<String, String>, override val consumer : TagConsumer<*>) : HTMLTag("rect", consumer, initialAttributes, "http://www.w3.org/2000/svg", false, false),
    CommonAttributeGroupFacade {
}
inline fun RBuilder.rect(classes: String? = null, block: RDOMBuilder<RECT>.() -> Unit): ReactElement = tag(block) { RECT(
    attributesMapOf("class", classes), it) }

open class CIRCLE(initialAttributes : Map<String, String>, override val consumer : TagConsumer<*>) : HTMLTag("circle", consumer, initialAttributes, "http://www.w3.org/2000/svg", false, false),
    CommonAttributeGroupFacade {
}
inline fun RBuilder.circle(classes: String? = null, block: RDOMBuilder<CIRCLE>.() -> Unit): ReactElement = tag(block) { CIRCLE(
    attributesMapOf("class", classes), it) }

open class TEXT(initialAttributes : Map<String, String>, override val consumer : TagConsumer<*>) : HTMLTag("text", consumer, initialAttributes, "http://www.w3.org/2000/svg", false, false),
    CommonAttributeGroupFacade {}
inline fun RBuilder.text(classes: String? = null, block: RDOMBuilder<TEXT>.() -> Unit): ReactElement = tag(block) { TEXT(
    attributesMapOf("class", classes), it) }
inline fun RBuilder.styledText(block: StyledDOMBuilder<TEXT>.() -> Unit) = styledTag(block) { TEXT(kotlinx.html.emptyMap, it) }

open class LINE(initialAttributes : Map<String, String>, override val consumer : TagConsumer<*>) : HTMLTag("line", consumer, initialAttributes, "http://www.w3.org/2000/svg", false, false),
    CommonAttributeGroupFacade {}
inline fun RBuilder.line(classes: String? = null, block: RDOMBuilder<LINE>.() -> Unit): ReactElement = tag(block) { LINE(
    attributesMapOf("class", classes), it) }

open class POLYLINE(initialAttributes : Map<String, String>, override val consumer : TagConsumer<*>) : HTMLTag("polyline", consumer, initialAttributes, "http://www.w3.org/2000/svg", false, false),
    CommonAttributeGroupFacade {}
inline fun RBuilder.polyline(classes: String? = null, block: RDOMBuilder<POLYLINE>.() -> Unit): ReactElement = tag(block) { POLYLINE(
    attributesMapOf("class", classes), it) }
