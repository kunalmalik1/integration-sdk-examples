package com.appian.sdk.csp.box.integration;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appian.connectedsystems.templateframework.sdk.diagnostics.IntegrationDesignerDiagnostic;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateRequestPolicy;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateType;
import com.appian.sdk.csp.box.BoxPlatformConnectedSystem;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxJSONRequest;
import com.box.sdk.BoxJSONResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@TemplateId(name="CreateFolder")
@IntegrationTemplateType(IntegrationTemplateRequestPolicy.WRITE)
public class CreateFolder extends AbstractBoxIntegration {

  public static final String OPERATION_DESCRIPTION = "operationDescription";
  public static final String FOLDER_NAME = "folderName";
  public static final String PARENT_FOLDER_ID = "parentFolderID";

  @Override
  protected SimpleConfiguration getConfiguration(
    SimpleConfiguration integrationConfiguration,
    SimpleConfiguration connectedSystemConfiguration,
    PropertyPath propertyPath,
    ExecutionContext executionContext) {

    SimpleConfiguration config = integrationConfiguration.setProperties(
      textProperty(OPERATION_DESCRIPTION)
        .isReadOnly(true)
        .build(),
      textProperty(FOLDER_NAME)
        .label("Name")
        .instructionText("The desired name for the folder. Box supports folder names of 255 characters or less. Names cannot contain non-printable ASCII characters, \"/\" or \"\\\", names with trailing spaces, or the special names '.' and '..'.")
        .isRequired(true)
        .isExpressionable(true)
        .build(),
      textProperty(PARENT_FOLDER_ID)
        .label("Parent Folder ID")
        .instructionText("The ID of the parent folder. The root folder of a Box account is always represented by the ID '0'.")
        .isRequired(true)
        .isExpressionable(true)
        .build()
    );

    config.setValue(OPERATION_DESCRIPTION, "Create a new folder. A full folder object is returned if the parent folder ID is valid and if no name collisions occur.");

    return config;
  }

  @Override
  protected IntegrationResponse execute(
    SimpleConfiguration integrationConfiguration,
    SimpleConfiguration connectedSystemConfiguration,
    ExecutionContext executionContext) {

    Map<String, Object> requestDiagnostic = new LinkedHashMap<>();
    Map<String, Object> responseDiagnostic = new LinkedHashMap<>();
    BoxPlatformConnectedSystem.addRequestDiagnostics(requestDiagnostic, connectedSystemConfiguration, executionContext);

    // Get integration inputs
    String folderName = integrationConfiguration.getValue(FOLDER_NAME);
    String parentFolderId = integrationConfiguration.getValue(PARENT_FOLDER_ID);

    Long executeStart = null;

    try {

      // Get client from connected system
      BoxDeveloperEditionAPIConnection conn = BoxPlatformConnectedSystem.getConnection(
        connectedSystemConfiguration, executionContext);

      // Create the request
      URL url = BoxFolder.CREATE_FOLDER_URL.build(conn.getBaseURL());
      String method = "POST";
      BoxJSONRequest request = new BoxJSONRequest(conn, url, method);

      Map<String, Object> parent = new HashMap<>();
      parent.put("id", parentFolderId);
      Map<String, Object> folder = new HashMap<>();
      folder.put("name", folderName);
      folder.put("parent", parent);
      String body = new ObjectMapper().writeValueAsString(folder);
      request.setBody(body);

      // Log the request
      requestDiagnostic.put("URL", url.toString());
      requestDiagnostic.put("Method", method);
      requestDiagnostic.put("Body", body);

      executeStart = System.currentTimeMillis();

      // Execute the request
      BoxJSONResponse response = (BoxJSONResponse) request.send();

      Long executeEnd = System.currentTimeMillis();

      responseDiagnostic.put("Status Code", response.getResponseCode());
//      for (Map.Entry<String,String> header : response.getHeaders().entrySet()){
//        responseDiagnostic.put(header.getKey(), header.getValue());
//      }
      responseDiagnostic.put("Headers", response.getHeaders());
      responseDiagnostic.put("Body", response.getJSON());

      ObjectMapper mapper = new ObjectMapper();
      Map<String, Object> result = mapper.readValue(response.getJSON(), new TypeReference<Map<String, Object>>(){});

      IntegrationDesignerDiagnostic diagnostic = IntegrationDesignerDiagnostic.builder()
        .addExecutionTimeDiagnostic(executeEnd - executeStart)
        .addRequestDiagnostic(requestDiagnostic)
        .addResponseDiagnostic(responseDiagnostic)
        .build();

      return IntegrationResponse
        .forSuccess(result)
        .withDiagnostic(diagnostic)
        .build();

    } catch (Exception e) {

      Long executeEnd = System.currentTimeMillis();

      return createExceptionResponse(e, executionContext, executeEnd - executeStart, requestDiagnostic, responseDiagnostic);
    }
  }


}
