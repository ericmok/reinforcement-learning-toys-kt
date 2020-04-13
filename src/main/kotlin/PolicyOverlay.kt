import kotlinx.css.LinearDimension
import kotlinx.css.fontFamily
import kotlinx.css.fontSize
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import styled.inlineStyles

class PolicyOverlayProps: RProps {
    var environment: RaceTrack? = null
    var agent: MonteCarloAgent<RaceTrackState, RaceTrackAction>? = null
}

class PolicyOverlay: RComponent<PolicyOverlayProps, RState>() {
    override fun RBuilder.render() {

        if (props.environment != null && props.agent != null) {
            for ((y, row) in props.environment!!.board.withIndex()) {
                for ((x, c) in row.withIndex()) {
                    val state = RaceTrackState(x, y)

                    val centerX = x * displayScaling + displayScaling / 2
                    val centerY = y * displayScaling + displayScaling / 2

                    val maxA = props.agent!!.pi.get(state)?.probabilities?.maxBy { it.weight }?.item

                    for (action in props.agent!!.actionsForState(state)) {
                        if (maxA != null) {
                            if (action != maxA) {
                                text {
                                    attrs {
                                        attributes["x"] = "${centerX - displayScaling * .303}"
                                        attributes["y"] = "${centerY - displayScaling * .303}"
                                        attributes["stroke"] = "none"
                                        attributes["fill"] = "#11FF11"
                                    }
                                    inlineStyles {
                                        fontSize = LinearDimension("12px")
                                        fontFamily = "Arial"
                                    }
                                    +"EX"
                                }
                            }
                        }

                        props.agent!!.pi.get(state)?.probabilities?.forEach {
                            when (it.item) {
                                RACETRACK_ACTION_UP -> {
                                    line {
                                        attrs {
                                            attributes["x1"] = "${centerX}"
                                            attributes["y1"] = "${centerY}"
                                            attributes["x2"] = "${centerX}"
                                            attributes["y2"] = "${centerY - it.weight * displayScaling / 2.0}"
                                            attributes["stroke"] = "#FFC300"
                                        }
                                        +it.weight.toString()
                                    }
                                }
                                RACETRACK_ACTION_LEFT -> {
                                    line {
                                        attrs {
                                            attributes["x1"] = "${centerX}"
                                            attributes["y1"] = "${centerY}"
                                            attributes["x2"] = "${centerX - it.weight * displayScaling / 2.0}"
                                            attributes["y2"] = "${centerY}"
                                            attributes["stroke"] = "#FFC300"
                                        }
                                        +it.weight.toString()

                                    }
                                }
                                RACETRACK_ACTION_RIGHT -> {
                                    line {
                                        attrs {
                                            attributes["x1"] = "${centerX}"
                                            attributes["y1"] = "${centerY}"
                                            attributes["x2"] = "${centerX + it.weight * displayScaling / 2.0}"
                                            attributes["y2"] = "${centerY}"
                                            attributes["stroke"] = "#FFC300"
                                        }
                                        +it.weight.toString()

                                    }
                                }
                                RACETRACK_ACTION_DOWN -> {
                                    line {
                                        attrs {
                                            attributes["x1"] = "${centerX}"
                                            attributes["y1"] = "${centerY}"
                                            attributes["x2"] = "${centerX}"
                                            attributes["y2"] = "${centerY + it.weight * displayScaling / 2.0}"
                                            attributes["stroke"] = "#FFC300"
                                        }
                                        +it.weight.toString()

                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
    }
}
