/**
 * Temporal Difference Episode / Training Runner
 */
class SarsaRunner<S: State, A: Action>(override var environment: Environment<S, A>,
                                       override var agent: SarsaAgent<S, A>): GeneralRunner<S, A, StateAction<S, A>> {

    /**
     * A max is required because termination via random walk within a finite time span is NOT guaranteed.
     * This is one of the drawbacks between Monte Carlo methods and online learning methods like Sarsa.
     */
    val maxRunTimeStepsInEpisode = 10000

    var currentStateAction: StateAction<S, A> = getStartingStateAction()

    /**
     * Store trajectory for aesthetics
     */
    override val trajectory = Trajectory<S, A>()

//
//    /**
//     * Prints the trajectory taken on the track as well as other info
//     */
    fun printStats() {
        println("")
        println(environment.getDrawTrajectoryString(trajectory.reversed()))
        println("Trajectory: ${trajectory.size} Steps")
        println("")

        if (environment.isTerminatingState(trajectory.last().state)) {
            println("WIN!")
        }

        println()

        println("alpha: ${agent.alpha}")
        println("epsilon: ${agent.epsilon}")
        println("gamma: ${agent.gamma}")

        println()
    }

    private fun getStartingStateAction(): StateAction<S, A> {
        val startingState = environment.restartForNextEpisode()
        val startingAction = agent.sampleActionFromState(startingState)
        return StateAction(startingState, startingAction)
    }

    /**
     * Start a new episode to step through. Trajectory is cleared and current state is initialized.
     */
    override fun start() {
        trajectory.clear()
        currentStateAction = getStartingStateAction()
    }

    /**
     * Take next step through current episode by having agent act in environment.
     * Runs Q-Learning algorithm from immediate experience of the step
     * Should call start() first before calling this.
     * Call canStillStep() before stepping to see if you can still step
     */
    override fun step() {
        val nextStateSample = environment.sampleNextStateFromStateAction(currentStateAction.state, currentStateAction.action)
        val nextAction = agent.sampleActionFromState(nextStateSample.state)

        trajectory.add(currentStateAction.state, currentStateAction.action, nextStateSample.reward)

        agent.improvePolicy(currentStateAction.state, currentStateAction.action, nextStateSample, nextAction)

        currentStateAction = StateAction(nextStateSample.state.clone(), nextAction)
    }

    /**
     * @return If current state in current episode is in a terminating state in environment
     */
    override fun canStillStep(): Boolean {
        return !environment.isTerminatingState(currentStateAction.state)
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
        var action = agent.sampleActionFromState(statePointer)

        while (!environment.isTerminatingState(statePointer) && maxTime > 1) {
            maxTime -= 1

            val nextStateSample = environment.sampleNextStateFromStateAction(statePointer, action)
            val nextAction = agent.sampleActionFromState(nextStateSample.state)

            trajectory.add(statePointer, nextAction, nextStateSample.reward)

            agent.improvePolicy(statePointer, action, nextStateSample, nextAction)

            statePointer = nextStateSample.state.clone()
            action = nextAction
        }

    }

    override fun currentPosition(): StateAction<S, A> {
        return currentStateAction
    }
}