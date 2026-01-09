package it.bocconi.bledger.service;

import it.bocconi.bledger.feature.am.service.AmService;
import it.bocconi.bledger.feature.company.service.CompanyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AmServiceTest {

    @Mock
    private CompanyService companyService;

    @Test
    void uploadCompaniesReadsZipAndDelegatesToCompanyService() throws IOException {
        byte[] zipBytes = createCompaniesZip();

        FilePart filePart = mock(FilePart.class);
        var buffer = new DefaultDataBufferFactory().wrap(zipBytes);
        when(filePart.content()).thenReturn(Flux.just(buffer));
        when(companyService.importCompaniesFromLogosAndExcel(any(List.class), any(Resource.class)))
                .thenReturn(Mono.just(true));

        AmService amService = new AmService(companyService);

        StepVerifier.create(amService.uploadCompanies(filePart))
                .verifyComplete();

        verify(companyService, times(1)).importCompaniesFromLogosAndExcel(any(List.class), any(Resource.class));
    }

    private static byte[] createCompaniesZip() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(baos)) {
            zip.putNextEntry(new ZipEntry("companies.xlsx"));
            zip.write(new byte[0]);
            zip.closeEntry();

            zip.putNextEntry(new ZipEntry("logos/company-010.png"));
            zip.write(new byte[]{1, 2, 3});
            zip.closeEntry();
        }
        return baos.toByteArray();
    }
}
