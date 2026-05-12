package com.kille.infrastructure.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value("\${jwt.secret:mysecretkeymysecretkeymysecretkey12}")
    private val secret: String,
    @Value("\${jwt.expiration:3600000}")
    private val expirationMs: Long
) {
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())
    private val parser: JwtParser = Jwts.parser()
        .verifyWith(secretKey)
        .build()

    fun generateToken(username: String, roles: List<String>): String {
        val now = Date()
        return Jwts.builder()
            .subject(username)
            .claim("roles", roles)
            .issuedAt(now)
            .expiration(Date(now.time + expirationMs))
            .signWith(secretKey)
            .compact()
    }

    fun extractUsername(token: String): String {
        return getClaims(token).subject!!
    }

    @Suppress("UNCHECKED_CAST")
    fun extractRoles(token: String): List<String> {
        val rolesClaim = getClaims(token)["roles"]
        return (rolesClaim as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    }

    fun isTokenValid(token: String): Boolean {
        return try {
            getClaims(token)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun getClaims(token: String): Claims {
        return parser.parseSignedClaims(token).payload
    }
}