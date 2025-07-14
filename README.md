# Freedger API Server

This is an Azure Functions project that provides API endpoints for Freedger applications.

OpenAPI spec: [api.yml](src/main/resources/api.yml).

## Features

- Uses Auth0 JWT Tokens for authentication
- Provides Ditto webhook for authorizing Ditto clients.
- Provides an API to create ledgers.

## Local Development

### Prerequisites

- JDK 21 or later
- Maven 3.6 or later
- [Azure Functions Core Tools](https://learn.microsoft.com/en-us/azure/azure-functions/functions-run-local)
- [Azure CLI (for deployment)](https://learn.microsoft.com/en-us/cli/azure/install-azure-cli)
- Login to `az` CLI with an account that can read the Key Vault secrets. See `KEY_VAULT_URL` in `local.settings.example.json`.

### Local settings

Copy `local.settings.example.json` to `local.settings.json` and edit it with your configuration.

### Running the Local Development Server

1. Set up environment variables
   ```bash
   cp local.settings.example.json local.settings.json
   # Edit local.settings.json with your configuration
   ```

2. Start the local development server
  From command line:
   ```bash
   mvn clean package
   mvn azure-functions:run
   ```
  From VS Code:
  - Go to "Run and Debug"
  - Run with the "Run locally" launch configuration

   <details>
   <summary>IDE reports "XXX cannot be resolved to a type" in `target/generated-sources`</summary>
   This project use Dagger for dependency injection. The generated code is stored in `target/generated-sources`.  
   IDE may incorrectly report errors in generated code even though the project can build successfully. 
   You can ignore these errors.
   </details>

3. Test the API:
   (Unix/Bash)
   ```bash
   curl -X POST http://localhost:7071/api/ditto/authorize \
     -H "Content-Type: application/json" \
     -d '{"appID": "bfaf1c4d-ee83-4215-9022-ac9c129364ea", "provider": "freedger_api", "token": "{TOKEN}"}'
   ```

  (Windows/Powershell)
   ```powershell
   Invoke-RestMethod -Uri "http://localhost:7071/api/GetDittoPermissions" `
     -Method Post `
     -Headers @{ "Content-Type" = "application/json" } `
     -Body '{"appID": "bfaf1c4d-ee83-4215-9022-ac9c129364ea", "provider": "freedger_api", "token": "{TOKEN}"}'
   ```

## Deployment to Azure

When a new commit is pushed to the `main` branch, Github Actions will automatically deploy the code to Azure.
See `.github\workflows\main_freedger-api.yml` for the workflow definition.

## Logging
This project uses Logback for logging.  
The log level can be configured in `src/main/resources/logback.xml`.  
When deployed to Azure Functions, the logs are automatically sent to Application Insights. 
See [Upgrading from Application Insights Java 2.x SDK](https://learn.microsoft.com/en-us/azure/azure-monitor/app/java-standalone-upgrade-from-2x), and [Configuration options: Azure Monitor Application Insights for Java](https://learn.microsoft.com/en-us/azure/azure-monitor/app/java-standalone-config).
