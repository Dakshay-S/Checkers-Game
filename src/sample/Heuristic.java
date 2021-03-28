package sample;

public class Heuristic {

    static int getEvaluation(State state, Player player)
    {
        return pieceRowHeuristic(state, player);
    }



    private static int pieceRowHeuristic(State state, Player max_player)
    {
        int heuristic = 0;
        final int PAWN_POINT = 30;
        final int KING_POINT = PAWN_POINT*2 + State.N;

        for (int i = 0; i <State.N ; i++) {
            for (int j = 0; j < State.N; j++) {

                if(state.isEmpty(i,j))
                    continue;

                if(state.getPieceType(i,j) == PieceType.NORMAL_WHITE)
                    heuristic += (PAWN_POINT + i) * (max_player == Player.WHITE? 1: -1);

                else if(state.getPieceType(i,j) == PieceType.NORMAL_BLACK)
                    heuristic += (PAWN_POINT + State.N-i) * (max_player == Player.BLACK? 1: -1);

                else if(state.getPieceType(i,j) == PieceType.KING_BLACK)
                    heuristic += KING_POINT * (max_player == Player.BLACK? 1: -1);

                else if(state.getPieceType(i,j) == PieceType.KING_WHITE)
                    heuristic += KING_POINT * (max_player == Player.WHITE? 1: -1);

            }
        }

        return heuristic;
    }

}
