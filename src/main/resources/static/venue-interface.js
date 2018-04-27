var venueMarkers = [];
var venues = [];

function googleMapShowVenuesButtonInitialization() {
    $("#map-show-venues-button").click(function () {
        if (!isValidEnvironment()) {
            return;
        }

        var city = JSON.parse(sessionStorage.getItem(MAP_CITY_KEY));
        var categories = $("#google-map-venue-category").val();
        var source = $("#google-map-venue-source").val();

        var params = jQuery.param({cityId: city.id, source: source.toUpperCase(), categories: categories.join(',')});
        $.put("/venues/collection?" + params, function (_venues) {
            var venuesIds = _venues.map(function (venue) { return venue.id;  });
            $.put("/venues/validation", JSON.stringify(venuesIds), function (validVenues) {
                validVenues.forEach(function (venue) {
                    venueMarkers.push(googleMarker(venue));
                });
                venues = venues.concat(validVenues);
                $("#google-map-heat-map-slider").prop("disabled", false);
                showInvalidBoundingBoxes(city, categories, source);
            }, function () {
                console.error("Heat-map venues validation service temporary unavailable...");
                alert("Heat-map venues validation service temporary unavailable...\nRepeat your last act after some pause or contact with developer");
            });
        }, function () {
            console.error("Heat-map venues mine service temporary unavailable...");
            alert("Heat-map venues mine service temporary unavailable...\nRepeat your last act after some pause or contact with developer");
        });
    })
}

function clearVenueMarkers() {
    venueMarkers.forEach(function (marker) {
        marker.setMap(null);
    });
    venueMarkers = [];
}
