
var griBoundingBoxes = [];
var invalidBoundingBoxes = [];
var invalidBoundingBoxesXXX = [];

function googleMapGridSliderInitialization() {
    $("#google-map-grid-slider").change(function () {
        griBoundingBoxes = clearMapCollection(griBoundingBoxes);
        if (this.value === 0) {
            return;
        }
        this.disabled = true;
        var city = JSON.parse(sessionStorage.getItem(MAP_CITY_KEY));
        var params = jQuery.param({cityId: city.id, grid: this.value});
        var slider = this;
        $.get("http://localhost:8080" + "/boundingboxes/grid?" + params, function (boundingBoxes) {
            boundingBoxes.forEach(function (boundingBox) {
                griBoundingBoxes.push(googleRectangle(boundingBox, 'black'));
            });
            slider.disabled = false;
        }).fail(function () {
            console.error("Heat-map grid service temporary unavailable...");
            alert("Heat-map grid service temporary unavailable...\nRepeat your last act after some pause or contact with developer");
            slider.disabled = false;
            slider.value = 0
        });
    });
}

function clearMapCollection(collection) {
    collection.forEach(function (element) {
        element.setMap(null);
    });
    return [];
}

function clearAnyBoundingBoxes() {
    griBoundingBoxes = clearMapCollection(griBoundingBoxes);
    invalidBoundingBoxes = clearMapCollection(invalidBoundingBoxes);
    invalidBoundingBoxesXXX = clearMapCollection(invalidBoundingBoxesXXX);
}

function showInvalidBoundingBoxes(city, categories, source) {
    var params = jQuery.param({cityId: city.id, source: source.toUpperCase(), categories: categories.join(',')});
    $.get("http://localhost:8080" + "/boundingboxes/invalid?" + params, function (boundingBoxes) {
        boundingBoxes.forEach(function (boundingBox) {
            invalidBoundingBoxes.push(googleRectangle(boundingBox, 'red'));
            invalidBoundingBoxesXXX = invalidBoundingBoxesXXX.concat(googleRectangleX(boundingBox, 'red'))
        })
    });
}

function googleMapGridHeatMapInitialization() {
    $("#google-map-grid-heat-map-button").click(function () {
        if (!isValidEnvironment()) {
            return;
        }

        var city = JSON.parse(sessionStorage.getItem(MAP_CITY_KEY));
        var grid = $("#google-map-grid-slider").val();
        var source = $("#google-map-venue-source").val();
        var categories = $("#google-map-venue-category").val();
        var params = jQuery.param({cityId: city.id, grid: grid, source: source.toUpperCase(), categories: categories.join(',')});

        $.get("http://localhost:8080" + "/venues/locations/scale?" + params, function (markers) {
            markers.forEach(function (marker) {
                new google.maps.Circle({
                    strokeColor: marker.color,
                    strokeOpacity: 0.8,
                    strokeWeight: 0.1,
                    fillColor: 'blue',
                    fillOpacity: 0.8,
                    map: MAP_GOOGLE,
                    center: {lat: marker.latitude, lng: marker.longitude},
                    radius: 20
                });
            })
        });

        // $.get("http://localhost:8080" + "/boundingboxes/grid/heat/map?" + params, function (colors) {
        //     $("#map-clear-button").trigger("dbclick");
        //     jQuery.each(colors, function (i, color) {
        //         if (color !== null) {
        //             griBoundingBoxesColored.push(googleRectangleColored(griBoundingBoxes[i], color))
        //         }
        //     });
        // });
    })
}
