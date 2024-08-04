<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Management Groups Configuration</title>
    <style>
        body {
            display: flex;
            justify-content: center;
            align-items: center;
            /* Remove height: 100vh; to allow scrolling */
            margin: 0;
            background-color: #f0f8ff; /* Light navy blue background */
        }
        .container {
            width: 50%;
            padding: 20px;
            background-color: #e6e6fa; /* Light navy blue background */
            border-radius: 10px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
            /* Add minimum height to ensure content fits without scrolling */
            min-height: 100vh;
            display: flex;
            flex-direction: column;
        }
        h1 {
            text-align: center;
            color: #000080; /* Navy blue */
        }
        h2 {
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
        <form action="resourceGroups.jsp" method="post">
            <h1>Management Groups Configuration</h1>
            
            <%
                int numManagementGroups = Integer.parseInt(request.getParameter("numManagementGroups"));
                int numSubscriptions = Integer.parseInt(request.getParameter("numSubscriptions"));
                String clientId = request.getParameter("clientId");
                String clientSecret = request.getParameter("clientSecret");
                String tenantId = request.getParameter("tenantId");
            %>
            
            <input type="hidden" name="numManagementGroups" value="<%= numManagementGroups %>">
            <input type="hidden" name="numSubscriptions" value="<%= numSubscriptions %>">
            <input type="hidden" name="clientId" value="<%= clientId %>">
            <input type="hidden" name="clientSecret" value="<%= clientSecret %>">
            <input type="hidden" name="tenantId" value="<%= tenantId %>">

            <table>
                <%
                    for (int i = 0; i < numManagementGroups; i++) {
                %>
                    <tr>
                        <td colspan="2"><h2>Management Group <%= i + 1 %></h2></td>
                    </tr>
                    <tr>
                        <th><label for="mgName<%= i %>">Management Group Name <%= i + 1 %>:</label></th>
                        <td><input type="text" id="mgName<%= i %>" name="mgName<%= i %>" required></td>
                    </tr>
                    <tr>
                        <th><label for="mgDisplayName<%= i %>">Display Name <%= i + 1 %>:</label></th>
                        <td><input type="text" id="mgDisplayName<%= i %>" name="mgDisplayName<%= i %>" required></td>
                    </tr>
                    <tr>
                        <th><label for="mgSubscriptionIds<%= i %>">Subscription IDs (comma-separated) <%= i + 1 %>:</label></th>
                        <td><input type="text" id="mgSubscriptionIds<%= i %>" name="mgSubscriptionIds<%= i %>" ></td>
                    </tr>
                <%
                    }
                %>

                <%
                    for (int i = 0; i < numSubscriptions; i++) {
                %>
                    <tr>
                        <td colspan="2"><h2>Subscription <%= i + 1 %></h2></td>
                    </tr>
                    <tr>
                        <th><label for="subscriptionId<%= i %>">Subscription ID <%= i + 1 %>:</label></th>
                        <td><input type="text" id="subscriptionId<%= i %>" name="subscriptionId<%= i %>" required></td>
                    </tr>
                    <tr>
                        <th><label for="numResourceGroups<%= i %>">Number of Resource Groups for Subscription <%= i + 1 %>:</label></th>
                        <td><input type="number" id="numResourceGroups<%= i %>" name="numResourceGroups<%= i %>" required></td>
                    </tr>
                <%
                    }
                %>
                <tr>
                    <td colspan="2">
                        <div class="button-container">
                            <input type="button" value="Back" onclick="window.history.back()">
                            <input type="submit" value="Next">
                        </div>
                    </td>
                </tr>
            </table>
        </form>
    </div>
</body>
</html>
