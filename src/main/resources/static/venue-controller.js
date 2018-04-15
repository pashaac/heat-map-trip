function venueCityMine(source, categories, city, callback) {
    console.log(url('/venue/city/mine'));
    jQuery.ajax({
        type: "PUT",
        dataType: "json",
        contentType: 'application/json',
        url: url('/venue/city/mine?') + jQuery.param({source: source.toUpperCase(), categories: categories.join(',')}),
        data: JSON.stringify(city),
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
