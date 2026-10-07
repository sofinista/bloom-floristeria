package com.floristeria.bloom.utilidades;

public final class Validaciones {

    private Validaciones() {
    }

    public static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    public static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}