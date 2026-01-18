# Makefile for Payment Tracker System

MOCK_LOG = mock_server.log
JOB_LOG = job.log

.PHONY: all clean-db run-mock run-job run-all stop-mock

all: help

help:
	@echo "Usage:"
	@echo "  make clean-db   - Delete SQLite database files"
	@echo "  make test       - Run all tests using Maven wrapper"
	@echo "  make run-mock   - Start the Mock Server (background)"
	@echo "  make run-job    - Run the Job Orchestrator"
	@echo "  make run-all    - Clean, Run Mock, Run Job, Stop Mock"

clean-db:
	rm -f cxf-five-module-project/mock_server.db
	rm -f cxf-five-module-project/service_uetrs.db
	rm -f mock_server.db
	rm -f service_uetrs.db

test:
	./mvnw test

run-mock:
	@echo "Starting Mock Server..."
	./mvnw exec:java -Dexec.mainClass="com.example.sts.mock.MockServerMain" -pl sts-mock > $(MOCK_LOG) 2>&1 &
	@echo "Mock Server started (PID in background, check $(MOCK_LOG))"

run-job:
	@echo "Running Job Orchestrator..."
	./mvnw exec:java -Dexec.mainClass="com.example.sts.job.JobMain" -pl sts-job

stop-mock:
	pkill -f MockServerMain || true
	@echo "Mock Server stopped"

run-all: clean-db run-mock
	sleep 10
	$(MAKE) run-job
	sleep 2
	$(MAKE) run-job
	$(MAKE) stop-mock
	@echo "All tasks completed."
