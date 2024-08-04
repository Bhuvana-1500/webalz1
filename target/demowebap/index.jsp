<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Index Page</title>
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
        input[type="text"] {
            width: 100%;
            padding: 10px;
            box-sizing: border-box;
            border: 1px solid #ccc;
            border-radius: 5px;
        }
        input[type="submit"] {
            width: 20%;
            padding: 10px;
            background-color: #000080; /* Navy blue */
            color: #ffffff;
            border: none;
            border-radius: 5px;
            cursor: pointer;
        }
        input[type="submit"]:hover {
            background-color: #0000cd; /* Medium blue */
        }
    </style>
</head>
<body>
    <div class="container">
        <form action="no.of.mgmt.jsp" method="post">
            <h1>Service Principle Configuration</h1>
            <table>
                <tr>
                    <th><label for="clientId">Client ID:</label></th>
                    <td><input type="text" id="clientId" name="clientId" ></td>
                </tr>
                <tr>
                    <th><label for="clientSecret">Client Secret:</label></th>
                    <td><input type="text" id="clientSecret" name="clientSecret" ></td>
                </tr>
                <tr>
                    <th><label for="tenantId">Tenant ID:</label></th>
                    <td><input type="text" id="tenantId" name="tenantId" ></td>
                </tr>
                <tr>
                    <td colspan="2"><center><input type="submit" value="Next"></center></td>
                </tr>
            </table>
        </form>
    </div>
</body>
</html>
