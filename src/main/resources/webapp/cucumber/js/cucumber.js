function handleSaveFeature(projectId, workItemId, filename, validate) {
    if (validate === "false") {
        saveCucumberFeature(projectId, workItemId, filename);
    } else {
        validateFeature().then(() => saveCucumberFeature(projectId, workItemId, filename));
    }
}

function saveCucumberFeature(projectId, workItemId, filename) {
    document.getElementById("cancel-edit-feature-button").disabled = 'true';
    document.getElementById("save-feature-button").disabled = 'true';
    globalThis.cucumberFeatureCodeEditor.readonly = true;

    var xhr = new XMLHttpRequest();
    xhr.open('POST', '/polarion/cucumber/rest/internal/feature', true);
    xhr.setRequestHeader('Content-Type', 'application/json');
    xhr.onload = (_event) => {
        document.location.reload(true);
    };

    xhr.send(JSON.stringify(
        {
            'projectId': projectId,
            'workItemId': workItemId,
            'filename': filename,
            'content': globalThis.cucumberFeatureCodeEditor.getValue()
        }
    ));
}

const handleEditFeature = () => {
    document.getElementById("edit-feature-button").disabled = true;
    document.getElementById("cancel-edit-feature-button").disabled = false;
    document.getElementById("save-feature-button").disabled = false;
    document.getElementById("validate-feature-button").disabled = false;
    globalThis.cucumberFeatureCodeEditor.readonly = false;
}

const handleValidateFeature = () => {
    return validateFeature();
}

const validateFeature = () => {
    return new Promise((resolve, reject) => {
        const xhr = new XMLHttpRequest();
        xhr.open('POST', '/polarion/cucumber/rest/internal/cucumber/validate', false);
        xhr.setRequestHeader('Content-Type', 'text/plain');
        xhr.onload = () => {
            let result = false;
            const validationResultSpan = document.getElementById("feature-validation-result");
            globalThis.cucumberFeatureCodeEditor.setLinesWithError([]);
            if (xhr.status === 200) {
                const response = JSON.parse(xhr.responseText);
                if (response && response.result === "valid") {
                    result = true;
                    validationResultSpan.innerText = "Cucumber feature is valid!";
                    validationResultSpan.className = "validation-pass";
                } else {
                    let errorMessage = "";
                    const linesWithError = [];
                    response.errors.forEach(error => {
                        if (errorMessage.length > 0) {
                            errorMessage += "<br>";
                        }
                        errorMessage += error.message;

                        const match = error.message.match(/\((?<linenumber>\d+):\d+\):/);
                        if (match && match.groups) {
                            linesWithError.push(Number.parseInt(match.groups.linenumber) - 1);
                        }
                    });
                    globalThis.cucumberFeatureCodeEditor.setLinesWithError(linesWithError);
                    validationResultSpan.innerHTML = errorMessage;
                    validationResultSpan.className = "validation-fail";
                }
            } else {
                validationResultSpan.innerText = "Error during validation, please contact system administrator for details";
                validationResultSpan.className = "validation-fail";
            }
            validationResultSpan.style.display = "block";
            globalThis.cucumberFeatureCodeEditor.update();
            if (result) {
                resolve();
            } else {
                reject();
            }
        };
        xhr.send(globalThis.cucumberFeatureCodeEditor.getValue());
    });
}

const handleCancelEditFeature = () => {
    if (confirm("Are you sure you want to cancel editing and revert changes?")) {
        document.getElementById("cancel-edit-feature-button").disabled = true;
        document.getElementById("save-feature-button").disabled = true;
        document.getElementById("edit-feature-button").disabled = false;
        document.getElementById("validate-feature-button").disabled = true;
        document.getElementById("feature-validation-result").style.display = "none";
        globalThis.cucumberFeatureCodeEditor.readonly = true;
        globalThis.cucumberFeatureCodeEditor.setValue(document.getElementById('cucumberFeatureCodeEditorOriginalContent').innerText);
    }
}

(function () {
    const imagesToUpdate = document.getElementsByClassName("append-build-number");
    if (imagesToUpdate && imagesToUpdate.length > 0) {
        const imagesWithBuildNumber = document.getElementsByClassName("polarion-ToolbarButton-Icon");
        if (imagesWithBuildNumber && imagesWithBuildNumber.length > 0) {
            const srcWithBuildNumber = imagesWithBuildNumber[0].getAttribute("src");
            const buildParamIndex = srcWithBuildNumber.indexOf("?buildId=");
            if (buildParamIndex >= 0) {
                const buildParam = srcWithBuildNumber.slice(buildParamIndex);
                if (buildParam) {
                    for (let i = 0; i < imagesToUpdate.length; i++) {
                        imagesToUpdate[i].setAttribute("src", imagesToUpdate[i].getAttribute("src").concat(buildParam));
                        imagesToUpdate[i].classList.remove("append-build-number");
                    }
                }
            }
        }
    }
})();
