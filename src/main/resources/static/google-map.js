
var MAP_HEAT_MAP;
var MAP_GOOGLE;
var MAP_CITY_KEY = 'google-map-city';

// Google Map + Heat-Map framework
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

// Heat Map slider
function googleMapHeatMapSliderInitialization() {
    $("#google-map-heat-map-slider").change(function () {
        if (this.value === 0 && venues.length === 0) {
            $(this).prop("disabled", true);
            MAP_HEAT_MAP.data = [];
            MAP_HEAT_MAP.update();
            return;
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
        $("#google-map-grid-slider").val(0).change();   // clear grid
        clearVenueMarkers();                            // clear markers on map
        clearAnyBoundingBoxes();                        // clear all bounding boxes
        venues = [];                                    // clear venues in memory
        // should be empty map after clear, only heat-map can be!
        if (MAP_HEAT_MAP.data.length === 0) {
            $("#google-map-heat-map-slider").prop("disabled", true);
        }
    });
    $mapClearButton.dblclick(function () {
        $mapClearButton.trigger("click");               // clear all
        $("#google-map-heat-map-slider").val(0).prop("disabled", true); // clear heat-map
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

