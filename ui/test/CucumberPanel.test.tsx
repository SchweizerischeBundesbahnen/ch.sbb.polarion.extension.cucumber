import { afterEach, describe, expect, it, vi } from 'vitest';
import { cleanup, render } from 'vitest-browser-react';
import CucumberPanel from '../src/formext/CucumberPanel';
import type { PanelContext } from '../src/formext/types';
import { installFetchMock, jsonResponse } from './mockFetch';

// Behavior of the Cucumber Test panel, ported from the legacy cucumber.js. Rendered directly with an
// explicit context (the same prop mount.tsx reads off the fragment). REST is mocked at the fetch
// boundary; the vendored petrel editor runs for real in Chromium.

const CONTEXT: PanelContext = {
  projectId: 'proj',
  workItemId: 'WI-1',
  fileName: 'WI-1.feature',
  validateOnSave: false,
};

const FEATURE = 'Feature: login\n  Scenario: works\n    Given a user\n';

const button = (id: string) => document.querySelector<HTMLButtonElement>(`#${id}`)!;
const editorText = () => document.querySelector('#cucumberFeatureCodeEditor')!.textContent ?? '';
const validationResult = () => document.querySelector('#feature-validation-result');

const featureRoutes = (content = FEATURE) => [{ method: 'GET', match: /\/feature\/proj\/WI-1$/, json: { content } }];

async function mount(routes: Parameters<typeof installFetchMock>[0] = [], context = CONTEXT, onSaved = () => {}) {
  installFetchMock([...routes, ...featureRoutes()]);
  render(<CucumberPanel context={context} onSaved={onSaved} />);
  await vi.waitFor(() => expect(document.querySelector('#cucumber-edit-panel')).not.toBeNull());
}

/** Clicks Edit and waits for the controls it unlocks - a click on a still-disabled button does nothing. */
async function startEditing() {
  await vi.waitFor(() => expect(editorText()).toContain('Scenario: works'));
  button('edit-feature-button').click();
  await vi.waitFor(() => expect(button('save-feature-button').disabled).toBe(false));
}

