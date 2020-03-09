var socket = new WebSocket("ws://localhost:8080/UHFData");
var transmit = true;
var slider = document.getElementById("antAngleSlider");
var output = document.getElementById("set_angle");
output.innerHTML = slider.value;

socket.addEventListener('open', function(event) {
    socket.send(JSON.stringify({header: "hello uhf data socket", body: ""}));
});

socket.addEventListener('message', function(event) {
    console.log(event.data);
    document.querySelector("#console_view").innerHTML = event.data; //not appending, new data each time
});

slider.oninput = function() {
    output.innerHTML = this.value;
}

function toggleTransmit() {
    transmit = true;
    var x = document.getElementById("data_entry_box");
    var state = document.getElementById("transmit_btn");
    var other = document.getElementById("recieve_btn");
    state.classList.add("active");
    other.classList.toggle("active");

    x.style.display = "block";

}

function toggleRecieve() {
    transmit = false;
    var x = document.getElementById("data_entry_box");
    var other = document.getElementById("transmit_btn");
    var state = document.getElementById("recieve_btn");
    state.classList.add("active");
    other.classList.toggle("active");

    x.style.display = "none";

}

function sendData() {
    var command = document.querySelector('#exampleFormControlTextarea1');
    // console.log(command.value);

    var array = command.value.split(" ");

    // Converting JSON data to string
    var data = JSON.stringify({header: array[0], body: array[1], userID: 1});
    // console.log(data);

    // Creating a XHR object
    // var xhr = new XMLHttpRequest();
    // xhr.open("POST", "/UHFData", false);
    // xhr.send(data);

    if (transmit) {
        socket.onopen = function (event) {
            socket.send(data);
        }
    }


    // socket.close();

    // console.log("RESP: " + xhr.response);
    // var response = JSON.parse(xhr.response); //Could check and see if request was successful

}


