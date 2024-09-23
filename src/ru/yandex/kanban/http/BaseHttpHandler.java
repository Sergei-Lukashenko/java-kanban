package ru.yandex.kanban.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BaseHttpHandler implements HttpHandler {

    public void handle(HttpExchange exchange) throws IOException {
        String httpMethod = exchange.getRequestMethod();
        switch (httpMethod) {
            case "GET":
                handleDefaultGet(exchange);
                break;
            case "DELETE":
                handleDefaultDelete(exchange);
                break;
            case "POST":
                handleDefaultPost(exchange);
                break;
            default:
                sendHttpStatus(exchange, 405);  // Method Not Allowed
        }
    }

    protected void handleDefaultGet(HttpExchange exchange) throws IOException {
        sendHttpStatus(exchange, 405);  // Method Not Allowed
    }

    protected void handleDefaultDelete(HttpExchange exchange) throws IOException {
        sendHttpStatus(exchange, 405);  // Method Not Allowed
    }

    protected void handleDefaultPost(HttpExchange exchange) throws IOException {
        sendHttpStatus(exchange, 405);  // Method Not Allowed
    }

    protected String readText(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    protected void sendText(HttpExchange exchange, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(200, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }

    protected void sendHttpStatus(HttpExchange exchange, int statusCode) throws IOException {
        exchange.sendResponseHeaders(statusCode, 0);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.close();
    }

    protected int parseIdFromPath(String path, String prefixToRemove) {
        return parseIdFromPath(path, prefixToRemove, "");
    }

    protected int parseIdFromPath(String path, String prefixToRemove, String postfixToRemove) {
        String pathId = path.replaceFirst(prefixToRemove, "");
        if (!postfixToRemove.isEmpty()) {
            pathId = pathId.replaceFirst(postfixToRemove, "");
        }
        try {
            return Integer.parseInt(pathId);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }
}
