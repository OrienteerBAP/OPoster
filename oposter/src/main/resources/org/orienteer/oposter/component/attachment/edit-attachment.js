
function initEditAttachments(wrapperId, uploadId, labelId) {
    var $uploadField = $('#' + uploadId);
    var lock = false;

    $('#' + wrapperId).click(function (e) {
        if (!lock) {
            lock = true;
            $uploadField.click();
            lock = false;
        }
    });

    $('#' + wrapperId).change(function (e) {
        var files = e.target.files;

        if (files && files.length > 0) {
            var title = files[0].name;

            if (files.length > 1) {
                title += ', ...';
            }

            $('#' + labelId).html(title);
        }
    });
}