package com.acadify;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Centralized response utility for normalized API responses.
 * All API responses follow the standard format:
 * {
 *   "success": boolean,
 *   "message": "...",
 *   "data": {...} or null
 * }
 */
public class ResponseUtil {
    
    /**
     * Send a successful response with data (200 OK)
     */
    public static void sendSuccess(HttpExchange exchange, String message, Object data) throws IOException {
        String jsonData = (data == null) ? "null" : data.toString();
        String json = buildResponse(true, message, jsonData);
        sendResponse(exchange, 200, json);
    }

    /**
     * Send a successful response without data (200 OK)
     */
    public static void sendSuccess(HttpExchange exchange, String message) throws IOException {
        sendSuccess(exchange, message, null);
    }

    /**
     * Send a created response (201 Created)
     */
    public static void sendCreated(HttpExchange exchange, String message, Object data) throws IOException {
        String jsonData = (data == null) ? "null" : data.toString();
        String json = buildResponse(true, message, jsonData);
        sendResponse(exchange, 201, json);
    }

    /**
     * Send a bad request error (400 Bad Request)
     */
    public static void sendBadRequest(HttpExchange exchange, String message) throws IOException {
        String json = buildResponse(false, message, "null");
        sendResponse(exchange, 400, json);
    }

    /**
     * Send an unauthorized error (401 Unauthorized)
     */
    public static void sendUnauthorized(HttpExchange exchange, String message) throws IOException {
        String json = buildResponse(false, message, "null");
        sendResponse(exchange, 401, json);
    }

    /**
     * Send a forbidden error (403 Forbidden)
     */
    public static void sendForbidden(HttpExchange exchange, String message) throws IOException {
        String json = buildResponse(false, message, "null");
        sendResponse(exchange, 403, json);
    }

    /**
     * Send a not found error (404 Not Found)
     */
    public static void sendNotFound(HttpExchange exchange, String message) throws IOException {
        String json = buildResponse(false, message, "null");
        sendResponse(exchange, 404, json);
    }

    /**
     * Send a method not allowed error (405 Method Not Allowed)
     */
    public static void sendMethodNotAllowed(HttpExchange exchange) throws IOException {
        String json = buildResponse(false, "Method not allowed", "null");
        sendResponse(exchange, 405, json);
    }

    /**
     * Send a conflict error (409 Conflict)
     */
    public static void sendConflict(HttpExchange exchange, String message) throws IOException {
        String json = buildResponse(false, message, "null");
        sendResponse(exchange, 409, json);
    }

    /**
     * Send an internal server error (500 Internal Server Error)
     */
    public static void sendServerError(HttpExchange exchange, String message) throws IOException {
        String json = buildResponse(false, message, "null");
        sendResponse(exchange, 500, json);
    }

    /**
     * DEPRECATED: Use specific methods instead (sendBadRequest, sendServerError, etc.)
     * Kept for backward compatibility during migration
     */
    @Deprecated
    public static void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        String json = buildResponse(false, message, "null");
        sendResponse(exchange, statusCode, json);
    }

    /**
     * DEPRECATED: Use sendSuccess with data parameter instead
     * Kept for backward compatibility during migration
     */
    @Deprecated
    public static void sendJson(HttpExchange exchange, int statusCode, String jsonBody) throws IOException {
        // For backward compatibility, wrap non-standard responses
        if (!jsonBody.contains("\"success\"")) {
            boolean isSuccess = statusCode >= 200 && statusCode < 300;
            String message = isSuccess ? "Operation successful" : "Operation failed";
            String wrappedJson = buildResponse(isSuccess, message, jsonBody);
            sendResponse(exchange, statusCode, wrappedJson);
        } else {
            sendResponse(exchange, statusCode, jsonBody);
        }
    }

    /**
     * Build the standard JSON response structure
     */
    private static String buildResponse(boolean success, String message, String data) {
        return "{" +
                "\"success\":" + success + "," +
                "\"message\":\"" + escapeJson(message) + "\"," +
                "\"data\":" + data +
                "}";
    }

    /**
     * Send the HTTP response
     */
    private static void sendResponse(HttpExchange exchange, int statusCode, String jsonBody) throws IOException {
        byte[] bytes = jsonBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    /**
     * Escape special characters for JSON
     */
    private static String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}
