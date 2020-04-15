var slider = document.getElementById("antAngleSlider");
var output = document.getElementById("set_angle");
output.innerHTML = slider.value;

slider.oninput = function() {
    output.innerHTML = this.value;
}

function toggleTransmit() {
    var x = document.getElementById("data_entry_box");
    var state = document.getElementById("transmit_btn");
    var other = document.getElementById("recieve_btn");
    state.classList.add("active");
    other.classList.toggle("active");


    x.style.display = "block";

}

function toggleRecieve() {
    var x = document.getElementById("data_entry_box");
    var other = document.getElementById("transmit_btn");
    var state = document.getElementById("recieve_btn");
    state.classList.add("active");
    other.classList.toggle("active");

    x.style.display = "none";

}

function sendData(clicked_id) {
    var programAction = clicked_id; //launch_orbitron, kill_orbitron
    var programActionArr = programAction.split("_");

    var action = programActionArr[0]; //launch or kill
    if (action === "launch") {
        action = "start_program";
    } else {
        action = "stop_program"
    }
    var program = programActionArr[1]; //which program

    var data = JSON.stringify({ header: action, body: program });

    var xhr = new XMLHttpRequest();

    xhr.open("POST", "/ProgramControl", false);
    xhr.send(data);

    console.log("RESP: " + xhr.response);

    var response = JSON.parse(xhr.response); //Could check and see if request was successful

    var responseArray = response.header.split("_");
    document.querySelector('#onsuccess').innerHTML = responseArray[0] + " " + program + " " + responseArray[1];

}

