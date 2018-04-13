function geolocationReverseBrowser() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(function (pos) {
            var params = $.param({lat: pos.coords.latitude, lng: pos.coords.longitude});
            console.log("automation browser geolocation coordinates: " + params);
            $.put("/geolocation/reverse?" + params, undefined, function (city) {
                var southwest = {lat: city.boundingBox.southWest.latitude, lng: city.boundingBox.southWest.longitude};
                var northeast = {lat: city.boundingBox.northEast.latitude, lng: city.boundingBox.northEast.longitude};
                MAP_GOOGLE.fitBounds(new google.maps.LatLngBounds(southwest, northeast));
                sessionStorage.setItem(MAP_CITY_KEY, JSON.stringify(city));
                console.log('City: \'' + city.city + '\' with id \'' + city.id + '\' was saved to session storage by key \'google-map-city\'');
                $("#google-map-city-search-box").val(city.city + ', ' + city.country);
            }, function () {
                console.warn("Heat-map geolocation service temporary unavailable...");
                alert("Heat-Map geolocation service temporary unavailable...\nRepeat your last act after some pause or contact with developer");
                sessionStorage.removeItem(MAP_CITY_KEY);
            });
        }, function () {
            console.warn("User browser does not support geolocation functionality");
            alert('Your browser does not support geolocation functionality.\nInitial location: ITMO University, Saint-Petersburg, Russia\nUse search box to find the city of interest');
        });
    } else {
        alert('Your browser does not support geolocation functionality.\nInitial location: ITMO University, Saint-Petersburg, Russia\nUse search box to find the city of interest');
    }
}

