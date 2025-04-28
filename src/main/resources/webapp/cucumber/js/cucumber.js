import ExtensionContext from "/polarion/pdf-exporter/ui/generic/js/modules/ExtensionContext.js";
import CodeEditor from './petrel/CodeEditor.js'
import GherkinAutoComplete from './petrel/GherkinAutoComplete.js'

import hljs from './highlight/core.min.js'
import gherkin from './highlight/gherkin.js'

const ctx = new ExtensionContext({
    extension: 'cucumber',
    rootComponentSelector: '#cucumber-edit-panel',
});

hljs.registerLanguage('gherkin', gherkin);

function handleSaveFeature() {
    if (this.dataset.validate === "false") {
        saveCucumberFeature(this.dataset.projectId, this.dataset.workItemId, this.dataset.filename);
    } else {
        validateFeature().then(() => saveCucumberFeature(this.dataset.projectId, this.dataset.workItemId, this.dataset.filename));
    }
}

function saveCucumberFeature(projectId, workItemId, filename) {
    ctx.getElementById("cancel-edit-feature-button").disabled = 'true';
    ctx.getElementById("save-feature-button").disabled = 'true';
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

const validateFeature = () => {
    return new Promise((resolve, reject) => {
        const xhr = new XMLHttpRequest();
        xhr.open('POST', '/polarion/cucumber/rest/internal/cucumber/validate', false);
        xhr.setRequestHeader('Content-Type', 'text/plain');
        xhr.onload = () => {
            let result = false;
            const validationResultSpan = ctx.getElementById("feature-validation-result");
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

function initEditor() {
    const content = ctx.getElementById('cucumberFeatureCodeEditorOriginalContent').innerText;

    const element = ctx.getElementById('cucumberFeatureCodeEditor');
    const codeEditor = new CodeEditor(element, {
        readonly: true,
        tabSize: 4
    });

    codeEditor.setHighlighter(code => hljs.highlight(code, {language: 'gherkin', ignoreIllegals: true}).value);
    codeEditor.setAutoCompleteHandler(new GherkinAutoComplete());
    codeEditor.setValue(content);
    codeEditor.create();

    // make it global
    globalThis.cucumberFeatureCodeEditor = codeEditor;
}

function updateImages() {
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
}

export function initPanel() {
    initEditor();
    updateImages();

    ctx.onClick(
        'edit-feature-button', () => {
            ctx.getElementById("edit-feature-button").disabled = true;
            ctx.getElementById("cancel-edit-feature-button").disabled = false;
            ctx.getElementById("save-feature-button").disabled = false;
            ctx.getElementById("validate-feature-button").disabled = false;
            globalThis.cucumberFeatureCodeEditor.readonly = false;
        },
        'validate-feature-button', () => validateFeature(),
        'cancel-edit-feature-button', () => {
            if (confirm("Are you sure you want to cancel editing and revert changes?")) {
                ctx.getElementById("cancel-edit-feature-button").disabled = true;
                ctx.getElementById("save-feature-button").disabled = true;
                ctx.getElementById("edit-feature-button").disabled = false;
                ctx.getElementById("validate-feature-button").disabled = true;
                ctx.getElementById("feature-validation-result").style.display = "none";
                globalThis.cucumberFeatureCodeEditor.readonly = true;
                globalThis.cucumberFeatureCodeEditor.setValue(document.getElementById('cucumberFeatureCodeEditorOriginalContent').innerText);
            }
        },
        'save-feature-button', handleSaveFeature
    );
}
