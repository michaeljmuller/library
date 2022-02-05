
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

// https://developer.mozilla.org/en-US/docs/Learn/Forms/Sending_forms_through_JavaScript
// https://stackoverflow.com/questions/1255948/post-data-in-json-format
// https://stackoverflow.com/questions/41431322/how-to-convert-formdata-html5-object-to-json
function convertFormToAjax(formId, csrfParameterName, csrfToken, mvProperties, responseHandler) {
    let form = document.getElementById(formId);

    form.onsubmit = function(e) {

        // stop the regular form submission
        e.preventDefault();

        const xhr = new XMLHttpRequest();

        // Bind the FormData object and the form element
        const formData = new FormData(form);

        // Define what happens on successful data submission
        /*
        xhr.addEventListener( "load", function(event) {
            alert( event.target.responseText );
        } );
         */
        xhr.addEventListener( "load", responseHandler);

        // Define what happens in case of error
        xhr.addEventListener( "error", function( event ) {
            // note: not sure when this gets called (server response 500 & 404 go to "load" listener above)
            alert('Unexpected: XHR error event fired');
        } );

        // Set up our request
        xhr.open(form.method, form.action);

        // prevent Spring Boot from rejecting us for
        xhr.setRequestHeader(csrfParameterName, csrfToken);

        // let Spring Book know we're sending JSON
        xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8");

        // convert the form to JSON
        var object = {};
        formData.forEach((value, key) => {

            console.log("key: " + key + " value: " + value);

            // if the property is a multi-value property
            if (mvProperties && mvProperties.includes(key)) {

                // create an array if one hasn't already been created
                if (!Reflect.has(object, key)) {
                    object[key] = [];
                }

                // add this value to the list of values
                object[key].push(value);
            }

            // if the property is NOT multi-value, just assign the value
            else {
                object[key] = value;
            }
        });

        var json = JSON.stringify(object);

        // The data sent is what the user provided in the form
        xhr.send(json);
    }
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
        e.removeChild(child);
        child = parent.lastElementChild;
    }
}
