package org.Smart.ExpenseSplitter.dto;

public record JsonResponse(boolean success, String message, Object data) {
    public JsonResponse(boolean success, String message) {
        this(success, message, null);
    }
}