RESOURCE_GROUP="aif-prod-rg"
LOCATION="koreacentral"
REGISTRY_NAME="aifprodacr"
ENV_NAME="aif-prod-env"
CONTAINER_APP_NAME="auth-prod"
LOG_WORKSPACE_NAME="aif-prod-workspace"
APP_INSIGHTS_NAME="aif-prod-insights"

echo "Creating Resource Group..."
az group create \
  --name $RESOURCE_GROUP \
  --location $LOCATION

echo "Creating Azure Container Registry..."
az acr create \
  --resource-group $RESOURCE_GROUP \
  --name $REGISTRY_NAME \
  --sku Basic \
  --admin-enabled false

echo "Creating Log Analytics Workspace..."
az monitor log-analytics workspace create \
  --resource-group $RESOURCE_GROUP \
  --workspace-name $LOG_WORKSPACE_NAME \
  --location $LOCATION

echo "Creating Application Insights..."
az monitor app-insights component create \
  --app $APP_INSIGHTS_NAME \
  --location $LOCATION \
  --resource-group $RESOURCE_GROUP \
  --workspace $LOG_WORKSPACE_NAME

echo "Creating Container Apps Environment..."
az containerapp env create \
  --name $ENV_NAME \
  --resource-group $RESOURCE_GROUP \
  --location $LOCATION \
  --logs-workspace-id $(az monitor log-analytics workspace show \
    --resource-group $RESOURCE_GROUP \
    --workspace-name $LOG_WORKSPACE_NAME \
    --query customerId -o tsv) \
  --logs-workspace-key $(az monitor log-analytics workspace get-shared-keys \
    --resource-group $RESOURCE_GROUP \
    --workspace-name $LOG_WORKSPACE_NAME \
    --query primarySharedKey -o tsv)


echo "Creating Container App..."
az containerapp create \
  --name $CONTAINER_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --environment $ENV_NAME \
  --image mcr.microsoft.com/azuredocs/containerapps-helloworld:latest \
  --target-port 80 \
  --ingress 'external' \
  --query properties.configuration.ingress.fqdn

echo "All resources have been created successfully!"
echo "Container App URL will be displayed above."


echo "Setting up ACR integration..."
az acr update -n $REGISTRY_NAME --admin-enabled false


echo "Creating service principal for ACR access..."
ACR_LOGIN_SERVER=$(az acr show --name $REGISTRY_NAME --resource-group $RESOURCE_GROUP --query loginServer --output tsv)
SERVICE_PRINCIPAL=$(az ad sp create-for-rbac --name "$CONTAINER_APP_NAME-acr-sp" --role acrpull --scope $(az acr show --name $REGISTRY_NAME --resource-group $RESOURCE_GROUP --query id --output tsv))

echo "ACR Login Server: $ACR_LOGIN_SERVER"
echo "Service Principal created for ACR access"

echo "Creating Azure Cache for Redis..."
az redis create \
  --name aif-prod-redis-001 \
  --resource-group aif-prod-rg \
  --location koreacentral \
  --sku Basic \
  --vm-size c0
