<#import "macros/page-macro.ftl" as c/>

<#-- from https://www.smashingmagazine.com/2018/01/drag-drop-file-uploader-vanilla-js/ -->

<#assign hdr>
    <style>
        #dropArea {
            width: 300px;
            height: 300px;
            padding: 20px;
            border-style: dashed;
            border-color: black;
            border-width: 1px;
            background-color: #dddddd;
        }

        #dropArea.highlight {
            border-style: solid;
            border-width: 2px;
        }
    </style>
    <script type="application/ecmascript">

        window.onload = function() {
            let dropArea = document.getElementById('dropArea');

            ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
                dropArea.addEventListener(eventName, preventDefaults, false)
            });

            ['dragenter', 'dragover'].forEach(eventName => {
                dropArea.addEventListener(eventName, highlight, false)
            });

            ['dragleave', 'drop'].forEach(eventName => {
                dropArea.addEventListener(eventName, unhighlight, false)
            });

            dropArea.addEventListener('drop', handleDrop, false)
        }

        function highlight(e) {
            dropArea.classList.add('highlight')
        }

        function unhighlight(e) {
            dropArea.classList.remove('highlight')
        }

        function preventDefaults (e) {
            e.preventDefault()
            e.stopPropagation()
        }

        function handleDrop(e) {
            let dt = e.dataTransfer
            let files = dt.files

            handleFiles(files)
        }

        function handleFiles(files) {
            ([...files]).forEach(uploadFile)
        }


        function handleEvent(e) {
            var log = document.querySelector('.event-log');
            log.textContent = log.textContent + "event: " + e.type + " " + e.loaded + " bytes transferred\n";
        }

        function uploadFile(file) {
            var url = '/asset'
            var xhr = new XMLHttpRequest()
            var formData = new FormData()

            xhr.open('POST', url, true)

            xhr.addEventListener('readystatechange', function(e) {
                if (xhr.readyState == 4 && xhr.status == 200) {
                    // Done. Inform the user
                }
                else if (xhr.readyState == 4 && xhr.status != 200) {
                    // Error. Inform the user
                }
            })

            xhr.addEventListener('progress', handleEvent);

            formData.append('file', file)
            formData.append('${_csrf.parameterName}', '${_csrf.token}')
            xhr.send(formData)


        }

    </script>
</#assign>

<@c.page title="Upload Asset" additionalHeadContent=hdr>
    <form action="/uploadAsset" method="post">

    </form>
    <div id="dropArea">
        Drop yer ebooks here.
    </div>
    <textarea readonly class="event-log"></textarea>
</@c.page>