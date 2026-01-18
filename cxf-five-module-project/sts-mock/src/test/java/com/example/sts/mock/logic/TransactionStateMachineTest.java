package com.example.sts.mock.logic;

import com.example.sts.model.TransactionStatus;
import net.jqwik.api.*;
import org.junit.jupiter.api.Assertions;

class TransactionStateMachineTest {

    private final TransactionStateMachine stateMachine = new TransactionStateMachine();

    @Property
    void acceptedIsTerminal(@ForAll("states") TransactionStatus nextState) {
        // Once ACCC, it can only stay ACCC (as per our Java logic allowing identity)
        if (nextState == TransactionStatus.ACCC) {
            Assertions.assertTrue(stateMachine.isValidTransition(TransactionStatus.ACCC, nextState));
        } else {
            Assertions.assertFalse(stateMachine.isValidTransition(TransactionStatus.ACCC, nextState));
        }
    }

    @Property
    void rejectedIsTerminal(@ForAll("states") TransactionStatus nextState) {
        if (nextState == TransactionStatus.RJCT) {
            Assertions.assertTrue(stateMachine.isValidTransition(TransactionStatus.RJCT, nextState));
        } else {
            Assertions.assertFalse(stateMachine.isValidTransition(TransactionStatus.RJCT, nextState));
        }
    }

    @Property
    void pendingCanTransitionToAny(@ForAll("states") TransactionStatus nextState) {
        Assertions.assertTrue(stateMachine.isValidTransition(TransactionStatus.PDNG, nextState));
    }

    @Provide
    Arbitrary<TransactionStatus> states() {
        return Arbitraries.of(TransactionStatus.values());
    }
}
