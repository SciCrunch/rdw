<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <title>Link Statistics Visualization</title>
    <g:javascript library="jquery"/>
    <script type="text/javascript" src="http://www.google.com/jsapi"></script>
    <g:javascript>
   google.load('visualization', '1.0', {'packages':['corechart']});
   
   $(function() {
      $('.menu').fixedMenu();
      var nmcrData = ${nmcrData};
      var lbyData = ${lbyData};
      var lssData = ${lssData};
      
      function prepLinkStatusStatsByYearChart() {
         var dt = new google.visualization.DataTable(), i, row;
         dt.addColumn('string','Year');
         dt.addColumn('number','Alive Links');
         dt.addColumn('number','Dead Links');
          
         for(i = 0; i < lssData.length; ++i) {
            row = lssData[i];
            dt.addRow([row.year.toString(), row.alive, row.dead]);
         }
         var options = {'width':900, 'height':450, 
           'title':"Overall Alive vs. Dead Links by Year", 
         };
         var el = document.getElementById('lss_chart');
         var chart = new google.visualization.ColumnChart(el);
         chart.draw(dt, options); 
      }
      
      function prepDeadLinkPercByYearChart() {
         var dt = new google.visualization.DataTable(), i, row, perc;
         dt.addColumn('number','Year');
         dt.addColumn('number','% Dead');
          
         for(i = 0; i < lssData.length; ++i) {
            row = lssData[i];
            perc = 100.0 * (row.dead / (row.alive + row.dead));
            dt.addRow([row.year, perc]);
         }
         var options = {'width':900, 'height':300, 
           'title':"Dead Link Percentage by Year",
           vAxis:{title:'Percentage'} 
         };
         var el = document.getElementById('dlp_chart');
         var chart = new google.visualization.LineChart(el);
         chart.draw(dt, options); 
      }
      
      function prepLinksByYearForResource(resourceName) {
         var dt = new google.visualization.DataTable();
         dt.addColumn('string','Year');
         dt.addColumn('number','Mentions');
         for(var i = 0; i < lbyData.length; ++i) {
            var row = lbyData[i];
            dt.addRow([row.label, row.count]);
         } 
         var options = {'width':900, 'height':450, 
           'title':"Mentions per Year for '" + resourceName + "'"};
        
         var el = document.getElementById('lby_chart');
         var chart = new google.visualization.ColumnChart(el);
         chart.draw(dt, options);    
      }
      
      var selResourceName = $('#rSel option:selected').text();
      
      prepLinkStatusStatsByYearChart();
      prepLinksByYearForResource(selResourceName);
      prepDeadLinkPercByYearChart();
    });
    </g:javascript>
</head>

<body>
<g:render template="/common_menu"/>
<div id="page-body" class="content" role="main">

    <div style="margin-top:10px; margin-left:10px;">
        <g:form name="vf" action="show">
            <g:select id="rSel" name="selRegId" from="${nmcrList}" value="${selRegId}" optionKey="id"
                      optionValue="label"/>
            <g:submitButton name="submitBut" value="Show"/>
        </g:form>
    </div>

    <div id="lby_chart" style="margin-top: 5px;"></div>

    <div id="lss_chart" style="margin-top: 5px;"></div>

    <div id="dlp_chart" style="margin-top: 5px;"></div>
</div>
</body>
</html>