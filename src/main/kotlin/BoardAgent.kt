import kotlinx.css.properties.Time
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import styled.inlineStyles

class BoardAgentProps(var x: Int = 0, var y: Int = 0): RProps

class BoardAgent: RComponent<BoardAgentProps, RState>() {

    private val time = Time("0.12s")

    override fun RBuilder.render() {

        circle {
            attrs {
                attributes["cx"] = "${props.x * displayScaling + displayScaling / 2}"
                attributes["cy"] = "${props.y * displayScaling + displayScaling / 2}"
                attributes["r"] = ((displayScaling / 2)*.8).toString()
                attributes["fill"] = "#EC7063"
                attributes["stroke"] = "black"
                attributes["stoke-width"] = "1"
            }
            inlineStyles {
//                transition("cx", time, Timing.linear)
//                transition("cy", time, Timing.linear)
            }
        }
    }
}
