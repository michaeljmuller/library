
// https://developer.mozilla.org/en-US/docs/Learn/Forms/Sending_forms_through_JavaScript
// https://stackoverflow.com/questions/1255948/post-data-in-json-format
// https://stackoverflow.com/questions/41431322/how-to-convert-formdata-html5-object-to-json
function convertFormToAjax(form, csrfParameterName, csrfToken, mvProperties, responseHandler) {

    form.onsubmit = function(e) {

        // stop the regular form submission
        e.preventDefault();

        const xhr = new XMLHttpRequest();

        // Bind the FormData object and the form element
        const formData = new FormData(form);

        // Define what happens on successful data submission
        xhr.addEventListener( "load", responseHandler);

        // Define what happens in case of error
        /*
        xhr.addEventListener( "error", function( event ) {
            // note: not sure when this gets called (server response 500 & 404 go to "load" listener above)
            alert('Unexpected: XHR error event fired');
        } );
*/

        // Set up our request
        let method = form.getAttribute("data-ajax-method");
        xhr.open(method, form.action);

        // prevent Spring Boot from rejecting us for
        xhr.setRequestHeader(csrfParameterName, csrfToken);

        // let Spring Book know we're sending JSON
        xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8");

        // convert the form to JSON
        var object = {};
        formData.forEach((value, key) => {

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

function parseResponse(response) {

    let obj = JSON.parse(response);

    /*
    sometimes (auth failure, for example) spring boot will respond rather than the controller;
    if that's the case, we get an object like this:

    {"timestamp":"2022-02-05T18:38:45.766+00:00","status":403,"error":"Forbidden","path":"/api/book"}

    when that happens, we build an error message array from the message in that response object
     */
    if (isSpringBootResponse(obj)) {
        return ["Server responded with status " + obj.status + " and message: " + obj.error];
    }

    // otherwise, we just return the unmarshalled javascript array
    else {
        return obj;
    }
}

function isSpringBootResponse(response) {
    return response.hasOwnProperty("timestamp")
        && response.hasOwnProperty("status")
        && response.hasOwnProperty("error")
        && response.hasOwnProperty("path");
}

/*
// https://developer.mozilla.org/en-US/docs/Learn/Forms/Sending_forms_through_JavaScript
// https://stackoverflow.com/questions/1255948/post-data-in-json-format
// https://stackoverflow.com/questions/41431322/how-to-convert-formdata-html5-object-to-json
function convertFormToAjax(form, csrfParameterName, csrfToken, mvProperties, responseHandler) {

    form.onsubmit = function(e) {

        // stop the regular form submission
        e.preventDefault();

        const xhr = new XMLHttpRequest();

        // Bind the FormData object and the form element
        const formData = new FormData(form);

        // Define what happens on successful data submission
        xhr.addEventListener( "load", responseHandler);

        // Define what happens in case of error
        xhr.addEventListener( "error", function( event ) {
            // note: not sure when this gets called (server response 500 & 404 go to "load" listener above)
            alert('Unexpected: XHR error event fired');
        } );

        // Set up our request
        xhr.open(form.method, form.action);

        // prevent Spring Boot from rejecting us for CSRF concerns
        xhr.setRequestHeader(csrfParameterName, csrfToken);

        // let Spring Boot know we're sending JSON
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
*/