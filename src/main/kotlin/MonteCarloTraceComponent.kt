import kotlinx.css.*
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import styled.css
import styled.inlineStyles

external class TraceProps: RProps {
    var agent: Agent<RaceTrackState, RaceTrackAction>
    var agentState: RaceTrackState
    var action: RaceTrackAction
}

class Trace: RComponent<TraceProps, RState>() {
    override fun RBuilder.render() {

        val centerX = props.agentState.x * displayScaling + displayScaling / 2
        val centerY = props.agentState.y * displayScaling + displayScaling / 2

        val maxAP = props.agent!!.pi.get(props.agentState)?.probabilities?.maxBy { it.weight }

        if (maxAP != null) {
            if (props.action != maxAP.item) {
                text {
                    attrs {
                        attributes["x"] = "${centerX - (displayScaling / 2) * .63}"
                        attributes["y"] = "${centerY + (displayScaling / 2) * .63}"
                        attributes["stroke"] = "none"
                        attributes["fill"] = "#04B404"
                    }
                    inlineStyles {
                        fontSize = LinearDimension("14px")
                        fontWeight = FontWeight.bold
                        fontFamily = "Times New Roman"
                    }
                    +"ε"
                }
            }
        }

        var arrowColor =  "black"
        //if (maxAP != null && action != maxAP.item) {
        //    arrowColor = "black"
        //}

        when (props.action) {
            RACETRACK_ACTION_UP -> {

                line {
                    attrs {
                        attributes["x1"] = "${centerX}"
                        attributes["y1"] = "${centerY + paddedDisplayScaling / 8}"
                        attributes["x2"] = "${centerX}"
                        attributes["y2"] = "${centerY - paddedDisplayScaling / 2.0}"
                        attributes["stroke"] = arrowColor
                    }
                }
                polyline {
                    attrs{
                        attributes["stroke"] = arrowColor
                        attributes["fill"] = arrowColor
                        attributes["points"] = "${centerX - 3},${centerY - paddedDisplayScaling / 2.0 + 3} ${centerX},${centerY - paddedDisplayScaling / 2.0} ${centerX + 3},${centerY - paddedDisplayScaling / 2.0 + 3} "
                        attributes["stroke"] = arrowColor
                    }
                }
            }
            RACETRACK_ACTION_LEFT -> {
                line {
                    attrs {
                        attributes["x1"] = "${centerX + paddedDisplayScaling / 8}"
                        attributes["y1"] = "${centerY}"
                        attributes["x2"] = "${centerX - paddedDisplayScaling / 2.0}"
                        attributes["y2"] = "${centerY}"
                        attributes["stroke"] = arrowColor
                    }
                }
                polyline {
                    attrs{
                        attributes["stroke"] = arrowColor
                        attributes["fill"] = arrowColor
                        attributes["points"] = "${centerX - paddedDisplayScaling / 2.0 + 3},${centerY - 3} ${centerX - paddedDisplayScaling / 2.0},${centerY} ${centerX - paddedDisplayScaling / 2.0 + 3},${centerY + 3}"
                        attributes["stroke"] = arrowColor
                    }
                }
            }
            RACETRACK_ACTION_RIGHT -> {
                line {
                    attrs {
                        attributes["x1"] = "${centerX - paddedDisplayScaling / 8}"
                        attributes["y1"] = "${centerY}"
                        attributes["x2"] = "${centerX + paddedDisplayScaling / 2.0}"
                        attributes["y2"] = "${centerY}"
                        attributes["stroke"] = arrowColor
                    }
                }
                polyline {
                    attrs{
                        attributes["stroke"] = arrowColor
                        attributes["fill"] = arrowColor
                        attributes["points"] = "${centerX + paddedDisplayScaling / 2.0 - 3},${centerY - 3} ${centerX + paddedDisplayScaling / 2.0},${centerY} ${centerX + paddedDisplayScaling / 2.0 - 3},${centerY + 3}"
                    }
                }
            }
            RACETRACK_ACTION_DOWN -> {
                line {
                    attrs {
                        attributes["x1"] = "${centerX}"
                        attributes["y1"] = "${centerY - paddedDisplayScaling / 8}"
                        attributes["x2"] = "${centerX}"
                        attributes["y2"] = "${centerY + paddedDisplayScaling / 2.0}"
                        attributes["stroke"] = arrowColor
                    }
                }
                polyline {
                    attrs{
                        attributes["stroke"] = arrowColor
                        attributes["fill"] = arrowColor
                        attributes["points"] = "${centerX - 3},${centerY + paddedDisplayScaling / 2.0 - 3} ${centerX},${centerY + paddedDisplayScaling / 2.0} ${centerX + 3},${centerY + paddedDisplayScaling / 2.0 - 3}"
                        attributes["stroke"] = arrowColor
                    }
                }
            }
        }

        val textColor = "#CCCCCF"

        props.agent!!.pi.get(props.agentState)?.probabilities?.forEach {
            val qSA = props.agent!!.q?.get(StateAction(props.agentState, it.item)) ?: -1.0
            val textQOffset = displayScaling / 8

            when (it.item) {
                RACETRACK_ACTION_UP -> {
                    styledText {
                        attrs {
                            attributes["x"] = "${centerX + textQOffset}"
                            attributes["y"] = "${centerY - paddedDisplayScaling / 2.0 + textQOffset}"
                        }
                        css {
                            this.declarations["stroke"] = textColor
                            fontWeight = FontWeight.normal
                            fontSize = LinearDimension("8px")
                        }
                        +qSA.toInt().toString()
                    }
                    line {
                        attrs {
                            attributes["x1"] = "${centerX}"
                            attributes["y1"] = "${centerY}"
                            attributes["x2"] = "${centerX}"
                            attributes["y2"] = "${centerY - it.weight * paddedDisplayScaling / 2.0}"
                            attributes["stroke"] = "#FFC300"
                            attributes["strokeWidth"] = "3"
                            attributes["opacity"] = "0.6"
                        }
                        +it.weight.toString()
                    }
                }
                RACETRACK_ACTION_LEFT -> {
                    styledText {
                        attrs {
                            attributes["x"] = "${centerX - paddedDisplayScaling / 2.0}"
                            attributes["y"] = "${centerY - textQOffset}"
                        }
                        css {
                            this.declarations["stroke"] = textColor
                            fontWeight = FontWeight.normal
                            fontSize = LinearDimension("8px")
                        }

                        +qSA.toInt().toString()
                    }
                    line {
                        attrs {
                            attributes["x1"] = "${centerX}"
                            attributes["y1"] = "${centerY}"
                            attributes["x2"] = "${centerX - it.weight * paddedDisplayScaling / 2.0}"
                            attributes["y2"] = "${centerY}"
                            attributes["stroke"] = "#FFC300"
                            attributes["strokeWidth"] = "3"
                            attributes["opacity"] = "0.6"
                        }
                        +it.weight.toString()

                    }
                }
                RACETRACK_ACTION_RIGHT -> {
                    styledText {
                        attrs {
                            attributes["x"] = "${centerX + paddedDisplayScaling / 2.0 - textQOffset * 2}"
                            attributes["y"] = "${centerY + textQOffset}"
                        }
                        css {
                            this.declarations["stroke"] = textColor
                            fontWeight = FontWeight.normal
                            fontSize = LinearDimension("8px")
                        }

                        +qSA.toInt().toString()
                    }
                    line {
                        attrs {
                            attributes["x1"] = "${centerX}"
                            attributes["y1"] = "${centerY}"
                            attributes["x2"] = "${centerX + it.weight * paddedDisplayScaling / 2.0}"
                            attributes["y2"] = "${centerY}"
                            attributes["stroke"] = "#FFC300"
                            attributes["strokeWidth"] = "3"
                            attributes["opacity"] = "0.6"
                        }
                        +it.weight.toString()

                    }
                }
                RACETRACK_ACTION_DOWN -> {
                    styledText {
                        attrs {
                            attributes["x"] = "${centerX + textQOffset}"
                            attributes["y"] = "${centerY + paddedDisplayScaling / 2.0}"
                        }
                        css {
                            this.declarations["stroke"] = textColor
                            fontWeight = FontWeight.normal
                            fontSize = LinearDimension("8px")
                        }

                        +qSA.toInt().toString()
                    }
                    line {
                        attrs {
                            attributes["x1"] = "${centerX}"
                            attributes["y1"] = "${centerY}"
                            attributes["x2"] = "${centerX}"
                            attributes["y2"] = "${centerY + it.weight * paddedDisplayScaling / 2.0}"
                            attributes["stroke"] = "#FFC300"
                            attributes["strokeWidth"] = "3"
                            attributes["opacity"] = "0.6"
                        }
                        +it.weight.toString()

                    }
                }
            }
        }


        text {
            attrs {
                attributes["x"] = "${props.agentState.x * displayScaling + displayScaling / 2}"
                attributes["y"] = "${props.agentState.y * displayScaling + displayScaling / 2}"
            }
//                +action.char.toString()
        }
    }
}