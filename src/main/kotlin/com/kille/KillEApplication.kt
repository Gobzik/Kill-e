package com.kille

import com.kille.config.BookServiceProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableConfigurationProperties(BookServiceProperties::class)
@EntityScan(basePackages = ["com.kille.infrastructure.persistence.entity"]) // updated to com.kille
class KillEApplication

fun main(args: Array<String>) {
    runApplication<KillEApplication>(*args)
}
