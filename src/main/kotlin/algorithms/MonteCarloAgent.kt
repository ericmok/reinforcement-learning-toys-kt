/**
 * A Monte Carlo Agent
 * @param gamma Discount rate
 * @param epsilon Parameter to control exploration probability
 * @param alpha Controls how q-values or returns are calculated when new rewards arrive.
 *              Set to a positive number (between 0 and 1) to make returns a weighted average of rewards (more recent
 *              having more weight).
 *              Set to -1 to not use alpha and instead strictly averaging over all returns in history.
 *              (That may slow down training over time.)
 */
abstract class MonteCarloAgent<S: State, A: Action>(override var gamma: Double = 1.0,
                                                    override var epsilon: Double = 0.5,
                                                    override var alpha: Double = 0.125): Agent<S, A> {

    override val q = HashMap<StateAction<S, A>, Double>()
    override val pi = HashMap<S, ProbabilityDistribution<A>>()

    /**
     * Memorize all returns for a StateAction (Q-value)
     */
    val returns = HashMap<StateAction<S, A>, ArrayList<Double>>()

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
    override fun sampleActionFromState(state: S): A {
        return getOrCreatePolicyForState(state).sample()
    }

    /**
     * Update the policy based on returns.
     *
     * Follows Sutton's On-Policy first-visit MC control algorithm for epsilon-soft policies in his
     * Reinforcement Learning book in Section 5.4
     */
    fun improvePolicy(trajectory: Trajectory<S, A>) {

        trajectory.reversed().fold(0.0) { successorReturn, visit ->
            val accumulatedReturn = gamma * successorReturn + visit.reward

            val sa = StateAction(visit.state.clone(), visit.action)

            if (!visit.isFirstVisit) {
                return@fold accumulatedReturn
            }

            returns.getOrPut(sa) { arrayListOf() }.add(accumulatedReturn)

            // strict average over all returns
            q[sa] = returns[sa]!!.average()

            // alpha
            if (alpha < 0.0) {
                alpha = 1.0 / returns[sa]!!.size
            }

            q[sa] = (q[sa] ?: 0.0) + alpha * (accumulatedReturn - (q[sa] ?: 0.0))

            // Look for all state actions with a particular state
            val maxEntry = q.entries.filter { it.key.state == sa.state }.maxBy { it.value }!!
            val maxAction = maxEntry.key.action

            val policy = getOrCreatePolicyForState(sa.state)

            policy.probabilities.forEach {
                if (it.item == maxAction) {
                    it.weight = 1.0 - epsilon + (epsilon / actionsForState(sa.state).size)
                } else {
                    it.weight = epsilon / actionsForState(sa.state).size
                }
            }
            policy.normalize()

            accumulatedReturn
        }
    }
}