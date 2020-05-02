var motorSocket = new WebSocket("ws://localhost:8080/MotorControl");

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


function sendMotorAEData() {
    var azVal = document.getElementById("azimuth").value;
    var elevationVal = document.getElementById("elevation").value;

    // Converting JSON data to string
    var data = JSON.stringify({header: "updateAzimuthElevation", azimuth: azVal, elevation: elevationVal});

    motorSocket.send(data);

}
