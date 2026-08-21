import type { SendRequest } from '@sbb-polarion/react-sbb-polarion';
import type { PanelContext, ValidationResult } from './types';

/**
 * The panel's REST calls, ported from the legacy cucumber.js (raw XMLHttpRequest) onto the
 * extension's `useRemote().sendRequest`.
 */

/** Throws an Error carrying the server's message when the response is not OK. */
async function ensureOk(response: Response): Promise<void> {
  if (response.ok) {
    return;
  }
  const text = await response.text().catch(() => '');
  let message = text;
  try {
    const parsed = JSON.parse(text) as { message?: string; errorMessage?: string };
    message = parsed.message ?? parsed.errorMessage ?? text;
  } catch {
    // not JSON - use the raw text
  }
  throw new Error(message || `Request failed (HTTP ${response.status})`);
}

/**
 * Reads the feature attached to the work item.
 *
 * The legacy panel never called this: the server inlined the feature into the rendered fragment, so
 * every form load carried the whole file through an HTML attribute. Fetching it here keeps the
 * fragment small and means the panel shows what is stored rather than what was stored at render time.
 */
export async function loadFeature(sendRequest: SendRequest, ctx: PanelContext): Promise<string> {
  const response = await sendRequest({
    method: 'GET',
    url: `/feature/${encodeURIComponent(ctx.projectId)}/${encodeURIComponent(ctx.workItemId)}`,
  });
  await ensureOk(response);
  const feature = (await response.json()) as { content?: string };
  return feature.content ?? '';
}

/** Writes the feature back as an attachment named `<workItemId>.feature`. */
export async function saveFeature(sendRequest: SendRequest, ctx: PanelContext, content: string): Promise<void> {
  const response = await sendRequest({
    method: 'POST',
    url: '/feature',
    contentType: 'application/json',
    body: JSON.stringify({
      projectId: ctx.projectId,
      workItemId: ctx.workItemId,
      filename: ctx.fileName,
      content,
    }),
  });
  await ensureOk(response);
}

/** Runs the Gherkin parser over the content and returns what it found. */
export async function validateFeature(sendRequest: SendRequest, content: string): Promise<ValidationResult> {
  const response = await sendRequest({
    method: 'POST',
    url: '/cucumber/validate',
    contentType: 'text/plain',
    body: content,
  });
  await ensureOk(response);
  return (await response.json()) as ValidationResult;
}
