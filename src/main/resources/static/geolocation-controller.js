function geolocationReverseBrowser() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(function (pos) {
            geolocationReverse({lat: pos.coords.latitude, lng: pos.coords.longitude}, function (city) {
                console.log('automation browser geolocation reverse city: ' + JSON.stringify(city));
                var southwest = {lat: city.boundingBox.southWest.latitude, lng: city.boundingBox.southWest.longitude};
                var northeast = {lat: city.boundingBox.northEast.latitude, lng: city.boundingBox.northEast.longitude};
                googleMap.fitBounds(new google.maps.LatLngBounds(southwest, northeast));
                googleMapCity = city;

                var googleSearchBox = document.getElementById("google-city-search-box");
                googleSearchBox.value = city.city + ', ' + city.country;
            }, function () {
                console.warn("heat-map geolocation service temporary unavailable...");
                alert("Heat-Map geolocation service temporary unavailable...");
                googleMapCity = undefined;
            });
        }, function () {
            console.warn("browser does not support geolocation functionality");
            alert('<p>Your browser does not support geolocation functionality.</p>' +
                '<p>Initial location: ITMO University, Saint-Petersburg, Russia</p>' +
                '<p>Use search box to find the city of interest</p>');
        });
        return;
    }
    alert('<p>Your browser does not support geolocation functionality.</p>' +
        '<p>Initial location: ITMO University, Saint-Petersburg, Russia</p>' +
        '<p>Use search box to find the city of interest</p>');
}


function geolocationReverse(args, callback, error) {
    console.log(url('/geolocation/reverse?' + jQuery.param(args)));
    jQuery.ajax({
        type: "GET",
        dataType: "json",
        url: url('/geolocation/reverse?' + jQuery.param(args)),
        async: false,
        success: callback,
        error: error
    });
}
