package it.bocconi.bledger.feature.init;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.bocconi.bledger.feature.company.entity.BcCompany;
import it.bocconi.bledger.feature.company.dao.service.BcCompanyDaoService;
import it.bocconi.bledger.feature.company.enums.CompanyType;
import it.bocconi.bledger.feature.file.entity.BcFile;
import it.bocconi.bledger.feature.file.entity.BcFileBinary;
import it.bocconi.bledger.feature.file.dao.service.BcFileBinaryDaoService;
import it.bocconi.bledger.feature.file.dao.service.BcFileDaoService;

import it.bocconi.bledger.feature.file.service.ResourceService;
import it.bocconi.bledger.feature.file.util.ExcelUtil;
import it.bocconi.bledger.feature.file.util.FileUtils;
import it.bocconi.bledger.feature.company.service.CompanyService;
import it.bocconi.bledger.feature.smartcontract.service.SmartContractService;
import it.bocconi.bledger.feature.smartcontract.dao.service.BcSmartContractDaoService;
import it.bocconi.bledger.feature.smartcontract.entity.BcSmartContract;
import it.bocconi.bledger.feature.smartcontract.enums.SmartContractType;
import it.bocconi.bledger.feature.transaction.enums.TransactionType;
import it.bocconi.bledger.feature.transaction.enums.WalletType;
import it.bocconi.bledger.feature.transaction.service.TransactionService;
import it.bocconi.bledger.feature.wallet.service.WalletService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.*;

@Service
@AllArgsConstructor
@Slf4j
public class InitService {

    private final BcCompanyDaoService bcCompanyDaoService;
    private final SmartContractService smartContractService;
    private final BcSmartContractDaoService bcSmartContractDaoService;
    private final CompanyService companyService;

    @Transactional
    public Mono<Boolean> importCompanies() {
        return bcCompanyDaoService.count()
                .flatMap(count -> {
                    if (count > 0) {
                        log.info("Companies already exist in the database, skipping import.");
                        return Mono.just(false);
                    }
                    return companyService.importCompaniesFromDefaults();
                });

    }

    public Flux<BcSmartContract> deploySmartContracts() {
        return Flux.fromIterable(List.of(SmartContractType.IDENTITY, SmartContractType.ERC721))
                .concatMap(type -> bcSmartContractDaoService.findByType(type)
                        .map(bcSmartContract -> {
                            log.info("Smart Contract {} already deployed with address: {}", type, bcSmartContract.getContractAddress());
                            return bcSmartContract;
                        })
                        .switchIfEmpty(smartContractService.deploySmartContract(type)));

    }

}
