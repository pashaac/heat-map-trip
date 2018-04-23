jQuery.each(["put", "delete"], function (i, method) {
    jQuery[method] = function (url, data, callback, error) {
        return jQuery.ajax({
            url: "http://localhost:8080" + url,
            type: method,
            contentType: "application/json",
            dataType: "json",
            data: data,
            success: callback,
            error: error
        });
    };
});

function clearGridBoundingBoxes() {
    griBoundingBoxes.forEach(function (boundingBox) {
        boundingBox.setMap(null);
    });
    griBoundingBoxes = [];
}

function clearGridBoundingBoxesColored() {
    griBoundingBoxesColored.forEach(function (boundingBox) {
        boundingBox.setMap(null);
    });
    griBoundingBoxesColored = [];
}

function clearFailCityBoundingBoxes() {
    failCityBoundingBoxes.forEach(function (boundingBox) {
        boundingBox.setMap(null);
    });
    failCityBoundingBoxes = [];
    failCityBoundingBoxesX.forEach(function (polyline) {
       polyline.setMap(null);
    });
    failCityBoundingBoxesX = [];
}

function clearSuccCityBoundingBoxes() {
    succCityBoundingBoxes.forEach(function (boundingBox) {
        boundingBox.setMap(null);
    });
    succCityBoundingBoxes = [];
}

function clearVenueMarkers() {
    venueMarkers.forEach(function (marker) {
        marker.setMap(null);
    });
    venueMarkers = [];
}

var googleRectangle = function (boundingBox, color) {
    return new google.maps.Rectangle({
        strokeColor: color,
        strokeOpacity: 1,
        strokeWeight: 2,
        fillOpacity: 0.05,
        map: MAP_GOOGLE,
        bounds: {
            north: boundingBox.northEast.latitude,
            south: boundingBox.southWest.latitude,
            east: boundingBox.northEast.longitude,
            west: boundingBox.southWest.longitude
        }
    });
};

var googleRectangleColored = function (boundingBox, color) {
    return new google.maps.Rectangle({
        strokeWeight: 0,
        fillOpacity: 0.9,
        fillColor: color,
        map: MAP_GOOGLE,
        bounds: boundingBox.bounds
    });
};

var googleRectangleX = function (boundingBox, color) {
    var oneLine = new google.maps.Polyline({
        path: [
            {lat: boundingBox.southWest.latitude, lng: boundingBox.southWest.longitude},
            {lat: boundingBox.northEast.latitude, lng: boundingBox.northEast.longitude},
        ],
        geodesic: true,
        strokeColor: color,
        strokeOpacity: 1,
        strokeWeight: 2 ,
        fillOpacity: 0.05,
        map: MAP_GOOGLE
    });

    var twoLine = new google.maps.Polyline({
        path: [
            {lat: boundingBox.southWest.latitude, lng: boundingBox.northEast.longitude},
            {lat: boundingBox.northEast.latitude, lng: boundingBox.southWest.longitude},
        ],
        geodesic: true,
        strokeColor: color,
        strokeOpacity: 1,
        strokeWeight: 2 ,
        fillOpacity: 0.05,
        map: MAP_GOOGLE
    });
    return [oneLine, twoLine];
};

var googleMarker = function (venue) {
    return new google.maps.Marker({
        position: {lat: venue.location.latitude, lng: venue.location.longitude},
        map: MAP_GOOGLE,
        animation: google.maps.Animation.DROP,
        title: venue.title + ' (rating: ' + venue.rating + ')'
    })
};

