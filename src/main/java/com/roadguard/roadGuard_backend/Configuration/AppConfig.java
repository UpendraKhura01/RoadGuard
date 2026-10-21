package com.roadguard.roadGuard_backend.Configuration;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roadguard.roadGuard_backend.dto.HazardReportResponseDto;
import com.roadguard.roadGuard_backend.entity.AiAssessment;
import com.roadguard.roadGuard_backend.entity.HazardReport;
import com.roadguard.roadGuard_backend.entity.ReportSecurityCheck;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper(){
        ModelMapper modelMapper = new ModelMapper();

        //Global Configurations
        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setSkipNullEnabled(true);

        //Custom Mappings

        //For HazardReport to HazardReportResponseDto
        modelMapper.createTypeMap(HazardReport.class, HazardReportResponseDto.class)
                .addMappings(mapper -> {
                    mapper.map(HazardReport::getId, HazardReportResponseDto::setReportId);
                    mapper.map(src -> src.getUser().getId(), HazardReportResponseDto::setUserId);
                    mapper.map(src -> src.getUser().getName(), HazardReportResponseDto::setUserName);
                    mapper.map(src ->{
                        ReportSecurityCheck securityCheck = src.getReportSecurityCheck();
                        return securityCheck != null ? securityCheck.getDecisionReason() : null;
                    }, HazardReportResponseDto::setDecisionReason);
                    mapper.map(src -> {
                        AiAssessment aiAssessment = src.getAiAssessment();
                        return aiAssessment != null ? aiAssessment.getExplanation() : null;
                    }, HazardReportResponseDto::setAiExplanation);
                });


        return modelMapper;
    }
    @Bean
    ObjectMapper objectMapper(){
        return new ObjectMapper();
    }

    @Value("${roadguard.geocoding.baseUrl}")
    private String baseUrl;

    @Bean
    public RestClient restClient(){
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.USER_AGENT, "RoadGuard/1.0")
                .build();
    }

    @Value("${roadguard.image.cloud_name}")
    private String cloudName;

    @Value("${roadguard.image.api_key}")
    private String apiKey;

    @Value("${roadguard.image.api_secret}")
    private String apiSecret;

    @Bean
    public Cloudinary getCloudinary(){
        Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true));
        return cloudinary;
    }


}
