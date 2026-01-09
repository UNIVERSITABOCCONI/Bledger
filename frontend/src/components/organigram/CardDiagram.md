# CardDiagram Component

The `CardDiagram` component displays a company name and supports inline editing through a dropdown menu. It provides three modes: **simple**, **editable**, and **editing**.

---

## Props

| Name           | Type                                      | Default | Description                                             |
|----------------|-------------------------------------------|---------|---------------------------------------------------------|
| `companyName`  | `string`                                  | —       | The displayed company name.                             |
| `modelValue`   | `string \| null`                          | —       | The current value for the dropdown (in editing mode).   |
| `editable`     | `boolean`                                 | `false` | Enables the edit button.                                |
| `editing`      | `boolean`                                 | `false` | Puts the component into editing mode with a dropdown.   |
| `disabled`     | `boolean`                                 | `false` | Disables all interactions and applies a disabled style. |
| `options`      | `Array<{ label: string; value: string }>` | —       | Dropdown options.                                       |
| `placeholder`  | `string`                                  | —       | Placeholder for the dropdown when no value is selected. |

---

## Events

| Event                 | Payload     | Description                                                |
|-----------------------|-------------|------------------------------------------------------------|
| `update:modelValue`   | `string`    | Emitted when the dropdown value changes.                   |
| `edit`                | `void`      | Emitted when the edit button is clicked.                   |
| `cancel`              | `void`      | Emitted when the cancel button is clicked in editing mode. |
| `save`                | `string`    | Emitted automatically when the dropdown value changes.     |

---

## Accessibility

- **ARIA labels** are added to edit, cancel, and save buttons for screen reader support.
- The `Dropdown` component includes an `aria-label` based on the placeholder or a default value.
- The `disabled` prop ensures keyboard users cannot interact when disabled.

---

## Modes

### 1. Simple View
`editable = false`  
Displays the company name without edit functionality.

### 2. Editable View
`editable = true` and `editing = false`  
Displays the company name with an edit button (`IconEdit`). Clicking triggers the `edit` event.

### 3. Editing Mode
`editing = true`  
Displays:
- A `Dropdown` for selecting a new company.
- A cancel button (`IconClose`).
- A save button (`✔`) that emits the `save` event.

---

## Example Usage

```vue
<CardDiagram
  companyName="OpenAI Inc."
  v-model="selectedCompany"
  :editable="true"
  :editing="isEditing"
  :options="companyOptions"
  placeholder="Select a company"
  @edit="isEditing = true"
  @cancel="isEditing = false"
  @save="handleSave"
/>
```

```ts
import { ref } from 'vue';

export default {
  setup() {
    const selectedCompany = ref<string | null>(null);
    const isEditing = ref(false);

    const companyOptions = [
      { label: 'OpenAI Inc.', value: 'openai' },
      { label: 'Example Corp.', value: 'example' },
    ];

    function handleSave(value: string | null) {
      console.log('Saved company:', value);
      isEditing.value = false;
    }

    return { selectedCompany, isEditing, companyOptions, handleSave };
  }
}
```
