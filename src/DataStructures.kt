/**
 * State within environment
 */
interface State

fun <S : State> S.clone(): S {
    return this
}

/**
 * The decision maker in RL Tasks
 */
interface Agent<S: State, A: Action> {
    /// Q Values
    val q: HashMap<StateAction<S, A>, Double>

    /// Policy(state) -> Probability Distribution for action to be taken
    val pi: HashMap<S, ProbabilityDistribution<A>>

    fun sampleActionFromState(state: S): A
    fun actionsForState(state: S): Set<A>
}

/**
 * Environment for RL Tasks.
 */
interface Environment<S: State, A: Action> {

    val startingStates: ArrayList<S>
    val endingStates: ArrayList<S>

    /**
     * Restart environment. New start may be a function of a state.
     */
    fun restartForNextEpisode(state: S? = null): S

    /**
     * @return Is the state the end of the episode?
     */
    fun isTerminatingState(state: S): Boolean

    /**
     * @return The next state as a function of a state and action
     */
    fun sampleNextStateFromStateAction(state: S, action: A): NextStateSample<S>

    /**
     * @return A println friendly string to show the trajectory in the environment in 2D
     */
    fun getDrawTrajectoryString(trajectory: Collection<Visit<S, A>>): String
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
class StateAction<S: State, A: Action>(var state: S, var action: A) {
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
data class NextStateSample<S: State>(val state: S, val reward: Double)

/**
 * A container for state-action pairs and extra data like reward and isFirstVisit to help with handling trajectories
 * and training.
 *
 * Think of this as an "Episode Step"
 */
data class Visit<S: State, A: Action>(val state: S, val action: A, var reward: Double) {

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

    fun setEvents(events: Set<T>) {
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
data class RaceTrackState(val x: Int, val y: Int): State {
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