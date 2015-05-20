<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <title>Resource Usage Co-occurrence Visualization</title>
    <g:javascript library="jquery"/>
    <r:require module="jquery-ui"/>
    <script src="http://d3js.org/d3.v2.min.js?2.8.1"></script>
    <style>
    .background {
        fill: #eee;
    }

    line {
        stroke: #fff;
    }

    text.active {
        fill: red;
    }

    </style>
</head>

<body>
<g:render template="/common_menu"/>
<div id="page-body" class="content" role="main">
    <div id="filterDiv">
        <fieldSet style="padding: 5px; border: 1px solid #ccc;">
            <legend style="font-weight: bold">Visualize By Resource</legend>
            <input id="filterInput" name="filterInput" value="">
            <input id="visualizeBut" type="submit" value="Visualize">
        </fieldSet>
    </div>

    <div style="margin: 10px;">
        <div style="display: inline-block">
            Order: <select id="order">
            <option value="name">by Name</option>
            <option value="count">by Frequency</option>
        </select>
        </div>

        <div style="display:inline-block;"><span id="selPairText"></span></div>
    </div>

    <div id="heatmap">
    </div>

</div>
<script type="text/javascript">
    $(function () {
        $('.menu').fixedMenu();
        var getResourcesUrl = "${g.createLink(controller: 'nerResourceRef', action: 'getResourceNames')}";
        var url = "${createLink(uri: '/service/cooccurrence/mostPopular')}";
        var rdw = nif.rdwModule;
        rdw.handleResourceFilter($('#filterDiv'), getResourcesUrl);
        $('#visualizeBut').click(function (event) {
            event.preventDefault();
            var resourceName = $.trim($('#filterInput').val());
            if (resourceName) {
                url = "${createLink(uri: '/service/cooccurrence/')}";
                url += resourceName;
                console.log("url:" + url);
                $('svg', '#heatmap').remove();
                $.getJSON(url, showCooccurenceMatrix);
            }
        });
        console.log('url:' + url);

        var showCooccurenceMatrix = function (coocData) {
            var margin = {top: 80, right: 0, bottom: 10, left: 80},
                    width = 720,
                    height = 720;
            var maxZ = coocData.maxPairs ? coocData.maxPairs : 457;
            var x = d3.scale.ordinal().rangeBands([0, width]),
                    z = d3.scale.sqrt().domain([0, maxZ]).clamp(true),
                    c = d3.scale.category10().domain(d3.range(10));

            var svg = d3.select("#heatmap").append("svg")
                    .attr("width", width + margin.left + margin.right)
                    .attr("height", height + margin.top + margin.bottom)
                    .style("margin-left", "0px")
                    .append("g")
                    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
            var matrix = [],
                    nodes = coocData.nodes,
                    n = nodes.length;
            // Compute index per node.
            nodes.forEach(function (node, i) {
                node.index = i;
                node.count = 0;
                matrix[i] = d3.range(n).map(function (j) {
                    return {x: j, y: i, z: 0};
                });
            });

            // Convert links to matrix; count co-occurrences.
            coocData.links.forEach(function (link) {
                matrix[link.source][link.target].z += link.value;
                matrix[link.target][link.source].z += link.value;
                matrix[link.source][link.source].z += link.value;
                matrix[link.target][link.target].z += link.value;
                nodes[link.source].count += link.value;
                nodes[link.target].count += link.value;
            });

            // Precompute the orders.
            var orders = {
                name: d3.range(n).sort(function (a, b) {
                    return d3.ascending(nodes[a].name, nodes[b].name);
                }),
                count: d3.range(n).sort(function (a, b) {
                    return nodes[b].count - nodes[a].count;
                }),
                group: d3.range(n).sort(function (a, b) {
                    return nodes[b].group - nodes[a].group;
                })
            };

            // The default sort order.
            x.domain(orders.name);
            svg.append("rect")
                    .attr("class", "background")
                    .attr("width", width)
                    .attr("height", height);

            var row = svg.selectAll(".row")
                    .data(matrix)
                    .enter().append("g")
                    .attr("class", "row")
                    .attr("transform", function (d, i) {
                        return "translate(0," + x(i) + ")";
                    })
                    .each(row);

            row.append("line")
                    .attr("x2", width);

            row.append("text")
                    .attr("x", -6)
                    .attr("y", x.rangeBand() / 2)
                    .attr("dy", ".32em")
                    .attr("text-anchor", "end")
                    .text(function (d, i) {
                        return nodes[i].name;
                    });

            var column = svg.selectAll(".column")
                    .data(matrix)
                    .enter().append("g")
                    .attr("class", "column")
                    .attr("transform", function (d, i) {
                        return "translate(" + x(i) + ")rotate(-90)";
                    });

            column.append("line")
                    .attr("x1", -width);

            column.append("text")
                    .attr("x", 6)
                    .attr("y", x.rangeBand() / 2)
                    .attr("dy", ".32em")
                    .attr("text-anchor", "start")
                    .text(function (d, i) {
                        return nodes[i].name;
                    });
            function row(row) {
                var cell = d3.select(this).selectAll(".cell")
                        .data(row.filter(function (d) {
                            return d.z;
                        }))
                        .enter().append("rect")
                        .attr("class", "cell")
                        .attr("x", function (d) {
                            return x(d.x);
                        })
                        .attr("width", x.rangeBand())
                        .attr("height", x.rangeBand())
                        .style("fill-opacity", function (d) {
                            return z(d.z);
                        })
                        .style("fill", function (d) {
                            return nodes[d.x].group == nodes[d.y].group ? c(nodes[d.x].group) : null;
                        })
                        .on("mouseover", mouseoverHandler)
                        .on("mouseout", mouseoutHandler);
            }

            function mouseoverHandler(p) {
                console.log('mouseover');
                d3.selectAll(".row text").classed("active", function (d, i) {
                    return i == p.y;
                });
                d3.selectAll(".column text").classed("active", function (d, i) {
                    return i == p.x;
                });
                d3.select('#selPairText').text(nodes[p.x].name + ' - ' + nodes[p.y].name + ' (' + matrix[p.x][p.y].z + ')');
                return false;
            }

            function mouseoutHandler() {
                d3.selectAll("text").classed("active", false);
                d3.select('#selPairText').text('');
                return false;
            }

            d3.select("#order").on("change", function () {
                clearTimeout(timeout);
                order(this.value);
            });
            function order(value) {
                x.domain(orders[value]);
                var t = svg.transition().duration(2500);
                t.selectAll(".row")
                        .delay(function (d, i) {
                            return x(i) * 4;
                        })
                        .attr("transform", function (d, i) {
                            return "translate(0," + x(i) + ")";
                        })
                        .selectAll(".cell")
                        .delay(function (d) {
                            return x(d.x) * 4;
                        })
                        .attr("x", function (d) {
                            return x(d.x);
                        });

                t.selectAll(".column")
                        .delay(function (d, i) {
                            return x(i) * 4;
                        })
                        .attr("transform", function (d, i) {
                            return "translate(" + x(i) + ")rotate(-90)";
                        });
            }

            var timeout = setTimeout(function () {
                order("name");
                d3.select("#order").property("selectedIndex", 0).node().focus();
            }, 5000);
        };
        $.getJSON(url, showCooccurenceMatrix);

    });
</script>
</body>
</html>