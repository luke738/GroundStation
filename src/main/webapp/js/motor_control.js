
if (sessionStorage.getItem("isAdmin") === "true") {
    document.getElementById("calibrate_wrap_check").style.display = "block";
} else {
    document.getElementById("calibrate_wrap_check").style.display = "none";
}

if (!dashBoardUHF) {
    /**
     * renable commented out code below
     * once s-band motor controls work
     */
    // var motorSocket = new WebSocket("ws://localhost:8080/MotorControl");

    // var dashboardVal = JSON.stringify({"header": "antenna_name", "body": "sband"});

    //tell motor controller it's sband dashboard
    //motorSocket.send(dashboardVal);

    //hide motor control dashboard controls, delete once sband controls work
    document.getElementById("s_band_motor_controls").style.display = "none";

} else {

    var motorSocket = new WebSocket("ws://localhost:8080/MotorControl");

    motorSocket.addEventListener("open", function(event) {
        var dashboardVal = JSON.stringify({"header": "antenna_name", "body": "uhf"});

        //tell motor controller it's uhf dashboard
        motorSocket.send(dashboardVal);
    });

    motorSocket.addEventListener('message', function(event) {

        // console.log(event.data);

        var head = event.data.header;
        var body = event.data.body;

        //check antenna wrapper light
        if (head === "antenna_wrapped")
        {
            if (body === "true")
            {
                document.getElementById("wrap_warning").innerHTML = "Antenna Wrap Warning: ON";
                document.getElementById("wrap_warning").style.color = "red";
            } else if (body === "false")
            {
                document.getElementById("wrap_warning").innerHTML = "Antenna Wrap Warning: OFF";
                document.getElementById("wrap_warning").style.color = "lightgray";
            }

        }

        //parse return value for current azimuth and elevation
        if (head === "currentAzimuth")
        {
            document.getElementById("curr_ax").innerHTML = body;

        }
        if (head === "currentElevation")
        {
            document.getElementById("curr_el").innerHTML = body;
        }

    });
}

function sendMotorAEData() {
    if (!dashBoardUHF) return;
    var azVal = document.getElementById("azimuth").value;
    var elevationVal = document.getElementById("elevation").value;

    if(azVal === "" || elevationVal === "") {
        return;
    }

    // Converting JSON data to string
    var data = JSON.stringify({header: "updateAzimuthElevation", azimuth: azVal, elevation: elevationVal});

    motorSocket.send(data);

}

function stopMotorController() {
    if (!dashBoardUHF) return;
    var data = JSON.stringify({header: "stop"});
    motorSocket.send(data);
}