class RacecarMonteCarloAgent(gamma: Double = 1.0,
                             epsilon: Double = 0.5):
    MonteCarloAgent<RaceTrackState, RaceTrackAction>(gamma = gamma, epsilon = epsilon) {

    override fun actionsForState(state: RaceTrackState): Set<RaceTrackAction> {
        return setOf(*RACETRACK_ACTIONS)
    }
}

class RacecarQLearningAgent(gamma: Double = 1.0,
                            epsilon: Double = 0.5,
                            alpha: Double = 0.1):
    QLearningAgent<RaceTrackState, RaceTrackAction>(gamma = gamma, epsilon = epsilon, alpha = alpha) {

    override fun actionsForState(state: RaceTrackState): Set<RaceTrackAction> {
        return setOf(*RACETRACK_ACTIONS)
    }
}

class RacecarSarsaAgent(gamma: Double = 1.0,
                        epsilon: Double = 0.5,
                        alpha: Double = 0.1):
    SarsaAgent<RaceTrackState, RaceTrackAction>(gamma = gamma, epsilon = epsilon, alpha = alpha) {

    override fun actionsForState(state: RaceTrackState): Set<RaceTrackAction> {
        return setOf(*RACETRACK_ACTIONS)
    }
}