/**
 * Q Learning Runner
 */
class QLearningRunner<S: State, A: Action>(override var environment: Environment<S, A>,
                                           override var agent: QLearningAgent<S, A>): GeneralRunner<S, A> {

    /**
     * A max is required because termination via random walk within a finite time span is NOT guaranteed.
     * This is one of the drawbacks between Monte Carlo methods and online learning methods like Sarsa.
     */
    var maxRunTimeStepsInEpisode = 10000

    /**
     * Current state for agent when  start(), step(), stop() methods are used
     */
    override var currentState: S = environment.restartForNextEpisode()

    /**
     * Stores one episode's trajectory. Should get cleared after each episode, using reset()
     */
    override var trajectory = Trajectory<S, A>()

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
     * Start a new episode to step through. Trajectory is cleared and current state is initialized.
     */
    override fun start() {
        trajectory.clear()
        currentState = environment.restartForNextEpisode(currentState)
    }

    /**
     * Take next step through current episode by having agent act in environment.
     * Runs Q-Learning algorithm from immediate experience of the step
     * Should call start() first before calling this.
     * Call canStillStep() before stepping to see if you can still step
     */
    override fun step() {
        val action = agent.sampleActionFromState(currentState)
        val nextStateSample = environment.sampleNextStateFromStateAction(currentState, action)
        trajectory.add(currentState, action, nextStateSample.reward)
        currentState = nextStateSample.state.clone()
        agent.improvePolicy(currentState, action, nextStateSample)
    }

    /**
     * @return If current state in current episode is in a terminating state in environment
     */
    override fun canStillStep(): Boolean {
        return !environment.isTerminatingState(currentState)
    }

    /**
     * Does nothing, since agent learns during stepping
     */
    override fun end() {}


    /**
     * Run one episode yielding a trajectory. Also runs policy improvement algorithm
     */
    override fun runOneEpisode() {
        trajectory.clear()
        var maxTime = maxRunTimeStepsInEpisode

        var statePointer = environment.restartForNextEpisode()

        while (!environment.isTerminatingState(statePointer) && maxTime > 1) {
            maxTime -= 1

            val action = agent.sampleActionFromState(statePointer)
            val nextStateSample = environment.sampleNextStateFromStateAction(statePointer, action)

            trajectory.add(statePointer, action, nextStateSample.reward)

            agent.improvePolicy(statePointer, action, nextStateSample)

            statePointer = nextStateSample.state.clone()
        }
    }
}