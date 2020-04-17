var motorSocket = new WebSocket("ws://localhost:8080/MotorControl");

motorSocket.addEventListener('open', function(event) {
    motorSocket.send(JSON.stringify({}));
});

motorSocket.addEventListener('message', function(event) {
    console.log(event.data);
    document.querySelector("#console_view").innerHTML = event.data; //not appending, new data each time
});


function sendMotorAngleData() {
    var slider = document.getElementById("antAngleSlider");
    var output = slider.value;

    // Converting JSON data to string
    var data = JSON.stringify({header: "updateAngle", body: output});


    motorSocket.onopen = function (event) {
        motorSocket.send(data);
    }

}

function sendMotorAEData() {
    var azVal = document.getElementById("azimuth").value;
    var elevationVal = document.getElementById("elevation").value;

    // Converting JSON data to string
    var data = JSON.stringify({header: "updateAzimuthElevation", azimuth: azVal, elevation: elevationVal});


    motorSocket.onopen = function (event) {
        motorSocket.send(data);
    }

}
