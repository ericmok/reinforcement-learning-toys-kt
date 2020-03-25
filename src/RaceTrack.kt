import java.lang.StringBuilder

const val OPEN_STATE = ' '
const val STARTING_STATE = '1'
const val ENDING_STATE = '2'
const val WALL = '@'

/**
 * "RaceTrack" or maze for an agent to run from start to finish in as few time steps as possible.
 * This is a rough, partial implementation of Sutton's Reinforcement Learning book's Programming Exercise 5.12
 *
 * The track gets automatically loaded on object construction
 */
class RaceTrack: Environment {

    /// List of starting states on board. Can be multiple. Gets loaded after parsing the board
    val startingStates = ArrayList<State>()

    /// List of ending states on board. Can be multiple. Gets loaded after parsing the board
    val endingStates = ArrayList<State>()

    /**
     * Internal representation of the 2D "RaceTrack" in chars.
     * The board is stored in row major format so indexing the board in 2D should
     * subscript y then x.
     *
     * For example: board[y][x]
     *
     * <b>Hint:</b> Use helper function #boardPositionAt(x, y)
     */
    val board: ArrayList<Array<Char>> = ArrayList()

    /// Dimensions of the 2D board. Gets loaded after loading the RaceTrack.
    var size = Pair(0, 0)

    init {
        load()
    }

    /**
     * Load a level of the RaceTrack. This is a hardcoded string
     */
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

    /**
     * Helper function to get the character at board position (x, y)
     */
    fun boardPositionAt(x: Int, y: Int): Char {
        return board[y][x]
    }

    /**
     * @return If state is an ending state. (Whether we win)
     */
    fun isTerminatingState(state: State): Boolean {
        return (boardPositionAt(state.x, state.y) == ENDING_STATE)
    }

    /**
     * Encodes environment dynamics and returns the "next state" that would occur
     * if an agent would to make an action at the state.
     */
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

        // Calculate the would-be next position in environment
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

    /**
     * Returns representation of board in string format
     */
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

    /**
     * @return Random state from array of starting states for the loaded board
     */
    fun getRandomStartingState(): State {
        return startingStates.random()
    }

    /**
     * @return String pictoral representation of the board with trajectory drawn on top
     */
    fun drawTrajectoryString(trajectory: List<Visit>): String {
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