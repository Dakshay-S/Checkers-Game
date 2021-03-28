package sample;

import java.util.List;

public class MinMax {

    final static Player MAX = Player.WHITE;
    final static Player MIN = Player.BLACK;
    final static int MAX_DEPTH = 7;

    int alpha = Integer.MAX_VALUE;
    int beta = Integer.MIN_VALUE;
    MinMax parent ;

    public MinMax(MinMax parent) {
        this.parent = parent;
    }


    public int getParentAlpha() {
        return parent == null ? Integer.MAX_VALUE : parent.alpha;
    }

    public int getParentBeta() {
        return parent == null ? Integer.MIN_VALUE : parent.beta;
    }

}

class MAX_Player extends MinMax {

    public MAX_Player(MinMax parent) {
        super(parent);
    }

    IntermediateState solveForMax(IntermediateState given, int depth, Player currPlayer , Player max_player) throws CloneNotSupportedException {

        if (depth == 0) {
            this.beta = Heuristic.getEvaluation(given.state, max_player);
            return given;
        }

        List<IntermediateState> nextStates = given.getAllPossibleNextStatesForTurn(currPlayer);
        IntermediateState nextBestState = null;

        for(IntermediateState state: nextStates)
        {
            // BETA PRUNING
            if(this.beta >= this.getParentAlpha())
                break;


            MIN_Player MIN = new MIN_Player(this);
            MIN.solveForMin(state, depth-1, currPlayer == Player.WHITE? Player.BLACK : Player.WHITE , max_player);

            if(MIN.alpha > this.beta) {
                nextBestState = state;
                this.beta = MIN.alpha;
            }
        }

        return nextBestState;
    }
}


class MIN_Player extends MinMax
{
    public MIN_Player(MinMax parent) {
        super(parent);
    }

    IntermediateState solveForMin(IntermediateState given, int depth, Player currPlayer , Player max_player) throws CloneNotSupportedException
    {
        if (depth == 0) {
            this.alpha = Heuristic.getEvaluation(given.state, max_player);
            return given;
        }

        List<IntermediateState> nextStates = given.getAllPossibleNextStatesForTurn(currPlayer);
        IntermediateState nextBestState = null;

        for(IntermediateState state: nextStates)
        {
            // ALPHA PRUNING
            if(this.alpha <= this.getParentBeta())
                break;


            MAX_Player MAX = new MAX_Player(this);
            MAX.solveForMax(state, depth-1, currPlayer == Player.WHITE? Player.BLACK: Player.WHITE, max_player);

            if(MAX.beta < this.alpha) {
                nextBestState = state;
                this.alpha = MAX.beta;
            }
        }

        return nextBestState;
    }
}
