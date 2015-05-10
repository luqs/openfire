<%@ page import="org.jivesoftware.openfire.*" %>
<%@ page import="com.skyseas.openfireplugins.addition.AdditionPlugin" %>

<%
    AdditionPlugin plugin = (AdditionPlugin) XMPPServer.getInstance().getPluginManager().getPlugin(AdditionPlugin.PLUGIN_NAME);
%>


<html>
<head>
    <title>Upload Testing</title>
    <meta name="pageID" content="ResourceUpload"/>
</head>
<body>
<form action="<%=  plugin.getServletUrl() %>" method="post" enctype="multipart/form-data" target="_blank">
    <h2>testing :</h2>
    <ul>
        <li>
            post:
            <input style="width: 200px;padding: 5px;" value="<%= plugin.getServletUrl() %>"/>
        </li>
        <li>
            content:
            <input style="width: 200px;padding: 5px;" type="file" name="content">
        </li>

        <li>
            you must set base path to global property(<%= AdditionPlugin.RESOURCE_BASEPATH %>),
        </li>
        <li>
            <input type="submit" value="save" name="save"/>
        </li>
    </ul>
</form>
</body>
</html>
