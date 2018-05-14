var venueMarkers = [];
var venues = [];
var venueCircles = [];

function googleMapShowVenuesButtonInitialization2() {
    $("#map-show-venues-button").click(function () {
        if (!isValidEnvironment()) {
            return;
        }

        var city = JSON.parse(sessionStorage.getItem(MAP_CITY_KEY));
        var categories = $("#google-map-venue-category").val();
        var source = $("#google-map-venue-source").val();
        var grid = $("#google-map-grid-slider").val();

        var params = jQuery.param({
            cityId: city.id,
            source: source.toUpperCase(),
            grid: grid,
            categories: categories.join(',')
        });
        $.get("http://localhost:8080" + "/boundingboxes/grid/collection?" + params, function (_venues) {
            _venues.forEach(function (venue) {
                venueMarkers.push(googleMarker(venue));
            });
            venues = venues.concat(_venues);
            showInvalidBoundingBoxes(city, categories, source);
        });
    })
}

function googleMapShowVenuesButtonInitialization() {
    $("#map-show-venues-button").click(function () {
        if (!isValidEnvironment()) {
            return;
        }

        var city = JSON.parse(sessionStorage.getItem(MAP_CITY_KEY));
        var categories = $("#google-map-venue-category").val();
        var source = $("#google-map-venue-source").val();

        var params = jQuery.param({cityId: city.id, source: source.toUpperCase(), categories: categories.join(',')});

        this.disabled = true;
        var button = this;
        $("#venues-spinner").prop("active", true);
        $.put("/venues/collection?" + params, function (_venues) {
            var venuesIds = _venues.map(function (venue) {
                return venue.id;
            });
            $.put("/venues/validation", JSON.stringify(venuesIds), function (validVenues) {
                validVenues.forEach(function (venue) {
                    venueMarkers.push(googleMarker(venue));
                });
                venues = venues.concat(validVenues);

                $("#google-map-heat-map-slider").prop("disabled", false);
                $("#google-map-grid-heat-map-button").prop("disabled", false);
                $("#map-show-venues-distribution-button").prop("disabled", false);
                $("#map-show-venues-quad-tree-grid-button").prop("disabled", false);
                $("#venues-spinner").prop("active", false);
                button.disabled = false;

                showInvalidBoundingBoxes(city, categories, source);
            }, function () {
                console.error("Heat-map venues validation service temporary unavailable...");
                alert("Heat-map venues validation service temporary unavailable...\nRepeat your last act after some pause or contact with developer");
            });
        });
    })
}

function googleMapShowVenuesDistributionButtonInitialization() {
    $("#map-show-venues-distribution-button").click(function () {
        if (venues.length === 0) {
            return;
        }
        var venuesIds = venues.map(function (venue) {
            return venue.id;
        });
        var button = this;
        button.disabled = true;
        $("#venues-spinner").prop("active", true);
        $.put("/venues/distribution", JSON.stringify(venuesIds), function (distribution) {
            distribution.forEach(function (marker) {
                venueCircles.push(googleMarkerCircle(marker));
            });
            $("#venues-spinner").prop("active", false);
            button.disabled = false;
        });
    });
}

function clearVenueMarkers() {
    venueMarkers.forEach(function (marker) {
        marker.setMap(null);
    });
    venueMarkers = [];
}
