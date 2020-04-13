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
import kotlin.math.nextTowards

val displayScaling = 50.0
val displayScalingString = displayScaling.toString() + "px"

val paddedDisplayScaling = displayScaling * .8

fun ArrayList<Array<Char>>.getDimensions(): Pair<Int, Int> {
    return (this[0].size) to this.size
}

class TrajectoryElementProps<S: State, A: Action>: RProps {
    var list = ArrayList<Visit<RaceTrackState, RaceTrackAction>>()
    var monteCarloAgent: MonteCarloAgent<S, A>? = null
}

class TrajectoryElement: RComponent<TrajectoryElementProps<RaceTrackState, RaceTrackAction>, RState>() {

    override fun RBuilder.render() {
        val overlayedList = hashMapOf<RaceTrackState, RaceTrackAction>()
        for (visit in props.list) {
            overlayedList.put(visit.state, visit.action)
        }

        for ((agentState, action) in overlayedList) {

            val centerX = agentState.x * displayScaling + displayScaling / 2
            val centerY = agentState.y * displayScaling + displayScaling / 2

            val maxAP = props.monteCarloAgent!!.pi.get(agentState)?.probabilities?.maxBy { it.weight }

            if (maxAP != null) {
                if (action != maxAP.item) {
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

            when (action) {
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

            props.monteCarloAgent!!.pi.get(agentState)?.probabilities?.forEach {
                val qSA = props.monteCarloAgent!!.q?.get(StateAction(agentState, it.item)) ?: -1.0
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
                    attributes["x"] = "${agentState.x * displayScaling + displayScaling / 2}"
                    attributes["y"] = "${agentState.y * displayScaling + displayScaling / 2}"
                }
//                +action.char.toString()
            }

        }
    }
}

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

class BoardAgentActionProps(var x: Int = 0, var y: Int = 0, var trajectoryStep: Int = 0, var actionTaken: Action, var qValue: ProbabilityDistribution<RaceTrackAction>?): RProps

class BoardAgentAction: RComponent<BoardAgentActionProps, RState>() {
    override fun RBuilder.render() {
        styledDiv {
            inlineStyles {
                left = LinearDimension("${props.x * displayScaling}px")
                top = LinearDimension("${props.y * displayScaling}px")
                transform {
//                    translateZ(LinearDimension("${props.trajectoryStep * 2}px"))
                }
            }
            css {
                position = Position.absolute
                width = LinearDimension(displayScalingString)
                height = LinearDimension(displayScalingString)
                border = "solid 1px black"
                boxSizing = BoxSizing.borderBox

                display = Display.grid
                GridTemplateAreas(". top .")
                GridTemplateAreas("left . right")
                GridTemplateAreas(". bottom .")
                this.justifyContent = JustifyContent.center
                alignItems = Align.center
            }

            styledDiv {
                inlineStyles {
                    width = LinearDimension("100%")
                    gridColumnStart = GridColumnStart("2")
                    gridColumnEnd = GridColumnEnd("3")
                    gridRowStart = GridRowStart("2")
                    gridRowEnd = GridRowEnd("3")
                }
                +props.actionTaken.char.toString()
            }


            if  (props.qValue != null) {
                for (probability in props.qValue!!.probabilities) {
                    when(probability.item) {
                        RACETRACK_ACTION_UP -> {
                            styledDiv {
                                inlineStyles {
//                                    position = Position.absolute
//                                    left = LinearDimension("${displayScaling / 2}px")
//                                    top = LinearDimension("${3}px")
                                    gridColumnStart = GridColumnStart("2")
                                    gridColumnEnd = GridColumnEnd("3")
                                    gridRowStart = GridRowStart("1")
                                    gridRowEnd = GridRowEnd("2")
                                }
//                                +probability.item.char.toString()
                                +probability.weight.toString()
                            }
                        }
                        RACETRACK_ACTION_LEFT -> {
                            styledDiv {
                                inlineStyles {
//                                    position = Position.absolute
//                                    left = LinearDimension("${3}px")
//                                    top = LinearDimension("${displayScaling / 2}px")
                                    gridColumnStart = GridColumnStart("1")
                                    gridColumnEnd = GridColumnEnd("2")
                                    gridRowStart = GridRowStart("2")
                                    gridRowEnd = GridRowEnd("3")
                                    this.declarations["justify-self"] = "start"
                                }
//                                +"\uD83E\uDC20"
                                +probability.weight.toString()
                            }
                        }
                        RACETRACK_ACTION_RIGHT -> {
                            styledDiv {
                                inlineStyles {
//                                    position = Position.absolute
//                                    left = LinearDimension("${displayScaling - 3}px")
//                                    top = LinearDimension("${displayScaling / 2}px")
                                    gridColumnStart = GridColumnStart("3")
                                    gridColumnEnd = GridColumnEnd("4")
                                    gridRowStart = GridRowStart("2")
                                    gridRowEnd = GridRowEnd("3")
                                    this.declarations["justify-self"] = "end"
                                }
//                                +"\uD83E\uDC22"
                                +probability.weight.toString()
                            }
                        }
                        RACETRACK_ACTION_DOWN -> {
                            styledDiv {
                                inlineStyles {
//                                    position = Position.absolute
//                                    left = LinearDimension("${displayScaling - 3}px")
//                                    top = LinearDimension("${displayScaling - 3}px")
                                    gridColumnStart = GridColumnStart("2")
                                    gridColumnEnd = GridColumnEnd("3")
                                    gridRowStart = GridRowStart("3")
                                    gridRowEnd = GridRowEnd("4")
                                }
//                                +probability.item.char.toString()
                                +probability.weight.toString()
                            }
                        }
                    }
                }
            }


        }
    }
}

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
//        styledDiv {
//            css {
//                position = Position.relative
//                boxSizing = BoxSizing.borderBox
//                border = "solid 1px #CCC"
//                borderRadius = LinearDimension("20px")
//                width = LinearDimension("40px")
//                height = LinearDimension("40px")
//                background = "#EC7063"
//            }
//            inlineStyles {
//                left = LinearDimension("${props.x * displayScaling + 5}px")
//                top = LinearDimension("${props.y * displayScaling + 5}px")
//
////                transition("left", time, Timing.linear)
////                transition("top", time, Timing.linear)
//            }
//        }
    }
}

