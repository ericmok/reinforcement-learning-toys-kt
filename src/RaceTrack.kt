import java.lang.StringBuilder
import javax.swing.plaf.basic.BasicSliderUI

interface Environment {
    fun sampleNextStateFromStateAction(state: State, action: Action): NextStateSample
}

interface Agent {
    fun sampleActionFromState(state: State): Action
}

enum class Action {
    UP, LEFT, RIGHT, DOWN
}

class ActionDistribution {
    val actionDistribution = ArrayList<Pair<Action, Double>>()

    fun addActionProbability(ap: Pair<Action, Double>) = actionDistribution.add(ap)
}

data class NextStateSample(var state: State, var reward: Double)

class Visit(state: State, var action: Action, var reward: Double) {

    var x: Int = state.x
    var y: Int = state.y
    var isFirstVisit: Boolean = false

    val returns = ArrayList<Double>()

    fun averageRewards(): Double {
        return returns.average()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Visit) {
            return false
        }
        return other.x == x && other.y == y
    }

    override fun hashCode(): Int {
        return 100_000 * x + y
    }

    override fun toString(): String {
        return "<${x}, ${y}>"
    }
}

class State(var x: Int, var y: Int) {

    companion object {
        fun fromVisit(visit: Visit): State {
            return State(visit.x, visit.y)
        }
    }

    fun copy(other: State): State {
        x = other.x
        y = other.y
        return this
    }

    override fun toString(): String {
        return "(${x}, ${y})"
    }

    override fun hashCode(): Int {
        return "${x} ${y}".hashCode()
    }
}

val OPEN_STATE = ' '
val STARTING_STATE = '1'
val ENDING_STATE = '2'
val WALL = 'x'

class RaceTrack: Environment {
    val startingStates = ArrayList<State>()
    val endingStates = ArrayList<State>()
    val board: ArrayList<Array<Char>> = ArrayList()
    var size = Pair(0, 0)

    init {
        load()
    }

    fun load() {
        var track = """
            x x x x x x x x x x x x
            x               2 2 2 x
            x                     x
            x                     x
            x                     x
            x                     x
            x 1 1 1               x
            x x x x x x x x x x x x
        """.trimIndent()

        val lines = track.lines()

        size = Pair(size.first, lines.size)

        var isFirstRead = true

        lines.withIndex().forEach { (y, line) ->
            val letters = line.withIndex().filter { (index, value) ->
                index % 2 == 0
            }

            val sizeX = letters.size
            if (isFirstRead) size = Pair(sizeX, size.second)
            isFirstRead = false


            val row = Array(sizeX) { x ->
                val char = letters[x].value

                when (char) {
                    STARTING_STATE -> {
                        startingStates.add(State(x, y))
                    }
                    ENDING_STATE -> {
                        endingStates.add(State(x, y))
                    }
                }
                letters[x].value
            }

            board.add(row)
        }

        print(this)
    }

    fun boardPositionAt(x: Int, y: Int): Char {
        return board[y][x]
    }

    fun isTerminatingState(state: State): Boolean {
        return (boardPositionAt(state.x, state.y) == ENDING_STATE)
    }

    override fun sampleNextStateFromStateAction(state: State, action: Action): NextStateSample {
        fun transitionReward(posX: Int, posY: Int): Double =
            when (boardPositionAt(posX, posY)) {
                WALL -> -2.0
                ENDING_STATE -> 100.0
                else -> -1.0
            }

        return when (action) {
            Action.UP -> {
                val nextPosX = state.x
                val nextPosY = state.y - 1

                if (state.y == 0) {
                    return NextStateSample(state, -1.0)
                }

                // TODO: Factor out nextState from transitionReward calc
                if (boardPositionAt(nextPosX, nextPosY) == WALL) {
                    return NextStateSample(getRandomStartingState(), transitionReward(nextPosX, nextPosY))
                }

                NextStateSample(State(nextPosX, nextPosY), transitionReward(nextPosX, nextPosY))
            }
            Action.DOWN -> {
                val nextPosX = state.x
                val nextPosY = state.y + 1

                if (state.y == size.second - 1) {
                    return NextStateSample(state, -1.0)
                }

                if (boardPositionAt(nextPosX, nextPosY) == WALL) {
                    return NextStateSample(getRandomStartingState(), transitionReward(nextPosX, nextPosY))
                }

                NextStateSample(State(nextPosX, nextPosY), transitionReward(nextPosX, nextPosY))
            }
            Action.LEFT -> {
                val nextPosX = state.x - 1
                val nextPosY = state.y

                if (state.x == 0) {
                    return NextStateSample(state, -1.0)
                }

                if (boardPositionAt(nextPosX, nextPosY) == WALL) {
                    return NextStateSample(getRandomStartingState(), transitionReward(nextPosX, nextPosY))
                }

                NextStateSample(State(nextPosX, nextPosY), transitionReward(nextPosX, nextPosY))
            }
            Action.RIGHT -> {
                val nextPosX = state.x + 1
                val nextPosY = state.y

                if (state.x == size.first - 1) {
                    return NextStateSample(state, -1.0)
                }

                if (boardPositionAt(nextPosX, nextPosY) == WALL) {
                    return NextStateSample(getRandomStartingState(), transitionReward(nextPosX, nextPosY))
                }

                NextStateSample(State(nextPosX, nextPosY), transitionReward(nextPosX, nextPosY))
            }
        }
    }

