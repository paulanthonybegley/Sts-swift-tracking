package com.example.sts.mock.api;

import com.example.sts.mock.logic.TransactionStateMachine;
import com.example.sts.mock.persistence.TransitionHistoryStore;
import com.example.sts.model.Transaction;
import com.example.sts.model.TransactionStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Component
@Path("/payment-transactions")
public class PaymentTransactionController {
    private static final Logger log = LoggerFactory.getLogger(PaymentTransactionController.class);

    private final JdbcTemplate jdbcTemplate;
    private final TransitionHistoryStore historyStore;
    private final TransactionStateMachine stateMachine;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    // In-memory cache for current state to allow transition checks without
    // re-reading all history
    private final Map<String, TransactionStatus> lastKnownState = new HashMap<>();

    public PaymentTransactionController(JdbcTemplate jdbcTemplate,
            TransitionHistoryStore historyStore,
            TransactionStateMachine stateMachine) {
        this.jdbcTemplate = jdbcTemplate;
        this.historyStore = historyStore;
        this.stateMachine = stateMachine;
    }

    @GET
    @Path("/{uetr}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTransaction(@PathParam("uetr") String uetr,
            @HeaderParam("Authorization") String authHeader) {

        // 1. Check Authorization
        if (!"Bearer mock-bearer-token-123".equals(authHeader)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // 2. Determine which file to read
        Integer nextIndex = jdbcTemplate.query(
                "SELECT next_index FROM transaction_pointer WHERE uetr = ?",
                (rs, rowNum) -> rs.getInt("next_index"),
                uetr).stream().findFirst().orElse(null);

        if (nextIndex == null) {
            nextIndex = 1;
            jdbcTemplate.update("INSERT INTO transaction_pointer (uetr, next_index) VALUES (?, ?)", uetr, 2);
        } else {
            jdbcTemplate.update("UPDATE transaction_pointer SET next_index = ? WHERE uetr = ?", nextIndex + 1, uetr);
        }

        // 3. Load file from resources: {uetr}.{index}.json
        String fileName = uetr + "." + nextIndex + ".json";
        String resourcePath = "classpath:/payment-transactions/" + fileName;

        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource resource = resolver.getResource(resourcePath);

            if (!resource.exists()) {
                log.warn("Resource not found: {}", resourcePath);
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            try (InputStream is = resource.getInputStream()) {
                Transaction transaction = objectMapper.readValue(is, Transaction.class);

                // 4. Validate State Transition
                TransactionStatus currentStatus = lastKnownState.get(uetr);
                TransactionStatus nextStatus = transaction.getStatus();

                if (currentStatus != null) {
                    stateMachine.ensureValidTransition(currentStatus, nextStatus);
                }

                // 5. Audit & Persist
                historyStore.recordTransition(uetr, currentStatus, nextStatus, transaction);
                lastKnownState.put(uetr, nextStatus);

                return Response.ok(transaction).build();
            }
        } catch (IllegalStateException e) {
            log.error("Illegal state transition for UETR: {}", uetr, e);
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        } catch (Exception e) {
            log.error("Error processing transaction for UETR: {}", uetr, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
