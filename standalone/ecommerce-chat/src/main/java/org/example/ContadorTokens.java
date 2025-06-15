package org.example;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.ModelType;

import java.math.BigDecimal;

public class ContadorTokens {

    public static void main(String[] args) {


        var qtc = contadorTokens("Hola, ¿cómo estás?");

        System.out.println("Quantidade de tokens: " + qtc);
    var custo = new BigDecimal(qtc)
                .divide(new BigDecimal("1000"))
            .multiply(new BigDecimal("0.00010"));

    System.out.println("Custo: " + custo.toPlainString() + " USD");





    }

    public static int contadorTokens(String message) {
        var registry = Encodings.newDefaultEncodingRegistry();
        var enc = registry.getEncodingForModel(ModelType.GPT_3_5_TURBO);

        var qtc = enc.countTokens(message);
        return qtc;
    }


}
