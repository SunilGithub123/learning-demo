package com.learningdemo.oauth2clientcredentials.web;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sample protected API. Reading requires the orders.read scope; creating an
 * order requires orders.write, so a client with only orders.read (see
 * "readonly-client" in application.yml) can prove out a 403 from this same
 * endpoint set.
 */
@RestController
public class OrdersController {

    private static final List<Map<String, Object>> ORDERS = List.of(
            Map.of("id", 1, "item", "Mechanical keyboard", "quantity", 1),
            Map.of("id", 2, "item", "USB-C dock", "quantity", 2));

    @GetMapping("/api/orders")
    @PreAuthorize("hasAuthority('SCOPE_orders.read')")
    public List<Map<String, Object>> listOrders() {
        return ORDERS;
    }

    @PostMapping("/api/orders")
    @PreAuthorize("hasAuthority('SCOPE_orders.write')")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createOrder(@RequestBody Map<String, Object> order) {
        return Map.of("id", 3, "item", order.getOrDefault("item", "unknown"), "quantity", order.getOrDefault("quantity", 1));
    }
}
