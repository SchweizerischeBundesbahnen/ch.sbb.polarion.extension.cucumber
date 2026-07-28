// Thin typed wrapper over the vendored petrel code editor (src/vendor), reproducing the setup the
// legacy cucumber.js did (readonly, 4-space tabs, Gherkin highlighting and autocompletion) but
// mounting into an element handed in by React instead of one looked up by id.
import hljsCore from '../vendor/highlight/core.min.js';
import hljsGherkin from '../vendor/highlight/gherkin.js';
import CodeEditor from '../vendor/petrel/CodeEditor.js';
import GherkinAutoComplete from '../vendor/petrel/GherkinAutoComplete.js';

/** The subset of the petrel CodeEditor API the panel uses. */
export interface FeatureCodeEditor {
  setValue(value: string): void;
  getValue(): string;
  setLinesWithError(lines: number[]): void;
  update(): void;
  readonly: boolean;
}

interface Highlighter {
  registerLanguage(name: string, language: unknown): void;
  highlight(code: string, options: { language: string; ignoreIllegals: boolean }): { value: string };
}

const hljs = hljsCore as unknown as Highlighter;

// Register the Gherkin grammar once (idempotent - highlight.js overwrites an existing registration).
hljs.registerLanguage('gherkin', hljsGherkin);

/**
 * Creates a petrel code editor inside `element`, configured for Gherkin exactly as the legacy panel:
 * readonly on creation, 4-space tabs, highlight.js Gherkin highlighting and keyword autocompletion.
 */
export function createFeatureCodeEditor(element: HTMLElement): FeatureCodeEditor {
  const editor = new CodeEditor(element, { readonly: true, tabSize: 4 });
  editor.setHighlighter((code: string) => hljs.highlight(code, { language: 'gherkin', ignoreIllegals: true }).value);
  editor.setAutoCompleteHandler(new GherkinAutoComplete());
  editor.setValue('');
  editor.create();
  return editor as FeatureCodeEditor;
}

/**
 * The 0-based lines the editor should mark, from what the parser reported.
 *
 * The parser hands back a line number per error; the legacy panel ignored it and dug the number out
 * of the message text with a regular expression instead. Errors without a location simply mark
 * nothing.
 */
export function linesWithError(errors: { line?: number }[] | undefined): number[] {
  return (errors ?? [])
    .map((error) => error.line)
    .filter((line): line is number => typeof line === 'number')
    .map((line) => line - 1);
}