class BoardProps(var board: ArrayList<Array<Char>>,
                 var runner: MonteCarloRunner<RaceTrackState, RaceTrackAction>): RProps

class Board: RComponent<BoardProps, RState>() {

    override fun RBuilder.render() {
        val (dimX, dimY) = props.board.getDimensions()

        svg {
            attrs {
                this.attributes["xmlns"] = "http://www.w3.org/2000/svg"
                this.attributes["viewBox"] = "0 0 ${dimX * displayScaling} ${dimY * displayScaling}"
                this.attributes["width"] = "1400"
            }

            for (y in 0 until dimY) {
                for (x in 0 until dimX) {
                    child(BoardCell::class) {
                        attrs {
                            this.idx = x
                            this.idy = y
                            this.character = props.board[y][x]
                            this.policy = null
//                            this.actionTaken = overlayedTrajectory[RaceTrackState(x, y)]
                        }
                    }
                }
            }

            child(BoardAgent::class) {
                attrs {
                    this.x = props.runner.currentState.x
                    this.y = props.runner.currentState.y
                }
            }
//
//            child(PolicyOverlay::class) {
//                attrs {
//                    this.environment = props.runner.environment as RaceTrack
//                    this.agent = props.runner.agent
//                }
//            }

            child(TrajectoryElement::class) {
                attrs {
                    this.list = props.runner.trajectory.list
                    this.monteCarloAgent = props.runner.agent
                }
            }
//
//            for ((idx, t) in props.runner.trajectory.list.withIndex()) {
//                child(BoardAgentAction::class) {
//                    attrs {
//                        this.x = t.state.x
//                        this.y = t.state.y
//                        this.qValue = props.runner.agent.pi[RaceTrackState(this.x, this.y)]
//                        this.trajectoryStep = idx
//                        this.actionTaken = t.action
//                    }
//                }
//            }

        }

//        styledDiv {
//            attrs {
//            }
//
//            val (dimX, dimY) = props.board.getDimensions()
//
//            css {
//                position = Position.relative
//                width = LinearDimension((displayScaling * dimX).toString() + "px")
//                height = LinearDimension((displayScaling * dimY).toString() + "px")
//                border = "solid 1px black"
////
////                transform {
////                    perspective(LinearDimension("100px"))
////                    rotateX(Angle("10deg"))
////                    translateZ(LinearDimension("-70px"))
////                }
////
////                this.declarations["transform-style"] = "preserve-3d"
//            }
//
//            println("${dimX}, ${dimY}")
//            for (y in 0 until dimY) {
//                for (x in 0 until dimX) {
//                    child(BoardCell::class) {
//                        attrs {
//                            this.idx = x
//                            this.idy = y
//                            this.character = props.board[y][x]
//                            this.policy = null
////                            this.actionTaken = overlayedTrajectory[RaceTrackState(x, y)]
//                        }
//                    }
//                }
//
//            }
//        }
    }
}

class TrajectoryBoardProps: RProps {
    var trajectory: Trajectory<RaceTrackState, RaceTrackAction> = Trajectory()
    var update: Boolean = false
}

class TrajectoryBoardState: RState {
    var overlayedTrajectory: HashMap<RaceTrackState, RaceTrackAction> = hashMapOf()
}

class TrajectoryBoard: RComponent<TrajectoryBoardProps, TrajectoryBoardState>() {

    override fun TrajectoryBoardState.init(props: TrajectoryBoardProps) {
        overlayedTrajectory.clear()
        for (visit in props.trajectory.list) {
            // overwrite with newer
            //overlayedTrajectory[visit.state] = visit.action
            overlayedTrajectory.put(visit.state, visit.action)
        }
    }

    override fun RBuilder.render() {
        for ((state, action) in state.overlayedTrajectory) {
            rect {
                attrs {
                    attributes["x"] = "${state.x * displayScaling}"
                    attributes["y"] = "${state.y * displayScaling}"
                    attributes["width"] = "${displayScaling}"
                }
            }
        }
    }
}


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
//            inlineStyles {
//                width = LinearDimension(displayScalingString)
//                height = LinearDimension(displayScalingString)
//                position = Position.absolute
//                top = LinearDimension("${props.idy * displayScaling}px")
//                left = LinearDimension("${props.idx * displayScaling}px")
//                padding = "0"
//                border = "solid 1px #DDDDDD"
//                boxSizing = BoxSizing.borderBox
//                display = Display.flex
//                flexDirection = FlexDirection.row
//                justifyContent = JustifyContent.center
//                alignItems = Align.center
//            }
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

            if (props.isCurrent) {
//                css {
//                    background = "red"
//                    borderRadius = LinearDimension("50px")
//                }
            }
        }
    }
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