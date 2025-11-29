package com.clinic.users.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

@Configuration
public class CognitoConfig {

    @Value("${aws.region}")
    private String region;

    @Value("${aws.auth.mode:default}") // static | profile | default
    private String authMode;

    @Value("${aws.profile:}")
    private String profile;

    @Value("${aws.accessKeyId:}")
    private String accessKeyId;

    @Value("${aws.secretAccessKey:}")
    private String secretAccessKey;

    @Value("${aws.sessionToken:}")
    private String sessionToken;

    @Bean
    public CognitoIdentityProviderClient cognitoClient() {
        AwsCredentialsProvider provider = switch (authMode.toLowerCase()) {
            case "static" -> {
                if (sessionToken != null && !sessionToken.isBlank()) {
                    yield StaticCredentialsProvider.create(
                            AwsSessionCredentials.create(accessKeyId, secretAccessKey, sessionToken));
                }
                yield StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKeyId, secretAccessKey));
            }
            case "profile" -> ProfileCredentialsProvider.create(
                    (profile == null || profile.isBlank()) ? "default" : profile);
            default -> DefaultCredentialsProvider.create();
        };

        return CognitoIdentityProviderClient.builder()
                .region(Region.of(region))
                .credentialsProvider(provider)
                .build();
    }
}
