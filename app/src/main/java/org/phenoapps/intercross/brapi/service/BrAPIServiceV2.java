package org.phenoapps.intercross.brapi.service;

import android.app.Activity;
import android.content.Context;

import androidx.arch.core.util.Function;

import org.brapi.client.v2.BrAPIClient;
import org.brapi.client.v2.model.exceptions.ApiException;
import org.brapi.client.v2.model.queryParams.core.ProgramQueryParams;
import org.brapi.client.v2.model.queryParams.core.StudyQueryParams;
import org.brapi.client.v2.model.queryParams.core.TrialQueryParams;
import org.brapi.client.v2.model.queryParams.germplasm.CrossingProjectQueryParams;
import org.brapi.client.v2.model.queryParams.germplasm.GermplasmQueryParams;
import org.brapi.client.v2.model.queryParams.germplasm.PlannedCrossQueryParams;
import org.brapi.client.v2.modules.core.ProgramsApi;
import org.brapi.client.v2.modules.core.StudiesApi;
import org.brapi.client.v2.modules.core.TrialsApi;
import org.brapi.client.v2.modules.germplasm.CrossesApi;
import org.brapi.client.v2.modules.germplasm.CrossingProjectsApi;
import org.brapi.client.v2.modules.germplasm.GermplasmApi;
import org.brapi.client.v2.modules.phenotype.ImagesApi;
import org.brapi.client.v2.modules.phenotype.ObservationUnitsApi;
import org.brapi.client.v2.modules.phenotype.ObservationVariablesApi;
import org.brapi.client.v2.modules.phenotype.ObservationsApi;
import org.brapi.v2.model.BrAPIMetadata;
import org.brapi.v2.model.TimeAdapter;
import org.brapi.v2.model.core.BrAPIProgram;
import org.brapi.v2.model.core.BrAPIStudy;
import org.brapi.v2.model.core.BrAPITrial;
import org.brapi.v2.model.core.request.BrAPITrialSearchRequest;
import org.brapi.v2.model.core.response.BrAPIProgramListResponse;
import org.brapi.v2.model.core.response.BrAPIStudyListResponse;
import org.brapi.v2.model.core.response.BrAPITrialListResponse;
import org.brapi.v2.model.germ.BrAPICross;
import org.brapi.v2.model.germ.BrAPICrossingProject;
import org.brapi.v2.model.germ.BrAPIGermplasm;
import org.brapi.v2.model.germ.BrAPIPlannedCross;
import org.brapi.v2.model.germ.response.BrAPICrossesListResponse;
import org.brapi.v2.model.germ.response.BrAPICrossingProjectsListResponse;
import org.brapi.v2.model.germ.response.BrAPIGermplasmListResponse;
import org.brapi.v2.model.germ.response.BrAPIPlannedCrossesListResponse;
import org.brapi.v2.model.pheno.BrAPIImage;
import org.brapi.v2.model.pheno.BrAPIObservation;
import org.brapi.v2.model.pheno.BrAPIObservationUnit;
import org.brapi.v2.model.pheno.BrAPIPositionCoordinateTypeEnum;
import org.brapi.v2.model.pheno.request.BrAPIObservationUnitSearchRequest;
import org.brapi.v2.model.pheno.response.BrAPIImageListResponse;
import org.brapi.v2.model.pheno.response.BrAPIImageSingleResponse;
import org.brapi.v2.model.pheno.response.BrAPIObservationListResponse;
import org.brapi.v2.model.pheno.response.BrAPIObservationUnitListResponse;
import org.phenoapps.intercross.brapi.model.BrapiProgram;
import org.phenoapps.intercross.brapi.model.BrapiStudyDetails;
import org.phenoapps.intercross.brapi.model.BrapiTrial;
import org.phenoapps.intercross.brapi.model.FieldBookImage;
import org.phenoapps.intercross.brapi.model.Observation;
import org.phenoapps.intercross.util.KeyUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.android.gms.common.util.CollectionUtils.listOf;

public class BrAPIServiceV2 implements BrAPIService{

