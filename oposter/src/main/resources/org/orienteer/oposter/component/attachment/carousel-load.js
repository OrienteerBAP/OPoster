

function initLoad(carouselId) {
    var $carousel = $('#' + carouselId);
    var $loadImage = $('#' + carouselId + ' > .carousel-inner > .carousel-item > img').first();

    if ($loadImage.length > 0) {

        var $indicator = $('<i class="fa fa-spinner fa-spin fa-3x fa-fw d-block mx-auto"></i>');

        $carousel.hide();
        $carousel.before($indicator);

        $loadImage.on('load', function () {
            $indicator.fadeOut('slow', function () {
                $indicator.remove();
                $carousel.show('slow');
            });
        });
    }
}