
# Tooltip Component

The `Tooltip` component provides additional contextual information when users hover, focus, or interact with an element. It is highly customizable, allowing you to specify tooltip content via props or slots, and position it around the trigger element.

## Props

| Prop       | Type                                           | Default  | Description                                                              |
|------------|------------------------------------------------|----------|--------------------------------------------------------------------------|
| `content`  | `string`                                       | `''`     | The default tooltip text to display when the `content` slot is not used. |
| `position` | `'top'` \| `'bottom'` \| `'left'` \| `'right'` | `'top'`  | Position of the tooltip relative to the trigger element.                 |

## Slots

| Slot Name   | Description |
|-------------|-------------|
| _default_   | The element that will trigger the tooltip on hover or focus. |
| `content`   | Optional slot to override the tooltip text with custom markup or components. |

## Accessibility
- The tooltip container has `tabindex="0"` to make it focusable via keyboard navigation.
- `role="tooltip"` is applied to the tooltip element for screen readers.
- Tooltip visibility is triggered by both mouse events (`mouseenter`/`mouseleave`) and keyboard events (`focus`/`blur`).

## Example Usage

### Basic Usage with `content` Prop
```vue
<Tooltip content="This is a tooltip" position="bottom">
  <button>Hover over me</button>
</Tooltip>
```

### Using the `content` Slot
```vue
<Tooltip position="right">
  <template #default>
    <span>Hover here</span>
  </template>
  <template #content>
    <strong>Custom HTML tooltip content</strong>
  </template>
</Tooltip>
```

## Notes
- If both `content` prop and `content` slot are provided, the slot takes precedence.
- Use the `position` prop to adjust tooltip placement according to your design needs.
