/*
 * graphvisualization.js
 *
 * Graph visualization library. Requires d3.js.
 */

/*
 * Graph data source. Takes an implementation object, which should implement
 * 'getVertices' and 'getIndices' functions.
 */
function GraphDataSource(implementation) {
    this.getVertices = implementation.getVertices;
    this.getEdges = implementation.getEdges;
}

/*
 * Graph visualization. Should be bound to a DOM element and data source to
 * start visualization.
 */
function GraphVisualization() {
    this.element = null;               // DOM element to display in
    this.selection = null;             // d3 selection of DOM element
    this.dataSource = null;            // Data source for vertices/edges
    this.svg = null;                   // SVG inside DOM element
    this.width = 100;                  // Width of visualization
    this.height = 100;                 // Height of visualization
    this.onvertexmousedown = null;     // Mouse down event on a vertex
    this.onedgemousedown = null;       // Mouse down event on an edge
    this.oncanvasmouseodwn = null;     // Mouse down event on the background
    this.force = d3.layout.force();    // d3 force layout underlying visualization
    this.edgeStroke = "black";         // Edge stroke color
    this.edgeRadius = 1;               // Edge radius
    this.vertexStroke = "black";       // Vertex stroke color
    this.vertexFill = "blue";          // Vertex fill color
    this.vertexRadius = 8;             // Vertex radius
    this.edgeLength = 50;              // Edge length

    /*
     * Bind the visualization to a DOM element
     */
    this.bindElement = function(element) {
        this.element = element;
        this.selection = d3.select(element);
        this.svg = this.selection.append("svg");

        this.updateSVGAttribs();
        this.refresh();

        return this;
    };

    /*
     * Bind the visualization to a data source
     */
    this.bindDataSource = function(dataSource) {
        this.dataSource = dataSource;
        this.refresh();

        return this;
    };

    /*
     * Set the size of the visualization
     */
    this.size = function(width, height) {
        this.force.size([width, height]);

        this.width = width;
        this.height = height;

        this.updateSVGAttribs();
        this.refresh();

        return this;
    };

    /*
     * Update attributes SVG for color, size, etc. Called when these properties
     * change or the DOM element is rebound.
     */
    this.updateSVGAttribs = function() {
        this.svg
            .attr("width", this.width)
            .attr("height", this.height);
    };

    /*
     * Update the visualization when the data from the data source changes. This
     * is called automatically when the visualization is bound to a new data
     * source or DOM element.
     */
    this.refresh = function() {
        if (!this.dataSource || !this.svg)
            return;

        var vertices = this.dataSource.getVertices();
        var edges = this.dataSource.getEdges();

        var edge = this.svg.selectAll(".link")
            .data(edges);
        edge.enter()
            .append("line")
            .attr("class", "link")
            .attr("stroke-width", this.edgeRadius)
            .style("stroke", this.edgeStroke);
        edge.exit()
            .remove();

        var node = this.svg.selectAll(".node")
            .data(vertices);
        node.enter()
            .append("circle")
            .attr("class", "node")
            .attr("r", this.vertexRadius)
            .style("fill", this.vertexFill)
            .style("stroke", this.vertexStroke)
            .call(this.force.drag);
        node.exit()
            .remove();

        this.edgeSelection = edge;
        this.nodeSelection = node;

        this.updateEvents();

        var tick = function(e) {
            edge.attr("x1", function(d) { return d.source.x; })
                .attr("y1", function(d) { return d.source.y; })
                .attr("x2", function(d) { return d.target.x; })
                .attr("y2", function(d) { return d.target.y; });

            node.attr("cx", function(d) { return d.x; })
                .attr("cy", function(d) { return d.y; });
        };

        this.force
            .nodes(vertices)
            .links(edges)
            .on("tick", tick)
            .linkDistance(this.edgeLength)
            .start();
    };

    /*
     * Update events on d3 selections
     */
    this.updateEvents = function() {
        if (!this.edgeSelection)
            return;

        this.edgeSelection.on("mousedown", this.edgemousedown);
        this.nodeSelection.on("mousedown", this.vertexmousedown);
        this.svg.on("mousedown", this.canvasmousedown);
    };

    /*
     * Handle various events:
     *
     *    vertexmousedown: Mouse down on a vertex
     *    edgemousedown: Mouse down on an edge
     *    canvasmousedown: Mouse down on background
     */
    this.on = function(evt_name, f) {
        if (evt_name == "vertexmousedown")
            this.vertexmousedown = f;
        else if (evt_name == "edgemousedown")
            this.edgemousedown = f;
        else if (evt_name == "canvasmousedown")
            this.canvasmousedown = f;

        this.updateEvents();

        return this;
    };
}
