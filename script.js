function summary() {
  var url = "http://localhost:8090/summary/"
  var market = "binance-BTCUSDT"
  // var newElement= "<input type='textbox' name='myTextbox'>";
  // document.body.innerHTML=newElement;
  var mkt = document.getElementById("mkt");
  var from = document.getElementById("from");
  var to = document.getElementById("to");
  if(mkt.value != ""){
    market = mkt.value;
  }

  if(from.value != "" && to.value != ""){
    console.log("here")
    window.location.href = url+market+"?"+"from="+from.value+"&to="+to.value;
  }else{
    window.location.href = url+market;
  }
}

function raw() {
  var url = "http://localhost:8090/raw/"
  var market = "binance-BTCUSDT"
  var mkt = document.getElementById("mkt");
  var from = document.getElementById("from");
  var to = document.getElementById("to");
  if(mkt.value != ""){
    market = mkt.value;
  }

  if(from.value != "" && to.value != ""){
    window.location.href = url+market+"?"+"from="+from.value+"&to="+to.value;
  }else{
    window.location.href = url+market;
  }

  
}


// Create a request variable and assign a new XMLHttpRequest object to it.
var request = new XMLHttpRequest();

// Open a new connection, using the GET request on the URL endpoint
request.open('GET', 'http://localhost:8090/summary/binance-BTCUSDT', true);

request.onload = function () {
  // Begin accessing JSON data here
  var data = JSON.parse(this.response);
}


// Send request
request.send();


