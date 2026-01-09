# NetworkController Component

The `NetworkController` component provides a UI for controlling a network diagram, including navigation via directional arrows and action buttons for adding and removing suppliers.

## Features

- **Directional pad** (Up, Down, Left, Right) for navigating within the network.
- **Keyboard accessibility** with roving tabindex pattern for better navigation.
- **ARIA support** for screen readers.
- **Customizable actions** for adding and deleting suppliers.
- **Edit network** button.

## Props

| Prop       | Type                                          | Default   | Description                                                |
|------------|-----------------------------------------------|-----------|------------------------------------------------------------|
| `active`   | `'up' \| 'down' \| 'left' \| 'right' \| null` | `null`    | Direction currently active (can be controlled externally). |
| `disabled` | `boolean`                                     | `false`   | Disables all controls.                                     |

## Emits

| Event           | Payload       | Description                                                             |
|-----------------|---------------|-------------------------------------------------------------------------|
| `move`          | `Dir`         | Emitted when a directional button is activated (via click or keyboard). |
| `add-supplier`  | —             | Emitted when the "Add supplier" button is clicked.                      |
| `delete`        | —             | Emitted when the "Delete" button is clicked.                            |
| `update:active` | `Dir \| null` | Emitted when the active direction changes.                              |
| `edit-network`  | —             | Emitted when the "Edit network" button is clicked.                      |

## Keyboard Interaction

- **Arrow keys**: Move focus between directional buttons and set active direction.
- **Enter / Space**: Trigger the action of the focused button.
- **Tab**: Moves focus out of the pad (only the currently active direction is tabbable).

## Accessibility

- Uses `aria-pressed` on direction buttons to indicate active state.
- Buttons have `aria-label` and `.sr-only` text for screen reader descriptions.
- Directional pad uses `role="group"` with an accessible name.

## Example Usage

```vue
<NetworkController
  :active="activeDir"
  :disabled="isDisabled"
  @move="handleMove"
  @add-supplier="handleAddSupplier"
  @delete="handleDelete"
  @update:active="activeDir = $event"
  @edit-network="openEditModal"
/>
```

## Styling

The component imports its styles from:

```css
@import '@/styles/networkController.css'
```
