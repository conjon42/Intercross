package org.phenoapps.intercross.brapi.clients

import org.phenoapps.intercross.brapi.BrApiServiceGenerator
import org.phenoapps.intercross.brapi.interfaces.GermplasmService

class BrApiClients(private val token: String) {

    val germplasm by lazy {

        BrApiServiceGenerator
                .createService(GermplasmService::class.java, token)

    }

}
