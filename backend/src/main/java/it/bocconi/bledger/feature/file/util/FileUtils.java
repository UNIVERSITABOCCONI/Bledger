package it.bocconi.bledger.feature.file.util;

import it.bocconi.bledger.feature.file.entity.BcFile;
import it.bocconi.bledger.feature.file.entity.BcFileBinary;
import it.bocconi.bledger.feature.file.enums.FileType;
import it.bocconi.bledger.reactive.util.ReactiveUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;
import reactor.core.publisher.Mono;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {

    private FileUtils() {
        // Utility class, no instantiation allowed
    }

    @NotNull
    public static Mono<byte[]> getBytesFromFilePartMono(FilePart part) {
        return DataBufferUtils.join(part.content())
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return bytes;
                });
    }

    public static Mono<FileLogoData> processLogoMono(Resource logo) {
        return ReactiveUtils.scheduleOnBundleElasticMono(() -> processLogo(logo));
    }

    public static Mono<FileLogoData> processLogoMono(String fileName, byte[] bytes) {
        return ReactiveUtils.scheduleOnBundleElasticMono(() -> processLogo(fileName, bytes));
    }

    public record FileLogoData(String companyId, BcFile bcFile, BcFileBinary bcFileBinary) {}

    private static FileLogoData processLogo(Resource logo) {
        try (InputStream is = logo.getInputStream()) {
            String fileName = logo.getFilename();
            byte[] binaryData = is.readAllBytes();
            return processLogoInternal(fileName, binaryData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static FileLogoData processLogo(String fileName, byte[] binaryData) {
        return processLogoInternal(fileName, binaryData);
    }

    private static FileLogoData processLogoInternal(String fileName, byte[] binaryData) {
        String companyId = fileName.substring(0, fileName.lastIndexOf('.'));

        try (BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(binaryData))) {

            String mimeType = mimeTypeDetection(bis, fileName);
            String keccak256 = hashFile(binaryData);

            BcFileBinary fileBinary = BcFileBinary.builder()
                    .fileBinary(binaryData)
                    .build();
            fileBinary.prePersist();

            BcFile file = BcFile.builder()
                    .fileName(fileName)
                    .type(FileType.PROFILE_IMAGE)
                    .mimeType(mimeType)
                    .keccak256(keccak256)
                    .size((long) binaryData.length)
                    .binaryId(fileBinary.getId())
                    .build();
            file.prePersist();

            return new FileLogoData(companyId, file, fileBinary);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static String mimeTypeDetection(InputStream is, String fileName) throws IOException {
        TikaConfig tikaConfig = TikaConfig.getDefaultConfig();
        Detector detector = tikaConfig.getDetector();

        Metadata metadata = new Metadata();
        metadata.set(Metadata.TIKA_MIME_FILE, fileName);

        MediaType mediaType = detector.detect(is, metadata);

        return mediaType.toString();
    }

    public static Mono<String> hashFileMono(byte[] binaryData){
        return ReactiveUtils.scheduleOnBundleElasticMono(() ->hashFile(binaryData));
    }

    public static String hashFile(byte[] binaryData) {
        byte[] hash = Hash.sha3(binaryData);
        return Numeric.toHexStringNoPrefix(hash);
    }
}
