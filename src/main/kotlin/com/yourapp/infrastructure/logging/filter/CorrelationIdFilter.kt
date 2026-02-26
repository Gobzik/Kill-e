package com.yourapp.infrastructure.logging.filter

import com.yourapp.application.logging.CorrelationIdContext
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class CorrelationIdFilter : OncePerRequestFilter() {

    companion object {
        private const val CORRELATION_ID_HEADER = "X-Correlation-ID"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val correlationId = request.getHeader(CORRELATION_ID_HEADER) ?: CorrelationIdContext.generate()
            CorrelationIdContext.set(correlationId)
            response.setHeader(CORRELATION_ID_HEADER, correlationId)

            filterChain.doFilter(request, response)
        } finally {
            CorrelationIdContext.clear()
        }
    }
}
