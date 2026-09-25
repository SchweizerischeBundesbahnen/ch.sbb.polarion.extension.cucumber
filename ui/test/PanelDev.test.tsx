import { pageViolations } from '@sbb-polarion/react-sbb-polarion/testing';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { cleanup, render } from 'vitest-browser-react';
import App from '../src/App';
import { installFetchMock } from './mockFetch';

// The dev harness for the Cucumber Test panel (?feature=panel). Not opened in Polarion and excluded from
// coverage; checked here for its own form controls. The panel it mounts has its own tests.

const origUrl = window.location.pathname + window.location.search;

const WORK_ITEMS = { data: [{ id: 'elibrary/EL-1', attributes: { title: 'Login works' } }] };

async function renderPanelDev() {
  installFetchMock([
    { method: 'GET', match: /\/polarion\/rest\/v1\/projects\/elibrary\/workitems/, json: WORK_ITEMS },
    { method: 'GET', match: /\/feature\//, json: { content: 'Feature: login' } },
  ]);
  window.history.replaceState({}, '', '?feature=panel&scope=project/elibrary/');
  render(<App />);
  await vi.waitFor(() => expect(document.querySelector('.landing-scope .sd-trigger')).not.toBeNull());
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
  window.history.replaceState({}, '', origUrl);
});

describe('Panel dev harness, accessibility', () => {
  it('has no WCAG A/AA violations', async () => {
    await renderPanelDev();
    expect(await pageViolations()).toEqual([]);
  });

  // axe accepts a placeholder as a name, so it would not catch a control that loses its label.
  it('names the work item picker and the checkbox after their labels', async () => {
    await renderPanelDev();
    expect(document.querySelector('.landing-scope .sd-trigger')).toHaveAccessibleName('Work item:');
    expect(document.querySelector('.landing-scope input[type="checkbox"]')).toHaveAccessibleName('Validate on save');
  });

  it('has no WCAG A/AA violations without a project scope', async () => {
    installFetchMock([]);
    window.history.replaceState({}, '', '?feature=panel');
    render(<App />);
    await vi.waitFor(() => expect(document.querySelector('.alert-error')).not.toBeNull());
    expect(await pageViolations()).toEqual([]);
  });
});