    private final Context context;
    private final ImagesApi imagesApi;
    private final StudiesApi studiesApi;
    private final ProgramsApi programsApi;
    private final TrialsApi trialsApi;
    private final CrossingProjectsApi crossingProjectsApi;
    private final CrossesApi crossesApi;
    private final GermplasmApi germplasmApi;
    private final ObservationsApi observationsApi;
    private final ObservationUnitsApi observationUnitsApi;
    private final ObservationVariablesApi traitsApi;

    private KeyUtil mKeyUtil;

    public BrAPIServiceV2(Context context) {
        this.context = context;
        this.mKeyUtil = new KeyUtil(context);
        // Make timeout longer. Set it to 60 seconds for now
        BrAPIClient apiClient = new BrAPIClient(BrAPIService.getBrapiUrl(context), 60000);
//        try {
//            //TODO
////             apiClient.authenticate(t -> context.getSharedPreferences("Settings", 0)
////                        .getString(mKeyUtil.getBrapiKeys().getBrapiTokenKey(), null));
//        } catch (ApiException e) {
//            e.printStackTrace();
//        }
        this.imagesApi = new ImagesApi(apiClient);
        this.studiesApi = new StudiesApi(apiClient);
        this.programsApi = new ProgramsApi(apiClient);
        this.trialsApi = new TrialsApi(apiClient);
        this.crossingProjectsApi = new CrossingProjectsApi(apiClient);
        this.germplasmApi = new GermplasmApi(apiClient);
        this.crossesApi = new CrossesApi(apiClient);
        this.traitsApi = new ObservationVariablesApi(apiClient);
        this.observationsApi = new ObservationsApi(apiClient);
        this.observationUnitsApi = new ObservationUnitsApi(apiClient);
    }

    private void updatePageInfo(BrapiPaginationManager paginationManager, BrAPIMetadata metadata){
        if(paginationManager.getContext() != null) { //null check for JUnits
            ((Activity) paginationManager.getContext())
                    .runOnUiThread(() -> paginationManager.updatePageInfo(metadata.getPagination().getTotalPages()));
        }
    }

    public void postImageMetaData(FieldBookImage image,
                                  final Function<FieldBookImage, Void> function,
                                  final Function<Integer, Void> failFunction) {
        try {
            BrapiV2ApiCallBack<BrAPIImageListResponse> callback = new BrapiV2ApiCallBack<BrAPIImageListResponse>() {
                @Override
                public void onSuccess(BrAPIImageListResponse imageResponse, int i, Map<String, List<String>> map) {
                    final BrAPIImage response = imageResponse.getResult().getData().get(0);
                    function.apply(mapToImage(response));
                }

                @Override
                public void onFailure(ApiException error, int statusCode, Map<String, List<String>> responseHeaders) {
                    failFunction.apply(error.getCode());
                }
            };

            BrAPIImage request = mapImage(image);
            imagesApi.imagesPostAsync(Arrays.asList(request), callback);

        } catch (ApiException e) {
            failFunction.apply(e.getCode());
            e.printStackTrace();
        }

    }

    private BrAPIImage mapImage(FieldBookImage image) {
        BrAPIImage request = new BrAPIImage();
        //request.setAdditionalInfo(image.getAdditionalInfo());
        request.setCopyright(image.getCopyright());
        request.setDescription(image.getDescription());
        request.setDescriptiveOntologyTerms(image.getDescriptiveOntologyTerms());
        request.setImageFileName(image.getFileName());
        request.setImageFileSize((int) image.getFileSize());
        request.setImageHeight(image.getHeight());
        request.setImageName(image.getImageName());
        request.setImageWidth(image.getWidth());
        request.setMimeType(image.getMimeType());
        request.setObservationUnitDbId(image.getUnitDbId());
        // TODO fix these
        //request.setImageLocation(image.getLocation());
        request.setImageTimeStamp(TimeAdapter.convertFrom(image.getTimestamp()));
        return request;
    }