    override fun toString(): String {
        val returnString = StringBuilder()

        board.forEach {
            it.forEach {
                returnString.append(it)
            }
            returnString.append("\n")
        }

        return returnString.toString()
    }

    fun getRandomStartingState(): State {
        return startingStates.random()
    }

    fun drawTrajectory(trajectory: ArrayList<Visit>): String {
        val returnString = StringBuilder()

        board.withIndex().forEach { (y, line) ->
            line.withIndex().forEach { (x, char) ->

                var found = false
                trajectory.forEach {
                    t ->
                    if (t.x == x && t.y == y) {
                        found = true
                    }
                }
                if (found) {
                    returnString.append("*")
                } else {
                    returnString.append(char)
                }
            }

            returnString.append("\n")
        }

        return returnString.toString()
    }
}

class StateAction(var state: State, var action: Action) {
    override fun hashCode(): Int {
        return "${state.x} ${state.y} ${action}".hashCode()
    }
}

class ActionProbability(var action: Action, var double: Double) {
    var cumulativeProb: Double = double
}

class ProbabilityEvent<T>(val item: T, var weight: Double, var integratedDensity: Double)

class ProbabilityDistribution<T> {
    val probabilities = ArrayList<ProbabilityEvent<T>>()
    private var cumulativeWeight: Double = 0.0

    fun normalize() {
        cumulativeWeight = probabilities.fold(0.0) {sum, probabilityEvent ->
            val ret = sum + probabilityEvent.weight
            probabilityEvent.integratedDensity = ret
            ret
        }

        probabilities.sortBy { probabilityEvent -> probabilityEvent.integratedDensity }
    }

    fun sample(): T {
        val r = Math.random() * cumulativeWeight

        probabilities.forEach { probabilityEvent: ProbabilityEvent<T> ->
            if (r >= probabilityEvent.integratedDensity) {
                return probabilityEvent.item
            }
        }

        return probabilities[0].item
    }
}

class Racecar: Agent {
    val q = HashMap<StateAction, Double>()
    val pi = HashMap<State, Array<ActionProbability>>()
    val returns = HashMap<Pair<State, Action>, List<Double>>()

//    val actionProbabilities = ArrayList<ActionProbability>()
//    private var accProbability = 0.0
//    private val actionCumulativeProbabilities = ArrayList<ActionProbability>()

    init {
//        val actions = Action.values()
//        val uniform = 1.0 / actions.size

//        actions.forEachIndexed { index, action ->  actionProbabilities.add(ActionProbability(action, uniform))}
//        actions.forEachIndexed { index, action -> actionCumulativeProbabilities.add(ActionProbability(action, index * uniform)) }
//        accProbability = 1.0
    }

    fun getOrCreatePolicyForState(state: State): Array<ActionProbability> {
        var actionDistFromStatePolicy = pi[state]

        // Initialize to uniform
        if (actionDistFromStatePolicy == null) {

            val numberActions = Action.values().size
            val prob = Array<ActionProbability>(numberActions) {
                ActionProbability(Action.values()[it], (1.0 / numberActions) * it.toDouble())
            }

//            prob.forEach { println(it.cumulativeProb) }
            pi[state] = prob
            actionDistFromStatePolicy = prob
        }
//        actionDistFromStatePolicy.forEach { println(it.cumulativeProb) }
        return actionDistFromStatePolicy
    }

    fun getOrCreateQValues(stateAction: StateAction): Double {
        var r = q[stateAction]

        return if (r == null) {
            q[StateAction(stateAction.state, stateAction.action)] = 0.0
            0.0
        } else {
            r
        }
    }

