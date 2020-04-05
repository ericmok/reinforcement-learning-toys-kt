/**
 * Q Learning Runner
 */
class QLearningRunner<S: State, A: Action>(var environment: Environment<S, A>, var agent: QLearningAgent<S, A>) {

    /**
     * A max is required because termination via random walk within a finite time span is NOT guaranteed.
     * This is one of the drawbacks between Monte Carlo methods and online learning methods like Sarsa.
     */
    var maxRunTimeStepsInEpisode = 10000

    /**
     * Stores one episode's trajectory. Should get cleared after each episode, using reset()
     */
    var trajectory = ArrayList<Visit<S, A>>()

    /**
     * Resets trajectory and firstVisit arrays, used for policy improvement
     */
    fun reset() {
        trajectory.clear()
    }

    /**
     * Prints the trajectory taken on the track as well as other info
     */
    fun printStats() {
        println("")
        println(environment.getDrawTrajectoryString(trajectory.reversed()))
        println("Trajectory: ${trajectory.size} Steps")
        println("")

        if (environment.isTerminatingState(trajectory.last().state)) {
            println("Termination was reached!")
        }

//        for (racetrackAction in RACETRACK_ACTIONS) {
//            println(raceCar.q.getOrDefault(StateAction(raceTrack.startingStates[0], racetrackAction), 0.0))
//        }

        println()

        println("epsilon: ${agent.epsilon}")
        println("gamma: ${agent.gamma}")
        println("alpha: ${agent.alpha}")

        println()
    }

    /**
     * Run one episode yielding a trajectory. Also runs policy improvement algorithm
     */
    fun runOneEpisode() {
        var maxTime = maxRunTimeStepsInEpisode

        var statePointer = environment.restartForNextEpisode()

        while (!environment.isTerminatingState(statePointer) && maxTime > 1) {
            maxTime -= 1

            val action = agent.sampleActionFromState(statePointer)
            val nextStateSample = environment.sampleNextStateFromStateAction(statePointer, action)

            val visit = Visit(statePointer, action, nextStateSample.reward)
            trajectory.add(visit)

            agent.improvePolicyWithQLearning(statePointer, action, nextStateSample)

            statePointer = nextStateSample.state.clone()
        }
    }
}