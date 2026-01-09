
# Modal Component

The `Modal` component is a controlled modal dialog built with Vue 3 and `<Teleport>`.  
It supports `v-model` for open/close state management, variant-based styling, custom icons, and event handling.

## Features
- **Controlled via `v-model`**: The modal’s visibility is fully managed from the parent.
- **Variants**: `confirm`, `alert`, `error`, `simple`.
- **Customizable Content**: Supports named and default slots for flexible layouts.
- **Custom Icons**: Pass your own icon via the `customIcon` prop.
- **Footer Controls**: Configurable close/confirm buttons with optional right-side text.
- **Keyboard Accessibility**: Closes on `Esc` if `closeOnEsc` is `true`.

## Props

| Prop                 | Type                                          | Default     | Description                           |
|----------------------|-----------------------------------------------|-------------|---------------------------------------|
| `v-model:open`       | `boolean`                                     | `false`     | Controls modal visibility             |
| `variant`            | `'confirm' \| 'alert' \| 'error' \| 'simple'` | `'confirm'` | Defines the modal's visual style      |
| `modalTitle`         | `string`                                      | `undefined` | Title text displayed at the top       |
| `ctaCloseLabel`      | `string`                                      | `undefined` | Label for the close button            |
| `ctaConfirmLabel`    | `string`                                      | `undefined` | Label for the confirm button          |
| `showFooter`         | `boolean`                                     | `true`      | Toggles the footer visibility         |
| `closeOnEsc`         | `boolean`                                     | `true`      | Enables closing on ESC key            |
| `customIcon`         | `VNode`                                       | `undefined` | Custom icon in header                 |
| `size`               | `'xl'`                                        | `undefined` | Sets modal size variant               |
| `textSideButton`     | `string`                                      | `undefined` | Small text next to the confirm button |
| `disableRightButton` | `boolean`                                     | `false`     | Disables confirm button               |

## Events

| Event      | Payload    | Description                                    |
|------------|------------|------------------------------------------------|
| `confirm`  | `void`     | Fired when confirm button is clicked           |
| `close`    | `void`     | Fired when close button or backdrop is clicked |

## Slots

| Slot       | Description                |
|------------|----------------------------|
| `default`  | Main modal content         |
| `icon`     | Custom icon slot in header |

## Example Usage

```vue
<template>
  <Modal
    v-model:open="isModalOpen"
    variant="alert"
    modalTitle="Delete Item"
    ctaCloseLabel="Cancel"
    ctaConfirmLabel="Delete"
    textSideButton="This action cannot be undone"
    @confirm="handleDelete"
    @close="handleCancel"
  >
    <p>Are you sure you want to delete this item?</p>
  </Modal>

  <Button @click="isModalOpen = true">Open Modal</Button>
</template>

<script setup>
import { ref } from 'vue'
import Modal from '@/components/modal/Modal.vue'

const isModalOpen = ref(false)

function handleDelete() {
  console.log("Item deleted")
}

function handleCancel() {
  console.log("Cancelled")
}
</script>
```

## Notes
- The modal uses `<Teleport to="body">` to ensure it is rendered outside the parent’s layout context.
- When using `v-model:open`, always manage the open/close state in the parent.
- For fully custom modals without built-in footer controls, you can hide the footer using `showFooter="false"` and provide your own actions.
