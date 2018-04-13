jQuery.each(["put", "delete"], function (i, method) {
    jQuery[method] = function (url, data, callback, error) {
        return jQuery.ajax({
            url: "http://localhost:8080" + url,
            type: method,
            dataType: "json",
            data: data,
            async: false,
            success: callback,
            error: error
        });
    };
});

function clearGridGoogleBoundingBoxes() {
    griGoogleBoundingBoxes.forEach(function (boundingBox) {
        boundingBox.setMap(null);
    });
    griGoogleBoundingBoxes = [];
}

function clearGoogleMarkers() {
    googleMarkers.forEach(function (marker) {
        marker.setMap(null);
    });
    googleMarkers = [];
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
        title: venue.title
    })
};

