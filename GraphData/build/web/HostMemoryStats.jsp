<%-- 
    Document   : HostMemoryJsp
    Created on : May 2, 2014, 3:30:39 PM
    Author     : john
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
   <HEAD>
        <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.8.2/jquery.min.js"></script>
        <script src="http://code.highcharts.com/highcharts.js"></script>
        <script src="http://code.highcharts.com/stock/highstock.js"></script>

        <SCRIPT>
            var chart; // global

            /**
             * Request data from the server, add it to the graph and set a timeout to request again
             */
            function requestData() {
                $.ajax({
                    url: 'http://localhost:8084/GraphData/GetData?machine=<%=request.getParameter("machine")%>&resource=2&timestamp=0',
                    success: function(point) {
                        var read = point.swapped;
                        var data = new Array();
                        for (var a in read)
                        {
                            data.push({x: read[a].x, y: read[a].y});
                        }

                        chart.series[0].setData(data);
                        var read = point.zipSaved;
                        var data = new Array();
                        for (var a in read)
                        {
                            data.push({x: read[a].x, y: read[a].y});
                        }

                        chart.series[2].setData(data);
                         var read = point.granted;
                        var data = new Array();
                        for (var a in read)
                        {
                            data.push({x: read[a].x, y: read[a].y});
                        }

                        
                        chart.series[1].setData(data);
                        setTimeout(requestData, 5 * 60 * 1000);
                    },
                    cache: false
                });
            }
            function dateToLong(t)
            {
                // alert("jas"+t);
                var aa = t.split("-");
                var bb = aa[2].split(" ");
                var cc = bb[1].split(":");
                //   document.getElementById("message").innerHTML = aa[0] + " " + aa[1] + " " + bb[0] + " " + cc;
                var date = new Date(aa[0], aa[1], bb[0], cc[0], cc[1], cc[2]);
                return date;
            }
            $(document).ready(function() {
                chart = new Highcharts.Chart({
                    chart: {
                        renderTo: 'container',
                        defaultSeriesType: 'spline',
                          zoomType: 'xy',
                        events: {
                            load: requestData
                        }
                    },
                    title: {
                        text: 'Host Memory Stats'
                    },
                    xAxis: {
                        type: 'datetime'
                    },
                    yAxis: {
                        minPadding: 0.2,
                        maxPadding: 0.2,
                        title: {
                            text: 'Value',
                            margin: 80
                        }
                    },
                    series: [{
                            name: 'Swapped',
                            data: []
                        }, {
                            name: 'Granted',
                            data: []
                        },{
                            name: 'Zip Saved',
                            data: []
                        }
                    ]
                });
            });
        </script>
    </HEAD>
    <body>
       	<div id="container" style="width: 100%; height: 100%; margin: 0 auto" data-highcharts-chart="0">
        </DIV>    
    </body>
</html>