    private FieldBookImage mapToImage(BrAPIImage image) {
        FieldBookImage request = new FieldBookImage();
        //request.setAdditionalInfo(image.getAdditionalInfo());
        request.setDescription(image.getDescription());
        request.setDescriptiveOntologyTerms(image.getDescriptiveOntologyTerms());
        request.setFileName(image.getImageFileName());
        request.setFileSize((int) image.getImageFileSize());
        request.setHeight(image.getImageHeight());
        request.setImageName(image.getImageName());
        request.setWidth(image.getImageWidth());
        request.setMimeType(image.getMimeType());
        request.setUnitDbId(image.getObservationUnitDbId());
        // TODO fix these
        //request.setLocation(image.getImageLocation());
        request.setTimestamp(TimeAdapter.convertFrom(image.getImageTimeStamp()));
        return request;
    }

    public void putImageContent(FieldBookImage image,
                                final Function<FieldBookImage, Void> function,
                                final Function<Integer, Void> failFunction) {
        try {

            BrapiV2ApiCallBack<BrAPIImageSingleResponse> callback = new BrapiV2ApiCallBack<BrAPIImageSingleResponse>() {
                @Override
                public void onSuccess(BrAPIImageSingleResponse imageResponse, int i, Map<String, List<String>> map) {
                    final BrAPIImage response = imageResponse.getResult();
                    function.apply(mapToImage(response));
                }

                @Override
                public void onFailure(ApiException error, int i, Map<String, List<String>> map) {
                    failFunction.apply(error.getCode());
                }
            };

            imagesApi.imagesImageDbIdImagecontentPutAsync(image.getDbId(), image.getImageData(), callback);

        } catch (ApiException e) {
            failFunction.apply(e.getCode());
            e.printStackTrace();
        }

    }

    public void putImage(FieldBookImage image,
                         final Function<FieldBookImage, Void> function,
                         final Function<Integer, Void> failFunction) {
        try {

            BrapiV2ApiCallBack<BrAPIImageSingleResponse> callback = new BrapiV2ApiCallBack<BrAPIImageSingleResponse>() {
                @Override
                public void onSuccess(BrAPIImageSingleResponse imageResponse, int i, Map<String, List<String>> map) {
                    final BrAPIImage response = imageResponse.getResult();
                    function.apply(mapToImage(response));
                }

                @Override
                public void onFailure(ApiException error, int i, Map<String, List<String>> map) {
                    failFunction.apply(error.getCode());
                }
            };

            BrAPIImage request = mapImage(image);
            imagesApi.imagesImageDbIdPutAsync(image.getDbId(), request, callback);

        } catch (ApiException e) {
            failFunction.apply(e.getCode());
            e.printStackTrace();
        }

    }

    public void getPrograms(final BrapiPaginationManager paginationManager,
                            final Function<List<BrapiProgram>, Void> function,
                            final Function<Integer, Void> failFunction) {
        Integer initPage = paginationManager.getPage();
        try {
           BrapiV2ApiCallBack<BrAPIProgramListResponse> callback = new BrapiV2ApiCallBack<BrAPIProgramListResponse>() {
               @Override
               public void onSuccess(BrAPIProgramListResponse programsResponse, int i, Map<String, List<String>> map) {
                   // Cancel processing if the page that was processed is not the page
                   // that we are currently on. For Example: User taps "Next Page" before brapi call returns data
                   if (initPage.equals(paginationManager.getPage())) {
                       updatePageInfo(paginationManager, programsResponse.getMetadata());
                       List<BrAPIProgram> programList = programsResponse.getResult().getData();
                       function.apply(mapPrograms(programList));
                   }
               }

               @Override
               public void onFailure(ApiException error, int i, Map<String, List<String>> map) {
                   failFunction.apply(error.getCode());
               }
           };
           ProgramQueryParams queryParams = new ProgramQueryParams();
           queryParams.page(paginationManager.getPage()).pageSize(paginationManager.getPageSize());
           programsApi.programsGetAsync(queryParams, callback);
       } catch (ApiException e) {
           failFunction.apply(e.getCode());
           e.printStackTrace();
       }
    }

