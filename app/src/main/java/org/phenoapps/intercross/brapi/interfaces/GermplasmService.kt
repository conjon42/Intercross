package org.phenoapps.intercross.brapi.interfaces

import io.swagger.client.models.Cross
import retrofit2.http.GET

interface GermplasmService {

    @GET("/crosses")
    fun getCrosses(): List<Cross>

}