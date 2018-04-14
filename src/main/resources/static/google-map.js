var googleMapCity;

var MAP_GOOGLE;
var MAP_CITY_KEY = 'google-map-city';

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
    MAP_GOOGLE = new google.maps.Map($("#google-map")[0], mapOptions);
}

function googleMapSearchBoxInitialization() {
    var $googleMapCitySearchBox = $("#google-map-city-search-box");
    $googleMapCitySearchBox.change(function () {
        sessionStorage.removeItem(MAP_CITY_KEY);
        this.invalid = true;
    });
    var autocomplete = new google.maps.places.Autocomplete($googleMapCitySearchBox[0], {types: ['(cities)']});
    autocomplete.bindTo('bounds', MAP_GOOGLE);
    autocomplete.addListener('place_changed', function () {
        var place = autocomplete.getPlace();
        if (place.geometry) {
            $googleMapCitySearchBox.val(place.formatted_address);
            $googleMapCitySearchBox.prop("invalid", false);

            var params = $.param({lat: place.geometry.location.lat(), lng: place.geometry.location.lng()});
            console.log("search box user item geolocation coordinates: " + params);
            $.put("/geolocation/reverse?" + params, undefined, function (city) {
                var southwest = {lat: city.boundingBox.southWest.latitude, lng: city.boundingBox.southWest.longitude};
                var northeast = {lat: city.boundingBox.northEast.latitude, lng: city.boundingBox.northEast.longitude};
                MAP_GOOGLE.fitBounds(new google.maps.LatLngBounds(southwest, northeast));
                sessionStorage.setItem(MAP_CITY_KEY, JSON.stringify(city));
                console.log('City: \'' + city.city + '\' with id \'' + city.id + '\' was saved to session storage by key \'google-map-city\'');
                $("#google-map-city-search-box").val(city.city + ', ' + city.country);
            }, function () {
                console.warn("Heat-map geolocation service temporary unavailable...");
                alert("Heat-Map geolocation service temporary unavailable...\nRepeat your last act after some pause or contact with developer");
                sessionStorage.removeItem(MAP_CITY_KEY);
                $googleMapCitySearchBox.prop("invalid", true);
            });
        } else {
            console.error("Heat-map geolocation service temporary unavailable or used incorrect place...");
            alert("Heat-map geolocation service temporary unavailable or used incorrect place...\nRepeat your last act after some pause or contact with developer");
            sessionStorage.removeItem(MAP_CITY_KEY);
            $googleMapCitySearchBox.prop("invalid", true);
        }
    });
}

function googleMapVenueSourceInitialization() {
    $("#google-map-venue-source").bind("close", function () {
        if (!this.invalid) {
            return;
        }
        var googleMapVenueSource = this;
        $(this).children().each(function () {
            if ($(this).prop('focused')) {
                googleMapVenueSource.invalid = false;
                return false;
            }
        });
    });
}

function googleMapCategoriesInitialization() {
    $.get("http://localhost:8080" + "/venue/categories", function (categories) {
        $("#google-map-venue-category-dom").prop('items', $.map(categories, function (category) {
            return {value: category};
        }))
    });
    $("#google-map-venue-category").bind("close", function () {
        var categoriesStr = this.value.join(' | ');
        if (!categoriesStr) {
            return;
        }
        $(this.$).prop('dropdownMenu').value = categoriesStr;
        this.invalid = false;
    })
}



function googleMapGriSliderInitialization() {
    $("#grid-slider").change(function () {
        this.disabled = true;
        clearGridGoogleBoundingBoxes();
        if (event.target.value === 0) {
            this.disabled = false;
            return;
        }
        gridBoundingBoxController(event.target.value, googleMapCity.boundingBox, function (boundingBoxes) {
            boundingBoxes.forEach(function (boundingBox) {
                griGoogleBoundingBoxes.push(googleRectangle(boundingBox, 'black'));
            });
            document.getElementById('grid-slider').disabled = false;
        });
    });
    // document.getElementById('grid-slider').addEventListener('change', function (event) {
    //
    // })
}

function googleMapClearInitialization() {
    $("#map-clear-button").click(function () {
        $("#grid-slider").val(0).change();
    })
}

function googleMapValidateButtonInitialization() {
    $("#map-validate-button").click(function () {
        if (!sessionStorage.getItem(MAP_CITY_KEY)) {
            $("#google-map-city-search-box").prop("invalid", true);
        }
        var $googleMapVenueSource = $("#google-map-venue-source");
        if (!$googleMapVenueSource.val()) {
            $googleMapVenueSource.prop("invalid", true);
        }
        var $googleMapVenueCategory = $("#google-map-venue-category");
        if (!$googleMapVenueCategory.val().length) {
            $googleMapVenueCategory.prop("invalid", true);
        }
    });
    // document.getElementById('map-validate-button').addEventListener('click', function (event) {

    // var categories = document.getElementById('google-map-venue-category');
    // if (categories.value == null || categories.value.length === 0) {
    //     categories.invalid = true;
    // }

    // if (invalid  === false) {
    //     venueCityMine(getDataSourceValue(), getSelectedCategoriesArray(), googleMapCity, function (venues) {
    //         venues.forEach(function (venue) {
    //             googleMarker(venue);
    //         })
    //     })
    // }
    // });
}

function mapComponentsInitialization() {

    // googleMapGriSliderInitialization();
    // googleMapClearInitialization();

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

    // document.getElementById('venue-call').addEventListener('click', function (event) {
    //     venueApiMine(getDataSourceValue(), getSelectedCategoriesArray(), getGoogleMapCityBoundingBox(), function (venueBox) {
    //         venueBox.validVenues.forEach(function (venue) {
    //             googleMarker(venue);
    //         })
    //     });
    // });
    //
    // document.getElementById('google-map-venue-source').addEventListener('iron-select', function (event) {
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
