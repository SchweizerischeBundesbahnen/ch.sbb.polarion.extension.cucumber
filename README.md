[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.cucumber&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.cucumber)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.cucumber&metric=bugs)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.cucumber)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.cucumber&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.cucumber)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.cucumber&metric=coverage)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.cucumber)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.cucumber&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.cucumber)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.cucumber&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.cucumber)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.cucumber&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.cucumber)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.cucumber&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.cucumber)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.cucumber&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.cucumber)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.cucumber&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.cucumber)

# Cucumber Integration Extension for Polarion ALM

This Polarion extension is designed to integrate BDD process into Polarion.
It provides the almost same functionality as Xray plugin for Jira:
- Cucumber feature tracked as WorkItem attachment and can be edited using Gherkin editor
- Cucumber features can be downloaded as .zip file according to provided criteria
- Execution results can be imported as Test Run into Polarion

> [!IMPORTANT]
> Starting from version 2.0.0 only latest version of Polarion is supported.
> Right now it is Polarion 2512.

## Quick start

The latest version of the extension can be downloaded from the [releases page](../../releases/latest) and installed to Polarion instance without necessity to be compiled from the sources.
The extension should be copied to `<polarion_home>/polarion/extensions/ch.sbb.polarion.extension.cucumber/eclipse/plugins` and changes will take effect after Polarion restart.
> [!IMPORTANT]
> Don't forget to clear `<polarion_home>/data/workspace/.config` folder after extension installation/update to make it work properly.

## Build

This extension can be produced using maven:
```bash
mvn clean package
```

## Installation to Polarion

To install the extension to Polarion `ch.sbb.polarion.extension.cucumber-<version>.jar`
should be copied to `<polarion_home>/polarion/extensions/ch.sbb.polarion.extension.cucumber/eclipse/plugins`
It can be done manually or automated using maven build:
```bash
mvn clean install -P install-to-local-polarion
```
For automated installation with maven env variable `POLARION_HOME` should be defined and point to folder where Polarion is installed.

Changes only take effect after restart of Polarion.

## Polarion configuration

### Cucumber feature editor to appear on Test Case page

1. Open a project which Test Case pages should display the editor
2. On the top of the project's navigation pane click ⚙ (Actions) ➙ 🔧 Administration. Project's administration page will be opened.
3. On the administration's navigation pane select Work Items ➙ Form Configuration.
4. On the form configuration page you will see 2 sections: Form Filters and Form Layouts.
5. In the table of Form Layouts section find line Test Case and click 📝 Edit
6. In opened Form Layout Configuration editor find a line with code:
   ```xml
   …
   <field id="description"/>
   …
   ```
7. Insert following new line after it:
   ```xml
   …
   <extension id="cucumber" label="Cucumber Test"/>
   …
   ```
8. Save changes by clicking 💾 Save

### Cucumber feature editor to appear on the Live Doc sidebar

1. Open a project which the Live Doc sidebar should display the editor
2. On the top of the project's navigation pane click ⚙ (Actions) ➙ 🔧 Administration. Project's administration page will be opened.
3. On the administration's navigation pane select Documents & Pages ➙ Work Item Properties Sidebar.
4. In the opened Edit Project Configuration editor insert the following line:
   ```xml
   …
   <extension id="cucumber" label="Cucumber Test"/>
   …
   ```
5. Save changes by clicking 💾 Save

### Test Run's description to appear on UI

By default, Test Run's UI doesn't display its description (as a custom field). To enable displaying description following needs to be done:
1. Open a project which Test Run Templates needs to be modified.
2. In Navigation, click Test Runs. The Test Runs page opens. The top section of the page displays a table of existing Test Runs.
3. On the right hand side of the toolbar of the Test Runs page, click Manage Templates. The table of Test Runs is replaced by a table of Test Run Templates.
4. Select a template you want to modify.
5. On the toolbar of the detail pane, click ⚙ (Actions) ➙ Customize Test Run Page.
6. You will see a warning block containing text:
   > <b>This is a Test Run template</b>, you cannot execute this test. Please create a Test Run first using this template
7. Click ✎ (Pencil) to edit code of this block, on the right hand side of the page you will see following lines of code:
   ```velocity
   #if($testRun.fields().isTemplate().get())
     $widgetContext.renderWarning("<b>This is a Test Run template</b>, you cannot execute this test. Please create a Test Run first using this template")
   #end
   #if($testRun.fields().records().is().empty() && $testRun.fields().selectTestCasesBy().optionId().equals("manualSelection"))
     $widgetContext.renderInfo("Select the test cases you want to plan for execution by clicking Operations > Select Test Cases.")
   #end
   ```
