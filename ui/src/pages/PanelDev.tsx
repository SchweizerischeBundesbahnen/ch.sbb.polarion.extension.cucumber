import { useEffect, useMemo, useRef, useState } from 'react';
import { PageLayout, SearchableSelect } from '@sbb-polarion/react-sbb-polarion';
import { mountCucumberPanel } from '../formext/mount';
import type { PanelContext } from '../formext/types';
import { getProjectIdFromScope, getScope } from '../services/scope';
import { fetchWorkItems } from '../services/workitems';
import type { ProjectWorkItem } from '../services/workitems';

const HOST_ID = 'cucumber-edit-panel';

/**
 * Dev-only harness for the Cucumber Test work-item panel. It exercises the **real** form-extension
 * mount path (`mountCucumberPanel`) in `vite dev`: the panel is mounted inside a shadow root exactly
 * as in the Polarion editor, so the encapsulated styling, the petrel code editor and the
 * Edit / Validate / Save flow can be eyeballed and driven locally against a real work item.
 */
export default function PanelDev() {
  const projectId = getProjectIdFromScope(getScope());
  const [workItems, setWorkItems] = useState<ProjectWorkItem[]>([]);
  const [workItemId, setWorkItemId] = useState('');
  const [validateOnSave, setValidateOnSave] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const hostRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!projectId) return undefined;
    let cancelled = false;
    setError(null);
    fetchWorkItems(projectId)
      .then((list) => {
        if (!cancelled) setWorkItems(list);
      })
      .catch(() => {
        if (!cancelled) {
          setError('Could not load work items. Set VITE_BEARER_TOKEN in ui/.env.local and restart dev.');
        }
      });
    return () => {
      cancelled = true;
    };
  }, [projectId]);

  const context: PanelContext | undefined = useMemo(
    () => (workItemId ? { projectId, workItemId, fileName: `${workItemId}.feature`, validateOnSave } : undefined),
    [projectId, workItemId, validateOnSave],
  );

  useEffect(() => {
    if (!hostRef.current || !context) return undefined;
    // Re-mounts when the context changes; mountInShadow reuses the shadow root and clears it.
    const root = mountCucumberPanel(`#${HOST_ID}`, { context });
    return () => root?.unmount();
  }, [context]);

  return (
    <PageLayout title="Cucumber Test panel (dev harness)">
      <p className="landing-intro">
        Mounts the panel through the real <code>mountCucumberPanel</code> path - inside a shadow root with
        react-sbb-polarion&apos;s stylesheet plus the panel CSS injected into it. Pick a work item to feed the panel the
        context it reads from the form in Polarion, then drive Edit / Validate / Save.
      </p>

      {!projectId && (
        <div className="alert alert-error">
          Pick a project scope from the <a href="?">Overview</a> page first - work items are listed per project.
        </div>
      )}

      {projectId && (
        <>
          <div className="landing-scope">
            <label>Work item:</label>
            <SearchableSelect
              value={workItemId}
              onChange={setWorkItemId}
              options={workItems.map((w) => ({
                id: w.workItemId,
                name: w.title ? `${w.workItemId} - ${w.title}` : w.workItemId,
              }))}
              placeholder="Select…"
              allowEmpty
            />
          </div>
          <div className="landing-scope">
            <label>
              <input type="checkbox" checked={validateOnSave} onChange={(e) => setValidateOnSave(e.target.checked)} />{' '}
              Validate on save
            </label>
          </div>
          {error && <div className="alert alert-error">{error}</div>}
          <div id={HOST_ID} ref={hostRef}></div>
        </>
      )}
    </PageLayout>
  );
}
