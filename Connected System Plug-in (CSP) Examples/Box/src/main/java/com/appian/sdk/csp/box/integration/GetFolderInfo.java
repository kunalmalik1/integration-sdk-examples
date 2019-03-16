package com.appian.sdk.csp.box.integration;

import java.util.Map;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateRequestPolicy;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateType;
import com.appian.sdk.csp.box.BoxIntegrationDesignerDiagnostic;
import com.appian.sdk.csp.box.BoxPlatformConnectedSystem;
import com.appian.sdk.csp.box.BoxService;
import com.box.sdk.BoxDeveloperEditionAPIConnection;

@TemplateId(name="GetFolderInfo")
@IntegrationTemplateType(IntegrationTemplateRequestPolicy.READ)
public class GetFolderInfo extends AbstractBoxIntegration {

  public static final String FOLDER_ID = "folderID";

  @Override
  protected SimpleConfiguration getConfiguration(
    SimpleConfiguration integrationConfiguration,
    SimpleConfiguration connectedSystemConfiguration,
    PropertyPath propertyPath,
    ExecutionContext executionContext) {

    return integrationConfiguration.setProperties(
      textProperty(FOLDER_ID)
        .label("Folder ID")
        .instructionText("The root folder of a Box account is always represented by the ID '0'.")
        .isRequired(true)
        .isExpressionable(true)
        .build()
    );
  }

  @Override
  protected String getOperationDescription() {
    return "Get information about a folder.";
  }

  @Override
  protected IntegrationResponse execute(
    SimpleConfiguration integrationConfiguration,
    SimpleConfiguration connectedSystemConfiguration,
    ExecutionContext executionContext) {

    BoxIntegrationDesignerDiagnostic diagnostics = new BoxIntegrationDesignerDiagnostic(executionContext.isDiagnosticsEnabled());
    BoxPlatformConnectedSystem.addRequestDiagnostics(diagnostics.getRequestDiagnostics(), connectedSystemConfiguration, executionContext);

    // Get integration inputs
    String folderId = integrationConfiguration.getValue(FOLDER_ID);

    try {

      // Get client from connected system
      BoxDeveloperEditionAPIConnection conn = BoxPlatformConnectedSystem.getConnection(
        connectedSystemConfiguration, executionContext);

      BoxService service = new BoxService(conn, diagnostics);

      Map<String, Object> result = service.getFolderInfo(folderId);

      return createSuccessResponse(result, executionContext, diagnostics);

    } catch (Exception e) {

      return createExceptionResponse(e, executionContext, diagnostics);
    }
  }
}
