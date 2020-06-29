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
package io.swagger.client.models

/**
 * 
 * @param additionalInfo Additional arbitrary info
 * @param commonCropName Crop name (examples: \"Maize\", \"Wheat\")
 * @param contextOfUse Indication of how trait is routinely used. (examples: [\"Trial evaluation\", \"Nursery evaluation\"])
 * @param defaultValue Variable default value. (examples: \"red\", \"2.3\", etc.)
 * @param documentationURL A URL to the human readable documentation of this object
 * @param externalReferences 
 * @param growthStage Growth stage at which measurement is made (examples: \"flowering\")
 * @param institution Name of institution submitting the variable
 * @param language 2 letter ISO 639-1 code for the language of submission of the variable.
 * @param method 
 * @param ontologyReference 
 * @param scale 
 * @param scientist Name of scientist submitting the variable.
 * @param status Variable status. (examples: \"recommended\", \"obsolete\", \"legacy\", etc.)
 * @param submissionTimestamp Timestamp when the Variable was added (ISO 8601)
 * @param synonyms Other variable names
 * @param trait 
 */
data class VariableBaseClass (
    val method: AllOfVariableBaseClassMethod,
    val scale: AllOfVariableBaseClassScale,
    val trait: AllOfVariableBaseClassTrait
,
    /* Additional arbitrary info */
    val additionalInfo: kotlin.collections.Map<kotlin.String, kotlin.String>? = null,
    /* Crop name (examples: \"Maize\", \"Wheat\") */
    val commonCropName: kotlin.String? = null,
    /* Indication of how trait is routinely used. (examples: [\"Trial evaluation\", \"Nursery evaluation\"]) */
    val contextOfUse: kotlin.Array<kotlin.String>? = null,
    /* Variable default value. (examples: \"red\", \"2.3\", etc.) */
    val defaultValue: kotlin.String? = null,
    /* A URL to the human readable documentation of this object */
    val documentationURL: kotlin.String? = null,
    val externalReferences: ExternalReferences? = null,
    /* Growth stage at which measurement is made (examples: \"flowering\") */
    val growthStage: kotlin.String? = null,
    /* Name of institution submitting the variable */
    val institution: kotlin.String? = null,
    /* 2 letter ISO 639-1 code for the language of submission of the variable. */
    val language: kotlin.String? = null,
    val ontologyReference: OntologyReference? = null,
    /* Name of scientist submitting the variable. */
    val scientist: kotlin.String? = null,
    /* Variable status. (examples: \"recommended\", \"obsolete\", \"legacy\", etc.) */
    val status: kotlin.String? = null,
    /* Timestamp when the Variable was added (ISO 8601) */
    val submissionTimestamp: java.time.LocalDateTime? = null,
    /* Other variable names */
    val synonyms: kotlin.Array<kotlin.String>? = null
) {
}