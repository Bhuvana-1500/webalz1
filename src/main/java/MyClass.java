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
    private static final String GITHUB_TOKEN = System.getenv("GITHUB_TOKEN"); // Retrieve GitHub token from environment variable
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
            createTerraformMainFile(mgNames, mgDisplayNames, mgSubscriptionIds, subscriptionIds, rgNames, rgLocations, numVNets, vnetNames, vnetAddressSpaces, numSubnets, subnetNames, subnetAddressSpaces, clientId, clientSecret, tenantId, numPeeringVNets, hubVNetName, hubToSpokeVNetNames, spokeVNetNames, spokeToHubVNetNames, mgNamesp, numPolicyMgmtGroups, principleId);

            // Upload file to GitHub
            File terraformFile = new File("terraform/main.tf");
            uploadFileToGitHub(terraformFile, "main.tf");
            out.println("done");
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
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("terraform/main.tf"))) {
            writer.write("provider \"azurerm\" {\n");
            writer.write("  features {}\n");
            writer.write("}\n\n");

            for (int i = 0; i < mgNames.length; i++) {
                writer.write(String.format("resource \"azurerm_management_group\" \"%s\" {\n", mgNames[i]));
                writer.write(String.format("  display_name = \"%s\"\n", mgDisplayNames[i]));
                writer.write(String.format("  subscription_ids = [\"%s\"]\n", mgSubscriptionIds[i]));
                writer.write("}\n\n");
            }

            for (int i = 0; i < subscriptionIds.length; i++) {
                writer.write(String.format("provider \"azurerm\" {\n  alias = \"sub%d\"\n  features {}\n  subscription_id = \"%s\"\n  client_id = \"%s\"\n  client_secret = \"%s\"\n  tenant_id = \"%s\"\n}\n\n",
                                           i, subscriptionIds[i], clientId, clientSecret, tenantId));
                for (int j = 0; j < rgNames[i].length; j++) {
                    writer.write(String.format("resource \"azurerm_resource_group\" \"%s\" {\n  name = \"%s\"\n  location = \"%s\"\n  provider = azurerm.sub%d\n}\n\n",
                                               rgNames[i][j], rgNames[i][j], rgLocations[i][j], i));
                    for (int k = 0; k < vnetNames[i][j].length; k++) {
                        writer.write(String.format("resource \"azurerm_virtual_network\" \"%s\" {\n  name = \"%s\"\n  address_space = [\"%s\"]\n  location = \"%s\"\n  resource_group_name = \"%s\"\n  provider = azurerm.sub%d\n}\n\n",
                                                   vnetNames[i][j][k], vnetNames[i][j][k], vnetAddressSpaces[i][j][k], rgLocations[i][j], rgNames[i][j], i));
                        for (int l = 0; l < subnetNames[i][j][k].length; l++) {
                            writer.write(String.format("resource \"azurerm_subnet\" \"%s\" {\n  name = \"%s\"\n  address_prefix = \"%s\"\n  resource_group_name = \"%s\"\n  virtual_network_name = \"%s\"\n  provider = azurerm.sub%d\n}\n\n",
                                                       subnetNames[i][j][k][l], subnetNames[i][j][k][l], subnetAddressSpaces[i][j][k][l], rgNames[i][j], vnetNames[i][j][k], i));
                        }
                    }
                }
            }

            writer.write("output \"subscription_ids\" {\n  value = [\n");
            for (String subscriptionId : subscriptionIds) {
                writer.write(String.format("    \"%s\",\n", subscriptionId));
            }
            writer.write("  ]\n}\n\n");

            writer.write("output \"resource_group_names\" {\n  value = [\n");
            for (String[] rgNameArray : rgNames) {
                for (String rgName : rgNameArray) {
                    writer.write(String.format("    \"%s\",\n", rgName));
                }
            }
            writer.write("  ]\n}\n\n");
        }
    }

    private void uploadFileToGitHub(File file, String fileName) throws IOException {
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        String base64File = Base64.getEncoder().encodeToString(fileBytes);

        URL url = new URL(GITHUB_API_URL + fileName);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Authorization", "Bearer " + GITHUB_TOKEN);
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        connection.setDoOutput(true);

        String payload = String.format("{\"message\": \"add %s\", \"content\": \"%s\"}", fileName, base64File);
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = payload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
            System.out.println("File uploaded successfully to GitHub.");
        } else {
            System.out.println("Failed to upload file to GitHub. Response code: " + responseCode);
        }
    }
}
