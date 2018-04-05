function dirty(source, categories, box, callback) {
    console.log(url('/venue/api/call/dirty'));
    jQuery.ajax({
        type: "PUT",
        dataType: "json",
        contentType: 'application/json',
        url: url('/venue/api/call/dirty?') + jQuery.param({source: source.toUpperCase(), categories: categories.join(',')}),
        data: JSON.stringify(box),
        cache: false,
        success: callback
    });
}

function valid(source, categories, box, callback) {
    console.log(url('/venue/api/call/valid'));
    jQuery.ajax({
        type: "PUT",
        dataType: "json",
        contentType: 'application/json',
        url: url('/venue/api/call/valid?') + jQuery.param({source: source.toUpperCase(), categories: JSON.stringify(categories)}),
        data: JSON.stringify(box),
        cache: false,
        success: callback
    });
}