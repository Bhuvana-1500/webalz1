import java.io.*;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/MyClass")
public class MyClass extends HttpServlet {
    private static Map<String, Integer> vnetNameToSubscriptionIndexMap = new HashMap<>();
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        PrintWriter out = res.getWriter();
        res.setContentType("text/html");
        String clientId = req.getParameter("clientId");
        String clientSecret = req.getParameter("clientSecret");
        String tenantId = req.getParameter("tenantId");
        String principleId = req.getParameter("principleid");


        String numPolicyMgmtGroupsStr = req.getParameter("numPolicyMgmtGroups");
        int numPolicyMgmtGroups = (numPolicyMgmtGroupsStr != null && !numPolicyMgmtGroupsStr.isEmpty()) ? Integer.parseInt(numPolicyMgmtGroupsStr) : 0;
        
        // Retrieve the management group names
        String[] mgNamesp = new String[numPolicyMgmtGroups];
        for (int i = 0; i < numPolicyMgmtGroups; i++) {
            mgNamesp[i] = req.getParameter("mgName" + i);
        }

        // Management Groups
        String numManagementGroupsStr = req.getParameter("numManagementGroups");
        if (numManagementGroupsStr == null) {
            out.println("Number of management groups is missing.");
            return;
        }
        int numManagementGroups = Integer.parseInt(numManagementGroupsStr);
        String[] mgNames = new String[numManagementGroups];
        String[] mgDisplayNames = new String[numManagementGroups];
        String[] mgSubscriptionIds = new String[numManagementGroups];

        for (int i = 0; i < numManagementGroups; i++) {
            mgNames[i] = req.getParameter("mgName" + i);
            mgDisplayNames[i] = req.getParameter("mgDisplayName" + i);
            mgSubscriptionIds[i] = req.getParameter("mgSubscriptionIds" + i);

            if (mgNames[i] == null || mgDisplayNames[i] == null || mgSubscriptionIds[i] == null) {
                out.println("Management group " + i + " information is missing.");
                return;
            }
        }

        // Subscriptions and Resource Groups
        String numSubscriptionsStr = req.getParameter("numSubscriptions");
        if (numSubscriptionsStr == null) {
            out.println("Number of subscriptions is missing.");
            return;
        }
        int numSubscriptions = Integer.parseInt(numSubscriptionsStr);
        String[] subscriptionIds = new String[numSubscriptions];
        int[] numResourceGroups = new int[numSubscriptions];
        String[][] rgNames = new String[numSubscriptions][];
        String[][] rgLocations = new String[numSubscriptions][];
        int[][] numVNets = new int[numSubscriptions][];
        String[][][] vnetNames = new String[numSubscriptions][][];
        String[][][] vnetAddressSpaces = new String[numSubscriptions][][];
        int[][][] numSubnets = new int[numSubscriptions][][];
        String[][][][] subnetNames = new String[numSubscriptions][][][];
        String[][][][] subnetAddressSpaces = new String[numSubscriptions][][][];