    private List<BrapiProgram> mapPrograms(List<BrAPIProgram> programList) {
        List<BrapiProgram> brapiPrograms = new ArrayList<>();
        if (programList != null) {
            for (BrAPIProgram program : programList) {
                BrapiProgram brapiProgram = new BrapiProgram();
                brapiProgram.setProgramName(program.getProgramName());
                brapiProgram.setProgramDbId(program.getProgramDbId());
                brapiPrograms.add(brapiProgram);
            }
        }
        return brapiPrograms;
    }

    public void getTrials(String programDbId, BrapiPaginationManager paginationManager,
                          final Function<List<BrapiTrial>, Void> function,
                          final Function<Integer, Void> failFunction) {
        Integer initPage = paginationManager.getPage();
        try {
            BrapiV2ApiCallBack<BrAPITrialListResponse> callback = new BrapiV2ApiCallBack<BrAPITrialListResponse>() {
                @Override
                public void onSuccess(BrAPITrialListResponse trialsResponse, int i, Map<String, List<String>> map) {
                    // Cancel processing if the page that was processed is not the page
                    // that we are currently on. For Example: User taps "Next Page" before brapi call returns data
                    if (initPage.equals(paginationManager.getPage())) {
                        updatePageInfo(paginationManager, trialsResponse.getMetadata());
                        List<BrAPITrial> trialList = trialsResponse.getResult().getData();
                        function.apply(mapTrials(trialList));
                    }
                }

                @Override
                public void onFailure(ApiException error, int i, Map<String, List<String>> map) {
                    failFunction.apply(error.getCode());
                }
            };
            TrialQueryParams queryParams = new TrialQueryParams();
            queryParams.programDbId(programDbId).page(paginationManager.getPage()).pageSize(paginationManager.getPageSize());
            trialsApi.trialsGetAsync(queryParams, callback);
        } catch (ApiException e) {
            failFunction.apply(e.getCode());
            e.printStackTrace();
        }
    }

    /**
     * a planned cross in brapi is a wishlist in intercross
     * just male/female pairs with a min/max and wish type (cross, seeds, fruits)
     * @param function
     * @param failFunction
     */
    public void postPlannedCross(List<BrAPIPlannedCross> plan, final Function<BrAPIPlannedCrossesListResponse, Void> function,
                                    final Function<Integer, Void> failFunction) {
        try {
            BrapiV2ApiCallBack<BrAPIPlannedCrossesListResponse> callback = new BrapiV2ApiCallBack<BrAPIPlannedCrossesListResponse>() {
                @Override
                public void onSuccess(BrAPIPlannedCrossesListResponse phenotypesResponse, int i, Map<String, List<String>> map) {

                    function.apply(phenotypesResponse);
                }

                @Override
                public void onFailure(ApiException error, int statusCode, Map<String, List<String>> responseHeaders) {
                    failFunction.apply(error.getCode());
                }
            };

            crossesApi.plannedcrossesPostAsync(plan, callback);

        } catch (ApiException e) {
            failFunction.apply(e.getCode());
            e.printStackTrace();
        }
    }

    public void postCrosses(List<BrAPICross> crosses, final Function<BrAPICrossesListResponse, Void> function,
                            final Function<Integer, Void> failFunction) {
        try {
            BrapiV2ApiCallBack<BrAPICrossesListResponse> callback = new BrapiV2ApiCallBack<BrAPICrossesListResponse>() {
                @Override
                public void onSuccess(BrAPICrossesListResponse response, int i, Map<String, List<String>> map) {

                    function.apply(response);
                }

                @Override
                public void onFailure(ApiException error, int statusCode, Map<String, List<String>> responseHeaders) {
                    failFunction.apply(error.getCode());
                }
            };

            crossesApi.crossesPostAsync(crosses, callback);

        } catch (ApiException e) {
            failFunction.apply(e.getCode());
            e.printStackTrace();
        }
    }

