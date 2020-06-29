/**
 * BrAPI-Germplasm
 * The Breeding API (BrAPI) is a Standardized REST ful Web Service API Specification for communicating Plant Breeding Data. BrAPI allows for easy data sharing between databases and tools involved in plant breeding. <div class=\"brapi-section\"> <h2 class=\"brapi-section-title\">General Reference Documentation</h2> <div class=\"gen-info-link\"><a href=\"https://github.com/plantbreeding/API/blob/master/Specification/GeneralInfo/URL_Structure.md\">URL Structure</a></div> <div class=\"gen-info-link\"><a href=\"https://github.com/plantbreeding/API/blob/master/Specification/GeneralInfo/Response_Structure.md\">Response Structure</a></div> <div class=\"gen-info-link\"><a href=\"https://github.com/plantbreeding/API/blob/master/Specification/GeneralInfo/Date_Time_Encoding.md\">Date/Time Encoding</a></div> <div class=\"gen-info-link\"><a href=\"https://github.com/plantbreeding/API/blob/master/Specification/GeneralInfo/Location_Encoding.md\">Location Encoding</a></div> <div class=\"gen-info-link\"><a href=\"https://github.com/plantbreeding/API/blob/master/Specification/GeneralInfo/Error_Handling.md\">Error Handling</a></div> <div class=\"gen-info-link\"><a href=\"https://github.com/plantbreeding/API/blob/master/Specification/GeneralInfo/Search_Services.md\">Search Services</a></div> </div>  <div class=\"brapi-section\"> <h2 class=\"brapi-section-title\">BrAPI Core</h2> <div class=\"brapi-section-description\">The BrAPI Core module contains high level entities used for organization and management. This includes Programs, Trials, Studies, Locations, People, and Lists</div> <div class=\"version-number\">V2.0</div> <div class=\"link-btn\"><a href=\"https://github.com/plantbreeding/API/tree/master/Specification/BrAPI-Core\">GitHub</a></div> <div class=\"link-btn\"><a href=\"https://app.swaggerhub.com/apis/PlantBreedingAPI/BrAPI-Core\">SwaggerHub</a></div> <div class=\"link-btn\"><a href=\"https://brapicore.docs.apiary.io\">Apiary</a></div> <div class=\"stop-float\"></div> </div>  <div class=\"brapi-section\"> <h2 class=\"brapi-section-title\">BrAPI Phenotyping</h2> <div class=\"brapi-section-description\">The BrAPI Phenotyping module contains entities related to phenotypic observations. This includes Observation Units, Observations, Observation Variables, Traits, Scales, Methods, and Images</div> <div class=\"version-number\">V2.0</div> <div class=\"link-btn\"><a href=\"https://github.com/plantbreeding/API/tree/master/Specification/BrAPI-Phenotyping\">GitHub</a></div> <div class=\"link-btn\"><a href=\"https://app.swaggerhub.com/apis/PlantBreedingAPI/BrAPI-Phenotyping\">SwaggerHub</a></div> <div class=\"link-btn\"><a href=\"https://brapiphenotyping.docs.apiary.io\">Apiary</a></div> <div class=\"stop-float\"></div> </div>  <div class=\"brapi-section\"> <h2 class=\"brapi-section-title\">BrAPI Genotyping</h2> <div class=\"brapi-section-description\">The BrAPI Genotyping module contains entities related to genotyping analysis. This includes Samples, Markers, Variant Sets, Variants, Call Sets, Calls, References, Reads, and Vendor Orders</div> <div class=\"version-number\">V2.0</div> <div class=\"link-btn\"><a href=\"https://github.com/plantbreeding/API/tree/master/Specification/BrAPI-Genotyping\">GitHub</a></div> <div class=\"link-btn\"><a href=\"https://app.swaggerhub.com/apis/PlantBreedingAPI/BrAPI-Genotyping\">SwaggerHub</a></div> <div class=\"link-btn\"><a href=\"https://brapigenotyping.docs.apiary.io\">Apiary</a></div> <div class=\"stop-float\"></div> </div>  <div class=\"current-brapi-section brapi-section\"> <h2 class=\"brapi-section-title\">BrAPI Germplasm</h2> <div class=\"brapi-section-description\">The BrAPI Germplasm module contains entities related to germplasm management. This includes Germplasm, Germplasm Attributes, Seed Lots, Crosses, Pedigree, and Progeny</div> <div class=\"version-number\">V2.0</div> <div class=\"link-btn\"><a href=\"https://github.com/plantbreeding/API/tree/master/Specification/BrAPI-Germplasm\">GitHub</a></div> <div class=\"link-btn\"><a href=\"https://app.swaggerhub.com/apis/PlantBreedingAPI/BrAPI-Germplasm\">SwaggerHub</a></div> <div class=\"link-btn\"><a href=\"https://brapigermplasm.docs.apiary.io\">Apiary</a></div> <div class=\"stop-float\"></div> </div>  <style> .link-btn{ float: left;  margin: 2px 10px 0 0;  padding: 0 5px;  border-radius: 5px;  background-color: #ddd; } .stop-float{   clear: both; } .version-number{   float: left;    margin: 5px 10px 0 5px; } .brapi-section-title{   margin: 0 10px 0 0;   font-size: 20px; } .current-brapi-section{   font-weight: bolder;   border-radius: 5px;    background-color: #ddd; } .brapi-section{   padding: 5px 5px;  } .brapi-section-description{   margin: 5px 0 0 5px; } </style>
 *
 * OpenAPI spec version: 2.0
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */
package io.swagger.client.apis

