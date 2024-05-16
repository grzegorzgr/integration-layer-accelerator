package sfdc.properties;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.kainos.exception.TechnicalException;

import lombok.Data;

@Data
@Component
@ConfigurationProperties("sfdc.request.add-file")
public class SfdcAddFileRequestProperties {

    private static final String DEFAULT_SFDC_VERSION_DATA_FILENAME = "defaultSfdcVersionData.txt";

    private String versionData = loadDefaultVersionDataFromFile();

    private String loadDefaultVersionDataFromFile() {
        try {
            Resource resource = new ClassPathResource(DEFAULT_SFDC_VERSION_DATA_FILENAME);
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ioException) {
            throw TechnicalException.builder()
                .message("Property: \\'sfdc.request.add-file.versionData\\' not provided and default value couldn't be fetched from file.")
                .cause(ioException)
                .build();
        }
    }
}
