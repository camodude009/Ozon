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

function summary() {

  document.getElementById("rawresults").value = "";
  document.getElementById('status').innerHTML = "Please Wait...";
  document.getElementById('status').style.color = randomColors();
  var url = "http://localhost:8090/summary/"
  var market = "binance-BTCUSDT"
  var mkt = document.getElementById("mkt");
  var from = document.getElementById("from");
  var to = document.getElementById("to");
  if (mkt.value != "") {
    market = mkt.value;
  }

  if (from.value != "" && to.value != "") {
    console.log("here")
    url = url + market + "?" + "from=" + from.value + "&to=" + to.value;
  } else {
    url = url + market;
  }

  // Create a request variable and assign a new XMLHttpRequest object to it.
  var request = new XMLHttpRequest();

  // Open a new connection, using the GET request on the URL endpoint
  request.open('GET', url, true);

  request.onload = function () {
    // Begin accessing JSON data here
    var data = JSON.stringify(JSON.parse(this.response));
    var element = document.getElementById("rawresults");
    element.value = data;
    document.getElementById('status').innerHTML = "";
  }


  // Send request
  request.send();

}

function raw() {

  document.getElementById("rawresults").value = "";
  document.getElementById('status').innerHTML = "Please Wait...";
  document.getElementById('status').style.color = randomColors();
  var url = "http://localhost:8090/raw/"
  var market = "binance-BTCUSDT"
  var mkt = document.getElementById("mkt");
  var from = document.getElementById("from");
  var to = document.getElementById("to");
  if (mkt.value != "") {
    market = mkt.value;
  }

  if (from.value != "" && to.value != "") {
    url = url + market + "?" + "from=" + from.value + "&to=" + to.value;
  } else {
    url = url + market;
  }

  // Create a request variable and assign a new XMLHttpRequest object to it.
  var request = new XMLHttpRequest();

  // Open a new connection, using the GET request on the URL endpoint
  request.open('GET', url, true);

  request.onload = function () {
    // Begin accessing JSON data here
    var data = JSON.parse(this.response);
    var para = "";
    data.forEach(element => {
      para += "Market:" + element.market + " Market_Buy:" + element.market_buy + " Price:" + element.price + " Volume:" + element.volume + " Time:" + JSON.stringify(element.time) + "\n";

      //console.log(movie);
    });
    var element = document.getElementById("rawresults");
    element.value = para;
    document.getElementById('status').innerHTML = "";
  }


  // Send request
  request.send();


}


function randomColors() {
  return '#' + Math.floor(Math.random() * 16777215).toString(16);
}


