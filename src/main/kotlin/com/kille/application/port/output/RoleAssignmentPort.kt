package com.kille.application.port.output

import java.util.UUID

interface RoleAssignmentPort {
    fun assignSubscriptionRole(userId: UUID, plan: String)
    fun clearSubscriptionRoles(userId: UUID)
}