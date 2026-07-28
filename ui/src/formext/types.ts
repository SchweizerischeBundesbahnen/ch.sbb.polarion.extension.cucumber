/**
 * What the Cucumber Test panel needs to talk to the REST API. In Polarion this is computed
 * server-side by CucumberIntegrationFormExtension and embedded in the fragment (data-* attributes);
 * the dev harness builds it from a picked work item.
 */
export interface PanelContext {
  projectId: string;
  workItemId: string;
  /** Name of the attachment holding the feature, always `<workItemId>.feature`. */
  fileName: string;
  /** Whether Save must validate the feature first (the `validateOnSave` form-layout attribute). */
  validateOnSave: boolean;
}

/** One problem the Gherkin parser reported. */
export interface ValidationError {
  message: string;
  /** 1-based line, as the parser counts them; absent when the error has no location. */
  line?: number;
  column?: number;
}

/** What `POST /internal/cucumber/validate` answers. */
export interface ValidationResult {
  result: string;
  errors?: ValidationError[];
}
