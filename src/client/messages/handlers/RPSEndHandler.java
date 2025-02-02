package client.messages.handlers;

import shared.constants.ConsoleColors;
import shared.messages.handling.MessageHandler;
import shared.messages.model.server.RPSEnd;
import shared.utils.PrintingUtils;

public class RPSEndHandler implements MessageHandler<RPSEnd> {
    @Override
    public void handle(RPSEnd message) {
        if(message.winner() != null){
            PrintingUtils.printMessage("Game ended. Winner: " + message.winner() + ". Your opponent selected: " + message.opponentChoice(), ConsoleColors.GREEN);
        } else {
            PrintingUtils.printMessage("Game ended in a draw. You both selected: " + message.opponentChoice(), ConsoleColors.YELLOW);
        }
    }
}
