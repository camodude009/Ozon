google.charts.load('current', {'packages': ['corechart']});
//google.charts.setOnLoadCallback(drawChart);

var newSelect = document.createElement('select');
newSelect.id = "mkt";

// Create a request variable and assign a new XMLHttpRequest object to it.
var request = new XMLHttpRequest();

// Open a new connection, using the GET request on the URL endpoint
request.open('GET', "http://localhost:8090/markets", true);

request.onload = function () {
  // Begin accessing JSON data here
  var data = JSON.parse(this.response);

  data.forEach(market => {
    var opt = document.createElement("option");
  opt.value = market;
  opt.innerHTML = market; // whatever property it has
  // then append it to the select element
  newSelect.appendChild(opt);
});
  document.getElementById("options").insertBefore(newSelect, document.getElementById("options").firstChild.nextSibling);
}

// Send request
request.send();

function requestData(){
  summary();
  chart();
}

function summary() {
  document.getElementById("rawresults").value = "";
  document.getElementById('status').innerHTML = "Please Wait...";
  var url = "http://localhost:8090/summary/"
  var market = document.getElementById("mkt").value;
  var from = document.getElementById("from").value;
  var to = document.getElementById("to").value;

  if (market.value == "") {
    market = "binance-BTCUSDT";
  }
  if (from == "" || to == "") {
    from = new Date().getTime() - (60 * 60 * 1000);
    to = (new Date()).getTime();
    console.log("Default from=" + from +" and to="+ to);
  }

  url = url + market + "?" + "from=" + from + "&to=" + to;
  console.log("Request:" + url);

  // Create a request variable and assign a new XMLHttpRequest object to it.
  var request = new XMLHttpRequest();

  // Open a new connection, using the GET request on the URL endpoint
  request.open('GET', url, true);

  request.onload = function () {
    // Begin accessing JSON data here
    var data = JSON.parse(this.response);
    var element = document.getElementById("rawresults");
    element.value = "Number of trades: " + data.count +
        "\nMax price: " + data.max +
        "\nMin price: " + data.min +
        "\nVolume: " + data.volume;
    document.getElementById('status').innerHTML = "";
  }

  // Send request
  request.send();

}

function chart() {

  document.getElementById('status').innerHTML = "Please Wait...";
  var url = "http://localhost:8090/raw/"
  var market = document.getElementById("mkt").value;
  var from = document.getElementById("from").value;
  var to = document.getElementById("to").value;
  var interval = document.getElementById("interval").value;

  if (market.value == "") {
    market = "binance-BTCUSDT";
  }
  if (interval == ""){
    interval = 1;
    console.log("Default interval="+interval);
  }
  if (from == "" || to == "") {
    from = new Date().getTime() - (60 * 60 * 1000);
    to = (new Date()).getTime();
    console.log("Default from=" + from +" and to="+ to);
  }
  var candles = (to - from)/(interval*60*1000);
  console.log(candles+" candles");

  if (candles > 1000) {
    window.alert("Too many candles, choose a larger interval or a smaller time range");
    return;
  }

  url = url + market + "?" + "from=" + from + "&to=" + to;
  console.log("Request:" + url);
  // Create a request variable and assign a new XMLHttpRequest object to it.
  var request = new XMLHttpRequest();

  // Open a new connection, using the GET request on the URL endpoint
  request.open('GET', url, true);

  var candles = [];
  request.onload = function () {
    // Begin accessing JSON data here
    var raw_data = JSON.parse(this.response);
    console.log("Received " + raw_data.length + " data points")
    if(raw_data.length == 0){
      window.alert("Not enough data points to build chart, please choose another market.");
      return;
    }
    var price = [];
    var change = false;
    var minute = 1;
    var row = [];

    var currMin = raw_data[0].time;
    raw_data.forEach(element => {
      if(element.time - currMin >= interval*60*1000){
      change = true;
      currMin = element.time;
    }

    //populate the row and start new set of prices and new row for new minute
    if (change) {
      row.push(minute);//minute
      var open = price[0];
      var close = price[price.length - 1];//close

      var max = Math.max.apply(null, price);//max
      var min = Math.min.apply(null, price);//min

      row.push(min);
      row.push(open);
      row.push(close);
      row.push(max);

      candles.push(row);

      price = [];
      row = [];
      change = false;
      minute++;

    }
    price.push(element.price);
  });

    var chart_data = google.visualization.arrayToDataTable(candles, true);
    var options = {legend: 'none'};
    var chart = new google.visualization.CandlestickChart(document.getElementById('chart_div'));
    chart.draw(chart_data, options);
  }

  // Send request
  request.send();
}