    override fun sampleActionFromState(state: State): Action {
        // This should take from Policy

        var actionDistFromStatePolicy = getOrCreatePolicyForState(state)
        // Now that we have action probabilities, pick an action


        val acc = actionDistFromStatePolicy.fold(0.0) { acc, actionProbability ->
            actionProbability.cumulativeProb = acc
//            println("act prob: ${actionProbability.cumulativeProb}")
            acc + actionProbability.double
        }

        val r = Math.random() * acc

        // Pick one based on weighted probabilities

        actionDistFromStatePolicy.forEach {
//            println("${r}>=${it.cumulativeProb}")
            if (it.cumulativeProb >= r) {
                return it.action
            }
        }

        return Action.values().random()
    }
//
//    private fun recalculateCumulativePolicy() {
//        accProbability = actionProbabilities.foldIndexed(0.0) {index, acc, actionProbability ->
//            val ret = acc + actionProbability.double
//            actionCumulativeProbabilities[index].double = acc
//            ret
//        }
//    }

    /**
     * No point if when executing policy, there is soft max.
     * Only point is to hard max it
     * Or do off policy stuff
     */
    fun improvePolicy(trajectory: ArrayList<Visit>) {
        // This should update the Q's and change the Policy

        trajectory.reversed().foldIndexed(0.0) {index, accumulatedReturn, visit ->
            var ret: Double = 0.1 * accumulatedReturn + visit.reward
            visit.returns.add(ret)

            // TODO: Add first visit condition
            q[StateAction(State(visit.x, visit.y), visit.action)] = visit.averageRewards()

            // argmax a of the q values. so we look at a state and go through all q values for that state

            var maxAction: Action = Action.values()[0]
            var maxValue: Double = -1000.0

            Action.values().forEach { action ->
                var v = getOrCreateQValues(StateAction(State.fromVisit(visit), action))
                if (v > maxValue) {
                    maxAction = action
                    maxValue = v
                }
            }

            // epsilon greedy i slipped in... cuz too tired...
            // TODO: Fix this. Need to add cumulative
            var cum = 0.0
            getOrCreatePolicyForState(State.fromVisit(visit)).forEachIndexed { index, actionProbability ->
                if (actionProbability.action == maxAction) {
                    actionProbability.double = 1.0 - 0.4 + (0.4 / Action.values().size)
                } else {
                    actionProbability.double = (0.4 / Action.values().size) * index
                }
                cum += actionProbability.double
                actionProbability.cumulativeProb = cum
            }

//            var a = getOrCreatePolicyForState(State(visit.x, visit.y)).maxWith(Comparator { t, t2 ->
//                if (t.double < t2.double) {
//                    return@Comparator -1
//                }
//                else if (t.double == t2.double) {
//                    return@Comparator 0
//                }
//                1
//            })

//            var policy = pi[State(visit.x, visit.y)]
//            policy!!.forEach { if (it.action == a.action) {
//                    // this is the max
//                }
//            }

            ret
        }
    }
}

class Runner(var raceTrack: RaceTrack, var racecar: Racecar) {

    var trajectory = ArrayList<Visit>()

    fun runOneEpisode() {

        var statePointer = raceTrack.getRandomStartingState()
        var maxTime = 10

        while (!raceTrack.isTerminatingState(statePointer) && maxTime > 1) {
            maxTime -= 1

            val action = racecar.sampleActionFromState(statePointer)
            val nextStateSample = raceTrack.sampleNextStateFromStateAction(statePointer, action)

            trajectory.add(Visit(statePointer, action, nextStateSample.reward))

            println("${statePointer}: ${action} -> ${nextStateSample.state}")

            statePointer.copy(nextStateSample.state)

            if (maxTime % 5 == 0) {
                println(raceTrack.drawTrajectory(trajectory))
            }
        }

        racecar.improvePolicy(trajectory)


//        println(trajectory)
        println(raceTrack.drawTrajectory(trajectory))
//        racecar.pi.entries.forEach {
//            for (actionProbability in it.value) {
//                println("pi ${it.key.x} ${it.key.y}: ${actionProbability.double}")
//            }
//        }


        if (raceTrack.isTerminatingState(statePointer)) {
            println("WIN!")
        }
    }
}

fun main() {
    var runner = Runner(RaceTrack(), Racecar())
    for (i in 0..5) {
        runner.runOneEpisode()
    }
}