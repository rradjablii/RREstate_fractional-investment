package com.rrestate.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** End-to-end API tests against a real PostgreSQL (Flyway migrations + Hibernate validate). */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class ApiIT {
    @Container @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine");

    @LocalServerPort int port;
    final HttpClient http = HttpClient.newHttpClient();
    final JsonMapper json = JsonMapper.builder().build();

    record Res(int status, JsonNode body, HttpResponse<String> raw) {}

    Res call(String method, String path, String token, Object body, String... headers) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).header("Content-Type", "application/json");
        if (token != null) b.header("Authorization", "Bearer " + token);
        for (int i = 0; i < headers.length; i += 2) b.header(headers[i], headers[i + 1]);
        b.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
        HttpResponse<String> r = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
        return new Res(r.statusCode(), r.body().isBlank() ? null : json.readTree(r.body()), r);
    }

    String register(String email) throws Exception {
        Res r = call("POST", "/api/v1/auth/register", null, Map.of("firstName", "Test", "lastName", "User", "email", email, "password", "correct-horse-battery"));
        assertThat(r.status()).isEqualTo(201);
        return r.body().get("accessToken").asString();
    }

    String adminToken() throws Exception {
        Res r = call("POST", "/api/v1/auth/login", null, Map.of("email", "admin@test.example", "password", "Admin-password-123"));
        assertThat(r.status()).isEqualTo(200);
        return r.body().get("accessToken").asString();
    }

    Map<String, Object> propertyBody(String name, String target) {
        return Map.ofEntries(Map.entry("name", name), Map.entry("country", "Turkey"), Map.entry("city", "Antalya"), Map.entry("propertyType", "Apartment"),
                Map.entry("description", "Test property"), Map.entry("status", "OPEN"), Map.entry("currency", "USD"),
                Map.entry("acquisitionPrice", "100000"), Map.entry("expectedYieldPercent", "7.8"), Map.entry("minimumInvestment", "100"),
                Map.entry("targetRaise", target), Map.entry("investmentEnabled", true),
                Map.entry("financials", List.of(Map.of("label", "Rent", "amount", "9000", "basis", "PROJECTED"))),
                Map.entry("risks", List.of("Vacancy risk")), Map.entry("disclosures", List.of("Projections are not guarantees.")));
    }

    JsonNode createProperty(String admin, String name, String target) throws Exception {
        Res r = call("POST", "/api/v1/admin/properties", admin, propertyBody(name, target));
        assertThat(r.status()).as(String.valueOf(r.body())).isEqualTo(201);
        return r.body();
    }

    Res invest(String token, JsonNode property, String amount, String key) throws Exception {
        return call("POST", "/api/v1/investments", token, Map.of("opportunityId", property.get("opportunityId").asString(), "amount", amount, "currency", "USD"), "Idempotency-Key", key);
    }

    @Test
    void authFlowAndErrors() throws Exception {
        String email = "auth-" + UUID.randomUUID() + "@example.com";
        Res reg = call("POST", "/api/v1/auth/register", null, Map.of("firstName", "A", "lastName", "B", "email", email, "password", "correct-horse-battery"));
        assertThat(reg.status()).isEqualTo(201);
        String setCookie = reg.raw().headers().firstValue("Set-Cookie").orElseThrow();
        assertThat(setCookie).contains("HttpOnly").contains("SameSite=Strict").contains("Path=/api/v1/auth");

        assertThat(call("GET", "/api/v1/users/me", reg.body().get("accessToken").asString(), null).body().get("email").asString()).isEqualTo(email);
        assertThat(call("GET", "/api/v1/users/me", null, null).status()).isEqualTo(401);

        Res bad = call("POST", "/api/v1/auth/login", null, Map.of("email", email, "password", "wrong-password-xx"));
        assertThat(bad.status()).isEqualTo(401);
        assertThat(bad.body().get("errorCode").asString()).isEqualTo("INVALID_CREDENTIALS");

        Res weak = call("POST", "/api/v1/auth/register", null, Map.of("firstName", "A", "lastName", "B", "email", "x@example.com", "password", "short"));
        assertThat(weak.status()).isEqualTo(400);
        assertThat(weak.body().get("validationErrors").get(0).get("field").asString()).isEqualTo("password");

        assertThat(call("POST", "/api/v1/auth/register", null, Map.of("firstName", "A", "lastName", "B", "email", email, "password", "correct-horse-battery")).status()).isEqualTo(409);

        // refresh rotates the cookie token; the old one is then rejected
        String cookie = setCookie.split(";")[0];
        Res r1 = call("POST", "/api/v1/auth/refresh", null, null, "Cookie", cookie);
        assertThat(r1.status()).isEqualTo(200);
        assertThat(call("POST", "/api/v1/auth/refresh", null, null, "Cookie", cookie).status()).isEqualTo(401);
    }

    @Test
    void adminEndpointsAreProtectedServerSide() throws Exception {
        String user = register("plain-" + UUID.randomUUID() + "@example.com");
        assertThat(call("GET", "/api/v1/admin/investments", user, null).status()).isEqualTo(403);
        assertThat(call("POST", "/api/v1/admin/properties", user, propertyBody("x", "1000")).status()).isEqualTo(403);
        assertThat(call("GET", "/api/v1/admin/investments", null, null).status()).isEqualTo(401);
    }

    @Test
    void investmentFlowIdempotencyPortfolioAndDistributions() throws Exception {
        String admin = adminToken();
        JsonNode property = createProperty(admin, "Flow Property", "10000");
        String userA = register("a-" + UUID.randomUUID() + "@example.com");
        String userB = register("b-" + UUID.randomUUID() + "@example.com");

        // public listing + detail
        Res list = call("GET", "/api/v1/properties?size=100", null, null);
        assertThat(list.status()).isEqualTo(200);
        assertThat(list.body().get("content").toString()).contains("Flow Property");
        assertThat(call("GET", "/api/v1/properties/" + property.get("id").asString(), null, null).body().get("investmentEnabled").asBoolean()).isTrue();

        // validation of business rules
        assertThat(invest(userA, property, "50", UUID.randomUUID().toString()).body().get("errorCode").asString()).isEqualTo("BELOW_MINIMUM");
        assertThat(call("POST", "/api/v1/investments", userA, Map.of("opportunityId", property.get("opportunityId").asString(), "amount", "100", "currency", "USD")).status()).isEqualTo(400);

        // create + idempotent replay
        String key = UUID.randomUUID().toString();
        Res first = invest(userA, property, "1000", key);
        assertThat(first.status()).isEqualTo(201);
        assertThat(first.body().get("status").asString()).isEqualTo("PAYMENT_PENDING");
        assertThat(first.body().get("interestPercent").asString()).isEqualTo("10.000000");
        Res replay = invest(userA, property, "1000", key);
        assertThat(replay.body().get("id").asString()).isEqualTo(first.body().get("id").asString());
        assertThat(invest(userA, property, "2000", key).status()).isEqualTo(409);
        assertThat(call("GET", "/api/v1/properties/" + property.get("id").asString(), null, null).body().get("raisedAmount").asString()).startsWith("1000");

        // pending investments are not in the portfolio yet
        assertThat(call("GET", "/api/v1/portfolio/me", userA, null).body().get("positions").size()).isZero();

        // operator confirms payment; double-confirm is a conflict
        String invId = first.body().get("id").asString();
        assertThat(call("POST", "/api/v1/admin/investments/" + invId + "/confirm", admin, null).status()).isEqualTo(200);
        assertThat(call("POST", "/api/v1/admin/investments/" + invId + "/confirm", admin, null).status()).isEqualTo(409);

        JsonNode pf = call("GET", "/api/v1/portfolio/me", userA, null).body();
        assertThat(pf.get("totalInvested").asString()).startsWith("1000");
        assertThat(pf.get("positions").size()).isEqualTo(1);
        assertThat(pf.get("positions").get(0).get("interestPercent").asString()).startsWith("10");

        // user isolation: B sees nothing of A
        JsonNode pfB = call("GET", "/api/v1/portfolio/me", userB, null).body();
        assertThat(pfB.get("positions").size()).isZero();
        assertThat(call("GET", "/api/v1/transactions", userB, null).body().get("content").size()).isZero();
        assertThat(call("GET", "/api/v1/transactions", userA, null).body().get("content").size()).isEqualTo(3);

        // distribution: create -> approve -> mark paid
        Res dist = call("POST", "/api/v1/admin/distributions", admin, Map.of("propertyId", property.get("id").asString(), "totalAmount", "75.50", "periodStart", "2026-01-01", "periodEnd", "2026-01-31"));
        assertThat(dist.status()).isEqualTo(201);
        String dId = dist.body().get("id").asString();
        assertThat(call("POST", "/api/v1/admin/distributions/" + dId + "/mark-paid", admin, null).status()).isEqualTo(409);
        assertThat(call("POST", "/api/v1/admin/distributions/" + dId + "/approve", admin, null).status()).isEqualTo(200);
        assertThat(call("POST", "/api/v1/admin/distributions/" + dId + "/mark-paid", admin, null).status()).isEqualTo(200);
        JsonNode dA = call("GET", "/api/v1/distributions", userA, null).body();
        assertThat(dA.get("content").get(0).get("amount").asString()).startsWith("75.5");
        assertThat(dA.get("content").get(0).get("status").asString()).isEqualTo("PAID");
        assertThat(call("GET", "/api/v1/portfolio/me", userA, null).body().get("totalDistributions").asString()).startsWith("75.5");
        assertThat(call("GET", "/api/v1/distributions", userB, null).body().get("content").size()).isZero();
        assertThat(call("GET", "/api/v1/admin/audit-logs", admin, null).status()).isEqualTo(200);
    }

    @Test
    void concurrentInvestmentsCannotExceedCapacity() throws Exception {
        String admin = adminToken();
        JsonNode property = createProperty(admin, "Capacity Property", "10000");
        String a = register("c1-" + UUID.randomUUID() + "@example.com");
        String b = register("c2-" + UUID.randomUUID() + "@example.com");
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch go = new CountDownLatch(1);
        List<Callable<Res>> tasks = List.of(
                () -> { go.await(); return invest(a, property, "8000", UUID.randomUUID().toString()); },
                () -> { go.await(); return invest(b, property, "8000", UUID.randomUUID().toString()); });
        List<Future<Res>> futures = tasks.stream().map(pool::submit).toList();
        go.countDown();
        int created = 0, rejected = 0;
        for (Future<Res> f : futures) {
            Res r = f.get();
            if (r.status() == 201) created++;
            else if (r.status() == 422 && "CAPACITY_EXCEEDED".equals(r.body().get("errorCode").asString())) rejected++;
        }
        pool.shutdown();
        assertThat(created).isEqualTo(1);
        assertThat(rejected).isEqualTo(1);
        assertThat(call("GET", "/api/v1/properties/" + property.get("id").asString(), null, null).body().get("raisedAmount").asString()).startsWith("8000");
    }

    @Test
    void failedPaymentReleasesCapacityAndFillingReopensStatus() throws Exception {
        String admin = adminToken();
        JsonNode property = createProperty(admin, "Fill Property", "500");
        String u = register("f-" + UUID.randomUUID() + "@example.com");
        Res inv = invest(u, property, "500", UUID.randomUUID().toString());
        assertThat(inv.status()).isEqualTo(201);
        assertThat(call("GET", "/api/v1/properties/" + property.get("id").asString(), null, null).body().get("status").asString()).isEqualTo("FUNDED");
        assertThat(invest(u, property, "100", UUID.randomUUID().toString()).status()).isEqualTo(422);
        assertThat(call("POST", "/api/v1/admin/investments/" + inv.body().get("id").asString() + "/fail", admin, null).status()).isEqualTo(200);
        JsonNode after = call("GET", "/api/v1/properties/" + property.get("id").asString(), null, null).body();
        assertThat(after.get("status").asString()).isEqualTo("OPEN");
        assertThat(after.get("raisedAmount").asString()).startsWith("0");
    }
}
