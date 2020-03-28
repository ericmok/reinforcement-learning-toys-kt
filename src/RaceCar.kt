/**
 * Agent for the race track
 */
class RaceCar(override var gamma: Double = 1.0,
              override var epsilon: Double = 0.5,
              override var alpha:Double = 0.1):
                MCAgent<RaceTrackState, RaceTrackAction>,
                QLearningAgent<RaceTrackState, RaceTrackAction>,
                SarsaAgent<RaceTrackState, RaceTrackAction> {

    /// Q Values
    override val q = HashMap<StateAction<RaceTrackState, RaceTrackAction>, Double>()

    /// Policy(state) -> Probability Distribution for action to be taken
    override val pi = HashMap<RaceTrackState, ProbabilityDistribution<RaceTrackAction>>()

    /// Memorize all returns for a StateAction (Q-value)
    override val returns = HashMap<StateAction<RaceTrackState, RaceTrackAction>, ArrayList<Double>>()

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
    override fun improvePolicyWithMonteCarlo(trajectory: Collection<Visit<RaceTrackState, RaceTrackAction>>) {

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
                    it.weight = 1.0 - epsilon + (epsilon / actionsForState(sa.state).size)
                } else {
                    it.weight = epsilon / actionsForState(sa.state).size
                }
            }
            policy.normalize()

            accumulatedReturn
        }
    }

    override fun improvePolicyWithQLearning(state: RaceTrackState, action: RaceTrackAction, nextStateSample: NextStateSample<RaceTrackState>) {
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

    override fun improvePolicyWithSarsa(
        state: RaceTrackState,
        action: RaceTrackAction,
        nextStateSample: NextStateSample<RaceTrackState>,
        nextAction: RaceTrackAction
    ) {
        val sa = StateAction(state, action)

        val delta = nextStateSample.reward + gamma * q.getOrDefault(StateAction(nextStateSample.state, nextAction), 0.0) - q.getOrPut(sa, { 0.0 })

        q[sa] = q[sa]!! + alpha * delta

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