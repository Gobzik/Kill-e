package org.example.kille

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["org.example.kille", "com.yourapp"])
@EntityScan(basePackages = ["com.yourapp.infrastructure.persistence.entity"])
class KillEApplication

fun main(args: Array<String>) {
    runApplication<KillEApplication>(*args)
}