import io.swagger.client.infrastructure.ApiClient
import io.swagger.client.infrastructure.ClientError
import io.swagger.client.infrastructure.ClientException
import io.swagger.client.infrastructure.MultiValueMap
import io.swagger.client.infrastructure.RequestMethod
import io.swagger.client.infrastructure.ResponseType
import io.swagger.client.infrastructure.ServerError
import io.swagger.client.infrastructure.ServerException
import io.swagger.client.infrastructure.Success
import io.swagger.client.models.GermplasmAttributeCategoryListResponse
import io.swagger.client.models.GermplasmAttributeListResponse
import io.swagger.client.models.GermplasmAttributeNewRequest
import io.swagger.client.models.GermplasmAttributeSearchRequest
import io.swagger.client.models.GermplasmAttributeSingleResponse
import org.phenoapps.intercross.io.swagger.client.infrastructure.RequestConfig

class GermplasmAttributesApi(basePath: kotlin.String = "https://test-server.brapi.org/brapi/v2") : ApiClient(basePath) {

    /**
     * Get the details for a specific Germplasm Attribute
     * Get the details for a specific Germplasm Attribute
     * @param attributeDbId The unique id for an attribute 
     * @param authorization HTTP HEADER - Token used for Authorization   &lt;strong&gt; Bearer {token_string} &lt;/strong&gt; (optional)
     * @return GermplasmAttributeSingleResponse
     */
    @Suppress("UNCHECKED_CAST")
    fun attributesAttributeDbIdGet(attributeDbId: kotlin.String, authorization: kotlin.String? = null): GermplasmAttributeSingleResponse {
        
        val localVariableHeaders: kotlin.collections.Map<kotlin.String, kotlin.String?> = mapOf("Authorization" to authorization)
        val localVariableConfig = RequestConfig(
                RequestMethod.GET,
                "/attributes/{attributeDbId}".replace("{" + "attributeDbId" + "}", "$attributeDbId"), headers = localVariableHeaders
        )
        val response = request<GermplasmAttributeSingleResponse>(
                localVariableConfig, null
        )

        return when (response.responseType) {
            ResponseType.Success -> (response as Success<*>).data as GermplasmAttributeSingleResponse
            ResponseType.Informational -> TODO()
            ResponseType.Redirection -> TODO()
            ResponseType.ClientError -> throw ClientException((response as ClientError<*>).body as? String ?: "Client error")
            ResponseType.ServerError -> throw ServerException((response as ServerError<*>).message ?: "Server error")
        }
    }
    /**
     * Update an existing Germplasm Attribute
     * Update an existing Germplasm Attribute
     * @param attributeDbId The unique id for an attribute 
     * @param body  (optional)
     * @param authorization HTTP HEADER - Token used for Authorization   &lt;strong&gt; Bearer {token_string} &lt;/strong&gt; (optional)
     * @return GermplasmAttributeSingleResponse
     */
    @Suppress("UNCHECKED_CAST")
    fun attributesAttributeDbIdPut(attributeDbId: kotlin.String, body: GermplasmAttributeNewRequest? = null, authorization: kotlin.String? = null): GermplasmAttributeSingleResponse {
        val localVariableBody: kotlin.Any? = body
        
        val localVariableHeaders: kotlin.collections.Map<kotlin.String, kotlin.String?> = mapOf("Authorization" to authorization)
        val localVariableConfig = RequestConfig(
                RequestMethod.PUT,
                "/attributes/{attributeDbId}".replace("{" + "attributeDbId" + "}", "$attributeDbId"), headers = localVariableHeaders
        )
        val response = request<GermplasmAttributeSingleResponse>(
                localVariableConfig, localVariableBody
        )

        return when (response.responseType) {
            ResponseType.Success -> (response as Success<*>).data as GermplasmAttributeSingleResponse
            ResponseType.Informational -> TODO()
            ResponseType.Redirection -> TODO()
            ResponseType.ClientError -> throw ClientException((response as ClientError<*>).body as? String ?: "Client error")
            ResponseType.ServerError -> throw ServerException((response as ServerError<*>).message ?: "Server error")
        }
    }
    /**
     * Get the Categories of Germplasm Attributes
     * List all available attribute categories.
     * @param page Used to request a specific page of data to be returned.  The page indexing starts at 0 (the first page is &#x27;page&#x27;&#x3D; 0). Default is &#x60;0&#x60;. (optional)
     * @param pageSize The size of the pages to be returned. Default is &#x60;1000&#x60;. (optional)
     * @param authorization HTTP HEADER - Token used for Authorization   &lt;strong&gt; Bearer {token_string} &lt;/strong&gt; (optional)
     * @return GermplasmAttributeCategoryListResponse
     */
    @Suppress("UNCHECKED_CAST")
    fun attributesCategoriesGet(page: kotlin.Int? = null, pageSize: kotlin.Int? = null, authorization: kotlin.String? = null): GermplasmAttributeCategoryListResponse {
        val localVariableQuery: MultiValueMap = mapOf("page" to listOf("$page"), "pageSize" to listOf("$pageSize"))
        val localVariableHeaders: kotlin.collections.Map<kotlin.String, kotlin.String?> = mapOf("Authorization" to authorization)
        val localVariableConfig = RequestConfig(
                RequestMethod.GET,
                "/attributes/categories", query = localVariableQuery, headers = localVariableHeaders
        )
        val response = request<GermplasmAttributeCategoryListResponse>(
                localVariableConfig, null
        )

        return when (response.responseType) {
            ResponseType.Success -> (response as Success<*>).data as GermplasmAttributeCategoryListResponse
            ResponseType.Informational -> TODO()
            ResponseType.Redirection -> TODO()
            ResponseType.ClientError -> throw ClientException((response as ClientError<*>).body as? String ?: "Client error")
            ResponseType.ServerError -> throw ServerException((response as ServerError<*>).message ?: "Server error")
        }
    }
    /**
     * Get the Germplasm Attributes
     * List available attributes.
     * @param attributeCategory The general category for the attribute. very similar to Trait class. (optional)
     * @param attributeDbId The unique id for an attribute (optional)
     * @param attributeName The human readable name for an attribute (optional)
     * @param germplasmDbId Get all attributes associated with this germplasm (optional)
     * @param externalReferenceID An external reference ID. Could be a simple string or a URI. (use with &#x60;externalReferenceSource&#x60; parameter) (optional)
     * @param externalReferenceSource An identifier for the source system or database of an external reference (use with &#x60;externalReferenceID&#x60; parameter) (optional)
     * @param page Used to request a specific page of data to be returned.  The page indexing starts at 0 (the first page is &#x27;page&#x27;&#x3D; 0). Default is &#x60;0&#x60;. (optional)
     * @param pageSize The size of the pages to be returned. Default is &#x60;1000&#x60;. (optional)
     * @param authorization HTTP HEADER - Token used for Authorization   &lt;strong&gt; Bearer {token_string} &lt;/strong&gt; (optional)
     * @return GermplasmAttributeListResponse
     */
    @Suppress("UNCHECKED_CAST")
    fun attributesGet(attributeCategory: kotlin.String? = null, attributeDbId: kotlin.String? = null, attributeName: kotlin.String? = null, germplasmDbId: kotlin.String? = null, externalReferenceID: kotlin.String? = null, externalReferenceSource: kotlin.String? = null, page: kotlin.Int? = null, pageSize: kotlin.Int? = null, authorization: kotlin.String? = null): GermplasmAttributeListResponse {
        val localVariableQuery: MultiValueMap = mapOf("attributeCategory" to listOf("$attributeCategory"), "attributeDbId" to listOf("$attributeDbId"), "attributeName" to listOf("$attributeName"), "germplasmDbId" to listOf("$germplasmDbId"), "externalReferenceID" to listOf("$externalReferenceID"), "externalReferenceSource" to listOf("$externalReferenceSource"), "page" to listOf("$page"), "pageSize" to listOf("$pageSize"))
        val localVariableHeaders: kotlin.collections.Map<kotlin.String, kotlin.String?> = mapOf("Authorization" to authorization)
        val localVariableConfig = RequestConfig(
                RequestMethod.GET,
                "/attributes", query = localVariableQuery, headers = localVariableHeaders
        )
        val response = request<GermplasmAttributeListResponse>(
                localVariableConfig, null
        )

        return when (response.responseType) {
            ResponseType.Success -> (response as Success<*>).data as GermplasmAttributeListResponse
            ResponseType.Informational -> TODO()
            ResponseType.Redirection -> TODO()
            ResponseType.ClientError -> throw ClientException((response as ClientError<*>).body as? String ?: "Client error")
            ResponseType.ServerError -> throw ServerException((response as ServerError<*>).message ?: "Server error")
        }
    }
    /**
     * Create new Germplasm Attributes
     * Create new Germplasm Attributes
     * @param body  (optional)
     * @param authorization HTTP HEADER - Token used for Authorization   &lt;strong&gt; Bearer {token_string} &lt;/strong&gt; (optional)
     * @return GermplasmAttributeListResponse
     */
    @Suppress("UNCHECKED_CAST")
    fun attributesPost(body: kotlin.Array<GermplasmAttributeNewRequest>? = null, authorization: kotlin.String? = null): GermplasmAttributeListResponse {
        val localVariableBody: kotlin.Any? = body
        
        val localVariableHeaders: kotlin.collections.Map<kotlin.String, kotlin.String?> = mapOf("Authorization" to authorization)
        val localVariableConfig = RequestConfig(
                RequestMethod.POST,
                "/attributes", headers = localVariableHeaders
        )
        val response = request<GermplasmAttributeListResponse>(
                localVariableConfig, localVariableBody
        )

        return when (response.responseType) {
            ResponseType.Success -> (response as Success<*>).data as GermplasmAttributeListResponse
            ResponseType.Informational -> TODO()
            ResponseType.Redirection -> TODO()
            ResponseType.ClientError -> throw ClientException((response as ClientError<*>).body as? String ?: "Client error")
            ResponseType.ServerError -> throw ServerException((response as ServerError<*>).message ?: "Server error")
        }
    }
    /**
     * Submit a search request for Germplasm Attributes
     * Search for a set of Germplasm Attributes based on some criteria          See Search Services for additional implementation details.
     * @param body  (optional)
     * @param authorization HTTP HEADER - Token used for Authorization   &lt;strong&gt; Bearer {token_string} &lt;/strong&gt; (optional)
     * @return GermplasmAttributeListResponse
     */
    @Suppress("UNCHECKED_CAST")
    fun searchAttributesPost(body: GermplasmAttributeSearchRequest? = null, authorization: kotlin.String? = null): GermplasmAttributeListResponse {
        val localVariableBody: kotlin.Any? = body
        
        val localVariableHeaders: kotlin.collections.Map<kotlin.String, kotlin.String?> = mapOf("Authorization" to authorization)
        val localVariableConfig = RequestConfig(
                RequestMethod.POST,
                "/search/attributes", headers = localVariableHeaders
        )
        val response = request<GermplasmAttributeListResponse>(
                localVariableConfig, localVariableBody
        )

        return when (response.responseType) {
            ResponseType.Success -> (response as Success<*>).data as GermplasmAttributeListResponse
            ResponseType.Informational -> TODO()
            ResponseType.Redirection -> TODO()
            ResponseType.ClientError -> throw ClientException((response as ClientError<*>).body as? String ?: "Client error")
            ResponseType.ServerError -> throw ServerException((response as ServerError<*>).message ?: "Server error")
        }
    }
    /**
     * Get the results of a Germplasm Attributes search request
     * Get the results of a Germplasm Attributes search request  See Search Services for additional implementation details.
     * @param searchResultsDbId Unique identifier which references the search results 
     * @param page Used to request a specific page of data to be returned.  The page indexing starts at 0 (the first page is &#x27;page&#x27;&#x3D; 0). Default is &#x60;0&#x60;. (optional)
     * @param pageSize The size of the pages to be returned. Default is &#x60;1000&#x60;. (optional)
     * @param authorization HTTP HEADER - Token used for Authorization   &lt;strong&gt; Bearer {token_string} &lt;/strong&gt; (optional)
     * @return GermplasmAttributeListResponse
     */
    @Suppress("UNCHECKED_CAST")
    fun searchAttributesSearchResultsDbIdGet(searchResultsDbId: kotlin.String, page: kotlin.Int? = null, pageSize: kotlin.Int? = null, authorization: kotlin.String? = null): GermplasmAttributeListResponse {
        val localVariableQuery: MultiValueMap = mapOf("page" to listOf("$page"), "pageSize" to listOf("$pageSize"))
        val localVariableHeaders: kotlin.collections.Map<kotlin.String, kotlin.String?> = mapOf("Authorization" to authorization)
        val localVariableConfig = RequestConfig(
                RequestMethod.GET,
                "/search/attributes/{searchResultsDbId}".replace("{" + "searchResultsDbId" + "}", "$searchResultsDbId"), query = localVariableQuery, headers = localVariableHeaders
        )
        val response = request<GermplasmAttributeListResponse>(
                localVariableConfig, null
        )

        return when (response.responseType) {
            ResponseType.Success -> (response as Success<*>).data as GermplasmAttributeListResponse
            ResponseType.Informational -> TODO()
            ResponseType.Redirection -> TODO()
            ResponseType.ClientError -> throw ClientException((response as ClientError<*>).body as? String ?: "Client error")
            ResponseType.ServerError -> throw ServerException((response as ServerError<*>).message ?: "Server error")
        }
    }
}
