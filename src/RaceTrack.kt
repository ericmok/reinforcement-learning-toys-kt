import java.lang.StringBuilder

interface Environment {
    fun sampleNextStateFromStateAction(state: State, action: Action): NextStateSample
}

interface Agent {
    fun sampleActionFromState(state: State): Action
}

enum class Action {
    UP {
        override fun toChar(): Char = '↑'
    },
    LEFT {
        override fun toChar(): Char = '<'
    },
    RIGHT {
        override fun toChar(): Char = '>'
    },
    DOWN {
        override fun toChar(): Char = '↓'
    };

    abstract fun toChar(): Char
}

data class NextStateSample(val state: State, val reward: Double)

data class Visit(val state: State, val action: Action, var reward: Double) {

    var isFirstVisit: Boolean = false

    override fun equals(other: Any?): Boolean {
        if (other is Visit) {
            if (state == other.state) {
                return true
            }
        }
        return false
    }

    override fun hashCode(): Int {
        return state.hashCode() * 10_000 + action.hashCode()
    }

    override fun toString(): String {
        return "<(${state.x}, ${state.y}) ${action} R:${reward} first:${isFirstVisit}>"
    }
}

data class State(val x: Int, val y: Int) {

    companion object {
        fun fromVisit(visit: Visit): State {
            return visit.state.clone()
        }
    }

    fun clone(): State {
        return State(x, y)
    }

    override fun toString(): String {
        return "(${x}, ${y})"
    }
}

class StateAction(var state: State, var action: Action) {
    override fun hashCode(): Int {
        return "${state.hashCode()} ${action}".hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is StateAction) {
            if (state == other.state && action == other.action) {
                return true
            }
        }
        return false
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

val OPEN_STATE = ' '
val STARTING_STATE = '1'
val ENDING_STATE = '2'
val WALL = '@'

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
            @@@@@@@@@@@@@@@@@@@@@@
            @            @@    2 @
            @            @@      @
            @            @@      @
            @            @@      @
            @                    @
            @                    @
            @                    @
            @     @@             @
            @     @@             @
            @ 1   @@             @
            @@@@@@@@@@@@@@@@@@@@@@
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
                ENDING_STATE -> 1.0
                else -> -1.0
            }

        fun nextStateAs(x: Int, y: Int, nextX: Int, nextY: Int): NextStateSample {
            val nextState: State = when (boardPositionAt(nextX, nextY)) {
                WALL -> State(x, y)
                ENDING_STATE -> State(nextX, nextY)
                else ->  State(nextX, nextY)
            }

            return NextStateSample(nextState, transitionReward(nextX, nextY))
        }

        return when (action) {
            Action.UP -> {
                val nextPosX = state.x
                val nextPosY = state.y - 1

                if (state.y == 0) {
                    return nextStateAs(state.x, state.y, state.x, state.y)
                }

                return nextStateAs(state.x, state.y, nextPosX, nextPosY)
            }
            Action.DOWN -> {
                val nextPosX = state.x
                val nextPosY = state.y + 1

                if (state.y == size.second - 1) {
                    return nextStateAs(state.x, state.y, state.x, state.y)
                }

                return nextStateAs(state.x, state.y, nextPosX, nextPosY)
            }
            Action.LEFT -> {
                val nextPosX = state.x - 1
                val nextPosY = state.y

                if (state.x == 0) {
                    return nextStateAs(state.x, state.y, state.x, state.y)
                }

                return nextStateAs(state.x, state.y, nextPosX, nextPosY)
            }
            Action.RIGHT -> {
                val nextPosX = state.x + 1
                val nextPosY = state.y

                if (state.x == size.first - 1) {
                    return nextStateAs(state.x, state.y, state.x, state.y)
                }

                return nextStateAs(state.x, state.y, nextPosX, nextPosY)
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
        val drawing = ArrayList<StringBuilder>()

        val returnString = StringBuilder()

        for ((y, line) in board.withIndex()) {
            drawing.add(StringBuilder())

            for ((x, char) in line.withIndex()) {
                drawing[y].append(char)
            }

            drawing[y].append("\n")
        }

        for (visit in trajectory) {
            drawing[visit.state.y][visit.state.x] = visit.action.toChar()
        }

        for (stringBuilder in drawing) {
            returnString.append(stringBuilder)
        }

        return returnString.toString()
    }
}

class Racecar(var gamma: Double = 1.0, var epsilon: Double = 0.5): Agent {
    val q = HashMap<StateAction, Double>()
    val pi = HashMap<State, ProbabilityDistribution<Action>>()
    val returns = HashMap<StateAction, ArrayList<Double>>()

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

            // Look for all state actions with a particular state
            val maxEntry = q.entries.filter { it.key.state == sa.state }.maxBy { it.value }!!
            val maxAction = maxEntry.key.action

            val policy = getOrCreatePolicyForState(sa.state)

            policy.probabilities.forEach {
                if (it.item == maxAction) {
                    it.weight = 1.0 - epsilon + (epsilon / Action.values().size)
                } else {
                    it.weight = epsilon / Action.values().size
                }
            }
            policy.normalize()

            accumulatedReturn
        }
    }
}

class Runner(var raceTrack: RaceTrack, var racecar: Racecar) {

    var firstVisit = hashSetOf<State>()
    var trajectory = ArrayList<Visit>()

    fun reset() {
        trajectory.clear()
        firstVisit.clear()
    }

    fun printStats() {
        println("")
        println(raceTrack.drawTrajectory(trajectory.reversed()))
        println("Trajectory: ${trajectory.size} Steps")
        println("")

        if (raceTrack.isTerminatingState(trajectory.last().state)) {
            println("WIN!")
        }

        for (startingState in raceTrack.startingStates) {
            for (action in Action.values()) {
                println("${startingState} ${action} reward: ")
                println(racecar.returns[StateAction(startingState, action)]?.average() ?: "0.0")
            }
        }

        println()

        println("epsilon: ${racecar.epsilon}")
        println("gamma: ${racecar.gamma}")

        println()
    }

    fun runOneEpisode() {

        var statePointer = raceTrack.getRandomStartingState()
        var maxTime = 10000

        while (!raceTrack.isTerminatingState(statePointer) && maxTime > 1) {
            maxTime -= 1

            val action = racecar.sampleActionFromState(statePointer)
            val nextStateSample = raceTrack.sampleNextStateFromStateAction(statePointer, action)

            val visit = Visit(statePointer, action, nextStateSample.reward)

            trajectory.add(visit)

            if (!firstVisit.contains(statePointer)) {
                firstVisit.add(statePointer)
                visit.isFirstVisit = true
            }

            statePointer = nextStateSample.state.clone()
        }

        racecar.improvePolicy(trajectory)
    }
}

fun main() {
    var runner = Runner(RaceTrack(), Racecar(epsilon = 0.6))
    for (i in 0..10_000) {

        // Drop off epsilon as we ramp down
        if (i >= 8000 && i % 10 == 0) {
            runner.racecar.epsilon *= 0.95
        }

        runner.runOneEpisode()

        if (i % 1000 == 0) {
            println("============= EPISODE ${i} =====")
            runner.printStats()
        }

        runner.reset()
    }
}