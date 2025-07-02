# Freedger Auth Server

This is an Azure Functions project that handles authentication for Freedger applications.

## Features

- Validates Auth0 JWT Tokens
- Generates Ditto Exchange Token for exchanging Auth0 Tokens to Ditto Tokens
- Provides RESTful API endpoints for token exchange

## Local Development

### Prerequisites

- JDK 23 or later
- Maven 3.6 or later
- [Azure Functions Core Tools](https://learn.microsoft.com/en-us/azure/azure-functions/functions-run-local)
- [Azure CLI (for deployment)](https://learn.microsoft.com/en-us/cli/azure/install-azure-cli)

### Local settings

Copy `local.settings.example.json` to `local.settings.json` and edit it with your configuration.

### Running the Local Development Server

1. Set up environment variables:
   ```bash
   cp local.settings.example.json local.settings.json
   # Edit local.settings.json with your configuration
   ```

2. Start the local development server:
   ```bash
   mvn clean package
   mvn azure-functions:run
   ```

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
## API Endpoints

### Get Ditto Permissions

- **URL**: `/api/ditto/authorize`
- **Method**: `POST`
- **Headers**: 
  - `Content-Type: application/json`
- **Request Body**:
  ```json
  {
    "appID": "{APP_ID}",
    "provider": "{PROVIDER}",
    "token": "{JWT_TOKEN}"
  }
  ```
- **Success Response (200 OK)**:
  ```json
  {
    "authenticated": true,
    "userID": "auth0|685e35fe029584349202c39d",
    "expirationSeconds": 86400,
    "permissions": {
      "read": {
        "everything": false,
        "queriesByCollection": {
          "Accounts": [
            "_id.ledgerId = 'f343ccac1ffe40f48dd0bca175076a62'"
          ],
          "Ledgers": [
            "_id = 'f343ccac1ffe40f48dd0bca175076a62'"
          ]
        }
      },
      "write": {
        "everything": false,
        "queriesByCollection": {
          "Accounts": [
            "_id.ledgerId = 'f343ccac1ffe40f48dd0bca175076a62'"
          ]
        }
      }
    }
  }
  ```
- **Error Response (4xx/5xx)**:
  ```json
  {
    "authenticated": false
  }
  ```

## Deployment to Azure

1. Log in to Azure:
   ```bash
   az login
   ```

1. Create a resource group (if not exists):
   ```bash
   az group create --name freedger-api --location eastasia
   ```

1. Create Storage Account
   ```bash
   az storage account create --name <storage_name> --location eastasia --resource-group freedger-api --sku Standard_LRS
   ```

1. Create Function App:
   ```bash
   az functionapp create --resource-group freedger-api --consumption-plan-location eastasia \
     --runtime java --runtime-version 23 --functions-version 4 \
     --name <app_name> --storage-account <storage_name>
   ```

1. Configure environment variables:
   ```bash
   az functionapp config appsettings set --name <app_name> --resource-group freedger-api \
     --settings "AUTH0_DOMAIN=your-tenant.auth0.com"
   az functionapp config appsettings set --name <app_name> --resource-group freedger-api \
     --settings "AUTH0_AUDIENCE=your-audience"
   az functionapp config appsettings set --name <app_name> --resource-group freedger-api \
     --settings "DITTO_APP_ID=com.example.freedger"
   az functionapp config appsettings set --name <app_name> --resource-group freedger-api \
     --settings "DITTO_TOKEN_SECRET=your-very-secret-key"
   ```

1. Deploy the code:
   ```bash
   mvn clean package
   mvn azure-functions:deploy
   ```

## Security Considerations

1. Keep `DITTO_TOKEN_SECRET` confidential and never commit it to version control
2. Enable HTTPS in production
3. Consider implementing rate limiting
4. Rotate secrets periodically
