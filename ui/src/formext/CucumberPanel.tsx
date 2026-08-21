import { useEffect, useRef, useState } from 'react';
import { useConfirm } from '@sbb-polarion/react-sbb-polarion';
import useRemote from '../services/useRemote';
import { createFeatureCodeEditor, linesWithError } from './codeEditor';
import type { FeatureCodeEditor } from './codeEditor';
import { loadFeature, saveFeature, validateFeature } from './restApi';
import type { PanelContext } from './types';
import validateIcon from './validate.svg';

interface CucumberPanelProps {
  /** Work item context. Must be stable across renders (memoize in the parent). */
  context: PanelContext;
  /** Called after a successful save. Defaults to reloading the page, which is what the legacy panel
   *  did: the reloaded form re-renders with the stored feature. Overridden in dev/tests. */
  onSaved?: () => void;
}

interface ValidationState {
  /** Already HTML-free: one line per parser error. */
  messages: string[];
  kind: 'pass' | 'fail';
}

/**
 * React port of the legacy Cucumber Test work-item panel (layout/form.html + cucumber.js). The shell
 * (Edit / Validate / Save / Cancel and the validation result) is React; the code editor itself is the
 * vendored petrel editor mounted into the container below (see codeEditor.ts). The markup keeps the
 * legacy ids and classes so the panel's own CSS (petrel.css / highlightjs.css / cucumber.css, injected
 * into the shadow root) styles it unchanged.
 */
export default function CucumberPanel({ context, onSaved }: CucumberPanelProps) {
  const { sendRequest } = useRemote();
  const { confirm, confirmDialog } = useConfirm();

  const [editing, setEditing] = useState(false);
  const [saving, setSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [validation, setValidation] = useState<ValidationState | null>(null);
  /** What is stored, so Cancel can restore it without another round trip. */
  const [persisted, setPersisted] = useState('');

  const editorHostRef = useRef<HTMLDivElement>(null);
  const editorRef = useRef<FeatureCodeEditor | null>(null);

  // Create the editor once, then fill it with what is attached to the work item.
  useEffect(() => {
    if (!editorHostRef.current || editorRef.current) return;
    editorRef.current = createFeatureCodeEditor(editorHostRef.current);
  }, []);

  useEffect(() => {
    let cancelled = false;
    loadFeature(sendRequest, context)
      .then((content) => {
        if (cancelled) return;
        setPersisted(content);
        editorRef.current?.setValue(content);
        editorRef.current?.update();
      })
      .catch((e: Error) => {
        if (!cancelled) setErrorMessage(e.message || 'Could not load the feature.');
      });
    return () => {
      cancelled = true;
    };
  }, [sendRequest, context]);

  const setReadonly = (readonly: boolean) => {
    const editor = editorRef.current;
    if (editor) {
      editor.readonly = readonly;
      editor.update();
    }
  };

  /** Runs the parser and shows what it said. Returns whether the feature is valid. */
  const runValidation = async (): Promise<boolean> => {
    const content = editorRef.current?.getValue() ?? '';
    editorRef.current?.setLinesWithError([]);
    try {
      const result = await validateFeature(sendRequest, content);
      if (result.result === 'valid') {
        setValidation({ messages: ['Cucumber feature is valid!'], kind: 'pass' });
        editorRef.current?.update();
        return true;
      }
      editorRef.current?.setLinesWithError(linesWithError(result.errors));
      setValidation({ messages: (result.errors ?? []).map((error) => error.message), kind: 'fail' });
      editorRef.current?.update();
      return false;
    } catch {
      setValidation({
        messages: ['Error during validation, please contact system administrator for details'],
        kind: 'fail',
      });
      editorRef.current?.update();
      return false;
    }
  };

  const handleEdit = () => {
    setEditing(true);
    setErrorMessage(null);
    setReadonly(false);
  };

  const handleSave = async () => {
    if (context.validateOnSave && !(await runValidation())) {
      return;
    }
    setSaving(true);
    setReadonly(true);
    try {
      await saveFeature(sendRequest, context, editorRef.current?.getValue() ?? '');
      // The legacy panel reloaded the page here so the form picks up the stored attachment.
      (onSaved ?? (() => window.location.reload()))();
    } catch (e) {
      setSaving(false);
      setEditing(true);
      setReadonly(false);
      setErrorMessage((e as Error).message || 'Could not save the feature.');
    }
  };

  const handleCancel = async () => {
    if (!(await confirm('Are you sure you want to cancel editing and revert changes?'))) {
      return;
    }
    setEditing(false);
    setValidation(null);
    setErrorMessage(null);
    editorRef.current?.setValue(persisted);
    editorRef.current?.setLinesWithError([]);
    setReadonly(true);
  };

  return (
    <div id="cucumber-edit-panel">
      <div className="editor-buttons">
        <button type="button" id="edit-feature-button" disabled={editing} onClick={handleEdit}>
          <span className="sbb-icon-edit" role="img" aria-label="Edit"></span>Edit
        </button>
        <button type="button" className="divider">
          &nbsp;
        </button>
        <button
          type="button"
          id="validate-feature-button"
          disabled={!editing || saving}
          onClick={() => void runValidation()}
        >
          <img alt="" src={validateIcon} />
          Validate
        </button>
        <button type="button" id="save-feature-button" disabled={!editing || saving} onClick={() => void handleSave()}>
          <span className="sbb-icon-save" role="img" aria-label="Save"></span>Save
        </button>
        <button
          type="button"
          id="cancel-edit-feature-button"
          disabled={!editing || saving}
          onClick={() => void handleCancel()}
        >
          <span className="sbb-icon-cancel" role="img" aria-label="Cancel"></span>Cancel
        </button>
      </div>

      <div className="validation-result">
        {validation && (
          <span
            id="feature-validation-result"
            className={validation.kind === 'pass' ? 'validation-pass' : 'validation-fail'}
          >
            {validation.messages.map((message, index) => (
              <span key={`${index}-${message}`}>
                {index > 0 && <br />}
                {message}
              </span>
            ))}
          </span>
        )}
        {errorMessage && (
          <span id="feature-error" className="validation-fail">
            {errorMessage}
          </span>
        )}
      </div>

      <div className="editor-wrapper">
        <div id="cucumberFeatureCodeEditor" ref={editorHostRef}></div>
      </div>

      {confirmDialog}
    </div>
  );
}
