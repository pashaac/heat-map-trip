var map;

var boundingBoxes = [];
var markers = [];

var server = 'http://localhost:8080/';

function mapInitialization() {
    var mapOptions = {
        center: {lat: 59.957570, lng: 30.307946},
        zoom: 3,
        language: 'ru'
    };
    map = new google.maps.Map(document.getElementById('google-map'), mapOptions);
    geolocationReverseBrowser();

    // heatmap = new HeatmapOverlay(map,
    //     {
    //         "radius": 0.01,
    //         "maxOpacity": 0.7,
    //         "scaleRadius": true,
    //         "useLocalExtrema": true,
    //         latField: 'lat',
    //         lngField: 'lng',
    //         valueField: 'rating'
    //     }
    // );
    //
    //
    // jQuery.ajax({
    //     type: "GET",
    //     dataType: "json",
    //     url: "http://localhost:8080/venue/api/call?latitude=59.957570&longitude=30.307946&radius=2000&source=GOOGLE",
    //     success: function (venues) {
    //         var testData = {max: 0, data: []};
    //         venues.forEach(function (venue) {
    //             console.log(venue);
    //             testData.data.push({
    //                 lat: venue.location.latitude,
    //                 lng: venue.location.longitude,
    //                 rating: venue.rating
    //             });
    //             // count: parseInt(parseFloat(venue.rating) * 10, 10)                 });
    //         });
    //         console.log(testData);
    //         heatmap.setData(testData);
    //     }
    // })
}