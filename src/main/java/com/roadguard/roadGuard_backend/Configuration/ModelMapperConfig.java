package com.roadguard.roadGuard_backend.Configuration;

import com.roadguard.roadGuard_backend.dto.HazardReportResponseDto;
import com.roadguard.roadGuard_backend.entity.HazardReport;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

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
                });
        return modelMapper;
    }
}
