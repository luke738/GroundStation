if (sessionStorage.getItem("loggedIn") !== "true") {
    window.location.href = "/login.html";
}

//update global
dashBoardUHF = false;

//hide motor control dashboard controls, delete once sband controls work
document.getElementById("s_band_motor_controls").style.display = "none";

//program control: show kill desktop button if admin
if (sessionStorage.getItem("isAdmin") !== "true") {
    document.getElementById("shutdown").style.display = "none";
    document.getElementById("calibrate_wrap_check").style.display = "none";
} else {
    document.getElementById("shutdown").style.display = "block";
    document.getElementById("calibrate_wrap_check").style.display = "inline";
}

function sendData(clicked_id) {

    var programAction = clicked_id; //launch_orbitron, kill_orbitron, or shutdown
    var data = null;
    if (programAction === "shutdown") {
        data = JSON.stringify({header: "sband", body: JSON.stringify({header: "shutdown"})});
    } else {
        var programActionArr = programAction.split("_");

        var action = programActionArr[0]; //launch or kill
        if (action === "launch") {
            action = "start_program";
        } else if (action === "kill") {
            action = "stop_program"
        }
        var program = programActionArr[1]; //which program

        data = JSON.stringify({header:"sband", body: JSON.stringify({header: action, body: program})});

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