        for (int i = 0; i < numSubscriptions; i++) {
            subscriptionIds[i] = req.getParameter("subscriptionId" + i);
            if (subscriptionIds[i] == null || subscriptionIds[i].isEmpty()) {
                out.println("Subscription ID " + i + " is missing or empty.");
                return;
            }

            numResourceGroups[i] = Integer.parseInt(req.getParameter("numResourceGroups" + i));
            rgNames[i] = new String[numResourceGroups[i]];
            rgLocations[i] = new String[numResourceGroups[i]];
            numVNets[i] = new int[numResourceGroups[i]];
            vnetNames[i] = new String[numResourceGroups[i]][];
            vnetAddressSpaces[i] = new String[numResourceGroups[i]][];
            numSubnets[i] = new int[numResourceGroups[i]][];
            subnetNames[i] = new String[numResourceGroups[i]][][];
            subnetAddressSpaces[i] = new String[numResourceGroups[i]][][];

            for (int j = 0; j < numResourceGroups[i]; j++) {
                rgNames[i][j] = req.getParameter("rgName" + i + "_" + j);
                rgLocations[i][j] = req.getParameter("rgLocation" + i + "_" + j);

                if (rgNames[i][j] == null || rgLocations[i][j] == null) {
                    out.println("Resource group " + j + " for subscription " + i + " information is missing.");
                    return;
                }

                numVNets[i][j] = Integer.parseInt(req.getParameter("numVNets" + i + "_" + j));
                vnetNames[i][j] = new String[numVNets[i][j]];
                vnetAddressSpaces[i][j] = new String[numVNets[i][j]];
                numSubnets[i][j] = new int[numVNets[i][j]];
                subnetNames[i][j] = new String[numVNets[i][j]][];
                subnetAddressSpaces[i][j] = new String[numVNets[i][j]][];

                for (int k = 0; k < numVNets[i][j]; k++) {
                    vnetNames[i][j][k] = req.getParameter("vnetName" + i + "_" + j + "_" + k);
                    vnetAddressSpaces[i][j][k] = req.getParameter("vnetAddressSpace" + i + "_" + j + "_" + k);
                    numSubnets[i][j][k] = Integer.parseInt(req.getParameter("numSubnets" + i + "_" + j + "_" + k));

                    if (vnetNames[i][j][k] == null || vnetAddressSpaces[i][j][k] == null) {
                        out.println("VNet " + k + " for resource group " + j + " in subscription " + i + " information is missing.");
                        return;
                    }

                    subnetNames[i][j][k] = new String[numSubnets[i][j][k]];
                    subnetAddressSpaces[i][j][k] = new String[numSubnets[i][j][k]];

                    for (int l = 0; l < numSubnets[i][j][k]; l++) {
                        subnetNames[i][j][k][l] = req.getParameter("subnetName" + i + "_" + j + "_" + k + "_" + l);
                        subnetAddressSpaces[i][j][k][l] = req.getParameter("subnetAddressSpace" + i + "_" + j + "_" + k + "_" + l);

                        if (subnetNames[i][j][k][l] == null || subnetAddressSpaces[i][j][k][l] == null) {
                            out.println("Subnet " + l + " for VNet " + k + " in resource group " + j + " in subscription " + i + " information is missing.");
                            return;
                        }
                    }
                }
                for (int k = 0; k < numVNets[i][j]; k++) {
                    vnetNameToSubscriptionIndexMap.put(vnetNames[i][j][k], i);
                }
            }
            
        }

        // Peering VNets
        int numPeeringVNets =0;
        String numPeeringVNetsStr = req.getParameter("numPeeringVNets");
        if (numPeeringVNetsStr != null) {
            
        numPeeringVNets = Integer.parseInt(numPeeringVNetsStr);
        }
        String hubVNetName = req.getParameter("hubVNetName");
        String[] hubToSpokeVNetNames = new String[numPeeringVNets];
        String[] spokeVNetNames = new String[numPeeringVNets];
        String[] spokeToHubVNetNames = new String[numPeeringVNets];
        

        for (int p = 0; p < numPeeringVNets; p++) {
            hubToSpokeVNetNames[p] = req.getParameter("hubToSpokeVNetName" + p);
            spokeVNetNames[p] = req.getParameter("spokeVNetName" + p);
            spokeToHubVNetNames[p] = req.getParameter("spokeToHubVNetName" + p);
        }

        // Generate Terraform files
        try {
            out.println("done");
            createTerraformMainFile(mgNames, mgDisplayNames, mgSubscriptionIds, subscriptionIds, rgNames, rgLocations, numVNets, vnetNames, vnetAddressSpaces, numSubnets, subnetNames, subnetAddressSpaces, clientId, clientSecret, tenantId, numPeeringVNets, hubVNetName, hubToSpokeVNetNames, spokeVNetNames, spokeToHubVNetNames, mgNamesp, numPolicyMgmtGroups, principleId);
        } catch (Exception e) {
            out.println("Error generating Terraform configuration: " + e.getMessage());
        }
    }


