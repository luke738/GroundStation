var isAdmin = sessionStorage.getItem("isAdmin");

if (isAdmin == "isAdmin") {
    document.getElementById("class_logistics_nav").style.display = "inline";
}else{
    document.getElementById("class_logistics_nav").style.display = "none";
}