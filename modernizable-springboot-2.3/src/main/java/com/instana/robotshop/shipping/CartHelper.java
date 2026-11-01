package com.instana.robotshop.shipping;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CartHelper {
    private static final Logger logger = LoggerFactory.getLogger(CartHelper.class);

    private String baseUrl;

    public CartHelper(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String addToCart(String id, String data) {
        logger.info("add shipping to cart {}", id);

        try (HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build()) {
            HttpRequest postRequest = HttpRequest.newBuilder(URI.create(baseUrl + id))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(data))
                    .build();
            HttpResponse<String> res = httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() == 200) {
                return res.body();
            }
            logger.warn("Failed with code {}", res.statusCode());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("http client interrupted", e);
        } catch (Exception e) {
            logger.warn("http client exception", e);
        }

        // this will be empty on error
        return "";
    }
}
