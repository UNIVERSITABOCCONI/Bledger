package it.bocconi.bledger.feature.company.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.bocconi.bledger.exception.NotFoundException;
import it.bocconi.bledger.feature.auth.filter.RequestContext;
import it.bocconi.bledger.feature.company.dao.service.BcCompanyDaoService;
import it.bocconi.bledger.feature.company.entity.BcCompany;
import it.bocconi.bledger.feature.company.enums.CompanyType;
import it.bocconi.bledger.feature.company.mapper.CompanyMapper;
import it.bocconi.bledger.feature.company.router.dto.CompanyDto;
import it.bocconi.bledger.feature.company.router.dto.CompanyLightDto;
import it.bocconi.bledger.feature.company.router.dto.CompanyProfileImageDto;
import it.bocconi.bledger.feature.file.dao.service.BcFileBinaryDaoService;
import it.bocconi.bledger.feature.file.dao.service.BcFileDaoService;
import it.bocconi.bledger.feature.file.entity.BcFile;
import it.bocconi.bledger.feature.file.entity.BcFileBinary;
import it.bocconi.bledger.feature.file.service.ResourceService;
import it.bocconi.bledger.feature.file.util.ExcelUtil;
import it.bocconi.bledger.feature.file.util.FileUtils;
import it.bocconi.bledger.feature.transaction.enums.TransactionType;
import it.bocconi.bledger.feature.transaction.enums.WalletType;
import it.bocconi.bledger.feature.transaction.service.TransactionService;
import it.bocconi.bledger.feature.wallet.entity.BcWallet;
import it.bocconi.bledger.feature.wallet.service.WalletService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class CompanyService {

    private final BcCompanyDaoService bcCompanyDaoService;
    private final BcFileDaoService bcFileDaoService;
    private final BcFileBinaryDaoService bcFileBinaryDaoService;
    private final ResourceService resourceService;
    private final WalletService walletService;
    private final TransactionService transactionService;
    private final ObjectMapper objectMapper;
    private final CompanyMapper companyMapper;

    @Transactional(readOnly = true)
    public Flux<CompanyLightDto> getCompaniesByType(CompanyType type) {
        return bcCompanyDaoService.getCompaniesByRolesRows(type)
                .map(row -> new CompanyLightDto(row.getId(), row.getCompanyName()));
    }

    @Transactional(readOnly = true)
    public Mono<CompanyDto> getMyCompany() {
        return RequestContext.getCompanyId()
                .flatMap(companyId -> Mono.zip(
                                bcCompanyDaoService.findById(companyId),
                                bcCompanyDaoService.getCompanyWalletAddressById(companyId)
                        )
                        .map(tuple -> {
                            BcCompany company = tuple.getT1();
                            String walletAddress = tuple.getT2();
                            return companyMapper.fromBcCompanyToCompanyDto(company, walletAddress);
                        }));
    }

    @Transactional
    public Mono<Boolean> importCompaniesFromDefaults() {
        return resourceService.getCompanyLogos()
                .flatMap(FileUtils::processLogoMono)
                .collectList()
                .flatMap(logoList ->
                        resourceService.getCompaniesFile()
                                .flatMap(resource -> importCompaniesFromLogosAndExcel(logoList, resource))
                );
    }

    @Transactional
    public Mono<Boolean> importCompaniesFromLogosAndExcel(List<FileUtils.FileLogoData> fileLogoDataList,
                                                            Resource companiesResource) {

        Map<String, String> companyFileMapping = new HashMap<>();
        List<BcFile> files = new ArrayList<>();
        List<BcFileBinary> binaries = new ArrayList<>();

        for (FileUtils.FileLogoData fileLogoData : fileLogoDataList) {
            files.add(fileLogoData.bcFile());
            binaries.add(fileLogoData.bcFileBinary());
            String companyId = fileLogoData.companyId();
            companyFileMapping.put(companyId, fileLogoData.bcFile().getId());
        }

        return ExcelUtil.getCompaniesAndRolesFlux(companyFileMapping, companiesResource)
                .concatMap(company ->
                    Mono.zip(createCompanyWallet(company), walletService.getOrCreateAdminWallet())
                        .flatMap(tuple -> saveCompanyCreationTransaction(company, tuple))
                )
                .collectList()
                .flatMap(updatedCompanies ->
                        bcFileBinaryDaoService.saveAll(binaries)
                                .thenMany(bcFileDaoService.saveAll(files))
                                .thenMany(bcCompanyDaoService.saveAll(updatedCompanies))
                                .then(Mono.just(true))
                ).switchIfEmpty(Mono.just(false));
    }

    @Transactional(readOnly = true)
    public Mono<CompanyProfileImageDto> getCompanyProfileImage() {
        return RequestContext.getCompanyId()
                .flatMap(bcFileDaoService::companyProfileImageRow)
                .switchIfEmpty(Mono.error(new NotFoundException("Company profile image not found")))
                .map(companyMapper::companyProfileImageRowToCompanyProfileImageDto);
    }

    @NotNull
    private Mono<BcCompany> saveCompanyCreationTransaction(BcCompany company, Tuple2<BcWallet, BcWallet> tuple) {
        var wallet = tuple.getT1();
        var adminWallet = tuple.getT2();
        String payload;
        try {
            payload = objectMapper.writeValueAsString(Map.of(
                    "walletId", company.getWalletId(),
                    "isAuditor", company.getCompanyType() == CompanyType.TPA
            ));
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return transactionService.createTransaction(
                        TransactionType.WHITELIST_WALLET,
                        adminWallet.getId(),
                        wallet.getWalletAddress(),
                        payload,
                        null
                )
                .thenReturn(company);
    }

    @NotNull
    private Mono<BcWallet> createCompanyWallet(BcCompany company) {
        return walletService.createWallet(
                company.getCompanyType() == CompanyType.ORG
                        ? WalletType.COMPANY
                        : WalletType.AUDITOR
        ).map(wallet -> {
            company.setWalletId(wallet.getId());
            return wallet;
        });
    }




}
