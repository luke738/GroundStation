if (sessionStorage.getItem("loggedIn") !== "true") {
    window.location.href = "/login.html";
}

//program control: show kill desktop button if admin
var admin = sessionStorage.getItem("isAdmin");
if (admin === "true") {
    document.getElementById("shutdown").style.display = "block";
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

        data = JSON.stringify({header: ["sband"], body: JSON.stringify({header: [action], body: [program]})});

    }

    var xhr = new XMLHttpRequest();

    xhr.open("POST", "/ProgramControl", false);
    xhr.send(data);

    console.log("RESP: " + xhr.response);

    var response = JSON.parse(xhr.response); //Could check and see if request was successful

    //recieves not logged in msg from backend
    if (response.header === "loginError") {
        sessionStorage.clear();
        window.location.href = "/login.html";
    } else {
        var responseArray = response.header.split("_");
        if (responseArray[0] === "shutdown") {
            document.querySelector('#shutdown_onsuccess').innerHTML = " " + responseArray[0] + " " + responseArray[1];
        } else {
            document.querySelector('#onsuccess').innerHTML = responseArray[0] + " " + program + " " + responseArray[1];
        }
    }
}


function download1() {

    var xhr = new XMLHttpRequest();
    xhr.open("POST", "/TLEData",false);
    var data = JSON.stringify({"header": "sband"});
    xhr.send(data);
// console.log(xhr.response);
    var response = JSON.parse(xhr.response);
// console.log(response);

    //recieves not logged in msg from backend
    if (response.header === "loginError") {
        sessionStorage.clear();
        window.location.href = "/login.html";
    } else {
        var success = response.header;
        if (success == "tle_success") {
            document.querySelector("#download_onsuccess").innerHTML = "TLE file downloaded";
        } else {
            document.querySelector("#download_onsuccess").innerHTML = response.header + " " + response.body;
        }
    }
}
