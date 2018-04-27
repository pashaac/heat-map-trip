
var gridBoundingBoxes = [];
var gridHeatMapBoundingBoxes = [];
var invalidBoundingBoxes = [];
var invalidBoundingBoxesXXX = [];

function googleMapGridSliderInitialization() {
    $("#google-map-grid-slider").change(function () {
        gridBoundingBoxes = clearMapCollection(gridBoundingBoxes);
        if (this.value === 0) {
            return;
        }
        this.disabled = true;
        var city = JSON.parse(sessionStorage.getItem(MAP_CITY_KEY));
        var params = jQuery.param({cityId: city.id, grid: this.value});
        var slider = this;
        $.get("http://localhost:8080" + "/boundingboxes/grid?" + params, function (boundingBoxes) {
            boundingBoxes.forEach(function (boundingBox) {
                gridBoundingBoxes.push(googleRectangle(boundingBox, 'black'));
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
    gridBoundingBoxes = clearMapCollection(gridBoundingBoxes);
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
        var params = jQuery.param({cityId: city.id, grid: grid});

        var venuesIds = venues.map(function (venue) { return venue.id;  });
        $.put("/boundingboxes/grid/heat/map?" + params, JSON.stringify(venuesIds), function (clusterBoundingBoxes) {
            jQuery.each(clusterBoundingBoxes, function (i, clusterBoundingBox) {
                if (clusterBoundingBox.color !== '#808080') {
                    gridHeatMapBoundingBoxes.push(googleRectangleColored(gridBoundingBoxes[clusterBoundingBox.id], clusterBoundingBox.color));
                }
            });
        });

        // $.get("http://localhost:8080" + "/boundingboxes/grid/heat/map?" + params, function (colors) {
        //     $("#map-clear-button").trigger("dbclick");
        //     jQuery.each(colors, function (i, color) {
        //         if (color !== null) {
        //             griBoundingBoxesColored.push(googleRectangleColored(gridBoundingBoxes[i], color))
        //         }
        //     });
        // });
    })
}
