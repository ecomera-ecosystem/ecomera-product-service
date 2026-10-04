package com.ecomera.product.batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfig {

    @Bean
    public FlatFileItemReader<String> productImportReader() {
        return new FlatFileItemReaderBuilder<String>()
                .name("productImportReader")
                .resource(new ClassPathResource("products-sample.csv"))
                .linesToSkip(1)
                .lineMapper(this::lineMapper)
                .build();
    }

    @Bean
    public Step productImportStep(JobRepository jobRepository,
                                  PlatformTransactionManager transactionManager,
                                  FlatFileItemReader<String> productImportReader) {
        return new StepBuilder("productImportStep", jobRepository)
                .<String, String>chunk(10, transactionManager)
                .reader(productImportReader)
                .writer(items -> {
                    // #26 replaces this with real upsert logic
                })
                .build();
    }

    @Bean
    public Job productImportJob(JobRepository jobRepository, Step step) {
        return new JobBuilder("productImportJob", jobRepository)
                .start(step)
                .build();
    }

    private String lineMapper(String line, int lineNumber) {
        return line;
    }
}
