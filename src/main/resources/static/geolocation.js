var geolocationReverseBrowser = function () {
    var message = 'Your browser does not support geolocation functionality.\n\nInitial location: ITMO University, Saint-Petersburg, Russia\n\nUse search box to find the city of interest';
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(function (pos) {
            geolocationReverse({lat: pos.coords.latitude, lng: pos.coords.longitude});
        }, function () {
            alert(message);
        });
        return;
    }
    alert(message);
};

var geolocationReverse = function (args) { // {lat: 0.0, lng: 0.0}
    console.log(server + 'geolocation/reverse?' + $.param(args));
    jQuery.ajax({
        type: "GET",
        dataType: "json",
        url: server + 'geolocation/reverse' + '?' + jQuery.param(args),
        success: function (city) {
            console.log('geolocation reverse city: ' + city.city + ' / ' + city.country);
            var southwest = {lat: city.boundingBox.southWest.latitude, lng: city.boundingBox.southWest.longitude};
            var northeast = {lat: city.boundingBox.northEast.latitude, lng: city.boundingBox.northEast.longitude};
            map.fitBounds(new google.maps.LatLngBounds(southwest, northeast));
            boundingBoxes.push(googleRectangle(city.boundingBox));
        }
    });
};
