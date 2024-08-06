import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/MyClass")
public class MyClass extends HttpServlet {

    private static final String GITHUB_API_URL = "https://api.github.com/repos/Bhuvana-1500/webalz1/contents/";
    private static final String GITHUB_TOKEN = System.getenv("GITHUB_TOKEN"); // Retrieve GitHub token from environment variable
    private static final String TF_DIR = "terraform";
    private static Map<String, Integer> vnetNameToSubscriptionIndexMap = new HashMap<>();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        PrintWriter out = res.getWriter();
        res.setContentType("text/html");

        if (GITHUB_TOKEN == null || GITHUB_TOKEN.isEmpty()) {
            out.println("GitHub token is not set. Please set the GITHUB_TOKEN environment variable.");
            return;
        }

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

            // Ensure the terraform directory exists
            Path terraformDirPath = Paths.get(TF_DIR);
            if (!Files.exists(terraformDirPath)) {
                Files.createDirectory(terraformDirPath);
            }

            createTerraformMainFile(mgNames, mgDisplayNames, mgSubscriptionIds, subscriptionIds, rgNames, rgLocations, numVNets, vnetNames, vnetAddressSpaces, numSubnets, subnetNames, subnetAddressSpaces, clientId, clientSecret, tenantId, numPeeringVNets, hubVNetName, hubToSpokeVNetNames, spokeVNetNames, spokeToHubVNetNames, mgNamesp, numPolicyMgmtGroups, principleId);

            // Upload file to GitHub
            File terraformFile = new File(TF_DIR + "/main.tf");
            if (terraformFile.exists()) {
                uploadFileToGitHub(terraformFile, "main.tf");
                out.println("File uploaded successfully.");
            } else {
                out.println("Error: Terraform file does not exist.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.println("Error: " + e.getMessage());
        }
    }

    private int getIntParameter(HttpServletRequest req, String name) {
        String value = req.getParameter(name);
        return value != null ? Integer.parseInt(value) : 0;
    }

    private int getIntParameter(HttpServletRequest req, String name, int defaultValue) {
        String value = req.getParameter(name);
        return value != null ? Integer.parseInt(value) : defaultValue;
    }

    private String[] getStringArrayParameter(HttpServletRequest req, String namePrefix, int count) {
        String[] values = new String[count];
        for (int i = 0; i < count; i++) {
            values[i] = req.getParameter(namePrefix + i);
        }
        return values;
    }