    public void postObservationUnits(List<BrAPIObservationUnit> observations, final Function<List<BrAPIObservationUnit>, Void> function,
                                 final Function<Integer, Void> failFunction) {
        try {
            BrapiV2ApiCallBack<BrAPIObservationUnitListResponse> callback = new BrapiV2ApiCallBack<BrAPIObservationUnitListResponse>() {
                @Override
                public void onSuccess(BrAPIObservationUnitListResponse response, int i, Map<String, List<String>> map) {

                    List<BrAPIObservationUnit> units = new ArrayList<>();
                    for(BrAPIObservationUnit obs: response.getResult().getData()){
                        units.add(obs);
                    }

                    function.apply(units);
                }

                @Override
                public void onFailure(ApiException error, int statusCode, Map<String, List<String>> responseHeaders) {
                    failFunction.apply(error.getCode());
                }
            };

            observationUnitsApi.observationunitsPostAsync(observations, callback);

        } catch (ApiException e) {
            failFunction.apply(e.getCode());
            e.printStackTrace();
        }
    }

    public void postCrossingProject(String name, final Function<List<BrAPICrossingProject>, Void> function,
                                    final Function<Integer, Void> failFunction) {
        try {
            BrapiV2ApiCallBack<BrAPICrossingProjectsListResponse> callback = new BrapiV2ApiCallBack<BrAPICrossingProjectsListResponse>() {
                @Override
                public void onSuccess(BrAPICrossingProjectsListResponse response, int i, Map<String, List<String>> map) {
                    function.apply(new ArrayList<>(response.getResult().getData()));
                }

                @Override
                public void onFailure(ApiException error, int statusCode, Map<String, List<String>> responseHeaders) {
                    failFunction.apply(error.getCode());
                }
            };

            List<BrAPICrossingProject> list = new ArrayList<>();

            BrAPICrossingProject body = new BrAPICrossingProject();
            body.crossingProjectName(name);

            list.add(body);

            crossingProjectsApi.crossingprojectsPostAsync(list, callback);

        } catch (ApiException e) {
            failFunction.apply(e.getCode());
            e.printStackTrace();
        }
    }

    public void getGermplasm(BrapiPaginationManager paginationManager,
                                  final Function<List<BrAPIGermplasm>, Void> function,
                                  final Function<Integer, Void> failFunction) {

        Integer initPage = paginationManager.getPage();

        try {
            BrapiV2ApiCallBack<BrAPIGermplasmListResponse> callback = new BrapiV2ApiCallBack<BrAPIGermplasmListResponse>() {

                @Override
                public void onSuccess(BrAPIGermplasmListResponse response, int i, Map<String, List<String>> map) {
                    // Cancel processing if the page that was processed is not the page
                    // that we are currently on. For Example: User taps "Next Page" before brapi call returns data
                    if (initPage.equals(paginationManager.getPage())) {
                        updatePageInfo(paginationManager, response.getMetadata());
                        List<BrAPIGermplasm> germs = response.getResult().getData();
                        function.apply(germs);
                    }
                }

                @Override
                public void onFailure(ApiException error, int i, Map<String, List<String>> map) {
                    failFunction.apply(error.getCode());
                }
            };

            GermplasmQueryParams request = new GermplasmQueryParams();
            request.page(paginationManager.getPage()).pageSize(paginationManager.getPageSize());

            germplasmApi.germplasmGetAsync(request, callback);

        } catch (ApiException e) {
            failFunction.apply(e.getCode());
            e.printStackTrace();
        }
    }


    public void getPlannedCrosses(String crossProjectDbId, BrapiPaginationManager paginationManager,
                            final Function<List<BrAPIPlannedCross>, Void> function,
                            final Function<Integer, Void> failFunction) {

        Integer initPage = paginationManager.getPage();

        try {
            BrapiV2ApiCallBack<BrAPIPlannedCrossesListResponse> callback = new BrapiV2ApiCallBack<BrAPIPlannedCrossesListResponse>() {

                @Override
                public void onSuccess(BrAPIPlannedCrossesListResponse response, int i, Map<String, List<String>> map) {
                    // Cancel processing if the page that was processed is not the page
                    // that we are currently on. For Example: User taps "Next Page" before brapi call returns data
                    if (initPage.equals(paginationManager.getPage())) {
                        updatePageInfo(paginationManager, response.getMetadata());
                        List<BrAPIPlannedCross> crosses = response.getResult().getData();
                        function.apply(crosses);
                    }
                }

                @Override
                public void onFailure(ApiException error, int i, Map<String, List<String>> map) {
                    failFunction.apply(error.getCode());
                }
            };

            PlannedCrossQueryParams request = new PlannedCrossQueryParams();
            request.page(paginationManager.getPage()).pageSize(paginationManager.getPageSize());

            //.crossingProjectDbId(crossProjectDbId)

            crossesApi.plannedcrossesGetAsync(request, callback);

        } catch (ApiException e) {
            failFunction.apply(e.getCode());
            e.printStackTrace();
        }
    }

