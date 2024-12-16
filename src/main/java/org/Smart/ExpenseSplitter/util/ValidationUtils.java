package org.Smart.ExpenseSplitter.util;

import java.util.List;
import java.util.stream.Collectors;

import org.Smart.ExpenseSplitter.dto.JsonResponse;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

public class ValidationUtils {
    public static List<String> generateValidationErrors(BindingResult bindingResult) {
        return bindingResult.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
    }

    public static ResponseEntity<JsonResponse> generateValidationResponse(
            BindingResult bindingResult) {
        return new ResponseEntity<>(
                new JsonResponse(false, "validation error", generateValidationErrors(bindingResult)),
                HttpStatus.FORBIDDEN);
    }
}
