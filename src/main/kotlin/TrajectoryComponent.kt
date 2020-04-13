import kotlinx.css.*
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import styled.css
import styled.inlineStyles

external interface TrajectoryComponentProps<S: State, A: Action>: RProps {
    var list: ArrayList<Visit<RaceTrackState, RaceTrackAction>>
    var monteCarloAgent: MonteCarloAgent<S, A>
}

class TrajectoryComponent: RComponent<TrajectoryComponentProps<RaceTrackState, RaceTrackAction>, RState>() {

    override fun RBuilder.render() {
        val overlayedList = hashMapOf<RaceTrackState, RaceTrackAction>()
        for (visit in props.list) {
            overlayedList.put(visit.state, visit.action)
        }

        for ((agentState, action) in overlayedList) {
            child(Trace::class) {
                attrs.action = action
                attrs.agentState = agentState
                attrs.monteCarloAgent = props.monteCarloAgent
            }
        }
    }
}
