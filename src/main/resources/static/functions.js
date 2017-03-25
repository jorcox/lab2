var client;
var subscription = null;

var subscriptionEndpoint = '/queue/search/'



$(document).ready(function() {
	stompConnection();
	registerSearch();
});

function stompConnection() {
	client = Stomp.over(new SockJS("/twitter"));
	
	var headers = {};
	var connectCallback = {};
	client.connect(headers, connectCallback);
}

function subscribeTwitter(query) {

	if (subscription != null) subscription.unsubscribe();

	subscription = client.subscribe(subscriptionEndpoint + query, function(tweet){

		var template =
			'<div class="row panel panel-default">'
			+	    '<div class="panel-heading">'
			+	        '<a href="https://twitter.com/{{fromUser}}"'
			+	           'target="_blank"><b>@{{fromUser}}</b></a>'
			+	        '<div class="pull-right">'
			+	            '<a href="https://twitter.com/{{fromUser}}/status/{{idStr}}"'
			+ 				'target="_blank"><span class="glyphicon glyphicon-link"></span></a>'
			+	        '</div>'
			+	    '</div>'
			+	    '<div class="panel-body">{{{unmodifiedText}}}</div>'
			+'</div>';

		var data = JSON.parse(tweet.body);

		var html = Mustache.to_html(template, data);

		$("#resultsBlock").append(html);
	}, function(error){
		// Error connecting to the endpoint
		console.log('Error: ' + error);
	});

	client.send('/app/search', {}, JSON.stringify({'query' : query}));
}

function registerSearch() {
	$("#search").submit(function(ev){
		event.preventDefault();
		$("#resultsBlock").empty();	
		// Creating WebSocket
		if (client == null) stompConnection();
		else subscribeTwitter($('input#q').val());
	});


}

