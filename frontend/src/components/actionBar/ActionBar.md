# ActionBar Component

The `ActionBar` component is a layout utility that organizes actions and controls into two distinct areas: a start (left) section and an end (right) section. It is designed to make it easy to place primary and secondary actions in a consistent horizontal layout.

## Features
- Splits content into **left** and **right** sections.
- Uses **slots** for maximum flexibility in placing any type of component or content.
- Automatically applies layout styling through an external CSS file (`actionBar.css`).

## Slots

| Name          | Description                                                |
|---------------|------------------------------------------------------------|
| (default)     | Content displayed on the **left** side of the action bar.  |
| `right-side`  | Content displayed on the **right** side of the action bar. |

## Example Usage

```vue
<ActionBar>
  <!-- Left side content -->
  <Button variant="primary">Save</Button>

  <!-- Right side content -->
  <template #right-side>
    <Button variant="secondary">Cancel</Button>
  </template>
</ActionBar>
```

## Styling
The component imports its styles from `@/styles/actionBar.css`, which is scoped to this component.
