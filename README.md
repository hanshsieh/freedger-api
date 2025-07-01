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
- Azure Functions Core Tools
- Azure CLI (for deployment)

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
   ```bash
   curl -X POST http://localhost:7071/api/GetDittoPermissions \
     -H "Content-Type: application/json" \
     -d '{"token": "your-token"}'
   ```

## API Endpoints

### Exchange Ditto Token

- **URL**: `/api/CreateDittoExchangeToken`
- **Method**: `POST`
- **Headers**: 
  - `Content-Type: application/json`
- **Request Body**:
  ```json
  {
    "token": "your-jwt-token"
  }
  ```
- **Success Response (200 OK)**:
  ```json
  {
    "token": "generated-ditto-jwt-token"
  }
  ```
- **Error Response (4xx/5xx)**:
  ```json
  {
    "message": "Error message"
  }
  ```

## Deployment to Azure

1. Log in to Azure:
   ```bash
   az login
   ```

2. Create a resource group (if not exists):
   ```bash
   az group create --name FreedgerRG --location eastus
   ```

3. Create a storage account:
   ```bash
   az storage account create --name <storage_name> --location eastus --resource-group FreedgerRG --sku Standard_LRS
   ```

4. Create Function App:
   ```bash
   az functionapp create --resource-group FreedgerRG --consumption-plan-location eastus \
     --runtime java --runtime-version 11 --functions-version 4 \
     --name <app_name> --storage-account <storage_name>
   ```

5. Configure environment variables:
   ```bash
   az functionapp config appsettings set --name <app_name> --resource-group FreedgerRG \
     --settings "AUTH0_DOMAIN=your-tenant.auth0.com"
   az functionapp config appsettings set --name <app_name> --resource-group FreedgerRG \
     --settings "AUTH0_AUDIENCE=your-audience"
   az functionapp config appsettings set --name <app_name> --resource-group FreedgerRG \
     --settings "DITTO_APP_ID=com.example.freedger"
   az functionapp config appsettings set --name <app_name> --resource-group FreedgerRG \
     --settings "DITTO_TOKEN_SECRET=your-very-secret-key"
   ```

6. Deploy the code:
   ```bash
   mvn clean package
   mvn azure-functions:deploy
   ```

## Security Considerations

1. Keep `DITTO_TOKEN_SECRET` confidential and never commit it to version control
2. Enable HTTPS in production
3. Consider implementing rate limiting
4. Rotate secrets periodically
