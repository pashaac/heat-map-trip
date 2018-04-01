var googleSearchBox;

function mapContentInitialization() {
    googleSearchBox = document.getElementById('google-search-box');
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

    document.getElementById('category-source-dropdown').addEventListener('selected-item-changed', function (event) {
        var checkedCategories = [];
        ['checkbox-art', 'checkbox-nature', 'checkbox-shrine', 'checkbox-sights'].forEach(function (id) {
            var checkBox = document.getElementById(id);
            if (checkBox.checked) {
                checkedCategories.push(checkBox.textContent);
            }
        });
        this.value = checkedCategories;
    });

    document.getElementById('animation-show').addEventListener('click', function (event) {
        animationMining();
    });
}
