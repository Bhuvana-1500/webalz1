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

provider "azurerm" {
  alias           = "provider1"
  subscription_id = "b1af8833-cc76-47d5-ac29-4f7d63cdb243"
  client_id       = "0d26f5b0-1c83-4b0b-af8d-ac3dbf8476cf"
  client_secret   = "SRS8Q~smnO~sFO2sykkNIJSTY.r4k-maLFt_caZU"
  tenant_id       = "30bf9f37-d550-4878-9494-1041656caf27"
  features        {}
}

resource "azurerm_management_group" "mgmt3" {
  name          = "mgmt3"
  display_name   = "mgmt3"
  subscription_ids = [
    "13ba43d9-3859-4c70-9f8d-182debaa038b",
    "b1af8833-cc76-47d5-ac29-4f7d63cdb243"
  ]
  provider = azurerm.provider0
}

resource "azurerm_resource_group" "rg1" {
  name     = "rg1"
  location = "East US"
  provider = azurerm.provider0
}

resource "azurerm_resource_group" "rg2" {
  name     = "rg2"
  location = "East US"
  provider = azurerm.provider1
}

resource "azurerm_virtual_network" "vnet1" {
  name                = "vnet1"
  address_space       = ["10.0.0.0/16"]
  location            = "East US"
  resource_group_name = azurerm_resource_group.rg1.name
  provider            = azurerm.provider0
}

resource "azurerm_virtual_network" "vnet2" {
  name                = "vnet2"
  address_space       = ["10.1.0.0/16"]
  location            = "East US"
  resource_group_name = azurerm_resource_group.rg1.name
  provider            = azurerm.provider0
}

resource "azurerm_virtual_network" "vnet3" {
  name                = "vnet3"
  address_space       = ["10.2.0.0/16"]
  location            = "East US"
  resource_group_name = azurerm_resource_group.rg2.name
  provider            = azurerm.provider1
}

resource "azurerm_subnet" "sub1" {
  name                 = "sub1"
  resource_group_name  = azurerm_resource_group.rg1.name
  virtual_network_name = azurerm_virtual_network.vnet1.name
  address_prefixes     = ["10.0.0.0/24"]
  provider             = azurerm.provider0
}

resource "azurerm_subnet" "sub2" {
  name                 = "sub2"
  resource_group_name  = azurerm_resource_group.rg1.name
  virtual_network_name = azurerm_virtual_network.vnet2.name
  address_prefixes     = ["10.1.0.0/24"]
  provider             = azurerm.provider0
}

resource "azurerm_subnet" "sub3" {
  name                 = "sub3"
  resource_group_name  = azurerm_resource_group.rg1.name
  virtual_network_name = azurerm_virtual_network.vnet2.name
  address_prefixes     = ["10.1.1.0/24"]
  provider             = azurerm.provider0
}

resource "azurerm_subnet" "sub4" {
  name                 = "sub4"
  resource_group_name  = azurerm_resource_group.rg2.name
  virtual_network_name = azurerm_virtual_network.vnet3.name
  address_prefixes     = ["10.2.0.0/24"]
  provider             = azurerm.provider1
}

resource "azurerm_virtual_network_peering" "vnet1_to_vnet2" {
  provider = azurerm.provider0
  name                = "hub-to-spoke1"
  resource_group_name = azurerm_virtual_network.vnet1.resource_group_name
  virtual_network_name = "vnet1"
  remote_virtual_network_id = azurerm_virtual_network.vnet2.id
  allow_virtual_network_access = true
  allow_forwarded_traffic = true
  allow_gateway_transit = false
  use_remote_gateways = false
}

resource "azurerm_virtual_network_peering" "vnet2_to_vnet1" {
  provider = azurerm.provider0
  name                = "spoke-to-hub1"
  resource_group_name = azurerm_virtual_network.vnet2.resource_group_name
  virtual_network_name = "vnet2"
  remote_virtual_network_id = azurerm_virtual_network.vnet1.id
  allow_virtual_network_access = true
  allow_forwarded_traffic = true
  allow_gateway_transit = false
  use_remote_gateways = false
}

resource "azurerm_virtual_network_peering" "vnet1_to_vnet3" {
  provider = azurerm.provider0
  name                = "hub-to-spoke2"
  resource_group_name = azurerm_virtual_network.vnet1.resource_group_name
  virtual_network_name = "vnet1"
  remote_virtual_network_id = azurerm_virtual_network.vnet3.id
  allow_virtual_network_access = true
  allow_forwarded_traffic = true
  allow_gateway_transit = false
  use_remote_gateways = false
}

resource "azurerm_virtual_network_peering" "vnet3_to_vnet1" {
  provider = azurerm.provider1
  name                = "spoke-to-hub2"
  resource_group_name = azurerm_virtual_network.vnet3.resource_group_name
  virtual_network_name = "vnet3"
  remote_virtual_network_id = azurerm_virtual_network.vnet1.id
  allow_virtual_network_access = true
  allow_forwarded_traffic = true
  allow_gateway_transit = false
  use_remote_gateways = false
}

resource "azurerm_role_assignment" "example0" {
  scope                = azurerm_management_group.mgmt3.id
  role_definition_name = "Owner"
  principal_id         = "200ba991-de6c-43f2-89c7-3082c59f39a7"
}

resource "azurerm_management_group_policy_assignment" "policyassignment0" {
  for_each = { for p in csvdecode(file("${path.module}/Policy.csv")): p.displayname => p }
  
  name                  = substr(replace(each.key, " ", "-"), 0, 24)
  display_name          = each.value.displayname
  policy_definition_id  = each.value.policyid
  management_group_id   = azurerm_management_group.mgmt3.id
}

