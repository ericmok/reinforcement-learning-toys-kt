/**
 * Monte Carlo Episode / Training Runner
 */
class MonteCarloRunner<S: State, A: Action>(var environment: Environment<S, A>, var agent: MonteCarloAgent<S, A>) {

    /**
     * Current state for agent when  start(), step(), stop() methods are used
     */
    var currentState: S = environment.restartForNextEpisode()

    /**
     * A max is required because termination via random walk within a finite time span is NOT guaranteed.
     * This is one of the drawbacks between Monte Carlo methods and online learning methods like Sarsa.
     */
    var maxRunTimeStepsInEpisode = 10000

    /**
     * Stores data from current / last episode. This will be used for the Monte Carlo Agent to learn from
     */
    val trajectory: Trajectory<S, A> = Trajectory()

    /**
     * Prints the trajectory taken on the track as well as other info
     */
    open fun printStats() {
        println()
        println(environment.getDrawTrajectoryString(trajectory.list.reversed()))
        println("Trajectory: ${trajectory.list.size} Steps")
        println()

        if (environment.isTerminatingState(trajectory.list.last().state)) {
            println("Termination found!")
        }

        println("epsilon: ${agent.epsilon}")
        println("gamma: ${agent.gamma}")
        println()
    }

    /**
     * Start a new episode to step through. Trajectory is cleared and current state is initialized.
     */
    fun start() {
        trajectory.clear()
        currentState = environment.restartForNextEpisode(currentState)
    }

    /**
     * Take next step through current episode by having agent act in environment.
     * Should call start() first before calling this.
     * Call canStillStep() before stepping to see if you can still step
     */
    fun step() {
        val action = agent.sampleActionFromState(currentState)
        val nextStateSample = environment.sampleNextStateFromStateAction(currentState, action)
        trajectory.add(currentState, action, nextStateSample.reward)
        currentState = nextStateSample.state.clone()
    }

    /**
     * @return If current state in current episode is in a terminating state in environment
     */
    fun canStillStep(): Boolean {
        return !environment.isTerminatingState(currentState)
    }

    /**
     * Ends the current episode, calling policy improvement algorithm
     */
    fun end() {
        agent.improvePolicy(trajectory)
    }

    /**
     * Run one episode yielding a trajectory. Also runs policy improvement algorithm
     */
    fun runOneEpisode() {
        trajectory.clear()
        var maxTime = maxRunTimeStepsInEpisode

        var statePointer = environment.restartForNextEpisode()

        while (!environment.isTerminatingState(statePointer) && maxTime > 1) {
            maxTime -= 1

            val action = agent.sampleActionFromState(statePointer)
            val nextStateSample = environment.sampleNextStateFromStateAction(statePointer, action)

            trajectory.add(statePointer, action, nextStateSample.reward)

            statePointer = nextStateSample.state.clone()
        }

        agent.improvePolicy(trajectory)
    }
}