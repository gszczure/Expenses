package org.example.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import org.example.dto.request.CreateAccountRequestDto;
import org.example.dto.request.CreateTransactionRequestDto;
import org.example.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Function;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ExpensesAppIntegrationTest extends IntegrationTests {

    private static final String ACCOUNT_NAME = "Main account";

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("Should update account balance when income and expensetransactions are created and deleted")
    void transactionFlow_shouldUpdateBalance_whenTransactionsAreProcessed() throws Exception {

        Long accountId = createAccount();

        createTransaction(accountId, this::createIncomeRequest);

        given()
                .when()
                .get(accountUrl(accountId))
                .then()
                .statusCode(200)
                .body("balance", equalTo(1000.0F));

        Long expenseTransactionId = createTransaction(accountId, this::createExpenseRequest);

        given()
                .when()
                .get(accountUrl(accountId))
                .then()
                .statusCode(200)
                .body("balance", equalTo(800.0F));

        given()
                .when()
                .delete(transactionUrl(expenseTransactionId))
                .then()
                .statusCode(204);

        given()
                .when()
                .get(accountUrl(accountId))
                .then()
                .statusCode(200)
                .body("balance", equalTo(1000.0F));
    }

    @Test
    @DisplayName("Should return conflict when account contains transactions")
    void deleteAccount_shouldReturnConflict_whenAccountContainsTransactions() throws Exception {

        Long accountId = createAccount();

        createTransaction(accountId, this::createIncomeRequest);

        given()
                .when()
                .delete(accountUrl(accountId))
                .then()
                .statusCode(409);
    }

    @Test
    @DisplayName("Should return not found when account does not exist")
    void createTransaction_shouldReturnNotFound_whenAccountDoesNotExist()
            throws Exception {

        CreateTransactionRequestDto request = createIncomeRequest(999L);

        given()
                .contentType("application/json")
                .body(objectMapper.writeValueAsString(request))
                .when()
                .post("/transactions")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Should return bad request when amount is negative")
    void createTransaction_shouldReturnBadRequest_whenAmountIsNegative() throws Exception {

        CreateTransactionRequestDto request =
                new CreateTransactionRequestDto(
                        BigDecimal.valueOf(-100),
                        TransactionType.INCOME,
                        "Food",
                        "Description",
                        LocalDate.now(),
                        1L
                );

        given()
                .contentType("application/json")
                .body(objectMapper.writeValueAsString(request))
                .when()
                .post("/transactions")
                .then()
                .statusCode(400);
    }

    private Long createAccount() throws Exception {
        CreateAccountRequestDto request = new CreateAccountRequestDto(ACCOUNT_NAME);

        return given()
                .contentType("application/json")
                .body(objectMapper.writeValueAsString(request))
                .when()
                .post("/accounts")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");
    }

    private Long createTransaction(Long accountId, Function<Long, ?> requestFactory)
            throws Exception {
        return given()
                .contentType("application/json")
                .body(objectMapper.writeValueAsString(requestFactory.apply(accountId)))
                .when()
                .post("/transactions")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");
    }

    private CreateTransactionRequestDto createIncomeRequest(Long accountId) {
        return new CreateTransactionRequestDto(
                BigDecimal.valueOf(1000),
                TransactionType.INCOME,
                "Salary",
                "Salary",
                LocalDate.now(),
                accountId
        );
    }

    private CreateTransactionRequestDto createExpenseRequest(Long accountId) {
        return new CreateTransactionRequestDto(
                BigDecimal.valueOf(200),
                TransactionType.EXPENSE,
                "Food",
                "Lunch",
                LocalDate.now(),
                accountId
        );
    }

    private String accountUrl(Long accountId) {
        return "/accounts/" + accountId;
    }

    private String transactionUrl(Long transactionId) {
        return "/transactions/" + transactionId;
    }
}
