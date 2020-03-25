/**
 * Monte Carlo Episode / Training Runner
 */
class Runner(var raceTrack: RaceTrack, var raceCar: RaceCar) {

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

        for (startingState in raceTrack.startingStates) {
            for (action in Action.values()) {
                println("${startingState} ${action} reward: ")
                println(raceCar.returns[StateAction(startingState, action)]?.average() ?: "0.0")
            }
        }

        println()

        println("epsilon: ${raceCar.epsilon}")
        println("gamma: ${raceCar.gamma}")

        println()
    }

    /**
     * Run one episode yielding a trajectory. Also runs policy improvement algorithm
     */
    fun runOneEpisode() {
        var statePointer = raceTrack.getRandomStartingState()
        var maxTime = 10000

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
    for (i in 0..10_000) {

        // After episode 8000, ramp down epsilon every 10 episodes to move towards greedy policy!
        if (i >= 8000 && i % 10 == 0) {
            runner.raceCar.epsilon *= 0.95
        }

        runner.runOneEpisode()

        if (i % 1000 == 0) {
            println("============= EPISODE ${i} =====")
            runner.printStats()
        }

        runner.reset()
    }
}