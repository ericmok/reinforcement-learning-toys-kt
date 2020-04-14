import kotlinx.css.*
import kotlinx.css.properties.*
import kotlinx.html.*
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLInputElement
import react.*
import react.dom.*
import styled.*
import kotlin.browser.document
import kotlin.browser.window

val displayScaling = 50.0
val displayScalingString = displayScaling.toString() + "px"

val paddedDisplayScaling = displayScaling * .8

fun ArrayList<Array<Char>>.getDimensions(): Pair<Int, Int> {
    return (this[0].size) to this.size
}

external interface AppState: RState {
    var runner: MonteCarloRunner<RaceTrackState, RaceTrackAction>
    var environment: RaceTrack
    var agent: RacecarMonteCarloAgent
    var interval: Int
    var epsilon: Double
}

class App: RComponent<RProps, AppState>() {

    override fun AppState.init() {
        environment = RaceTrack()
        agent = RacecarMonteCarloAgent()
        runner = MonteCarloRunner(environment, agent)
        epsilon = 0.4
    }

    fun startOver() {
        state.runner.start()
        forceUpdate()
    }

    fun stopAutoRun() {
        window.clearInterval(state.interval)
        setState {
            interval = -1
        }
    }

    fun runAutoStep() {
        stopAutoRun()
        setState {
            interval = window.setInterval({
                if (!state.runner.canStillStep()) {
                    state.runner.end()
                    state.runner.start()
                }
                state.runner.step()

                forceUpdate()
            }, 32)
        }
    }

    fun runSingleStep() {
        stopAutoRun()
        state.runner.step()
        forceUpdate()
    }

    fun autoEpisode() {
        setState {
            window.clearInterval(state.interval)

            interval = window.setInterval({
                runner.runOneEpisode()
                forceUpdate()
            }, 200)
        }
    }

    override fun RBuilder.render() {
        h1 {
            +"Reinforcement Learning"
        }
        div {
            child(Board::class) {
                attrs {
                    runner = state.runner
                    board = state.environment.board
//                trajectory = state.runner.trajectory
                }
            }
        }

        input {
            attrs {
                type = InputType.range
                min = "0.01"
                max = "1.0"
                step = "0.05"
                value = state.epsilon.toString()

                onChangeFunction = { ev ->
                    val newValue = (ev.target as HTMLInputElement)!!.value.toDouble()
                    println(newValue)
                    state.runner.agent.epsilon = newValue
                    setState{
                       this.epsilon = newValue
                    }
                }
            }
        }
        label {
            +"Epsilon: "
        }
        span {
            +state.epsilon.toString()
        }
        br {}
        button {
            +"AUTO EPISODE \uD83D\uDDD8"
            attrs {
                onClickFunction = {
                    autoEpisode()
                }
            }
        }
        button {
            +"AUTO STEP \uD83D\uDDD8"
            attrs {
                onClickFunction = {
                    runAutoStep()
                }
            }
        }
        button {
            +"BACK TO START POSITION"
            attrs {
                onClickFunction = {
                    startOver()
                }
            }
        }
        button {
            +"STEP"
            attrs {
                onClickFunction = { ev ->
                    runSingleStep()
                }
            }
        }
        styledButton {
            +"STOP"
            attrs {
                onClickFunction = {
                    stopAutoRun()
                }
            }
            css {
                padding = "10px"
                background = "#FCC"
                border = "solid 1px black"
                borderRadius = LinearDimension("3px")
                fontWeight = FontWeight.bold
            }
        }
    }
}

fun draw() {
    render(document.getElementById("app")) {
        child(App::class) {}
    }
}

fun main() {
    draw()
}