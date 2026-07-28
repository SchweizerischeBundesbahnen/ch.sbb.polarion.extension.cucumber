import { afterEach, describe, expect, it, vi } from 'vitest';
import { mountCucumberPanel } from '../src/formext/mount';
import { installFetchMock } from './mockFetch';

// The form-extension mount glue: mountCucumberPanel reads the context off the host's data-*
// attributes and mounts the panel inside a shadow root (shadowMount). Exercised against a real
// Chromium shadow root.

let host: HTMLDivElement | null = null;

function makeHost(dataset: Record<string, string> = {}): HTMLDivElement {
  const el = document.createElement('div');
  el.id = 'cucumber-edit-panel';
  el.dataset.projectId = 'proj';
  el.dataset.workItemId = 'WI-1';
  el.dataset.fileName = 'WI-1.feature';
  el.dataset.validateOnSave = 'true';
  Object.assign(el.dataset, dataset);
  document.body.appendChild(el);
  return el;
}

const panelIn = (shadow: ShadowRoot) =>
  vi.waitFor(() => {
    const el = shadow.querySelector('#cucumber-edit-panel');
    expect(el).not.toBeNull();
    return el!;
  });

afterEach(() => {
  host?.remove();
  host = null;
  vi.unstubAllGlobals();
});

describe('mountCucumberPanel', () => {
  it('returns undefined and logs when the target is missing', () => {
    const err = vi.spyOn(console, 'error').mockImplementation(() => {});

    expect(mountCucumberPanel('#does-not-exist')).toBeUndefined();

    expect(err).toHaveBeenCalled();
    err.mockRestore();
  });

  it('mounts the panel into a shadow root and fetches the feature named by the attributes', async () => {
    const requested: string[] = [];
    installFetchMock([
      {
        method: 'GET',
        match: /\/feature\//,
        respond: (url) => {
          requested.push(url);
          return new Response(JSON.stringify({ content: 'Feature: mounted' }), {
            headers: { 'Content-Type': 'application/json' },
          });
        },
      },
    ]);
    host = makeHost();

    const root = mountCucumberPanel('#cucumber-edit-panel');

    expect(root).toBeDefined();
    expect(host.shadowRoot).not.toBeNull();
    await panelIn(host.shadowRoot!);
    await vi.waitFor(() => expect(requested.some((url) => url.includes('/feature/proj/WI-1'))).toBe(true));
    // The panel's markup is inside the shadow root, not in the page.
    expect(document.querySelector('#cucumberFeatureCodeEditor')).toBeNull();
  });

  it('derives the file name when the attribute is missing', async () => {
    let posted: { filename?: string } = {};
    installFetchMock([
      { method: 'GET', match: /\/feature\//, json: { content: '' } },
      {
        method: 'POST',
        match: /\/feature$/,
        respond: (_url, init) => {
          posted = JSON.parse(String(init?.body));
          return new Response('{}', { headers: { 'Content-Type': 'application/json' } });
        },
      },
    ]);
    // validateOnSave off: this is about the derived name, not the validation step.
    host = makeHost({ fileName: '', validateOnSave: 'false' });

    // Without this the panel's default post-save page reload would reload the test iframe.
    mountCucumberPanel('#cucumber-edit-panel', { onSaved: () => {} });
    const panel = await panelIn(host.shadowRoot!);

    panel.querySelector<HTMLButtonElement>('#edit-feature-button')!.click();
    await vi.waitFor(() =>
      expect(panel.querySelector<HTMLButtonElement>('#save-feature-button')!.disabled).toBe(false),
    );
    panel.querySelector<HTMLButtonElement>('#save-feature-button')!.click();

    await vi.waitFor(() => expect(posted.filename).toBe('WI-1.feature'));
  });

  it('takes an explicit context, which is how the dev harness drives it', async () => {
    const requested: string[] = [];
    installFetchMock([
      {
        method: 'GET',
        match: /\/feature\//,
        respond: (url) => {
          requested.push(url);
          return new Response('{"content":""}', { headers: { 'Content-Type': 'application/json' } });
        },
      },
    ]);
    host = makeHost();

    mountCucumberPanel('#cucumber-edit-panel', {
      context: { projectId: 'other', workItemId: 'WI-9', fileName: 'WI-9.feature', validateOnSave: false },
    });
    await panelIn(host.shadowRoot!);

    await vi.waitFor(() => expect(requested.some((url) => url.includes('/feature/other/WI-9'))).toBe(true));
  });
});
