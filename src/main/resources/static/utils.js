function clearBoundingBoxes() {
    boundingBoxes.forEach(function (boundingBox) {
        boundingBox.setMap(null);
    });
    boundingBoxes = [];
}

function clearMarkers() {
    markers.forEach(function (marker) {
        marker.setMap(null);
    });
    markers = [];
}

var googleRectangle = function (boundingBox) {
    return new google.maps.Rectangle({
        strokeColor: '#000000',
        strokeOpacity: 0.75,
        strokeWeight: 1,
        fillOpacity: 0.1,
        map: map,
        bounds: {
            north: boundingBox.northEast.latitude,
            south: boundingBox.southWest.latitude,
            east: boundingBox.northEast.longitude,
            west: boundingBox.southWest.longitude
        }
    });
};
