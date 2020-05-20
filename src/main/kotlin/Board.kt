import kotlinx.html.style
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.style
import react.dom.svg

class BoardProps(var board: ArrayList<Array<Char>>,
                 var runner: GeneralRunner<RaceTrackState, RaceTrackAction, RaceTrackState>): RProps

class Board: RComponent<BoardProps, RState>() {

    override fun RBuilder.render() {
        val (dimX, dimY) = props.board.getDimensions()

        svg {
            attrs {
                this.attributes["xmlns"] = "http://www.w3.org/2000/svg"
                this.attributes["viewBox"] = "0 0 ${dimX * displayScaling} ${dimY * displayScaling}"
                //this.attributes["width"] = "1024"
            }

            for (y in 0 until dimY) {
                for (x in 0 until dimX) {
                    child(BoardCell::class) {
                        attrs {
                            this.idx = x
                            this.idy = y
                            this.character = props.board[y][x]
                            this.policy = null
//                            this.actionTaken = overlayedTrajectory[RaceTrackState(x, y)]
                        }
                    }
                }
            }

            child(BoardAgent::class) {
                attrs {
                    this.x = props.runner.currentPosition().x
                    this.y = props.runner.currentPosition().y
                }
            }
//
//            child(PolicyOverlay::class) {
//                attrs {
//                    this.environment = props.runner.environment as RaceTrack
//                    this.agent = props.runner.agent
//                }
//            }

            child(TrajectoryComponent::class) {
                attrs {
                    this.list = props.runner.trajectory.list
                    this.agent = props.runner.agent
                }
            }
        }
    }
}
