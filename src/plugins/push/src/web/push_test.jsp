<%@ page import="org.jivesoftware.openfire.*" %>
<%@ page import="com.skyseas.openfireplugins.webservice.HttpPushPlugin" %>
<%@ page import="com.skyseas.openfireplugins.webservice.PushServlet" %>

<%
    HttpPushPlugin plugin = (HttpPushPlugin) XMPPServer.getInstance().getPluginManager().getPlugin(HttpPushPlugin.PLUGIN_NAME);
    if (plugin != null) {
        String pushUrl = plugin.getServletUrl();
%>


<html>
<head>
    <title>Http Push Testing</title>
    <meta name="pageID" content="push"/>
</head>
<body>
<form action="<%= pushUrl %>" method="post" target="_blank">
    <h2>testing :</h2>
    <ul>
        <li>
            post:
            <br/>
            <input value="<%= pushUrl %>" />
        </li>
        <li>
            packet_content:
            <br/>
            <textarea name="packet_content" style="width:500px;height: 200px;">
&lt;message from=&#39;skysea.com&#39;&gt;
&lt;body&gt;hi! all.&lt;/body&gt;
&lt;/message&gt;
            </textarea>
        </li>

        <li>
            you current ip: <%= request.getRemoteAddr()%>. <br />
            you must add your ip to global property(<%= PushServlet.ALLOW_IP_LIST_KEY %>),
            eg. 192.168.1.3;192.168.1.2
        </li>
        <li>
            <input type="submit" value="save" name="save"/>
        </li>
    </ul>
</form>
</body>
</html>

<%
    }
%>