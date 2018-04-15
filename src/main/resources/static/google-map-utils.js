jQuery.each(["put", "delete"], function (i, method) {
    jQuery[method] = function (url, data, callback, error) {
        return jQuery.ajax({
            url: "http://localhost:8080" + url,
            type: method,
            contentType: "application/json",
            dataType: "json",
            data: data,
            async: false,
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

var googleMarker = function (venue) {
    return new google.maps.Marker({
        position: {lat: venue.location.latitude, lng: venue.location.longitude},
        map: MAP_GOOGLE,
        animation: google.maps.Animation.DROP,
        title: venue.title + ' (rating: ' + venue.rating + ')'
    })
};

