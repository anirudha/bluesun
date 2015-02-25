
function GremlinDataSource(){

   var allVertices = new Array();
   var allEdges = new Array();


   this.queryData(){
	<script> type="text/javascript" src="gremlin.js"></script>
	var gremlin = require('gremlin-client');
	var client = gremlin.createClient(8182, 'localhost');
	var query = client.stream('g.V()'); //default data
	var i = 0;
	var j = 0;
	query.on('data', function(result) {
  		// Handle vertices
		this.allVertices(i) = result;
		var outEdgeIterator = result.outE();
		for (v in outEdgeIterator){
			this.allEdges(j) = (result,v);
			j++;
		};
		var inEdgeIterator = result.inE();
		for (v in inEdgeIterator){
			this.allEdges(j) = (v,result);
			j++;
		};		
		i++;
	});
	query.on('end', function() {
  		console.log("All results fetched");
	});
   };

   this.getVertices() = function(){
	return this.allVertices;
   };

  this.getEdges() = function(){
	return this.allEdges;
  };
