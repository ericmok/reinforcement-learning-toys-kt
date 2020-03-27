/**
 * Temporal Difference Episode / Training Runner
 */
class QLearningRunner(var raceTrack: RaceTrack, var raceCar: RaceCar) {

    /**
     * A max is required because termination via random walk within a finite time span is NOT guaranteed.
     * This is one of the drawbacks between Monte Carlo methods and online learning methods like Sarsa.
     */
    var maxRunTimeStepsInEpisode = 10000

    /**
     * Stores one episode's trajectory. Should get cleared after each episode, using reset()
     */
    var trajectory = ArrayList<Visit<RaceTrackState, RaceTrackAction>>()

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
        println(raceTrack.drawTrajectoryString(trajectory.reversed()))
        println("Trajectory: ${trajectory.size} Steps")
        println("")

        if (raceTrack.isTerminatingState(trajectory.last().state)) {
            println("WIN!")
        }

        for (racetrackAction in RACETRACK_ACTIONS) {
            println(raceCar.q.getOrDefault(StateAction(raceTrack.startingStates[0], racetrackAction), 0.0))
        }

        println()

        println("epsilon: ${raceCar.epsilon}")
        println("gamma: ${raceCar.gamma}")
        println("alpha: ${raceCar.alpha}")

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

            raceCar.improvePolicyWithQLearning(statePointer, action, nextStateSample)

            statePointer = nextStateSample.state.clone()
        }
    }
}


fun main() {
    var runner = QLearningRunner(RaceTrack(), RaceCar(epsilon = 0.3, alpha = 0.5))

    for (i in 0..800) {

        if (i == 6400) {
            println("================================")
            println("Here we begin ramping down epsilon to move towards greedy")
        }

        runner.raceCar.alpha *= 0.9995

        // After some episodes, ramp down epsilon every 10 episodes to move towards greedy policy!
        if (i >= 6400 && i % 50 == 0) {
            runner.raceCar.epsilon *= 0.98
            //runner.raceCar.alpha *= 0.99
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