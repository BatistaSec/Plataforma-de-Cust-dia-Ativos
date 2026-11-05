package com.custody.custody_service.domain.exceptions;

public class InvalidScaleException extends BusinessException {

    public InvalidScaleException(String message) {
        super(message);
    }

    public InvalidScaleException(int expectedScale, int actualScale) {
        super("Escala invalida: esperada no maximo " + expectedScale + " casas decimais, recebido " + actualScale);
    }
}
