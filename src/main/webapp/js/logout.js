function logout() {
    sessionStorage.clear();

    //go to login servlet and say user logged out
    var data = {};
    var xhr = new XMLHttpRequest();
    xhr.open("POST", "/Login", false);
    xhr.send(data);

    console.log("RESP: " + xhr.response);

    var response = JSON.parse(xhr.response); //Could check and see if request was successful

}