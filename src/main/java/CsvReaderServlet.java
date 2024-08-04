import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CsvReaderServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String csvFilePath = getServletContext().getRealPath("/WEB-INF/Policy.csv");
        List<String[]> csvData = readCSV(csvFilePath);

        request.setAttribute("csvData", csvData);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/displayCSV.jsp");
        dispatcher.forward(request, response);
    }

    private List<String[]> readCSV(String filePath) throws IOException {
        List<String[]> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                data.add(values);
            }
        }
        return data;
    }
}