    public void getCrossingProjects(BrapiPaginationManager paginationManager,
                             final Function<List<BrAPICrossingProject>, Void> function,
                             final Function<Integer, Void> failFunction) {
        Integer initPage = paginationManager.getPage();
        try {
            BrapiV2ApiCallBack<BrAPICrossingProjectsListResponse> callback = new BrapiV2ApiCallBack<BrAPICrossingProjectsListResponse>() {
                @Override
                public void onSuccess(BrAPICrossingProjectsListResponse projectsListResponse, int i, Map<String, List<String>> map) {
                    // Cancel processing if the page that was processed is not the page
                    // that we are currently on. For Example: User taps "Next Page" before brapi call returns data
                    if (initPage.equals(paginationManager.getPage())) {
                        updatePageInfo(paginationManager, projectsListResponse.getMetadata());
                        List<BrAPICrossingProject> projects = projectsListResponse.getResult().getData();
                        function.apply(projects);
                    }
                }

                @Override
                public void onFailure(ApiException error, int i, Map<String, List<String>> map) {
                    failFunction.apply(error.getCode());
                }
            };

            CrossingProjectQueryParams request = new CrossingProjectQueryParams();
            request.page(paginationManager.getPage()).pageSize(paginationManager.getPageSize());
            crossingProjectsApi.crossingprojectsGetAsync(request, callback);

        } catch (ApiException e) {
            failFunction.apply(e.getCode());
            e.printStackTrace();
        }
    }

    public void searchTrials(List<String> programDbIds, BrapiPaginationManager paginationManager,
                          final Function<List<BrapiTrial>, Void> function,
                          final Function<Integer, Void> failFunction) {
        Integer initPage = paginationManager.getPage();
        try {
            BrapiV2ApiCallBack<BrAPITrialListResponse> callback = new BrapiV2ApiCallBack<BrAPITrialListResponse>() {
                @Override
                public void onSuccess(BrAPITrialListResponse trialsResponse, int i, Map<String, List<String>> map) {
                    // Cancel processing if the page that was processed is not the page
                    // that we are currently on. For Example: User taps "Next Page" before brapi call returns data
                    if (initPage.equals(paginationManager.getPage())) {
                        updatePageInfo(paginationManager, trialsResponse.getMetadata());
                        List<BrAPITrial> trialList = trialsResponse.getResult().getData();
                        function.apply(mapTrials(trialList));
                    }
                }

                @Override
                public void onFailure(ApiException error, int i, Map<String, List<String>> map) {
                    failFunction.apply(error.getCode());
                }
            };

            BrAPITrialSearchRequest request = new BrAPITrialSearchRequest();
            request.programDbIds(programDbIds).page(paginationManager.getPage()).pageSize(paginationManager.getPageSize());

            trialsApi.searchTrialsPostAsync(request, callback);
        } catch (ApiException e) {
            failFunction.apply(e.getCode());
            e.printStackTrace();
        }
    }

