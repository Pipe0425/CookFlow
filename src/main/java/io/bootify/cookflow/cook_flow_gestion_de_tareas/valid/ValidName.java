package io.bootify.cookflow.cook_flow_gestion_de_tareas.valid;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = ValidNameValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidName {
    String message() default "El nombre no parece válido.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}