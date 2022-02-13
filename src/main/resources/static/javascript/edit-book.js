
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
        url: '/api/book/cover/' + bookId,
        method: 'post',
        chunking: true,
        init: function() {
            this.on("sending", function (file, xhr, formData) {

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
    inputRadio.setAttribute("name", "coverImage");
    inputRadio.setAttribute("id", "image" + nextImageNum);
    inputRadio.setAttribute("value", file.name);
    labelAroundText.setAttribute("for", "image" + nextImageNum);
    labelAroundText.appendChild(document.createTextNode(file.name));

    // select the radio button we just created
    inputRadio.checked = true;
}

function displayErrors(errors, errorListElementId) {
    let errorListElement = document.getElementById(errorListElementId);
    removeAllChildren(errorListElement);

    for (const error of errors) {
        let listItemElement = document.createElement("li");
        listItemElement.appendChild(document.createTextNode(error));
        errorListElement.appendChild(listItemElement);
    }

    // scroll to the top of the page -- https://stackoverflow.com/a/4210821
    document.body.scrollTop = document.documentElement.scrollTop = 0;
}

function removeAllChildren(parent) {
    var child = parent.lastElementChild;
    while (child) {
        parent.removeChild(child);
        child = parent.lastElementChild;
    }
}
