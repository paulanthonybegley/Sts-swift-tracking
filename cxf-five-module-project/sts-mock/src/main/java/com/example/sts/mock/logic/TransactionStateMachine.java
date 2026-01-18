package com.example.sts.mock.logic;

import com.example.sts.model.TransactionStatus;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Formal State Machine implementation for Transaction lifecycle.
 * Logic:
 * PDNG -> PDNG (allow)
 * PDNG -> ACCC (allow)
 * PDNG -> RJCT (allow)
 * ACCC -> * (deny)
 * RJCT -> * (deny)
 */
@Component
public class TransactionStateMachine {

    /**
     * Validates if a transition from currentState to nextState is allowed.
     * @param currentState The current state of the transaction
     * @param nextState The target state
     * @return true if valid, false otherwise
     */
    public boolean isValidTransition(TransactionStatus currentState, TransactionStatus nextState) {
        if (currentState == null || nextState == null) {
            return false;
        }

        switch (currentState) {
            case PDNG:
                // Can transition to itself, ACCC, or RJCT
                return Set.of(TransactionStatus.PDNG, TransactionStatus.ACCC, TransactionStatus.RJCT).contains(nextState);
            case ACCC:
                // Terminal state, can only stay ACCC (idempotent check usually, but strict transition implies change)
                // If we treat it as "next state", staying in ACCC is valid next state in NuSMV 
                // but usually "change" to ACCC from ACCC is a no-op.
                // Let's allow ACCC -> ACCC identity, but nothing else.
                return nextState == TransactionStatus.ACCC;
            case RJCT:
                // Terminal state
                return nextState == TransactionStatus.RJCT;
            default:
                return false;
        }
    }
    
    public void ensureValidTransition(TransactionStatus current, TransactionStatus next) {
        if (!isValidTransition(current, next)) {
            throw new IllegalStateException("Invalid state transition from " + current + " to " + next);
        }
    }
}
