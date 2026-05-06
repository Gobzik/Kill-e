package com.kille.infrastructure.persistence.repository

import com.kille.infrastructure.persistence.entity.BookEntityJpa
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface BookJpaRepository : JpaRepository<BookEntityJpa, UUID>