# Educational Guide: Formal State Machine Payment Tracker

## 1. Source of Truth Architecture
In financial systems (like SWIFT gpi), there is always one "Source of Truth" (SoT). Our `sts-mock` acts as this SoT.
- **Stateful Persistence**: The SoT doesn't just return data; it tracks the lifecycle.
- **Audit Trails**: Every transition is recorded with full payload context in `transition_history`.

## 2. Formal Verification
We use **NuSMV** to model-check our state machine.
- **Safety**: "Once Accepted, always Accepted."
- **Liveness**: "A transaction can always reach a terminal state."
- **Equivalence**: Our Java `TransactionStateMachine` mirrors the logic verified in `transaction_state_machine.smv`.

## 3. Design Patterns for Robustness
- **Visitor Pattern**: Separates data from behavior. We can add new reports (Excel, PDF) by just adding a new `TransactionVisitor` without touching the core model.
- **Decorator Pattern**: Used for clean-room logging. `LoggingUetrProcessor` wraps the execution without cluttering the business logic.
- **State Pattern**: (Implemented via Enum logic) ensures validity of lifecycle stages.

## 4. Resilient Clients
The `sts-service` is designed to be autonomous.
- **Local Persistence**: It caches what it has seen in `service_uetrs.db`.
- **Intelligent Seeding**: It knows when to initialize itself and when to resume, preventing duplicate processing.

## 5. Property-Based Testing
While standard Unit tests check specific cases, **jqwik** checks properties across the entire state space. This is critical for financial logic where edge cases (like transitioning from Rejected back to Pending) must be mathematically impossible.
