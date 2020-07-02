package org.phenoapps.intercross.brapi.repository

import org.phenoapps.intercross.brapi.clients.BrApiClients

class CrossRepository(private val token: String) {

    var client = BrApiClients(token).germplasm

    suspend fun getCrosses() = client.getCrosses()

}