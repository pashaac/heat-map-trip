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
        var categoriesStr = this.value.sort().join(' | ');
        if (!categoriesStr) {
            return;
        }
        $(this.$).prop('dropdownMenu').value = categoriesStr;
        this.invalid = false;
    })
}
