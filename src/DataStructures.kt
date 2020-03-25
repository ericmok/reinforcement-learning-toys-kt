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

/// Used to represent what the environment returns from a state-action pair
data class NextStateSample(val state: State, val reward: Double)

/**
 * A container for state-action pairs and extra data like reward and isFirstVisit to help with handling trajectories
 * and training.
 *
 * Think of this as an "Episode Step"
 */
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
