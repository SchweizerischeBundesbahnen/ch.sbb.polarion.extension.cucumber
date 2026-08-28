import { afterEach, describe, expect, it, vi } from 'vitest';
import { cleanup, render } from 'vitest-browser-react';
import { page } from 'vitest/browser';
import App from '../src/App';
import { installFetchMock } from './mockFetch';
import { settleBeforeCapture, settleLayout } from './visualHelpers';

// Docker-only snapshot of the User Guide page. It renders the build-generated article, so what is
// pinned is the page frame and the markdown styling around it - both come from the shared library and
// nothing else in this app would notice them changing.

const origUrl = window.location.pathname + window.location.search;

const ARTICLE =
  '<h1>User Guide</h1><p>How to run Cucumber tests from Polarion.</p><h2>Test runs</h2>' +
  '<ul><li>Pick a template</li><li>Start the run</li></ul><pre><code>mvn verify</code></pre>';

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
  window.history.replaceState({}, '', origUrl);
});

describe.skipIf(!__PIXEL_REFERENCES__)('User Guide page visual', () => {
  it('the generated article rendered', async () => {
    installFetchMock([
      { method: 'GET', match: /\/user-guide$/, respond: () => new Response(ARTICLE, { status: 200 }) },
    ]);
    window.history.replaceState({}, '', '?feature=user-guide&embedded=true');
    render(<App />);

    await vi.waitFor(() => expect(document.querySelector('article.markdown-body')).not.toBeNull());
    const app = document.querySelector('.app') as HTMLElement;
    await settleLayout();
    await page.viewport(1280, Math.ceil(app.scrollHeight) + 40);
    await settleBeforeCapture();
    await expect(page.elementLocator(app)).toMatchScreenshot('user-guide-loaded');
  });
});