    public void searchObservationUnits(List<String> observationUnitIds, BrapiPaginationManager paginationManager,
                             final Function<List<BrAPIObservationUnit>, Void> function,
                             final Function<Integer, Void> failFunction) {
        Integer initPage = paginationManager.getPage();
        try {
            BrapiV2ApiCallBack<BrAPIObservationUnitListResponse> callback = new BrapiV2ApiCallBack<BrAPIObservationUnitListResponse>() {
                @Override
                public void onSuccess(BrAPIObservationUnitListResponse response, int i, Map<String, List<String>> map) {
                    // Cancel processing if the page that was processed is not the page
                    // that we are currently on. For Example: User taps "Next Page" before brapi call returns data
                    if (initPage.equals(paginationManager.getPage())) {
                        updatePageInfo(paginationManager, response.getMetadata());
                        List<BrAPIObservationUnit> list = response.getResult().getData();
                        function.apply(list);
                    }
                }

                @Override
                public void onFailure(ApiException error, int i, Map<String, List<String>> map) {
                    failFunction.apply(error.getCode());
                }
            };

            BrAPIObservationUnitSearchRequest request = new BrAPIObservationUnitSearchRequest();
            request.observationUnitDbIds(observationUnitIds).page(paginationManager.getPage()).pageSize(paginationManager.getPageSize());

            observationUnitsApi.searchObservationunitsPostAsync(request, callback);

        } catch (ApiException e) {
            failFunction.apply(e.getCode());
            e.printStackTrace();
        }
    }

    private List<BrapiStudyDetails> mapStudies(List<BrAPIStudy> studies) {
        List<BrapiStudyDetails> brapiStudies = new ArrayList<>();
        if (studies != null) {
            for (BrAPIStudy study : studies) {
                BrapiStudyDetails brapiStudy = new BrapiStudyDetails();
                brapiStudy.setStudyName(study.getStudyName());
                brapiStudy.setStudyDbId(study.getStudyDbId());
                brapiStudies.add(brapiStudy);
            }
        }
        return brapiStudies;
    }

    private List<BrapiTrial> mapTrials(List<BrAPITrial> trialList) {
        List<BrapiTrial> brapiTrials = new ArrayList<>();
        if (trialList != null) {
            for (BrAPITrial trial : trialList) {
                BrapiTrial brapiTrial = new BrapiTrial();
                String name = trial.getTrialName();
                brapiTrial.setTrialName(name);
                brapiTrial.setTrialDbId(trial.getTrialDbId());
                brapiTrials.add(brapiTrial);
            }
        }
        return brapiTrials;
    }

    public void getStudies(String programDbId, String trialDbId, BrapiPaginationManager paginationManager,
                           final Function<List<BrapiStudyDetails>, Void> function,
                           final Function<Integer, Void> failFunction) {
        Integer initPage = paginationManager.getPage();
        try {

            BrapiV2ApiCallBack<BrAPIStudyListResponse> callback = new BrapiV2ApiCallBack<BrAPIStudyListResponse>() {
                @Override
                public void onSuccess(BrAPIStudyListResponse studiesResponse, int i, Map<String, List<String>> map) {
                    // Cancel processing if the page that was processed is not the page
                    // that we are currently on. For Example: User taps "Next Page" before brapi call returns data
                    if (initPage.equals(paginationManager.getPage())) {
                        updatePageInfo(paginationManager, studiesResponse.getMetadata());
                        final List<BrapiStudyDetails> studies = new ArrayList<>();
                        final List<BrAPIStudy> studySummaryList = studiesResponse.getResult().getData();
                        for (BrAPIStudy studySummary : studySummaryList) {
                            studies.add(mapStudy(studySummary));
                        }

                        function.apply(studies);
                    }
                }

                @Override
                public void onFailure(ApiException error, int i, Map<String, List<String>> map) {
                    failFunction.apply(error.getCode());
                }
            };

            StudyQueryParams queryParams = new StudyQueryParams();
            queryParams.active("true").programDbId(programDbId).trialDbId(trialDbId).page(paginationManager.getPage()).pageSize(paginationManager.getPageSize());
            studiesApi.studiesGetAsync(queryParams, callback);

        } catch (ApiException e) {
            failFunction.apply(e.getCode());
            e.printStackTrace();
        }

    }

    private BrapiStudyDetails mapStudy(BrAPIStudy study) {
        BrapiStudyDetails studyDetails = new BrapiStudyDetails();
        studyDetails.setStudyDbId(study.getStudyDbId());
        studyDetails.setStudyName(study.getStudyName());
        studyDetails.setCommonCropName(study.getCommonCropName());
        studyDetails.setStudyDescription(study.getStudyDescription());
        studyDetails.setStudyLocation(study.getLocationName());
        return studyDetails;
    }

