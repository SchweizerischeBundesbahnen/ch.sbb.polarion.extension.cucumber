# cucumber-app (React UI)

The React app for the Cucumber extension, built with Vite and consuming the shared
`@sbb-polarion/react-sbb-polarion` (RSP) component library. One bundle serves every
surface; the page is chosen by the `?feature=<id>` query parameter (feature routing).

Surfaces:

- **`about`** - the admin About page (RSP `About`), wired into Polarion by `hivemodule.xml`.
- **`user-guide`** - the admin User Guide page (RSP `UserGuide`), rendering the build-generated
  article from `USER_GUIDE.md`.
- **`panel` (dev)** - a `vite dev` harness for the Cucumber Test work-item panel. In Polarion the
  panel is not an admin page: it is a form-extension fragment (`CucumberIntegrationFormExtension`,
  template `layout/form.html`) that imports the fixed-name bundle `assets/cucumberPanel.js` and calls
  `mountCucumberPanel(...)`, mounting the panel inside a shadow root on the work item form.

## Local development

```bash
cd ui
cp .env.local.template .env.local     # then edit VITE_BASE_URL / VITE_BEARER_TOKEN
npm install
npm run dev                           # http://localhost:5173/
```

- `http://localhost:5173/` - landing stub: project-scope picker + feature links.
- `http://localhost:5173/?feature=about&scope=project/<id>/` - the About page for a project scope.
- `http://localhost:5173/?feature=user-guide&scope=project/<id>/` - the User Guide page.
- `http://localhost:5173/?feature=panel&scope=project/<id>/` - the panel harness; pick a work item to
  feed the panel the context it reads from the form's data-* attributes in Polarion.

`VITE_BEARER_TOKEN` switches REST calls to the token-authenticated `/api` endpoints (no session
needed); without it, calls hit the session-based `/internal` endpoints.

## Build

```bash
npm run build        # SPA build, then the form-extension lib build (assets/cucumberPanel.js)
```

The full Maven build runs this automatically and copies `ui/dist/app` into the `cucumber-app`
webapp resources.

## Tests, coverage, lint

```bash
npm run test                  # Vitest browser mode (behavior; visual snapshots are Docker-only)
npm run test:docker           # full suite (behavior + visual) in the pinned Playwright image
npm run test:update:docker    # regenerate visual reference PNGs (Docker/Linux only)
npm run test:coverage         # behavior-only coverage (istanbul, 80% gate)
npm run test:coverage:docker  # authoritative full-suite coverage (what pre-commit runs)
npm run lint
npm run format:check
```

`src/vendor/` holds the vendored petrel code editor (with its Gherkin autocompletion) and
highlight.js with the Gherkin grammar, kept verbatim from the legacy webapp; it is excluded from lint,
formatting and coverage.

### Running the tests

**One command, locally and in CI: `npm run test:coverage:docker`.** It runs the full suite (behavior +
visual regression) plus the 80% istanbul coverage gate inside the pinned Playwright Docker image, which
is what the Maven `test` phase and the pre-commit hook execute. Docker must be running.

```bash
npm run test:coverage:docker   # the canonical run: full suite + coverage gate, in the pinned image
npm run test:coverage          # fast local loop: behavior only + the gate, no Docker, no pixels
npm run test:update:docker     # regenerate the committed reference PNGs after an intentional UI change
```

> `npm run test:coverage:full` is the inner command the Docker wrapper invokes. Run outside a container
> it is green, but it proves less than it looks: the reference screenshots are pixel-locked to the
> pinned image, so the visual suites detect that they are not in the reference environment and **skip
> themselves** rather than failing on the host's font metrics. It therefore reports the behavior suite
> and the coverage gate only - which is exactly what the `-DjsTestsNoDocker` Maven profile needs on a
> Docker-less host. To check the screenshots, use `test:coverage:docker`.
