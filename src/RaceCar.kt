/**
 * Agent for the race track
 */
class RaceCar(var gamma: Double = 1.0, var epsilon: Double = 0.5): Agent {

    /// Q Values
    val q = HashMap<StateAction, Double>()

    /// Policy(state) -> Probability Distribution for action to be taken
    val pi = HashMap<State, ProbabilityDistribution<Action>>()

    /// Memorize all returns for a StateAction (Q-value)
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

    /**
     * Get q value for stateAction. Set to zero if not exist
     */
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
     * Update the policy based on returns.
     *
     * Follows Sutton's On-Policy first-visit MC control algorithm for epsilon-soft policies in his
     * Reinforcement Learning book in Section 5.4
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