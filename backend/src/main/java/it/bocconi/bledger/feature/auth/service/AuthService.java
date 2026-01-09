package it.bocconi.bledger.feature.auth.service;

import it.bocconi.bledger.exception.UnauthorizedException;
import it.bocconi.bledger.feature.auth.router.dto.LoginRequestDto;
import it.bocconi.bledger.feature.company.dao.service.BcCompanyDaoService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class AuthService {

    private BcCompanyDaoService bcCompanyDaoService;

    public Mono<String> login(LoginRequestDto loginRequestDto) {
        return bcCompanyDaoService.existsById(loginRequestDto.companyId())
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new UnauthorizedException(
                                "Company not found, id: " + loginRequestDto.companyId()));
                    }
                    return Mono.just(loginRequestDto.companyId());
                });
    }

}
