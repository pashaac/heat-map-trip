var animationTimeout = 1000;

function animationMining() {
    var city = JSON.parse(sessionStorage.getItem(MAP_CITY_KEY));
    animationCallSlowMotion(city.boundingBox);
}

function animationCall(box) {
    console.log(server + 'api/call/details');
    var googleBox = googleRectangle(box, 'orange');
    jQuery.ajax({
        type: "PUT",
        dataType: "json",
        contentType: 'application/json',
        url: server + 'venue/api/call/details?source=GOOGLE',
        data: JSON.stringify(box),
        cache: false,
        success: function (apiBox) {
            var city = JSON.parse(sessionStorage.getItem(MAP_CITY_KEY));
            console.log('API call inside city: ' + city.city + ' return ' + apiBox.venues.length + ' venues');
            var googleMarkers = [];
            for (i = 0; i < apiBox.venues.length; i++) {
                googleMarkers.push(googleMarker(apiBox.venues[i]));
            }
            if (!apiBox.rateTheLimit) {
                googleBox.setMap(null);
                googleBox = googleRectangle(box, 'green');
                return;
            }
            googleBox.setMap(null);
            googleBox = googleRectangle(box, 'red');
            for (i = 0; i < googleMarkers.length; i++) {
                googleMarkers[i].setMap(null);
            }
            googleBox.setMap(null);
            var googleBoxQuarters = [];
            for (i = 0; i < apiBox.boundingBoxQuarters.length; i++) {
                googleBoxQuarters.push(googleRectangle(apiBox.boundingBoxQuarters[i], 'red'));
            }
            for (i = 0; i < apiBox.boundingBoxQuarters.length; i++) {
                googleBoxQuarters[i].setMap(null);
                animationCall(apiBox.boundingBoxQuarters[i]);
            }
        }
    });
}


function animationCallSlowMotion(box) {
    console.log(server + 'api/call/details');
    var googleBox = googleRectangle(box, 'orange');
    jQuery.ajax({
        type: "PUT",
        dataType: "json",
        contentType: 'application/json',
        url: server + 'venue/api/call/details',
        data: JSON.stringify(box),
        cache: false,
        success: function (apiBox) {
            var city = JSON.parse(sessionStorage.getItem(MAP_CITY_KEY));
            console.log('API call inside city: ' + city.city + ' return ' + apiBox.venues.length + ' venues');
            setTimeout(function () {
                var googleMarkers = [];
                for (i = 0; i < apiBox.venues.length; i++) {
                    googleMarkers.push(googleMarker(apiBox.venues[i]));
                }
                setTimeout(function () {
                    if (!apiBox.rateTheLimit) {
                        googleBox.setMap(null);
                        googleBox = googleRectangle(box, 'green');
                        return;
                    }
                    googleBox.setMap(null);
                    googleBox = googleRectangle(box, 'red');
                    for (i = 0; i < googleMarkers.length; i++) {
                        googleMarkers[i].setMap(null);
                    }
                    setTimeout(function () {
                        googleBox.setMap(null);
                        var googleBoxQuarters = [];
                        for (i = 0; i < apiBox.boundingBoxQuarters.length; i++) {
                            googleBoxQuarters.push(googleRectangle(apiBox.boundingBoxQuarters[i], 'red'));
                        }
                        setTimeout(function () {
                            for (i = 0; i < apiBox.boundingBoxQuarters.length; i++) {
                                googleBoxQuarters[i].setMap(null);
                                animationCallSlowMotion(apiBox.boundingBoxQuarters[i]);
                            }
                        }, animationTimeout);
                    }, animationTimeout);
                }, animationTimeout);
            }, animationTimeout);
        }
    });

}