    private void createTerraformMainFile(String[] mgNames, String[] mgDisplayNames, String[] mgSubscriptionIds, String[] subscriptionIds,
                                      String[][] rgNames, String[][] rgLocations, int[][] numVNets, String[][][] vnetNames,
                                      String[][][] vnetAddressSpaces, int[][][] numSubnets, String[][][][] subnetNames,
                                      String[][][][] subnetAddressSpaces, String clientId, String clientSecret, String tenantId,
                                      int numPeeringVNets, String hubVNetName, String[] hubToSpokeVNetNames, String[] spokeVNetNames,
                                      String[] spokeToHubVNetNames, String[] mgNamesp, int numPolicyMgmtGroups, String principleId) throws IOException {
    File terraformFile = new File(TF_DIR + "/main.tf");

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(terraformFile))) {
        // Write Terraform configuration to the file
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
        writer.write("  subscription_id = \"" + subscriptionIds[0] + "\"\n");
            writer.write("  client_id       = \"" + clientId + "\"\n");
            writer.write("  client_secret   = \"" + clientSecret + "\"\n");
            writer.write("  tenant_id       = \"" + tenantId + "\"\n");
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
            writer.write("  name          = \"" + mgNames[i] + "\"\n");
            writer.write("  display_name   = \"" + mgDisplayNames[i] + "\"\n");
            if (mgSubscriptionIds[i] != null) {
                writer.write("  subscription_ids = [\n");
                String[] subscriptionIdList = mgSubscriptionIds[i].split(",");
                for (int j = 0; j < subscriptionIdList.length; j++) {
                    writer.write("    \"" + subscriptionIdList[j].trim() + "\"");
                    if (j < subscriptionIdList.length - 1) {
                        writer.write(",\n");
                    }
                }
                writer.write("\n  ]\n");
            }
            writer.write("  provider = azurerm.provider0\n");
            writer.write("}\n\n");
        }

        // Generate resource groups, VNets, subnets
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
            } else {
                System.out.println("Skipping peering because hub and spoke VNet names are the same: " + hubVNetName + " and " + spokeVNetNames[p]);
            }
        }

        for (int k = 0; k < numPolicyMgmtGroups ; k++) {
                writer.write("resource \"azurerm_role_assignment\" \"example" + k + "\" {\n");
                writer.write("  scope                = azurerm_management_group." + mgNamesp[k]  + ".id\n");
                writer.write("  role_definition_name = \"Owner\"\n");
                writer.write("  principal_id         = \""+ principleId +"\"\n");
                writer.write("}\n\n");
            }
        
        for (int k = 0; k < numPolicyMgmtGroups ; k++) {
            writer.write("resource \"azurerm_management_group_policy_assignment\" \"policyassignment"+ k +"\" {\n");
            writer.write("  for_each = { for p in csvdecode(file(\"${path.module}/Policy.csv\")): p.displayname => p }\n");
            writer.write("  \n");
            writer.write("  name                  = substr(replace(each.key, \" \", \"-\"), 0, 24)\n");
            writer.write("  display_name          = each.value.displayname\n");
            writer.write("  policy_definition_id  = each.value.policyid\n");
            writer.write("  management_group_id   = azurerm_management_group."+mgNamesp[k]+".id\n");
            writer.write("}\n\n");
            }


    } catch (IOException e) {
        e.printStackTrace();
        throw new IOException("Failed to create Terraform configuration file", e);
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

    private void uploadFileToGitHub(File file, String path) throws IOException {
    // First, retrieve the current SHA of the file
    String sha = getFileShaFromGitHub(path);
    
    if (sha == null) {
        throw new IOException("Failed to retrieve the SHA of the file.");
    }

    // Prepare the content to be uploaded
    String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    String encodedContent = Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));
    
    // Create the URL for the GitHub API
    URL url = new URL(GITHUB_API_URL + path);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("PUT");
    conn.setRequestProperty("Authorization", "token " + GITHUB_TOKEN);
    conn.setRequestProperty("Content-Type", "application/json");
    conn.setDoOutput(true);

    // Prepare the request body
    String requestBody = "{"
            + "\"message\": \"Updating file via API\","
            + "\"content\": \"" + encodedContent + "\","
            + "\"sha\": \"" + sha + "\""
            + "}";

    try (OutputStream os = conn.getOutputStream()) {
        byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
    }

    // Check response code
    int responseCode = conn.getResponseCode();
    if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_CREATED) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line.trim());
            }
            throw new IOException("Failed to upload file to GitHub. Response code: " + responseCode + ". Error: " + response.toString());
        }
    }
}

private String getFileShaFromGitHub(String path) throws IOException {
    URL url = new URL(GITHUB_API_URL + path);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    conn.setRequestProperty("Authorization", "token " + GITHUB_TOKEN);
    conn.setRequestProperty("Accept", "application/vnd.github.v3+json");

    // Check response code
    int responseCode = conn.getResponseCode();
    if (responseCode != HttpURLConnection.HTTP_OK) {
        throw new IOException("Failed to retrieve file SHA. Response code: " + responseCode);
    }

    // Read the response to get the SHA
    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line.trim());
        }
        String responseBody = response.toString();
        
        // Extract the SHA from the response JSON
        String shaPrefix = "\"sha\":\"";
        int shaStart = responseBody.indexOf(shaPrefix) + shaPrefix.length();
        int shaEnd = responseBody.indexOf("\"", shaStart);
        return responseBody.substring(shaStart, shaEnd);
    }
}

}
