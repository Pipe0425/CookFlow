package io.bootify.cookflow.cook_flow_gestion_de_tareas.valid;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidNameValidator implements ConstraintValidator<ValidName, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;

        // Solo letras y espacios
        if (!value.matches("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$")) return false;

        // No permitir repeticiones de letras seguidas (ej: ppp)
        if (value.matches("^(.)\\1{2,}$")) return false;

        // No permitir nombres muy cortos
        if (value.trim().length() < 2) return false;

        // No permitir más de 3 palabras
        String[] words = value.trim().split("\\s+");
        if (words.length > 3) return false;

        // Evitar repeticiones de palabras (ej: "Pi Pi" o "Ca Ca")
        for (int i = 1; i < words.length; i++) {
            if (words[i].equalsIgnoreCase(words[i - 1])) {
                return false;
            }
        }

        // Evitar palabras de una sola letra (ej: "A B")
        for (String w : words) {
            if (w.length() == 1) {
                return false;
            }
        }

        return true;
    }
}
