<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Landing Zone Configuration</title>
    <style>
        body {
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
            background-color: #f0f8ff; /* Light navy blue background */
        }
        .container {
            width: 50%;
            padding: 20px;
            background-color: #e6e6fa; /* Light navy blue background */
            border-radius: 10px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
        }
        h1 {
            text-align: center;
            color: #000080; /* Navy blue */
        }
        table {
            width: 100%;
        }
        th, td {
            padding: 10px;
            text-align: left;
        }
        th {
            font-weight: bold;
            color: #000080; /* Navy blue */
        }
        input[type="text"], input[type="number"] {
            width: 100%;
            padding: 10px;
            box-sizing: border-box;
            border: 1px solid #ccc;
            border-radius: 5px;
        }
        input[type="submit"], input[type="button"] {
            width: 20%;
            padding: 10px;
            background-color: #000080; /* Navy blue */
            color: #ffffff;
            border: none;
            border-radius: 5px;
            cursor: pointer;
        }
        input[type="submit"]:hover, input[type="button"]:hover {
            background-color: #0000cd; /* Medium blue */
        }
        .button-container {
            display: flex;
            justify-content: space-between;
        }
    </style>
</head>
<body>
    <div class="container">
        <form action="mgmt.jsp" method="post">
            <h1>Landing Zone Configuration</h1>
            <table>
                <tr>
                    <th><label for="numManagementGroups">Number of Management Groups:</label></th>
                    <td><input type="number" id="numManagementGroups" name="numManagementGroups" required></td>
                </tr>
                <tr>
                    <th><label for="numSubscriptions">Number of Subscriptions:</label></th>
                    <td><input type="number" id="numSubscriptions" name="numSubscriptions" required></td>
                </tr>
                <tr><center>
                    <td colspan="2">
                        <div class="button-container">
                            <input type="button" value="Back" onclick="window.location.href='index.jsp'">
                            <input type="submit" value="Next">
                        </div>
                    </td></center>
                </tr>
            </table>

            <%
                String clientId = request.getParameter("clientId");
                String clientSecret = request.getParameter("clientSecret");
                String tenantId = request.getParameter("tenantId");
            %>

            <input type="hidden" name="clientId" value="<%= clientId %>">
            <input type="hidden" name="clientSecret" value="<%= clientSecret %>">
            <input type="hidden" name="tenantId" value="<%= tenantId %>">
        </form>
    </div>
</body>
</html>
