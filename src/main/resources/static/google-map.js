var googleMap;
var googleMapCity;

var griGoogleBoundingBoxes = [];
// var googleMarkers = [];

function url(url) {
    return 'http://localhost:8080' + url;
}

function googleMapInitialization() {
    var mapOptions = {
        center: {lat: 59.957570, lng: 30.307946}, // ITMO University
        zoom: 3,
        scaleControl: true,
        language: 'ru'
    };
    googleMap = new google.maps.Map(document.getElementById('google-map'), mapOptions);
}

function googleSearchBoxInitialization() {
    var googleSearchBox = document.getElementById("google-city-search-box");
    googleSearchBox.addEventListener('change', function (event) {
        googleMapCity = undefined;
        googleSearchBox.invalid = true;
    });
    var autocomplete = new google.maps.places.Autocomplete(googleSearchBox, {types: ['(cities)']});
    autocomplete.bindTo('bounds', googleMap);
    autocomplete.addListener('place_changed', function () {
        var place = autocomplete.getPlace();
        if (place.geometry) {
            var location = place.geometry.location;
            googleSearchBox.value = place.formatted_address;
            googleSearchBox.invalid = false;
            geolocationReverse({lat: location.lat(), lng: location.lng()}, function (city) {
                console.log('geolocation reverse city: ' + JSON.stringify(city));
                var southwest = {lat: city.boundingBox.southWest.latitude, lng: city.boundingBox.southWest.longitude};
                var northeast = {lat: city.boundingBox.northEast.latitude, lng: city.boundingBox.northEast.longitude};
                googleMap.fitBounds(new google.maps.LatLngBounds(southwest, northeast));
                googleMapCity = city;
            }, function () {
                console.error("Heat-Map geolocation service temporary unavailable...");
                alert("Heat-Map geolocation service temporary unavailable...");
                googleMapCity = undefined;
            });
        } else {
            console.error("Google Map geolocation service temporary unavailable or entered error place...");
            alert("Google Map geolocation service temporary unavailable or entered error place...");
            googleMapCity = undefined;
            googleSearchBox.invalid = true;
        }
    });
}

function googleDataSourceInitialization() {
    document.getElementById('data-source-dropdown').addEventListener('close', function (event) {
        if (this.invalid) {
            var flag = true;
            this.$.list.items.forEach(function (item) {
              if (item.getAttribute('focused') != null) {
                   flag = false;
              }
            });
            this.invalid = flag;
        }
    });
}

function googleCategoriesInitialization() {
    venueCategories(function (categories) {
        var domBindCategories = document.getElementById('dom-categories');
        var items = [];
        categories.forEach(function (category) {
            items.push({value: category});
        });
        domBindCategories.items = items;
    });
    document.getElementById('categories-dropdown').addEventListener('close', function (event) {
        var dropdownValue = this.value.join(', ');
        if (this.invalid && dropdownValue != null && dropdownValue.length !== 0) {
            this.invalid = false;
        }
        this.$.dropdownMenu.value = dropdownValue;
    });
}

function googleGriSliderInitialization() {
    document.getElementById('grid-slider').addEventListener('change', function (event) {
        this.disabled = true;
        clearGridGoogleBoundingBoxes();
        if (event.target.value === 0) {
            this.disabled = false;
            return;
        }
        geolocationCityGrid(event.target.value, googleMapCity.boundingBox, function (boundingBoxes) {
            boundingBoxes.forEach(function (boundingBox) {
                griGoogleBoundingBoxes.push(googleRectangle(boundingBox, 'black'));
            });
            document.getElementById('grid-slider').disabled = false;
        });
    })
}

function mapComponentsInitialization() {
    googleMapInitialization();
    googleSearchBoxInitialization();
    googleDataSourceInitialization();
    googleCategoriesInitialization();
    googleGriSliderInitialization();
    // // Categories dropdown
    // document.getElementById("category-source-dropdown").addEventListener('on-paper-dropdown-close', function (event) {
    //     console.log(event);
    //     if (this._value.length === 0) {
    //         this._value = undefined;
    //     }
    // });
    // categoryAll(function (categories) {
    //     document.getElementById("category-source-dropdown").selections = categories;
    // });


    document.getElementById('test-button').addEventListener('click', function (event) {
        console.log('Click test button');
        if (googleMapCity == null) {
            var googleSearchBox = document.getElementById("google-city-search-box");
            googleSearchBox.invalid = true;
        }
        var dataSource = document.getElementById("data-source-dropdown");
        if (dataSource.value == null) {
            dataSource.invalid = true;
        }
        var categories = document.getElementById('categories-dropdown');
        if (categories.value == null || categories.value.length === 0) {
            categories.invalid = true;
        }
    });

    // document.getElementById('venue-call').addEventListener('click', function (event) {
    //     venueApiMine(getDataSourceValue(), getSelectedCategoriesArray(), getGoogleMapCityBoundingBox(), function (venueBox) {
    //         venueBox.validVenues.forEach(function (venue) {
    //             googleMarker(venue);
    //         })
    //     });
    // });
    //
    // document.getElementById('data-source-dropdown').addEventListener('iron-select', function (event) {
    //     this.invalid = false;
    // })


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


function getDataSourceValue() {
    return document.getElementById("data-source-dropdown").value;
}

function getSelectedCategoriesArray() {
    return document.getElementById("category-source-dropdown").value;
}
