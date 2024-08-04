<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Subnets Configuration</title>
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
        <form action="peering.jsp" method="post">
            <h1>Subnets Configuration</h1>

            <%-- Retrieve parameters from previous page --%>
            <% 
            int numManagementGroups = Integer.parseInt(request.getParameter("numManagementGroups"));
            for (int i = 0; i < numManagementGroups; i++) { 
            %>
                <input type="hidden" name="mgName<%= i %>" value="<%= request.getParameter("mgName" + i) %>">
                <input type="hidden" name="mgDisplayName<%= i %>" value="<%= request.getParameter("mgDisplayName" + i) %>">
                <input type="hidden" name="mgSubscriptionIds<%= i %>" value="<%= request.getParameter("mgSubscriptionIds" + i) %>">
            <% } %>
            
            <% 
            int numSubscriptions = Integer.parseInt(request.getParameter("numSubscriptions"));
            int[] numResourceGroups = new int[numSubscriptions];
            for (int i = 0; i < numSubscriptions; i++) {
                numResourceGroups[i] = Integer.parseInt(request.getParameter("numResourceGroups" + i));
            }
            %>

            <input type="hidden" name="numManagementGroups" value="<%= numManagementGroups %>">
            <input type="hidden" name="numSubscriptions" value="<%= numSubscriptions %>">
            
            <%-- Pass Azure AD parameters --%>
            <input type="hidden" name="clientId" value="<%= request.getParameter("clientId") %>">
            <input type="hidden" name="clientSecret" value="<%= request.getParameter("clientSecret") %>">
            <input type="hidden" name="tenantId" value="<%= request.getParameter("tenantId") %>">
            
            <% for (int i = 0; i < numSubscriptions; i++) { %>
                <h2>Subscription <%= i + 1 %></h2>
                <label for="subscriptionId<%= i %>">Subscription ID <%= i + 1 %>:</label>
                <input type="text" id="subscriptionId<%= i %>" name="subscriptionId<%= i %>" value="<%= request.getParameter("subscriptionId" + i) %>" required><br>
            
                <label for="numResourceGroups<%= i %>">Number of Resource Groups for Subscription <%= i + 1 %>:</label>
                <input type="number" id="numResourceGroups<%= i %>" name="numResourceGroups<%= i %>" value="<%= request.getParameter("numResourceGroups" + i) %>" required><br>
            
                <%-- Loop for resource groups --%>
                <% for (int j = 0; j < numResourceGroups[i]; j++) { %>
                    <label for="rgName<%= i %>_<%= j %>">Resource Group Name:</label>
                    <input type="text" id="rgName<%= i %>_<%= j %>" name="rgName<%= i %>_<%= j %>" value="<%= request.getParameter("rgName" + i + "_" + j) %>" required><br>
            
                    <label for="rgLocation<%= i %>_<%= j %>">Location:</label>
                    <input type="text" id="rgLocation<%= i %>_<%= j %>" name="rgLocation<%= i %>_<%= j %>" value="<%= request.getParameter("rgLocation" + i + "_" + j) %>" required><br>
            
                    <label for="numVNets<%= i %>_<%= j %>">Number of VNets for Resource Group <%= j + 1 %> in Subscription <%= i + 1 %>:</label>
                    <input type="number" id="numVNets<%= i %>_<%= j %>" name="numVNets<%= i %>_<%= j %>" value="<%= request.getParameter("numVNets" + i + "_" + j) %>"  required><br>
            
                    <%-- Loop for VNets within each resource group --%>
                    <% for (int k = 0; k < Integer.parseInt(request.getParameter("numVNets" + i + "_" + j)); k++) { %>
                        <label for="vnetName<%= i %>_<%= j %>_<%= k %>">VNet Name:</label>
                        <input type="text" id="vnetName<%= i %>_<%= j %>_<%= k %>" name="vnetName<%= i %>_<%= j %>_<%= k %>" value="<%= request.getParameter("vnetName" + i + "_" + j + "_" + k) %>" required><br>
            
                        <label for="vnetAddressSpace<%= i %>_<%= j %>_<%= k %>">Address Space:</label>
                        <input type="text" id="vnetAddressSpace<%= i %>_<%= j %>_<%= k %>" name="vnetAddressSpace<%= i %>_<%= j %>_<%= k %>" value="<%= request.getParameter("vnetAddressSpace" + i + "_" + j + "_" + k) %>" required><br>
            
                        <label for="numSubnets<%= i %>_<%= j %>_<%= k %>">Number of Subnets:</label>
                        <input type="number" id="numSubnets<%= i %>_<%= j %>_<%= k %>" name="numSubnets<%= i %>_<%= j %>_<%= k %>" value="<%= request.getParameter("numSubnets" + i + "_" + j + "_" + k) %>" required><br>
                        
                        <%-- Loop for Subnets within each VNet --%>
                        <% for (int l = 0; l < Integer.parseInt(request.getParameter("numSubnets" + i + "_" + j + "_" + k)); l++) { %>
                            <label for="subnetName<%= i %>_<%= j %>_<%= k %>_<%= l %>">Subnet Name:</label>
                            <input type="text" id="subnetName<%= i %>_<%= j %>_<%= k %>_<%= l %>" name="subnetName<%= i %>_<%= j %>_<%= k %>_<%= l %>"  required><br>
                            
                            <label for="subnetAddressSpace<%= i %>_<%= j %>_<%= k %>_<%= l %>">Subnet Address Space:</label>
                            <input type="text" id="subnetAddressSpace<%= i %>_<%= j %>_<%= k %>_<%= l %>" name="subnetAddressSpace<%= i %>_<%= j %>_<%= k %>_<%= l %>"  required><br>
                        <% } %>
                        
                        <input type="hidden" name="vnetName_<%= i %>_<%= j %>_<%= k %>" value="<%= request.getParameter("vnetName" + i + "_" + j + "_" + k) %>">
                        <input type="hidden" name="vnetAddressSpace_<%= i %>_<%= j %>_<%= k %>" value="<%= request.getParameter("vnetAddressSpace" + i + "_" + j + "_" + k) %>">
                        <input type="hidden" name="numSubnets_<%= i %>_<%= j %>_<%= k %>" value="<%= request.getParameter("numSubnets" + i + "_" + j + "_" + k) %>">
                    <% } %>
                    
                    <input type="hidden" name="rgName_<%= i %>_<%= j %>" value="<%= request.getParameter("rgName" + i + "_" + j) %>">
                    <input type="hidden" name="rgLocation_<%= i %>_<%= j %>" value="<%= request.getParameter("rgLocation" + i + "_" + j) %>">
                    <input type="hidden" name="numVNets_<%= i %>_<%= j %>" value="<%= request.getParameter("numVNets" + i + "_" + j) %>">
                <% } %>

            <% } %>
            
            <h2>Peering VNets</h2>
            <label for="numPeeringVNets">Number of Spoke VNets Required:</label>
            <input type="number" id="numPeeringVNets" name="numPeeringVNets" required><br>
            
            <div class="button-container">
                <input type="button" value="Back" onclick="history.back()">
                <input type="submit" value="Next">
                
            </div>
        </form>
    </div>
</body>
</html>
