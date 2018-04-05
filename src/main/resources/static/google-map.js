var googleMap;
var googleMapCity;

// var googleBoundingBoxes = [];
// var googleMarkers = [];

function url(url) {
    return 'http://localhost:8080' + url;
}

function getMapBoundingBox() {
    return googleMapCity.boundingBox;
}

function getGoogleMap() {
    return googleMap;
}

function getGoogleMapCity() {
    return googleMapCity;
}

function setGoogleMapCity(city) {
    googleMapCity = city;
}

function getGoogleSearchBox() {
    return document.getElementById("google-search-box");
}

function getSource() {
    return document.getElementById("data-source-dropdown").value;
}

function getCategories() {
    return document.getElementById("category-source-dropdown").value;
}

function mapInitialization() {
    var mapOptions = {
        center: {lat: 59.957570, lng: 30.307946},
        zoom: 3,
        language: 'ru'
    };
    googleMap = new google.maps.Map(document.getElementById('google-map'), mapOptions);
    googleSearchBoxInitialization();


    // document.getElementById('animation-show').addEventListener('click', function (event) {
    //     animationMining();
    // });

    document.getElementById('venue-call').addEventListener('click', function (event) {
        dirty(getSource(), getCategories(), getMapBoundingBox(), function (venues) {
            venues.forEach(function (venue) {
                googleMarker(venue);
            })
        });
    });

    document.getElementById('data-source-dropdown').addEventListener('iron-select', function (event) {
        this.invalid = false;
    })


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

function googleSearchBoxInitialization() {
    var googleSearchBox = getGoogleSearchBox();
    var autocomplete = new google.maps.places.Autocomplete(googleSearchBox, {types: ['(cities)']});
    autocomplete.bindTo('bounds', googleMap);
    autocomplete.addListener('place_changed', function () {
        var place = autocomplete.getPlace();
        if (place.geometry) {
            var location = place.geometry.location;
            geolocationReverse({lat: location.lat(), lng: location.lng()});
        } else {
            googleSearchBox.invalid = true;
        }
    });
}

