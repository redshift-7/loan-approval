package com.example.loans.dao;

import com.example.loans.exception.BusinessRuleException;
import com.example.loans.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

import static com.example.loans.model.DecisionState.PENDING;
import static org.junit.jupiter.api.Assertions.*;

public class LoanRepositoryTest {

    ConcurrentSkipListMap<EventKey, UUID> events;
    ConcurrentHashMap<ApprovalRequest, ConcurrentHashMap<String, Decision>> storage;

    private static ThreadPoolTaskScheduler threadPoolTaskScheduler;
    private LoanRepository repository;

    @BeforeAll
    public static void setup() {
        threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(5);
        threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
    }

    @BeforeEach
    public void each() {
        events = new ConcurrentSkipListMap<>();
        storage = new ConcurrentHashMap<>();
        repository = new LoanRepository(events, storage, threadPoolTaskScheduler);
    }

    @Test
    public void givenMultiplePendingApprovalRequest_expectFail() {
        printStorage();
        repository.saveAsPending(generatePendingLoanApprovalRequest(UUID.randomUUID(), "AAA", 111.01));
        printStorage();
        BusinessRuleException thrown = assertThrows(BusinessRuleException.class,
                () -> repository.saveAsPending(generatePendingLoanApprovalRequest(UUID.randomUUID(), "AAA", 111.01)));
        assertTrue(thrown.getMessage().contains("Attempt to save duplicate pending LoanApprovalRequest"));
        Statistics statistics = repository.getStatistics(Duration.ofSeconds(60));
        assertEquals(1, storage.size());
        assertEquals(1, statistics.getCount());
        assertEquals(0, BigDecimal.valueOf(111.01).compareTo(statistics.getSum()));
        assertEquals(0, BigDecimal.valueOf(111.01).compareTo(statistics.getAvg()));
        assertEquals(0, BigDecimal.valueOf(111.01).compareTo(statistics.getMin()));
        assertEquals(0, BigDecimal.valueOf(111.01).compareTo(statistics.getMax()));
    }

    @Test
    public void givenTwoProducers_whenSaving2UniqueLoanApprovalRequestsConcurrently_expectCorrectStatistics() throws InterruptedException {
        final int numberOfThreads = 2;
        final ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        CountDownLatch completedThreadCounter = new CountDownLatch(numberOfThreads);
        CountDownLatch readyThreadCounter = new CountDownLatch(numberOfThreads);
        CountDownLatch callingThreadBlocker = new CountDownLatch(1);

        Runnable producer1 = () -> {
            try {
                readyThreadCounter.countDown();
                callingThreadBlocker.await();
                for (int i = 0; i < 3; i++) {
                    try {
                        repository.saveAsPending(LoanApprovalRequest.builder()
                                .customerId("1X-XXXX-XAX")
                                .decisionState(PENDING)
                                .loanAmount(BigDecimal.valueOf(123.01 + i))
                                .approvers(new HashSet<>(Arrays.asList(new Approver("Under €1_000 Approver"))))
                                .timestamp(ZonedDateTime.now())
                                .build());
                    } catch (BusinessRuleException be) {
                        System.out.println("BusinessRuleException @ producer1: " + be.getMessage());
                    }
                }
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            } finally {
                System.out.println("Completed producer1");
                completedThreadCounter.countDown();
            }
        };

        Runnable producer2 = () -> {
            try {
                readyThreadCounter.countDown();
                callingThreadBlocker.await();
                for (int i = 0; i < 3; i++) {
                    try {
                        repository.saveAsPending(LoanApprovalRequest.builder()
                                .customerId("2X-XXXX-XWX")
                                .loanAmount(BigDecimal.valueOf(1023.55 + i * 10))
                                .decisionState(PENDING)
                                .approvers(new HashSet<>(Arrays.asList(new Approver("Under €9_000 Approver"))))
                                .timestamp(ZonedDateTime.now())
                                .build());
                    } catch (BusinessRuleException be) {
                        System.out.println("BusinessRuleException @ producer2: " + be.getMessage());
                    }
                }
                completedThreadCounter.countDown();
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            } finally {
                System.out.println("Completed producer2");
                completedThreadCounter.countDown();
            }
        };

        executorService.execute(producer1);
        executorService.execute(producer2);

        readyThreadCounter.await();
        callingThreadBlocker.countDown();
        completedThreadCounter.await();

        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        Statistics statistics = repository.getStatistics(Duration.ofSeconds(60));

        printStorage();

        assertEquals(2, storage.size());
        assertEquals(6, events.size());
        assertEquals(2, statistics.getCount());
        assertEquals(0, BigDecimal.valueOf(123.01).add(BigDecimal.valueOf(1023.55)).compareTo(statistics.getSum()));
        assertEquals(0, BigDecimal.valueOf(573.28).compareTo(statistics.getAvg()));
        assertEquals(0, BigDecimal.valueOf(123.01).compareTo(statistics.getMin()));
        assertEquals(0, BigDecimal.valueOf(1023.55).compareTo(statistics.getMax()));
    }

