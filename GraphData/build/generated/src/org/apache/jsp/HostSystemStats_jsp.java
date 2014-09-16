package org.apache.jsp;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

public final class HostSystemStats_jsp extends org.apache.jasper.runtime.HttpJspBase
    implements org.apache.jasper.runtime.JspSourceDependent {

  private static final JspFactory _jspxFactory = JspFactory.getDefaultFactory();

  private static java.util.List<String> _jspx_dependants;

  private org.glassfish.jsp.api.ResourceInjector _jspx_resourceInjector;

  public java.util.List<String> getDependants() {
    return _jspx_dependants;
  }

  public void _jspService(HttpServletRequest request, HttpServletResponse response)
        throws java.io.IOException, ServletException {

    PageContext pageContext = null;
    HttpSession session = null;
    ServletContext application = null;
    ServletConfig config = null;
    JspWriter out = null;
    Object page = this;
    JspWriter _jspx_out = null;
    PageContext _jspx_page_context = null;

    try {
      response.setContentType("text/html;charset=UTF-8");
      pageContext = _jspxFactory.getPageContext(this, request, response,
      			null, true, 8192, true);
      _jspx_page_context = pageContext;
      application = pageContext.getServletContext();
      config = pageContext.getServletConfig();
      session = pageContext.getSession();
      out = pageContext.getOut();
      _jspx_out = out;
      _jspx_resourceInjector = (org.glassfish.jsp.api.ResourceInjector) application.getAttribute("com.sun.appserv.jsp.resource.injector");

      out.write("\n");
      out.write("\n");
      out.write("\n");
      out.write("<!DOCTYPE html>\n");
      out.write("<html>\n");
      out.write("   <HEAD>\n");
      out.write("        <script src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.8.2/jquery.min.js\"></script>\n");
      out.write("        <script src=\"http://code.highcharts.com/highcharts.js\"></script>\n");
      out.write("        <script src=\"http://code.highcharts.com/stock/highstock.js\"></script>\n");
      out.write("\n");
      out.write("        <SCRIPT>\n");
      out.write("            var chart; // global\n");
      out.write("\n");
      out.write("            /**\n");
      out.write("             * Request data from the server, add it to the graph and set a timeout to request again\n");
      out.write("             */\n");
      out.write("            function requestData() {\n");
      out.write("                $.ajax({\n");
      out.write("                    url: 'http://localhost:8084/GraphData/GetData?machine=");
      out.print(request.getParameter("machine"));
      out.write("&resource=5&timestamp=0',\n");
      out.write("                    success: function(point) {\n");
      out.write("                        var read = point.sys;\n");
      out.write("                        var data = new Array();\n");
      out.write("                        for (var a in read)\n");
      out.write("                        {\n");
      out.write("                            data.push({x: read[a].x, y: read[a].y});\n");
      out.write("                        }\n");
      out.write("\n");
      out.write("                        chart.series[0].setData(data);\n");
      out.write("                        setTimeout(requestData, 5 * 60 * 1000);\n");
      out.write("                    },\n");
      out.write("                    cache: false\n");
      out.write("                });\n");
      out.write("            }\n");
      out.write("            function dateToLong(t)\n");
      out.write("            {\n");
      out.write("                // alert(\"jas\"+t);\n");
      out.write("                var aa = t.split(\"-\");\n");
      out.write("                var bb = aa[2].split(\" \");\n");
      out.write("                var cc = bb[1].split(\":\");\n");
      out.write("                //   document.getElementById(\"message\").innerHTML = aa[0] + \" \" + aa[1] + \" \" + bb[0] + \" \" + cc;\n");
      out.write("                var date = new Date(aa[0], aa[1], bb[0], cc[0], cc[1], cc[2]);\n");
      out.write("                return date;\n");
      out.write("            }\n");
      out.write("            $(document).ready(function() {\n");
      out.write("                chart = new Highcharts.Chart({\n");
      out.write("                    chart: {\n");
      out.write("                        renderTo: 'container',\n");
      out.write("                        defaultSeriesType: 'spline',\n");
      out.write("                          zoomType: 'xy',\n");
      out.write("                        events: {\n");
      out.write("                            load: requestData\n");
      out.write("                        }\n");
      out.write("                    },\n");
      out.write("                    title: {\n");
      out.write("                        text: 'Host System Usage'\n");
      out.write("                    },\n");
      out.write("                    xAxis: {\n");
      out.write("                        type: 'datetime'\n");
      out.write("                    },\n");
      out.write("                    yAxis: {\n");
      out.write("                        minPadding: 0.2,\n");
      out.write("                        maxPadding: 0.2,\n");
      out.write("                        title: {\n");
      out.write("                            text: 'Value',\n");
      out.write("                            margin: 80\n");
      out.write("                        }\n");
      out.write("                    },\n");
      out.write("                    series: [{\n");
      out.write("                            name: 'System stats',\n");
      out.write("                            data: []\n");
      out.write("                        }, \n");
      out.write("                    ]\n");
      out.write("                });\n");
      out.write("            });\n");
      out.write("        </script>\n");
      out.write("    </HEAD>\n");
      out.write("    <body>\n");
      out.write("       \t<div id=\"container\" style=\"width: 100%; height: 100%; margin: 0 auto\" data-highcharts-chart=\"0\">\n");
      out.write("        </DIV>    \n");
      out.write("    </body>\n");
      out.write("</html>\n");
    } catch (Throwable t) {
      if (!(t instanceof SkipPageException)){
        out = _jspx_out;
        if (out != null && out.getBufferSize() != 0)
          out.clearBuffer();
        if (_jspx_page_context != null) _jspx_page_context.handlePageException(t);
        else throw new ServletException(t);
      }
    } finally {
      _jspxFactory.releasePageContext(_jspx_page_context);
    }
  }
}