private void createTerraformMainFile(String[] mgNames, String[] mgDisplayNames, String[] mgSubscriptionIds, 
                                      String[] subscriptionIds, String[][] rgNames, String[][] rgLocations, 
                                      int[][] numVNets, String[][][] vnetNames, String[][][] vnetAddressSpaces, 
                                      int[][][] numSubnets, String[][][][] subnetNames, String[][][][] subnetAddressSpaces, 
                                      String clientId, String clientSecret, String tenantId, int numPeeringVNets, 
                                      String hubVNetName, String[] hubToSpokeVNetNames, String[] spokeVNetNames, 
                                      String[] spokeToHubVNetNames, String[] mgNamesp, int numPolicyMgmtGroups, 
                                      String principleId) throws IOException {
    File terraformDir = new File("terraform");
    if (!terraformDir.exists()) {
        terraformDir.mkdir();
    }

    try (BufferedWriter writer = new BufferedWriter(new FileWriter("terraform/main.tf"))) {
        writer.write("terraform {\n");
        writer.write("  required_providers {\n");
        writer.write("    azurerm = {\n");
        writer.write("      source  = \"hashicorp/azurerm\"\n");
        writer.write("      version = \"3.106.1\"\n");
        writer.write("    }\n");
        writer.write("  }\n");
        writer.write("}\n\n");

        writer.write("provider \"azurerm\" {\n");
        writer.write("  features {}\n");
        writer.write("}\n\n");

        for (int i = 0; i < subscriptionIds.length; i++) {
            writer.write("provider \"azurerm\" {\n");
            writer.write("  alias           = \"provider" + i + "\"\n");
            writer.write("  subscription_id = \"" + subscriptionIds[i] + "\"\n");
            writer.write("  client_id       = \"" + clientId + "\"\n");
            writer.write("  client_secret   = \"" + clientSecret + "\"\n");
            writer.write("  tenant_id       = \"" + tenantId + "\"\n");
            writer.write("  features        {}\n");
            writer.write("}\n\n");
        }

        for (int i = 0; i < mgNames.length; i++) {
            writer.write("resource \"azurerm_management_group\" \"" + mgNames[i] + "\" {\n");
            writer.write("    name = \"" + mgNames[i] + "\"\n");
            writer.write("    display_name = \"" + mgDisplayNames[i] + "\"\n");
            if (mgNames[i] != null && mgSubscriptionIds[i] != null) {
                writer.write("    subscription_ids = [");
                String[] subscriptionIdList = mgSubscriptionIds[i].split(",");
                for (int j = 0; j < subscriptionIdList.length; j++) {
                    writer.write("\"" + subscriptionIdList[j].trim() + "\"");
                    if (j < subscriptionIdList.length - 1) {
                        writer.write(", ");
                    }
                }
                writer.write("]\n");
            }
            writer.write("    provider = azurerm.provider0\n");
            writer.write("}\n");
        }

        for (int i = 0; i < subscriptionIds.length; i++) {
            for (int j = 0; j < rgNames[i].length; j++) {
                writer.write("resource \"azurerm_resource_group\" \"" + rgNames[i][j] + "\" {\n");
                writer.write("  name     = \"" + rgNames[i][j] + "\"\n");
                writer.write("  location = \"" + rgLocations[i][j] + "\"\n");
                writer.write("  provider = azurerm.provider" + i + "\n");
                writer.write("}\n\n");
            }
        }

        for (int i = 0; i < subscriptionIds.length; i++) {
            for (int j = 0; j < rgNames[i].length; j++) {
                for (int k = 0; k < numVNets[i][j]; k++) {
                    writer.write("resource \"azurerm_virtual_network\" \"" + vnetNames[i][j][k] + "\" {\n");
                    writer.write("  name                = \"" + vnetNames[i][j][k] + "\"\n");
                    writer.write("  address_space       = [\"" + vnetAddressSpaces[i][j][k] + "\"]\n");
                    writer.write("  location            = \"" + rgLocations[i][j] + "\"\n");
                    writer.write("  resource_group_name = azurerm_resource_group." + rgNames[i][j] + ".name\n");
                    writer.write("  provider            = azurerm.provider" + i + "\n");
                    writer.write("}\n\n");
                }
            }
        }

        for (int i = 0; i < subscriptionIds.length; i++) {
            for (int j = 0; j < rgNames[i].length; j++) {
                for (int k = 0; k < numVNets[i][j]; k++) {
                    for (int l = 0; l < numSubnets[i][j][k]; l++) {
                        writer.write("resource \"azurerm_subnet\" \"" + subnetNames[i][j][k][l] + "\" {\n");
                        writer.write("  name                 = \"" + subnetNames[i][j][k][l] + "\"\n");
                        writer.write("  resource_group_name  = azurerm_resource_group." + rgNames[i][j] + ".name\n");
                        writer.write("  virtual_network_name = azurerm_virtual_network." + vnetNames[i][j][k] + ".name\n");
                        writer.write("  address_prefixes     = [\"" + subnetAddressSpaces[i][j][k][l] + "\"]\n");
                        writer.write("  provider             = azurerm.provider" + i + "\n");
                        writer.write("}\n\n");
                    }
                }
            }
        }

        for (int p = 0; p < numPeeringVNets; p++) {
            if (!hubVNetName.equals(spokeVNetNames[p])) {
                writer.write("resource \"azurerm_virtual_network_peering\" \"" + hubVNetName + "_to_" + spokeVNetNames[p] + "\" {\n");
                writer.write("  provider = azurerm.provider" + getSubscriptionIndexByVNetName(subscriptionIds, hubVNetName) + "\n");
                writer.write("  name                = \"" + hubToSpokeVNetNames[p] + "\"\n");
                writer.write("  resource_group_name = azurerm_virtual_network." + hubVNetName + ".resource_group_name\n");
                writer.write("  virtual_network_name = \"" + hubVNetName + "\"\n");
                writer.write("  remote_virtual_network_id = azurerm_virtual_network." + spokeVNetNames[p] + ".id\n");
                writer.write("  allow_virtual_network_access = true\n");
                writer.write("  allow_forwarded_traffic = true\n");
                writer.write("  allow_gateway_transit = false\n");
                writer.write("  use_remote_gateways = false\n");
                writer.write("}\n\n");
            }
        }

        for (int p = 0; p < hubToSpokeVNetNames.length; p++) {
            if (!spokeVNetNames[p].equals(hubVNetName)) {
                writer.write("resource \"azurerm_virtual_network_peering\" \"" + spokeVNetNames[p] + "_to_" + hubVNetName + "\" {\n");
                writer.write("  provider = azurerm.provider" + getSubscriptionIndexByVNetName(subscriptionIds, spokeVNetNames[p]) + "\n");
                writer.write("  name                = \"" + spokeToHubVNetNames[p] + "\"\n");
                writer.write("  resource_group_name = azurerm_virtual_network." + spokeVNetNames[p] + ".resource_group_name\n");
                writer.write("  virtual_network_name = \"" + spokeVNetNames[p] + "\"\n");
                writer.write("  remote_virtual_network_id = azurerm_virtual_network." + hubVNetName + ".id\n");
                writer.write("  allow_virtual_network_access = true\n");
                writer.write("  allow_forwarded_traffic = true\n");
                writer.write("  allow_gateway_transit = false\n");
                writer.write("  use_remote_gateways = false\n");
                writer.write("}\n\n");
            }
        }

        // Policy assignments (complete this section as needed)
        for (int i = 0; i < numPolicyMgmtGroups; i++) {
            writer.write("resource \"azurerm_policy_assignment\" \"" + mgNamesp[i] + "\" {\n");
            writer.write("  name                 = \"" + mgNamesp[i] + "\"\n");
            writer.write("  scope                = azurerm_management_group." + mgNamesp[i] + ".id\n");
            writer.write("  policy_definition_id = azurerm_policy_definition.example.id\n"); // Replace with actual policy ID
            writer.write("  provider             = azurerm.provider0\n");
            writer.write("}\n");
        }

        for (int k = 0; k < numPolicyMgmtGroups ; k++) {
                writer.write("resource \"azurerm_role_assignment\" \"example" + k + "\" {\n");
                writer.write("  scope                = azurerm_management_group." + mgNamesp[k]  + ".id\n");
                writer.write("  role_definition_name = \"Owner\"\n");
                writer.write("  principal_id         = \""+ principleId +"\"\n");
                writer.write("}\n\n");
            }

        writer.flush();
    }
}

    private static int getSubscriptionIndexByVNetName(String[] subscriptionIds, String vnetName) {
        // Find the subscription index associated with the VNet name
        Integer index = vnetNameToSubscriptionIndexMap.get(vnetName);
        if (index == null) {
            throw new IllegalArgumentException("VNet name " + vnetName + " is not associated with any subscription.");
        }
        return index;
    }
    
    private static void executeTerraformCommand(String command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
        processBuilder.directory(new File("terraform"));
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        process.waitFor();
    }
}
