terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "3.106.1"
    }
  }
}

provider "azurerm" {
  features {}
  subscription_id = "13ba43d9-3859-4c70-9f8d-182debaa038b"
  client_id       = "0d26f5b0-1c83-4b0b-af8d-ac3dbf8476cf"
  client_secret   = "SRS8Q~smnO~sFO2sykkNIJSTY.r4k-maLFt_caZU"
  tenant_id       = "30bf9f37-d550-4878-9494-1041656caf27"
}

provider "azurerm" {
  alias           = "provider0"
  subscription_id = "13ba43d9-3859-4c70-9f8d-182debaa038b"
  client_id       = "0d26f5b0-1c83-4b0b-af8d-ac3dbf8476cf"
  client_secret   = "SRS8Q~smnO~sFO2sykkNIJSTY.r4k-maLFt_caZU"
  tenant_id       = "30bf9f37-d550-4878-9494-1041656caf27"
  features        {}
}

resource "azurerm_resource_group" "rg1" {
  name     = "rg1"
  location = "East US"
  provider = azurerm.provider0
}
  

