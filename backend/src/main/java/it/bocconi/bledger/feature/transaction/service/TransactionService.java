package it.bocconi.bledger.feature.transaction.service;

import it.bocconi.bledger.feature.transaction.dao.service.BcTransactionDaoService;
import it.bocconi.bledger.feature.transaction.entity.BcTransaction;
import it.bocconi.bledger.feature.transaction.enums.TransactionStatus;
import it.bocconi.bledger.feature.transaction.enums.TransactionType;
import it.bocconi.bledger.feature.transaction.router.dto.TransactionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.EnumSet;

@Service
@Slf4j
public class TransactionService {

    private final BcTransactionDaoService transactionDaoService;

    private final int maxRetries;

    public TransactionService(BcTransactionDaoService transactionDaoService,
                              @Value("${it.bocconi.bledger.job.max-retries:5}") int maxRetries) {
        this.transactionDaoService = transactionDaoService;
        this.maxRetries = maxRetries;
    }

    @Transactional
    public Mono<BcTransaction> createTransaction(TransactionType transactionType,
                                                 String creatorId,
                                                 String to,
                                                 String payload,
                                                 String networkId,
                                                 String nodeId
    ) {
        BcTransaction transaction = BcTransaction.builder()
                .transactionType(transactionType)
                .toAddress(to)
                .status(TransactionStatus.PENDING)
                .payload(payload)
                .networkId(networkId)
                .readyAt(LocalDateTime.now())
                .creatorId(creatorId)
                .nodeId(nodeId)
                .build();
        return transactionDaoService.save(transaction);
    }

    @Transactional
    public Mono<BcTransaction> createTransaction(TransactionType transactionType,
                                                 String creatorId,
                                                 String to,
                                                 String payload,
                                                 String networkId
    ) {
        return createTransaction(transactionType, creatorId, to, payload, networkId, null);
    }

    @Transactional
    public Mono<BcTransaction> markTransactionAsProcessed(String transactionId) {
        return transactionDaoService.findById(transactionId)
                .switchIfEmpty(Mono.error(new RuntimeException("transaction not found")))
                .flatMap(bcTransaction -> {
                    bcTransaction.setStatus(TransactionStatus.CONFIRMED);
                    bcTransaction.setConfirmedAt(LocalDateTime.now());
                    return transactionDaoService.save(bcTransaction).
                            doOnSuccess((a) ->  log.info("Processed transaction id {}, hash {}, type {}", bcTransaction.getId(), bcTransaction.getTxHash(), bcTransaction.getTransactionType()));
                });
    }

    @Transactional
    public Mono<BcTransaction> markTransactionAsFailed(String transactionId) {
        return transactionDaoService.findById(transactionId)
                .switchIfEmpty(Mono.error(new RuntimeException("transaction not found")))
                .flatMap(tx -> {
                    Integer currentTry = tx.getTryCount() == null ? 0 : tx.getTryCount();

                    if (currentTry.equals(maxRetries)) {
                        tx.setStatus(TransactionStatus.FAILED);

                        return transactionDaoService.save(tx)
                                .doOnSuccess(a -> log.error(
                                        "Failed to process transaction id {}, hash {}, type {}",
                                        tx.getId(), tx.getTxHash(), tx.getTransactionType()
                                ));
                    } else {
                        int nextTry = currentTry + 1;
                        tx.setTryCount(nextTry);
                        tx.setStatus(TransactionStatus.FAIL_PENDING);
                        tx.setReadyAt(LocalDateTime.now().plusSeconds(nextTry * 60L));

                        return transactionDaoService.save(tx)
                                .doOnSuccess(a -> log.error(
                                        "Failed to process transaction id {}, hash {}, type {}",
                                        tx.getId(), tx.getTxHash(), tx.getTransactionType()
                                ));
                    }
                });
    }

    @Transactional(readOnly = true)
    public Mono<Page<TransactionDto>> getNetworkTransactions(String networkId, Pageable pageable) {
        return transactionDaoService.findByNetworkAndTypes(
                        networkId,
                        EnumSet.of(TransactionType.CREATE_NETWORK, TransactionType.UPDATE_NETWORK, TransactionType.ADD_AUDITORS),
                        pageable)
                .map(page -> page.map(this::toListDto));
    }

    private TransactionDto toListDto(BcTransaction tx) {
        return new TransactionDto(
                tx.getId(),
                tx.getCreatedAt(),
                tx.getUpdatedAt(),
                tx.getConfirmedAt(),
                tx.getTransactionType(),
                tx.getStatus(),
                tx.getTxHash()
        );
    }

    @Transactional(readOnly = true)
    public Mono<Page<TransactionDto>> getNodeTransactions(String nodeId, Pageable pageable) {
        // opzionale: validazioni/visibilità su nodeId tramite RequestContext
        return  transactionDaoService.findByNodeAndTypes(
                        nodeId,
                        EnumSet.of(TransactionType.UPLOAD_FILE, TransactionType.AUDIT),
                        pageable)
                .map(page -> page.map(this::toListDto));
    }

    @Transactional(readOnly = true)
    public Mono<TransactionDto> findByIdAuthorized(String transactionId) {
        // autorizzazione semplice: il creator deve coincidere con la company corrente
        return transactionDaoService.findById(transactionId).map(this::toListDto);
    }

}
