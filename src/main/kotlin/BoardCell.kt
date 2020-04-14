import kotlinx.css.LinearDimension
import kotlinx.css.fontSize
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import styled.inlineStyles

class BoardCellProps(var idx: Int, var idy: Int,
                     var isCurrent: Boolean,
                     var character: Char,
                     var actionTaken: Action?,
                     var policy: ProbabilityDistribution<RaceTrackAction>?): RProps

class BoardCell: RComponent<BoardCellProps, RState>() {
    override fun RBuilder.render() {
        rect {
            attrs {
                onClickFunction = {
                    println(props.actionTaken)
                }
                attributes["x"] = "${props.idx * displayScaling}"
                attributes["y"] = "${props.idy * displayScaling}"
                attributes["width"] = displayScaling.toString()
                attributes["height"] = displayScaling.toString()
                attributes["stroke"] = "#DDDDDD"
                attributes["strokeWidth"] = "1"
            }
            when (props.character) {
                '1' -> {
                    attrs {
                        attributes["fill"] = "#48D1CC"
                    }
                }
                '2' -> {
                    attrs {
                        attributes["fill"] = "#3CB371"
                    }
                }
                '@' -> {
                    attrs {
                        attributes["fill"] = "skyblue"
                    }
                }
                else -> {
                    attrs {
                        attributes["fill"] = "none"
                    }
                }
            }

            if (props.actionTaken != null) {
                text {
                    attrs {
                        attributes["x"] = "${props.idx * displayScaling + displayScaling / 2}"
                        attributes["y"] = "${props.idy * displayScaling + displayScaling / 2}"
                        attributes["fill"] = "black"
                        inlineStyles {
                            fontSize = LinearDimension("16px")
                        }
                    }
                    when (props.actionTaken!!.char) {
                        '<' -> +"\uD83E\uDC50"
                        '>' -> +"\uD83E\uDC52"
                        else -> +props.actionTaken!!.char.toString()
                    }
                }
            }
        }
    }
}
