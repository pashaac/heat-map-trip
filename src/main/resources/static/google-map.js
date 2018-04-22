var googleMapCity;
var MAP_HEAT_MAP;

var MAP_GOOGLE;
var MAP_CITY_KEY = 'google-map-city';

var griBoundingBoxes = [];
var failCityBoundingBoxes = [];
var failCityBoundingBoxesX = [];
var succCityBoundingBoxes = [];
var venueMarkers = [];
var venues = [];

function url(url) {
    return 'http://localhost:8080' + url;
}

// Google Map
function googleMapInitialization() {
    var mapOptions = {
        center: {lat: 59.957570, lng: 30.307946}, // ITMO University
        zoom: 3,
        scaleControl: true,
        language: 'ru'
    };
    MAP_GOOGLE = new google.maps.Map($("#google-map")[0], mapOptions);

    MAP_HEAT_MAP = new HeatmapOverlay(MAP_GOOGLE, {
        "radius": 0.005,
        "maxOpacity": 0.8,
        "scaleRadius": true,
        "useLocalExtrema": false,
        latField: 'latitude',
        lngField: 'longitude',
        valueField: 'rating'
    });
}

// Search box (cities search panel)
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

// Venue source dropdown: FOURSQUARE / GOOGLE
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

// Heat-Map service categories from server
function googleMapCategoriesInitialization() {
    $.get("http://localhost:8080" + "/categories", function (categories) {
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

// Grid count slider
function googleMapGridSliderInitialization() {
    $("#google-map-grid-slider").change(function () {
        this.disabled = true;
        clearGridBoundingBoxes();
        if (this.value === 0) {
            this.disabled = false;
            return;
        }
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
        });
    });
}

// Heat Map count slider
function googleMapHeatMapSliderInitialization() {
    $("#google-map-heat-map-grid-slider").change(function () {
        if (this.value === 0 && venues.length === 0) {
            $(this).prop("disabled", true);
            MAP_HEAT_MAP.data = [];
            MAP_HEAT_MAP.update();
            return
        }
        if (this.value === 0) {
            MAP_HEAT_MAP.data = [];
            MAP_HEAT_MAP.update();
            return;
        }

        if (MAP_HEAT_MAP.data.length === 0) {
            var venuesMax = 0;
            var heatMapData = $.map(venues, function (venue) {
                venuesMax = Math.max(venuesMax, venue.rating);
                return {latitude: venue.location.latitude, longitude: venue.location.longitude, rating: venue.rating}
            });
            MAP_HEAT_MAP.setData({data: heatMapData, max: venuesMax});
        }

        MAP_HEAT_MAP.cfg.radius = this.value / 4000;
        MAP_HEAT_MAP.update();
    });
}

// Clear button
function googleMapClearInitialization() {
    var $mapClearButton = $("#map-clear-button");
    $mapClearButton.click(function () {
        $("#google-map-grid-slider").val(0).change();
        clearVenueMarkers();
        clearFailCityBoundingBoxes();
        clearSuccCityBoundingBoxes();
        venues = [];
    });
    $mapClearButton.dblclick(function () {
        $mapClearButton.trigger("click");
        $("#google-map-heat-map-grid-slider").val(0).prop("disabled", true);
        MAP_HEAT_MAP.data = [];
        MAP_HEAT_MAP.update();
    })
}

function isValidEnvironment() {
    var valid = true;
    if (!sessionStorage.getItem(MAP_CITY_KEY)) {
        $("#google-map-city-search-box").prop("invalid", true);
        valid = false;
    }
    var $googleMapVenueSource = $("#google-map-venue-source");
    if (!$googleMapVenueSource.val()) {
        $googleMapVenueSource.prop("invalid", true);
        valid = false;
    }
    var $googleMapVenueCategory = $("#google-map-venue-category");
    if (!$googleMapVenueCategory.val().length) {
        $googleMapVenueCategory.prop("invalid", true);
        valid = false;
    }
    return valid;
}

function googleMapShowVenuesButtonInitialization() {
    $("#map-show-venues-button").click(function () {
        if (!isValidEnvironment()) {
            return;
        }
        var city = JSON.parse(sessionStorage.getItem(MAP_CITY_KEY));
        var categories = $("#google-map-venue-category").val();
        var source = $("#google-map-venue-source").val();
        var params = jQuery.param({cityId: city.id, source: source.toUpperCase(), categories: categories.join(',')});

        $.put("/venues/quad/tree/collect?" + params, undefined, function (_venues) {
            _venues.forEach(function (venue) {
                venueMarkers.push(googleMarker(venue));
            });
            venues = venues.concat(_venues);
            $("#google-map-heat-map-grid-slider").prop("disabled", false);
        }, function () {
            console.error("Heat-map venues mine service temporary unavailable...");
            alert("Heat-map venues mine service temporary unavailable...\nRepeat your last act after some pause or contact with developer");
        });

        $.get("http://localhost:8080" + "/boundingboxes/invalid?" + params, function (boundingBoxes) {
            boundingBoxes.forEach(function (boundingBox) {
                failCityBoundingBoxes.push(googleRectangle(boundingBox, 'red'));
                failCityBoundingBoxesX = failCityBoundingBoxesX.concat(googleRectangleX(boundingBox, 'red'))
            })
        });
    })
}


