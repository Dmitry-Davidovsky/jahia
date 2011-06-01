<%--

    Jahia Enterprise Edition v6

    Copyright (C) 2002-2010 Jahia Solutions Group. All rights reserved.

    Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
    with Document Management and Portal features.

    The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
    IMPLIED.

    Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
    you and Jahia (Jahia Sustainable Enterprise License - JSEL).

    If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ page import="org.jahia.registries.ServicesRegistry" %>
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ page import="org.jahia.services.scheduler.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.quartz.*" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <link rel="stylesheet" href="tools.css" type="text/css" />
    <title>Process Administration</title>
</head>
<body>
	   <h1> Process Administration</h1>
	   <p>
	   	  This jsp allows to remove Quartz jobs from Database. If you remove finished jobs it will be a performance boost on Server startup. 
	   	  <br/>
	   	  If you flush not finished jobs you will loose this jobs (e.g. activated autoexport job)
	   </p>
	  <%
	  List<JobDetail> allJobs = ServicesRegistry.getInstance().getSchedulerService().getAllJobs();
	  
    if(request.getParameter("flushfinish") != null) {

      int jobsflushed = ServicesRegistry.getInstance().getSchedulerService().deleteAllCompletedJobs();

      allJobs = ServicesRegistry.getInstance().getSchedulerService().getAllJobs();
      %>
         <b><%=jobsflushed%> finished Jobs</b> from Job history removed<br/><br/>
      <%
  
    }
  %> 

   <h2>Currently database contains <b><%=allJobs.size()%> Jobs</b> </h2>
   
   <form action="" method="post">
   	  <h3>Available actions:</h3>
   	  <input type="submit" value="Refresh" name="refresh" /><br/>
   	  <input type="submit" value="Flush finished jobs" name="flushfinish" /> removes all successful, failed and aborted jobs<br/>
   	  <input type="submit" value="Flush waiting jobs" name="flushwait" /> removes all waiting jobs<br/>
   	  <input type="submit" value="Flush pooled jobs" name="flushpool" /> removes all pooled jobs<br/>
   	  
   	  
   </form>	
   


</body>
</html>