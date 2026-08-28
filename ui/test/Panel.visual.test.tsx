import { afterEach, describe, expect, it, vi } from 'vitest';
import { cleanup, render } from 'vitest-browser-react';
import { page } from 'vitest/browser';
import CucumberPanel from '../src/formext/CucumberPanel';
import type { PanelContext } from '../src/formext/types';
import { installFetchMock, jsonResponse } from './mockFetch';
import { settleBeforeCapture, settleLayout } from './visualHelpers';

// Docker-only snapshots of the Cucumber Test panel: the read-only state it opens in, and the state
// after a failed validation - the two looks the panel's own CSS (petrel / highlight.js / cucumber)
// is responsible for.

const CONTEXT: PanelContext = {
  projectId: 'proj',
  workItemId: 'WI-1',
  fileName: 'WI-1.feature',
  validateOnSave: false,
};

const FEATURE = [
  'Feature: Login',
  '',
  '  Scenario: A known user signs in',
  '    Given a registered user',
  '    When they sign in with valid credentials',
  '    Then they reach the start page',
  '',
].join('\n');

const shoot = async (name: string) => {
  const panel = document.querySelector('#cucumber-edit-panel') as HTMLElement;
  await settleLayout();
  await page.viewport(1000, Math.ceil(panel.scrollHeight) + 40);
  await settleBeforeCapture();
  await expect(page.elementLocator(panel)).toMatchScreenshot(name);
};

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe.skipIf(!__PIXEL_REFERENCES__)('Cucumber panel visual', () => {
  it('read-only, with the feature highlighted', async () => {
    installFetchMock([{ method: 'GET', match: /\/feature\//, json: { content: FEATURE } }]);
    render(<CucumberPanel context={CONTEXT} onSaved={() => {}} />);

    await vi.waitFor(() =>
      expect(document.querySelector('#cucumberFeatureCodeEditor')!.textContent).toContain('Scenario'),
    );
    await shoot('panel-readonly');
  });

  it('editing, with the parser errors listed', async () => {
    installFetchMock([
      { method: 'GET', match: /\/feature\//, json: { content: FEATURE } },
      {
        method: 'POST',
        match: /\/cucumber\/validate$/,
        respond: () =>
          jsonResponse({
            result: 'invalid',
            errors: [
              { message: "(3:3): expected: #EOF, #Language, got 'Scenario'", line: 3 },
              { message: '(5:5): inconsistent cell count within the table', line: 5 },
            ],
          }),
      },
    ]);
    render(<CucumberPanel context={CONTEXT} onSaved={() => {}} />);
    await vi.waitFor(() =>
      expect(document.querySelector('#cucumberFeatureCodeEditor')!.textContent).toContain('Scenario'),
    );

    document.querySelector<HTMLButtonElement>('#edit-feature-button')!.click();
    await vi.waitFor(() =>
      expect(document.querySelector<HTMLButtonElement>('#validate-feature-button')!.disabled).toBe(false),
    );
    document.querySelector<HTMLButtonElement>('#validate-feature-button')!.click();

    await vi.waitFor(() => expect(document.querySelector('#feature-validation-result')).not.toBeNull());
    await shoot('panel-validation-errors');
  });
});
