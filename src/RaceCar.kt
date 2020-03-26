/**
 * Agent for the race track
 */
class RaceCar(var gamma: Double = 1.0, var epsilon: Double = 0.5): MCAgent<RaceTrackState, RaceTrackAction> {

    /// Q Values
    private val q = HashMap<StateAction<RaceTrackState, RaceTrackAction>, Double>()

    /// Policy(state) -> Probability Distribution for action to be taken
    private val pi = HashMap<RaceTrackState, ProbabilityDistribution<RaceTrackAction>>()

    /// Memorize all returns for a StateAction (Q-value)
    val returns = HashMap<StateAction<RaceTrackState, RaceTrackAction>, ArrayList<Double>>()

    /**
     * Get probability of action from state.
     * Initialize policy to random if first time
     */
    fun getOrCreatePolicyForState(raceTrackState: RaceTrackState): ProbabilityDistribution<RaceTrackAction> {
        return pi.getOrPut(raceTrackState) {
            val newProbs = ProbabilityDistribution<RaceTrackAction>()
            newProbs.setEvents(*RACETRACK_ACTIONS)
            newProbs.normalize()
            newProbs
        }
    }

    override fun actionsForState(state: RaceTrackState): Set<RaceTrackAction> {
        return setOf(*RACETRACK_ACTIONS)
    }

    /**
     * Get an action from internal policy
     */
    override fun sampleActionFromState(state: RaceTrackState): RaceTrackAction {
        val actionProbs = getOrCreatePolicyForState(state)
        return actionProbs.sample()
    }

    /**
     * Update the policy based on returns.
     *
     * Follows Sutton's On-Policy first-visit MC control algorithm for epsilon-soft policies in his
     * Reinforcement Learning book in Section 5.4
     */
    override fun improvePolicy(trajectory: Collection<Visit<RaceTrackState, RaceTrackAction>>) {

        trajectory.reversed().fold(0.0) { successorReturn, visit ->
            val accumulatedReturn = gamma * successorReturn + visit.reward

            val sa = StateAction(RaceTrackState.fromVisit(visit), visit.action)

            if (!visit.isFirstVisit) {
                return@fold accumulatedReturn
            }

            returns.getOrPut(sa) {
                arrayListOf()
            }.add(accumulatedReturn)

            q[StateAction(RaceTrackState.fromVisit(visit), visit.action)] = returns[sa]!!.average()

            // Look for all state actions with a particular state
            val maxEntry = q.entries.filter { it.key.state == sa.state }.maxBy { it.value }!!
            val maxAction = maxEntry.key.action

            val policy = getOrCreatePolicyForState(sa.state)

            policy.probabilities.forEach {
                if (it.item == maxAction) {
                    it.weight = 1.0 - epsilon + (epsilon / RACETRACK_ACTIONS.size)
                } else {
                    it.weight = epsilon / RACETRACK_ACTIONS.size
                }
            }
            policy.normalize()

            accumulatedReturn
        }
    }
}