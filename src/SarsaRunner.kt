/**
 * Temporal Difference Episode / Training Runner
 */
class SarsaRunner<S: State, A: Action>(var environment: Environment<S, A>, var agent: SarsaAgent<S, A>) {

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
            println("WIN!")
        }

        println()

        println("alpha: ${agent.alpha}")
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
        var action = agent.sampleActionFromState(statePointer)

        while (!environment.isTerminatingState(statePointer) && maxTime > 1) {
            maxTime -= 1

            val nextStateSample = environment.sampleNextStateFromStateAction(statePointer, action)
            val nextAction = agent.sampleActionFromState(nextStateSample.state)

            val visit = Visit(statePointer, action, nextStateSample.reward)
            trajectory.add(visit)

            agent.improvePolicyWithSarsa(statePointer, action, nextStateSample, nextAction)

            statePointer = nextStateSample.state.clone()
            action = nextAction
        }

    }
}


fun main() {
    var runner = SarsaRunner(RaceTrack(), RaceCar(epsilon = 0.5))

    for (i in 0..8_000) {

        if (i == 6400) {
            println("================================")
            println("Here we begin ramping down epsilon to move towards greedy")
        }

        // After some episodes, ramp down epsilon every 10 episodes to move towards greedy policy!
        if (i >= 6400 && i % 50 == 0) {
            runner.agent.epsilon *= 0.9
        }


        runner.runOneEpisode()


        if (i < 100 && i % 20 == 0) {
            println("============= EPISODE ${i} =====")
            runner.printStats()
        }

        if (i % 800 == 0 && i != 0) {
            println("============= EPISODE ${i} =====")
            runner.printStats()
        }

        runner.reset()
    }
}