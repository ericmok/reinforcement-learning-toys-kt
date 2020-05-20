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

external interface AppProps: RProps {
    var runner: GeneralRunner<RaceTrackState, RaceTrackAction>
    var environment: RaceTrack
    var agent: Agent<RaceTrackState, RaceTrackAction>
}

external interface AppState: RState {
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

class App: RComponent<AppProps, AppState>() {

    override fun AppState.init() {
        epsilon = 0.3
        gamma = 1.0
        alpha = 0.0625
        useAlpha = true
        numberEpisodes = 0
        performance = arrayListOf()
        singleStepMode = false

        autoEpisodeIsRunning = false
    }


    fun startOver() {
        stopAutoRuns()
        props.runner.start()
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
            props.runner.start()
            setState {
                singleStepMode = true
            }
        }
        setState {
            autoStepInterval = window.setInterval({
                if (!props.runner.canStillStep()) {
                    props.runner.end()
                    props.runner.start()
                    setState {
                        numberEpisodes += 1
                    }
                    state.performance.add(props.runner.trajectory.size)
                }
                props.runner.step()

                forceUpdate()
            }, 32)
        }
    }

    fun runSingleStep() {
        stopAutoRuns()
        if (!state.singleStepMode) {
            props.runner.start()
            setState {
                singleStepMode = true
                autoEpisodeIsRunning = false
            }
        }
        props.runner.step()
        forceUpdate()
    }

    fun runOneEpisode() {
        stopAutoRuns()
        props.runner.runOneEpisode()
        setState {
            this.autoEpisodeIsRunning = false
            this.singleStepMode = false
            this.numberEpisodes = numberEpisodes + 1
        }
    }

    private fun autoEpisodeTimeoutFunction() {
        props.runner.runOneEpisode()
        setState {
            numberEpisodes += 1
        }
        state.performance.add(props.runner.trajectory.size)

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
                padding = "10px"
                boxSizing = BoxSizing.borderBox
            }
            div {
                label {
                    +"Epsilon (affects exploration):"
                }
                br {}
                styledInput {
                    css {
                        +Styles.inputRange
                    }
                    attrs {
                        type = InputType.range
                        min = "0.0"
                        max = "1.0"
                        step = "0.025"
                        value = state.epsilon.toString()

                        onChangeFunction = { ev ->
                            val newValue = (ev.target as HTMLInputElement).value.toDouble()
                            println(newValue)
                            props.runner.agent.epsilon = newValue
                            setState {
                                this.epsilon = newValue
                            }
                        }
                    }
                }
                span {
                    +state.epsilon.toString()
                }
            }
            div {
                label {
                    +"Gamma (affects discounting):"
                }
                br {}
                styledInput {
                    css {
                        +Styles.inputRange
                    }
                    attrs {
                        type = InputType.range
                        min = "0.00"
                        max = "1.0"
                        step = "0.00625"
                        value = state.gamma.toString()

                        onChangeFunction = { ev ->
                            val newValue = (ev.target as HTMLInputElement).value.toDouble()
                            println(newValue)
                            props.runner.agent.gamma = newValue
                            setState {
                                this.gamma = newValue
                            }
                        }
                    }
                }

                span {
                    +state.gamma.toString()
                }
            }
            div {
                // This code sets the useAlpha parameter...
                // This was for the MC agent, but it isn't generalizable to QLearning and Sarsa though...
//                styledFieldSet {
//                    css {
//                        maxWidth = LinearDimension("500px")
//                    }
//                    legend {
//                        label {
//                            +"Use Fixed Alpha"
//                        }
//                        input {
//                            attrs {
//                                type = InputType.checkBox
//                                checked = state.useAlpha
//
//
//                                onChangeFunction = { ev ->
//                                    setState {
//                                        useAlpha = !state.useAlpha //(ev.target as HTMLInputElement).checked
//                                    }
//
//                                    if (!state.useAlpha) {
//                                        props.runner.agent.alpha = -1.0
//                                    }
//                                }
//                            }
//                        }
//                    }
                    styledDiv {
                        label {
                            +"Alpha (affects Q update amount): "
                        }
                        br {}
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
                                    props.runner.agent.alpha = newValue
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
                            span {
                                +state.alpha.toString()
                            }
                        }

                    }
//                }
            }

            div {
                div {
                    +"Trajectory Length: "
                    +props.runner.trajectory.size.toString()
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
                padding = "10px"
                boxSizing = BoxSizing.borderBox
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
                    "board params"
                    "board controls"
                """.trimIndent())
                padding = "10px"
                boxSizing = BoxSizing.borderBox
            }
            styledDiv {
                css {
                    this.declarations["grid-area"] = "board"
                }
                child(Board::class) {
                    attrs {
                        runner = props.runner
                        board = props.environment.board
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
        h1 {
            +"Reinforcement Learning Toy"
        }
        h2 {
            +"Monte Carlo"
        }
        child(App::class) {
            this.attrs {
                environment = RaceTrack()
                agent = RacecarMonteCarloAgent()
                runner = MonteCarloRunner(environment, agent as RacecarMonteCarloAgent)
            }
        }
        h2 {
            +"Q Learning"
        }
        child(App::class) {
            this.attrs {
                environment = RaceTrack()
                agent = RacecarQLearningAgent()
                runner = QLearningRunner(environment, agent as RacecarQLearningAgent)
            }
        }
        h2 {
            +"Sarsa"
        }
        child(App::class) {
            this.attrs {
                environment = RaceTrack()
                agent = RacecarSarsaAgent()
                runner = SarsaRunner(environment, agent as RacecarSarsaAgent)
            }
        }
    }
}

fun main() {
    draw()
}