# Freedger Auth Server

This is an Azure Functions project that handles authentication for Freedger applications and Ditto token exchange.

## Features

- Validates Auth0 JWT Tokens
- Generates Ditto-specific JWT Tokens
- Provides RESTful API endpoints for token exchange

## Required Environment Variables

Configure the following environment variables in `local.settings.json` (for local development) or in Azure Function App settings:

| Variable Name | Description | Example |
|---------------|-------------|---------|
| AUTH0_DOMAIN | Auth0 Domain | `your-tenant.auth0.com` |
| AUTH0_AUDIENCE | Auth0 API Identifier | `https://api.freedger.app` |
| DITTO_APP_ID | Ditto Application ID | `com.example.freedger` |
| DITTO_TOKEN_SECRET | Secret key used to sign Ditto Tokens | `your-very-secret-key` |

## Local Development

### Prerequisites

- JDK 11 or later
- Maven 3.6 or later
- Azure Functions Core Tools
- Azure CLI (for deployment)

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
   curl -X POST http://localhost:7071/api/CreateDittoExchangeToken \
     -H "Content-Type: application/json" \
     -d '{"token": "your-auth0-token"}'
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
    "token": "your-auth0-jwt-token"
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
    "error": "Error message"
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

## License

Copyright © 2025 Freedger Team
