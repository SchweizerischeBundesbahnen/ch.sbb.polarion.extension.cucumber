import { describe, expect, it } from 'vitest';
import { createFeatureCodeEditor, linesWithError } from '../src/formext/codeEditor';

// The vendored petrel editor wrapper. The editor itself is third-party and runs for real here; what
// is worth pinning is the configuration it gets and the line mapping the panel feeds it.

describe('linesWithError', () => {
  it('turns the parser 1-based lines into the 0-based ones the editor marks', () => {
    expect(linesWithError([{ line: 1 }, { line: 7 }])).toEqual([0, 6]);
  });

  it('ignores errors the parser could not locate', () => {
    expect(linesWithError([{ line: 2 }, {}, { line: undefined }])).toEqual([1]);
  });

  it('handles no errors at all', () => {
    expect(linesWithError(undefined)).toEqual([]);
    expect(linesWithError([])).toEqual([]);
  });
});

describe('createFeatureCodeEditor', () => {
  it('creates a read-only editor that highlights Gherkin', async () => {
    const host = document.createElement('div');
    document.body.appendChild(host);
    try {
      const editor = createFeatureCodeEditor(host);
      editor.setValue('Feature: login\n  Scenario: works\n');
      editor.update();

      expect(editor.readonly).toBe(true);
      expect(editor.getValue()).toContain('Scenario: works');
      // highlight.js ran: the Gherkin keywords carry hljs classes.
      expect(host.innerHTML).toContain('hljs');
    } finally {
      host.remove();
    }
  });
});
