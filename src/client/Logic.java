package client;

import artificialplayer.AlphaBeta;
import datastructures.BitBoard;
import game.GameColor;
import game.GameDirection;
import game.GameMove;
import game.MyGameState;
import helpers.FEN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sc.framework.plugins.Player;
import sc.plugin2019.*;
import sc.plugin2019.util.GameRuleLogic;
import sc.shared.GameResult;
import sc.shared.PlayerColor;

import java.util.ArrayList;

/**
 * Das Herz des Clients:
 * Eine sehr simple Logik, die ihre Zuege zufaellig waehlt,
 * aber gueltige Zuege macht. Ausserdem werden zum Spielverlauf
 * Konsolenausgaben gemacht.
 */
public class Logic implements IGameHandler {

    private Starter client;
    private GameState gameState;
    private Player currentPlayer;

    private static final Logger log = LoggerFactory.getLogger(Logic.class);

    /**
     * Erzeugt ein neues Strategieobjekt, das zufaellige Zuege taetigt.
     *
     * @param client Der zugrundeliegende Client, der mit dem Spielserver
     *               kommuniziert.
     */
    public Logic(Starter client) {
        this.client = client;
    }

    /**
     * {@inheritDoc}
     */
    public void gameEnded(GameResult data, PlayerColor color, String errorMessage) {
        log.info("Das Spiel ist beendet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRequestAction() {
        long startTime = System.currentTimeMillis();
        log.info("Es wurde ein Zug angefordert.");
        ArrayList<Move> possibleMoves = GameRuleLogic.getPossibleMoves(gameState);
        //MyGameState mg = new MyGameState(, , , , , );
        BitBoard roteFische = new BitBoard(0, 0);
        BitBoard blaueFische = new BitBoard(0, 0);
        BitBoard kraken = new BitBoard(0, 0);
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                Field f = this.gameState.getField(x, y);
                FieldState fs = f.getState();
                int shift = y * 10 + 9 - x;
                if (fs == FieldState.BLUE) {
                    blaueFische.orEquals(new BitBoard(0, 1).leftShift(shift));
                } else if (fs == FieldState.RED) {
                    roteFische.orEquals(new BitBoard(0, 1).leftShift(shift));
                } else if (fs == FieldState.OBSTRUCTED) {
                    kraken.orEquals(new BitBoard(0, 1).leftShift(shift));
                }
            }

        }
        GameColor player;
        if (this.currentPlayer.getColor() == PlayerColor.RED) {
            player = GameColor.RED;
        } else {
            player = GameColor.BLUE;
        }
        MyGameState mg = new MyGameState(roteFische, blaueFische, kraken, player, this.gameState.getTurn(), this.gameState.getRound());
        log.info("FEN:\n" + FEN.toFEN(mg));
        GameMove m = AlphaBeta.alphaBetaRoot(mg, 3, mg.move == GameColor.RED ? 1 : -1);
        Direction resultDirection;
        if (m.dir == GameDirection.DOWN) {
            resultDirection = Direction.DOWN;
        } else if (m.dir == GameDirection.DOWN_LEFT) {
            resultDirection = Direction.DOWN_LEFT;
        } else if (m.dir == GameDirection.LEFT) {
            resultDirection = Direction.LEFT;
        } else if (m.dir == GameDirection.UP_LEFT) {
            resultDirection = Direction.UP_LEFT;
        } else if (m.dir == GameDirection.UP) {
            resultDirection = Direction.UP;
        } else if (m.dir == GameDirection.UP_RIGHT) {
            resultDirection = Direction.UP_RIGHT;
        } else if (m.dir == GameDirection.RIGHT) {
            resultDirection = Direction.RIGHT;
        } else {
            resultDirection = Direction.DOWN_RIGHT;
        }
        Move result = new Move(9 - m.from % 10, m.from / 10, resultDirection);
        //log.info("Turn: "+this.gameState.getTurn());
        //log.info("Round: "+this.gameState.getRound());
        //sendAction(possibleMoves.get((int) (Math.random() * possibleMoves.size())));
        sendAction(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUpdate(Player player, Player otherPlayer) {
        currentPlayer = player;
        log.info("Spielerwechsel: " + player.getColor());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUpdate(GameState gameState) {
        this.gameState = gameState;
        currentPlayer = gameState.getCurrentPlayer();
        log.info("Zug: {} Spieler: {}", gameState.getTurn(), currentPlayer.getColor());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendAction(Move move) {
        client.sendMove(move);
    }

}
