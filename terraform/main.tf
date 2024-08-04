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
}

provider "azurerm" {
  alias           = "provider0"
  subscription_id = "13ba43d9-3859-4c70-9f8d-182debaa038b"
  client_id       = "0d26f5b0-1c83-4b0b-af8d-ac3dbf8476cf"
  client_secret   = "SRS8Q~smnO~sFO2sykkNIJSTY.r4k-maLFt_caZU"
  tenant_id       = "30bf9f37-d550-4878-9494-1041656caf27"
  features        {}
}

resource "azurerm_management_group" "mgmt1" {
    name = "mgmt1"
    display_name = "mgmt1"
    subscription_ids = ["13ba43d9-3859-4c70-9f8d-182debaa038b"]
    provider = azurerm.provider0
}
resource "azurerm_resource_group" "rg1" {
  name     = "rg1"
  location = "East US"
  provider = azurerm.provider0
}

resource "azurerm_virtual_network" "vnet1" {
  name                = "vnet1"
  address_space       = ["10.0.0.0/16"]
  location            = "East US"
  resource_group_name = azurerm_resource_group.rg1.name
  provider            = azurerm.provider0
}

resource "azurerm_subnet" "sub1" {
  name                 = "sub1"
  resource_group_name  = azurerm_resource_group.rg1.name
  virtual_network_name = azurerm_virtual_network.vnet1.name
  address_prefixes     = ["10.0.0.0/24"]
  provider             = azurerm.provider0
}

resource "azurerm_management_group_policy_assignment" "policyassignment0" {
  for_each = { for p in csvdecode(file("${path.module}/Policy.csv")): p.displayname => p }
  
  name                  = substr(replace(each.key, " ", "-"), 0, 24)
  display_name          = each.value.displayname
  policy_definition_id  = each.value.policyid
  management_group_id   = azurerm_management_group.mgmt1.id
}

resource "azurerm_role_assignment" "example0" {
  scope                = azurerm_management_group.mgmt1.id
  role_definition_name = "Owner"
  principal_id         = "200ba991-de6c-43f2-89c7-3082c59f39a7"
}

