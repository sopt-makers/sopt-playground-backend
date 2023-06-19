package org.sopt.makers.internal.exception;

public class WordChainGameHasWrongInputException extends BusinessLogicException {

    public WordChainGameHasWrongInputException(String message) {
        super("[WordChainGameHasWrongInputException] : " + message);
    }
}
