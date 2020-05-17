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

object ButtonStyles : StyleSheet("ButtonStyles") {
    val button by css {
        border = "solid 1px #1082c7"
        padding = "5px"
        margin = "1px"
        borderRadius = LinearDimension("3px")
        lineHeight = LineHeight("1")
        height = LinearDimension("40px")
        color = Color("#1082c7")
        background = "none"
        cursor = Cursor.pointer

        hover {
            background = "#333"
            color = Color("#eee")
        }
    }
}

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
    var autoStepInterval: Int
    var epsilon: Double
    var gamma: Double
    var numberEpisodes: Int
    var performance: ArrayList<Int>
    var singleStepMode: Boolean
    var autoEpisodeIsRunning: Boolean
}

class App: RComponent<RProps, AppState>() {

    override fun AppState.init() {
        environment = RaceTrack()
        agent = RacecarMonteCarloAgent()
        runner = MonteCarloRunner(environment, agent)
        epsilon = 0.4
        gamma = 1.0
        numberEpisodes = 0
        performance = arrayListOf()
        singleStepMode = false

        autoEpisodeIsRunning = false
    }


    fun startOver() {
        stopAutoRuns()
        state.runner.start()
    }

    fun stopAutoRuns() {
        window.clearInterval(state.autoStepInterval)
        setState {
            //interval = -1
            autoStepInterval = -1
            autoEpisodeIsRunning = false
        }
    }

    fun runAutoStep() {
        stopAutoRuns()
        if (!state.singleStepMode) {
            state.runner.start()
            setState {
                singleStepMode = true
            }
        }
        setState {
            autoStepInterval = window.setInterval({
                if (!state.runner.canStillStep()) {
                    state.runner.end()
                    state.runner.start()
                    setState {
                        numberEpisodes += 1
                    }
                    state.performance.add(state.runner.trajectory.size)
                }
                state.runner.step()

                forceUpdate()
            }, 32)
        }
    }

    fun runSingleStep() {
        stopAutoRuns()
        if (!state.singleStepMode) {
            state.runner.start()
            setState {
                singleStepMode = true
                autoEpisodeIsRunning = false
            }
        }
        state.runner.step()
        forceUpdate()
    }

    fun runOneEpisode() {
        stopAutoRuns()
        state.runner.runOneEpisode()
        setState {
            this.autoEpisodeIsRunning = false
            this.singleStepMode = false
            this.numberEpisodes = numberEpisodes + 1
        }
    }

    private fun autoEpisodeTimeoutFunction() {
        state.runner.runOneEpisode()
        setState {
            numberEpisodes += 1
        }
        state.performance.add(state.runner.trajectory.size)

        window.setTimeout({
            if (state.autoEpisodeIsRunning) {
                autoEpisodeTimeoutFunction()
            }
        }, 200)
    }

    fun autoEpisode() {
        setState {
            singleStepMode = false
            autoEpisodeIsRunning = true
        }
        autoEpisodeTimeoutFunction()
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
                    val newValue = (ev.target as HTMLInputElement).value.toDouble()
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


        input {
            attrs {
                type = InputType.range
                min = "0.00"
                max = "1.0"
                step = "0.05"
                value = state.gamma.toString()

                onChangeFunction = { ev ->
                    val newValue = (ev.target as HTMLInputElement).value.toDouble()
                    println(newValue)
                    state.runner.agent.gamma = newValue
                    setState{
                        this.gamma = newValue
                    }
                }
            }
        }
        label {
            +"Gamma: "
        }
        span {
            +state.gamma.toString()
        }

        div {
            div {
                +"Trajectory Length: "
                +state.runner.trajectory.size.toString()
            }
            div {
                +"Number episodes: "
                +state.numberEpisodes.toString()
            }
        }
//        svg {
//            polyline {
//                attrs {
//                    attributes["points"] = state.performance.takeLast(100).foldIndexed("") { index, acc, i ->
//                        acc + " ${index * 100},${i}"
//                    }
//                    attributes["fill"] = "none"
//                    attributes["stroke"] = "black"
//                    attributes["stroke-width"] = "2px"
//                }
//            }
//        }

        br {}
        styledDiv {
            css {
                display = Display.grid
                gridTemplateColumns = GridTemplateColumns("1fr 1fr")
            }
            styledDiv {
                h4 {+"Per Episode Runs"}

                styledButton {
                    +"\uD83D\uDDD8 AUTO EPISODE"
                    attrs {
                        onClickFunction = {
                            autoEpisode()
                        }
                    }
                    css {
                        +ButtonStyles.button
                    }
                }
                styledButton {
                    +"▶️ RUN ONE EPISODE"
                    attrs {
                        onClickFunction = {
                            runOneEpisode()
                        }
                    }
                    css {
                        +ButtonStyles.button
                    }
                }
                styledButton {
                    +"⏸️ PAUSE"
                    attrs {
                        onClickFunction = {
                            stopAutoRuns()
                        }
                    }
                    css {
                        +ButtonStyles.button
                    }
                }
                styledButton {
                    +"⏹️ CLEAR"
                    css {
                        +ButtonStyles.button
                    }
                    attrs {
                        onClickFunction = {
                            startOver()
                        }
                    }
                }
            }
            styledDiv {
                h4 { +"Per Step Runs" }

                styledButton {
                    +"\uD83D\uDDD8 AUTO STEP"
                    attrs {
                        onClickFunction = {
                            runAutoStep()
                        }
                    }
                    css {
                        +ButtonStyles.button
                    }
                }
                styledButton {
                    +"▶️ STEP"
                    attrs {
                        onClickFunction = { ev ->
                            runSingleStep()
                        }
                    }
                    css {
                        +ButtonStyles.button
                    }
                }
                styledButton {
                    +"⏸️ PAUSE"
                    attrs {
                        onClickFunction = {
                            stopAutoRuns()
                        }
                    }
                    css {
                        +ButtonStyles.button
                    }
                }
                styledButton {
                    +"⏹️ CLEAR"
                    attrs {
                        onClickFunction = {
                            startOver()
                        }
                    }
                    css {
                        +ButtonStyles.button
                    }
                }
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