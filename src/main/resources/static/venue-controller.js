function venueApiMine(source, categories, box, callback) {
    console.log(url('/venue/api/call/dirty'));
    jQuery.ajax({
        type: "PUT",
        dataType: "json",
        contentType: 'application/json',
        url: url('/venue/api/mine?') + jQuery.param({source: source.toUpperCase(), categories: categories.join(',')}),
        data: JSON.stringify(box),
        cache: false,
        success: callback
    });
}

function venueCategories(callback) {
    console.log(url('/venue/categories'));
    jQuery.ajax({
        type: "GET",
        dataType: "json",
        contentType: 'application/json',
        url: url('/venue/categories'),
        cache: false,
        success: callback
    });
}

function geolocationCityGrid(grid, box, callback) {
    console.log(url('/geolocation/grid'));
    jQuery.ajax({
        type: "PUT",
        dataType: "json",
        contentType: 'application/json',
        url: url('/geolocation/grid?') + jQuery.param({grid: grid}),
        data: JSON.stringify(box),
        cache: false,
        success: callback
    });
}