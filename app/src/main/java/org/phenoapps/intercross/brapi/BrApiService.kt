package org.phenoapps.intercross.brapi

import android.util.Log
import com.google.android.gms.common.api.ApiException
import io.swagger.client.infrastructure.ClientException
import io.swagger.client.models.CrossesListResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.phenoapps.intercross.io.swagger.client.apis.CrossesApi


class BrApiService {

    private val viewModelJob = Job()

    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val client: CrossesApi by lazy {

        CrossesApi()

    }

    fun brapiCrosses(token: String): CrossesListResponse? {

        var response: CrossesListResponse? = null

        try {

            response = client.crossesGet(authorization = token)

        } catch (e: ApiException) {

            e.printStackTrace()

            Log.d("BrApi", "Error thrown while trying to get all crosses from BrApi.")

        } catch (e: ClientException) {

            e.printStackTrace()

            Log.d("BrApiClient", "Error from API client thrown to get all crosses from BrApi.")
        }

        return response

    }
}