/** Answer the confirmation dialog the panel renders in place of the former window.confirm. */
async function answerDialog(label: 'OK' | 'Cancel') {
  await vi.waitFor(() => expect(document.querySelector('.rsp-modal')).not.toBeNull());
  const target = Array.from(document.querySelectorAll<HTMLButtonElement>('.rsp-modal-footer .sbb-btn')).find(
    (b) => (b.textContent ?? '').trim() === label,
  );
  if (!target) throw new Error(`dialog button "${label}" not found`);
  target.click();
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe('Cucumber Test panel', () => {
  it('loads the stored feature into the editor, read-only', async () => {
    await mount();

    await vi.waitFor(() => expect(editorText()).toContain('Scenario: works'));
    expect(button('edit-feature-button').disabled).toBe(false);
    expect(button('save-feature-button').disabled).toBe(true);
    expect(button('validate-feature-button').disabled).toBe(true);
    expect(button('cancel-edit-feature-button').disabled).toBe(true);
  });

  it('reports a feature it could not load instead of showing an empty editor', async () => {
    installFetchMock([
      { method: 'GET', match: /\/feature\/proj\/WI-1$/, respond: () => jsonResponse({ message: 'gone' }, 500) },
    ]);
    render(<CucumberPanel context={CONTEXT} />);

    await vi.waitFor(() => expect(document.querySelector('#feature-error')!.textContent).toContain('gone'));
  });

  it('unlocks the editing controls on Edit', async () => {
    await mount();
    await vi.waitFor(() => expect(editorText()).toContain('Scenario: works'));

    button('edit-feature-button').click();

    await vi.waitFor(() => expect(button('save-feature-button').disabled).toBe(false));
    expect(button('validate-feature-button').disabled).toBe(false);
    expect(button('cancel-edit-feature-button').disabled).toBe(false);
    expect(button('edit-feature-button').disabled).toBe(true);
  });

  it('says so when the parser accepts the feature', async () => {
    await mount([{ method: 'POST', match: /\/cucumber\/validate$/, json: { result: 'valid' } }]);
    await startEditing();

    button('validate-feature-button').click();

    await vi.waitFor(() => expect(validationResult()!.textContent).toContain('valid'));
    expect(validationResult()!.className).toBe('validation-pass');
  });

  it('lists every parser error and marks the lines they point at', async () => {
    await mount([
      {
        method: 'POST',
        match: /\/cucumber\/validate$/,
        json: {
          result: 'invalid',
          errors: [
            { message: "expected: #Language, got 'oops'", line: 2 },
            { message: 'inconsistent cell count', line: 5 },
            { message: 'no location for this one' },
          ],
        },
      },
    ]);
    await startEditing();

    button('validate-feature-button').click();

    await vi.waitFor(() => expect(validationResult()).not.toBeNull());
    expect(validationResult()!.className).toBe('validation-fail');
    const text = validationResult()!.textContent!;
    expect(text).toContain("expected: #Language, got 'oops'");
    expect(text).toContain('inconsistent cell count');
    expect(text).toContain('no location for this one');
  });

  it('reports a validation call that failed outright', async () => {
    await mount([{ method: 'POST', match: /\/cucumber\/validate$/, respond: () => jsonResponse({}, 500) }]);
    await startEditing();

    button('validate-feature-button').click();

    await vi.waitFor(() => expect(validationResult()!.textContent).toContain('Error during validation'));
  });

  it('saves the feature and hands over to the caller', async () => {
    let saved: unknown;
    const onSaved = vi.fn();
    await mount(
      [
        {
          method: 'POST',
          match: /\/feature$/,
          respond: (_url, init) => {
            saved = JSON.parse(String(init?.body));
            return jsonResponse({});
          },
        },
      ],
      CONTEXT,
      onSaved,
    );
    await startEditing();

    button('save-feature-button').click();

    await vi.waitFor(() => expect(onSaved).toHaveBeenCalled());
    expect(saved).toMatchObject({ projectId: 'proj', workItemId: 'WI-1', filename: 'WI-1.feature' });
  });

  it('validates before saving when the form asks it to, and saves nothing when invalid', async () => {
    const onSaved = vi.fn();
    let savePosted = false;
    await mount(
      [
        { method: 'POST', match: /\/cucumber\/validate$/, json: { result: 'invalid', errors: [{ message: 'nope' }] } },
        {
          method: 'POST',
          match: /\/feature$/,
          respond: () => {
            savePosted = true;
            return jsonResponse({});
          },
        },
      ],
      { ...CONTEXT, validateOnSave: true },
      onSaved,
    );
    await startEditing();

    button('save-feature-button').click();

    await vi.waitFor(() => expect(validationResult()!.textContent).toContain('nope'));
    expect(savePosted).toBe(false);
    expect(onSaved).not.toHaveBeenCalled();
    // Still editing, so the user can fix it.
    expect(button('save-feature-button').disabled).toBe(false);
  });

  it('stays editable and explains itself when saving fails', async () => {
    await mount([{ method: 'POST', match: /\/feature$/, respond: () => jsonResponse({ message: 'read-only' }, 403) }]);
    await startEditing();

    button('save-feature-button').click();

    await vi.waitFor(() => expect(document.querySelector('#feature-error')!.textContent).toContain('read-only'));
    expect(button('save-feature-button').disabled).toBe(false);
  });

  it('restores the stored feature when the cancel is confirmed', async () => {
    await mount();
    await startEditing();

    button('cancel-edit-feature-button').click();
    await answerDialog('OK');

    await vi.waitFor(() => expect(button('edit-feature-button').disabled).toBe(false));
    expect(editorText()).toContain('Scenario: works');
    expect(button('save-feature-button').disabled).toBe(true);
  });

  it('keeps editing when the cancel is dismissed', async () => {
    await mount();
    await startEditing();

    button('cancel-edit-feature-button').click();
    await answerDialog('Cancel');

    expect(button('edit-feature-button').disabled).toBe(true);
    expect(button('save-feature-button').disabled).toBe(false);
  });
});
