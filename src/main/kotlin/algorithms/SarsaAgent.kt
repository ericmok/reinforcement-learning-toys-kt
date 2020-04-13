/**
 * A Sarsa Learning Agent
 */
abstract class SarsaAgent<S: State, A: Action>(var gamma: Double = 1.0,
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

    fun improvePolicy(
        state: S,
        action: A,
        nextStateSample: NextStateSample<S>,
        nextAction: A) {
        val sa = StateAction(state, action)

        val delta = nextStateSample.reward + gamma * q.getOrElse(StateAction(nextStateSample.state, nextAction), { 0.0 }) - q.getOrPut(sa, { 0.0 })

        q[sa] = q[sa]!! + alpha * delta

        val policy = getOrCreatePolicyForState(sa.state)

        val maxAction = policy.probabilities.map {
            Pair(it, q.getOrElse(StateAction(state, it.item), { Double.NEGATIVE_INFINITY }))
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