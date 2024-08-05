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

@WebServlet("/MyClass")
public class MyClass extends HttpServlet {

    private static final String GITHUB_API_URL = "https://api.github.com/repos/Bhuvana-1500/webalz1/contents/";
    private static final String GITHUB_TOKEN = System.getenv("GITHUB_TOKEN"); // Retrieve GitHub token from environment variable
    private static final String TF_DIR = "terraform";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        PrintWriter out = res.getWriter();
        res.setContentType("text/html");

        if (GITHUB_TOKEN == null || GITHUB_TOKEN.isEmpty()) {
            out.println("GitHub token is not set. Please set the GITHUB_TOKEN environment variable.");
            return;
        }

        // Retrieve parameters from the request
        // ... (existing parameter retrieval code)

        // Generate Terraform files
        try {
            // Ensure the terraform directory exists
            Path terraformDirPath = Paths.get(TF_DIR);
            if (!Files.exists(terraformDirPath)) {
                Files.createDirectory(terraformDirPath);
            }

            // Create Terraform main file
            Path terraformFilePath = terraformDirPath.resolve("main.tf");
            createTerraformMainFile(terraformFilePath, /* other parameters */);

            // Upload file to GitHub
            if (Files.exists(terraformFilePath)) {
                uploadFileToGitHub(terraformFilePath.toFile(), "main.tf");
                out.println("File uploaded successfully.");
            } else {
                out.println("Error: Terraform file does not exist.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.println("Error: " + e.getMessage());
        }
    }

    private void createTerraformMainFile(Path terraformFilePath, /* other parameters */) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(terraformFilePath, StandardCharsets.UTF_8)) {
            // Write Terraform configuration to the file
            writer.write("# Terraform configuration\n");
            writer.write("provider \"azurerm\" {\n");
            writer.write("  features {}\n");
            writer.write("}\n");
            // Write more configurations here
        }
    }

    private void uploadFileToGitHub(File file, String fileName) throws IOException {
        String urlString = GITHUB_API_URL + fileName;
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Authorization", "token " + GITHUB_TOKEN);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        String encodedContent = Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));

        String jsonPayload = "{\"message\": \"Add " + fileName + "\", \"content\": \"" + encodedContent + "\"}";

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
            // File uploaded successfully
        } else {
            throw new IOException("Failed to upload file to GitHub. Response code: " + responseCode);
        }
    }
}
