package it.bocconi.bledger.feature.transaction.job;

import it.bocconi.bledger.blockchain.service.IdentityManagerService;
import it.bocconi.bledger.feature.smartcontract.service.BLedgerNetworkService;
import it.bocconi.bledger.feature.transaction.dao.service.BcTransactionDaoService;
import it.bocconi.bledger.feature.transaction.service.TransactionService;
import it.bocconi.bledger.feature.wallet.service.WalletService;
import it.bocconi.bledger.reactive.job.BLAbstractJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class TransactionJob extends BLAbstractJob {

    private final BcTransactionDaoService bcTransactionDaoService;
    private final BLedgerNetworkService bLedgerNetworkService;
    private final TransactionService transactionService;
    private final WalletService walletService;
    private final IdentityManagerService identityManagerService;

    @Value("${it.bocconi.bledger.job.transaction-job.batch-size:20}")
    private int batchSize;


    public TransactionJob(Environment environment, BcTransactionDaoService bcTransactionDaoService, BLedgerNetworkService bLedgerNetworkService, TransactionService transactionService, WalletService walletService, IdentityManagerService identityManagerService) {
        super(environment);
        this.bcTransactionDaoService = bcTransactionDaoService;
        this.bLedgerNetworkService = bLedgerNetworkService;
        this.transactionService = transactionService;
        this.walletService = walletService;
        this.identityManagerService = identityManagerService;
    }

    @Override
    public Mono<Void> execute() {
        return bcTransactionDaoService
                .fetchToExecute(batchSize)
                .concatMap(bcTransaction -> {
                    Mono<Boolean> processResult = switch (bcTransaction.getTransactionType()) {
                        case CREATE_NETWORK -> bLedgerNetworkService.processMintTransaction(bcTransaction.getId());
                        case UPDATE_NETWORK, UPLOAD_FILE, AUDIT, REFUSED_INVITATION -> bLedgerNetworkService.processUpdateTransaction(bcTransaction.getId());
                        case ADD_AUDITORS -> bLedgerNetworkService.processAddAuditors(bcTransaction.getId());
                        case DEPLOY_WALLET -> walletService.deployWalletByTransaction(bcTransaction.getId());
                        case WHITELIST_WALLET -> identityManagerService.whitelistWalletByTransaction(bcTransaction.getId());
                        default -> Mono.error(new RuntimeException("Unsupported operation"));
                    };
                    return processResult
                            .onErrorResume(err -> {
                                log.error("Error processing transaction", err);
                                return Mono.just(false);
                            })
                            .flatMap(result -> result
                                    ? transactionService.markTransactionAsProcessed(bcTransaction.getId())
                                    : transactionService.markTransactionAsFailed(bcTransaction.getId())
                            )
                            .then()
                            .onErrorResume(err -> {
                                log.error("Error processing transaction {}", bcTransaction.getId(), err);
                                return Mono.error(err);
                            });
                })
                .then();
    }

}