8. Add new lines:
   ```velocity
   …
   #if(!$testRun.fields().description().is().empty())
     $testRun.fields.description.render().withText()
   #end
   ```
9. Save template by clicking 💾

### Custom fields: `Components`

1. Create an enum containing all acceptable `components` names for selected project:
    - go to `Project Administration -> Work Items -> Enumerations`
    - use Work Item Type: `--Unspecific--`, Enumeration: `Custom` + provide own name e.g. `components`
    - fill all possible values of this enum, e.g. `TA-Adapter`, `DailyTrainDataCalculator`, `ItisGateway`, etc
    - save enum configuration
2. Create a custom field for selected project:
    - go to `Project Administration -> Testing -> Test Run Custom Fields`
    - use ID: `components`, Name: `Components`, Type: Enum (+ select enum name created on step 1) and enable `'Multi'` checkbox
    - save configuration
3. Customize Test Run Report to show Components field:
    - Test Runs -> on the right hand side of the toolbar of the Test Runs page, click Manage Templates and select a template you want to modify
    - Place where it is needed widget "Script - Block" and put following code inside:
      ```velocity
      #if(!$testRun.fields().get("components").is().empty())
      <h2>Components</h2>
      $testRun.fields().get("components").render()
      #end
      ```
    - click 'Save'
    - Now 'Components' field would appear on detail page if the field was sent during importing test results

### Custom fields: `Test Plan`

1. Create a custom field for selected project :
    - go to `Project Administration -> Testing -> Test Run Custom Fields`
    - use ID: `plans`, Name: `Test Plan`, Type: `Rich Text (multi-line)`
    - save configuration
2. Customize Test Run Report to show `Test Plan` field:
    - Test Runs -> on the right hand side of the toolbar of the Test Runs page, click Manage Templates and select a template you want to modify
    - Place where it is needed widget "Script - Block" and put following code inside:
      ```velocity
      #if(!$testRun.fields().get("plans").is().empty())
      <h2>Test plan</h2>
      $testRun.fields().get("plans").render()
      #end
      ```
    - click 'Save'
    - Now 'Test Plan' field would appear on Test Run detail page if the field was sent by maven-plugin. Also if current project contains some plan(s) with the id equal to given value - the value will be transformaed into a link to certain plan.
3. Customize Plan to show links to Test Runs:
    - go to `Administration -> Plans -> Plan Custom Fields` and create custom field using ID: `testRunLinks`, Name: `Test Run Links`, Type: `Rich Text (multi-line)`
    - go to `Plans` -> select any plan and click ⚙ -> `Customize Plan Report -> Customize Shared Report`
    - Place where it is needed widget "Script - Block" and put following code inside:
      ```velocity
      #if(!$plan.fields().get("testRunLinks").is().empty())
      <h2>Test runs</h2>
      $plan.fields().get("testRunLinks").render()
      #end
      ```
    - click 'Save'
    - Now 'Test Runs' field should contain links to the Test Runs which had created references to current plan on step 2.


### Custom field: `Team-SBB`

1. Create an enum containing all acceptable team names for selected project:
    - go to `Administration -> Work Items -> Enumerations`
    - use Work Item Type: `--Unspecific--`, Enumeration: `Custom` + provide own name e.g. `team_sbb`
    - fill all possible values of this enum, e.g. `Adler`, `Otter`, `Orca`, etc
    - save enum configuration
2. Create a custom field which will keep the value (this step can be made in global configuration, but global fields will be visible only if the local configuration for test runs is deleted https://community.sw.siemens.com/s/question/0D54O00007Fh7iLSAR/do-global-test-run-custom-fields-work-locally):
    - go to `Administration -> Testing -> Test Run Custom Fields`
    - use ID: `team-sbb`, Name: `Team-SBB`, Type: `Enum` (+ select enum name created on step 1)
3. Customize Test Run Report to show Team-SBB field:
    - Test Runs -> on the right hand side of the toolbar of the Test Runs page, click Manage Templates and select a template you want to modify
    - Place where it is needed widget "Script - Block" and put following code inside:
      ```velocity
      #if(!$testRun.fields().get("teamSBB").is().empty())
      <h2>Team-SBB</h2>
      $testRun.fields().get("teamSBB").render()
      #end
      ```
    - click 'Save'
    - Now 'Team-SBB' field would appear on detail page if the field was sent by maven-plugin

## REST API
This extension provides REST API. OpenAPI Specification can be obtained [here](docs/openapi.json).
