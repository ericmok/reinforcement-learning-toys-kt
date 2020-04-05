/**
 * Monte Carlo Episode / Training Runner
 */
class MonteCarloRunner<S: State, A: Action>(var environment: Environment<S, A>, var agent: MCAgent<S, A>) {

    /**
     * A max is required because termination via random walk within a finite time span is NOT guaranteed.
     * This is one of the drawbacks between Monte Carlo methods and online learning methods like Sarsa.
     */
    var maxRunTimeStepsInEpisode = 10000

    /**
     * Used to keep track of which visits were first to a step within an episode.
     * Should get cleared after playing an episode using reset()
     */
    var firstVisit = hashSetOf<S>()

    /**
     * Stores one episode's trajectory. Should get cleared after each episode, using reset()
     */
    var trajectory = ArrayList<Visit<S, A>>()

    /**
     * Resets trajectory and firstVisit arrays, used for policy improvement
     */
    fun reset() {
        trajectory.clear()
        firstVisit.clear()
    }

    /**
     * Prints the trajectory taken on the track as well as other info
     */
    open fun printStats() {
        println()
        println(environment.getDrawTrajectoryString(trajectory.reversed()))
        println("Trajectory: ${trajectory.size} Steps")
        println()

        if (environment.isTerminatingState(trajectory.last().state)) {
            println("Termination found!")
        }

        println("epsilon: ${agent.epsilon}")
        println("gamma: ${agent.gamma}")
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

            if (!firstVisit.contains(statePointer)) {
                firstVisit.add(statePointer)
                visit.isFirstVisit = true
            }

            statePointer = nextStateSample.state.clone()
        }

        agent.improvePolicyWithMonteCarlo(trajectory)
    }
}