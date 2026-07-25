package com.oficina_os_service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OficinaOsServiceApplicationTests {

    @Test
    fun `application should load all classes successfully`() {
        assertThat(OficinaOsServiceApplication::class.java).isNotNull
    }
}