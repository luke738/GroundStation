var socket = new WebSocket("ws://localhost:8080/UHFData");
var transmit = true;
var slider = document.getElementById("antAngleSlider");
var output = document.getElementById("set_angle");
output.innerHTML = slider.value;

//program control: show kill desktop button if admin
var admin = sessionStorage.getItem("isAdmin");
if (admin == "true") {
    document.getElementById("shutdown").style.display = "block";
}

socket.addEventListener('open', function(event) {
    socket.send(JSON.stringify({}));
});

socket.addEventListener('message', function(event) {
    console.log(event.data);
    document.querySelector("#console_view").innerHTML = event.data; //not appending, new data each time
});

slider.oninput = function() {
    output.innerHTML = this.value;
};

// Start file download.
document.getElementById("dwn-btn").addEventListener("click", function(){
    download1();
}, false);


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

    var array = command.value.split(" ");

    // Converting JSON data to string
    var data = JSON.stringify({header: array[0], body: array[1], userID: 1});

    if (transmit) {
        socket.onopen = function (event) {
            socket.send(data);
        }
    }

}

function download1() {

    var xhr = new XMLHttpRequest();
    xhr.open("POST", "/TLEData",false);
    var data = {};
    xhr.send(data);
// console.log(xhr.response);
    var response = JSON.parse(xhr.response);
// console.log(response);
    var success = response.header;
    if (success == "tle_success") {
        document.querySelector("#download_onsuccess").innerHTML = "TLE file downloaded";
    }

}

function program_sendData(clicked_id) {

    var programAction = clicked_id; //launch_orbitron, kill_orbitron, or shutdown
    var data = null;
    if (programAction == "shutdown") {
        data = JSON.stringify({header: "shutdown"});
    } else {
        var programActionArr = programAction.split("_");

        var action = programActionArr[0]; //launch or kill
        if (action === "launch") {
            action = "start_program";
        } else if (action === "kill") {
            action = "stop_program"
        }
        var program = programActionArr[1]; //which program

        data = JSON.stringify({ header: action, body: program });
    }

    var xhr = new XMLHttpRequest();

    xhr.open("POST", "/ProgramControl", false);
    xhr.send(data);

    console.log("RESP: " + xhr.response);

    var response = JSON.parse(xhr.response); //Could check and see if request was successful

    var responseArray = response.header.split("_");
    if (responseArray[0] === "shutdown") {
        document.querySelector('#shutdown_onsuccess').innerHTML = " " + responseArray[0] + " " + responseArray[1];
    } else {
        document.querySelector('#onsuccess').innerHTML = responseArray[0] + " " + program + " " + responseArray[1];
    }
}