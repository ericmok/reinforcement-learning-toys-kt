/**
 * Environment for RL Tasks.
 */
interface Environment<S, A: Action> {
    fun sampleNextStateFromStateAction(state: S, action: A): NextStateSample<S>
}

/**
 * The decision maker in RL Tasks
 */
interface Agent<S, A: Action> {
    fun sampleActionFromState(state: S): A
    fun actionsForState(state: S): Set<A>
}

/**
 * Monte Carlo agent learns using trajectory
 */
interface MCAgent<S, A: Action>: Agent<S, A> {
    fun improvePolicy(trajectory: Collection<Visit<S, A>>)
}

/**
 * Represents a discrete and countable action in an RL Task.
 * Extend this with a sealed class to create a new set of actions
 */
abstract class Action(open val name: String, open val char: Char) {
    override fun toString(): String {
        return name
    }
}

/**
 * State action container. We may associate values to state-actions to figure out an optimal policy
 */
class StateAction<S, A: Action>(var state: S, var action: A) {
    override fun hashCode(): Int {
        return "${state.hashCode()} ${action}".hashCode()
    }

    override fun equals(other: Any?): Boolean {
        val o = other as? StateAction<S, A> ?: return false
        return state == o.state && action == o.action
    }
}

/**
 * Used to represent what the environment returns from a state-action pair
 */
data class NextStateSample<S>(val state: S, val reward: Double)

/**
 * A container for state-action pairs and extra data like reward and isFirstVisit to help with handling trajectories
 * and training.
 *
 * Think of this as an "Episode Step"
 */
data class Visit<S, A: Action>(val state: S, val action: A, var reward: Double) {

    var isFirstVisit: Boolean = false

    override fun equals(other: Any?): Boolean {
        val o = other as? Visit<S, A> ?: return false
        return state == o.state
    }

    override fun hashCode(): Int {
        return state.hashCode() * 10_000 + action.hashCode()
    }

    override fun toString(): String {
        return "<(${state}) ${action} R:${reward} first:${isFirstVisit}>"
    }
}

/**
 * A random variable outcome in a distribution with a weight.
 */
class ProbabilityEvent<T>(val item: T, var weight: Double, var integratedDensity: Double)

/**
 * A class used to help manage sampling from a list of elements with weighted probabilities
 */
class ProbabilityDistribution<T> {
    val probabilities = ArrayList<ProbabilityEvent<T>>()
    private var cumulativeWeight: Double = 0.0

    fun setEvents(vararg events: T) {
        val size = events.size.toDouble()

        events.forEachIndexed { index, element ->
            probabilities.add(ProbabilityEvent(element, 1.0 / size, index / size))
        }
    }

    /**
     * Doesn't actually normalize but sets cumulative probability count for each event and sorts them.
     * This helps in random sampling from list with weighted probabilities.
     */
    fun normalize() {
        cumulativeWeight = probabilities.fold(0.0) {sum, probabilityEvent ->
            val ret = sum + probabilityEvent.weight
            probabilityEvent.integratedDensity = ret
            ret
        }

//        probabilities.shuffle()
        probabilities.sortBy { probabilityEvent -> probabilityEvent.integratedDensity }
    }

    /**
     * Take from this distribution a random element. Elements may be weighted to be more likely to be picked or not
     *
     * Picture for algorithm is roughly like:
     *
     *
     * |                |       |       |
     * |    A 60%       | B 20% | C 20% |
     * |                |       |       |
     *
     *                    ^
     *                    ^
     *              Random Number < - - - - -
     *
     * Bigger the partition of A, more likely it will be chosen
     */
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

/**
 * A RaceTrackState basically is a position on the board
 */
data class RaceTrackState(val x: Int, val y: Int) {
    companion object {
        fun fromVisit(visit: Visit<RaceTrackState, RaceTrackAction>): RaceTrackState {
            return visit.state.clone()
        }
    }

    fun clone(): RaceTrackState {
        return RaceTrackState(x, y)
    }

    override fun toString(): String {
        return "(${x}, ${y})"
    }
}

/**
 * Sealed class as a container for all actions for the Race Track example
 */
sealed class RaceTrackAction(override val name: String, override val char: Char): Action(name, char)

object RACETRACK_ACTION_UP: RaceTrackAction("UP", '↑')
object RACETRACK_ACTION_LEFT: RaceTrackAction("LEFT", '<')
object RACETRACK_ACTION_RIGHT: RaceTrackAction("RIGHT", '>')
object RACETRACK_ACTION_DOWN: RaceTrackAction("DOWN", '↓')

/// Set of all race track actions takeable
val RACETRACK_ACTIONS = arrayOf(RACETRACK_ACTION_UP, RACETRACK_ACTION_LEFT, RACETRACK_ACTION_RIGHT, RACETRACK_ACTION_DOWN)
