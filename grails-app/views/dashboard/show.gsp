<%@ page contentType="text/html;charset=UTF-8"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<title>Dashboard</title>
<g:javascript library="jquery" />
<script type="text/javascript" src="http://www.google.com/jsapi"></script>
<g:javascript>
   google.load('visualization', '1.0', {'packages':['corechart']});
   
   $(function() {
      $('.menu').fixedMenu();
      var nmcrData = ${nmcrData};
      var rmpbData = ${rmpbData};
      var lrsvData = ${lrsvData};
      var mcprData = ${mcprData};    
      var mcnerData = ${mcnerData};
      // console.log(nmcrData);
      console.log(lrsvData);
      
      function prepNMostCitedResourcesFromNER() {
         var dt = new google.visualization.DataTable();
         dt.addColumn('string','Resource');
         dt.addColumn('number','Mentions');
         for(var i = 0; i < mcnerData.length; ++i) {
            var row = mcnerData[i];
            dt.addRow([row.label, row.count]);
         }             
         var options = {'width':900, 'height':450, 'title':'Most Cited Resources by NER'};
         var el = document.getElementById('mcner_chart');
         var chart = new google.visualization.ColumnChart(el);
         chart.draw(dt, options);         
      }
      
      function prepNMostCitedResourcesFromPublishers() {
         var dt = new google.visualization.DataTable();
         dt.addColumn('string','Resource');
         dt.addColumn('number','Mentions');
         for(var i = 0; i < mcprData.length; ++i) {
            var row = mcprData[i];
            dt.addRow([row.label, row.count]);
         }             
         var options = {'width':900, 'height':450, 'title':'Most Cited Resources From Publishers'};
         var el = document.getElementById('mcpr_chart');
         var chart = new google.visualization.ColumnChart(el);
         chart.draw(dt, options);         
      }
      
      function prepNMostCitedResources() {
         console.log('prepNMostCitedResources');
        
         var dt = new google.visualization.DataTable();
         dt.addColumn('string','Resource');
         dt.addColumn('number','Mentions');
         for(var i = 0; i < nmcrData.length; ++i) {
            var row = nmcrData[i];
            dt.addRow([row.label, row.count]);
         }
               
         var options = {'width':900, 'height':450, 'title':'Most Cited Resources'};
         var el = document.getElementById('nmcr_chart');
         var chart = new google.visualization.ColumnChart(el);
         chart.draw(dt, options);         
      }
      
      function prepResourceMentionsPerBatch() {
         var dt = new google.visualization.DataTable();
         dt.addColumn('string','Batch');
         dt.addColumn('number','Total Mentions');
         for(var i = 0; i < rmpbData.length; ++i) {
            var row = rmpbData[i];
            dt.addRow([row.label, row.count]);
         }
               
         var options = {'width':400, 'height':300, 'title':'Total Resource Mentions per Batch'};
         var el = document.getElementById('rmpb_chart');
         var chart = new google.visualization.PieChart(el);
         chart.draw(dt, options);
      }
      
      function prepLatestRegistrySiteValidation() {
         var dt = new google.visualization.DataTable();
         dt.addColumn('string','Resource Site Status');
         dt.addColumn('number','Number of Sites');
         dt.addRow(['Up', lrsvData[0]]);
         dt.addRow(['Down', lrsvData[1]]);       
         var options = {'width':400, 'height':300, 'title':'Resource Site Health'};
         var el = document.getElementById('lrsv_chart');
         var chart = new google.visualization.PieChart(el);
         chart.draw(dt, options);
      }
      
      function prepDashboard() {
          prepNMostCitedResources();
          prepResourceMentionsPerBatch();
          prepLatestRegistrySiteValidation();
          prepNMostCitedResourcesFromPublishers();
          prepNMostCitedResourcesFromNER();
      }
      
      prepDashboard();
   });
</g:javascript>
</head>
<body>
	<g:render template="/common_menu" /> 
	<div id="page-body" class="content" role="main">

		<div id="nmcr_chart" style="margin-top: 5px;"></div>
		<div>
		<div id="rmpb_chart" style="margin-top: 5px; float:left;"></div>
		<div id="lrsv_chart" style="margin-top: 5px; float:right;"></div>
		</div>
		<div id="mcpr_chart" style="margin-top: 5px; clear:both"></div>
		<div id="mcner_chart" style="margin-top:5px;"></div>
		
	</div>
</body>
</html>