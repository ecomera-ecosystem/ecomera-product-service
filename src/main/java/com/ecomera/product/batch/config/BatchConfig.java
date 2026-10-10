package com.ecomera.product.batch.config;

import com.ecomera.product.batch.model.ProductImportItem;
import com.ecomera.product.batch.processor.ProductImportProcessor;
import com.ecomera.product.batch.writer.ProductImportWriter;
import jakarta.persistence.PersistenceException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfig {

    @Bean
    public FlatFileItemReader<ProductImportItem> productImportReader() {
        return new FlatFileItemReaderBuilder<ProductImportItem>()
                .name("productImportReader")
                .resource(new ClassPathResource("products-sample.csv"))
                .linesToSkip(1)
                .delimited()
                .names("sku", "title", "description", "price", "stock", "categoryId", "color", "size", "imageUrl")
                .targetType(ProductImportItem.class)
                .build();
    }

    @Bean
    public Step productImportStep(JobRepository jobRepository,
                                  PlatformTransactionManager transactionManager,
                                  FlatFileItemReader<ProductImportItem> productImportReader,
                                  ProductImportProcessor productImportProcessor,
                                  ProductImportWriter productImportWriter) {
        return new StepBuilder("productImportStep", jobRepository)
                .<ProductImportItem, ProductImportItem>chunk(100, transactionManager)
                .reader(productImportReader)
                .processor(productImportProcessor)
                .writer(productImportWriter)

                .faultTolerant()

                .skipLimit(50)
                .skip(FlatFileParseException.class)
                .skip(PersistenceException.class)
                .build();
    }

    @Bean
    public Job productImportJob(JobRepository jobRepository, Step step) {
        return new JobBuilder("productImportJob", jobRepository)
                .start(step)
                .build();
    }
}