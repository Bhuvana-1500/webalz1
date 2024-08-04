import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/MyClass")
public class MyClass extends HttpServlet {

    private static final String GITHUB_API_URL = "https://api.github.com/repos/Bhuvana-1500/webalz1/contents/";
    private static final String GITHUB_TOKEN = "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"; // Use environment variable for GitHub token
    private static Map<String, Integer> vnetNameToSubscriptionIndexMap = new HashMap<>();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        PrintWriter out = res.getWriter();
        res.setContentType("text/html");

        // Retrieve parameters from the request
        String clientId = req.getParameter("clientId");
        String clientSecret = req.getParameter("clientSecret");
        String tenantId = req.getParameter("tenantId");
        String principleId = req.getParameter("principleid");

        // Retrieve management group names
        int numPolicyMgmtGroups = getIntParameter(req, "numPolicyMgmtGroups", 0);
        String[] mgNamesp = getStringArrayParameter(req, "mgName", numPolicyMgmtGroups);

        // Retrieve management groups
        int numManagementGroups = getIntParameter(req, "numManagementGroups");
        String[] mgNames = getStringArrayParameter(req, "mgName", numManagementGroups);
        String[] mgDisplayNames = getStringArrayParameter(req, "mgDisplayName", numManagementGroups);
        String[] mgSubscriptionIds = getStringArrayParameter(req, "mgSubscriptionIds", numManagementGroups);

        // Retrieve subscriptions and resource groups
        int numSubscriptions = getIntParameter(req, "numSubscriptions");
        String[] subscriptionIds = getStringArrayParameter(req, "subscriptionId", numSubscriptions);
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
            numResourceGroups[i] = getIntParameter(req, "numResourceGroups" + i);
            rgNames[i] = getStringArrayParameter(req, "rgName" + i + "_", numResourceGroups[i]);
            rgLocations[i] = getStringArrayParameter(req, "rgLocation" + i + "_", numResourceGroups[i]);
            numVNets[i] = new int[numResourceGroups[i]];
            vnetNames[i] = new String[numResourceGroups[i]][];
            vnetAddressSpaces[i] = new String[numResourceGroups[i]][];
            numSubnets[i] = new int[numResourceGroups[i]][];
            subnetNames[i] = new String[numResourceGroups[i]][][];
            subnetAddressSpaces[i] = new String[numResourceGroups[i]][][];

            for (int j = 0; j < numResourceGroups[i]; j++) {
                numVNets[i][j] = getIntParameter(req, "numVNets" + i + "_" + j);
                vnetNames[i][j] = getStringArrayParameter(req, "vnetName" + i + "_" + j + "_", numVNets[i][j]);
                vnetAddressSpaces[i][j] = getStringArrayParameter(req, "vnetAddressSpace" + i + "_" + j + "_", numVNets[i][j]);
                numSubnets[i][j] = new int[numVNets[i][j]];
                subnetNames[i][j] = new String[numVNets[i][j]][];
                subnetAddressSpaces[i][j] = new String[numVNets[i][j]][];

                for (int k = 0; k < numVNets[i][j]; k++) {
                    numSubnets[i][j][k] = getIntParameter(req, "numSubnets" + i + "_" + j + "_" + k);
                    subnetNames[i][j][k] = getStringArrayParameter(req, "subnetName" + i + "_" + j + "_" + k + "_", numSubnets[i][j][k]);
                    subnetAddressSpaces[i][j][k] = getStringArrayParameter(req, "subnetAddressSpace" + i + "_" + j + "_" + k + "_", numSubnets[i][j][k]);

                    for (int l = 0; l < numSubnets[i][j][k]; l++) {
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
        int numPeeringVNets = getIntParameter(req, "numPeeringVNets", 0);
        String hubVNetName = req.getParameter("hubVNetName");
        String[] hubToSpokeVNetNames = getStringArrayParameter(req, "hubToSpokeVNetName", numPeeringVNets);
        String[] spokeVNetNames = getStringArrayParameter(req, "spokeVNetName", numPeeringVNets);
        String[] spokeToHubVNetNames = getStringArrayParameter(req, "spokeToHubVNetName", numPeeringVNets);

        // Generate Terraform files
        try {
            out.println("done");
            createTerraformMainFile(mgNames, mgDisplayNames, mgSubscriptionIds, subscriptionIds, rgNames, rgLocations, numVNets, vnetNames, vnetAddressSpaces, numSubnets, subnetNames, subnetAddressSpaces, clientId, clientSecret, tenantId, numPeeringVNets, hubVNetName, hubToSpokeVNetNames, spokeVNetNames, spokeToHubVNetNames, mgNamesp, numPolicyMgmtGroups, principleId);

            // Upload file to GitHub
            File terraformFile = new File("terraform/main.tf");
            uploadFileToGitHub("terraform/main.tf", terraformFile);
        } catch (Exception e) {
            out.println("Error generating Terraform configuration: " + e.getMessage());
        }
    }

    private int getIntParameter(HttpServletRequest req, String paramName) {
        return getIntParameter(req, paramName, -1);
    }

    private int getIntParameter(HttpServletRequest req, String paramName, int defaultValue) {
        String param = req.getParameter(paramName);
        return (param != null && !param.isEmpty()) ? Integer.parseInt(param) : defaultValue;
    }

    private String[] getStringArrayParameter(HttpServletRequest req, String baseParamName, int length) {
        String[] values = new String[length];
        for (int i = 0; i < length; i++) {
            values[i] = req.getParameter(baseParamName + i);
        }
        return values;
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

private void uploadFileToGitHub(String filePath, File file) throws IOException {
    String repoPath = "terraform/main.tf";
    String apiUrl = GITHUB_API_URL + repoPath;

    // Read the file content
    byte[] fileContent = java.nio.file.Files.readAllBytes(file.toPath());
    String base64Content = Base64.getEncoder().encodeToString(fileContent);

    // Prepare the request
    URL url = new URL(apiUrl);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("PUT");
    conn.setRequestProperty("Authorization", "token " + GITHUB_TOKEN);
    conn.setRequestProperty("Content-Type", "application/json");
    conn.setDoOutput(true);

    String requestBody = String.format("{\"message\": \"Add Terraform main.tf file\", \"content\": \"%s\"}",
            base64Content);
    try (OutputStream os = conn.getOutputStream()) {
        os.write(requestBody.getBytes(StandardCharsets.UTF_8));
    }

    // Handle the response
    int responseCode = conn.getResponseCode();
    if (responseCode == HttpURLConnection.HTTP_OK) {
        System.out.println("File uploaded successfully.");
    } else {
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        System.out.println("Failed to upload file. Response code: " + responseCode);
        System.out.println("Response: " + response.toString());
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
