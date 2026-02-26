package com.yourapp.infrastructure.logging.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy

@Configuration
@EnableAspectJAutoProxy
@EnableConfigurationProperties(LoggingProperties::class)
class LoggingConfiguration
