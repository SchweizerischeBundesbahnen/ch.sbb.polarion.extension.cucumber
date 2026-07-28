import { afterEach, describe, expect, it, vi } from 'vitest';
import { loadFeature, saveFeature, validateFeature } from '../src/formext/restApi';
import type { PanelContext } from '../src/formext/types';
import { installFetchMock, jsonResponse } from './mockFetch';

// The panel's REST layer, ported from the legacy cucumber.js. The flows are covered end to end in
// CucumberPanel.test.tsx; this pins the request shapes and the error handling.

const CONTEXT: PanelContext = {
  projectId: 'my project',
  workItemId: 'WI 1',
  fileName: 'WI 1.feature',
  validateOnSave: false,
};

const sendRequest: Parameters<typeof loadFeature>[0] = ({ method, url, body, contentType }) => {
  const headers: Record<string, string> = {};
  if (contentType) headers['Content-Type'] = contentType;
  return fetch(`/polarion/cucumber/rest/internal${url}`, { method, headers, body });
};

afterEach(() => vi.unstubAllGlobals());

describe('loadFeature', () => {
  it('url-encodes the path segments so ids with spaces survive', async () => {
    const urls: string[] = [];
    installFetchMock([
      {
        method: 'GET',
        match: /\/feature\//,
        respond: (url) => {
          urls.push(url);
          return jsonResponse({ content: 'Feature: x' });
        },
      },
    ]);

    expect(await loadFeature(sendRequest, CONTEXT)).toBe('Feature: x');
    expect(urls[0]).toContain('/feature/my%20project/WI%201');
  });

  it('treats a feature with no content as empty rather than undefined', async () => {
    installFetchMock([{ method: 'GET', match: /\/feature\//, json: {} }]);

    expect(await loadFeature(sendRequest, CONTEXT)).toBe('');
  });

  it("carries the server's message on a failure", async () => {
    installFetchMock([
      { method: 'GET', match: /\/feature\//, respond: () => jsonResponse({ message: 'no such work item' }, 404) },
    ]);

    await expect(loadFeature(sendRequest, CONTEXT)).rejects.toThrow('no such work item');
  });
});

describe('saveFeature', () => {
  it('posts the feature as the endpoint expects it', async () => {
    let body: Record<string, string> = {};
    installFetchMock([
      {
        method: 'POST',
        match: /\/feature$/,
        respond: (_url, init) => {
          body = JSON.parse(String(init?.body));
          return jsonResponse({});
        },
      },
    ]);

    await saveFeature(sendRequest, CONTEXT, 'Feature: saved');

    expect(body).toEqual({
      projectId: 'my project',
      workItemId: 'WI 1',
      filename: 'WI 1.feature',
      content: 'Feature: saved',
    });
  });

  it('falls back to the status when the failure carries no message', async () => {
    installFetchMock([{ method: 'POST', match: /\/feature$/, respond: () => new Response('', { status: 500 }) }]);

    await expect(saveFeature(sendRequest, CONTEXT, '')).rejects.toThrow('HTTP 500');
  });
});

describe('validateFeature', () => {
  it('sends the feature as plain text and returns what the parser said', async () => {
    let sent: string | undefined;
    let contentType: string | null = null;
    installFetchMock([
      {
        method: 'POST',
        match: /\/cucumber\/validate$/,
        respond: (_url, init) => {
          sent = String(init?.body);
          contentType = new Headers(init?.headers).get('Content-Type');
          return jsonResponse({ result: 'invalid', errors: [{ message: 'bad', line: 3 }] });
        },
      },
    ]);

    const result = await validateFeature(sendRequest, 'Feature: x');

    expect(sent).toBe('Feature: x');
    expect(contentType).toBe('text/plain');
    expect(result.errors?.[0]).toEqual({ message: 'bad', line: 3 });
  });
});
