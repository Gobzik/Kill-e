package com.kille.presentation.controller

import com.kille.application.service.PurchaseSubscriptionUseCase
import com.kille.domain.repository.SubscriptionRepository
import com.kille.presentation.dto.request.PurchaseSubscriptionRequest
import com.kille.presentation.dto.response.SubscriptionResponse
import com.kille.presentation.mapper.toResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/subscriptions")
class SubscriptionController(
    private val purchaseSubscriptionUseCase: PurchaseSubscriptionUseCase,
    private val subscriptionRepository: SubscriptionRepository
) {
    @PostMapping("/purchase")
    @ResponseStatus(HttpStatus.CREATED)
    fun purchase(@RequestBody request: PurchaseSubscriptionRequest): SubscriptionResponse {
        val subscription = purchaseSubscriptionUseCase.purchase(
            userId = request.userId,
            plan = request.plan,
            durationDays = request.durationDays
        )
        return subscription.toResponse()
    }

    @GetMapping("/{userId}")
    fun getByUser(@PathVariable userId: UUID): List<SubscriptionResponse> {
        return subscriptionRepository.findByUserId(userId).map { it.toResponse() }
    }
}
