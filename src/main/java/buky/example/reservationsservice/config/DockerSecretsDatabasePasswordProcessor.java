package buky.example.reservationsservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;

@Slf4j
public class DockerSecretsDatabasePasswordProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Resource usernameFile = new FileSystemResource("/run/secrets/db-username");
        Resource passwordFile = new FileSystemResource("/run/secrets/db-password");

        if (passwordFile.exists() && usernameFile.exists()) {
            try {
                String dbPassword = StreamUtils.copyToString(passwordFile.getInputStream(), Charset.defaultCharset());
                String dbUsername = StreamUtils.copyToString(usernameFile.getInputStream(), Charset.defaultCharset());

                // Set the properties directly in the environment
                var propertySources = environment.getPropertySources();
                propertySources.remove("spring.datasource.password");
                propertySources.remove("spring.datasource.username");

                System.setProperty("spring.datasource.password", dbPassword.trim());
                System.setProperty("spring.datasource.username", dbUsername.trim());

            } catch (IOException e) {
                return;
            }
        }
    }
}
