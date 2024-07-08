import CodeEditor from './petrel/CodeEditor.js'
import GherkinAutoComplete from './petrel/GherkinAutoComplete.js'

import hljs from './highlight/core.min.js'
import gherkin from './highlight/gherkin.js'

hljs.registerLanguage('gherkin', gherkin);

const content = document.getElementById('cucumberFeatureCodeEditorOriginalContent').innerText;

const element = document.getElementById('cucumberFeatureCodeEditor');
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
