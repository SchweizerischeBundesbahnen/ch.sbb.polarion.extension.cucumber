import { createRoot } from 'react-dom/client';
import type { Root } from 'react-dom/client';
import CucumberPanel from './CucumberPanel';
import panelStyle from './cucumber.css?inline';
import highlightStyle from './highlightjs.css?inline';
import petrelStyle from './petrel.css?inline';
import { mountInShadow } from './shadowMount';
import type { PanelContext } from './types';

/**
 * Entry point for the Cucumber Test work-item panel, built by Vite into a fixed-name module
 * (`assets/cucumberPanel.js`; the Vite input key `cucumberPanel` sets the output name). The
 * server-rendered form-extension fragment (layout/form.html, emitted by
 * CucumberIntegrationFormExtension) dynamically imports this module and calls
 * `mountCucumberPanel("#cucumber-edit-panel")`.
 *
 * The panel is mounted inside a **shadow root** on the fragment div, so its styles are fully
 * encapsulated on the shared editor page (see shadowMount.ts). The extension's own panel CSS
 * (petrel.css / highlightjs.css / cucumber.css) is bundled via `?inline` and injected into the shadow
 * alongside react-sbb-polarion's bundled stylesheet.
 */

interface MountOptions {
  /** Explicit context (dev harness / tests). In Polarion it is read from the host's data-* attributes. */
  context?: PanelContext;
  /** Overrides what happens after a save. Polarion wants the page reload the panel does by default. */
  onSaved?: () => void;
}

function readContext(host: HTMLElement): PanelContext {
  const d = host.dataset;
  const workItemId = d.workItemId ?? '';
  return {
    projectId: d.projectId ?? '',
    workItemId,
    // The server sends it as well, but the name is derived, so fall back rather than render a broken panel.
    fileName: d.fileName || `${workItemId}.feature`,
    validateOnSave: d.validateOnSave === 'true',
  };
}

export function mountCucumberPanel(selector: string, options: MountOptions = {}): Root | undefined {
  const host = document.querySelector<HTMLElement>(selector);
  if (!host) {
    console.error(`cucumber: panel mount target "${selector}" not found.`);
    return undefined;
  }
  const container = mountInShadow(host, {
    containerClassName: 'sbb-ui',
    styleTexts: [petrelStyle, highlightStyle, panelStyle],
  });
  const root = createRoot(container);
  root.render(<CucumberPanel context={options.context ?? readContext(host)} onSaved={options.onSaved} />);
  // Returned so the dev harness can unmount; the Polarion fragment ignores it.
  return root;
}
