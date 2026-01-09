# Button Component

A reusable button component with visual variants, optional leading/trailing icon slots, and a typed click event.

---

## Props

| Prop           | Type                                                                                              | Default     | Description                                                                                                                       |
|----------------|---------------------------------------------------------------------------------------------------|-------------|-----------------------------------------------------------------------------------------------------------------------------------|
| `variant`      | `'primary' \\| 'secondary' \\| 'alert' \\| 'negative' \\| 'icon' \\| 'secondary-dark' \\| 'link'` | `'primary'` | Visual style of the button.                                                                                                       |
| `icon`         | `boolean`                                                                                         | `false`     | If `true`, the component expects an icon in the named slot `icon`.                                                                |
| `iconPosition` | `'left' \\| 'right'`                                                                              | `'left'`    | Where to place the icon slot relative to the label.                                                                               |
| `disabled`     | `boolean`                                                                                         | `false`     | Disables the button.                                                                                                              |
| `onclick`      | `() => void`                                                                                      | `undefined` | Optional native click handler passed to the underlying `<button>` element (in addition to the component's emitted `click` event). |

> **Note:** The component also emits a `click` event (see _Events_). You can use either `@click` on the component or the `onclick` prop to attach handlers.

---

## Slots

| Slot | Description |
|------|-------------|
| _(default)_ | The button label/content. |
| `icon` | An icon (component or element) rendered at the left or right of the label depending on `iconPosition`. |

---

## Events

| Event | Payload | Description |
|-------|---------|-------------|
| `click` | `MouseEvent` | Emitted when the button is clicked. |

Usage examples:
```vue
<!-- Basic -->
<Button @click="onSubmit">Save</Button>

<!-- With icon on the left -->
<Button variant="secondary" icon icon-position="left" @click="onDownload">
  <template #icon><IconDownload /></template>
  Download
</Button>

<!-- As a link-style button -->
<Button variant="link" @click="goToDocs">Docs</Button>

<!-- Disabled -->
<Button variant="alert" :disabled="true">Delete</Button>
```

---

## Styling

The component computes a BEM-like class based on variant:

- button--primary (default)
- button--secondary
- button--alert
- button--negative
- button--icon
- button--link
- button--secondary-dark

Base styles are loaded from: @/styles/button.css.

---

## Accessibility

- The underlying element is a native `<button>`, which supports keyboard activation and ARIA by default. 
- Provide meaningful text in the default slot (avoid icon-only buttons without an accessible label). 
- When `variant="icon"` and there is no visible text, consider adding aria-label on the component usage.

