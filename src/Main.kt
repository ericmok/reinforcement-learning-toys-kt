/**
 * An example of using a runner via step functions
 */
fun stepWiseSarsa() {
    var runner = SarsaRunner(RaceTrack(), RacecarSarsaAgent(epsilon = 0.5))

    for (i in 0..1000) {
        runner.start()
        while (runner.canStillStep()) {
            runner.step()
        }
        if (i % 100 == 0) {
            print("======= Episode $i =====")
            runner.printStats()
        }
        runner.end()
    }
}

fun doMonteCarloExperiment() {
    var runner = MonteCarloRunner(RaceTrack(), RacecarMonteCarloAgent(epsilon = 0.5))

    for (i in 0..8_000) {

        if (i == 6400) {
            println("================================")
            println("Here we begin ramping down epsilon to move towards greedy")
        }

        // After some episodes, ramp down epsilon every 10 episodes to move towards greedy policy!
        if (i >= 6400 && i % 50 == 0) {
            runner.agent.epsilon *= 0.95
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
    }
}

fun doQLearningExperiment() {
    var runner = QLearningRunner(RaceTrack(), RacecarQLearningAgent(epsilon = 0.3, alpha = 0.5))

    for (i in 0..6400) {
        runner.agent.alpha *= 0.9995

        // After some episodes, ramp down epsilon every 10 episodes to move towards greedy policy!
        if (i >= 4800 && i % 50 == 0) {
            runner.agent.epsilon *= 0.98
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
    }
}

fun doSarsaExperiment() {
    var runner = SarsaRunner(RaceTrack(), RacecarSarsaAgent(epsilon = 0.5))

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
    }
}

fun main() {
    var choice: Int = 0

    while (choice != 4) {
        println("Choose a number to run an experiment:")
        println("1) Monte Carlo")
        println("2) SARSA")
        println("3) Q Learning")
        println("4) Quit")
        println()
        print("Option [1-4] >> ")
        val input = readLine()
        choice = input?.toInt() ?: 0

        when (choice) {
            1 -> doMonteCarloExperiment()
            2 -> stepWiseSarsa()//doSarsaExperiment()
            3 -> doQLearningExperiment()
            0 -> println("Invalid choice")
        }
    }

    println("Thanks for running me :D")
}