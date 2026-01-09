# TableCustom Component

`TableCustom` is a flexible Vue 3 component for rendering data tables with configurable columns, typed renderers, and support for arbitrary Vue nodes (VNodes) inside cells. It uses a lightweight Flexbox layout instead of `<table>` for maximum control over responsive designs.

---

## Features
- Column definitions with explicit `key`, `name`, `width`, and `type`
- Built‑in cell renderers: `name`, `level`, `status`
- Custom cells via VNodes (e.g., buttons, modals)
- Optional fixed-height, scrollable body
- Stable keys for efficient updates

---

## Props

### `headers` (required)
**Type:** `Array<{ key: string; name: string; width: string; type?: 'name' | 'level' | 'status' | string }>`

Defines the columns and how each cell is rendered.

- `key` — unique column id (also used to map row fields, e.g., `row[key]`).
- `name` — header label.
- `width` — CSS width for the column (e.g. `'55%'`, `'25%'`).
- `type` — built‑in renderers:
    - `name` → avatar placeholder + company name
    - `level` → numeric level badge
    - `status` → uploaded/verified logic (or a custom VNode override)
    - any other string → generic renderer (uses `row[key]`, supports VNodes)

**Example**
```js
const headers = [
  { key: 'name',   name: 'Clients',     width: '55%', type: 'name' },
  { key: 'level',  name: 'Level',       width: '20%', type: 'level' },
  { key: 'status', name: 'Scope 1 & 2', width: '25%', type: 'status' },
]
```

---

### `rows` (required)
**Type:**

```ts
type Row = {
  id?: string | number
  name: string
  level?: number
  uploaded?: boolean
  verified?: boolean
  custom?: VNode | VNode[]
  verifiedDate?: string
  [key: string]: unknown
  // You may add more fields; cells will read them via row[header.key]
}
```

Represents data lines. For the `status` column, the following rules apply:
- If `custom` is provided → the cell renders that VNode.
- Else if `uploaded` is true:
    - If `verified` is true → shows “Verified” and `verifiedDate` (if provided) with icons.
    - Else → shows “Uploaded”.
- Else → shows placeholder dashes.

**Example**
```js
const rows = [
  { id: 1, avatart: '/avatar-placeholder.png', name: 'Company A', level: 4, uploaded: true, verified: true, verifiedDate: '14/05/2025' },
  { id: 2, name: 'Company B', level: 3, uploaded: true, verified: false },
  { id: 3, name: 'Company C', level: 2, uploaded: false, verified: false },
]
```

---

### `fixedHeight`
**Type:** `boolean`  
When `true`, applies the `fixedHeight` class to the body wrapper so the rows area becomes scrollable. Ensure the CSS class is defined in `tableCustom.css`.

---

## Custom Cells (VNodes)
You can inject any Vue node in a cell using the field referenced by `header.key`. The component will detect VNodes via `isVNode` and mount them; otherwise it will print plain text.

**Example: action buttons**
```ts
import { h } from 'vue'
import Button from '@/components/button/Button.vue'

const actions = h('div', { class: 'actions' }, [
  h(Button, { variant: 'secondary' }, { default: () => 'Accept' }),
  h(Button, { variant: 'alert' }, { default: () => 'Decline' }),
])
```

```vue
<TableCustom
  :headers="[
    { key: 'name', name: 'Invitations', width: '50%', type: 'name' },
    { key: 'action', name: '', width: '50%', type: 'action' }  // generic type
  ]"
  :rows="[
    { id: 10, name: 'Company name', action: actions },
    { id: 11, name: 'Company name', action: actions }
  ]"
/>
```

**Example: “status” with a custom VNode override**
```ts
const reviewBtn = h(Button, { variant: 'secondary' }, { default: () => 'Review files' })
const rows = [
  { id: 21, name: 'Company X', level: 4, custom: reviewBtn }, // overrides status cell
]
```

---

## Usage Example
```vue
<TableCustom
  :headers="[
    { key: 'name',   name: 'Clients',     width: '55%', type: 'name' },
    { key: 'level',  name: 'Level',       width: '20%', type: 'level' },
    { key: 'status', name: 'Scope 1 & 2', width: '25%', type: 'status' }
  ]"
  :rows="[
    { id: 1, name: 'Company name', level: 4, uploaded: true, verified: true,  verifiedDate: '14/05/2025' },
    { id: 2, name: 'Company name', level: 4, uploaded: true, verified: false },
    { id: 3, name: 'Company name', level: 4, uploaded: false, verified: false }
  ]"
  :fixedHeight="true"
/>
```

---

## Accessibility Notes
- This component uses `<ul>`/`<li>` wrappers for layout flexibility. If you require semantic HTML tables for screen readers, consider creating a parallel `<table>`-based variant.
- Provide meaningful `name` values and ensure header widths sum close to `100%` to avoid overflow.

---

## Styling
The component uses the `tableCustom.css` file for styling. The container receives a dynamic CSS class based on its When `fixedHeight` is true, the wrapper receives the `.fixedHeight` extra class if the value of fixedHeight props is true.

---

## Tips
- Prefer stable row keys (`row.id`) for best update performance.
- You can extend columns by introducing new `type` strings and handling them in the template (or keep them generic and rely on `row[header.key]`).

