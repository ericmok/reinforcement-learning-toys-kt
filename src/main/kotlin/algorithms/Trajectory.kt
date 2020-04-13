class Trajectory<S: State, A: Action> {
    /**
     * Used to keep track of which visits were first to a step within an episode.
     * Should get cleared after playing an episode using reset()
     */
    val firstVisit = hashSetOf<S>()

    /**
     * Stores one episode's trajectory. Should get cleared after each episode, using reset()
     */
    val list = ArrayList<Visit<S, A>>()

    fun clear() {
        list.clear()
        firstVisit.clear()
    }

    fun add(state: S, action: A, reward: Double) {
        val visit = Visit(state.clone(), action, reward)
        list.add(visit)

        if (!firstVisit.contains(state)) {
            firstVisit.add(state)
            visit.isFirstVisit = true
        }
    }

    val size: Int
        get() {
            return list.size
        }

    fun last(): Visit<S, A> {
        return list.last()
    }

    fun reversed(): List<Visit<S, A>> {
        return list.reversed()
    }
}