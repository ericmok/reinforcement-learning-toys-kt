/**
 * A Q-Learning Agent
 */
abstract class QLearningAgent<S: State, A: Action>(var gamma: Double = 1.0,
                                                   var epsilon: Double = 0.5,
                                                   var alpha: Double = 0.1): Agent<S, A> {

    override val q = HashMap<StateAction<S, A>, Double>()
    override val pi = HashMap<S, ProbabilityDistribution<A>>()

    /**
     * Get probability of action from state.
     * Initialize policy to random if first time
     */
    open fun getOrCreatePolicyForState(state: S): ProbabilityDistribution<A> {
        return pi.getOrPut(state) {
            val newProbs = ProbabilityDistribution<A>()
            newProbs.setEvents(actionsForState(state))
            newProbs.normalize()
            newProbs
        }
    }

    /**
     * Set of actions agent can take for a state
     */
    abstract override fun actionsForState(state: S): Set<A>

    /**
     * Get an action from internal policy
     */
    open override fun sampleActionFromState(state: S): A {
        return getOrCreatePolicyForState(state).sample()
    }

    fun improvePolicy(state: S,
                      action: A,
                      nextStateSample: NextStateSample<S>) {

        val sa = StateAction(state, action)
        val saValue = q.getOrPut(sa, {0.0})

        val maxQ: Double = actionsForState(state).map {
            q.getOrDefault(StateAction(nextStateSample.state, it), 0.0)
        }.max()!!

        val delta = nextStateSample.reward + gamma * maxQ - saValue

        q[StateAction(state, action)] = saValue + alpha * delta

        val policy = getOrCreatePolicyForState(sa.state)

        val maxAction = policy.probabilities.map {
            Pair(it, q.getOrDefault(StateAction(state, it.item), Double.NEGATIVE_INFINITY))
        }.maxBy {
            it.second
        }!!.first.item

        for (probability in policy.probabilities) {
            if (probability.item == maxAction) {
                probability.weight = 1.0 - epsilon + (epsilon / actionsForState(state).size)
            } else {
                probability.weight = epsilon / actionsForState(state).size
            }
        }
        policy.normalize()
    }
}