    @Test
    public void given3ApprovalRequestWithin1MinAnd5Outside_whenGettingStatistics_expectValidStatisticsFor3ApprovalRequests() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        UUID uuid3 = UUID.randomUUID();

        UUID uuid4 = UUID.randomUUID();
        UUID uuid5 = UUID.randomUUID();

        events.putIfAbsent(new EventKey(ZonedDateTime.now().minus(Duration.ofSeconds(10))), uuid1);
        events.putIfAbsent(new EventKey(ZonedDateTime.now().minus(Duration.ofSeconds(20))), uuid2);
        events.putIfAbsent(new EventKey(ZonedDateTime.now().minus(Duration.ofSeconds(30))), uuid3);
        events.putIfAbsent(new EventKey(ZonedDateTime.now().minus(Duration.ofSeconds(66))), uuid4);
        events.putIfAbsent(new EventKey(ZonedDateTime.now().minus(Duration.ofSeconds(90))), uuid5);

        ConcurrentHashMap<String, Decision> storagePutResult = storage.put(generatePendingLoanApprovalRequest(uuid1, "001", 111.01), generateDecision("001"));
        assertEquals(null, storagePutResult);
        storagePutResult = storage.put(generatePendingLoanApprovalRequest(uuid2, "002", 121.01), generateDecision("002"));
        assertEquals(null, storagePutResult);
        storagePutResult = storage.put(generatePendingLoanApprovalRequest(uuid3, "003", 131.01), generateDecision("003"));
        assertEquals(null, storagePutResult);

        storagePutResult = storage.put(generatePendingLoanApprovalRequest(uuid4, "004", 141.01), generateDecision("004"));
        assertEquals(null, storagePutResult);
        storagePutResult = storage.put(generatePendingLoanApprovalRequest(uuid5, "005", 151.01), generateDecision("005"));
        assertEquals(null, storagePutResult);

        Statistics statistics = repository.getStatistics(Duration.ofSeconds(60));

        assertEquals(5, storage.size());
        assertEquals(5, events.size());
        assertEquals(3, statistics.getCount());
        assertEquals(0, BigDecimal.valueOf(363.03).compareTo(statistics.getSum()));
        assertEquals(0, BigDecimal.valueOf(121.01).compareTo(statistics.getAvg()));
        assertEquals(0, BigDecimal.valueOf(111.01).compareTo(statistics.getMin()));
        assertEquals(0, BigDecimal.valueOf(131.01).compareTo(statistics.getMax()));
    }

    private ConcurrentHashMap<String, Decision> generateDecision(String customerIdSuffix) {
        Decision pendingDecision = Decision.builder()
                .customerId("OK-XXXX-" + customerIdSuffix)
                .approverUsername("Under €1_000 Approver")
                .state(PENDING)
                .build();
        ConcurrentHashMap<String, Decision> decisions = new ConcurrentHashMap<>();
        decisions.put("Under €1_000 Approver", pendingDecision);
        return decisions;
    }

    public LoanApprovalRequest generatePendingLoanApprovalRequest(UUID uuid1, String customerIdSuffix, double amount) {
        Set<Approver> defaultApprover = new HashSet<>();
        defaultApprover.add(new Approver("Under €1_000 Approver"));
        return LoanApprovalRequest.builder()
                .uuid(uuid1)
                .customerId("OK-XXXX-" + customerIdSuffix)
                .loanAmount(BigDecimal.valueOf(amount))
                .decisionState(PENDING)
                .approvers(defaultApprover)
                .timestamp(ZonedDateTime.now().minus(Duration.ofSeconds(10)))
                .build();
    }

    private void printStorage() {
        if (storage.isEmpty()) {
            System.out.println(">----------------------storage :: EMPTY--------------------------------<");
        } else {
            System.out.println("-----------------------storage :: START---------------------------------");
            storage.forEach((k, v) -> System.out.println(k));
            System.out.println("-----------------------storage :: END---------------------------------");
        }
    }
}
