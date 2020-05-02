if (sessionStorage.getItem("isAdmin") === "true") {
    document.getElementById("class_logistics_nav").style.display = "inline";
}else{
    document.getElementById("class_logistics_nav").style.display = "none";
}