    private String getRowColStr(BrAPIPositionCoordinateTypeEnum type) {
        if(null != type){
            switch (type) {
                case PLANTED_INDIVIDUAL:
                case GRID_COL:
                case MEASURED_COL:
                case LATITUDE:
                    return "Column";
                case PLANTED_ROW:
                case GRID_ROW:
                case MEASURED_ROW:
                case LONGITUDE:
                    return "Row";
            }
        }
        return null;
    }

    private Observation mapToObservation(BrAPIObservation obs){
        Observation newObservation = new Observation();
        newObservation.setDbId(obs.getObservationDbId());
        newObservation.setUnitDbId(obs.getObservationUnitDbId());
        newObservation.setVariableDbId(obs.getObservationVariableDbId());
        return newObservation;
    }

    public void postPhenotypes(List<Observation> observations,
                               final Function<List<Observation>, Void> function,
                               final Function<Integer, Void> failFunction) {
        try {
            BrapiV2ApiCallBack<BrAPIObservationListResponse> callback = new BrapiV2ApiCallBack<BrAPIObservationListResponse>() {
                @Override
                public void onSuccess(BrAPIObservationListResponse phenotypesResponse, int i, Map<String, List<String>> map) {
                    List<Observation> newObservations = new ArrayList<>();
                    for(BrAPIObservation obs: phenotypesResponse.getResult().getData()){
                        newObservations.add(mapToObservation(obs));
                    }

                    function.apply(newObservations);
                }

                @Override
                public void onFailure(ApiException error, int statusCode, Map<String, List<String>> responseHeaders) {
                    failFunction.apply(error.getCode());
                }
            };

            List<BrAPIObservation> request = new ArrayList<>();

            for (Observation observation : observations) {
                BrAPIObservation newObservation = new BrAPIObservation();
                newObservation.setCollector(observation.getCollector().trim());
                newObservation.setObservationTimeStamp(TimeAdapter.convertFrom(observation.getTimestamp()));
                newObservation.setObservationUnitDbId(observation.getUnitDbId());
                newObservation.setStudyDbId(observation.getStudyId());
                newObservation.setObservationVariableDbId(observation.getVariableDbId());
                newObservation.setObservationVariableName(observation.getVariableName());
                newObservation.setValue(observation.getValue());

                request.add(newObservation);
            }

            observationsApi.observationsPostAsync(request, callback);

        } catch (ApiException e) {
            failFunction.apply(e.getCode());
            e.printStackTrace();
        }
    }

    public void putObservations(List<Observation> observations,
                                final Function<List<Observation>, Void> function,
                                final Function<Integer, Void> failFunction) {
        try {

            BrapiV2ApiCallBack<BrAPIObservationListResponse> callback = new BrapiV2ApiCallBack<BrAPIObservationListResponse>() {
                @Override
                public void onSuccess(BrAPIObservationListResponse observationsResponse, int i, Map<String, List<String>> map) {
                    List<Observation> newObservations = new ArrayList<>();
                    for(BrAPIObservation obs: observationsResponse.getResult().getData()){
                        newObservations.add(mapToObservation(obs));
                    }
                    function.apply(newObservations);
                }

                @Override
                public void onFailure(ApiException error, int statusCode, Map<String, List<String>> responseHeaders) {
                    failFunction.apply(error.getCode());
                }
            };

            Map<String, BrAPIObservation> request = new HashMap<>();

            for (Observation obs : observations) {
                BrAPIObservation o = new BrAPIObservation();
                o.setCollector(obs.getCollector().trim());
                o.setObservationDbId(obs.getDbId());
                o.setObservationTimeStamp(TimeAdapter.convertFrom(obs.getTimestamp()));
                o.setObservationUnitDbId(obs.getUnitDbId());
                o.setObservationVariableDbId(obs.getVariableDbId());
                o.setValue(obs.getValue());

                request.put(obs.getDbId(), o);
            }

            observationsApi.observationsPutAsync(request, callback);

        } catch (ApiException e) {
            failFunction.apply(e.getCode());
            e.printStackTrace();
        }
    }
}