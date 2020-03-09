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

function sendData() {

}

