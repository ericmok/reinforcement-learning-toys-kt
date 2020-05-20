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

object Styles : StyleSheet("ButtonStyles") {
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

    val inputRange by css {
        width = LinearDimension("200px")
    }
}

val displayScaling = 50.0
val displayScalingString = displayScaling.toString() + "px"

val paddedDisplayScaling = displayScaling * .8

fun ArrayList<Array<Char>>.getDimensions(): Pair<Int, Int> {
    return (this[0].size) to this.size
}

external interface AppState: RState {
    var runner: GeneralRunner<RaceTrackState, RaceTrackAction>
    var environment: RaceTrack
    var agent: Agent<RaceTrackState, RaceTrackAction>
    var autoStepInterval: Int
    var epsilon: Double
    var gamma: Double
    var alpha: Double
    var useAlpha: Boolean
    var numberEpisodes: Int
    var performance: ArrayList<Int>
    var singleStepMode: Boolean
    var autoEpisodeIsRunning: Boolean
}

class App: RComponent<RProps, AppState>() {

    override fun AppState.init() {
        environment = RaceTrack()
        agent = RacecarSarsaAgent()
        runner = SarsaRunner(environment, agent as RacecarSarsaAgent)
        epsilon = 0.4
        gamma = 1.0
        alpha = 0.125
        useAlpha = true
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

        window.requestAnimationFrame {
            if (state.autoEpisodeIsRunning) {
                autoEpisodeTimeoutFunction()
            }
        }
    }

    fun autoEpisode() {
        setState {
            singleStepMode = false
            autoEpisodeIsRunning = true
        }
        autoEpisodeTimeoutFunction()
    }

    fun RBuilder.renderParameters() {
        styledDiv {
            css {
                declarations["grid-area"] = "params"
            }
            div {
                styledInput {
                    css {
                        +Styles.inputRange
                    }
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
                            setState {
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
            }
            div {
                styledInput {
                    css {
                        +Styles.inputRange
                    }
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
                            setState {
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
            }
            div {
                styledFieldSet {
                    css {
                        maxWidth = LinearDimension("500px")
                    }
                    legend {
                        label {
                            +"Use Fixed Alpha"
                        }
                        input {
                            attrs {
                                type = InputType.checkBox
                                checked = state.useAlpha


                                onChangeFunction = { ev ->
                                    setState {
                                        useAlpha = !state.useAlpha //(ev.target as HTMLInputElement).checked
                                    }

                                    if (!state.useAlpha) {
                                        state.runner.agent.alpha = -1.0
                                    }
                                }
                            }
                        }
                    }
                    styledDiv {
                        styledInput {
                            css {
                                +Styles.inputRange
                            }
                            attrs {
                                type = InputType.range
                                min = "0.0"
                                max = "1.0"
                                step = "0.03125"
                                value = state.alpha.toString()
                                disabled = !state.useAlpha

                                onChangeFunction = { ev ->
                                    val newValue = (ev.target as HTMLInputElement).value.toDouble()
                                    println(newValue)
                                    state.runner.agent.alpha = newValue
                                    setState {
                                        this.alpha = newValue
                                    }
                                }
                            }
                        }
                        styledSpan {
                            css {
                                if (!state.useAlpha) {
                                    opacity = 0.5
                                }
                            }
                            label {
                                +"Alpha: "
                            }
                            span {
                                +state.alpha.toString()
                            }
                        }

                    }
                }
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
        }
    }

    fun RBuilder.renderControls() {
        styledDiv {
            css {
                declarations["grid-area"] = "controls"
                display = Display.grid
//                gridTemplateColumns = GridTemplateColumns("1fr 1fr")
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
                        +Styles.button
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
                        +Styles.button
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
                        +Styles.button
                    }
                }
                styledButton {
                    +"⏹️ CLEAR"
                    css {
                        +Styles.button
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
                        +Styles.button
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
                        +Styles.button
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
                        +Styles.button
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
                        +Styles.button
                    }
                }
            }
        }
    }

    override fun RBuilder.render() {
        styledDiv {
            css {
                display = Display.grid
                gridTemplateColumns = GridTemplateColumns("2fr 1fr")
                gridTemplateAreas = GridTemplateAreas("""
                    "header header"
                    "board params"
                    "board controls"
                    "footer footer"
                """.trimIndent())
            }
            styledH1 {
                css {
                    this.declarations["grid-area"] = "header"
                }
                +"Reinforcement Learning"
            }
            styledDiv {
                css {
                    this.declarations["grid-area"] = "board"
                }
                child(Board::class) {
                    attrs {
                        runner = state.runner
                        board = state.environment.board
//                trajectory = state.runner.trajectory
                    }
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
            renderParameters()
            renderControls()
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