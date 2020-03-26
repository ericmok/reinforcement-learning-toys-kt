/**
 * Monte Carlo Episode / Training Runner
 */
class Runner(var raceTrack: RaceTrack, var raceCar: RaceCar) {

    /**
     * A max is required because termination via random walk within a finite time span is NOT guaranteed.
     * This is one of the drawbacks between Monte Carlo methods and online learning methods like Sarsa.
     */
    var maxRunTimeStepsInEpisode = 10000

    /**
     * Used to keep track of which visits were first to a step within an episode.
     * Should get cleared after playing an episode using reset()
     */
    var firstVisit = hashSetOf<State>()

    /**
     * Stores one episode's trajectory. Should get cleared after each episode, using reset()
     */
    var trajectory = ArrayList<Visit>()

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
    fun printStats() {
        println("")
        println(raceTrack.drawTrajectoryString(trajectory.reversed()))
        println("Trajectory: ${trajectory.size} Steps")
        println("")

        if (raceTrack.isTerminatingState(trajectory.last().state)) {
            println("WIN!")
        }

        val averages = ArrayList<Double>()
        for (startingState in raceTrack.startingStates) {
            for (action in Action.values()) {
                //println("${startingState} ${action} reward: ")
                averages.add(raceCar.returns[StateAction(startingState, action)]?.average() ?: -Double.NEGATIVE_INFINITY)
            }
        }

        println("Max starting state return:")
        println("${averages.max()}")

        println()

        println("epsilon: ${raceCar.epsilon}")
        println("gamma: ${raceCar.gamma}")

        println()
    }

    /**
     * Run one episode yielding a trajectory. Also runs policy improvement algorithm
     */
    fun runOneEpisode() {
        var maxTime = maxRunTimeStepsInEpisode

        var statePointer = raceTrack.getRandomStartingState()

        while (!raceTrack.isTerminatingState(statePointer) && maxTime > 1) {
            maxTime -= 1

            val action = raceCar.sampleActionFromState(statePointer)
            val nextStateSample = raceTrack.sampleNextStateFromStateAction(statePointer, action)

            val visit = Visit(statePointer, action, nextStateSample.reward)

            trajectory.add(visit)

            if (!firstVisit.contains(statePointer)) {
                firstVisit.add(statePointer)
                visit.isFirstVisit = true
            }

            statePointer = nextStateSample.state.clone()
        }

        raceCar.improvePolicy(trajectory)
    }
}


fun main() {
    var runner = Runner(RaceTrack(), RaceCar(epsilon = 0.6))

    for (i in 0..8_000) {

        if (i == 6400) {
            println("================================")
            println("Here we begin ramping down epsilon to move towards greedy")
        }

        // After some episodes, ramp down epsilon every 10 episodes to move towards greedy policy!
        if (i >= 6400 && i % 50 == 0) {
            runner.raceCar.epsilon *= 0.9
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