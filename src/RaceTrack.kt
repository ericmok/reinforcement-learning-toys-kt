import java.lang.StringBuilder

interface Environment {
    fun sampleNextStateFromStateAction(state: State, action: Action): NextStateSample
}

interface Agent {
    fun sampleActionFromState(state: State): Action
}

enum class Action {
    UP, LEFT, RIGHT, DOWN
}

data class NextStateSample(val state: State, val reward: Double)

class Visit(state: State, var action: Action, var reward: Double) {

    var x: Int = state.x
    var y: Int = state.y
    var isFirstVisit: Boolean = false

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
        return "<${x}, ${y}> ${action} R:${reward} first:${isFirstVisit}>"
    }
}

class State(val x: Int, val y: Int) {

    companion object {
        fun fromVisit(visit: Visit): State {
            return State(visit.x, visit.y)
        }
    }

    fun clone(): State {
        return State(x, y)
    }

    fun copy(other: State): State {
        return State(other.x, other.y)
    }

    override fun toString(): String {
        return "(${x}, ${y})"
    }

    override fun equals(other: Any?): Boolean {
        return other != null &&
               other is State &&
               other.x == x &&
               other.y == y
    }

    override fun hashCode(): Int {
        return (x * 100_000 + y)
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
            xxxxxxxxxxxxxxxxxxxxx
            xx          xx     2x
            x           xx      x
            xx                  x
            x                   x
            x      xx           x
            x1     xx           x
            xxxxxxxxxxxxxxxxxxxxx
        """.trimIndent()

        val lines = track.lines()

        size = Pair(size.first, lines.size)

        var isFirstRead = true

        lines.withIndex().forEach { (y, line) ->
            val letters = line

            val sizeX = letters.length

            if (isFirstRead) size = Pair(sizeX, size.second)
            isFirstRead = false


            val row = Array(sizeX) { x ->
                when (letters[x]) {
                    STARTING_STATE -> {
                        startingStates.add(State(x, y))
                    }
                    ENDING_STATE -> {
                        endingStates.add(State(x, y))
                    }
                }
                letters[x]
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
                WALL -> -1.5
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
                    return NextStateSample(state, transitionReward(nextPosX, nextPosY))
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
                    return NextStateSample(state, transitionReward(nextPosX, nextPosY))
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
                    return NextStateSample(state, transitionReward(nextPosX, nextPosY))
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
                    return NextStateSample(state, transitionReward(nextPosX, nextPosY))
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

    fun drawTrajectory(trajectory: List<Visit>): String {
        val returnString = StringBuilder()

        board.withIndex().forEach { (y, line) ->
            line.withIndex().forEach { (x, char) ->

                val t = trajectory.find { it.x == x && it.y ==y }

                if (t != null) {
                    when (t.action) {
                        Action.UP -> returnString.append("↑") //↑
                        Action.LEFT -> returnString.append("<") //←
                        Action.DOWN -> returnString.append("↓") //↓
                        Action.RIGHT -> returnString.append(">") //→
                    }
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


class ProbabilityEvent<T>(val item: T, var weight: Double, var integratedDensity: Double)

class ProbabilityDistribution<T> {
    val probabilities = ArrayList<ProbabilityEvent<T>>()
    private var cumulativeWeight: Double = 0.0

    fun setEvents(vararg events: T) {
        val size = events.size.toDouble()

        events.forEachIndexed { index, element ->
            probabilities.add(ProbabilityEvent(element, size, index / size))
        }
    }

    fun normalize() {
        cumulativeWeight = probabilities.fold(0.0) {sum, probabilityEvent ->
            val ret = sum + probabilityEvent.weight
            probabilityEvent.integratedDensity = ret
            ret
        }

//        probabilities.shuffle()
        probabilities.sortBy { probabilityEvent -> probabilityEvent.integratedDensity }
    }

    fun sample(): T {
        val r = Math.random() * cumulativeWeight

        probabilities.forEach { probabilityEvent: ProbabilityEvent<T> ->
            if (probabilityEvent.integratedDensity >= r) {
                return probabilityEvent.item
            }
        }

        return probabilities[0].item
    }
}

class Racecar: Agent {
    val q = HashMap<StateAction, Double>()
    val pi = HashMap<State, ProbabilityDistribution<Action>>()
    val returns = HashMap<StateAction, ArrayList<Double>>()
    var gamma = 0.9
    var epsilon = 0.2

    /**
     * Get probability of action from state.
     * Initialize policy to random if first time
     */
    fun getOrCreatePolicyForState(state: State): ProbabilityDistribution<Action> {
        return pi.getOrPut(state) {
            val newProbs = ProbabilityDistribution<Action>()
            newProbs.setEvents(*Action.values())
            newProbs.normalize()
            newProbs
        }
    }

    fun getOrCreateQValues(stateAction: StateAction): Double {
        return q.getOrDefault(stateAction, 0.0)
    }

    /**
     * Get an action from internal policy
     */
    override fun sampleActionFromState(state: State): Action {
        val actionProbs = getOrCreatePolicyForState(state)
        return actionProbs.sample()
    }

    /**
     * Update the policy probabilities based on returns
     */
    fun improvePolicy(trajectory: ArrayList<Visit>) {

        trajectory.reversed().foldIndexed(0.0) { index, successorReturn, visit ->
            val accumulatedReturn = gamma * successorReturn + visit.reward

            val sa = StateAction(State.fromVisit(visit), visit.action)

            if (!visit.isFirstVisit) {
                return@foldIndexed accumulatedReturn
            }

            returns.getOrPut(sa) {
                arrayListOf()
            }.add(accumulatedReturn)

            q[StateAction(State.fromVisit(visit), visit.action)] = returns[sa]!!.average()

            // I need to look for all state actions with a particular state!!!
            val maxEntry = q.entries.filter { it.key.state == sa.state }.maxBy { it.value }!!
            val maxAction = maxEntry.key.action

            val policy = getOrCreatePolicyForState(sa.state)
            policy.probabilities.find { it.item == maxAction }!!
                    .weight = 1.0 - epsilon + (epsilon / Action.values().size)
            policy.probabilities.forEach {
                it.weight = epsilon / Action.values().size
            }
            policy.normalize()

            accumulatedReturn
        }
    }
}

class Runner(var raceTrack: RaceTrack, var racecar: Racecar) {

    var firstVisit = hashSetOf<State>()
    var trajectory = ArrayList<Visit>()

    fun runOneEpisode() {

        println("starting states:")
        for (startingState in raceTrack.startingStates) {
            print("${startingState} ")
        }
        println("")

        var statePointer = raceTrack.getRandomStartingState()
        var maxTime = 10000

        while (!raceTrack.isTerminatingState(statePointer) && maxTime > 1) {
            maxTime -= 1

            val action = racecar.sampleActionFromState(statePointer)
            val nextStateSample = raceTrack.sampleNextStateFromStateAction(statePointer, action)

//            print(nextStateSample)

            val visit = Visit(statePointer, action, nextStateSample.reward)

            trajectory.add(visit)

            if (!firstVisit.contains(statePointer)) {
                firstVisit.add(statePointer)
                visit.isFirstVisit = true
            }

//            println("${statePointer}: ${action} -> ${nextStateSample.state}")
//            if (maxTime % 5 == 0) {
//                println(raceTrack.drawTrajectory(trajectory))
//            }
            if (maxTime % 500 == 0) {
                if (maxTime % 1000 == 0) print("_") else print(".")
            }

            statePointer = nextStateSample.state.clone()
        }

        println("")
        println(raceTrack.drawTrajectory(trajectory))
        println("Trajectory: ${trajectory.size} Steps")
        println("")

        if (raceTrack.isTerminatingState(statePointer)) {
            println("WIN!")
//            racecar.epsilon *= 0.9
        }
        println(racecar.returns.get(StateAction(State(2,6), Action.UP))?.size)

        racecar.improvePolicy(trajectory)

//        println(trajectory)

//
//        racecar.pi.entries.forEach {
//            it.value.probabilities.forEach {probabilityEvent: ProbabilityEvent<Action> ->
//                println("(${it.key.x},${it.key.y}) ${probabilityEvent.item.name} ${probabilityEvent.weight}")
//            }
//        }
//
//        if (trajectory.size < 100) {
//            for (visit in trajectory) {
//                println(visit)
//            }
//        }


        println("")

        trajectory.clear()
        firstVisit.clear()
    }
}

fun main() {
    var runner = Runner(RaceTrack(), Racecar())
    for (i in 0..50) {
        println("======= EPISODE ${i} ==============")
        runner.runOneEpisode()
    }
}