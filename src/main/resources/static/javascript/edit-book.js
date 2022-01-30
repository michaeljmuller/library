
function initializeAutocomplete(elementId, data) {
    let ac = new autoComplete({
        selector: elementId,
        data: {
            src: data,
        },
        events: {
            input: {
                selection: (event) => {
                    ac.input.value = event.detail.selection.value;
                }
            }
        },
    });
}

function initializeDropzone(bookId, csrfParameterName, csrfToken) {

    // create the dropzone
    let myDropzone = new Dropzone("div#dz", {
        url: '/uploadCover',
        method: 'post',
        chunking: true,
        init: function() {
            this.on("sending", function (file, xhr, formData) {

                // include the book id with form post
                formData.append("bookId", bookId);

                // include CSRF token (if provided)
                if (csrfParameterName != null && csrfToken != null) {
                    formData.append(csrfParameterName, csrfToken);
                }
            });
        },
    });

    // establish post-upload behaviors
    myDropzone.on("complete", function(file) {

        // 3 seconds after the upload completes...
        setTimeout(function () {

            // remove the preview image from the drop zone
            myDropzone.removeFile(file);

            // add the uploaded cover image to the list of options on the page
            insertNewCover(bookId, file);

        }, 3000);

    });

}

function insertNewCover(bookId, file) {

    // this is the div into which we will be adding a new item
    let divCoverImages = document.getElementById("coverImages");

    // count the existing items to determine the next number to use in the new item's ID
    let nextImageNum = divCoverImages.children.length;

    // create the elements that will be part of the HTML we're adding
    let divCoverImageWrapper = document.createElement("div");
    let divCoverImage = document.createElement("div");
    let labelAroundImg = document.createElement("label");
    let img = document.createElement("img");
    let divCoverImageLabel = document.createElement("div");
    let inputRadio = document.createElement("input");
    let labelAroundText = document.createElement("label");

    // organize the elements
    divCoverImages.insertBefore(divCoverImageWrapper, document.getElementById("dz-wrapper"));
    divCoverImageWrapper.appendChild(divCoverImage);
    divCoverImage.appendChild(labelAroundImg);
    labelAroundImg.appendChild(img);
    divCoverImageWrapper.appendChild(divCoverImageLabel);
    divCoverImageLabel.appendChild(inputRadio);
    divCoverImageLabel.appendChild(labelAroundText);

    // set the attributes (and inner text) for each of the elements
    divCoverImageWrapper.setAttribute("class", "coverImageWrapper");
    divCoverImage.setAttribute("class", "coverImage");
    labelAroundImg.setAttribute("for", "image" + nextImageNum);
    img.setAttribute("src", "/uploadedImage?bookId=" + bookId + "&file=" + encodeURIComponent(file.name));
    divCoverImageLabel.setAttribute("class", "coverImageLabel");
    inputRadio.setAttribute("type", "radio");
    inputRadio.setAttribute("name", "selectedCoverImg");
    inputRadio.setAttribute("id", "image" + nextImageNum);
    labelAroundText.setAttribute("for", "image" + nextImageNum);
    labelAroundText.appendChild(document.createTextNode(file.name));

    // select the radio button we just created
    inputRadio.checked = true;
}
