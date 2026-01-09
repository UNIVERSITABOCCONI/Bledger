# Table Component Documentation

## Overview
The **Table** component displays a structured breakdown of emissions by scope and category.
It supports headers, sections with subtotals, individual rows, and a final total row.

This component is fully accessible, using semantic HTML table elements (`<table>`, `<thead>`, `<tbody>`, `<tfoot>`)
and appropriate `scope` attributes for improved screen reader support.

---

## Props

| Prop Name     | Type                                                                                                            | Required | Default              | Description                                                                                                   |
|---------------|-----------------------------------------------------------------------------------------------------------------|----------|----------------------|---------------------------------------------------------------------------------------------------------------|
| `sections`    | `Array<{ title: string; total: number; unit: string; rows: { label: string; value: number; unit: string }[] }>` | No       | `[]`                 | The list of sections, each with a title, total, unit, and an array of rows containing label, value, and unit. |
| `total`       | `number`                                                                                                        | No       | `0`                  | The overall total value for the table footer.                                                                 |
| `unit`        | `string`                                                                                                        | No       | `""`                 | The unit to be displayed in the footer total cell.                                                            |
| `headers`     | `string[]`                                                                                                      | No       | `["Label", "Value"]` | The table column headers.                                                                                     |
| `showHeader`  | `boolean`                                                                                                       | No       | `true`               | Whether to display the table header row.                                                                      |
| `fixedHeight` | `boolean`                                                                                                       | No       | `false`              | Whether to fix the table height with a scrollable body.                                                       |

---

## Slots
This component does not use custom slots, as it renders based on the provided data props.

---

## Accessibility Features
- Uses semantic HTML table structure.
- `scope="col"` for column headers.
- `scope="row"` for row headers.
- All numeric values are right-aligned for readability.
- `<small>` tags are used for units to improve clarity without affecting accessibility.

---

## Example Usage

```vue
<Table
  :headers="['Category', 'Emissions']"
  :sections="[
    {
      title: 'Scope 1',
      total: 120.5,
      unit: 'tCO2e',
      rows: [
        { label: 'Fuel combustion', value: 80.2, unit: 'tCO2e' },
        { label: 'Company vehicles', value: 40.3, unit: 'tCO2e' }
      ]
    },
    {
      title: 'Scope 2',
      total: 300.0,
      unit: 'tCO2e',
      rows: [
        { label: 'Purchased electricity', value: 300.0, unit: 'tCO2e' }
      ]
    }
  ]"
  :total="420.5"
  unit="tCO2e"
  :showHeader="true"
/>
```

---

## Styling
The table styling is managed in `@/styles/table.css`. You can override or extend it for customization.
