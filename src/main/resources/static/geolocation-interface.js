function geolocationReverseBrowser() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(function (pos) {
            var params = $.param({lat: pos.coords.latitude, lng: pos.coords.longitude});
            console.log("automation browser geolocation coordinates: " + params);
            $.put("/geolocation/reverse?" + params, function (city) {
                var southwest = {lat: city.boundingBox.southWest.latitude, lng: city.boundingBox.southWest.longitude};
                var northeast = {lat: city.boundingBox.northEast.latitude, lng: city.boundingBox.northEast.longitude};
                MAP_GOOGLE.fitBounds(new google.maps.LatLngBounds(southwest, northeast));
                sessionStorage.setItem(MAP_CITY_KEY, JSON.stringify(city));
                console.log('City (id = ' + city.id + ', city = ' + city.city + ') was saved to session storage by key \"google-map-city\"');
                var $googleMapCitySearchBox = $("#google-map-city-search-box");
                $googleMapCitySearchBox.val(city.city + ', ' + city.country);
                $googleMapCitySearchBox.prop("invalid", false)
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

function googleMapSearchBoxInitialization() {
    var $googleMapCitySearchBox = $("#google-map-city-search-box");
    $googleMapCitySearchBox.change(function () {
        sessionStorage.removeItem(MAP_CITY_KEY);
        this.invalid = true;
    });
    var autocomplete = new google.maps.places.Autocomplete($googleMapCitySearchBox[0], {types: ['(cities)']});
    autocomplete.bindTo('bounds', MAP_GOOGLE);
    autocomplete.addListener('place_changed', function () {
        var place = autocomplete.getPlace();
        if (!place.geometry) {
            console.error("Heat-map geolocation service temporary unavailable or used incorrect place...");
            alert("Heat-map geolocation service temporary unavailable or used incorrect place...\nRepeat your last act after some pause or contact with developer");
            sessionStorage.removeItem(MAP_CITY_KEY);
            $googleMapCitySearchBox.prop("invalid", true);
            return;
        }

        $googleMapCitySearchBox.val(place.formatted_address);
        var params = $.param({lat: place.geometry.location.lat(), lng: place.geometry.location.lng()});
        console.log("search box city geolocation coordinates: " + params);
        $.put("/geolocation/reverse?" + params, function (city) {
            var southwest = {lat: city.boundingBox.southWest.latitude, lng: city.boundingBox.southWest.longitude};
            var northeast = {lat: city.boundingBox.northEast.latitude, lng: city.boundingBox.northEast.longitude};
            MAP_GOOGLE.fitBounds(new google.maps.LatLngBounds(southwest, northeast));
            sessionStorage.setItem(MAP_CITY_KEY, JSON.stringify(city));
            console.log('City (id = ' + city.id + ', city = ' + city.city + ') was saved to session storage by key \"google-map-city\"');
            $googleMapCitySearchBox.val(city.city + ', ' + city.country);
            $googleMapCitySearchBox.prop("invalid", false);
        }, function () {
            console.warn("Heat-map geolocation service temporary unavailable...");
            alert("Heat-Map geolocation service temporary unavailable...\nRepeat your last act after some pause or contact with developer");
            sessionStorage.removeItem(MAP_CITY_KEY);
            $googleMapCitySearchBox.prop("invalid", true);
        });
    });
}

